/*
 * NISAccountMapper.java
 *
 * Created on May 25, 2004, 2:25 PM
 */

package gov.bnl.gums.account;

import gov.bnl.gums.GUMS;
import gov.bnl.gums.configuration.Configuration;

import java.util.*;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchResult;

import org.apache.commons.logging.*;

/** 
 * Maps a user to a local account based on the CN of the certificate and the
 * gecos field in the NIS/YP database. The mapping can't be perfect, but contains
 * a series of heuristics that solve up to 90% of the cases, depending on how
 * the NIS database itself is kept.
 * <p>
 * It's suggested not to use this policy by itself, but to have it part of a 
 * CompositeAccountMapper in which a ManualAccountMapper comes first. This allows
 * to override those user mapping that are not satisfying.
 *
 * @author Jay Packard
 */
public class LdapAccountMapper extends AccountMapper {
    static private Log log = LogFactory.getLog(LdapAccountMapper.class);
	static public String getTypeStatic() {
		return "ldap";
	}
    private String jndiLdapUrl = "";
	private String dnField = "description";
	private String accountField = "uid";
    
    public LdapAccountMapper() {
    	super();
    }
    
    public LdapAccountMapper(Configuration configuration) {
    	super(configuration);
    }
    
    public LdapAccountMapper(Configuration configuration, String name) {
    	super(configuration, name);
    }
    
    public AccountMapper clone(Configuration configuration) {
    	LdapAccountMapper accountMapper = new LdapAccountMapper(configuration, new String(getName()));
    	accountMapper.setDescription(new String(getDescription()));
    	accountMapper.setJndiLdapUrl(new String(jndiLdapUrl));
    	accountMapper.setDnField(new String(dnField));
    	accountMapper.setAccountField(new String(accountField));
    	return accountMapper;
    }
    
    public String getAccountField() {
        return accountField;
    }
    
    public String getDnField() {
        return dnField;
    }
    
    public String getJndiLdapUrl() {
        return jndiLdapUrl;
    }
    
    public String getType() {
		return "ldap";
	}
    
    public String mapUser(String userDN, boolean createIfDoesNotExist) {
    	Properties jndiProperties = retrieveJndiProperties();
    	String userDNWithSubject = "subject="+userDN;
        int nTries = 5;
        int i = 0;
        for (; i < nTries; i++) {
            log.debug("Attempt " + i + " to retrieve map at '" + jndiLdapUrl + "'");
            try {
                DirContext jndiCtx = new InitialDirContext(jndiProperties);
                NamingEnumeration nisMap = jndiCtx.search("ou=People", "(cn=*)", null);
                log.debug("Server responded");
                while (nisMap.hasMore()) {
                    SearchResult res = (SearchResult) nisMap.next();
                    Attributes atts = res.getAttributes();
                    Attribute dnAtt = atts.get(dnField);
                    if (dnAtt != null) {
                        String dn = dnAtt.get().toString();
                        if (userDN.equals(dn) || userDNWithSubject.equals(dn)) {
	                        String account = (String) atts.get(accountField).get();
	                        log.debug("Found account '" + account + "' for DN '" + userDN + "'");
	                        return account;
                        }
                    }
                }
                jndiCtx.close();
            } catch (javax.naming.NamingException ne) {
                log.warn("Error searching LDAP at "+jndiLdapUrl, ne);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    log.warn("Interrupted", e);
                }
            } catch (Exception e) {
                log.warn("Error searching LDAP at "+jndiLdapUrl, e);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ie) {
                    log.warn("Interrupted", e);
                }
            }
        }
        return null;       
    }

    public void setAccountField(String accountField) {
        this.accountField = accountField;
    }
    
    public void setDnField(String dnField) {
        this.dnField = dnField;
    }
    
    public void setJndiLdapUrl(String jndiLdapUrl) {
        this.jndiLdapUrl = jndiLdapUrl;
    }

    public String toString(String bgColor) {
    	return "<td bgcolor=\""+bgColor+"\"><a href=\"accountMappers.jsp?command=edit&name=" + getName() + "\">" + getName() + "</a></td><td bgcolor=\""+bgColor+"\">" + getType() + "</td><td bgcolor=\""+bgColor+"\">&nbsp;</td>";
    }

    public String toXML() {
    	return "\t\t<ldapAccountMapper\n"+
			"\t\t\tname='"+getName()+"'\n"+
			"\t\t\tdescription='"+getDescription()+"'\n"+
			"\t\t\tjndiLdapUrl='"+jndiLdapUrl+"'\n"+
			"\t\t\tdnField='"+dnField+"'\n"+
			"\t\t\taccountField='"+accountField+"'/>\n\n";
    }
    
    private Properties retrieveJndiProperties() {
        Properties jndiProperties = new java.util.Properties();
        jndiProperties.put("java.naming.provider.url", jndiLdapUrl);
        jndiProperties.put("java.naming.factory.initial","com.sun.jndi.ldap.LdapCtxFactory");
        jndiProperties.put(Context.SECURITY_PROTOCOL, "none");
        return jndiProperties;
    } 
}
