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
import net.sf.hibernate.*;
import org.apache.commons.logging.*;

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
        factory.setProperties(LDAPPersistenceFactoryTest.readLdapProperties());
        try {
            factory.destroyMap("testPool", "map=testPool,ou=GUMS");
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        db.addAccount("grid001");
        db.addAccount("grid002");
        db.addAccount("grid003");
        db.addAccount("grid004");
        db.addAccount("grid005");
        db.addAccount("grid006");
        assertEquals("grid001", db.assignAccount("test"));
        assertEquals("grid002", db.assignAccount("test2"));
        assertEquals("grid003", db.assignAccount("test3"));
        ((LDAPAccountMapperDB) db).unassignUser("test");
        ((LDAPAccountMapperDB) db).unassignUser("test2");
        ((LDAPAccountMapperDB) db).unassignUser("test3");
        assertEquals("grid001", db.assignAccount("test4"));
        assertEquals("grid002", db.assignAccount("test5"));
        assertEquals("grid003", db.assignAccount("test6"));
        ((LDAPAccountMapperDB) db).unassignAccount("grid001");
        ((LDAPAccountMapperDB) db).unassignAccount("grid002");
        ((LDAPAccountMapperDB) db).unassignAccount("grid003");
        assertEquals("grid001", db.assignAccount("test4"));
        assertEquals("grid002", db.assignAccount("test5"));
        assertEquals("grid003", db.assignAccount("test6"));
    }
}
