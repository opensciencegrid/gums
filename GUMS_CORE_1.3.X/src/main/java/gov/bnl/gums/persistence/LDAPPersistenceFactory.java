
/*
 * LDAPPersistenceFactory.java
 *
 * Created on January 21, 2005, 9:37 AM
 */

package gov.bnl.gums.persistence;

import gov.bnl.gums.configuration.Configuration;
import gov.bnl.gums.db.AccountPoolMapperDB;
import gov.bnl.gums.db.LDAPAccountMapperDB;
import gov.bnl.gums.db.LDAPUserGroupDB;
import gov.bnl.gums.db.ManualAccountMapperDB;
import gov.bnl.gums.db.ManualUserGroupDB;
import gov.bnl.gums.db.UserGroupDB;
import gov.bnl.gums.db.ConfigurationDB;
import gov.bnl.gums.db.LDAPConfigurationDB;
import gov.bnl.gums.*;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.naming.*;
import javax.naming.directory.*;
import javax.naming.ldap.InitialLdapContext;

import org.apache.log4j.Logger;

/**
 *
 * @author Gabriele Carcassi, Jay Packard
 */
public class LDAPPersistenceFactory extends PersistenceFactory {
	static public String getTypeStatic() {
		return "ldap";
	}

	private Logger log = Logger.getLogger(LDAPPersistenceFactory.class);
	private Logger gumsAdminLog = Logger.getLogger(GUMS.gumsAdminLogName);
	private boolean synch;
	private List contexts = Collections.synchronizedList(new LinkedList());// *** LDAP connection pool management    
	private boolean skipReleaseContext = false;
	private String trustStore = System.getProperty("java.home")+"/lib/security/cacerts"; // doesn't do anything anymore because it required tomcat restart
	private String trustStorePassword = ""; // doesn't do anything anymore because it required tomcat restart
	private String caCertFile = ""; // doesn't do anything anymore because it required tomcat restart
	private String uidField = "uid";
	private String emailField = "mail";
	private String memberUidField = "memberUid";
	private String gidNumberField = "gidNumber";
	private String groupCnField = "cn";
	private String peopleTree = "";
	private String peopleObject = "ou=People";
	private String peopleContext = null;
	private String groupTree = "";
	private String groupObject = "ou=Group";
	private String groupContext = null; 
	private String gumsTree = "";
	private String gumsObject = "ou=GUMS";
	private String gumsContext = null; 

	/**
	 * Create a new ldap persistence factory.  This empty constructor is needed by the XML Digester.
	 */
	public LDAPPersistenceFactory() {
		super();
	}

	/**
	 * Create a new ldap persistence factory with a configuration.
	 * 
	 * @param configuration
	 */
	public LDAPPersistenceFactory(Configuration configuration) {
		super(configuration);
	}

	/**
	 * Create a new ldap persistence factory with a configuration and a name.
	 * 
	 * @param configuration
	 * @param name
	 */
	public LDAPPersistenceFactory(Configuration configuration, String name) {
		super(configuration, name);
	}

	/** 
	 * Adds a userDN -> account mapping entry in the "map=mapName" LDAP map.
	 * 
	 * @param userDN the certificate DN of the user (i.e. "/DC=org/DC=doegrids/OU=People/CN=John Smith")
	 * @param account the account to whith to map the DN (i.e. "carcassi")
	 * @param mapName the name of the map (i.e. "usatlasSpecialMap")
	 * @param mapDN the map DN (i.e. "map=usatlasSpecialMap, ou=GUMS")
	 */
	public void addMapEntry(String userDN, String account, String mapName, String mapDN) {
		DirContext context = retrieveGumsDirContext();
		try {
			try {
				ModificationItem[] mods = new ModificationItem[1];
				mods[0] = new ModificationItem(context.ADD_ATTRIBUTE, new BasicAttribute("user", userDN));
				context.modifyAttributes("account=" + account + "," + mapDN, mods);
				log.trace("Added user '" + userDN + "' / account '" + account + "' to map '" + mapName + "' at '" + mapDN + "' (account entry present)");
			} catch (NameNotFoundException e) {
				Attributes atts = new BasicAttributes();
				Attribute oc = new BasicAttribute("objectclass");
				oc.add("GUMStruct");
				oc.add("GUMSAccount");
				Attribute userAtt = new BasicAttribute("user", userDN);
				Attribute accountAtt = new BasicAttribute("account", account);
				atts.put(oc);
				atts.put(userAtt);
				atts.put(accountAtt);
				context.createSubcontext("account=" + account + "," + mapDN , atts);
				log.trace("Added user '" + userDN + "' / account '" + account + "' to map '" + mapName + "' at '" + mapDN + "' (account entry created)");
			}
		} catch (Exception e) {
			log.info("LDAPPersistence error - addMapEntry - user '" + userDN + "' / account '" + account + "' to map '" + mapName + "' at '" + mapDN + "'", e);
			throw new RuntimeException("Couldn't add mapping to LDAP map - user '" + userDN + "' / account '" + account + "' to map '" + mapName + "' at '" + mapDN + "': " + e.getMessage(), e);
		} finally {
			releaseContext(context);
		}
	}

