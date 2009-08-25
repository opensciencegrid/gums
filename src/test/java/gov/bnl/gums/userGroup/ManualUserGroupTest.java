/*
 * VOMSGroupTest.java
 * JUnit based test
 *
 * Created on May 25, 2004, 11:41 AM
 */

package gov.bnl.gums.userGroup;

import gov.bnl.gums.configuration.Configuration;
import gov.bnl.gums.GridUser;

import java.net.URL;
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
        group = userGroup;
    }
    
    public void testUpdateMembers() {
    	ManualUserGroup userGroup = (ManualUserGroup)group;
    	URL url = getClass().getClassLoader().getResource("manual_members");
        userGroup.setMembersUri(url.toString());
        userGroup.updateMembers();
        assertTrue(group.isMember(new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", "/griddev")));
        assertTrue(group.isMember(new GridUser("/DC=org/DC=griddev/OU=People/CN=Jane Doe 12345", null)));
        group.updateMembers();
    }
    
    public void testAddMember() {
        ((ManualUserGroup) group).addMember(new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", null));
        assertTrue(group.isMember(new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", null)));
    }

    public void testRemoveMember() {
        ((ManualUserGroup) group).addMember(new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", null));
        assertTrue(group.isMember(new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", null)));
        assertTrue(((ManualUserGroup) group).removeMember(new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", null)));
        assertFalse(group.isMember(new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", null)));
    }
    
    public void testGetMemberList() {
        ((ManualUserGroup) group).addMember(new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", null));
        ((ManualUserGroup) group).addMember(new GridUser("/DC=org/DC=griddev/OU=People/CN=Jane Doe 12345", null));
        Set<GridUser> members = group.getMembers();
        assertTrue(members.size() > 0);
        Iterator<GridUser> iter = members.iterator();
        while (iter.hasNext()) {
            GridUser user = (GridUser) iter.next();
            assertTrue(group.isMember(user));
        }
    }
    
    public void testFqan() {
        ((ManualUserGroup) group).addMember(new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", "/griddev"));
        assertFalse(group.isMember(new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", null)));
        assertTrue(group.isMember(new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", "/griddev")));
        ((ManualUserGroup) group).addMember(new GridUser("/DC=org/DC=griddev/OU=People/CN=Jane Doe 12345", null));
        assertTrue(group.isMember(new GridUser("/DC=org/DC=griddev/OU=People/CN=Jane Doe 12345", null)));
        assertFalse(group.isMember(new GridUser("/DC=org/DC=griddev/OU=People/CN=Jane Doe 12345", "/griddev")));
    }
    
}
