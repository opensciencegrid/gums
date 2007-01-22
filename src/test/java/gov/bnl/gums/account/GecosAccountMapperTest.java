/*
 * GecosAccountMapperTest.java
 * JUnit based test
 *
 * Created on May 10, 2005, 1:35 PM
 */

package gov.bnl.gums.account;

import gov.bnl.gums.configuration.Configuration;
import junit.framework.*;
import java.util.*;
import org.apache.commons.logging.*;

/**
 *
 * @author carcassi
 */
public class GecosAccountMapperTest extends TestCase {
    GecosAccountMapper mapper;
    
    public GecosAccountMapperTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
         mapper = new GecosAccountMapperImpl(new Configuration(), "accountMapper");
    }

    protected void tearDown() throws Exception {
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(GecosAccountMapperTest.class);
        
        return suite;
    }
    
    public void testMapUser() {
        assertEquals("carcassi", mapper.mapUser("/DC=org/DC=doegrids/OU=People/CN=Gabriele Carcassi"));
        assertEquals(null, mapper.mapUser("/DC=org/DC=doegrids/OU=People/CN=Evil Person"));
    }

    public void testParsing() {
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

    private class GecosAccountMapperImpl extends GecosAccountMapper {

        GecosAccountMapperImpl() {
            super();
        }

        GecosAccountMapperImpl(Configuration configuration, String name) {
            super(configuration, name);
        }
        
        protected gov.bnl.gums.account.GecosMap createMap() {
            GecosMap map = new GecosMap();
            map.addEntry("carcassi", "Gabriele Carcassi");
            return map;
        }

        protected java.lang.String mapName() {
            return "testMap";
        }
        
        public String getSummary(String bgColor) {
        	return null;
        }
        
        public Object clone() {
        	return null;
        }
    }

    
}