	/** 
	 * Adds the account to the given secondary group.
	 * 
	 * @param account the account to add to the secondary group (i.e. "carcassi")
	 * @param groupname the secondary group name (i.e. "usatlas")
	 */
	public void addToSecondaryGroup(String account, String groupname) {
		DirContext context = null;
		try {
			context = retrieveGroupContext();
			SearchControls ctrls = new SearchControls();
			ctrls.setSearchScope(SearchControls.SUBTREE_SCOPE);
			NamingEnumeration result;
			result = context.search(groupCnField+"="+groupname+","+groupObject, "("+memberUidField+"={0})", new Object[] {account}, ctrls);
			if (result.hasMore()) return;
			ModificationItem[] mods = new ModificationItem[1];
			mods[0] = new ModificationItem(context.ADD_ATTRIBUTE, new BasicAttribute(memberUidField, account));
			context.modifyAttributes(groupCnField+"="+groupname+","+groupObject, mods);
			log.trace("Added secondary group to user - user '" + account + "' to group '" + groupname + "'");
		} catch (Exception e) {
			log.info("Couldn't add user to secondary group - user '" + account + "' to group '" + groupname + "'", e);
			throw new RuntimeException("Couldn't add user to secondary group - user '" + account + "' to group '" + groupname + "': " + e.getMessage(), e);
		} finally {
			releaseContext(context);
		}
	}

	/** 
	 * Adds a certificate DN to the group "group=groupName".
	 * 
	 * @param userDN the certificate DN of the user (i.e. "/DC=org/DC=doegrids/OU=People/CN=John Smith")
	 * @param groupName the name of the group (i.e. "usatlas")
	 * @param groupDN the group DN (i.e. "group=usatlas, ou=GUMS")
	 */
	public void addUserGroupEntry(String userDN, String groupName, String groupDN) {
		DirContext context = retrieveGumsDirContext();
		try {
			ModificationItem[] mods = new ModificationItem[1];
			mods[0] = new ModificationItem(context.ADD_ATTRIBUTE,
					new BasicAttribute("user", userDN));
			context.modifyAttributes(groupDN, mods);
			log.trace("Added user '" + userDN + "' to group '" + groupName + "' at '" + groupDN + "'");
		} catch (Exception e) {
			log.info("LDAPPersistence error - addUserGroupEntry - user '" + userDN + "' to group '" + groupName + "' at '" + groupDN + "'", e);
			throw new RuntimeException("Couldn't add user to LDAP group - user '" + userDN + "' to group '" + groupName + "' at '" + groupDN + "': " + e.getMessage(), e);
		} finally {
			releaseContext(context);
		}
	}

	/** 
	 * Changes the email for the given account.
	 * 
	 * @param account the account to change the primary group (i.e. "carcassi")
	 * @param email
	 */
	public void changeEmail(String account, String email) {
		if (emailField!=null && emailField.length()>0 && email!=null && email.length()>0) {
			DirContext context = retrievePeopleContext();
			try {
				ModificationItem[] mods = new ModificationItem[1];
				mods[0] = new ModificationItem(context.REPLACE_ATTRIBUTE, new BasicAttribute(emailField, email));
				context.modifyAttributes(uidField+"="+account+","+peopleObject, mods);
				log.trace("Changed email for user '" + account + "' to email '" + email + "''");
			} catch (Exception e) {
				log.warn("Couldn't change email for user '" + account + "' to email '" + email + "'", e);
				throw new RuntimeException("Couldn't change email for user '" + account + "' to email '" + email + "'", e);
			} finally {
				releaseContext(context);
			}
		}
	}
	
