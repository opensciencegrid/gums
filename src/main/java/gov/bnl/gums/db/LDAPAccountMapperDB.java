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

import gov.bnl.gums.GUMS;
import gov.bnl.gums.GridUser;
import gov.bnl.gums.persistence.LDAPPersistenceFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.naming.NameAlreadyBoundException;
import javax.naming.NamingEnumeration;
import javax.naming.directory.*;
import javax.naming.ldap.Control;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.PagedResultsControl;

import org.apache.log4j.Logger;

/**
 * 
 * @author Gabriele Carcassi, Jay Packard
 */
public class LDAPAccountMapperDB implements AccountPoolMapperDB, ManualAccountMapperDB {
	private Logger log = Logger.getLogger(LDAPAccountMapperDB.class);
	private LDAPPersistenceFactory factory;
	private String map;
	private String mapDN;
	private String group;
	private List secondaryGroups;
	private static Map needsCacheRefresh = Collections.synchronizedMap(new HashMap());

	/**
	 * Creates a new LDAP map, named "map=map" in the defaultGumsOU.
	 * 
	 * @param factory the LDAP factory that will provide LDAP connectivity
	 * @param map the name of the map
	 */
	public LDAPAccountMapperDB(LDAPPersistenceFactory factory, String map) {
		this.factory = factory;
		this.map = map;
		this.mapDN = "map=" + map + "," + factory.getGumsObject();
		createGroupIfNotExists();
		log.trace("LDAPMapDB object create: map '" + map + "' factory "
				+ factory);
	}

	/**
	 * Creates a new LDAP map, named "map=map" in the defaultGumsOU. When
	 * accounts are assigned they will be associated with the gids for the UNIX
	 * groups given.
	 * 
	 * @param factorythe LDAP factory that will provide LDAP connectivity
	 * @param mapthe name of the map
	 * @param groupthe UNIX primary group for the accounts assigned
	 * @param secondaryGroups the UNIX secondary groups for the accounts assigned
	 */
	public LDAPAccountMapperDB(LDAPPersistenceFactory factory, String map, String group, List secondaryGroups) {
		this(factory, map);
		this.group = group;
		this.secondaryGroups = secondaryGroups;
		if (group==null)
			log.info("No primary group: factory '" + factory + "'");
		log.trace("LDAPMapDB object create: map '" + map + "' factory "
				+ factory + " primary group '" + group + "' secondary groups '"
				+ secondaryGroups + "'");
	}

	public void addAccount(String account) {
		try {
			factory.createAccountInMap(account, map, mapDN);
			setNeedsCacheRefresh(true);
		} catch (Exception e) {
			if (e.getCause() instanceof NameAlreadyBoundException) {
				throw new IllegalArgumentException("Account '" + account
						+ "' is already present in LDAP pool '" + map + "'");
			}
		}
	}
	
	public String assignAccount(GridUser user) {
		DirContext context = factory.retrieveGumsDirContext();
		String account = null;
		Control[] controlsBackup = null;
		String userDN = user.getCertificateDN();
		String email = user.getEmail();
		try {
			LdapContext ldapContext = (LdapContext) context;
			controlsBackup = ldapContext.getRequestControls();
			ldapContext.setRequestControls(new Control[] { new PagedResultsControl(	100, Control.CRITICAL) });
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
			log.error("Couldn't assign account from LDAP map '" + map
					+ "' to user '" + userDN + "'", e);
			throw new RuntimeException(
					"Couldn't assign account from LDAP map '" + map
							+ "' to user '" + userDN + "': " + e.getMessage(),
					e);
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
			if (group!=null) {
				assignGroups(account, group, secondaryGroups);
				log.trace("Assigned gids for user '" + userDN + "' account '" + account + "'");
			}
			if (email!=null) {
				assignEmail(account, email);
				log.trace("Assigned email for account '" + account + "' to email '" + email + "'");
			}
			factory.addMapEntry(userDN, account, map, mapDN);
			log.trace("Assigned account for LDAP map '" + map + "' user '"
					+ userDN + "' account '" + account + "'");
		} else {
			log.trace("No account to assign for LDAP map '" + map + "' user '"
					+ userDN + "' account '" + account + "'");
		}
		setNeedsCacheRefresh(true);
		
		return account;
	}
	
	public void createGroupIfNotExists() {
		if (!doesMapExist()) {
			factory.createMap(map, mapDN);
			log.trace("LDAP group '" + map + "' didn't exist, and it was created");
			setNeedsCacheRefresh(true);
		}
	}
	
	public void createMapping(String userDN, String account) {
		factory.addMapEntry(userDN, account, map, mapDN);
		setNeedsCacheRefresh(true);
	}

