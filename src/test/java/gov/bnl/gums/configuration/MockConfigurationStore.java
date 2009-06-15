/*
 * MockConfigurationStore.java
 *
 * Created on January 21, 2005, 3:47 PM
 */

package gov.bnl.gums.configuration;

import gov.bnl.gums.GridUser;
import gov.bnl.gums.account.AccountMapper;
import gov.bnl.gums.account.MockAccountMapper;
import gov.bnl.gums.groupToAccount.GroupToAccountMapping;
import gov.bnl.gums.hostToGroup.MockHostToGroupMapping;
import gov.bnl.gums.persistence.MockPersistenceFactory;
import gov.bnl.gums.userGroup.MockUserGroup;
import gov.bnl.gums.userGroup.UserGroup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Date;

/**
 *
 * @author carcassi
 */
public class MockConfigurationStore extends ConfigurationStore {
    Configuration conf;
    
    /** Creates a new instance of MockConfigurationStore */
    public MockConfigurationStore() {
        conf = new Configuration();
        MockHostToGroupMapping hMap = new MockHostToGroupMapping(conf);
        List groupMappers = new ArrayList();
        GroupToAccountMapping gMap = new GroupToAccountMapping(conf, "mockGroup");
        MockUserGroup userGroup = new MockUserGroup(conf, "mockUserGroup", true);
        userGroup.addMember(new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", null));
        conf.addUserGroup(userGroup);
        gMap.addUserGroup(userGroup.getName());
        AccountMapper accountMapper = new MockAccountMapper(conf, "mockAccountMapper");
        conf.addAccountMapper(accountMapper);
        gMap.addAccountMapper(accountMapper.getName());
        groupMappers.add(gMap);
        hMap.setGroupMappers(groupMappers);
        try {
			conf.addPersistenceFactory( new MockPersistenceFactory(conf, "mockPers") );
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    public void deleteBackupConfiguration(String dateStr) {
    }
    
    public Collection getBackupNames() {
    	return null;
    }
    
    public Date getLastModification() {
    	return new Date();
    }
    
    public boolean needsReload() {
    	return true;
    }
    
    public void storeConfiguration(Configuration conf) {
        this.conf = conf;
    }

    public Configuration retrieveConfiguration() {
        return conf;
    }
    
    public Configuration retrieveConfiguration(boolean reload) {
        return conf;
    }

    public Configuration restoreConfiguration(String strDate) {
        return null;
    }
    
    public void setConfiguration(Configuration conf, boolean backup, String name, Date date) {
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