	/** 
	 * Changes the primary gid for the given account.
	 * 
	 * @param account the account to change the primary group (i.e. "carcassi")
	 * @param groupname the primary group name (i.e. "usatlas")
	 */
	public void changeGroupID(String account, String groupname) {
		try { 
			String gid = findGID(groupname);
			if (gid == null) {
				log.error("GID for group '" + groupname + "' wasn't found.");
				throw new RuntimeException("GID for group '" + groupname + "' wasn't found.");
			}
			updateGID(account, gid);
		}
		catch(NamingException e) {
			log.error(e.getMessage());
			throw new RuntimeException(e.getMessage());
		}
	}	

	public PersistenceFactory clone(Configuration configuration) {
		LDAPPersistenceFactory persistenceFactory = new LDAPPersistenceFactory(configuration, new String(getName()));
		persistenceFactory.setDescription(new String(getDescription()));
		persistenceFactory.setStoreConfig(getStoreConfig());
//		persistenceFactory.setCaCertFile(getCaCertFile());
//		persistenceFactory.setTrustStorePassword(getTrustStorePassword());
		persistenceFactory.setUidField(new String(getUidField()));
		persistenceFactory.setGroupCnField(new String(getGroupCnField()));
		persistenceFactory.setGidNumberField(new String(getGidNumberField()));
		persistenceFactory.setMemberUidField(new String(getMemberUidField()));
		persistenceFactory.setEmailField(new String(getEmailField()));
		persistenceFactory.setGroupTree(new String(getGroupTree()));
		persistenceFactory.setPeopleTree(new String(getPeopleTree()));
		persistenceFactory.setGumsTree(new String(getGumsTree()));
		persistenceFactory.setProperties((Properties)getProperties().clone());
		persistenceFactory.setSynch(persistenceFactory.isSynch());
		return persistenceFactory;
	}

	/** 
	 * Creates an account in the map "map=mapName", without having a userDN: this is useful
	 * for pools of accounts.
	 * 
	 * @param account the account to whith to map the DN (i.e. "grid0001")
	 * @param mapName the name of the map (i.e. "usatlasSpecialMap")
	 * @param mapDN the map DN (i.e. "map=usatlasSpecialMap, ou=GUMS")
	 */
	public void createAccountInMap(String account, String mapName, String mapDN) {
		DirContext context = retrieveGumsDirContext();
		try {
			Attributes atts = new BasicAttributes();
			Attribute oc = new BasicAttribute("objectclass");
			oc.add("GUMStruct");
			oc.add("GUMSAccount");
			Attribute accountAtt = new BasicAttribute("account", account);
			atts.put(oc);
			atts.put(accountAtt);
			context.createSubcontext("account=" + account + "," + mapDN , atts);
			log.trace("Added account '" + account + "' to map '" + mapName + "' at '" + mapDN + "'");
		} catch (Exception e) {
			log.info("LDAPPersistence error - createAccountInMap - account '" + account + "' to map '" + mapName + "' at '" + mapDN + "'", e);
			throw new RuntimeException("Couldn't add account to LDAP map - account '" + account + "' to map '" + mapName + "' at '" + mapDN + "': " + e.getMessage(), e);
		} finally {
			releaseContext(context);
		}
	}

	/** 
	 * Creates a new "map=mapName" entry in the LDAP GUMS tree.
	 * 
	 * @param mapName the name of the map (i.e. "usatlasSpecialMap")
	 * @param mapDN the map DN (i.e. "map=usatlasSpecialMap, ou=GUMS")
	 */
	public void createMap(String mapName, String mapDN) {
		DirContext context = retrieveGumsDirContext();
		try {
			Attributes atts = new BasicAttributes();
			Attribute oc = new BasicAttribute("objectclass");
			oc.add("GUMStruct");
			oc.add("GUMSMap");
			Attribute map = new BasicAttribute("map", mapName);
			atts.put(oc);
			atts.put(map);
			context.createSubcontext(mapDN , atts);
			log.trace("Created LDAP map '" + mapName + "' at '" + mapDN + "'");
		} catch (Exception e) {
			log.info("LDAPPersistence error - createMap - map '" + mapName + "'", e);
			throw new RuntimeException("Couldn't create LDAP map '" + mapName + "': " + e.getMessage(), e);
		} finally {
			releaseContext(context);
		}
	}

