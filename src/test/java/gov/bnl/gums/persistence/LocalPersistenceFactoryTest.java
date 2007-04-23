/*
 * BNLPersistenceFactoryTest.java
 * JUnit based test
 *
 * Created on February 3, 2005, 10:10 AM
 */

package gov.bnl.gums.persistence;

import gov.bnl.gums.configuration.Configuration;
import junit.framework.*;
import java.util.Enumeration;
import java.util.Properties;

/**
 *
 * @author carcassi
 */
public class LocalPersistenceFactoryTest extends TestCase {
    
    LocalPersistenceFactory factory;
    Configuration configuration;
    
    public LocalPersistenceFactoryTest() {
        super("test");
        this.configuration = new Configuration();
    }

    protected void setUp() throws java.lang.Exception {
        factory = new LocalPersistenceFactory(configuration, "test");
    }

    protected void tearDown() throws java.lang.Exception {
    }

    public static junit.framework.Test suite() {
        junit.framework.TestSuite suite = new junit.framework.TestSuite(LocalPersistenceFactoryTest.class);
        
        return suite;
    }

    public void testRetrieveUserGroupDB() {
        assertTrue(factory.retrieveUserGroupDB("test").toString().indexOf("HibernateUserGroupDB") != -1);
    }

    public void testRetrieveManualUserGroupDB() {
        assertTrue(factory.retrieveManualUserGroupDB("test").toString().indexOf("HibernateUserGroupDB") != -1);
    }

    public void testRetrieveManualAccountMapperDB() {
        assertTrue(factory.retrieveManualAccountMapperDB("test").toString().indexOf("HibernateAccountMapperDB") != -1);
    }

    public void testRetrieveAccountPoolMapperDB() {
        assertTrue(factory.retrieveAccountPoolMapperDB("test").toString().indexOf("HibernateAccountMapperDB") != -1);
        assertTrue(factory.retrieveAccountPoolMapperDB("test.pool").toString().indexOf("LocalAccountPoolMapperDB") != -1);
        assertTrue(factory.retrieveAccountPoolMapperDB("test.fail.pool").toString().indexOf("LocalAccountPoolMapperDB") != -1);
    }
    
    public void testProperties() {
        Properties prop = new Properties();
        prop.put("name", "test");
        prop.put("mysql.driver", "com.mysql.jdbc.Driver");
        prop.put("ldap.url", "ldap://myserver.bnl.gov/dc=bnl,dc=gov");
        factory.setProperties(prop);
        assertEquals(prop, factory.getProperties());
        assertEquals("com.mysql.jdbc.Driver", factory.getMySQLProperties().getProperty("driver"));
        assertNull(factory.getMySQLProperties().getProperty("name"));
        assertEquals("ldap://myserver.bnl.gov/dc=bnl,dc=gov", factory.getLDAPProperties().getProperty("url"));
    }
    
}
