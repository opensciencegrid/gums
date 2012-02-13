/*
 * GecosNisAccountMapperTest.java
 * JUnit based test
 *
 * Created on May 10, 2005, 11:39 AM
 */

package gov.bnl.gums.account;

import java.util.Enumeration;
import java.util.Properties;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import gov.bnl.gums.account.GecosMap;
import gov.bnl.gums.configuration.Configuration;
import junit.framework.*;

/**
 * If you want to use this junit test, you need to fill in the values for your site below
 * 
 * @author carcassi
 */
public class GecosNisAccountMapperTest extends TestCase {
    GecosNisAccountMapper mapper;
    
    public GecosNisAccountMapperTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        PropertyResourceBundle prop = (PropertyResourceBundle) ResourceBundle.getBundle("nis");
        Properties properties = new Properties();
        Enumeration keys = prop.getKeys();
        while (keys.hasMoreElements()) {
            String key = (String) keys.nextElement();
            properties.setProperty(key, prop.getString(key));
        }
        mapper = new GecosNisAccountMapper(new Configuration(), "GecosNisAccountMapper");
        mapper.setJndiNisUrl( properties.getProperty("java.naming.provider.url") );
    }

    protected void tearDown() throws Exception {
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(GecosNisAccountMapperTest.class);
        
        return suite;
    }

    public void testGetJndiNisUrl() {
        GecosMap map = mapper.createMap();
        String account = map.findAccount("John",  "Smith");
        assertEquals("jsmith", account);
    }
    
}
