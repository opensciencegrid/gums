/*
 * GUMSAPI.java
 *
 * Created on October 28, 2004, 9:36 AM
 */

package gov.bnl.gums.admin;

import java.rmi.Remote;
import java.util.Collection;

import gov.bnl.gums.configuration.Configuration;

/** 
 * Interface to the GUMS admin functionality. All accesses to GUMS will go through
 * an implementation of this class. This also serves as a definition to the admin
 * service.
 *
 * @author Gabriele Carcassi, Jay Packard
 */
public interface GUMSAPI extends Remote {
    /**
     * Add a range of pool accounts
     * 
     * @param persistanceManager
     * @param poolAccountMapperName
     * @param range
     */
    void addAccountRange(String accountPoolMapperName, String range);
    
    /**
     * Removes a userDN from an account mapping
     * 
     * @param persistanceFactory
     * @param accountMapperName name of manual account mapper
     * @param userDN
     */
    void anualMappingRemove(String manualAccountMapperName, String userDN);
    
    /**
     * Backup current configuration
     */
    void backupConfiguration();
    
    /**
     * Delete a backup configuration by date
     * 
     * @param dateStr
     */
    void deleteBackupConfiguration(String dateStr);
    
    /**
     * Generate the inverse entry->VO map used by OSG.
     * 
     * @param hostname Host name to generate the map for.
     * @return The map, or null if no map is found
     */
    String generateGrid3UserVoMap(String hostname);
    
    /**
     * Generate gridmap files used by gateway in case GUMS is down 
     * and by certain applications like dCache
     * 
     * @param hostname
     * @return
     */
    String generateGridMapfile(String hostname);
    
    /**
     * Genereate Vo grid map file used for accounting purposes
     * 
     * @param hostname
     * @return
     */
    String generateVoGridMapfile(String hostname);
    
    /**
     * Get a list of dates for which there is a backup
     * 
     * @return
     */
    Collection getBackupConfigDates();
    
    /**
     * Get current configuration
     * 
     * @return
     */
    Configuration getConfiguration();
    
    /**
     * Get current version of GUMS
     * 
     * @return
     */
    String getVersion();
    
    /**
     * Add a userDN to a manual user group
     * 
     * @param persistanceFactory
     * @param group name of manual user group
     * @param userDN
     */
    void manualGroupAdd(String userGroupName, String userDN);
    
    /**
     * Remove a userDN from a manual user group
     * 
     * @param persistanceFactory
     * @param group name of manual user group
     * @param userDN
     */
    void manualGroupRemove(String manualUserGroupName, String userDN);
    
    /**
     * Add a userDN to account mapping
     * 
     * @param persistanceFactory 
     * @param accountMapperName name of manual account mapper
     * @param userDN
     * @param account
     */
    void manualMappingAdd(String manualAccountMapperName, String userDN, String account);
    
    /**
     * Map an account to a list of grid DNs
     * 
     * @param accountName
     * @return
     */
    String mapAccount(String accountName);
    
    /**
     * Map a grid DN to an account
     * 
     * @param hostname
     * @param userDN
     * @param fqan
     * @return
     */
    String mapUser(String hostname, String userDN, String fqan);
    
    /**
     * 
     * Remove a range of pool accounts
     * 
     * @param persistanceManager
     * @param poolAccountMapperName
     * @param range
     */
    void removeAccountRange(String accountPoolMapperName, String range);
    
    /**
     * Restore a configuration by date
     * 
     * @param dateStr
     * @throws Exception
     */
    void restoreConfiguration(String dateStr) throws Exception;
    
    /**
     * Set the configuration
     * 
     * @param configuration
     * @throws Exception
     */
    void setConfiguration(Configuration configuration) throws Exception;

    /**
     * Unassign all accounts in a pool
     * 
     * @param persistanceManager
     * @param accountMapperName
     */
    void unassignAllPoolAccounts(String accountPoolMapperName);
    
    /**
     * Update members in user groups by querying VO servers - may be time consuming
     */
    void updateGroups();
}
