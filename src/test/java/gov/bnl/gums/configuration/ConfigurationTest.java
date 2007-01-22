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
        assertTrue((conf.getUserGroup( (String)gMap.getUserGroups().get(0)) ).isInGroup(new GridUser("/DC=org/DC=doegrids/OU=People/CN=Gabriele Carcassi", null)));
        assertEquals("carcassi", conf.getAccountMapper( (String)gMap.getAccountMappers().get(0) ).mapUser("/DC=org/DC=doegrids/OU=People/CN=Gabriele Carcassi"));
    }
    
    static Configuration createSimpleConfiguration() {
        Configuration conf = new Configuration();

        GroupToAccountMapping gMap = new GroupToAccountMapping(conf, "mockGroup");
        gMap.setAccountingVo("mock");
        gMap.setAccountingDesc("mock");
        UserGroup userGroup = new MockUserGroup(conf, "mockUserGroup");
        gMap.addUserGroup(userGroup.getName());
        AccountMapper accountMapper = new MockAccountMapper(conf, "mockAccountMapper");
        gMap.addAccountMapper(accountMapper.getName());

        MockHostToGroupMapping hMap = new MockHostToGroupMapping(conf);
        hMap.addGroupToAccountMapping(gMap.getName());
        
        new MockPersistenceFactory(conf, "mockPers");
        
        return conf;
    }
    
    public void testSingleUserGroupCopy() {
        Configuration conf = new Configuration();
        MySQLPersistenceFactory factory = new MySQLPersistenceFactory(conf, "mysql");
        GroupToAccountMapping gMap = new GroupToAccountMapping(conf, "group1");
        LDAPUserGroup userGroup = new LDAPUserGroup(conf, "userGroup1");
        userGroup.setPersistenceFactory(factory.getName());
        userGroup.setQuery("query");
        userGroup.setServer("server");
        gMap.addUserGroup(userGroup.getName());
        MockAccountMapper accountMapper = new MockAccountMapper(conf, "mockAccountMapper");
        gMap.addAccountMapper(accountMapper.getName());
        gMap = new GroupToAccountMapping(conf, "group2");
        userGroup = new LDAPUserGroup(conf, "userGroup2");
        userGroup.setPersistenceFactory(factory.getName());
        userGroup.setQuery("query");
        userGroup.setServer("server");
        gMap.addUserGroup(userGroup.getName());
        accountMapper = new MockAccountMapper(conf, "mockAccountMapper");
        gMap.addAccountMapper(accountMapper.getName());
        assertEquals(2, conf.getGroupToAccountMappings().values().size());
        assertEquals(1, gMap.getUserGroups().size());
    }
    
}
