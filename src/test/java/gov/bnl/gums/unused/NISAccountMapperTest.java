/*
 * NISAccountMapperTest.java
 * JUnit based test
 *
 * Created on May 25, 2004, 2:36 PM
 */

package gov.bnl.gums.unused;

import gov.bnl.gums.account.NISAccountMapper;
import gov.bnl.gums.configuration.Configuration;
import junit.framework.*;

/**
 * If you want to use this junit test, you need to fill in the values for your site below
 *
 * @author carcassi
 */
public class NISAccountMapperTest extends TestCase {
    
    NISAccountMapper mapper;
    
    public NISAccountMapperTest(java.lang.String testName) {
        super(testName);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(NISAccountMapperTest.class);
        return suite;
    }
    
    
    public void setUp() {
    	mapper = new NISAccountMapper(new Configuration(), "NisAccountMapper");
        mapper.setJndiNisUrl("nis://130.199.48.26/usatlas.bnl.gov");
    }
    
    public void testMapUser() {
        assertEquals("jsmith", mapper.mapUser("/DC=org/DC=griddev/OU=People/CN=John Smith"));
        assertEquals(null, mapper.mapUser("/DC=org/DC=griddev/OU=People/CN=Evil Person"));
    }
    
    public void testNameSurnameFromCertificateSubject() {
        String dn = "/DC=org/DC=griddev/OU=People/CN=John Smith";
        String[] name = mapper.parseNameAndSurname(dn);
        assertEquals("John", name[0]);
        assertEquals("Smith", name[1]);
        name = mapper.parseNameAndSurname("/DC=org/DC=griddev/OU=People/CN=Jane Doe 12345");
        assertEquals("Jane", name[0]);
        assertEquals("Doe", name[1]);
    }
    
    public void testCheckSurname() {
        assertTrue(mapper.checkSurname("Smith"));
        assertFalse(mapper.checkSurname("1234"));
        assertFalse(mapper.checkSurname("Jr."));
    }    
}
