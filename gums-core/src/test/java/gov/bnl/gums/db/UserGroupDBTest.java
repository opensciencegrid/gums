/*
 * UserGroupDBTest.java
 * JUnit based test
 *
 * Created on May 25, 2004, 10:38 AM
 */

package gov.bnl.gums.db;

import gov.bnl.gums.GridUser;

import java.util.*;

import junit.framework.*;

/**
 *
 * @author carcassi
 */
public class UserGroupDBTest extends TestCase {
    
    protected UserGroupDB db;
    
    public UserGroupDBTest(java.lang.String testName) {
        super(testName);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(UserGroupDBTest.class);
        return suite;
    }

    public void setUp() throws Exception {
        db = new MockUserGroupDB();
        // Tests assume the UserGroupDB object has a couple of values
        initDB();
    }
    
    public void tearDown() throws Exception {
        db.loadUpdatedList(new ArrayList());    	
    }
    
    protected void initDB() {
        List mockUsers = new ArrayList();
        mockUsers.add(new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", "/griddev/subgriddev/Role=griddevrole"));
        mockUsers.add(new GridUser("/DC=org/DC=griddev/OU=People/CN=Jane Doe 12345", "/griddev/subgriddev/Role=griddevrole"));
        db.loadUpdatedList(mockUsers);
    }
    
    public void testIsMemberInGroup() {
        assertTrue(db.isMemberInGroup(new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", "/griddev/subgriddev/Role=griddevrole")));
        assertFalse(db.isMemberInGroup(new GridUser("/DC=org/DC=griddev/OU=People/CN=Evil Person", "/griddev/subgriddev/Role=griddevrole")));
        assertFalse(db.isMemberInGroup(new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", "/myGroup/mySubGroup")));
    }
    
    public void testRetrieveMembers() {
        List members = db.retrieveMembers();
        Iterator iter = members.iterator();
        while (iter.hasNext()) {
            GridUser user = (GridUser) iter.next();
            assertTrue(db.isMemberInGroup(user));
        }
    }
    
    public void testUpdate() {
        UserGroupDB db = new MockUserGroupDB();
        assertNull(db.retrieveNewMembers());
        assertNull(db.retrieveRemovedMembers());
        
        List newMemberList = new ArrayList();
        newMemberList.add(new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", "/griddev/subgriddev/Role=griddevrole"));
        newMemberList.add(new GridUser("/DC=org/DC=griddev/OU=People/CN=Jason Smith", "/griddev/subgriddev/Role=griddevrole"));
        db.loadUpdatedList(newMemberList);
        List newMembers = db.retrieveNewMembers();
        List removedMembers = db.retrieveRemovedMembers();
        assertEquals(1, newMembers.size());
        assertEquals("GridID[/DC=org/DC=griddev/OU=People/CN=Jason Smith, /griddev/subgriddev/Role=griddevrole]", newMembers.get(0).toString());
        assertEquals(1, removedMembers.size());
        
        newMemberList = new ArrayList();
        newMemberList.add(new GridUser("/DC=org/DC=griddev/OU=People/CN=Jane Doe 12345", "/griddev/subgriddev/Role=griddevrole"));
        newMemberList.add(new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", "/griddev/subgriddev/Role=griddevrole"));
        db.loadUpdatedList(newMemberList);
        newMembers = db.retrieveNewMembers();
        removedMembers = db.retrieveRemovedMembers();
        assertEquals(1, newMembers.size());
        assertEquals("GridID[/DC=org/DC=griddev/OU=People/CN=Jane Doe 12345, /griddev/subgriddev/Role=griddevrole]", newMembers.get(0).toString());
        assertEquals(1, removedMembers.size());
        assertEquals("GridID[/DC=org/DC=griddev/OU=People/CN=Jason Smith, /griddev/subgriddev/Role=griddevrole]", removedMembers.get(0).toString());
    }

}
