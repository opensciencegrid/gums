/*
 * MockPersistenceFactory.java
 *
 * Created on June 10, 2004, 2:59 PM
 */

package gov.bnl.gums.persistence;

import java.util.Properties;

import gov.bnl.gums.db.*;

/**
 *
 * @author  carcassi
 */
public class MockPersistenceFactory extends PersistenceFactory {
	
	public MockPersistenceFactory(String name) {
		super(name);
	}
	
    public String getName() {
        return "mock";
    }    
    
    public ManualAccountMapperDB retrieveManualAccountMapperDB(String name) {
        return new MockManualAccountMapperDB();
    }
    
    public ManualUserGroupDB retrieveManualUserGroupDB(String name) {
        return new MockManualUserGroupDB();
    }
    
    public UserGroupDB retrieveUserGroupDB(String name) {
        return new MockUserGroupDB();
    }
    
    public AccountPoolMapperDB retrieveAccountPoolMapperDB(String name) {
        return new MockAccountPoolMapperDB();
    }
    
    public void setProperties(Properties properties) {
    	
    }
    
}
