/*
 * BNLPersistenceFactory.java
 *
 * Created on February 3, 2005, 9:52 AM
 */

package gov.bnl.gums.persistence;

import gov.bnl.gums.GridUser;
import gov.bnl.gums.configuration.Configuration;
import gov.bnl.gums.db.AccountPoolMapperDB;
import gov.bnl.gums.db.LDAPAccountMapperDB;
import gov.bnl.gums.db.ManualAccountMapperDB;
import gov.bnl.gums.db.ManualUserGroupDB;
import gov.bnl.gums.db.UserGroupDB;
import gov.bnl.gums.db.ConfigurationDB;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;

/**
 * Persistence factory instantiation that combines a hibernate persistence factory
 * with a small part of the ldap persistence factory (see addToSecondaryGroup).  When a pool 
 * account is assigned a group, this is set in ldap.
 *
 * @author Gabriele Carcassi, Jay Packard
 */
public class LocalPersistenceFactory extends PersistenceFactory {
	/**
	 * Implements AccountPoolMapperDB for the local persistence factory
	 * 
	 * @author Gabriele Carcassi, Jay Packard
	 */
	private class LocalAccountPoolMapperDB implements AccountPoolMapperDB {
		private AccountPoolMapperDB db;
		private String group;
		private List secondaryGroups;

		public LocalAccountPoolMapperDB(String poolname, String group, List secondaryGroups) {
			this.group = group;
			this.secondaryGroups = secondaryGroups;
			db = persFactory.retrieveAccountPoolMapperDB(poolname);
		}

		public void addAccount(String account) {
			db.addAccount(account);
		}

		public String assignAccount(GridUser user) {
			String account = db.assignAccount(user);
			if (account == null) return null;
			log.trace("Assigned '" + account + "' to '" + user.getCertificateDN());
			try {
				assignGroups(account);
				assignEmail(account, user.getEmail());
				return account;
			} catch (RuntimeException e) {
				log.trace("Group assignment failed: unassign user", e);
				unassignUser(user.getCertificateDN());
				log.trace("User unassigned");
				throw e;
			}
		}
		
	    public String getMap() {
	    	return db.getMap();
	    }

		public boolean needsCacheRefresh() {
			return db.needsCacheRefresh();
		}

		public boolean removeAccount(String account) {
			return db.removeAccount(account);
		}

		public String retrieveAccount(GridUser user) {
			String account = db.retrieveAccount(user);
			if (account!=null && synch) {
				assignGroups(account);
				assignEmail(account, user.getEmail());
			}
			return account;
		}

		public java.util.Map retrieveAccountMap() {
			return db.retrieveAccountMap();
		}

		public java.util.Map retrieveReverseAccountMap() {
			return db.retrieveReverseAccountMap();
		}

		public List retrieveUsersNotUsedSince(Date date) {
			return db.retrieveUsersNotUsedSince(date);
		}

		synchronized public void setCacheRefreshed() {
			db.setCacheRefreshed();
		}

		public void unassignAccount(String account) {
			db.unassignAccount(account);
		}

		public void unassignUser(String user) {
			db.unassignUser(user);
		}

		private void assignEmail(String account, String email) {
			ldap.changeEmail(account, email);
			log.trace("Assigned email '" + email + "' to '" + account);
		}
		
		private void assignGroups(String account) {
			ldap.changeGroupID(account, group);
			log.trace("Assigned '" + group + "' to '" + account);
			Iterator iter = secondaryGroups.iterator();
			while (iter.hasNext()) {
				String group = (String) iter.next();
				ldap.addToSecondaryGroup(account, group);
				log.trace("Assigned secondary group '" + group + "' to '" + account);
			}
		}

	}

	static public String getTypeStatic() {
		return "local";
	}
	private Logger log = Logger.getLogger(LocalPersistenceFactory.class);    
	private HibernatePersistenceFactory persFactory;
	private LDAPPersistenceFactory ldap;

	private boolean synch = false;

	/**
	 * Create a new local persistence factory.  This empty constructor is needed by the XML Digester.
	 */
	public LocalPersistenceFactory() {
		super();
		persFactory = new HibernatePersistenceFactory();
		ldap = new LDAPPersistenceFactory();   	
		log.trace("LocalPersistenceFactory instanciated");
	}

