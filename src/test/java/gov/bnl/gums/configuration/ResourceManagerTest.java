/*
 * GridMapfileGeneratorTest.java
 * JUnit based test
 *
 * Created on March 24, 2004, 2:44 PM
 */

package gov.bnl.gums.configuration;

import gov.bnl.gums.account.AccountMapper;
import gov.bnl.gums.account.GroupAccountMapper;
import gov.bnl.gums.account.MockAccountMapper;
import gov.bnl.gums.hostToGroup.HostToGroupMapping;
import gov.bnl.gums.hostToGroup.MockHostToGroupMapping;
import gov.bnl.gums.groupToAccount.GroupToAccountMapping;
import gov.bnl.gums.persistence.MockPersistenceFactory;
import gov.bnl.gums.persistence.PersistenceFactory;
import gov.bnl.gums.userGroup.ManualUserGroup;
import gov.bnl.gums.userGroup.MockUserGroup;
import gov.bnl.gums.userGroup.UserGroup;
import gov.bnl.gums.*;

import java.util.*;


import junit.framework.*;

/**
 *
 * @author carcassi
 */
public class ResourceManagerTest extends TestCase {
    ResourceManager man;
    Configuration conf;
    
    public ResourceManagerTest(java.lang.String testName) {
        super(testName);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(ResourceManagerTest.class);
        return suite;
    }
    
    public void setUp() {
        GUMS gums = new GUMS(ConfigurationTest.createSimpleConfiguration());
        man = gums.getResourceManager();
        conf = gums.getConfiguration();
    }
    
    public void testMap() {
        assertEquals("jsmith", man.map("known.site.com", new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", null)));
        assertNull(man.map("known.site.com", new GridUser("/DC=org/DC=griddev/OU=People/CN=Evil Person", null)));
        assertNull(man.map("unknown.site.com", new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", null)));
    }
    
    public void testGenerateGrid3UserVoMap() {
        String map = man.generateGrid3UserVoMap("known.site.com");
        String expectedMap = "#User-VO map\n" +
                "# #comment line, format of each regular line line: account VO\n" +
                "# Next 2 lines with VO names, same order, all lowercase, with case (lines starting with #voi, #VOc)\n" +
                "#voi mock\n" +
                "#VOc mock\n" +
                "#---- accounts for vo: mockUserGroup ----#\n" +
                "jsmith mock\n";
        assertEquals("grid3-user-vo-map generated incorrectly", expectedMap, map);
    }
    
    public void testGenerateGridMapfile() {
        String mapfile = man.generateGridMapfile("known.site.com");
        String expectedGridmap = "#---- members of vo: mockUserGroup ----#\n" +
        "\"/DC=org/DC=griddev/OU=People/CN=John Smith\" jsmith\n";
        assertEquals("Grid mapfile generated incorrectly", 
        expectedGridmap,
        mapfile);
    }
    
    public void testGenerateGridMapfileOrder() {
        ManualUserGroup userGroup = new ManualUserGroup(conf, "testUserGroup");
        conf.addUserGroup(userGroup);
        PersistenceFactory persistenceFactory = new MockPersistenceFactory(conf, "testUserGroup");
        conf.addPersistenceFactory(persistenceFactory);
        userGroup.setPersistenceFactory(persistenceFactory.getName());
        userGroup.addMember(new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", null));
        userGroup.addMember(new GridUser("/DC=org/DC=griddev/OU=People/CN=Jane Doe 12345", null));

        GroupAccountMapper groupAccountMapper = new GroupAccountMapper(conf, "test");
        conf.addAccountMapper(groupAccountMapper);
        groupAccountMapper.setAccountName(groupAccountMapper.getName());
        
        GroupToAccountMapping groupToAccountMapping = new GroupToAccountMapping(conf, "mockGroup2");
        conf.addGroupToAccountMapping(groupToAccountMapping);
        groupToAccountMapping.addUserGroup(userGroup.getName());
        groupToAccountMapping.addAccountMapper(groupAccountMapper.getName());

        ((HostToGroupMapping)conf.getHostToGroupMappings().get(0)).addGroupToAccountMapping(groupToAccountMapping.getName());

        String mapfile = man.generateGridMapfile("known.site.com");
        String expectedGridmap = "#---- members of vo: mockUserGroup ----#\n" +
        "\"/DC=org/DC=griddev/OU=People/CN=John Smith\" jsmith\n" +
        "#---- members of vo: testUserGroup ----#\n" +
        "\"/DC=org/DC=griddev/OU=People/CN=Jane Doe 12345\" test\n";
        assertEquals("Grid mapfile generated incorrectly", expectedGridmap, mapfile);
    }
    
    public void testUpdate() {
        man.updateGroups();
        Collection groups = conf.getGroupToAccountMappings().values();
        Iterator iter = groups.iterator();
        while (iter.hasNext()) {
            GroupToAccountMapping gMap = (GroupToAccountMapping) iter.next();
            MockUserGroup group = (MockUserGroup) conf.getUserGroup( (String)gMap.getUserGroups().get(0) );
            group.updateMembers();
            assertTrue(group.isUpdated());
        }
    }
    
}
