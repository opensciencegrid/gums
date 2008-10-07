/*
 * GecosNisAccountMapper.java
 *
 * Created on April 13, 2005, 4:21 PM
 */

package gov.bnl.gums.account;

import gov.bnl.gums.GUMS;
import gov.bnl.gums.configuration.Configuration;

import java.util.Properties;

import javax.naming.NamingEnumeration;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchResult;

import org.apache.log4j.Logger;

/** 
 * Matches the DN with the account information retrieved from a NIS server.
 *
 * @author Gabriele Carcassi, Jay Packard
 */
public class GecosNisAccountMapper extends GecosAccountMapper {
    static private Logger log = Logger.getLogger(GecosNisAccountMapper.class);
    static Logger gumsAdminLog = Logger.getLogger(GUMS.gumsAdminLogName);
    
    static public String getTypeStatic() {
		return "gecosNIS";
	}
    
	private String jndiNisUrl = "";
	private String gecosField = "gecos";
	private String accountField = "cn";
    
    public GecosNisAccountMapper() {
    	super();
    	gumsAdminLog.debug("The use of gov.bnl.gums.GecosNisAccountMapper is unsupported.");
    }
 
    public GecosNisAccountMapper(Configuration configuration) {
    	super(configuration);
    }
    
    public GecosNisAccountMapper(Configuration configuration, String name) {
    	super(configuration, name);
    }
    
    public AccountMapper clone(Configuration configuration) {
    	GecosNisAccountMapper accountMapper = new GecosNisAccountMapper(configuration, new String(getName()));
    	accountMapper.setDescription(new String(getDescription()));
    	accountMapper.setJndiNisUrl(new String(jndiNisUrl));
    	accountMapper.setGecosField(new String(gecosField));
    	accountMapper.setAccountField(new String(accountField));
    	return accountMapper;
    }
    
    public GecosMap createMap() {
        Properties jndiProperties = retrieveJndiProperties();
        int nTries = 5;
        Exception lastException = null;
        int i = 0;
        for (; i < nTries; i++) {
            GecosMap map = new GecosMap();
            log.debug("Attempt " + i + " to retrieve map for '" + jndiNisUrl + "'");
            try {
                DirContext jndiCtx = new InitialDirContext(jndiProperties);
                NamingEnumeration nisMap = jndiCtx.search("system/passwd.byname", "(cn=*)", null);
                log.trace("Server responded");
                while (nisMap.hasMore()) {
                    SearchResult res = (SearchResult) nisMap.next();
                    Attributes atts = res.getAttributes();
                    String account = (String) atts.get(accountField).get();
                    Attribute gecosAtt = atts.get(gecosField);
                    if (gecosAtt != null) {
                        String gecos = gecosAtt.get().toString();
                        map.addEntry(account, gecos);
                    } else {
                        log.trace("Found user '" + account + "' with no GECOS field");
                    }
                }
                jndiCtx.close();
                return map;
            } catch (javax.naming.NamingException ne) {
                log.warn("Error filling the maps for NIS "+jndiNisUrl, ne);
                lastException = ne;
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    log.warn("Interrupted", e);
                }
            } catch (Exception e) {
                log.warn("Error filling the maps for NIS "+jndiNisUrl, e);
                lastException = e;
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ie) {
                    log.warn("Interrupted", e);
                }
            }
        }
        if (i == nTries) {
            throw new RuntimeException("Couldn't retrieve NIS maps from " + jndiNisUrl, lastException);
        }
        return null;
    }
    
    public String getAccountField() {
    	return accountField;
    }
    
    public String getGecosField() {
    	return gecosField;
    }
    
    public String getJndiNisUrl() {
        return jndiNisUrl;
    }
    
    public void setAccountField(String accountField) {
    	this.accountField = accountField;
    }
    
    public void setGecosField(String gecosField) {
    	this.gecosField = gecosField;
    }
    
    public String getType() {
		return "gecosNIS";
	}
    
    public void setJndiNisUrl(String jndiNisUrl) {
        this.jndiNisUrl = jndiNisUrl;
    }

    public String toString(String bgColor) {
    	return "<td bgcolor=\""+bgColor+"\"><a href=\"accountMappers.jsp?command=edit&name=" + getName() + "\">" + getName() + "</a></td><td bgcolor=\""+bgColor+"\">" + getType() + "</td><td bgcolor=\""+bgColor+"\"></td>";
    }

    public String toXML() {
    	return "\t\t<gecosNisAccountMapper\n"+
			"\t\t\tname='"+getName()+"'\n"+
			"\t\t\tdescription='"+getDescription()+"'\n"+
			"\t\t\tjndiNisUrl='"+jndiNisUrl+"'\n"+
    		"\t\t\tgecosField='"+gecosField+"'\n"+
			"\t\t\taccountField='"+accountField+"'/>\n\n";
    }

    private Properties retrieveJndiProperties() {
        Properties jndiProperties = new java.util.Properties();
        jndiProperties.put("java.naming.provider.url", jndiNisUrl);
        jndiProperties.put("java.naming.factory.initial","com.sun.jndi.nis.NISCtxFactory");
        return jndiProperties;
    }      
    
    protected String getMapName() {
        return jndiNisUrl;
    }
}