	/** 
	 * Creates a new "group=groupName" entry in the LDAP GUMS tree.
	 * 
	 * @param groupName the name of the group (i.e. "usatlas")
	 * @param groupDN the group DN (i.e. "group=usatlas, ou=GUMS")
	 */
	public void createUserGroup(String groupName, String groupDN) {
		DirContext context = retrieveGumsDirContext();
		try {
			Attributes atts = new BasicAttributes();
			Attribute oc = new BasicAttribute("objectclass");
			oc.add("GUMStruct");
			oc.add("GUMSGroup");
			Attribute group = new BasicAttribute("group", groupName);
			atts.put(oc);
			atts.put(group);
			context.createSubcontext(groupDN , atts);
			log.trace("Created user group '" + groupName + "' at '" + groupDN + "'");
		} catch (Exception e) {
			log.info("LDAPPersistence error - createUserGroup - group '" + groupName + "'", e);
			throw new RuntimeException("Couldn't create LDAP group '" + groupName + "': " + e.getMessage(), e);
		} finally {
			releaseContext(context);
		}
	}

	/** 
	 * Deletes the account in map.
	 * 
	 * @param mapName the name of the map (i.e. "usatlasSpecialMap")
	 * @param mapDN the map DN (i.e. "map=usatlasSpecialMap, ou=GUMS")
	 */
	public boolean destroyAccountInMap(String account, String mapName, String mapDN) {
		DirContext context = retrieveGumsDirContext();
		try {
			context.destroySubcontext("account=" + account + "," + mapDN );
			log.trace("Destroyed LDAP map '" + mapName + "' at '" + mapDN + "'");
		} catch (Exception e) {
			log.info("LDAPPersistence error - destroyMap - map '" + mapName + "'", e);
			throw new RuntimeException("Couldn't destroy LDAP map '" + mapName + "': " + e.getMessage(), e);
		} finally {
			releaseContext(context);
		}
		return true;
	}

	/** 
	 * Deletes the "map=mapName" map in the LDAP GUMS tree. Will completely
	 * delete the map.
	 * 
	 * @param mapName the name of the map (i.e. "usatlasSpecialMap")
	 * @param mapDN the map DN (i.e. "map=usatlasSpecialMap, ou=GUMS")
	 */
	public void destroyMap(String mapName, String mapDN) {
		DirContext context = retrieveGumsDirContext();
		try {
			SearchControls ctrls = new SearchControls();
			ctrls.setSearchScope(SearchControls.SUBTREE_SCOPE);
			NamingEnumeration result = context.search(mapDN, "(objectclass=*)", ctrls);
			while (result.hasMore()) {
				SearchResult res = (SearchResult) result.next();
				if ("".equals(res.getName().trim())) continue;
				context.destroySubcontext(res.getName() + "," + mapDN);
			}
			context.destroySubcontext(mapDN);
			log.trace("Destroyed LDAP map '" + mapName + "' at '" + mapDN + "'");
		} catch (Exception e) {
			log.info("LDAPPersistence error - destroyMap - map '" + mapName + "'", e);
			throw new RuntimeException("Couldn't destroy LDAP map '" + mapName + "': " + e.getMessage(), e);
		} finally {
			releaseContext(context);
		}
	}    

	/* @depricated */
	public String getAccountField() {
		return uidField;
	}

	public String getCaCertFile() {
		return caCertFile;
	}

	public String getEmailField() {
		return emailField;
	}

	public String getGidNumberField() {
		return gidNumberField;
	}

	public String getGroupCnField() {
		return groupCnField;
	}
	
	public String getGroupField() {
		return groupCnField;
	}

	/* @depricated */
	public String getGroupIdField() {
		return gidNumberField;
	}

	public String getGroupTree() {
		return groupTree;
	}
	
	public String getGumsObject() {
		return gumsObject;
	}
	
	public String getGumsTree() {
		return gumsTree;
	}

	/* @depricated */
	public String getMemberAccountField() {
		return memberUidField;
	}

	public String getMemberUidField() {
		return memberUidField;
	}

	public String getPeopleTree() {
		return peopleTree;
	}

	public String getTrustStorePassword() {
		return trustStorePassword;
	}

	public String getType() {
		return "ldap";
	}

	public String getUidField() {
		return uidField;
	}	
	/**
	 * This property forces the update for account pools at every access.
	 * It's handy for when gid and email gets out of synch.
	 * 
	 * @return if true information is updated every time accounts from the pool are returned.
	 */
	public boolean isSynch() {
		return this.synch;
	}

	/*
	 * @depricated
	 */
	public boolean isSynchGroups() {
		return this.synch;
	}

