/*
 * LDAPMapDB.java
 *
 * Created on September 23, 2005, 11:29 AM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package gov.bnl.gums.db;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NamingEnumeration;
import javax.naming.directory.*;
import javax.naming.ldap.Control;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.PagedResultsControl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


import gov.bnl.gums.persistence.LDAPPersistenceFactory;

/**
 *
 * @author carcassi
 */
public class LDAPMappingDB implements AccountPoolMapperDB, ManualAccountMapperDB {
    private Log log = LogFactory.getLog(LDAPMappingDB.class);

    private LDAPPersistenceFactory factory;
    private String map;
    private String mapDN;
    private String group;
    private List secondaryGroups;
    
    /**
     * Creates a new LDAP map, named "map=map" in the defaultGumsOU.
     * @param factory the LDAP factory that will provide LDAP connectivity
     * @param map the name of the map
     */
    public LDAPMappingDB(LDAPPersistenceFactory factory, String map) {
        this.factory = factory;
        this.map = map;
        this.mapDN = "map=" + map + "," + factory.getDefaultGumsOU();
        createGroupIfNotExists();
        log.trace("LDAPMapDB object create: map '" + map + "' factory " + factory);
    }

    /**
     * Creates a new LDAP map, named "map=map" in the defaultGumsOU.
     * When accounts are assigned they will be associated with the gids for the
     * UNIX groups given.
     *
     * @param factory the LDAP factory that will provide LDAP connectivity
     * @param map the name of the map
     * @param group the UNIX primary group for the accounts assigned
     * @param secondaryGroups the UNIX secondary groups for the accounts assigned
     */
    public LDAPMappingDB(LDAPPersistenceFactory factory, String map, String group, List secondaryGroups) {
        this(factory, map);
        this.group = group;
        this.secondaryGroups = secondaryGroups;
        log.trace("LDAPMapDB object create: map '" + map + "' factory " + factory + " primary group '" + group + "' secondary groups '" + secondaryGroups + "'");
    }
    
    /**
     * Checks whether the map actually exists in LDAP.
     * @return true if it exists
     */
    boolean doesMapExist() {
        DirContext context = factory.retrieveContext();
        try {
            SearchControls ctrls = new SearchControls();
            ctrls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            log.trace("Checking if LDAP map '" + map + "' exists");
            NamingEnumeration result = context.search(factory.getDefaultGumsOU(), "(map={0})", new Object[] {map}, ctrls);
            return result.hasMore();
        } catch (Exception e) {
            log.error("Couldn't determine if LDAP map exists '" + map + "'", e);
            throw new RuntimeException("Couldn't determine if LDAP map exists '" + map + "': " + e.getMessage(), e);
        } finally {
            factory.releaseContext(context);
        }
    }
    
    /**
     * Creates the group in LDAP if it doesn't exists.
     */
    void createGroupIfNotExists() {
        if (!doesMapExist()) {
            factory.createMap(map, mapDN);
            log.trace("LDAP group '" + map + "' didn't exist, and it was created");
        }
    }

    /**
     * Retrieve the mapping for the certificate DN of the user.
     * @param userDN full certificate DN of the user
     * @return the UNIX account to be mapped to
     */
    public String retrieveMapping(String userDN) {
        DirContext context = factory.retrieveContext();
        try {
            SearchControls ctrls = new SearchControls();
            ctrls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            NamingEnumeration result = context.search(mapDN, "(user={0})", new Object[] {userDN}, ctrls);
            if (result.hasMore()) {
                SearchResult res = (SearchResult) result.next();
                Attributes atts = res.getAttributes();
                Attribute map = atts.get("account");
                if (map == null)
                    return null;
                String account = (String) map.get();
                log.trace("Retrieved map entry in map '" + map + "' for user '" + userDN + "' to account '" + account + "'");
                return account;
            }
            return null;
        } catch (Exception e) {
            log.error("Couldn't retrieve entry from LDAP map '" + map + "' for user '" + userDN + "'", e);
            throw new RuntimeException("Couldn't retrieve entry from LDAP map '" + map + "' for user '" + userDN + "': " + e.getMessage(), e);
        } finally {
            factory.releaseContext(context);
        }
    }
    
    public java.util.List retrieveMappings() {
    	return null;
    }

    /**
     * Remove the mapping associated to the certificate DN of the user provided.
     * @param userDN full certificate DN of the user
     * @return true if a mapping was actually removed
     */
    public boolean removeMapping(String userDN) {
        try {
            return factory.removeMapEntry(userDN, map, mapDN);
        } catch (RuntimeException e) {
            if (e.getCause() instanceof NoSuchAttributeException)
                return false;
            throw e;
        }
    }

    /**
     * For an account pool, frees the account that was associated to a particular
     * user.
     * @param userDN full certificate DN of the user
     */
    public void unassignUser(String userDN) {
        factory.removeMapEntry(userDN, map, mapDN);
    }

    /**
     * Retrieves the account associated with the certificate DN of the user.
     * @param userDN full certificate DN of the user
     * @return the UNIX account or null if no associated account was found
     */
    public String retrieveAccount(String userDN) {
        String account = retrieveMapping(userDN);
        log.trace("Retrieving account from LDAP map '" + map + "' for user '" + userDN + "' account '" + account + "'");
        if (account != null) {
            factory.retrieveAssigner().reassignGroups(account, group, secondaryGroups);
            log.trace("Reassigned gids for user '" + userDN + "' account '" + account + "'");
        }
        return account;
    }

