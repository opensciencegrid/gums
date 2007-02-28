/*
 * CertCacheTest.java
 * JUnit based test
 *
 * Created on December 21, 2004, 1:56 PM
 */

package gov.bnl.gums.admin;

import junit.framework.*;

/**
 *
 * @author carcassi
 */
public class CertCacheTest extends TestCase {
    
    public CertCacheTest(String testName) {
        super(testName);
    }

    protected void setUp() throws java.lang.Exception {
    }

    protected void tearDown() throws java.lang.Exception {
    }
    
    public void testDNConversion() {
        String commaDN = "CN=John (Test) Smith 12345, OU=People, DC=doegrids, DC=org";
        String DN = "/DC=org/DC=doegrids/OU=People/CN=John (Test) Smith 12345";
        assertEquals(DN, CertToolkit.convertDN(commaDN));
    }
}