	public boolean doesMapExist() {
		DirContext context = factory.retrieveGumsDirContext();
		try {
			SearchControls ctrls = new SearchControls();
			ctrls.setSearchScope(SearchControls.SUBTREE_SCOPE);
			log.trace("Checking if LDAP map '" + map + "' exists");
			NamingEnumeration result = context.search(factory.getGumsObject(), "(map={0})", new Object[] { map },
					ctrls);
			return result.hasMore();
		} catch (Exception e) {
			log.error("Couldn't determine if LDAP map exists '" + map + "'", e);
			throw new RuntimeException(
					"Couldn't determine if LDAP map exists '" + map + "': "
							+ e.getMessage(), e);
		} finally {
			factory.releaseContext(context);
		}
	}

    public String getMap() {
    	return map;
    }
	
	public boolean needsCacheRefresh() {
		if (needsCacheRefresh.get(map) != null)
			return ((Boolean)needsCacheRefresh.get(map)).booleanValue();
		else
			return true;
	}

	public boolean removeAccount(String account) {
		try {
			boolean retVal = factory.destroyAccountInMap(account, map, mapDN);
			setNeedsCacheRefresh(true);
			return retVal;
		} catch (RuntimeException e) {
			if (e.getCause() instanceof NameAlreadyBoundException)
				throw new IllegalArgumentException("Cannot remove '" + account + "' from LDAP pool '" + map + "'");
			throw e;
		}
	}

	public boolean removeMapping(String userDN) {
		try {
			boolean retVal = factory.removeMapEntry(userDN, map, mapDN);
			setNeedsCacheRefresh(true);
			return retVal;
		} catch (RuntimeException e) {
			if (e.getCause() instanceof NoSuchAttributeException)
				return false;
			throw e;
		}
	}

	public String retrieveAccount(GridUser user) {
		String userDN = user.getCertificateDN();
		String account = retrieveMapping(userDN);
		log.trace("Retrieving account from LDAP map '" + map + "' for user '"
				+ userDN + "' account '" + account + "'");
		if (account != null) {
			reassignGroups(account, group, secondaryGroups);
			reassignEmail(account, user.getEmail());
			log.trace("Reassigned gids for user '" + userDN + "' account '"
					+ account + "'");
		}
		return account;
	}

	public java.util.Map retrieveAccountMap() {
		DirContext context = factory.retrieveGumsDirContext();
		Map map = new Hashtable();
		try {
			SearchControls ctrls = new SearchControls();
			ctrls.setSearchScope(SearchControls.ONELEVEL_SCOPE);
			NamingEnumeration result = context.search(mapDN, "(user=*)", null,
					ctrls);
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
			throw new RuntimeException("Couldn't retrieve LDAP map '" + map
					+ "': " + e.getMessage(), e);
		} finally {
			factory.releaseContext(context);
		}
	}

	public String retrieveMapping(String userDN) {
		DirContext context = factory.retrieveGumsDirContext();
		try {
			SearchControls ctrls = new SearchControls();
			ctrls.setSearchScope(SearchControls.SUBTREE_SCOPE);
			NamingEnumeration result = context.search(mapDN, "(user={0})",
					new Object[] { userDN }, ctrls);
			if (result.hasMore()) {
				SearchResult res = (SearchResult) result.next();
				Attributes atts = res.getAttributes();
				Attribute map = atts.get("account");
				if (map == null)
					return null;
				String account = (String) map.get();
				log.trace("Retrieved map entry in map '" + map + "' for user '"
						+ userDN + "' to account '" + account + "'");
				return account;
			}
			return null;
		} catch (Exception e) {
			log.error("Couldn't retrieve entry from LDAP map '" + map
					+ "' for user '" + userDN + "'", e);
			throw new RuntimeException(
					"Couldn't retrieve entry from LDAP map '" + map
							+ "' for user '" + userDN + "': " + e.getMessage(),
					e);
		} finally {
			factory.releaseContext(context);
		}
	}

	public java.util.Map retrieveReverseAccountMap() {
		DirContext context = factory.retrieveGumsDirContext();
		Map map = new Hashtable();
		try {
			SearchControls ctrls = new SearchControls();
			ctrls.setSearchScope(SearchControls.ONELEVEL_SCOPE);
			NamingEnumeration result = context.search(mapDN, "(account=*)",
					null, ctrls);
			while (result.hasMore()) {
				SearchResult res = (SearchResult) result.next();
				Attributes atts = res.getAttributes();
				Attribute accounts = atts.get("account");
				String account = (String) accounts.get();
				Attribute users = atts.get("user");
				String user = users!=null ? (String) users.get() : "";
				map.put(account, user);
			}
			log.trace("Retrieved LDAP map '" + map + "'");
			return map;
		} catch (Exception e) {
			log.error("Couldn't retrieve LDAP map '" + map + "'", e);
			throw new RuntimeException("Couldn't retrieve LDAP map '" + map
					+ "': " + e.getMessage(), e);
		} finally {
			factory.releaseContext(context);
		}
	}