	/** Returns the LDAP DirContext to the pool, so that it can be reused.
	 * 
	 * @param context the LDAP context to be returned
	 */
	public void releaseContext(DirContext context) {
		if (skipReleaseContext) {
			skipReleaseContext = false;
			return;
		}
		contexts.add(0, context);
		log.trace("LDAP connection returned to pool " + context);
	}

	/** Removes a userDN -> acount mapping entry in the "map=mapName LDAP map.
	 * It will only remove the user entry, while leaving the account entry.
	 * 
	 * @param userDN the certificate DN of the user (i.e. "/DC=org/DC=doegrids/OU=People/CN=Gabriele Carcassi 12345")
	 * @param mapName the name of the map (i.e. "usatlasSpecialMap")
	 * @param mapDN the map DN (i.e. "map=usatlasSpecialMap")
	 * @return false if no mapping was removed
	 */
	public boolean removeMapEntry(String userDN, String mapName, String mapDN) {
		DirContext context = retrieveGumsDirContext();
		boolean deleted = false;
		try {
			SearchControls ctrls = new SearchControls();
			ctrls.setSearchScope(SearchControls.SUBTREE_SCOPE);
			NamingEnumeration result = context.search(mapDN, "(user={0})", new Object[] {userDN}, ctrls);
			while (result.hasMore()) {
				SearchResult res = (SearchResult) result.next();
				if ("".equals(res.getName().trim())) continue;
				ModificationItem[] mods = new ModificationItem[1];
				mods[0] = new ModificationItem(context.REMOVE_ATTRIBUTE, new BasicAttribute("user", userDN));
				context.modifyAttributes(res.getName() + "," + mapDN, mods);
				deleted = true;
				log.trace("Removed map entry - user '" + userDN + "' to map '" + mapName + "' at '" + mapDN + "'");
			}
			return deleted;
		} catch (Exception e) {
			log.info("LDAPPersistence error - removeMapEntry - user '" + userDN + "' to map '" + mapName + "' at '" + mapDN + "'", e);
			throw new RuntimeException("Couldn't remove map entry from LDAP map - user '" + userDN + "' to map '" + mapName + "' at '" + mapDN + "': " + e.getMessage(), e);
		} finally {
			releaseContext(context);
		}
	}

	/** Removes a certificate DN to the group "group=groupName".
	 * 
	 * @param userDN the certificate DN of the user (i.e. "/DC=org/DC=doegrids/OU=People/CN=Gabriele Carcassi 12345")
	 * @param groupName the name of the group (i.e. "usatlas")
	 * @param groupDN the group DN (i.e. "group=usatlas")
	 */
	public void removeUserGroupEntry(String userDN, String groupName, String groupDN) {
		DirContext context = retrieveGumsDirContext();
		try {
			ModificationItem[] mods = new ModificationItem[1];
			mods[0] = new ModificationItem(context.REMOVE_ATTRIBUTE,
					new BasicAttribute("user", userDN));
			context.modifyAttributes(groupDN, mods);
			log.trace("Removed user '" + userDN + "' to group '" + groupName + "' at '" + groupDN + "'");
		} catch (Exception e) {
			log.info("LDAPPersistence error - removeUserGroupEntry - user '" + userDN + "' to group '" + groupName + "' at '" + groupDN + "'", e);
			throw new RuntimeException("Couldn't remove user to LDAP group  - user '" + userDN + "' to group '" + groupName + "' at '" + groupDN + "': " + e.getMessage(), e);
		} finally {
			releaseContext(context);
		}
	}
	
	public AccountPoolMapperDB retrieveAccountPoolMapperDB(String nameAndGroups) {
		StringTokenizer tokens = new StringTokenizer(nameAndGroups, ".");
		if (!tokens.hasMoreTokens()) {
			log.trace("Creating LDAP AccountPoolMapperDB '" + nameAndGroups + "' (no GIDs)");
			return new LDAPAccountMapperDB(this, nameAndGroups);
		}

		String pool = tokens.nextToken();
		if (!tokens.hasMoreTokens()) {
			log.trace("Creating LDAP AccountPoolMapperDB '" + nameAndGroups + "' (no GIDs)");
			return new LDAPAccountMapperDB(this, nameAndGroups);
		}

		String group = tokens.nextToken();
		List secondaryGroups = new ArrayList();
		while (tokens.hasMoreTokens()) {
			secondaryGroups.add(tokens.nextToken());
		}

		log.trace("Creating LDAP AccountPoolMapperDB '" + nameAndGroups + "' primary group '" + group + "' secondary groups '" + secondaryGroups + "'");
		return new LDAPAccountMapperDB(this, pool, group, secondaryGroups);
	}	
	
