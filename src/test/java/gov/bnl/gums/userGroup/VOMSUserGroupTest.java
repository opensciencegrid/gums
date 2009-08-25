/*
 * VOMSGroupTest.java
 * JUnit based test
 *
 * Created on May 25, 2004, 11:41 AM
 */

package gov.bnl.gums.userGroup;

import gov.bnl.gums.GridUser;
import gov.bnl.gums.configuration.Configuration;

import java.util.*;

import junit.framework.*;

/**
 *
 * @author carcassi
 * @author jhover
 */
public class VOMSUserGroupTest extends TestCase {
	UserGroup userGroup;
	Configuration configuration = new Configuration();
       
    public VOMSUserGroupTest(java.lang.String testName) {
        super(testName);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(VOMSUserGroupTest.class);
        return suite;
    }
    
    protected static Properties readVomsProperties() {
        PropertyResourceBundle prop = (PropertyResourceBundle) ResourceBundle.getBundle("voms");
        Properties prop2 = new Properties();
        Enumeration<String> keys = prop.getKeys();
        while (keys.hasMoreElements()) {
            String key = (String) keys.nextElement();
            prop2.setProperty(key, prop.getString(key));
        }
        return prop2;
    }
    
    public void setUp() {
    	Properties properties = readVomsProperties();
        VOMSUserGroup vomsUserGroup = new VOMSUserGroup(configuration, "group1");
        userGroup = vomsUserGroup;
        vomsUserGroup.setVomsServerUrls(properties.getProperty("voms.connection.vomsServerUrls"));
        vomsUserGroup.setVoGroup("/griddev/subgriddev");
        vomsUserGroup.setRole("griddevrole");
        configuration.setSslCertfile(properties.getProperty("voms.security.sslCertfile"));
        configuration.setSslKey(properties.getProperty("voms.security.sslKey"));
    	userGroup.updateMembers();
    }

    public void testDummyTest() {
    	assertTrue(true);
    	assertFalse(false);    	
    }   
 
    public void testGetMemberList() {
        Set<GridUser> members = userGroup.getMembers();
        assertTrue(members.size() > 0);
        Iterator<GridUser> iter = members.iterator();
        while (iter.hasNext()) {
            GridUser user = (GridUser) iter.next();
            assertTrue(userGroup.isMember(user));
            if (user.getDn().equals("/DC=org/DC=griddev/OU=People/CN=Jane Doe 12345"))
            	assertEquals("jdoe@griddev.org", user.getEmail());
            else if (user.getDn().equals("/DC=org/DC=griddev/OU=People/CN=John Smith"))
            	assertEquals("jsmith@griddev.org", user.getEmail());
        }
    }
    
    public void testSetMatchFQAN() {
        VOMSUserGroup vomsUserGroup = (VOMSUserGroup) userGroup;
        vomsUserGroup.setMatchFQAN("exact");
        assertEquals("exact", vomsUserGroup.getMatchFQAN());
        vomsUserGroup.setMatchFQAN("vo");
        assertEquals("vo", vomsUserGroup.getMatchFQAN());
        vomsUserGroup.setMatchFQAN("group");
        assertEquals("vogroup", vomsUserGroup.getMatchFQAN());
        vomsUserGroup.setMatchFQAN("ignore");
        assertEquals("ignore", vomsUserGroup.getMatchFQAN());
    }
    
    public void testMatchFQAN() {
        VOMSUserGroup VOMSUserGroup = (VOMSUserGroup) userGroup;
        VOMSUserGroup.setMatchFQAN("ignore");
        VOMSUserGroup.setAcceptProxyWithoutFQAN(true);
        assertTrue(userGroup.isMember(new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", null)));
        assertTrue(userGroup.isMember(new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", "/griddev")));
        assertTrue(userGroup.isMember(new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", "/griddev/subgriddev")));
        assertTrue(userGroup.isMember(new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", "/griddev/Role=production")));
        assertTrue(userGroup.isMember(new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", "/griddev/subgriddev/Role=griddevrole")));
        assertTrue(userGroup.isMember(new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", "/griddev/subgriddev/Role=otherrole")));
        VOMSUserGroup.setAcceptProxyWithoutFQAN(false);
        VOMSUserGroup.setMatchFQAN("vo");
        assertFalse(userGroup.isMember(new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", null)));
        assertTrue(userGroup.isMember(new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", "/griddev")));
        assertTrue(userGroup.isMember(new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", "/griddev/subgriddev")));
        assertTrue(userGroup.isMember(new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", "/griddev/Role=production")));
        assertTrue(userGroup.isMember(new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", "/griddev/subgriddev/Role=griddevrole")));
        assertTrue(userGroup.isMember(new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", "/griddev/subgriddev/Role=otherrole")));
        VOMSUserGroup.setAcceptProxyWithoutFQAN(true);
        VOMSUserGroup.setMatchFQAN("vo");
        assertTrue(userGroup.isMember(new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", null)));
        assertTrue(userGroup.isMember(new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", "/griddev")));
        assertTrue(userGroup.isMember(new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", "/griddev/subgriddev")));
        assertTrue(userGroup.isMember(new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", "/griddev/Role=production")));
        assertTrue(userGroup.isMember(new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", "/griddev/subgriddev/Role=griddevrole")));
        assertTrue(userGroup.isMember(new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", "/griddev/subgriddev/Role=otherrole")));
        VOMSUserGroup.setAcceptProxyWithoutFQAN(false);
        VOMSUserGroup.setMatchFQAN("group");
        assertFalse(userGroup.isMember(new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", null)));
        assertFalse(userGroup.isMember(new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", "/griddev")));
        assertTrue(userGroup.isMember(new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", "/griddev/subgriddev")));
        assertFalse(userGroup.isMember(new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", "/griddev/Role=production")));
        assertTrue(userGroup.isMember(new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", "/griddev/subgriddev/Role=griddevrole")));
        assertTrue(userGroup.isMember(new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", "/griddev/subgriddev/Role=otherrole")));
        VOMSUserGroup.setMatchFQAN("exact");
        assertFalse(userGroup.isMember(new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", null)));
        assertFalse(userGroup.isMember(new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", "/griddev")));
        assertFalse(userGroup.isMember(new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", "/griddev/subgriddev")));
        assertFalse(userGroup.isMember(new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", "/griddev/Role=production")));
        assertTrue(userGroup.isMember(new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", "/griddev/subgriddev/Role=griddevrole")));
        assertFalse(userGroup.isMember(new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", "/griddev/subgriddev/Role=otherrole")));
        VOMSUserGroup.setAcceptProxyWithoutFQAN(true);
        VOMSUserGroup.setMatchFQAN("exact");
        assertTrue(userGroup.isMember(new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", null)));
        assertFalse(userGroup.isMember(new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", "/griddev")));
        assertFalse(userGroup.isMember(new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", "/griddev/subgriddev")));
        assertFalse(userGroup.isMember(new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", "/griddev/Role=production")));
        assertTrue(userGroup.isMember(new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", "/griddev/subgriddev/Role=griddevrole")));
        assertFalse(userGroup.isMember(new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", "/griddev/subgriddev/Role=otherrole")));
    }

}
