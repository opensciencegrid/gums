/*
 * CompositeAccountMapperTest.java
 * JUnit based test
 *
 * Created on May 25, 2004, 3:26 PM
 */

package gov.bnl.gums.db;


import java.sql.*;
import java.util.*;
import junit.framework.*;

/**
 *
 * @author carcassi
 */
public class ManualAccountMapperDBTest extends TestCase {
    
    protected ManualAccountMapperDB db;
    
    public ManualAccountMapperDBTest(java.lang.String testName) {
        super(testName);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(MySQLManualAccountMapperDBTest.class);
        return suite;
    }
    
    public void setUp() throws Exception {
        db = new MockManualAccountMapperDB();
    }
    
    public void testCreateMapping() {
        db.createMapping("/DC=org/DC=doegrids/OU=People/CN=Gabriele Carcassi", "carcassi");
        assertEquals("carcassi", db.retrieveMapping("/DC=org/DC=doegrids/OU=People/CN=Gabriele Carcassi"));
        assertEquals(null, db.retrieveMapping("/DC=org/DC=doegrids/OU=People/CN=Evil Persons"));
    }
    
    public void testRetrieveMapping() {
        assertEquals(null, db.retrieveMapping("/DC=org/DC=doegrids/OU=People/CN=Gabriele Carcassi"));
        db.createMapping("/DC=org/DC=doegrids/OU=People/CN=Gabriele Carcassi", "carcassi");
        assertEquals("carcassi", db.retrieveMapping("/DC=org/DC=doegrids/OU=People/CN=Gabriele Carcassi"));
        assertEquals(null, db.retrieveMapping("/DC=org/DC=doegrids/OU=People/CN=Evil Persons"));
    }
    
    public void testRemoveMapping() {
        db.createMapping("/DC=org/DC=doegrids/OU=People/CN=Gabriele Carcassi", "carcassi");
        assertEquals("carcassi", db.retrieveMapping("/DC=org/DC=doegrids/OU=People/CN=Gabriele Carcassi"));
        assertTrue(db.removeMapping("/DC=org/DC=doegrids/OU=People/CN=Gabriele Carcassi"));
        assertEquals(null, db.retrieveMapping("/DC=org/DC=doegrids/OU=People/CN=Gabriele Carcassi"));
        assertFalse(db.removeMapping("/DC=org/DC=doegrids/OU=People/CN=Evil Person"));
        assertEquals(null, db.retrieveMapping("/DC=org/DC=doegrids/OU=People/CN=Evil Persons"));
    }
    
}
