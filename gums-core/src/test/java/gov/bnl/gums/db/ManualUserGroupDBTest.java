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
    
    public void tearDown() throws Exception {
    	if (db.isMemberInGroup(new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", "/atlas/group/Role=VoAdmin")))
    		db.removeMember(new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", "/atlas/group/Role=VoAdmin"));
    	if (db.isMemberInGroup(new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", null)))
    		db.removeMember(new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", null));
    	if (db.isMemberInGroup(new GridUser("/DC=org/DC=griddev/OU=People/CN=Jane Doe 12345", null)))
    		db.removeMember(new GridUser("/DC=org/DC=griddev/OU=People/CN=Jane Doe 12345", null));
    }
    
    public void testaddMembers() {
        db.addMember(new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", null));
        assertTrue(db.isMemberInGroup(new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", null)));
        assertFalse(db.isMemberInGroup(new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", "/atlas/group")));
    }
    
    public void testaddMemberWithFQAN() {
        db.addMember(new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", "/atlas/group/Role=VoAdmin"));
        assertTrue(db.isMemberInGroup(new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", "/atlas/group/Role=VoAdmin")));
        assertFalse(db.isMemberInGroup(new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", "/atlas/group")));
        assertFalse(db.isMemberInGroup(new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", null)));
    }
    
    public void testIsMemberInGroup() {
        db.addMember(new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", null));
        assertTrue(db.isMemberInGroup(new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", null)));
        assertFalse(db.isMemberInGroup(new GridUser("/DC=org/DC=griddev/OU=People/CN=Evil Person", null)));
    }
    
    public void testRemoveMember() {
        db.addMember(new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", null));
        assertTrue(db.isMemberInGroup(new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", null)));
        assertTrue(db.removeMember(new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", null)));
        assertFalse(db.isMemberInGroup(new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", null)));
        assertFalse(db.removeMember(new GridUser("/DC=org/DC=griddev/OU=People/CN=Evil Person", null)));
    }
    
    public void testRemoveMemberWithFQAN() {
        db.addMember(new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", "/atlas/group/Role=VoAdmin"));
        db.addMember(new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", null));
        assertTrue(db.isMemberInGroup(new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", "/atlas/group/Role=VoAdmin")));
        assertTrue(db.removeMember(new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", "/atlas/group/Role=VoAdmin")));
        assertFalse(db.isMemberInGroup(new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", "/atlas/group/Role=VoAdmin")));
        assertFalse(db.removeMember(new GridUser("/DC=org/DC=griddev/OU=People/CN=Evil Person", null)));
        assertTrue(db.isMemberInGroup(new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", null)));
    }
    
    public void testRetrieveMembers() {
        db.addMember(new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", null));
        db.addMember(new GridUser("/DC=org/DC=griddev/OU=People/CN=Jane Doe 12345", null));
        List members = db.retrieveMembers();
        assertEquals(2, members.size());
        Iterator iter = members.iterator();
        while (iter.hasNext()) {
            GridUser dn = (GridUser) iter.next();
            assertTrue(db.isMemberInGroup(dn));
        }
    }
    
    public void testRetrieveMembersWithFQAN() {
        GridUser user = new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", "/atlas/group/Role=VoAdmin");
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
        db.addMember(new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", null));
        try {
            db.addMember(new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", null));
            fail("Manual group allowed to enter a member twice");
        } catch (Exception e) {
            // Should generate an error
        }
    }
    
}
