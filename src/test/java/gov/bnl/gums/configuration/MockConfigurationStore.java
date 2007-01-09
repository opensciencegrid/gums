/*
 * MockConfigurationStore.java
 *
 * Created on January 21, 2005, 3:47 PM
 */

package gov.bnl.gums.configuration;

import gov.bnl.gums.account.MockAccountMapper;
import gov.bnl.gums.groupToAccount.GroupToAccountMapping;
import gov.bnl.gums.hostToGroup.MockHostToGroupMapping;
import gov.bnl.gums.persistence.MockPersistenceFactory;
import gov.bnl.gums.userGroup.MockUserGroup;

import java.util.ArrayList;
import java.util.List;


/**
 *
 * @author carcassi
 */
public class MockConfigurationStore implements ConfigurationStore {
    Configuration conf;
    
    /** Creates a new instance of MockConfigurationStore */
    public MockConfigurationStore() {
        conf = new Configuration();
        MockHostToGroupMapping hMap = new MockHostToGroupMapping();
        List groupMappers = new ArrayList();
        GroupToAccountMapping gMap = new GroupToAccountMapping("mockGroup");
        gMap.addUserGroup(new MockUserGroup());
        gMap.addAccountMapper(new MockAccountMapper());
        groupMappers.add(gMap);
        hMap.setGroupMappers(groupMappers);
        try {
			conf.addPersistenceFactory(new MockPersistenceFactory("mockPers"));
			conf.addGroupToAccountMapping(gMap);
			conf.addHostToGroupMapping(hMap);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

    public void storeConfiguration(Configuration conf) {
        this.conf = conf;
    }

    public Configuration retrieveConfiguration() {
        return conf;
    }

    public void setConfiguration(Configuration conf) {
        this.conf = conf;
    }
    
    public void storeConfiguration() {
    }
    
    public boolean isReadOnly() {
        return false;
    }

    public boolean isActive() {
        return true;
    }
    
}
