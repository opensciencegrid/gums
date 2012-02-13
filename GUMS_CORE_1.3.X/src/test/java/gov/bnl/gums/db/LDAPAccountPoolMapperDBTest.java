/*
 * LDAPAccountPoolMapperDBTest.java
 * JUnit based test
 *
 * Created on June 16, 2005, 10:17 AM
 */

package gov.bnl.gums.db;

import java.sql.*;
import junit.framework.*;
import gov.bnl.gums.*;
import gov.bnl.gums.configuration.Configuration;
import gov.bnl.gums.persistence.LDAPPersistenceFactory;
import gov.bnl.gums.persistence.LDAPPersistenceFactoryTest;
import org.hibernate.*;
import org.apache.log4j.Logger;

/**extends AccountPoolMapperDBTest
 *
 * @author carcassi
 */
public class LDAPAccountPoolMapperDBTest extends AccountPoolMapperDBTest {
    
    public LDAPAccountPoolMapperDBTest(String testName) {
        super(testName);
    }

    public void setUp() throws Exception {
        LDAPPersistenceFactory factory = new LDAPPersistenceFactory(new Configuration(), "ldapPers1");
        factory.setEmailField("homeDirectory"); // hack since current test ldap schema doesn't have an email field
        factory.setProperties(LDAPPersistenceFactoryTest.readLdapProperties());
        db = factory.retrieveAccountPoolMapperDB("testPool.griddevGroup.griddevGroup");
        initDB();
    }
    
    public void tearDown() {
    	LDAPPersistenceFactory factory = new LDAPPersistenceFactory(new Configuration(), "ldapPers1");
        factory.setProperties(LDAPPersistenceFactoryTest.readLdapProperties());
    	try {
            factory.destroyMap("testPool", "map=testPool,ou=GUMS");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(LDAPAccountPoolMapperDBTest.class);
        
        return suite;
    }
    
    public void testResetPool() {
        ((LDAPAccountMapperDB) db).unassignAccount("pool0");
        ((LDAPAccountMapperDB) db).unassignAccount("pool1");
        ((LDAPAccountMapperDB) db).unassignAccount("pool2");
        assertEquals("pool0", db.assignAccount(new GridUser("test")));
        assertEquals("pool1", db.assignAccount(new GridUser("test2")));
        assertEquals("pool2", db.assignAccount(new GridUser("test3")));
        ((LDAPAccountMapperDB) db).unassignUser("test");
        ((LDAPAccountMapperDB) db).unassignUser("test2");
        ((LDAPAccountMapperDB) db).unassignUser("test3");
        assertEquals("pool0", db.assignAccount(new GridUser("test4")));
        assertEquals("pool1", db.assignAccount(new GridUser("test5")));
        assertEquals("pool2", db.assignAccount(new GridUser("test6")));
        ((LDAPAccountMapperDB) db).unassignAccount("pool0");
        ((LDAPAccountMapperDB) db).unassignAccount("pool1");
        ((LDAPAccountMapperDB) db).unassignAccount("pool2");
        assertEquals("pool0", db.assignAccount(new GridUser("test4")));
        assertEquals("pool1", db.assignAccount(new GridUser("test5")));
        assertEquals("pool2", db.assignAccount(new GridUser("test6", null, "pool2@griddev.org")));
        assertEquals("pool2@griddev.org", ((LDAPAccountMapperDB) db).retrieveEmail("pool2"));
    }
}
