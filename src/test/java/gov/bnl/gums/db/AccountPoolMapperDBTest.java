/*
 * ManualUserGroupDBTest.java
 * JUnit based test
 *
 * Created on May 25, 2004, 4:41 PM
 */

package gov.bnl.gums.db;


import java.util.*;
import junit.framework.*;

/**
 *
 * @author carcassi
 */
public class AccountPoolMapperDBTest extends TestCase {
    
    protected AccountPoolMapperDB db;
    protected long lastUsedDelay;
    
    public AccountPoolMapperDBTest(java.lang.String testName) {
        super(testName);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(AccountPoolMapperDBTest.class);
        return suite;
    }
    
    public void initDB() {
        for (int i = 0; i < 10; i++) {
            db.addAccount("pool"+i);
        }
    }
    
    public void setUp() throws Exception {
        db = new MockAccountPoolMapperDB();
        lastUsedDelay = 200;
    }
    
    public void tearDown() throws Exception {
        for (int i = 0; i < 10; i++) {
        	if (db.retrieveAccount("/DC=org/DC=doegrids/OU=People/CN=User "+i)!=null)
        		db.unassignUser("/DC=org/DC=doegrids/OU=People/CN=User "+i);
        }
    }
    
    public void testAssignAccount() {
        String account = db.assignAccount("/DC=org/DC=doegrids/OU=People/CN=John Smith");
        assertEquals(account, db.retrieveAccount("/DC=org/DC=doegrids/OU=People/CN=John Smith"));
        assertEquals(account, db.retrieveAccountMap().get("/DC=org/DC=doegrids/OU=People/CN=John Smith"));
    }
    
    public void testUnassignAccount() {
        String account = db.assignAccount("/DC=org/DC=doegrids/OU=People/CN=John Smith");
        assertEquals(account, db.retrieveAccount("/DC=org/DC=doegrids/OU=People/CN=John Smith"));
        db.unassignUser("/DC=org/DC=doegrids/OU=People/CN=John Smith");
        assertNull(db.retrieveAccount("/DC=org/DC=doegrids/OU=People/CN=John Smith"));
    }
    
    public void testLastUsedOn() throws Exception {
        try {
            String account = db.assignAccount("/DC=org/DC=doegrids/OU=People/CN=John Smith");
            assertEquals(account, db.retrieveAccount("/DC=org/DC=doegrids/OU=People/CN=John Smith"));
            Thread.sleep(lastUsedDelay);
            Date date = new Date();
            Thread.sleep(lastUsedDelay);
            assertEquals(1, db.retrieveUsersNotUsedSince(date).size());
            db.retrieveAccount("/DC=org/DC=doegrids/OU=People/CN=John Smith");
            assertEquals(0, db.retrieveUsersNotUsedSince(date).size());
        } catch (UnsupportedOperationException e) {
            // Operation can be not supported
        }
    }
    
    public void testFillPool() throws Exception {
        int n = 0;
        while (db.assignAccount("/DC=org/DC=doegrids/OU=People/CN=User "+n) != null) {
            n++;
        }
        assertEquals(10, n);
        for (int i = 0; i < n; i++) {
            db.unassignUser("/DC=org/DC=doegrids/OU=People/CN=User "+i);
        }
        for (int i = 0; i < n; i++) {
            db.assignAccount("/DC=org/DC=doegrids/OU=People/CN=User "+i);
        }
        assertNull(db.assignAccount("/DC=org/DC=doegrids/OU=People/CN=John Smith"));
    }
    
    public void testAddAccount() throws Exception {
        int n = 0;
        while (db.assignAccount("/DC=org/DC=doegrids/OU=People/CN=User "+n) != null) {
            n++;
        }
        assertEquals(10, n);
        db.addAccount("newAccount");
        assertEquals("newAccount", db.assignAccount("/DC=org/DC=doegrids/OU=People/CN=John Smith"));
        
        try {
            db.addAccount("newAccount");
        } catch (IllegalArgumentException e) {
            // exception was thrown as supposed to
        }
        
        db.unassignUser("/DC=org/DC=doegrids/OU=People/CN=John Smith");
        
        try {
            db.addAccount("newAccount");
        } catch (IllegalArgumentException e) {
            // exception was thrown as supposed to
        }
    }
    
    public static void main(String args[]) {
        Test test = suite();
        test.run(new TestResult());
    }
    
}
