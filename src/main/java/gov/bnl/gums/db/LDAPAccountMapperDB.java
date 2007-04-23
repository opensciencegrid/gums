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

import gov.bnl.gums.persistence.LDAPPersistenceFactory;

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

/**
 * 
 * @author Gabriele Carcassi, Jay Packard
 */
public class LDAPAccountMapperDB implements AccountPoolMapperDB, ManualAccountMapperDB {
	private Log log = LogFactory.getLog(LDAPAccountMapperDB.class);
	private LDAPPersistenceFactory factory;
	private String map;
	private String mapDN;
	private String group;
	private List secondaryGroups;

	/**
	 * Creates a new LDAP map, named "map=map" in the defaultGumsOU.
	 * 
	 * @param factory the LDAP factory that will provide LDAP connectivity
	 * @param map the name of the map
	 */
	public LDAPAccountMapperDB(LDAPPersistenceFactory factory, String map) {
		this.factory = factory;
		this.map = map;
		this.mapDN = "map=" + map + "," + factory.getDefaultGumsOU();
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
	public LDAPAccountMapperDB(LDAPPersistenceFactory factory, String map,
			String group, List secondaryGroups) {
		this(factory, map);
		this.group = group;
		this.secondaryGroups = secondaryGroups;
		log.trace("LDAPMapDB object create: map '" + map + "' factory "
				+ factory + " primary group '" + group + "' secondary groups '"
				+ secondaryGroups + "'");
	}

	public void addAccount(String account) {
		try {
			factory.createAccountInMap(account, map, mapDN);
		} catch (Exception e) {
			if (e.getCause() instanceof NameAlreadyBoundException) {
				throw new IllegalArgumentException("Account '" + account
						+ "' is already present in LDAP pool '" + map + "'");
			}
		}
	}

	public String assignAccount(String userDN) {
		DirContext context = factory.retrieveContext();
		String account = null;
		Control[] controlsBackup = null;
		try {
			LdapContext ldapContext = (LdapContext) context;
			controlsBackup = ldapContext.getRequestControls();
			ldapContext
					.setRequestControls(new Control[] { new PagedResultsControl(
							100, Control.CRITICAL) });
			SearchControls ctrls = new SearchControls();
			ctrls.setSearchScope(SearchControls.ONELEVEL_SCOPE);
			NamingEnumeration result = context.search(mapDN, "(!(user=*))",
					null, ctrls);
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
			factory.retrieveAssigner().assignGroups(account, group,
					secondaryGroups);
			log.trace("Assigned gids for user '" + userDN + "' account '"
					+ account + "'");
			factory.addMapEntry(userDN, account, map, mapDN);
			log.trace("Assigned account for LDAP map '" + map + "' user '"
					+ userDN + "' account '" + account + "'");
		} else {
			log.trace("No account to assign for LDAP map '" + map + "' user '"
					+ userDN + "' account '" + account + "'");
		}
		return account;
	}

	public void createGroupIfNotExists() {
		if (!doesMapExist()) {
			factory.createMap(map, mapDN);
			log.trace("LDAP group '" + map
					+ "' didn't exist, and it was created");
		}
	}

	public void createMapping(String userDN, String account) {
		factory.addMapEntry(userDN, account, map, mapDN);
	}

	public boolean doesMapExist() {
		DirContext context = factory.retrieveContext();
		try {
			SearchControls ctrls = new SearchControls();
			ctrls.setSearchScope(SearchControls.SUBTREE_SCOPE);
			log.trace("Checking if LDAP map '" + map + "' exists");
			NamingEnumeration result = context.search(factory
					.getDefaultGumsOU(), "(map={0})", new Object[] { map },
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

	public boolean removeAccount(String account) {
		try {
			return factory.destroyAccountInMap(account, map, mapDN);
		} catch (RuntimeException e) {
			if (e.getCause() instanceof NameAlreadyBoundException)
				throw new IllegalArgumentException("Cannot remove '" + account + "' from LDAP pool '" + map + "'");
			throw e;
		}
	}

	public boolean removeMapping(String userDN) {
		try {
			return factory.removeMapEntry(userDN, map, mapDN);
		} catch (RuntimeException e) {
			if (e.getCause() instanceof NoSuchAttributeException)
				return false;
			throw e;
		}
	}

	public String retrieveAccount(String userDN) {
		String account = retrieveMapping(userDN);
		log.trace("Retrieving account from LDAP map '" + map + "' for user '"
				+ userDN + "' account '" + account + "'");
		if (account != null) {
			factory.retrieveAssigner().reassignGroups(account, group,
					secondaryGroups);
			log.trace("Reassigned gids for user '" + userDN + "' account '"
					+ account + "'");
		}
		return account;
	}

	public java.util.Map retrieveAccountMap() {
		DirContext context = factory.retrieveContext();
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
		DirContext context = factory.retrieveContext();
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

	public java.util.List retrieveMappings() {
		return null;
	}

	public java.util.Map retrieveReverseAccountMap() {
		DirContext context = factory.retrieveContext();
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

	public void unassignAccount(String account) {
		DirContext context = factory.retrieveContext();
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
	}

}
