/*
 * MockPersistenceFactory.java
 *
 * Created on June 10, 2004, 2:59 PM
 */

package gov.bnl.gums.persistence;

import java.util.Properties;

import gov.bnl.gums.configuration.Configuration;
import gov.bnl.gums.db.*;

/**
 *
 * @author  carcassi
 */
public class MockPersistenceFactory extends PersistenceFactory {
	MockManualUserGroupDB manualUserGroupDB = new MockManualUserGroupDB();
	MockAccountPoolMapperDB accountPoolMapperDB = new MockAccountPoolMapperDB();
	MockUserGroupDB userGroupDB = new MockUserGroupDB();
	MockManualAccountMapperDB manualAccountMapperDB = new MockManualAccountMapperDB();
	MockConfigurationDB configurationDB = new MockConfigurationDB();
	
	public MockPersistenceFactory(Configuration configuration, String name) {
		super(configuration, name);
	}
    
    public ManualAccountMapperDB retrieveManualAccountMapperDB(String name) {
        return manualAccountMapperDB;
    }
    
    public void closeSessions(){}
    
    public ConfigurationDB retrieveConfigurationDB() {
        return configurationDB;
    }
    
    public ManualUserGroupDB retrieveManualUserGroupDB(String name) {
        return manualUserGroupDB;
    }
    
    public UserGroupDB retrieveUserGroupDB(String name) {
        return userGroupDB;
    }
    
    public AccountPoolMapperDB retrieveAccountPoolMapperDB(String name) {
        return accountPoolMapperDB;
    }
    
    public void setProperties(Properties properties) {
    	
    }
    
    public PersistenceFactory clone(Configuration configuration) {
    	return null;
    }
    
    public String toXML(){
    	return "";
    }
    
}
