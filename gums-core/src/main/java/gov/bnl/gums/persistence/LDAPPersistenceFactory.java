
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

import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author Gabriele Carcassi, Jay Packard
 */
public class LDAPPersistenceFactory extends PersistenceFactory {
    private static String gumsOU = "ou=GUMS";
    static public String getTypeStatic() {
		return "ldap";
	}
    
    private Log log = LogFactory.getLog(LDAPPersistenceFactory.class);
    private Log adminLog = LogFactory.getLog(GUMS.resourceAdminLog);
    private boolean synchGroups;
    private List contexts = Collections.synchronizedList(new LinkedList());// *** LDAP connection pool management    
	private boolean skipReleaseContext = false;
	private String trustStore = System.getProperty("java.home")+"/lib/security/cacerts"; // doesn't do anything anymore because it required tomcat restart
	private String trustStorePassword = ""; // doesn't do anything anymore because it required tomcat restart
	private String caCertFile = ""; // doesn't do anything anymore because it required tomcat restart
	private String accountField = "uid";
	private String memberAccountField = "memberUid";
	private String groupIdField = "gidNumber";
    
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
        DirContext context = retrieveContext();
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
        DirContext context = retrieveContext();
        try {
            SearchControls ctrls = new SearchControls();
            ctrls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            NamingEnumeration result;
           	result = context.search("cn="+groupname+",ou=Group", "("+memberAccountField+"={0})", new Object[] {account}, ctrls);
            if (result.hasMore()) return;
            ModificationItem[] mods = new ModificationItem[1];
            mods[0] = new ModificationItem(context.ADD_ATTRIBUTE, new BasicAttribute(memberAccountField, account));
            context.modifyAttributes("cn="+groupname+",ou=Group", mods);
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
        DirContext context = retrieveContext();
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
     * Changes the primary gid for the given account.
     * 
     * @param account the account to change the primary group (i.e. "carcassi")
     * @param groupname the primary group name (i.e. "usatlas")
     */
    public void changeGroupID(String account, String groupname) {
        String gid = findGID(groupname);
        if (gid == null) {
        	log.error("GID for group '" + groupname + "' wasn't found.");
            throw new RuntimeException("GID for group '" + groupname + "' wasn't found.");
        }
        updateGID(account, gid);
    }

    public PersistenceFactory clone(Configuration configuration) {
    	LDAPPersistenceFactory persistenceFactory = new LDAPPersistenceFactory(configuration, new String(getName()));
    	persistenceFactory.setDescription(new String(getDescription()));
//    	persistenceFactory.setCaCertFile(getCaCertFile());
//   	persistenceFactory.setTrustStorePassword(getTrustStorePassword());
    	persistenceFactory.setAccountField(new String(getAccountField()));
    	persistenceFactory.setGroupIdField(new String(getGroupIdField()));
    	persistenceFactory.setMemberAccountField(new String(getMemberAccountField()));
    	persistenceFactory.setProperties((Properties)getProperties().clone());
    	persistenceFactory.setSynchGroups(persistenceFactory.isSynchGroups());
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
        DirContext context = retrieveContext();
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
        DirContext context = retrieveContext();
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
        DirContext context = retrieveContext();
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
        DirContext context = retrieveContext();
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
        DirContext context = retrieveContext();
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

    public String getAccountField() {
    	return accountField;
    }
    
    public String getCaCertFile() {
    	return caCertFile;
    }
    
    public String getGumsOU() {
    	return gumsOU;
    }
    
    public String getGroupIdField() {
    	return groupIdField;
    }
    
    /** 
     * Returns a Context ready to be used (taken from the pool).
     * This is the entry point for the pool, and it can be used
     * by test cases to prepare the LDAP server.
     * 
     * @return an LDAP context
     */
    public DirContext getLDAPContext() {
        return retrieveContext();
    }
        
    public String getMemberAccountField() {
    	return memberAccountField;
    }
    

    public String getTrustStorePassword() {
    	return trustStorePassword;
    }

    public String getType() {
		return "ldap";
	}

    /**
     * This property forces the gid update for account pools at every access.
     * It's handy for when gids gets out of synch.
     * 
     * @return if true gids are updated every time accounts from the pool are returned.
     */
    public boolean isSynchGroups() {
        return this.synchGroups;
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
        DirContext context = retrieveContext();
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
        DirContext context = retrieveContext();
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

    /** 
     * Retrieves an LDAP DirContext from the pool, if available and still valid,
     * or creates a new DirContext if none are found.
     * 
     * @return an LDAP DirContext
     */
    public DirContext retrieveContext() {
        DirContext context;
        while (contexts.size() != 0) {
            context = (DirContext) contexts.remove(0);
            if (isContextValid(context)) {
                log.trace("Using LDAP connection from pool " + context);
                return context;
            }
        }
        context = createLDAPContext();
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
    
    public UserGroupDB retrieveUserGroupDB(String name) {
        log.trace("Creating LDAP UserGroupDB '" + name + "'");
        return new LDAPUserGroupDB(this, name);
    }

    public void setAccountField(String accountField) {
    	this.accountField = accountField;
    }
    
    public void setCaCertFile(String caCertFile) {
       	//System.setProperty("javax.net.ssl.trustStore", trustStore );
    	this.caCertFile = caCertFile;
    	//if (!trustStorePassword.equals(""))
    		//addCertToTrustStore();
    }
    
    public void setGroupIdField(String groupIdField) {
    	this.groupIdField = groupIdField;
    }
    
    public void setMemberAccountField(String memberAccountField) {
    	this.memberAccountField = memberAccountField;
    }
    
    /**
     * Sets the list of properties to be used to connect to LDAP, that is
     * to create the JNDI context.
     * 
     * @param properties a set of JNDI properties
     */
    public void setProperties(Properties properties) {
        if (properties.getProperty("java.naming.factory.initial") == null) {
            properties.put("java.naming.factory.initial", "com.sun.jndi.ldap.LdapCtxFactory");
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
     * This property forces the gid update for account pools at every access.
     * It's handy for when gids gets out of synch.
     * @param synchGroups if true gids are updated every time accounts from the pool are returned.
     */
    public void setSynchGroups(boolean synchGroups) {
        this.synchGroups = synchGroups;
    }
    
    public void setTrustStorePassword(String trustStorePassword) {
       	//System.setProperty("javax.net.ssl.trustStorePassword", trustStorePassword );
        this.trustStorePassword = trustStorePassword;
    	//if (!caCertFile.equals(""))
    		//addCertToTrustStore();
    }
    
    public String toXML() {
    	String retStr = "\t\t<ldapPersistenceFactory\n"+
    		"\t\t\tname='"+getName()+"'\n"+
    		"\t\t\tdescription='"+getDescription()+"'\n"+
    		"\t\t\tsynchGroups='"+synchGroups+"'\n"+
//			"\t\t\tcaCertFile='"+getCaCertFile()+"'\n"+
//			"\t\t\ttrustStorePassword='"+trustStorePassword+"'\n"+
			"\t\t\tgroupIdField='"+groupIdField+"'\n"+
			"\t\t\taccountField='"+accountField+"'\n"+
			"\t\t\tmemberAccountField='"+memberAccountField+"'\n";
    	
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
    
    private void addCertToTrustStore() {
    	X509Certificate cert = null;
    	try {
			InputStream inStream = new FileInputStream(caCertFile);
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			cert = (X509Certificate)cf.generateCertificate(inStream);
			inStream.close();
		} catch (Exception e) {
            log.error("Cannot open " + caCertFile, e);
            adminLog.error("Cannot open " + caCertFile + ": " + e.getMessage());
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
            adminLog.error("Couldn't put " + caCertFile + " into trust store: " + e.getMessage() );
		}
    }
    
    private String findGID(String groupname) {
        DirContext context = retrieveContext();
        try {
            NamingEnumeration result = context.search("ou=Group", "(cn={0})", new Object[] {groupname}, null);
            String gid = null;
            if (result.hasMore()) {
                SearchResult item = (SearchResult) result.next();
                Attributes atts = item.getAttributes();
                Attribute gidAtt = atts.get(groupIdField);
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
            context.search(gumsOU, "(map=*)", null);
            return true;
        } catch (Exception e) {
            log.trace("Removing stale LDAP connection from pool " + context, e);
            adminLog.warn("LDAP connection test failed, discarding connection from pool: " + e.getMessage());
            return false;
        }
    }
    
    private void updateGID(String account, String gid) {
        DirContext context = retrieveContext();
        try {
            ModificationItem[] mods = new ModificationItem[1];
            mods[0] = new ModificationItem(context.REPLACE_ATTRIBUTE, new BasicAttribute(groupIdField, gid));
            context.modifyAttributes(accountField+"="+account+",ou=People", mods);
            log.trace("Changed primary gid for user '" + account + "' to gid '" + gid + "''");
        } catch (Exception e) {
            log.warn("Couldn't change gid for user '" + account + "' to gid '" + gid + "''", e);
            throw new RuntimeException("Couldn't change gid for user '" + account + "' to gid '" + gid + "''", e);
        } finally {
            releaseContext(context);
        }
    }
    
    /** Create a new LDAP DirContext based on the configuration.
     * 
     * @return a new LDAP DirContext
     */
    protected DirContext createLDAPContext() {
        try {
            log.info("Trying to create LDAP connection with properties: " + getProperties());
            return new InitialLdapContext(getProperties(), null);
        } catch (NamingException e) {
            log.warn("Couldn't create LDAP connection: " + e.getMessage() + " - parameters: " + getProperties(), e);
            throw new RuntimeException("Couldn't create LDAP connection: " + e.getMessage());
        }
    }

}
