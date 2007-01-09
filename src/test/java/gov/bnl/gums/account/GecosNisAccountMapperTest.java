/*
 * GecosNisAccountMapperTest.java
 * JUnit based test
 *
 * Created on May 10, 2005, 11:39 AM
 */

package gov.bnl.gums.account;

import junit.framework.*;
import java.util.Properties;
import javax.naming.NamingEnumeration;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchResult;

/**
 *
 * @author carcassi
 */
public class GecosNisAccountMapperTest extends TestCase {
    GecosNisAccountMapper mapper;
    
    public GecosNisAccountMapperTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        mapper = new GecosNisAccountMapper();
        mapper.setJndiNisUrl("nis://130.199.48.26/usatlas.bnl.gov");
    }

    protected void tearDown() throws Exception {
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(GecosNisAccountMapperTest.class);
        
        return suite;
    }

    public void testGetJndiNisUrl() {
        GecosMap map = mapper.createMap();
        String account = map.findAccount("Gabriele",  "Carcassi");
        assertEquals("carcassi", account);
    }
    
}
