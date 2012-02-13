/*
 * ManualUserGroupTest.java
 * JUnit based test
 *
 * Created on May 25, 2004, 11:41 AM
 */

package gov.bnl.gums.userGroup;

import gov.bnl.gums.configuration.Configuration;
import gov.bnl.gums.persistence.MockPersistenceFactory;
import gov.bnl.gums.persistence.PersistenceFactory;
import gov.bnl.gums.GridUser;

import java.util.*;

import junit.framework.*;

/**
 *
 * @author carcassi
 */
public class ManualUserGroupTest extends TestCase {
    
    UserGroup group;
    Configuration configuration = new Configuration();
    
    public ManualUserGroupTest(java.lang.String testName) {
        super(testName);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(ManualUserGroupTest.class);
        return suite;
    }
    
    public void setUp() {
        ManualUserGroup userGroup = new ManualUserGroup(configuration, "mockUserGroup");
        configuration.addUserGroup(userGroup);
        PersistenceFactory persistenceFactory = new MockPersistenceFactory(configuration, "mockPers");
        configuration.addPersistenceFactory(persistenceFactory);
        userGroup.setPersistenceFactory(persistenceFactory.getName());
        group = userGroup;
    }
    
    public void testUpdateMembers() {
        group.updateMembers();
    }

    public void testToString() {
        assertEquals("ManualUserGroup: persistenceFactory='mockPers' - group='mockUserGroup'", group.toString());
    }
    
    public void testAddMember() {
        ((ManualUserGroup) group).addMember(new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", null));
        assertTrue(group.isInGroup(new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", null)));
    }

    public void testRemoveMember() {
        ((ManualUserGroup) group).addMember(new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", null));
        assertTrue(group.isInGroup(new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", null)));
        assertTrue(((ManualUserGroup) group).removeMember(new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", null)));
        assertFalse(group.isInGroup(new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", null)));
    }
    
    public void testGetMemberList() {
        ((ManualUserGroup) group).addMember(new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", null));
        ((ManualUserGroup) group).addMember(new GridUser("/DC=org/DC=griddev/OU=People/CN=Jane Doe 12345", null));
        List members = group.getMemberList();
        assertTrue(members.size() > 0);
        Iterator iter = members.iterator();
        while (iter.hasNext()) {
            GridUser user = (GridUser) iter.next();
            assertTrue(group.isInGroup(user));
        }
    }
    
}
