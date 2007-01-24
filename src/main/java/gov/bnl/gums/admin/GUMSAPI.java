/*
 * GUMSAPI.java
 *
 * Created on October 28, 2004, 9:36 AM
 */

package gov.bnl.gums.admin;

import java.rmi.Remote;

import gov.bnl.gums.configuration.Configuration;

/** Interface to the GUMS admin functionality. All accesses to GUMS will go through
 * an implementation of this class. This also serves as a definition to the admin
 * service.
 *
 * @author  Gabriele Carcassi
 */
public interface GUMSAPI extends Remote {
    /**
     * Generate the inverse entry->VO map used by OSG.
     * @param hostname Host name to generate the map for.
     * @return The map, or null if no map is found
     */
    String generateGrid3UserVoMap(String hostname);
    String generateGridMapfile(String hostname);
    void manualGroupAdd(String persistanceManager, String group, String userDN);
    void manualGroupRemove(String persistanceManager, String group, String userDN);
    void manualMappingAdd(String persistanceManager, String group, String userDN, String account);
    void manualMappingRemove(String persistanceManager, String group, String userDN);
    void addAccountRange(String persistanceManager, String group, String range);
    String mapUser(String hostname, String userDN, String fqan);
    String mapAccount(String accountName);
    void mapfileCacheRefresh();
    void updateGroups();
    Configuration getConfiguration();
    void setConfiguration(Configuration configuration) throws Exception;
    String getVersion();
    void backupConfiguration();
}
