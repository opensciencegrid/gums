/*
 * GUMSAPIMock.java
 *
 * Created on February 4, 2005, 4:26 PM
 */

package gov.bnl.gums.admin;

import gov.bnl.gums.configuration.Configuration;

/**
 *
 * @author carcassi
 */
public class GUMSAPIMock implements GUMSAPI {
    public String generateGridMapfile(String hostname) {
        return "mapfile";
    }

    public String generateGrid3UserVoMap(String hostname) {
        return "inverseMap";
    }

    public void updateGroups() {
    }

    public void addAccountRange(String persistanceManager, String group, String range) {
    }

    public void mapfileCacheRefresh() {
    }

    public String mapUser(String hostname, String userDN, String fqan) {
        return "account";
    }

    public String mapAccount(String accountName) {
        return "userDN";
    }
    
    public void manualMappingRemove(String persistanceManager, String group, String userDN) {
    }

    public void manualMappingAdd(String persistanceManager, String group, String userDN, String account) {
    }

    public void manualGroupRemove(String persistanceManager, String group, String userDN) {
    }

    public void manualGroupAdd(String persistanceManager, String group, String userDN) {
    }
    
    public Configuration getConfiguration() {
    	return null;
    }
    
    public void setConfiguration(Configuration configuration) throws Exception {
    }
    
}
