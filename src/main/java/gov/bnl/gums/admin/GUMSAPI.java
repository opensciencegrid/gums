/*
 * GUMSAPI.java
 *
 * Created on October 28, 2004, 9:36 AM
 */

package gov.bnl.gums.admin;

import java.rmi.Remote;
import java.util.Collection;

import gov.bnl.gums.configuration.Configuration;

/** Interface to the GUMS admin functionality. All accesses to GUMS will go through
 * an implementation of this class. This also serves as a definition to the admin
 * service.
 *
 * @author Gabriele Carcassi, Jay Packard
 */
public interface GUMSAPI extends Remote {
    void addAccountRange(String persistanceManager, String group, String range);
    
    void backupConfiguration();
    
    void deleteBackupConfiguration(String dateStr);
    
    /**
     * Generate the inverse entry->VO map used by OSG.
     * @param hostname Host name to generate the map for.
     * @return The map, or null if no map is found
     */
    String generateGrid3UserVoMap(String hostname);
    
    String generateGridMapfile(String hostname);
    
    String generateVoGridMapfile(String hostname);
    
    Configuration getConfiguration();
    
    Collection getBackupConfigDates();
    
    String getVersion();
    
    void manualGroupAdd(String persistanceManager, String group, String userDN);
    
    void manualGroupRemove(String persistanceManager, String group, String userDN);
    
    void manualMappingAdd(String persistanceManager, String group, String userDN, String account);
    
    void manualMappingRemove(String persistanceManager, String group, String userDN);
    
    String mapAccount(String accountName);
    
    void mapfileCacheRefresh();
    
    String mapUser(String hostname, String userDN, String fqan);
    
    void setConfiguration(Configuration configuration) throws Exception;
    
    void restoreConfiguration(String dateStr) throws Exception;
    
    void updateGroups();
}
