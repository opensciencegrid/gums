/*
 * GecosLdapAccountMapper.java
 *
 * Created on April 13, 2005, 4:21 PM
 */

package gov.bnl.gums.account;

import gov.bnl.gums.configuration.Configuration;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchResult;

import org.apache.log4j.Logger;

/** 
 * Matches the DN with the account information retrieved from an LDAP server.
 *
 * @author Gabriele Carcassi, Jay Packard
 */
public class GecosLdapAccountMapper extends GecosAccountMapper {
	static private Logger log = Logger.getLogger(GecosLdapAccountMapper.class);

	static public String getTypeStatic() {
		return "gecosLdap";
	}

	private String jndiLdapUrl = "";
	private String gecosField = "gecos";
	private String accountField = "uid";
	private String peopleObject = "ou=People";

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
		GecosLdapAccountMapper accountMapper = new GecosLdapAccountMapper(configuration, new String(getName()));
		accountMapper.setDescription(new String(getDescription()));
		accountMapper.setJndiLdapUrl(new String(jndiLdapUrl));
		accountMapper.setGecosField(new String(gecosField));
		accountMapper.setAccountField(new String(accountField));
		accountMapper.setPeopleObject(new String(peopleObject));
		return accountMapper;
	}

	public String getAccountField() {
		return accountField;
	}

	public String getGecosField() {
		return gecosField;
	}

	public String getJndiLdapUrl() {
		return jndiLdapUrl;
	}

	public String getPeopleObject() {
		return peopleObject;
	}

	public String getType() {
		return "gecosLdap";
	}

	public void setAccountField(String accountField) {
		this.accountField = accountField;
	}

	public void setGecosField(String gecosField) {
		this.gecosField = gecosField;
	}

	public void setJndiLdapUrl(String jndiLdapUrl) {
		this.jndiLdapUrl = jndiLdapUrl;
	}

	public void setPeopleObject(String peopleObject) {
		this.peopleObject = peopleObject;
	}

	public String toString(String bgColor) {
		return "<td bgcolor=\""+bgColor+"\"><a href=\"accountMappers.jsp?command=edit&name=" + getName() + "\">" + getName() + "</a></td><td bgcolor=\""+bgColor+"\">" + getType() + "</td><td bgcolor=\""+bgColor+"\">&nbsp;</td>";
	}

	public String toXML() {
		return "\t\t<gecosLdapAccountMapper\n"+
		"\t\t\tname='"+getName()+"'\n"+
		"\t\t\tdescription='"+getDescription()+"'\n"+
		"\t\t\tjndiLdapUrl='"+jndiLdapUrl+"'\n"+
		"\t\t\tgecosField='"+gecosField+"'\n"+
		"\t\t\taccountField='"+accountField+"'\n"+
		"\t\t\tpeopleObject='"+peopleObject+"'/>\n\n";
	}

	private Properties retrieveJndiProperties() {
		Properties jndiProperties = new java.util.Properties();
		jndiProperties.put("java.naming.provider.url", jndiLdapUrl);
		jndiProperties.put("java.naming.factory.initial","com.sun.jndi.ldap.LdapCtxFactory");
		jndiProperties.put(Context.SECURITY_PROTOCOL, "none");
		return jndiProperties;
	}

	protected GecosMap createMap() {
		Properties jndiProperties = retrieveJndiProperties();
		int nTries = 5;
		Exception lastException = null;
		int i = 0;
		for (; i < nTries; i++) {
			GecosMap map = new GecosMap();
			log.debug("Attempt " + i + " to retrieve map for '" + jndiLdapUrl + "'");
			try {
				DirContext jndiCtx = new InitialDirContext(jndiProperties);
				NamingEnumeration nisMap = jndiCtx.search(peopleObject, "("+accountField+"=*)", null);
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

	protected String getMapName() {
		return jndiLdapUrl;
	}
}