    /**
     * Assigns a new account taken from the pool to the certificate DN of the user.
     * @param userDN full certificate DN of the user
     * @return the UNIX account or null if no free account was found
     */
    public String assignAccount(String userDN) {
        DirContext context = factory.retrieveContext();
        String account = null;
        Control[] controlsBackup = null;
        try {
            LdapContext ldapContext = (LdapContext) context;
            controlsBackup = ldapContext.getRequestControls();
            ldapContext.setRequestControls(new Control[] {new PagedResultsControl(100, Control.CRITICAL)});
            SearchControls ctrls = new SearchControls();
            ctrls.setSearchScope(SearchControls.ONELEVEL_SCOPE);
            NamingEnumeration result = context.search(mapDN, "(!(user=*))", null, ctrls);
            while (result.hasMore()) {
                SearchResult res = (SearchResult) result.next();
                Attributes atts = res.getAttributes();
                Attribute accounts = atts.get("account");
                if (accounts != null) {
                    String newAccount = (String) accounts.get();
                    if (account == null) {
                        account = newAccount;
                    } else {
                        if (account.compareTo(newAccount) > 0) {
                            account = newAccount;
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Couldn't assign account from LDAP map '" + map + "' to user '" + userDN + "'", e);
            throw new RuntimeException("Couldn't assign account from LDAP map '" + map + "' to user '" + userDN + "': " + e.getMessage(), e);
        } finally {
            if (controlsBackup != null) {
                try {
                    ((LdapContext) context).setRequestControls(controlsBackup);
                } catch (Exception e) {
                    log.error("Couldn't reset controls", e);
                }
            }
            factory.releaseContext(context);
        }
        if (account != null) {
            factory.retrieveAssigner().assignGroups(account, group, secondaryGroups);
            log.trace("Assigned gids for user '" + userDN + "' account '" + account + "'");
            factory.addMapEntry(userDN, account, map, mapDN);
            log.trace("Assigned account for LDAP map '" + map + "' user '" + userDN + "' account '" + account + "'");
        } else {
            log.trace("No account to assign for LDAP map '" + map + "' user '" + userDN + "' account '" + account + "'");
        }
        return account;
    }

    /**
     * Adds an account to the pool of accounts available.
     * @param account a UNIX account
     */
    public void addAccount(String account) {
        try {
            factory.createAccountInMap(account, map, mapDN);
        } catch (Exception e) {
            if (e.getCause() instanceof NameAlreadyBoundException) {
                throw new IllegalArgumentException("Account '" + account + "' is already present in LDAP pool '" + map + "'");
            }
        }
    }

    /**
     * This is not supported anymore
     * @param date ignored
     * @return nothing
     * @throws UnsupportedOperationException
     */
    public java.util.List retrieveUsersNotUsedSince(java.util.Date date) {
        throw new UnsupportedOperationException("retrieveUsersNotUsedSince is not supported anymore");
    }

    /**
     * Creates a new mapping in the map, associating a certificate DN with an account.
     * @param userDN full certificate DN of the user
     * @param account a UNIX account
     */
    public void createMapping(String userDN, String account) {
        factory.addMapEntry(userDN, account, map, mapDN);
    }

    /**
     * Retrieves the full association of certificates DNs to accounts that
     * the map has stored.
     * @return a Map with the certificate DN as the key and the account as the value
     */
    public java.util.Map retrieveAccountMap() {
        DirContext context = factory.retrieveContext();
        Map map = new Hashtable();
        try {
            SearchControls ctrls = new SearchControls();
            ctrls.setSearchScope(SearchControls.ONELEVEL_SCOPE);
            NamingEnumeration result = context.search(mapDN, "(user=*)", null, ctrls);
            while (result.hasMore()) {
                SearchResult res = (SearchResult) result.next();
                Attributes atts = res.getAttributes();
                Attribute accounts = atts.get("account");
                if (accounts != null) {
                    String account = (String) accounts.get();
                    Attribute users = atts.get("user");
                    String user = (String) users.get();
                    map.put(user, account);
                }
            }
            log.trace("Retrieved LDAP map '" + map + "'");
            return map;
        } catch (Exception e) {
            log.error("Couldn't retrieve LDAP map '" + map + "'", e);
            throw new RuntimeException("Couldn't retrieve LDAP map '" + map + "': " + e.getMessage(), e);
        } finally {
            factory.releaseContext(context);
        }
    }
    
    public void resetAccountPool() {
        DirContext context = factory.retrieveContext();
        try {
            SearchControls ctrls = new SearchControls();
            ctrls.setSearchScope(SearchControls.ONELEVEL_SCOPE);
            NamingEnumeration result = context.search(mapDN, "(user=*)", null, ctrls);
            while (result.hasMore()) {
                SearchResult res = (SearchResult) result.next();
                Attributes atts = res.getAttributes();
                Attribute accounts = atts.get("account");
                if (accounts != null) {
                    String account = (String) accounts.get();
                    Attribute users = atts.get("user");
                    String user = (String) users.get();
                    ModificationItem[] mods = new ModificationItem[1];
                    mods[0] = new ModificationItem(context.REMOVE_ATTRIBUTE,
                        new BasicAttribute("user", user));
                    context.modifyAttributes("account=" + account + "," + mapDN, mods);
                }
            }
            log.trace("Reset LDAP account pool '" + map + "'");
        } catch (Exception e) {
            log.error("Couldn't reset LDAP account pool '" + map + "'", e);
            throw new RuntimeException("Couldn't reset LDAP account pool '" + map + "': " + e.getMessage(), e);
        } finally {
            factory.releaseContext(context);
        }
    }
    
}