	/**
	 * Create a new local persistence factory with a configuration.
	 * 
	 * @param configuration
	 */
	public LocalPersistenceFactory(Configuration configuration) {
		super(configuration);
		persFactory = new HibernatePersistenceFactory(configuration);
		ldap = new LDAPPersistenceFactory(configuration);   	
		log.trace("LocalPersistenceFactory instanciated");
	}

	/**
	 * Create a new local persistence factory with a configuration and a name.
	 * 
	 * @param configuration
	 * @param name
	 */
	public LocalPersistenceFactory(Configuration configuration, String name) {
		super(configuration, name);
		persFactory = new HibernatePersistenceFactory(configuration, name);
		ldap = new LDAPPersistenceFactory(configuration, name);  
		log.trace("LocalPersistenceFactory instanciated");
	}
	
	public PersistenceFactory clone(Configuration configuration) {
		LocalPersistenceFactory persistenceFactory = new LocalPersistenceFactory(configuration, new String(getName()));
		persistenceFactory.setDescription(new String(getDescription()));
		persistenceFactory.setStoreConfig(getStoreConfig());
//		persistenceFactory.setCaCertFile(getCaCertFile());
//		persistenceFactory.setTrustStorePassword(getTrustStorePassword());
		persistenceFactory.setPeopleTree(new String(getPeopleTree()));
		persistenceFactory.setGroupTree(new String(getGroupTree()));
		persistenceFactory.setGumsTree(new String(getGumsTree()));
		persistenceFactory.setGroupCnField(new String(getGroupCnField()));
		persistenceFactory.setUidField(new String(getUidField()));
		persistenceFactory.setGidNumberField(new String(getGidNumberField()));
		persistenceFactory.setMemberUidField(new String(getMemberUidField()));
		persistenceFactory.setProperties((Properties)getProperties().clone());
		persistenceFactory.setSynch(persistenceFactory.isSynch());
		persistenceFactory.setEmailField(new String(persistenceFactory.getEmailField()));
		return persistenceFactory;
	}

	public String getCaCertFile() {
		return ldap.getCaCertFile();
	}
	
	public String getEmailField() {
		return ldap.getEmailField();
	}

	public String getGidNumberField() {
		return ldap.getGidNumberField();
	}

	public String getGroupCnField() {
		return ldap.getGroupCnField();
	}

	public String getGroupTree() {
		return ldap.getGroupTree();
	}

	public String getGumsObject() {
		return ldap.getGumsObject();
	}
	
	public String getGumsTree() {
		return ldap.getGumsTree();
	}
	
	public Properties getLDAPProperties() {
		return filterProperties("ldap.");
	}

	public String getMemberUidField() {
		return ldap.getMemberUidField();
	}

	public Properties getMySQLProperties() {
		return filterProperties("mysql.");
	}

	public String getPeopleTree() {
		return ldap.getPeopleTree();
	}

	public String getTrustStorePassword() {
		return ldap.getTrustStorePassword();
	}

	public String getType() {
		return "local";
	}

	public String getUidField() {
		return ldap.getUidField();
	}

	/**
	 * Getter for property synchGroups.
	 * @return Value of property synchGroups.
	 */
	public boolean isSynch() {
		return this.synch;
	}
	/**
	 * @depricated
	 */
	public boolean isSynchGroups() {
		return this.synch;
	}

	public AccountPoolMapperDB retrieveAccountPoolMapperDB(String name) {
		StringTokenizer tokens = new StringTokenizer(name, ".");
		if (!tokens.hasMoreTokens()) {
			return persFactory.retrieveAccountPoolMapperDB(name);
		}
		String pool = tokens.nextToken();
		if (!tokens.hasMoreTokens()) {
			return persFactory.retrieveAccountPoolMapperDB(name);
		}
		String group = tokens.nextToken();
		List secondaryGroups = new ArrayList();
		while (tokens.hasMoreTokens()) {
			secondaryGroups.add(tokens.nextToken());
		}
		return new LocalAccountPoolMapperDB(pool, group, secondaryGroups);
	}

	public ConfigurationDB retrieveConfigurationDB() {
		return persFactory.retrieveConfigurationDB();
	}
	
	public ManualAccountMapperDB retrieveManualAccountMapperDB(String name) {
		return persFactory.retrieveManualAccountMapperDB(name);
	}

	public ManualUserGroupDB retrieveManualUserGroupDB(String name) {
		return persFactory.retrieveManualUserGroupDB(name);
	}