	public ConfigurationDB retrieveConfigurationDB() {
		log.trace("Creating LDAP ConfigurationDB");
		return new LDAPConfigurationDB(this);
	}

	public String retrieveEmail(String uid) {
		DirContext context = retrievePeopleContext();
		try {
			NamingEnumeration result = context.search(peopleObject, "("+uidField+"={0})", new Object[] {uid}, null);
			String email = null;
			if (result.hasMore()) {
				SearchResult item = (SearchResult) result.next();
				Attributes atts = item.getAttributes();
				Attribute emailAtt = atts.get(emailField);
				if (emailAtt != null) {
					email = (String) emailAtt.get();
				}
			}
			log.trace("Found email '" + email + "' for uid '" + uid + "'");
			return email;
		} catch (Exception e) {
			log.info("Couldn't retrieve email for uid '" + uid + "'", e);
			throw new RuntimeException("Couldn't retrieve email for account '" + uid + "': " + e.getMessage(), e);
		} finally {
			releaseContext(context);
		}		
	}
	
	public DirContext retrieveGroupContext() {
		DirContext context;
		while (contexts.size() != 0) {
			context = (DirContext) contexts.remove(0);
			if (isContextValid(context)) {
				log.trace("Using LDAP connection from pool " + context);
				return context;
			}
		}
		context = createGroupContext();
		log.trace("New LDAP connection created " + context);
		return context;	
	}

	public DirContext retrieveGumsDirContext() {
		DirContext context;
		while (contexts.size() != 0) {
			context = (DirContext) contexts.remove(0);
			if (isContextValid(context)) {
				log.trace("Using LDAP connection from pool " + context);
				return context;
			}
		}
		context = createGumsContext();
		log.trace("New LDAP connection created " + context);
		return context;
	}

	public ManualAccountMapperDB retrieveManualAccountMapperDB(String name) {
		log.trace("Creating LDAP ManualAccountMapperDB '" + name + "'");
		return new LDAPAccountMapperDB(this, name);
	}

	public ManualUserGroupDB retrieveManualUserGroupDB(String name) {
		log.trace("Creating LDAP ManualUserGroupDB '" + name + "'");
		return new LDAPUserGroupDB(this, name);
	}

	public DirContext retrievePeopleContext() {
		DirContext context;
		while (contexts.size() != 0) {
			context = (DirContext) contexts.remove(0);
			if (isContextValid(context)) {
				log.trace("Using LDAP connection from pool " + context);
				return context;
			}
		}
		context = createPeopleContext();
		log.trace("New LDAP connection created " + context);
		return context;		
	}
	
	public UserGroupDB retrieveUserGroupDB(String name) {
		log.trace("Creating LDAP UserGroupDB '" + name + "'");
		return new LDAPUserGroupDB(this, name);
	}

	// @depricated
	public void setAccountField(String accountField) {
		this.uidField = accountField;
	}

	public void setCaCertFile(String caCertFile) {
		//System.setProperty("javax.net.ssl.trustStore", trustStore );
		this.caCertFile = caCertFile;
		//if (!trustStorePassword.equals(""))
		//addCertToTrustStore();
	}
	
	public void setEmailField(String emailField) {
		this.emailField = emailField;
	}
	
	public void setGidNumberField(String gidNumberField) {
		this.gidNumberField = gidNumberField;
	}

	public void setGroupCnField(String groupCnField) {
		this.groupCnField = groupCnField;
	}
	
	// @depricated
	public void setGroupField(String groupField) {
		this.groupCnField = groupField;
	}
	
	// @depricated 
	public void setGroupIdField(String groupIdField) {
		this.gidNumberField = groupIdField;
	}

	public void setGroupTree(String groupTree) {
		if (groupTree.length()>0) {
			this.groupTree = groupTree;
			if (groupTree.indexOf(',')!=-1) {
				this.groupObject = groupTree.substring(0, groupTree.indexOf(','));
				this.groupContext = groupTree.substring(groupTree.indexOf(',')+1);   
			}
			else
				this.groupObject = groupTree;
		} 
	}
	
