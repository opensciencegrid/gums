/*
 * GUMSAPIImpl.java
 *
 * Created on November 1, 2004, 12:18 PM
 */

package gov.bnl.gums.admin;

import gov.bnl.gums.*;
import gov.bnl.gums.configuration.Configuration;
import gov.bnl.gums.configuration.FileConfigurationStore;
import gov.bnl.gums.userGroup.UserGroup;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import gov.bnl.gums.db.AccountPoolMapperDB;
import gov.bnl.gums.db.ManualAccountMapperDB;
import gov.bnl.gums.db.ManualUserGroupDB;

import gov.bnl.gums.persistence.PersistenceFactory;

/**
 *
 * @author  carcassi
 */
public class GUMSAPIImpl implements GUMSAPI {
    private Log log = LogFactory.getLog(GUMSAPI.class);
    private Log gumsResourceAdminLog = LogFactory.getLog(GUMS.resourceAdminLog);
    private Log siteLog = LogFactory.getLog(GUMS.siteAdminLog);
    private static GUMS gums;
    
    private GUMS gums() {
        if (gums == null) {
        	String confPath = CertCache.getConfPath();
        	FileConfigurationStore confStore = new FileConfigurationStore(confPath, !(new File(confPath).exists()));
            gums = new GUMS(confStore);
        }
        return gums;
    }
    
    public String generateGrid3UserVoMap(String hostname) {
        try {
            if (hasReadAllAccess(currentUser())) {
                String map = gums().getResourceManager().generateGrid3UserVoMap(hostname);
                gumsResourceAdminLog.info(logUserAccess() + "Generated grid3 vo-user map for host '" + hostname + "': " + map);
                return map;
            } else {
                throw new AuthorizationDeniedException();
            }
        } catch (AuthorizationDeniedException e) {
            gumsResourceAdminLog.info(logUserAccess() + "Failed to generate grid3 vo-user map for host '" + hostname + "' - " + e.getMessage());
            siteLog.info(logUserAccess() + "Unauthorized access to generate grid3 vo-user map for host '" + hostname + "'");
            throw e;
        } catch (RuntimeException e) {
            gumsResourceAdminLog.error(logUserAccess() + "Failed to generate grid3 vo-user map for host '" + hostname + "' - " + e.getMessage());
            throw e;
        }
    }
    
    public String generateGridMapfile(String hostname) {
        try {
            if (hasReadAllAccess(currentUser())) {
                String map = gums().getResourceManager().generateGridMapfile(hostname);
                gumsResourceAdminLog.info(logUserAccess() + "Generated mapfile for host '" + hostname + "': " + map);
                return map;
            } else {
                throw new AuthorizationDeniedException();
            }
        } catch (AuthorizationDeniedException e) {
            gumsResourceAdminLog.info(logUserAccess() + "Failed to generate mapfile for host '" + hostname + "' - " + e.getMessage());
            siteLog.info(logUserAccess() + "Unauthorized access to generate mapfile for host '" + hostname + "'");
            throw e;
        } catch (RuntimeException e) {
            gumsResourceAdminLog.error(logUserAccess() + "Failed to generate mapfile for host '" + hostname + "' - " + e.getMessage());
            throw e;
        }
    }
    
