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
    void addAccountRange2(String accountPoolMapperName, String range);
    
    /**
     * Backup current configuration with a name
     * 
     * @param name as String
     */
    void backupConfiguration(String name);
    
    /**
     * Delete a backup configuration by date
     * 
     * @param dateStr
     */
    void deleteBackupConfiguration(String name);
    
    /**
     * Generate email gridmap file to be able to contact user if needed
     * 
     * @param hostname
     * @return
     */
	String generateEmailMapfile(String hostname);
    
    /**
     * Generate fqanmap file used to validate mappings
     * 
     * @param hostname
     * @return
     */
    String generateFqanMapfile(String hostname);
    
    /**
     * Generate the inverse entry->VO map used by OSG.
     * 
     * @param hostname Host name to generate the map for.
     * @return The map, or null if no map is found
     * @deprecated
     */
    String generateGrid3UserVoMap(String hostname);
    
    /**
     * Generate gridmap file used by gateway in case GUMS is down 
     * and by certain applications like dCache
     * 
     * @param hostname
     * @return
     */
    String generateGridMapfile(String hostname);
    
    /**
     * Generate the inverse entry->VO map used by OSG.
     * 
     * @param hostname Host name to generate the map for.
     * @return The map, or null if no map is found
     */
    String generateOsgUserVoMap(String hostname);
    
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
    Collection getBackupNames();
    
    /**
     * Get current configuration
     * 
     * @return
     */
    Configuration getConfiguration();

    /**
     * Get current pool account assignments
     * 
     * @param accountMapper
     * @return printout of assignments
     */
    String getPoolAccountAssignments(String accountMapper);
    
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
    void manualGroupAdd2(String userGroupName, String userDN);
    
    /**
     * Add a userDN to a manual user group
     * 
     * @param persistanceFactory
     * @param group name of manual user group
     * @param userDN
     * @param fqan
     * @param email
     */
    public void manualGroupAdd3(String manualUserGroupName, String userDN, String fqan, String email);
    
    /**
     * Remove a userDN from a manual user group
     * 
     * @param persistanceFactory
     * @param group name of manual user group
     * @param userDN
     */
    void manualGroupRemove2(String manualUserGroupName, String userDN);
    
    /**
     * Remove a userDN from a manual user group
     * 
     * @param persistanceFactory
     * @param group name of manual user group
     * @param userDN
     * @param fqan
     */
    void manualGroupRemove3(String manualUserGroupName, String userDN, String fqan);
    
    /**
     * Add a userDN to account mapping
     * 
     * @param persistanceFactory 
     * @param accountMapperName name of manual account mapper
     * @param userDN
     * @param account
     */
    void manualMappingAdd2(String manualAccountMapperName, String userDN, String account);
    
    /**
     * Removes a userDN from an account mapping
     * 
     * @param persistanceFactory
     * @param accountMapperName name of manual account mapper
     * @param userDN
     */
    void manualMappingRemove2(String manualAccountMapperName, String userDN);
    
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
     * Merge configuration into current configuration
     * 
     * @param uri
     * @param persistenceFactory
     * @param hostToGroupMapping
     * @return
     */
    void mergeConfiguration(Configuration conf, String newConfUri, String persistenceFactory, String hostToGroupMapping);
    
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
    void unassignAccountRange(String accountPoolMapperName, String range);
    
    /**
     * Update members in user groups by querying VO servers - may be time consuming
     */
    void updateGroups();

    /**
     * Get currently logged in user DN
     */
    String getCurrentDn();
    
    // Depricated
    
    void manualGroupAdd(String persistanceFactory, String group, String userDN);
    
    void manualGroupRemove(String persistanceFactory, String group, String userDN);
    
    void manualMappingAdd(String persistanceFactory, String group, String userDN, String account);
    
    void manualMappingRemove(String persistanceFactory, String group, String userDN);
    
    void poolAddAccount(String persistanceFactory, String group, String username);
}
