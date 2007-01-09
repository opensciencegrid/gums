/*
 * ManualUserGroupDBTest.java
 * JUnit based test
 *
 * Created on May 25, 2004, 4:41 PM
 */

package gov.bnl.gums.db;

import gov.bnl.gums.GridUser;

import java.util.Iterator;
import java.util.List;

import junit.framework.*;

/**
 *
 * @author carcassi
 */
public class ManualUserGroupDBTest extends TestCase {
    
    protected ManualUserGroupDB db;
    
    public ManualUserGroupDBTest(java.lang.String testName) {
        super(testName);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(ManualUserGroupDBTest.class);
        return suite;
    }
    
    public void setUp() throws Exception {
        db = new MockManualUserGroupDB();
    }
    
    public void testaddMembers() {
        db.addMember(new GridUser("/DC=org/DC=doegrids/OU=People/CN=Gabriele Carcassi", null));
        assertTrue(db.isMemberInGroup(new GridUser("/DC=org/DC=doegrids/OU=People/CN=Gabriele Carcassi", null)));
        assertFalse(db.isMemberInGroup(new GridUser("/DC=org/DC=doegrids/OU=People/CN=Gabriele Carcassi", "/atlas/group")));
    }
    
    public void testaddMemberWithFQAN() {
        db.addMember(new GridUser("/DC=org/DC=doegrids/OU=People/CN=Gabriele Carcassi", "/atlas/group/Role=VoAdmin"));
        assertTrue(db.isMemberInGroup(new GridUser("/DC=org/DC=doegrids/OU=People/CN=Gabriele Carcassi", "/atlas/group/Role=VoAdmin")));
        assertFalse(db.isMemberInGroup(new GridUser("/DC=org/DC=doegrids/OU=People/CN=Gabriele Carcassi", "/atlas/group")));
        assertFalse(db.isMemberInGroup(new GridUser("/DC=org/DC=doegrids/OU=People/CN=Gabriele Carcassi", null)));
    }
    
    public void testIsMemberInGroup() {
        db.addMember(new GridUser("/DC=org/DC=doegrids/OU=People/CN=Gabriele Carcassi", null));
        assertTrue(db.isMemberInGroup(new GridUser("/DC=org/DC=doegrids/OU=People/CN=Gabriele Carcassi", null)));
        assertFalse(db.isMemberInGroup(new GridUser("/DC=org/DC=doegrids/OU=People/CN=Evil Person", null)));
    }
    
    public void testRemoveMember() {
        db.addMember(new GridUser("/DC=org/DC=doegrids/OU=People/CN=Gabriele Carcassi", null));
        assertTrue(db.isMemberInGroup(new GridUser("/DC=org/DC=doegrids/OU=People/CN=Gabriele Carcassi", null)));
        assertTrue(db.removeMember(new GridUser("/DC=org/DC=doegrids/OU=People/CN=Gabriele Carcassi", null)));
        assertFalse(db.isMemberInGroup(new GridUser("/DC=org/DC=doegrids/OU=People/CN=Gabriele Carcassi", null)));
        assertFalse(db.removeMember(new GridUser("/DC=org/DC=doegrids/OU=People/CN=Evil Person", null)));
    }
    
    public void testRemoveMemberWithFQAN() {
        db.addMember(new GridUser("/DC=org/DC=doegrids/OU=People/CN=Gabriele Carcassi", "/atlas/group/Role=VoAdmin"));
        db.addMember(new GridUser("/DC=org/DC=doegrids/OU=People/CN=Gabriele Carcassi", null));
        assertTrue(db.isMemberInGroup(new GridUser("/DC=org/DC=doegrids/OU=People/CN=Gabriele Carcassi", "/atlas/group/Role=VoAdmin")));
        assertTrue(db.removeMember(new GridUser("/DC=org/DC=doegrids/OU=People/CN=Gabriele Carcassi", "/atlas/group/Role=VoAdmin")));
        assertFalse(db.isMemberInGroup(new GridUser("/DC=org/DC=doegrids/OU=People/CN=Gabriele Carcassi", "/atlas/group/Role=VoAdmin")));
        assertFalse(db.removeMember(new GridUser("/DC=org/DC=doegrids/OU=People/CN=Evil Person", null)));
        assertTrue(db.isMemberInGroup(new GridUser("/DC=org/DC=doegrids/OU=People/CN=Gabriele Carcassi", null)));
    }
    
    public void testRetrieveMembers() {
        db.addMember(new GridUser("/DC=org/DC=doegrids/OU=People/CN=Gabriele Carcassi", null));
        db.addMember(new GridUser("/DC=org/DC=doegrids/OU=People/CN=Dantong Yu", null));
        List members = db.retrieveMembers();
        assertEquals(2, members.size());
        Iterator iter = members.iterator();
        while (iter.hasNext()) {
            GridUser dn = (GridUser) iter.next();
            assertTrue(db.isMemberInGroup(dn));
        }
    }
    
    public void testRetrieveMembersWithFQAN() {
        GridUser user = new GridUser("/DC=org/DC=doegrids/OU=People/CN=Gabriele Carcassi", "/atlas/group/Role=VoAdmin");
        db.addMember(user);
        List members = db.retrieveMembers();
        assertEquals(1, members.size());
        Iterator iter = members.iterator();
        while (iter.hasNext()) {
            GridUser user2 = (GridUser) iter.next();
            assertTrue(db.isMemberInGroup(user2));
            assertEquals(user, user2);
        }
    }
    
    public void testDoubleAdd() {
        db.addMember(new GridUser("/DC=org/DC=doegrids/OU=People/CN=Gabriele Carcassi", null));
        try {
            db.addMember(new GridUser("/DC=org/DC=doegrids/OU=People/CN=Gabriele Carcassi", null));
            fail("Manual group allowed to enter a member twice");
        } catch (Exception e) {
            // Should generate an error
        }
    }
    
}
