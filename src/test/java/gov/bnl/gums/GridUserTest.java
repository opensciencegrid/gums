/*
 * GridUserTest.java
 * JUnit based test
 *
 * Created on September 10, 2004, 11:55 AM
 */

package gov.bnl.gums;

import junit.framework.*;

/**
 *
 * @author carcassi
 */
public class GridUserTest extends TestCase {
    
    public GridUserTest(java.lang.String testName) {
        super(testName);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(GridUserTest.class);
        return suite;
    }
    
    public void testEquals() {
        GridUser a = new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", null);
        GridUser b = new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", null);
        assertEquals(a, b);
        assertEquals(b, a);
    }
    
    public void testEquals2() {
        GridUser a = new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", null);
        GridUser b = new GridUser("/DC=org/DC=griddev/OU=People/CN=Evil Person", null);
        assertFalse(a.equals(b));
        assertFalse(b.equals(a));
    }
    
    public void testEquals3() {
        GridUser a = new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", "/myGroup/mySubGroup/Role=admin/Capability=important");
        GridUser b = new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", "/myGroup/mySubGroup/Role=admin/Capability=important");
        assertEquals(a, b);
        assertEquals(b, a);
    }
    
    public void testEquals4() {
        GridUser a = new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", "/myGroup/mySubGroup/Role=admin/Capability=important");
        GridUser b = new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", null);
        assertFalse(a.equals(b));
        assertFalse(b.equals(a));
    }
    
    public void testEquals5() {
        GridUser a = new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", "/myGroup/mySubGroup/Role=admin/Capability=important");
        GridUser b = new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", "/myGroup/mySubGroup/Role=user");
        assertFalse(a.equals(b));
        assertFalse(b.equals(a));
    }
    
    public void testEquals6() {
        GridUser a = new GridUser(null, null);
        GridUser b = new GridUser("/DC=org/DC=griddev/OU=People/CN=Evil Person", null);
        assertFalse(a.equals(b));
        assertFalse(b.equals(a));
    }
    
}
