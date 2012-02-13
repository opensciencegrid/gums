/*
 * GridMapfileGeneratorTest.java
 * JUnit based test
 *
 * Created on March 24, 2004, 2:44 PM
 */

package gov.bnl.gums.configuration;

import gov.bnl.gums.userGroup.*;
import gov.bnl.gums.account.*;
import gov.bnl.gums.hostToGroup.*;
import gov.bnl.gums.groupToAccount.GroupToAccountMapping;
import gov.bnl.gums.persistence.*;
import gov.bnl.gums.*;

import java.util.*;

import junit.framework.*;

/**
 *
 * @author carcassi
 */
public class ConfigurationTest extends TestCase {
    
    public ConfigurationTest(java.lang.String testName) {
        super(testName);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(ConfigurationTest.class);
        return suite;
    }
    
    public void testSimpleConfiguration() {
        Configuration conf = createSimpleConfiguration();
        HostToGroupMapping host2GroupMapping = (HostToGroupMapping) conf.getHostToGroupMappings().get(0);
        assertNotNull(host2GroupMapping);
        List groupToAccountMappings = host2GroupMapping.getGroupToAccountMappings();
        assertNotNull(groupToAccountMappings);
        assertEquals(1, groupToAccountMappings.size());
        GroupToAccountMapping gMap = conf.getGroupToAccountMapping( (String)groupToAccountMappings.get(0) );
        assertTrue((conf.getUserGroup( (String)gMap.getUserGroups().get(0)) ).isInGroup(new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", null)));
        assertEquals("jsmith", conf.getAccountMapper( (String)gMap.getAccountMappers().get(0) ).mapUser("/DC=org/DC=griddev/OU=People/CN=John Smith", true));
    }
    
    public static Configuration createSimpleConfiguration() {
        Configuration conf = new Configuration();

        GroupToAccountMapping gMap = new GroupToAccountMapping(conf, "mockGroup");
        conf.addGroupToAccountMapping(gMap);
        gMap.setAccountingVoSubgroup("mock");
        gMap.setAccountingVo("mock");
        MockUserGroup userGroup = new MockUserGroup(conf, "mockUserGroup", true);
        userGroup.addMember(new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", null));
        conf.addUserGroup(userGroup);
        gMap.addUserGroup(userGroup.getName());
        AccountMapper accountMapper = new MockAccountMapper(conf, "mockAccountMapper");
        conf.addAccountMapper(accountMapper);
        gMap.addAccountMapper(accountMapper.getName());

        MockHostToGroupMapping hMap = new MockHostToGroupMapping(conf);
        conf.addHostToGroupMapping(hMap);
        hMap.addGroupToAccountMapping(gMap.getName());
        
        conf.addPersistenceFactory( new MockPersistenceFactory(conf, "mockPers") );
        
        return conf;
    }
    
    public void testSingleUserGroupCopy() {
        Configuration conf = new Configuration();
        HibernatePersistenceFactory factory = new HibernatePersistenceFactory(conf, "mysql");
        conf.addPersistenceFactory(factory);
        GroupToAccountMapping gMap = new GroupToAccountMapping(conf, "group1");
        conf.addGroupToAccountMapping(gMap);
        LDAPUserGroup userGroup = new LDAPUserGroup(conf, "userGroup1");
        conf.addUserGroup(userGroup);
        userGroup.setPersistenceFactory(factory.getName());
		userGroup.setQuery("ou=People,query");
		userGroup.setServer("server");
		gMap.addUserGroup(userGroup.getName());
        MockAccountMapper accountMapper = new MockAccountMapper(conf, "mockAccountMapper");
        conf.addAccountMapper(accountMapper);
        gMap.addAccountMapper(accountMapper.getName());
        gMap = new GroupToAccountMapping(conf, "group2");
        conf.addGroupToAccountMapping(gMap);
        userGroup = new LDAPUserGroup(conf, "userGroup2");
        conf.addUserGroup(userGroup);
        userGroup.setPersistenceFactory(factory.getName());
        userGroup.setQuery("ou=People,query");
        userGroup.setServer("server");
        gMap.addUserGroup(userGroup.getName());
        accountMapper = new MockAccountMapper(conf, "mockAccountMapper2");
        conf.addAccountMapper(accountMapper);
        gMap.addAccountMapper(accountMapper.getName());
        assertEquals(2, conf.getGroupToAccountMappings().values().size());
        assertEquals(1, gMap.getUserGroups().size());
    }
    
    public void testMergeConfiguration() {
    	Configuration conf = new Configuration();
        GroupToAccountMapping gMap = new GroupToAccountMapping(conf, "mockGroup");
        conf.addGroupToAccountMapping(gMap);
        gMap.setAccountingVoSubgroup("mock");
        gMap.setAccountingVo("mock");
        MockUserGroup userGroup = new MockUserGroup(conf, "mockUserGroup", true);
        userGroup.addMember(new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", null));
        conf.addUserGroup(userGroup);
        gMap.addUserGroup(userGroup.getName());
        AccountMapper accountMapper = new MockAccountMapper(conf, "mockAccountMapper");
        conf.addAccountMapper(accountMapper);
        gMap.addAccountMapper(accountMapper.getName());
        MockHostToGroupMapping hMap = new MockHostToGroupMapping(conf);
        conf.addHostToGroupMapping(hMap);
        hMap.addGroupToAccountMapping(gMap.getName());
        conf.addPersistenceFactory( new MockPersistenceFactory(conf, "mockPers") );
        
    	Configuration newConf = new Configuration();
        gMap = new GroupToAccountMapping(newConf, "mockGroup");
        newConf.addGroupToAccountMapping(gMap);
        gMap.setAccountingVoSubgroup("mock2");
        gMap.setAccountingVo("mock2");
        GroupToAccountMapping gMap2 = new GroupToAccountMapping(newConf, "mockGroup2");
        conf.addGroupToAccountMapping(gMap2);
        hMap.addGroupToAccountMapping(gMap2.getName());
        userGroup = new MockUserGroup(newConf, "mockUserGroup", true);
        userGroup.addMember(new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", null));
        MockUserGroup userGroup2 = new MockUserGroup(newConf, "mockUserGroup2", true);
        userGroup2.addMember(new GridUser("/DC=org/DC=griddev/OU=People/CN=Jane Doe", null));
        newConf.addUserGroup(userGroup);
        newConf.addUserGroup(userGroup2);
        gMap.addUserGroup(userGroup.getName());
        gMap.addUserGroup(userGroup2.getName());
        accountMapper = new MockAccountMapper(newConf, "mockAccountMapper2");
        newConf.addAccountMapper(accountMapper);
        gMap.addAccountMapper(accountMapper.getName());
        hMap = new MockHostToGroupMapping(newConf);
        newConf.addHostToGroupMapping(hMap);
        hMap.addGroupToAccountMapping(gMap.getName());
        newConf.addPersistenceFactory( new MockPersistenceFactory(newConf, "mockPers") );
        
        conf.mergeConfiguration(newConf, "mockPers", "known.site.com");

        assertEquals(conf.getGroupToAccountMapping("mockGroup").getAccountingVo(), "mock2");
        assertEquals(conf.getGroupToAccountMapping("mockGroup").getAccountingVoSubgroup(), "mock2");
        assertEquals(conf.getGroupToAccountMapping("mockGroup").getUserGroups().size(), 2);
        assertNotNull(conf.getGroupToAccountMapping("mockGroup2"));
        assertNotNull(conf.getUserGroup("mockUserGroup"));
        assertNotNull(conf.getUserGroup("mockUserGroup2"));
        assertNotNull(conf.getAccountMapper("mockAccountMapper"));
        assertNotNull(conf.getAccountMapper("mockAccountMapper2"));
    }    
}