    public void manualGroupAdd(String persistanceManager, String group, String userDN) {
        try {
           if (hasWriteAccess(currentUser())) {
                PersistenceFactory factory = (PersistenceFactory) gums().getConfiguration().getPersistenceFactories().get(persistanceManager);
                ManualUserGroupDB db = factory.retrieveManualUserGroupDB(group);
                db.addMember(new GridUser(userDN, null));
                gumsResourceAdminLog.info(logUserAccess() + "Added to persistence '" + persistanceManager + "' group '" + group + "'  user '" + userDN + "'");
                siteLog.info(logUserAccess() + "Added to persistence '" + persistanceManager + "' group '" + group + "'  user '" + userDN + "'");
            } else {
                throw new AuthorizationDeniedException();
            }
        } catch (AuthorizationDeniedException e) {
            gumsResourceAdminLog.info(logUserAccess() + "Failed to add to persistence '" + persistanceManager + "' group '" + group + "' user '" + userDN + "' - " + e.getMessage());
            siteLog.info(logUserAccess() + "Unauthorized access to add to persistence '" + persistanceManager + "' group '" + group + "' user '" + userDN + "'");
            throw e;
        } catch (RuntimeException e) {
            gumsResourceAdminLog.error(logUserAccess() + "Failed to add to persistence '" + persistanceManager + "' group '" + group + "' user '" + userDN + "' - " + e.getMessage());
            siteLog.info(logUserAccess() + "Failed to add to persistence '" + persistanceManager + "' group '" + group + "' user '" + userDN + "' - " + e.getMessage());
            throw e;
        }
    }
    
    public void manualGroupRemove(String persistanceManager, String group, String userDN) {
        try {
            if (hasWriteAccess(currentUser())) {
                PersistenceFactory factory = (PersistenceFactory) gums().getConfiguration().getPersistenceFactories().get(persistanceManager);
                ManualUserGroupDB db = factory.retrieveManualUserGroupDB(group);
                db.removeMember(new GridUser(userDN, null));
                gumsResourceAdminLog.info(logUserAccess() + "Removed from persistence '" + persistanceManager + "' group '" + group + "'  user '" + userDN + "'");
                siteLog.info(logUserAccess() + "Removed from persistence '" + persistanceManager + "' group '" + group + "'  user '" + userDN + "'");
            } else {
                throw new AuthorizationDeniedException();
            }
        } catch (AuthorizationDeniedException e) {
            gumsResourceAdminLog.info(logUserAccess() + "Failed to remove from persistence '" + persistanceManager + "' group '" + group + "' user '" + userDN + "' - " + e.getMessage());
            siteLog.info(logUserAccess() + "Unauthorized access to remove from persistence '" + persistanceManager + "' group '" + group + "' user '" + userDN + "'");
            throw e;
        } catch (RuntimeException e) {
            gumsResourceAdminLog.error(logUserAccess() + "Failed to remove from persistence '" + persistanceManager + "' group '" + group + "' user '" + userDN + "' - " + e.getMessage());
            siteLog.info(logUserAccess() + "Failed to remove from persistence '" + persistanceManager + "' group '" + group + "' user '" + userDN + "' - " + e.getMessage());
            throw e;
        }
    }
    
    public void manualMappingAdd(String persistanceManager, String group, String userDN, String account) {
        try {
            if (hasWriteAccess(currentUser())) {
                PersistenceFactory factory = (PersistenceFactory) gums().getConfiguration().getPersistenceFactories().get(persistanceManager);
                ManualAccountMapperDB db = factory.retrieveManualAccountMapperDB(group);
                db.createMapping(userDN, account);
                gumsResourceAdminLog.info(logUserAccess() + "Added mapping to persistence '" + persistanceManager + "' group '" + group + "' for user '" + userDN + "' to account '" + account + "'");
                siteLog.info(logUserAccess() + "Added mapping to persistence '" + persistanceManager + "' group '" + group + "' for user '" + userDN + "' to account '" + account + "'");
            } else {
                throw new AuthorizationDeniedException();
            }
        } catch (AuthorizationDeniedException e) {
            gumsResourceAdminLog.info(logUserAccess() + "Failed to add mapping to persistence '" + persistanceManager + "' group '" + group + "' for user '" + userDN + "' to account '" + account + "' - " + e.getMessage());
            siteLog.info(logUserAccess() + "Unauthorized access to add mapping to persistence '" + persistanceManager + "' group '" + group + "' for user '" + userDN + "' to account '" + account + "'");
            throw e;
        } catch (RuntimeException e) {
            gumsResourceAdminLog.error(logUserAccess() + "Failed to add mapping to persistence '" + persistanceManager + "' group '" + group + "' for user '" + userDN + "' to account '" + account + "' - " + e.getMessage());
            siteLog.info(logUserAccess() + "Failed to add mapping to persistence '" + persistanceManager + "' group '" + group + "' for user '" + userDN + "' to account '" + account + "' - " + e.getMessage());
            throw e;
        }
    }
    
