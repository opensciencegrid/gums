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
         mapper = new GecosAccountMapperImpl(new Configuration(), "myAccountMapper");
    }

    protected void tearDown() throws Exception {
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(GecosAccountMapperTest.class);
        
        return suite;
    }
    
    public void testMapUser() {
        assertEquals("jsmith", mapper.mapUser("/DC=org/DC=griddev/OU=People/CN=John Smith"));
        assertEquals(null, mapper.mapUser("/DC=org/DC=griddev/OU=People/CN=Evil Person"));
    }

    public void testParsing() {
        String dn = "/DC=org/DC=griddev/OU=People/CN=John Smith Jr.";
        String[] name = mapper.parseNameAndSurname(dn);
        assertEquals("John", name[0]);
        assertEquals("Smith", name[1]);
        name = mapper.parseNameAndSurname("/DC=org/DC=griddev/OU=People/CN=Jane Doe 12345");
        assertEquals("Jane", name[0]);
        assertEquals("Doe", name[1]);
        name = mapper.parseNameAndSurname("/DC=org/DC=griddev/OU=People/CN=John W. Smith Jr. 12345");
        assertEquals("John", name[0]);
        assertEquals("Smith", name[1]);
    }
    
    public void testCheckSurname() {
        assertTrue(mapper.checkSurname("Smith"));
        assertFalse(mapper.checkSurname("12345"));
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
            map.addEntry("jsmith", "John Smith");
            return map;
        }

        protected java.lang.String mapName() {
            return getName();
        }
        
        public String toString(String bgColor) {
        	return "";
        }
        
        public String toXML(){
        	return null;
        }
        
        public AccountMapper clone(Configuration configuration) {
        	return null;
        }
    }

    
}
