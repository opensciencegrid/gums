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
    
    protected void initDB() {
        List mockUsers = new ArrayList();
        mockUsers.add(new GridUser("/DC=org/DC=doegrids/OU=People/CN=Gabriele Carcassi", null));
        mockUsers.add(new GridUser("/DC=org/DC=doegrids/OU=People/CN=Jason Smith", null));
        db.loadUpdatedList(mockUsers);
    }
    
    public void testIsMemberInGroup() {
        assertTrue(db.isMemberInGroup(new GridUser("/DC=org/DC=doegrids/OU=People/CN=Gabriele Carcassi", null)));
        assertFalse(db.isMemberInGroup(new GridUser("/DC=org/DC=doegrids/OU=People/CN=Evil Person", null)));
        assertFalse(db.isMemberInGroup(new GridUser("/DC=org/DC=doegrids/OU=People/CN=Gabriele Carcassi", "/atlas/usatlas")));
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
        newMemberList.add(new GridUser("/DC=org/DC=doegrids/OU=People/CN=Gabriele Carcassi", null));
        newMemberList.add(new GridUser("/DC=org/DC=doegrids/OU=People/CN=Evil Person", null));
        db.loadUpdatedList(newMemberList);
        List newMembers = db.retrieveNewMembers();
        List removedMembers = db.retrieveRemovedMembers();
        assertEquals(1, newMembers.size());
        assertEquals("GridID[/DC=org/DC=doegrids/OU=People/CN=Evil Person]", newMembers.get(0).toString());
        assertEquals(0, removedMembers.size());
        
        newMemberList = new ArrayList();
        newMemberList.add(new GridUser("/DC=org/DC=doegrids/OU=People/CN=Dantong Yu", null));
        newMemberList.add(new GridUser("/DC=org/DC=doegrids/OU=People/CN=Gabriele Carcassi", null));
        db.loadUpdatedList(newMemberList);
        newMembers = db.retrieveNewMembers();
        removedMembers = db.retrieveRemovedMembers();
        assertEquals(1, newMembers.size());
        assertEquals("GridID[/DC=org/DC=doegrids/OU=People/CN=Dantong Yu]", newMembers.get(0).toString());
        assertEquals(1, removedMembers.size());
        assertEquals("GridID[/DC=org/DC=doegrids/OU=People/CN=Evil Person]", removedMembers.get(0).toString());
    }
    
    public void testUpdate2() {
        List newMemberList = new ArrayList();
        newMemberList.add(new GridUser("/DC=org/DC=doegrids/OU=People/CN=Gabriele Carcassi", null));
        newMemberList.add(new GridUser("/DC=org/DC=doegrids/OU=People/CN=Jason Smith", null));
        newMemberList.add(new GridUser("/DC=org/DC=doegrids/OU=People/CN=Evil Person", null));
        db.loadUpdatedList(newMemberList);
        List newMembers = db.retrieveNewMembers();
        List removedMembers = db.retrieveRemovedMembers();
        assertEquals(1, newMembers.size());
        assertEquals(new GridUser("/DC=org/DC=doegrids/OU=People/CN=Evil Person", null), newMembers.get(0));
        assertEquals(0, removedMembers.size());
        
        newMemberList = new ArrayList();
        newMemberList.add(new GridUser("/DC=org/DC=doegrids/OU=People/CN=Dantong Yu", null));
        newMemberList.add(new GridUser("/DC=org/DC=doegrids/OU=People/CN=Gabriele Carcassi", null));
        newMemberList.add(new GridUser("/DC=org/DC=doegrids/OU=People/CN=Jason Smith", null));
        db.loadUpdatedList(newMemberList);
        newMembers = db.retrieveNewMembers();
        removedMembers = db.retrieveRemovedMembers();
        assertEquals(1, newMembers.size());
        assertEquals(new GridUser("/DC=org/DC=doegrids/OU=People/CN=Dantong Yu", null), newMembers.get(0));
        assertEquals(1, removedMembers.size());
        assertEquals(new GridUser("/DC=org/DC=doegrids/OU=People/CN=Evil Person", null), removedMembers.get(0));
        assertFalse(db.isMemberInGroup(new GridUser("/DC=org/DC=doegrids/OU=People/CN=Evil Person", null)));
    }
}