    public void manualMappingRemove(String persistanceManager, String group, String userDN) {
        try {
            if (hasWriteAccess(currentUser())) {
                PersistenceFactory factory = (PersistenceFactory) gums().getConfiguration().getPersistenceFactories().get(persistanceManager);
                ManualAccountMapperDB db = factory.retrieveManualAccountMapperDB(group);
                db.removeMapping(userDN);
                gumsResourceAdminLog.info(logUserAccess() + "Removed mapping from persistence '" + persistanceManager + "' group '" + group + "' for user '" + userDN + "'");
                siteLog.info(logUserAccess() + "Removed mapping from persistence '" + persistanceManager + "' group '" + group + "' for user '" + userDN + "'");
            } else {
                throw new AuthorizationDeniedException();
            }
        } catch (AuthorizationDeniedException e) {
            gumsResourceAdminLog.info(logUserAccess() + "Failed to remove mapping from persistence '" + persistanceManager + "' group '" + group + "' for user '" + userDN + "' - " + e.getMessage());
            siteLog.info(logUserAccess() + "Unauthorized access to remove mapping from persistence '" + persistanceManager + "' group '" + group + "' for user '" + userDN + "'");
            throw e;
        } catch (RuntimeException e) {
            gumsResourceAdminLog.error(logUserAccess() + "Failed to remove mapping from persistence '" + persistanceManager + "' group '" + group + "' for user '" + userDN + "' - " + e.getMessage());
            siteLog.info(logUserAccess() + "Failed to remove mapping from persistence '" + persistanceManager + "' group '" + group + "' for user '" + userDN + "' - " + e.getMessage());
            throw e;
        }
    }
    
    public String mapUser(String hostname, String userDN, String fqan) {
        try {
            if ( (hasReadSelfAccess(currentUser()) && currentUser().getCertificateDN().equals(userDN)) || hasReadAllAccess(currentUser())) {
                String username = gums().getResourceManager().map(hostname, new GridUser(userDN, fqan));
                gumsResourceAdminLog.info(logUserAccess() + "Mapped on host '" + hostname + "' the user '" + userDN + "' / '" + fqan + "' to '" + username + "'");
                return username;
            } else {
                throw new AuthorizationDeniedException();
            }
        } catch (AuthorizationDeniedException e) {
            gumsResourceAdminLog.info(logUserAccess() + "Failed to map on host '" + hostname + "' the user '" + userDN + "' / '" + fqan + "' - " + e.getMessage());
            siteLog.info(logUserAccess() + "Unauthorized access to map on host '" + hostname + "' the user '" + userDN + "' / '" + fqan + "'");
            throw e;
        } catch (RuntimeException e) {
            gumsResourceAdminLog.error(logUserAccess() + "Failed to map on host '" + hostname + "' the user '" + userDN + "' / '" + fqan + "' - " + e.getMessage());
            throw e;
        }
    }
    
    public String mapAccount(String accountName) {
        try {
            if (hasReadAllAccess(currentUser())) {
                String DNs = gums().getResourceManager().mapAccount(accountName);
                gumsResourceAdminLog.info(logUserAccess() + "Mapped the account '" + accountName + "' to '" + DNs + "'");
                return DNs;
            } else {
                throw new AuthorizationDeniedException();
            }
        } catch (AuthorizationDeniedException e) {
            gumsResourceAdminLog.info(logUserAccess() + "Failed to map the account '" + accountName + "' - " + e.getMessage());
            siteLog.info(logUserAccess() + "Unauthorized access to map the account '" + accountName + "'");
            throw e;
        } catch (RuntimeException e) {
            gumsResourceAdminLog.error(logUserAccess() + "Failed to map the account '" + accountName + "' - "  + e.getMessage());
            throw e;
        }   	
    }
    
