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
        GroupToAccountMapping gMap = (GroupToAccountMapping) groupToAccountMappings.get(0);
        assertTrue(((UserGroup)gMap.getUserGroups().get(0)).isInGroup(new GridUser("/DC=org/DC=doegrids/OU=People/CN=Gabriele Carcassi", null)));
        assertEquals("carcassi", ((AccountMapper)gMap.getAccountMappers().get(0)).mapUser("/DC=org/DC=doegrids/OU=People/CN=Gabriele Carcassi"));
    }
    
    static Configuration createSimpleConfiguration() {
        Configuration conf = new Configuration();

        GroupToAccountMapping gMap = new GroupToAccountMapping("mockGroup");
        gMap.setAccountingVo("mock");
        gMap.setAccountingDesc("mock");
        gMap.addUserGroup(new MockUserGroup());
        gMap.addAccountMapper(new MockAccountMapper());

        MockHostToGroupMapping hMap = new MockHostToGroupMapping();
        hMap.addGroupToAccountMapping(gMap);
        
        conf.addPersistenceFactory(new MockPersistenceFactory("mockPers"));
        conf.addGroupToAccountMapping(gMap);
        conf.addHostToGroupMapping(hMap);
        
        return conf;
    }
    
    public void testSingleUserGroupCopy() {
        Configuration conf = new Configuration();
        MySQLPersistenceFactory factory = new MySQLPersistenceFactory("mysql");
        conf.addPersistenceFactory(factory);
        GroupToAccountMapping gMap = new GroupToAccountMapping("group1");
        LDAPUserGroup userGroup = new LDAPUserGroup("userGroup1");
        userGroup.setPersistenceFactory(factory);
        userGroup.setQuery("query");
        userGroup.setServer("server");
        gMap.addUserGroup(userGroup);
        gMap.addAccountMapper(new MockAccountMapper());
        conf.addGroupToAccountMapping(gMap);
        gMap = new GroupToAccountMapping("group2");
        userGroup = new LDAPUserGroup("userGroup2");
        userGroup.setPersistenceFactory(factory);
        userGroup.setQuery("query");
        userGroup.setServer("server");
        gMap.addUserGroup(userGroup);
        gMap.addAccountMapper(new MockAccountMapper());
        conf.addGroupToAccountMapping(gMap);
        assertEquals(2, conf.getGroupToAccountMappings().values().size());
        assertEquals(1, gMap.getUserGroups().size());
    }
    
}
