/*
 * CompositeAccountMapperTest.java
 * JUnit based test
 *
 * Created on May 25, 2004, 3:26 PM
 */

package gov.bnl.gums.db;


import junit.framework.*;

/**
 *
 * @author carcassi
 */
public class ManualAccountMapperDBTest extends TestCase {
    
    protected ManualAccountMapperDB db;
    
    public ManualAccountMapperDBTest(String testName) {
        super(testName);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(ManualAccountMapperDBTest.class);
        return suite;
    }
    
    public void setUp() throws Exception {
        db = new MockManualAccountMapperDB();
    }
    
    public void tearDown() throws Exception {
    }
    
    public void testCreateMapping() {
        db.createMapping("/DC=org/DC=griddev/OU=People/CN=John Smith", "jsmith");
        assertEquals("jsmith", db.retrieveMapping("/DC=org/DC=griddev/OU=People/CN=John Smith"));
        assertEquals(null, db.retrieveMapping("/DC=org/DC=griddev/OU=People/CN=Evil Persons"));
    }
    
    public void testRetrieveMapping() {
        assertEquals(null, db.retrieveMapping("/DC=org/DC=griddev/OU=People/CN=John Smith"));
        db.createMapping("/DC=org/DC=griddev/OU=People/CN=John Smith", "jsmith");
        assertEquals("jsmith", db.retrieveMapping("/DC=org/DC=griddev/OU=People/CN=John Smith"));
        assertEquals(null, db.retrieveMapping("/DC=org/DC=griddev/OU=People/CN=Evil Persons"));
    }
    
    public void testRemoveMapping() {
        db.createMapping("/DC=org/DC=griddev/OU=People/CN=John Smith", "jsmith");
        assertEquals("jsmith", db.retrieveMapping("/DC=org/DC=griddev/OU=People/CN=John Smith"));
        assertTrue(db.removeMapping("/DC=org/DC=griddev/OU=People/CN=John Smith"));
        assertEquals(null, db.retrieveMapping("/DC=org/DC=griddev/OU=People/CN=John Smith"));
        assertFalse(db.removeMapping("/DC=org/DC=griddev/OU=People/CN=Evil Person"));
        assertEquals(null, db.retrieveMapping("/DC=org/DC=griddev/OU=People/CN=Evil Persons"));
    }
    
}