    public void mapfileCacheRefresh() {
        try {
            if (hasWriteAccess(currentUser())) {
                throw new RuntimeException("As of GUMS 1.1.0, the mapfile cache is no longer supported. Please use the web service door.");
            } else {
                throw new AuthorizationDeniedException();
            }
        } catch (AuthorizationDeniedException e) {
            gumsResourceAdminLog.info(logUserAccess() + "Failed to refresh the mapfile cache - " + e.getMessage());
            siteLog.info(logUserAccess() + "Unauthorized access to refresh the mapfile cache");
            throw e;
        } catch (RuntimeException e) {
            gumsResourceAdminLog.error(logUserAccess() + "Failed to refresh the mapfile cache - " + e.getMessage());
            throw e;
        }
    }
    
    public void updateGroups() {
        try {
            if (hasWriteAccess(currentUser())) {
                gums().getResourceManager().updateGroups();
                gumsResourceAdminLog.info(logUserAccess() + "Groups updated");
                siteLog.info(logUserAccess() + "Groups updated");
            } else {
                throw new AuthorizationDeniedException();
            }
        } catch (AuthorizationDeniedException e) {
            gumsResourceAdminLog.info(logUserAccess() + "Failed to update all groups - " + e.getMessage());
            siteLog.info(logUserAccess() + "Unauthorized access to update all groups");
            throw e;
        } catch (RuntimeException e) {
            gumsResourceAdminLog.error(logUserAccess() + "Failed to update all groups - " + e.getMessage());
            siteLog.info(logUserAccess() + "Failed to update all groups - " + e.getMessage());
            throw e;
        }
    }

    public Configuration getConfiguration() {
    	return gums().getConfiguration();
    }
    
    public void setConfiguration(Configuration configuration) throws Exception {
    	if (hasWriteAccess(currentUser()))
    		gums().setConfiguration(configuration);
    	else {
            gumsResourceAdminLog.info(logUserAccess() + "Failed to set configuration because user doesn't have write access");
    		siteLog.info(logUserAccess() + "Failed to set configuration because user doesn't have write access");
    		throw new AuthorizationDeniedException();
    	}
    }
    
    String logUserAccess() {
        if (currentUser() == null) {
            return "No AuthN - ";
        } else {
            return currentUser() + " - ";
        }
    }
    
    private boolean isInWeb = false;
    {
        try {
            Class.forName("javax.servlet.Filter");
            isInWeb = true;
        } catch (ClassNotFoundException e) {
            isInWeb = false;
        }
    }
    
    private GridUser currentUser() {
        if (!isInWeb) return null;
        String DN = CertCache.getUserDN();
        if (DN != null) {
            return new GridUser(DN, null);
        } else {
            return null;
        }
    }

    private boolean hasWriteAccess(GridUser user) {
        if (user == null) return false;
        if (gums().getConfiguration().getWriteUserGroups() == null)
            return false;
        Collection writeUserGroups = gums().getConfiguration().getWriteUserGroups();
        Iterator it = writeUserGroups.iterator();
        while (it.hasNext()) {
        	UserGroup userGroupManager = (UserGroup)it.next();
        	if (userGroupManager.isInGroup(user))
        		return true;
        }
        return false;
    }    
    
    private boolean hasReadSelfAccess(GridUser currentUser) {
        if (currentUser == null) return false;
        if (gums().getConfiguration().getReadSelfUserGroups() == null)
            return false;
        Collection readSelfUserGroups = gums().getConfiguration().getReadSelfUserGroups();
        Iterator it = readSelfUserGroups.iterator();
        while (it.hasNext()) {
        	UserGroup userGroupManager = (UserGroup)it.next();
        	if (userGroupManager.isInGroup(currentUser))
        		return true;
        }
        return false;
    }

