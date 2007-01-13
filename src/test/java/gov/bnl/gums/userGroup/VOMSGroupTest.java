/*
 * VOMSGroupTest.java
 * JUnit based test
 *
 * Created on May 25, 2004, 11:41 AM
 */

package gov.bnl.gums.userGroup;

import gov.bnl.gums.persistence.MockPersistenceFactory;
import gov.bnl.gums.userGroup.*;
import java.util.*;
import junit.framework.*;

/**
 *
 * @author carcassi
 * @author jhover
 */
public class VOMSGroupTest extends TestCase {
    
	UserGroup group;
       
    public VOMSGroupTest(java.lang.String testName) {
        super(testName);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(VOMSGroupTest.class);
        return suite;
    }
    
    public void setUp() {
        VirtualOrganization vo = new VirtualOrganization();
        vo.setBaseUrl("https://vo.racf.bnl.gov:8443/edg-voms-admin");
        vo.setSslCertfile(System.getProperty("user.home") + "/etc/grid-security/hostcert.pem");
        vo.setSslKey(System.getProperty("user.home") + "/etc/grid-security/hostkey.pem");
        vo.setPersistenceFactory(new MockPersistenceFactory("star"));
        
        VOMSUserGroup vomsGroup = new VOMSUserGroup("group1");
        group = vomsGroup;
        vomsGroup.setRemainderUrl("/star/services/VOMSAdmin");
        vomsGroup.setVirtualOrganization(vo);
//        vomsGroup.setIgnoreFQAN(true);
        vomsGroup.setVirtualOrganization(vo);
    }

