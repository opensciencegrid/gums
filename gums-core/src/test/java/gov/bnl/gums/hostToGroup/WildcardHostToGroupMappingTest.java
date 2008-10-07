/*
 * WilldcardHostGroupTest.java
 * JUnit based test
 *
 * Created on May 27, 2004, 3:41 PM
 */

package gov.bnl.gums.hostToGroup;

import gov.bnl.gums.configuration.Configuration;
import gov.bnl.gums.groupToAccount.GroupToAccountMapping;

import java.util.*;
import junit.framework.*;

/**
 *
 * @author carcassi
 */
public class WildcardHostToGroupMappingTest extends TestCase {
    
    public WildcardHostToGroupMappingTest(java.lang.String testName) {
        super(testName);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(WildcardHostToGroupMappingTest.class);
        return suite;
    }
    
    public void testRetrieveGroupMappers() {
    	Configuration configuration = new Configuration();
        WildcardHostToGroupMapping wMapping = new WildcardHostToGroupMapping(configuration);
        wMapping.addGroupToAccountMapping(new GroupToAccountMapping(configuration, "group1").getName());
        wMapping.setWildcard("star*.bnl.gov");
        assertTrue(wMapping.isInGroup("stargrid01.bnl.gov"));
        assertTrue(wMapping.isInGroup("/DC=org/DC=griddev/OU=Services/CN=stargrid01.bnl.gov"));
        assertTrue(wMapping.isInGroup("stargrid02.bnl.gov"));
        assertTrue(wMapping.isInGroup("/DC=org/DC=griddev/OU=Services/CN=stargrid02.bnl.gov"));
        assertFalse(wMapping.isInGroup("stargrid01.gov"));
        assertFalse(wMapping.isInGroup("/DC=org/DC=griddev/OU=Services/CN=stargrid01.gov"));
        assertFalse(wMapping.isInGroup("stargrid01.rhic.bnl.gov"));
        assertFalse(wMapping.isInGroup("/DC=org/DC=griddev/OU=Services/CN=stargrid01.rhic.bnl.gov"));
        assertFalse(wMapping.isInGroup("atlasgrid01.bnl.gov"));
        assertFalse(wMapping.isInGroup("/DC=org/DC=griddev/OU=Services/CN=atlasgrid01.bnl.gov"));
        wMapping.setWildcard("*.usatlas.bnl.gov");
        assertTrue(wMapping.isInGroup("gremlin.usatlas.bnl.gov"));
        assertTrue(wMapping.isInGroup("/DC=org/DC=griddev/OU=Services/CN=gremlin.usatlas.bnl.gov"));
        assertTrue(wMapping.isInGroup("atlasgrid01.usatlas.bnl.gov"));
        assertTrue(wMapping.isInGroup("/DC=org/DC=griddev/OU=Services/CN=atlasgrid01.usatlas.bnl.gov"));
        assertFalse(wMapping.isInGroup("stargrid01.rhic.bnl.gov"));
        assertFalse(wMapping.isInGroup("/DC=org/DC=griddev/OU=Services/CN=stargrid01.rhic.bnl.gov"));
        assertFalse(wMapping.isInGroup("atlasgrid01.test.usatlas.bnl.gov"));
        assertFalse(wMapping.isInGroup("/DC=org/DC=griddev/OU=Services/CN=atlasgrid01.test.usatlas.bnl.gov"));
        assertFalse(wMapping.isInGroup("atlasgrid01.bnl.gov"));
        assertFalse(wMapping.isInGroup("/DC=org/DC=griddev/OU=Services/CN=atlasgrid01.bnl.gov"));
        wMapping.setWildcard("aftp*.usatlas.bnl.gov,spider.usatlas.bnl.gov");
        assertTrue(wMapping.isInGroup("spider.usatlas.bnl.gov"));
        assertTrue(wMapping.isInGroup("/DC=org/DC=griddev/OU=Services/CN=spider.usatlas.bnl.gov"));
        assertTrue(wMapping.isInGroup("aftpexp01.usatlas.bnl.gov"));
        assertTrue(wMapping.isInGroup("/DC=org/DC=griddev/OU=Services/CN=aftpexp01.usatlas.bnl.gov"));
        assertFalse(wMapping.isInGroup("stargrid01.rhic.bnl.gov"));
        assertFalse(wMapping.isInGroup("/DC=org/DC=griddev/OU=Services/CN=stargrid01.rhic.bnl.gov"));
        assertFalse(wMapping.isInGroup("atlasgrid01.usatlas.bnl.gov"));
        assertFalse(wMapping.isInGroup("/DC=org/DC=griddev/OU=Services/CN=atlasgrid01.usatlas.bnl.gov"));
        assertFalse(wMapping.isInGroup("atlasgrid01.bnl.gov"));
        assertFalse(wMapping.isInGroup("/DC=org/DC=griddev/OU=Services/CN=atlasgrid01.bnl.gov"));
    }
    
    
}
