/*
 * NISAccountMapperTest.java
 * JUnit based test
 *
 * Created on May 25, 2004, 2:36 PM
 */

package gov.bnl.gums.account;

import gov.bnl.gums.configuration.Configuration;
import junit.framework.*;

/**
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
        NISAccountMapper gMapper = new NISAccountMapper(new Configuration(), "NisAccountMapper");
        mapper = gMapper;
        gMapper.setJndiNisUrl("nis://130.199.48.26/usatlas.bnl.gov");
    }
    
    public void testMapUser() {
        assertEquals("carcassi", mapper.mapUser("/DC=org/DC=doegrids/OU=People/CN=Gabriele Carcassi"));
        assertEquals(null, mapper.mapUser("/DC=org/DC=doegrids/OU=People/CN=Evil Person"));
    }
    
    public void testNameSurnameFromCertificateSubject() {
        String carcassiDN = "/DC=org/DC=doegrids/OU=People/CN=Gabriele Carcassi";
        String[] carcassi = mapper.parseNameAndSurname(carcassiDN);
        assertEquals("Gabriele", carcassi[0]);
        assertEquals("Carcassi", carcassi[1]);
        String[] timThomas = mapper.parseNameAndSurname("/DC=org/DC=doegrids/OU=People/CN=Timothy L. Thomas 324580");
        assertEquals("Timothy", timThomas[0]);
        assertEquals("Thomas", timThomas[1]);
        String[] robGardner = mapper.parseNameAndSurname("/DC=org/DC=doegrids/OU=People/CN=Robert W. Gardner Jr. 669916");
        assertEquals("Robert", robGardner[0]);
        assertEquals("Gardner", robGardner[1]);
    }
    
    public void testCheckSurname() {
        assertTrue(mapper.checkSurname("Carcassi"));
        assertFalse(mapper.checkSurname("1234"));
        assertFalse(mapper.checkSurname("Jr."));
    }    
}
