/*
 * GUMSAPIMock.java
 *
 * Created on February 4, 2005, 4:26 PM
 */

package gov.bnl.gums.admin;

import java.util.Collection;

import gov.bnl.gums.GUMS;
import gov.bnl.gums.configuration.Configuration;

/**
 *
 * @author Gabriele Carcassi, Jay Packard
 */
public class GUMSAPIMock implements GUMSAPI {
    public void addAccountRange(String persistanceManager, String group, String range) {
    }

    public void backupConfiguration() {
    }

    public void deleteBackupConfiguration(String dateStr) {
    }
    
    public String generateGrid3UserVoMap(String hostname) {
        return "inverseMap";
    }

    public String generateGridMapfile(String hostname) {
        return "mapfile";
    }
    
    public String generateVoGridMapfile(String hostname) {
        return "mapfile";
    }

    public Collection getBackupConfigDates() {
    	return null;
    }
    
    public Configuration getConfiguration() {
    	return null;
    }

    public String getVersion() {
    	return GUMS.getVersion();
    }

    public void manualGroupAdd(String persistanceManager, String group, String userDN) {
    }
    
    public void manualGroupRemove(String persistanceManager, String group, String userDN) {
    }

    public void manualMappingAdd(String persistanceManager, String group, String userDN, String account) {
    }

    public void manualMappingRemove(String persistanceManager, String group, String userDN) {
    }

    public String mapAccount(String accountName) {
        return "userDN";
    }
    
    public void mapfileCacheRefresh() {
    }
    
    public String mapUser(String hostname, String userDN, String fqan) {
        return "account";
    }
    
    public void restoreConfiguration(String dateStr) {
    }
    
    public void setConfiguration(Configuration configuration) throws Exception {
    }
    
    public void updateGroups() {
    }
    
}
