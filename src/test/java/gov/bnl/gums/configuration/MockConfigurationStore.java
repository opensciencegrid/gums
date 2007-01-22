/*
 * MockConfigurationStore.java
 *
 * Created on January 21, 2005, 3:47 PM
 */

package gov.bnl.gums.configuration;

import gov.bnl.gums.account.AccountMapper;
import gov.bnl.gums.account.MockAccountMapper;
import gov.bnl.gums.groupToAccount.GroupToAccountMapping;
import gov.bnl.gums.hostToGroup.MockHostToGroupMapping;
import gov.bnl.gums.persistence.MockPersistenceFactory;
import gov.bnl.gums.userGroup.MockUserGroup;
import gov.bnl.gums.userGroup.UserGroup;

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
        MockHostToGroupMapping hMap = new MockHostToGroupMapping(conf);
        List groupMappers = new ArrayList();
        GroupToAccountMapping gMap = new GroupToAccountMapping(conf, "mockGroup");
        UserGroup userGroup = new MockUserGroup(conf, "mockUserGroup");
        gMap.addUserGroup(userGroup.getName());
        AccountMapper accountMapper = new MockAccountMapper(conf, "mockAccountMapper");
        gMap.addAccountMapper(accountMapper.getName());
        groupMappers.add(gMap);
        hMap.setGroupMappers(groupMappers);
        try {
			new MockPersistenceFactory(conf, "mockPers");
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