    private boolean hasReadAllAccess(GridUser user) {
        if (user == null) return false;
        if (gums().getConfiguration().getReadAllUserGroups() == null)
            return false;
        Collection readAllUserGroups = gums().getConfiguration().getReadAllUserGroups();
        Iterator it = readAllUserGroups.iterator();
        while (it.hasNext()) {
        	UserGroup userGroupManager = (UserGroup)it.next();
        	if (userGroupManager.isInGroup(user))
        		return true;
        }
        return false;
    }
    
    public void addAccountRange(String persistenceManager, String groupName, String range) {
        String firstAccount = range.substring(0, range.indexOf('-'));
        String lastAccountN = range.substring(range.indexOf('-') + 1);
        String firstAccountN = firstAccount.substring(firstAccount.length() - lastAccountN.length());
        String accountBase = firstAccount.substring(0, firstAccount.length() - lastAccountN.length());
        int nFirstAccount = Integer.parseInt(firstAccountN);
        int nLastAccount = Integer.parseInt(lastAccountN);

        StringBuffer last = new StringBuffer(firstAccount);
        String nLastAccountString = Integer.toString(nLastAccount);
        last.replace(firstAccount.length() - nLastAccountString.length(), firstAccount.length(), nLastAccountString);
        
        System.out.println("Adding accounts between '" + firstAccount + "' and '" + last.toString() + "' to pool '" + groupName + "'");
        
        StringBuffer buf = new StringBuffer(firstAccount);
        int len = firstAccount.length();
        for (int account = nFirstAccount; account <= nLastAccount; account++) {
            String nAccount = Integer.toString(account);
            buf.replace(len - nAccount.length(), len, nAccount);
            addPoolAccount(persistenceManager, groupName, buf.toString());
            System.out.println(buf.toString() + " added");
        }
    }
    
    public String getVersion() {
    	return GUMS.getVersion();
    }
    
    private void addPoolAccount(String persistanceManager, String group, String username) {
        try {
            if (hasWriteAccess(currentUser())) {
                PersistenceFactory factory = (PersistenceFactory) gums().getConfiguration().getPersistenceFactories().get(persistanceManager);
                if (factory == null) {
                    throw new RuntimeException("PersistenceManager '" + persistanceManager + "' does not exist");
                }
                AccountPoolMapperDB db = factory.retrieveAccountPoolMapperDB(group);
                db.addAccount(username);
                gumsResourceAdminLog.info(logUserAccess() + "Added account to pool: persistence '" + persistanceManager + "' group '" + group + "' username '" + username + "'");
                siteLog.info(logUserAccess() + "Added account to pool: persistence '" + persistanceManager + "' group '" + group + "' username '" + username + "'");
            } else {
                throw new AuthorizationDeniedException();
            }
        } catch (AuthorizationDeniedException e) {
            gumsResourceAdminLog.info(logUserAccess() + "Failed to add account to pool: persistence '" + persistanceManager + "' group '" + group + "' username '" + username + "' - " + e.getMessage());
            siteLog.info(logUserAccess() + "Unauthorized access to add account to pool: persistence '" + persistanceManager + "' group '" + group + "' username '" + username + "'");
            throw e;
        } catch (RuntimeException e) {
            gumsResourceAdminLog.error(logUserAccess() + "Failed to add account to pool: persistence '" + persistanceManager + "' group '" + group + "' username '" + username + "' - " + e.getMessage());
            siteLog.info(logUserAccess() + "Failed to add account to pool: persistence '" + persistanceManager + "' group '" + group + "' username '" + username + "' - " + e.getMessage());
            throw e;
        }
    }
    
}
