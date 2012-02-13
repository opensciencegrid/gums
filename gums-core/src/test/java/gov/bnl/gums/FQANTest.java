/*
 * FQANTest.java
 * JUnit based test
 *
 * Created on September 10, 2004, 11:55 AM
 */

package gov.bnl.gums;

import junit.framework.*;

/**
 *
 * @author carcassi
 */
public class FQANTest extends TestCase {
    
    FQAN fqan;
    
    public FQANTest(java.lang.String testName) {
        super(testName);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(FQANTest.class);
        return suite;
    }
    
    public void testSetFQAN() {
        String voFqan = "/star/users/Role=admin/Capability=important";
        fqan = new FQAN(voFqan);
        assertEquals(voFqan, fqan.getFqan());
        assertEquals("star", fqan.getVo());
        assertEquals("/users", fqan.getGroup());
        assertEquals("admin", fqan.getRole());
        assertEquals("important", fqan.getCapability());
    }
    
    public void testSetFQAN2() {
        String voFqan = "/rhic.star/Role=analysis";
        fqan = new FQAN(voFqan);
        assertEquals(voFqan, fqan.getFqan());
        assertEquals("rhic.star", fqan.getVo());
        assertEquals(null, fqan.getGroup());
        assertEquals("analysis", fqan.getRole());
        assertEquals(null, fqan.getCapability());
    }
    
    public void testSetFQAN3() {
        String voFqan = "/star/analysis/HBT";
        fqan = new FQAN(voFqan);
        assertEquals(voFqan, fqan.getFqan());
        assertEquals("star", fqan.getVo());
        assertEquals("/analysis/HBT", fqan.getGroup());
        assertEquals(null, fqan.getRole());
        assertEquals(null, fqan.getCapability());
    }
    
    public void testSetFQAN4() {
        String voFqan = "/star/Capability=test";
        fqan = new FQAN(voFqan);
        assertEquals(voFqan, fqan.getFqan());
        assertEquals("star", fqan.getVo());
        assertEquals(null, fqan.getGroup());
        assertEquals(null, fqan.getRole());
        assertEquals("test", fqan.getCapability());
    }
    
    public void testSetFQAN5() {
        String voFqan = "/star/Role=analysis/Capability=important";
        fqan = new FQAN(voFqan);
        assertEquals(voFqan, fqan.getFqan());
        assertEquals("star", fqan.getVo());
        assertEquals(null, fqan.getGroup());
        assertEquals("analysis", fqan.getRole());
        assertEquals("important", fqan.getCapability());
    }
    
    public void testSetFQAN6() {
        String voFqan = "/star/production/Role=NULL/Capability=NULL";
        fqan = new FQAN(voFqan);
        assertEquals("/star/production", fqan.getFqan());
        assertEquals("star", fqan.getVo());
        assertEquals("/production", fqan.getGroup());
        assertEquals(null, fqan.getRole());
        assertEquals(null, fqan.getCapability());
    }
    
    public void testSetFQAN7() {
        String voFqan = "/star/production/Role=null/Capability=null";
        fqan = new FQAN(voFqan);
        assertEquals("/star/production", fqan.getFqan());
        assertEquals("star", fqan.getVo());
        assertEquals("/production", fqan.getGroup());
        assertEquals(null, fqan.getRole());
        assertEquals(null, fqan.getCapability());
    }
    
    public void testSetFQAN8() {
        String voFqan = "/UPPERCASE/voGroup";
        fqan = new FQAN(voFqan);
        assertEquals("UPPERCASE", fqan.getVo());
        assertEquals("/voGroup", fqan.getGroup());
        assertEquals(null, fqan.getRole());
        assertEquals(null, fqan.getCapability());
    }
    
    public void testSetFQAN9() {
        String voFqan = "/my.vo-with_characters/sure.sure/Role=ImPorTant_role/Capability=Mah...";
        fqan = new FQAN(voFqan);
        assertEquals(voFqan, fqan.getFqan());
        assertEquals("my.vo-with_characters", fqan.getVo());
        assertEquals("/sure.sure", fqan.getGroup());
        assertEquals("ImPorTant_role", fqan.getRole());
        assertEquals("Mah...", fqan.getCapability());
    }
    
    public void testEquals() {
        String voFqan = "/star/production/Role=null/Capability=null";
        String voFqan2 = "/star/production";
        FQAN fqan = new FQAN(voFqan);
        FQAN fqan2 = new FQAN(voFqan2);
        assertEquals(fqan, fqan2);
    }
    
    public void testSetFQANError() {
        String voFqan = "/Role=analysis/Capability=important";
        try {
            fqan = new FQAN(voFqan);
        } catch (Exception e) {
            return;
        }
        fail("Incorrect FQAN syntax should throw an exception");
    }
    
    public void testSetFQANError2() {
        String voFqan = "/star/Capability=important/Role=analysis";
        try {
            fqan = new FQAN(voFqan);
        } catch (Exception e) {
            return;
        }
        fail("Incorrect FQAN syntax should throw an exception");
    }
    
    public void testSetFQANError3() {
        String voFqan = "/star/Rule=analysis";
        try {
            fqan = new FQAN(voFqan);
        } catch (Exception e) {
            return;
        }
        fail("Incorrect FQAN syntax should throw an exception");
    }
    
    public void testSetFragment() {
        fqan = new FQAN("star", null, null, null);
        assertEquals("/star", fqan.getFqan());
        fqan = new FQAN("star", null, "admin", null);
        assertEquals("/star/Role=admin", fqan.getFqan());
        fqan = new FQAN("star", "/analysis/hipt", "admin", null);
        assertEquals("/star/analysis/hipt/Role=admin", fqan.getFqan());
        fqan = new FQAN("star", "/analysis/hipt", "admin", "important");
        assertEquals("/star/analysis/hipt/Role=admin/Capability=important", fqan.getFqan());
        fqan = new FQAN("star", "/analysis/hipt", null, "important");
        assertEquals("/star/analysis/hipt/Capability=important", fqan.getFqan());
        fqan = new FQAN("star", "/analysis/hipt", null, null);
        assertEquals("/star/analysis/hipt", fqan.getFqan());
        fqan = new FQAN("star", null, null, "important");
        assertEquals("/star/Capability=important", fqan.getFqan());
    }
    
    public void testNoVO() {
        try {
            fqan = new FQAN(null, null, null, "important");
        } catch (Exception e) {
            return;
        }
        fail("FQAN with null VO shouldn't exist");
    }
    
}