    public void testDummyTest() {
    	assertTrue(true);
    	assertFalse(false);    	
    }
    
/*    
    
    public void testRole() {
        VOMSGroup vomsGroup = (VOMSGroup) group;
        vomsGroup.setPersistence(new MockPersistenceFactory(), "test");
        vomsGroup.setUrl("https://vo.racf.bnl.gov:8443/edg-voms-admin/atlas/services/VOMSAdmin");
        vomsGroup.setVoGroup("/atlas/usatlas");
        vomsGroup.setVoRole("production");
        vomsGroup.setIgnoreFQAN(false);
        group.updateMembers();
        assertTrue(group.isInGroup(new GridUser("/DC=org/DC=doegrids/OU=People/CN=GUMS Mock Test Account One", "/atlas/usatlas/Role=production")));
        assertFalse(group.isInGroup(new GridUser("/DC=org/DC=doegrids/OU=People/CN=GUMS Mock Test Account One", null)));
        assertFalse(group.isInGroup(new GridUser("/DC=org/DC=doegrids/OU=People/CN=GUMS Mock Test Account Two", "/atlas/usatlas/Role=production")));
    }
    
    public void testRole2() {
        VOMSGroup vomsGroup = (VOMSGroup) group;
        vomsGroup.setPersistence(new MockPersistenceFactory(), "test");
        vomsGroup.setUrl("https://vo.racf.bnl.gov:8443/edg-voms-admin/atlas/services/VOMSAdmin");
        vomsGroup.setVoGroup("/atlas/usatlas");
        vomsGroup.setVoRole("production");
        vomsGroup.setIgnoreFQAN(true);
        group.updateMembers();
        assertTrue(group.isInGroup(new GridUser("/DC=org/DC=doegrids/OU=People/CN=GUMS Mock Test Account One", "/atlas/usatlas/Role=production")));
        assertTrue(group.isInGroup(new GridUser("/DC=org/DC=doegrids/OU=People/CN=GUMS Mock Test Account One", null)));
        assertFalse(group.isInGroup(new GridUser("/DC=org/DC=doegrids/OU=People/CN=GUMS Mock Test Account Two", "/atlas/usatlas/Role=production")));
    }

    public void testUpdateMembers() {
        group.updateMembers();
        assertTrue(group.isInGroup(new GridUser("/DC=org/DC=doegrids/OU=People/CN=GUMS Mock Test Account One", null)));
       
    }
    
    public void testGetMemberList() {
        group.updateMembers();
        List members = group.getMemberList();
        assertTrue(members.size() > 0);
        Iterator iter = members.iterator();
        while (iter.hasNext()) {
            GridUser user = (GridUser) iter.next();
            assertTrue(group.isInGroup(user));
        }
    }
    
    public void testString() {
        assertEquals("VOMSGroup: https://vo.racf.bnl.gov:8443/edg-voms-admin/star/services/VOMSAdmin -" +
        " voGroup='null' - voRole='null' -" +
        " sslCAFiles='null' sslCertfile='" + System.getProperty("user.home") + "/etc/grid-security/hostcert.pem' " +
        "sslKey='" + System.getProperty("user.home") + "/etc/grid-security/hostkey.pem' sslKeyPasswd=[not set]",
        group.toString());
    }
    
   
    public void testSetMatchFQAN() {
        VOMSGroup vomsGroup = (VOMSGroup) group;
        vomsGroup.setMatchFQAN("exact");
        assertEquals("exact", vomsGroup.getMatchFQAN());
        vomsGroup.setMatchFQAN("vo");
        vomsGroup.setMatchFQAN("group");
        vomsGroup.setMatchFQAN("ignore");
        try {
            vomsGroup.setMatchFQAN("exart");
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
        
    }

    public void testUpdateMembersChangingGroup() {
        VOMSGroup vomsGroup = (VOMSGroup) group;
        vomsGroup.setIgnoreFQAN(false);
        group.updateMembers();
        assertTrue(group.isInGroup(new GridUser("/DC=org/DC=doegrids/OU=People/CN=GUMS Mock Test Account One", null)));
        assertTrue(group.isInGroup(new GridUser("/DC=org/DC=doegrids/OU=People/CN=Jerome LAURET 694693", null)));
        vomsGroup.setVoGroup("/star");
        group.updateMembers();
        assertTrue(group.isInGroup(new GridUser("/DC=org/DC=doegrids/OU=People/CN=GUMS Mock Test Account One", "/star")));
        assertTrue(group.isInGroup(new GridUser("/DC=org/DC=doegrids/OU=People/CN=Jerome LAURET 694693", "/star")));
        assertFalse(group.isInGroup(new GridUser("/DC=org/DC=doegrids/OU=People/CN=GUMS Mock Test Account One", null)));
        assertFalse(group.isInGroup(new GridUser("/DC=org/DC=doegrids/OU=People/CN=Jerome LAURET 694693", null)));
    }

    public void testMatchFQAN() {
        VOMSGroup vomsGroup = (VOMSGroup) group;
        vomsGroup.setPersistence(new MockPersistenceFactory(), "usatlas");
        vomsGroup.setUrl("https://vo.racf.bnl.gov:8443/edg-voms-admin/atlas/services/VOMSAdmin");
        vomsGroup.setVoGroup("/atlas/usatlas");
        group.updateMembers();
        vomsGroup.setMatchFQAN("ignore");
        vomsGroup.setAcceptProxyWithoutFQAN(true);
        assertTrue(group.isInGroup(new GridUser("/DC=org/DC=doegrids/OU=People/CN=GUMS Mock Test Account One", null)));
        assertTrue(group.isInGroup(new GridUser("/DC=org/DC=doegrids/OU=People/CN=GUMS Mock Test Account One", "/atlas")));
        assertTrue(group.isInGroup(new GridUser("/DC=org/DC=doegrids/OU=People/CN=GUMS Mock Test Account One", "/atlas/usatlas")));
        assertTrue(group.isInGroup(new GridUser("/DC=org/DC=doegrids/OU=People/CN=GUMS Mock Test Account One", "/atlas/Role=production")));
        assertTrue(group.isInGroup(new GridUser("/DC=org/DC=doegrids/OU=People/CN=GUMS Mock Test Account One", "/atlas/usatlas/Role=production")));
        assertTrue(group.isInGroup(new GridUser("/DC=org/DC=doegrids/OU=People/CN=GUMS Mock Test Account One", "/atlas/usatlas/test/Role=production")));
        vomsGroup.setAcceptProxyWithoutFQAN(false);
        vomsGroup.setMatchFQAN("vo");
        assertFalse(group.isInGroup(new GridUser("/DC=org/DC=doegrids/OU=People/CN=GUMS Mock Test Account One", null)));
        assertTrue(group.isInGroup(new GridUser("/DC=org/DC=doegrids/OU=People/CN=GUMS Mock Test Account One", "/atlas")));
        assertTrue(group.isInGroup(new GridUser("/DC=org/DC=doegrids/OU=People/CN=GUMS Mock Test Account One", "/atlas/usatlas")));
        assertTrue(group.isInGroup(new GridUser("/DC=org/DC=doegrids/OU=People/CN=GUMS Mock Test Account One", "/atlas/Role=production")));
        assertTrue(group.isInGroup(new GridUser("/DC=org/DC=doegrids/OU=People/CN=GUMS Mock Test Account One", "/atlas/usatlas/Role=production")));
        assertTrue(group.isInGroup(new GridUser("/DC=org/DC=doegrids/OU=People/CN=GUMS Mock Test Account One", "/atlas/usatlas/test/Role=production")));
        vomsGroup.setAcceptProxyWithoutFQAN(true);
        vomsGroup.setMatchFQAN("vo");
        assertTrue(group.isInGroup(new GridUser("/DC=org/DC=doegrids/OU=People/CN=GUMS Mock Test Account One", null)));
        assertTrue(group.isInGroup(new GridUser("/DC=org/DC=doegrids/OU=People/CN=GUMS Mock Test Account One", "/atlas")));
        assertTrue(group.isInGroup(new GridUser("/DC=org/DC=doegrids/OU=People/CN=GUMS Mock Test Account One", "/atlas/usatlas")));
        assertTrue(group.isInGroup(new GridUser("/DC=org/DC=doegrids/OU=People/CN=GUMS Mock Test Account One", "/atlas/Role=production")));
        assertTrue(group.isInGroup(new GridUser("/DC=org/DC=doegrids/OU=People/CN=GUMS Mock Test Account One", "/atlas/usatlas/Role=production")));
        assertTrue(group.isInGroup(new GridUser("/DC=org/DC=doegrids/OU=People/CN=GUMS Mock Test Account One", "/atlas/usatlas/test/Role=production")));
        vomsGroup.setAcceptProxyWithoutFQAN(false);
        vomsGroup.setMatchFQAN("group");
        assertFalse(group.isInGroup(new GridUser("/DC=org/DC=doegrids/OU=People/CN=GUMS Mock Test Account One", null)));
        assertFalse(group.isInGroup(new GridUser("/DC=org/DC=doegrids/OU=People/CN=GUMS Mock Test Account One", "/atlas")));
        assertTrue(group.isInGroup(new GridUser("/DC=org/DC=doegrids/OU=People/CN=GUMS Mock Test Account One", "/atlas/usatlas")));
        assertFalse(group.isInGroup(new GridUser("/DC=org/DC=doegrids/OU=People/CN=GUMS Mock Test Account One", "/atlas/Role=production")));
        assertTrue(group.isInGroup(new GridUser("/DC=org/DC=doegrids/OU=People/CN=GUMS Mock Test Account One", "/atlas/usatlas/Role=production")));
        assertTrue(group.isInGroup(new GridUser("/DC=org/DC=doegrids/OU=People/CN=GUMS Mock Test Account One", "/atlas/usatlas/test/Role=production")));
        vomsGroup.setMatchFQAN("exact");
        assertFalse(group.isInGroup(new GridUser("/DC=org/DC=doegrids/OU=People/CN=GUMS Mock Test Account One", null)));
        assertFalse(group.isInGroup(new GridUser("/DC=org/DC=doegrids/OU=People/CN=GUMS Mock Test Account One", "/atlas")));
        assertTrue(group.isInGroup(new GridUser("/DC=org/DC=doegrids/OU=People/CN=GUMS Mock Test Account One", "/atlas/usatlas")));
        assertFalse(group.isInGroup(new GridUser("/DC=org/DC=doegrids/OU=People/CN=GUMS Mock Test Account One", "/atlas/Role=production")));
        assertFalse(group.isInGroup(new GridUser("/DC=org/DC=doegrids/OU=People/CN=GUMS Mock Test Account One", "/atlas/usatlas/Role=production")));
        assertFalse(group.isInGroup(new GridUser("/DC=org/DC=doegrids/OU=People/CN=GUMS Mock Test Account One", "/atlas/usatlas/test/Role=production")));
        vomsGroup.setMatchFQAN("exact");
        vomsGroup.setAcceptProxyWithoutFQAN(true);
        assertTrue(group.isInGroup(new GridUser("/DC=org/DC=doegrids/OU=People/CN=GUMS Mock Test Account One", null)));
        assertFalse(group.isInGroup(new GridUser("/DC=org/DC=doegrids/OU=People/CN=GUMS Mock Test Account One", "/atlas")));
        assertTrue(group.isInGroup(new GridUser("/DC=org/DC=doegrids/OU=People/CN=GUMS Mock Test Account One", "/atlas/usatlas")));
        assertFalse(group.isInGroup(new GridUser("/DC=org/DC=doegrids/OU=People/CN=GUMS Mock Test Account One", "/atlas/Role=production")));
        assertFalse(group.isInGroup(new GridUser("/DC=org/DC=doegrids/OU=People/CN=GUMS Mock Test Account One", "/atlas/usatlas/Role=production")));
        assertFalse(group.isInGroup(new GridUser("/DC=org/DC=doegrids/OU=People/CN=GUMS Mock Test Account One", "/atlas/usatlas/test/Role=production")));
    }
    
    */
}