	public void setGumsTree(String gumsTree) {
		if (gumsTree.length()>0) {
			this.gumsTree = gumsTree;
			if (gumsTree.indexOf(',')!=-1) {
				this.gumsObject = gumsTree.substring(0, gumsTree.indexOf(','));
				this.gumsContext = gumsTree.substring(gumsTree.indexOf(',')+1);   
			}
			else
				this.gumsObject = gumsTree;
		} 
	}

	// @depricated
	public void setMemberAccountField(String memberAccountField) {
		this.memberUidField = memberAccountField;
	}

	public void setMemberUidField(String memberUidField) {
		this.memberUidField = memberUidField;
	}

	public void setPeopleTree(String peopleTree) {
		if (peopleTree.length()>0) {
			this.peopleTree = peopleTree;
			if (peopleTree.indexOf(',')!=-1) {
				this.peopleObject = peopleTree.substring(0, peopleTree.indexOf(','));
				this.peopleContext = peopleTree.substring(peopleTree.indexOf(',')+1);   
			}
			else
				this.peopleObject = peopleTree;
		} 
	}
	/**
	 * Sets the list of properties to be used to connect to LDAP, that is
	 * to create the JNDI context.
	 * 
	 * @param properties a set of JNDI properties
	 */
	public void setProperties(Properties properties) {
		if (properties.getProperty(Context.INITIAL_CONTEXT_FACTORY) == null) {
			properties.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		}

		// For JUnit test only
		/*if (properties.getProperty("caCertFile")!=null) {
        	setCaCertFile(properties.getProperty("caCertFile"));
        	properties.remove("caCertFile");
        }
        if (properties.getProperty("trustStorePassword")!=null) {
        	setTrustStorePassword(properties.getProperty("trustStorePassword"));
        	properties.remove("trustStorePassword");
        }*/

		super.setProperties(properties);
	}

	/**
	 * This property forces the update for account pools at every access.
	 * It's handy for when gid or email gets out of synch.
	 * @param synchGroups if information is updated every time accounts from the pool are returned.
	 */
	public void setSynch(boolean synch) {
		this.synch = synch;
	}

	/*
	 * @depricated
	 */
	public void setSynchGroups(boolean synchGroups) {
		this.synch = synchGroups;
	}

	public void setTrustStorePassword(String trustStorePassword) {
		//System.setProperty("javax.net.ssl.trustStorePassword", trustStorePassword );
		this.trustStorePassword = trustStorePassword;
		//if (!caCertFile.equals(""))
		//addCertToTrustStore();
	}

	public void setUidField(String uidField) {
		this.uidField = uidField;
	}