	public java.util.List retrieveUsersNotUsedSince(java.util.Date date) {
		throw new UnsupportedOperationException(
				"retrieveUsersNotUsedSince is not supported anymore");
	}
	
	public String retrieveEmail(String account) {
		return factory.retrieveEmail(account);
	}

	public synchronized void setCacheRefreshed() {
		needsCacheRefresh.put(map, new Boolean(false));
	}

	public void unassignAccount(String account) {
		DirContext context = factory.retrieveGumsDirContext();
		try {
			SearchControls ctrls = new SearchControls();
			ctrls.setSearchScope(SearchControls.ONELEVEL_SCOPE);
			NamingEnumeration result = context.search(mapDN, "(account={0})", new Object[] { account }, ctrls);
			while (result.hasMore()) {
				SearchResult res = (SearchResult) result.next();
				Attributes atts = res.getAttributes();
				Attribute users = atts.get("user");
				if (users!=null) {
					String user = (String) users.get();
					factory.removeMapEntry(user, map, mapDN);
				}
			}
			setNeedsCacheRefresh(true);
			log.trace("Unassigned account '" + account + "' at LDAP map '" + map + "'");
		} catch (Exception e) {
			log.error("Unassigned account '" + account + "' at LDAP map '" + map + "'", e);
			throw new RuntimeException("Couldn't retrieve LDAP map '" + map
					+ "': " + e.getMessage(), e);
		} finally {
			factory.releaseContext(context);
		}
	}
	

	public void unassignUser(String userDN) {
		factory.removeMapEntry(userDN, map, mapDN);
		setNeedsCacheRefresh(true);
	}
    
    /**
     * Assigns the groups to the account.
     * 
     * @param account A UNIX account (i.e. 'carcassi')
     * @param primary A UNIX group name (i.e. 'usatlas')
     * @param secondary A list of Strings representing secondary UNIX group names
     */
	private void assignGroups(String account, String primaryGroup, List secondaryGroups) {
        try {
        	factory.changeGroupID(account, primaryGroup);
            log.trace("Assigned '" + primaryGroup + "' to '" + account + "'");
            if (secondaryGroups == null) return;
            Iterator iter = secondaryGroups.iterator();
            while (iter.hasNext()) {
                String group = (String) iter.next();
                factory.addToSecondaryGroup(account, group);
                log.trace("Assigned secondary group '" + group + "' to '" + account + "'");
            }
        } catch (Exception e) {
            log.warn("Couldn't assign GIDs. account '" + account + "' - primary group '" + primaryGroup + "' - secondary '" + secondaryGroups + "'", e);
            throw new RuntimeException("Couldn't assign GIDs: " + e.getMessage() + ". account '" + account + "' - primary group '" + primaryGroup + "' - secondary '" + secondaryGroups + "' - " + e.getMessage());
        }
    }
	
    /**
     * Assigns the email to an account.
     * 
     * @param account A UNIX account (i.e. 'carcassi')
     * @param email
     */
	private void assignEmail(String account, String email) {
        try {
        	factory.changeEmail(account, email);
            log.trace("Assigned '" + email + "' to '" + account + "'");
        } catch (Exception e) {
            log.warn("Couldn't assign email. account '" + account + "' - email '" + email + "'", e);
            throw new RuntimeException("Couldn't assign email: " + e.getMessage() + ". account '" + account + "' - email '" + email + "' - " + e.getMessage());
        }
    }
    
    /**
     * Reassigns the groups to the account, refreshing something that should be
     * already be present in LDAP. The LDAP factory controls whether this
     * actually is performed by setting the synchGroups property.
     * 
     * @param account A UNIX account (i.e. 'carcassi')
     * @param primary A UNIX group name (i.e. 'usatlas')
     * @param secondary A list of Strings representing secondary UNIX group names
     */
	private void reassignGroups(String account, String primary, List secondary) {
        if (factory.isSynch()) {
            assignGroups(account, primary, secondary);
        } else {
            log.trace("Skip reassign groups for account '" + account + "' - primary group '" + primary + "' - secondary '" + secondary + "'");
        }
    }
	
    /**
     * Reassigns the email to the account, refreshing something that should be
     * already be present in LDAP.
     * 
     * @param account A UNIX account (i.e. 'carcassi')
     * @param email
     */
	private void reassignEmail(String account, String email) {
        if (factory.isSynch()) {
            assignEmail(account, email);
        } else {
            log.trace("Skip reassign email for account '" + account + "' - email '" + email + "'");
        }
    }	
	
    private void setNeedsCacheRefresh(boolean value) {
    	needsCacheRefresh.put(map, new Boolean(value));
    }

}
