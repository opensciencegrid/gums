/*
 * GecosNisAccountMapper.java
 *
 * Created on April 13, 2005, 4:21 PM
 */

package gov.bnl.gums.account;

import gov.bnl.gums.configuration.Configuration;

import java.util.Properties;

import javax.naming.NamingEnumeration;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/** Matches the DN with the account information retrieved from an LDAP server.
 *
 * @author Gabriele Carcassi
 */
public class GecosLdapAccountMapper extends GecosAccountMapper {
    static private Log log = LogFactory.getLog(GecosLdapAccountMapper.class);
    private String jndiLdapUrl = "";

    public GecosLdapAccountMapper() {
    	super();
    }
 
    public GecosLdapAccountMapper(Configuration configuration) {
    	super(configuration);
    }
    
    public GecosLdapAccountMapper(Configuration configuration, String name) {
    	super(configuration, name);
    }
    
    public AccountMapper clone(Configuration configuration) {
    	GecosLdapAccountMapper accountMapper = new GecosLdapAccountMapper(configuration, getName());
    	accountMapper.setJndiLdapUrl(jndiLdapUrl);
    	return accountMapper;
    }
    
    /**
     * Returns the URL used to describe the LDAP server.
     * @return LDAP url according to JNDI LDAP driver.
     */
    public String getJndiLdapUrl() {
        return jndiLdapUrl;
    }
    
    /**
     * Changes the LDAP server to use.
     * @param jndiLdapUrl LDAP url according to JNDI LDAP driver.
     */
    public void setJndiLdapUrl(String jndiLdapUrl) {
        this.jndiLdapUrl = jndiLdapUrl;
    }

    public String toString(String bgColor) {
    	return "<td bgcolor=\""+bgColor+"\">" + getName() + "</td><td bgcolor=\""+bgColor+"\">"+jndiLdapUrl+"</td>";
    }

    public String toXML() {
    	return super.toXML() +
			"\t\t\tjndiLdapUrl='"+jndiLdapUrl+"'/>\n\n";
    }

    private Properties retrieveJndiProperties() {
        Properties jndiProperties = new java.util.Properties();
        jndiProperties.put("java.naming.provider.url", jndiLdapUrl);
        jndiProperties.put("java.naming.factory.initial","com.sun.jndi.ldap.LdapCtxFactory");
        return jndiProperties;
    }

    protected GecosMap createMap() {
        Properties jndiProperties = retrieveJndiProperties();
        int nTries = 5;
        Exception lastException = null;
        int i = 0;
        for (; i < nTries; i++) {
            GecosMap map = new GecosMap();
            log.debug("Attemp " + i + " to retrieve map for '" + jndiLdapUrl + "'");
            try {
                DirContext jndiCtx = new InitialDirContext(jndiProperties);
                NamingEnumeration nisMap = jndiCtx.search("ou=People", "(cn=*)", null);
                log.trace("Server responded");
                while (nisMap.hasMore()) {
                    SearchResult res = (SearchResult) nisMap.next();
                    Attributes atts = res.getAttributes();
                    String username = (String) atts.get("uid").get();
                    Attribute gecosAtt = atts.get("gecos");
                    if (gecosAtt != null) {
                        String gecos = gecosAtt.get().toString();
                        map.addEntry(username, gecos);
                    } else {
                        log.trace("Found user '" + username + "' with no GECOS field");
                    }
                }
                jndiCtx.close();
                return map;
            } catch (javax.naming.NamingException ne) {
                log.warn("Error filling the maps for NIS "+jndiLdapUrl, ne);
                lastException = ne;
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    log.warn("Interrupted", e);
                }
            } catch (Exception e) {
                log.warn("Error filling the maps for NIS "+jndiLdapUrl, e);
                lastException = e;
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ie) {
                    log.warn("Interrupted", e);
                }
            }
        }
        if (i == nTries) {
            throw new RuntimeException("Couldn't retrieve NIS maps from " + jndiLdapUrl, lastException);
        }
        return null;
    }    
    
    protected String mapName() {
        return jndiLdapUrl;
    }
}