	public String toXML() {
		String retStr = "\t\t<ldapPersistenceFactory\n"+
		"\t\t\tname='"+getName()+"'\n"+
		"\t\t\tdescription='"+getDescription()+"'\n"+
		"\t\t\tstoreConfig='"+(getStoreConfig()?"true":"false")+"'\n"+
		"\t\t\tsynch='"+synch+"'\n"+
//		"\t\t\tcaCertFile='"+getCaCertFile()+"'\n"+
//		"\t\t\ttrustStorePassword='"+trustStorePassword+"'\n"+
		"\t\t\tgidNumberField='"+gidNumberField+"'\n"+
		"\t\t\tuidField='"+uidField+"'\n"+
		"\t\t\tgroupCnField='"+groupCnField+"'\n"+
		"\t\t\tmemberUidField='"+memberUidField+"'\n"+
		"\t\t\temailField='"+emailField+"'\n"+
		"\t\t\tgroupTree='"+groupTree+"'\n"+
		"\t\t\tpeopleTree='"+peopleTree+"'\n"+
		"\t\t\tgumsTree='"+gumsTree+"'\n";

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

/*	private void addCertToTrustStore() {
		X509Certificate cert = null;
		try {
			InputStream inStream = new FileInputStream(caCertFile);
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			cert = (X509Certificate)cf.generateCertificate(inStream);
			inStream.close();
		} catch (Exception e) {
			log.error("Cannot open " + caCertFile, e);
			gumsAdminLog.error("Cannot open " + caCertFile + ": " + e.getMessage());
			return;
		}
		try {
			KeyStore ks = KeyStore.getInstance("jks");
			ks.load(new FileInputStream(trustStore), trustStorePassword.toCharArray());
			ks.setCertificateEntry("gumsldapda", cert);
			FileOutputStream fos = new FileOutputStream(trustStore);
			ks.store(fos, trustStorePassword.toCharArray());
			fos.close();
		} catch (Exception e) {
			log.error("Couldn't put " + caCertFile + " into trust store", e);
			gumsAdminLog.error("Couldn't put " + caCertFile + " into trust store: " + e.getMessage() );
		}
	}*/
	
	private String findGID(String groupname) throws NamingException {
		DirContext context = retrieveGroupContext();
		try {
			NamingEnumeration result = context.search(groupObject, "("+groupCnField+"={0})", new Object[] {groupname}, null);
			String gid = null;
			if (result.hasMore()) {
				SearchResult item = (SearchResult) result.next();
				Attributes atts = item.getAttributes();
				Attribute gidAtt = atts.get(gidNumberField);
				if (gidAtt != null) {
					gid = (String) gidAtt.get();
				}
			}
			log.trace("Found gid '" + gid + "' for group '" + groupname + "'");
			return gid;
		} catch (Exception e) {
			log.info("Couldn't retrieve gid for '" + groupname + "'", e);
			throw new RuntimeException("Couldn't retrieve gid for '" + groupname + "': " + e.getMessage(), e);
		} finally {
			releaseContext(context);
		}
	}

	private boolean isContextValid(DirContext context) {
		try {
			context.search(gumsObject, "(map=*)", null);
			return true;
		} catch (Exception e) {
			log.trace("Removing stale LDAP connection from pool " + context, e);
			gumsAdminLog.warn("LDAP connection test failed, discarding connection from pool: " + e.getMessage());
			return false;
		}
	}

	private void updateGID(String account, String gid) throws NamingException {
		DirContext context = retrievePeopleContext();
		try {
			ModificationItem[] mods = new ModificationItem[1];
			mods[0] = new ModificationItem(context.REPLACE_ATTRIBUTE, new BasicAttribute(gidNumberField, gid));
			context.modifyAttributes(uidField+"="+account+","+peopleObject, mods);
			log.trace("Changed primary gid for user '" + account + "' to gid '" + gid + "''");
		} catch (Exception e) {
			log.warn("Couldn't change gid for user '" + account + "' to gid '" + gid + "''", e);
			throw new RuntimeException("Couldn't change gid for user '" + account + "' to gid '" + gid + "''", e);
		} finally {
			releaseContext(context);
		}
	}

	protected DirContext createGroupContext() {
		try {
			Properties properties = (Properties)getProperties().clone();
			if (groupContext!=null)
				properties.setProperty("java.naming.provider.url", properties.getProperty("java.naming.provider.url")+"/"+(groupContext!=null?groupContext:""));
			log.info("Trying to create LDAP connection with properties: " + properties);
			return (DirContext)new InitialLdapContext(properties, null);
		} catch (NamingException e) {
			log.warn("Couldn't create LDAP connection: " + e.getMessage() + " - parameters: " + getProperties(), e);
			throw new RuntimeException("Couldn't create LDAP connection: " + e.getMessage());
		}
	}

	/** Create a new LDAP DirContext based on the configuration.
	 * 
	 * @return a new LDAP DirContext
	 */
	protected DirContext createGumsContext() {
		try {		 
			Properties properties = (Properties)getProperties().clone();
			if (gumsContext!=null)
				properties.setProperty(Context.PROVIDER_URL, properties.getProperty(Context.PROVIDER_URL)+"/"+(gumsContext!=null?gumsContext:""));
			log.info("Trying to create LDAP connection with properties: " + properties);
			return (DirContext)new InitialLdapContext(properties, null);
		} catch (NamingException e) {
			log.warn("Couldn't create LDAP connection: " + e.getMessage() + " - parameters: " + getProperties(), e);
			throw new RuntimeException("Couldn't create LDAP connection: " + e.getMessage());
		}
	}

	protected DirContext createPeopleContext() {
		try {
			Properties properties = (Properties)getProperties().clone();
			if (peopleContext!=null)
				properties.setProperty(Context.PROVIDER_URL, properties.getProperty(Context.PROVIDER_URL)+"/"+(peopleContext!=null?peopleContext:""));
			log.info("Trying to create LDAP connection with properties: " + properties);
			return (DirContext)new InitialLdapContext(properties, null);
		} catch (NamingException e) {
			log.warn("Couldn't create LDAP connection: " + e.getMessage() + " - parameters: " + getProperties(), e);
			throw new RuntimeException("Couldn't create LDAP connection: " + e.getMessage());
		}
	}	

}