	public UserGroupDB retrieveUserGroupDB(String name) {
		return persFactory.retrieveUserGroupDB(name);
	}

	// @depricated
	public void setAccountField(String accountField) {
		ldap.setAccountField(accountField);
	}

	public void setCaCertFile(String caCertFile) {
		ldap.setCaCertFile( caCertFile );
	}

	public void setConfiguration(Configuration configuration) {
		super.setConfiguration(configuration);
		ldap.setConfiguration(configuration);
		persFactory.setConfiguration(configuration);
	}
	
	public void setEmailField(String emailField) {
		ldap.setEmailField(emailField);
	}

	public void setGidNumberField(String gidNumberField) {
		ldap.setGidNumberField(gidNumberField);
	}
	
	public void setGroupCnField(String groupCnField) {
		ldap.setGroupCnField(groupCnField);
	}
	// @depricated
	public void setGroupField(String groupField) {
		ldap.setGroupCnField(groupField);
	}

	// @depricated
	public void setGroupIdField(String groupIdField) {
		ldap.setGroupIdField(groupIdField);
	}
	public void setGroupTree(String groupTree) {
		ldap.setGroupTree(groupTree);
	}

	public void setGumsTree(String gumsTree) {
		ldap.setGumsTree(gumsTree);
	}
	// @depricated 
	public void setMemberAccountField(String memberAccountField) {
		ldap.setMemberAccountField(memberAccountField);
	}

	public void setMemberUidField(String memberUidField) {
		ldap.setMemberUidField(memberUidField);
	}
	
	public void setName(String name) {
		super.setName(name);
		ldap.setName(name);
		persFactory.setName(name);
	}

	public void setPeopleTree(String peopleTree) {
		ldap.setPeopleTree(peopleTree);
	}

	public void setProperties(Properties properties) {
		super.setProperties(properties);
		persFactory.setProperties(getMySQLProperties());
		ldap.setProperties(getLDAPProperties());
	}

	public void setSynch(boolean synch) {
		this.synch = synch;
	}

	/**
	 * @depricated
	 */
	public void setSynchGroups(boolean synchGroups) {
		this.synch = synchGroups;
	}
	public void setTrustStorePassword(String trustStorePassword) {
		ldap.setTrustStorePassword(trustStorePassword);
	}

	public void setUidField(String uidField) {
		ldap.setUidField(uidField);
	}    

	public String toXML() {
		String retStr = "\t\t<localPersistenceFactory\n"+
		"\t\t\tname='"+getName()+"'\n"+
		"\t\t\tdescription='"+getDescription()+"'\n"+
		"\t\t\tstoreConfig='"+(getStoreConfig()?"true":"false")+"'\n"+
		"\t\t\tsynch='"+synch+"'\n"+
//		"\t\t\tcaCertFile='"+getCaCertFile()+"'\n"+
//		"\t\t\ttrustStorePassword='"+getTrustStorePassword()+"'\n"+
		"\t\t\tgidNumberField='"+getGidNumberField()+"'\n"+
		"\t\t\tgroupCnField='"+getGroupCnField()+"'\n"+
		"\t\t\tuidField='"+getUidField()+"'\n"+
		"\t\t\tmemberUidField='"+getMemberUidField()+"'\n"+
		"\t\t\temailField='"+getEmailField()+"'\n"+
		"\t\t\tgroupTree='"+getGroupTree()+"'\n"+
		"\t\t\tgumsTree='"+getGroupTree()+"'\n"+
		"\t\t\tpeopleTree='"+getPeopleTree()+"'\n";
		
		Iterator keyIt = getProperties().keySet().iterator();
		while(keyIt.hasNext()) {
			String key = (String)keyIt.next();
			retStr += "\t\t\t"+key+"='"+getProperties().getProperty(key)+"'\n";
		}

		if (retStr.charAt(retStr.length()-1)=='\n')
			retStr = retStr.substring(0, retStr.length()-1);    	

		retStr += "/>\n\n";

		return retStr;
	}
	
	private Properties filterProperties(String prefix) {
		Properties filtered = new Properties();
		Enumeration keys = getProperties().propertyNames();
		while (keys.hasMoreElements()) {
			String name = (String) keys.nextElement();
			if (name.startsWith(prefix)) {
				filtered.setProperty(name.substring(prefix.length()), getProperties().getProperty(name));
			}
		}
		return filtered;
	}

}
