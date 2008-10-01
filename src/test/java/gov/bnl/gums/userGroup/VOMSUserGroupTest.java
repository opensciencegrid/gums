/*
 * VOMSGroupTest.java
 * JUnit based test
 *
 * Created on May 25, 2004, 11:41 AM
 */

package gov.bnl.gums.userGroup;

import gov.bnl.gums.GridUser;
import gov.bnl.gums.configuration.Configuration;
import gov.bnl.gums.persistence.LDAPPersistenceFactory;
import gov.bnl.gums.persistence.MockPersistenceFactory;
import gov.bnl.gums.persistence.PersistenceFactory;

import java.util.*;

import org.apache.log4j.Logger;

import junit.framework.*;

/**
 *
 * @author carcassi
 * @author jhover
 */
public class VOMSUserGroupTest extends TestCase {
	VomsServer vomsServer;
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
    	Logger.getLogger(LDAPPersistenceFactory.class).trace("Retrieving VOMS properties from voms.properties in the classpath");
        PropertyResourceBundle prop = (PropertyResourceBundle) ResourceBundle.getBundle("voms");
        Properties prop2 = new Properties();
        Enumeration keys = prop.getKeys();
        while (keys.hasMoreElements()) {
            String key = (String) keys.nextElement();
            prop2.setProperty(key, prop.getString(key));
        }
        return prop2;
    }
    
    public void setUp() {
    	Properties properties = readVomsProperties();
        vomsServer = new VomsServer(configuration, "vo");
        vomsServer.setBaseUrl( properties.getProperty("voms.connection.baseUrl") );
        vomsServer.setSslCertfile( properties.getProperty("voms.security.sslCertfile") );
        vomsServer.setSslKey( properties.getProperty("voms.security.sslKey") );
        PersistenceFactory persistenceFactory = new MockPersistenceFactory(configuration, "test");
        configuration.addPersistenceFactory(persistenceFactory);
        vomsServer.setPersistenceFactory(persistenceFactory.getName());
        configuration.addVomsServer(vomsServer);
        
        VOMSUserGroup vomsUserGroup = new VOMSUserGroup(configuration, "group1");
        userGroup = vomsUserGroup;
        vomsUserGroup.setRemainderUrl("");
        vomsUserGroup.setVomsServer(vomsServer.getName());
        vomsUserGroup.setVoGroup("/griddev/subgriddev");
        vomsUserGroup.setRole("griddevrole");
        configuration.addUserGroup(userGroup);
    }

    public void testDummyTest() {
    	assertTrue(true);
    	assertFalse(false);    	
    }   
 
    public void testGetMemberList() {
        userGroup.updateMembers();
        List members = userGroup.getMemberList();
        assertTrue(members.size() > 0);
        Iterator iter = members.iterator();
        while (iter.hasNext()) {
            GridUser user = (GridUser) iter.next();
            assertTrue(userGroup.isInGroup(user));
            if (user.getCertificateDN().equals("/DC=org/DC=griddev/OU=People/CN=Jane Doe 12345"))
            	assertEquals("jdoe@griddev.org", user.getEmail());
            else if (user.getCertificateDN().equals("/DC=org/DC=griddev/OU=People/CN=John Smith"))
            	assertEquals("jsmith@griddev.org", user.getEmail());
        }
    }
    
    public void testSetMatchFQAN() {
        VOMSUserGroup VOMSUserGroup = (VOMSUserGroup) userGroup;
        VOMSUserGroup.setMatchFQAN("exact");
        assertEquals("exact", VOMSUserGroup.getMatchFQAN());
        VOMSUserGroup.setMatchFQAN("vo");
        VOMSUserGroup.setMatchFQAN("group");
        VOMSUserGroup.setMatchFQAN("ignore");
    }
    
    public void testMatchFQAN() {
        VOMSUserGroup VOMSUserGroup = (VOMSUserGroup) userGroup;
        VOMSUserGroup.setMatchFQAN("ignore");
        VOMSUserGroup.setAcceptProxyWithoutFQAN(true);
        assertTrue(userGroup.isInGroup(new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", null)));
        assertTrue(userGroup.isInGroup(new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", "/griddev")));
        assertTrue(userGroup.isInGroup(new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", "/griddev/subgriddev")));
        assertTrue(userGroup.isInGroup(new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", "/griddev/Role=production")));
        assertTrue(userGroup.isInGroup(new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", "/griddev/subgriddev/Role=griddevrole")));
        assertTrue(userGroup.isInGroup(new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", "/griddev/subgriddev/Role=otherrole")));
        VOMSUserGroup.setAcceptProxyWithoutFQAN(false);
        VOMSUserGroup.setMatchFQAN("vo");
        assertFalse(userGroup.isInGroup(new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", null)));
        assertTrue(userGroup.isInGroup(new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", "/griddev")));
        assertTrue(userGroup.isInGroup(new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", "/griddev/subgriddev")));
        assertTrue(userGroup.isInGroup(new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", "/griddev/Role=production")));
        assertTrue(userGroup.isInGroup(new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", "/griddev/subgriddev/Role=griddevrole")));
        assertTrue(userGroup.isInGroup(new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", "/griddev/subgriddev/Role=otherrole")));
        VOMSUserGroup.setAcceptProxyWithoutFQAN(true);
        VOMSUserGroup.setMatchFQAN("vo");
        assertTrue(userGroup.isInGroup(new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", null)));
        assertTrue(userGroup.isInGroup(new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", "/griddev")));
        assertTrue(userGroup.isInGroup(new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", "/griddev/subgriddev")));
        assertTrue(userGroup.isInGroup(new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", "/griddev/Role=production")));
        assertTrue(userGroup.isInGroup(new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", "/griddev/subgriddev/Role=griddevrole")));
        assertTrue(userGroup.isInGroup(new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", "/griddev/subgriddev/Role=otherrole")));
        VOMSUserGroup.setAcceptProxyWithoutFQAN(false);
        VOMSUserGroup.setMatchFQAN("group");
        assertFalse(userGroup.isInGroup(new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", null)));
        assertFalse(userGroup.isInGroup(new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", "/griddev")));
        assertTrue(userGroup.isInGroup(new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", "/griddev/subgriddev")));
        assertFalse(userGroup.isInGroup(new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", "/griddev/Role=production")));
        assertTrue(userGroup.isInGroup(new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", "/griddev/subgriddev/Role=griddevrole")));
        assertTrue(userGroup.isInGroup(new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", "/griddev/subgriddev/Role=otherrole")));
        VOMSUserGroup.setMatchFQAN("exact");
        assertFalse(userGroup.isInGroup(new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", null)));
        assertFalse(userGroup.isInGroup(new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", "/griddev")));
        assertFalse(userGroup.isInGroup(new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", "/griddev/subgriddev")));
        assertFalse(userGroup.isInGroup(new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", "/griddev/Role=production")));
        assertTrue(userGroup.isInGroup(new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", "/griddev/subgriddev/Role=griddevrole")));
        assertFalse(userGroup.isInGroup(new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", "/griddev/subgriddev/Role=otherrole")));
        VOMSUserGroup.setAcceptProxyWithoutFQAN(true);
        VOMSUserGroup.setMatchFQAN("exact");
        assertTrue(userGroup.isInGroup(new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", null)));
        assertFalse(userGroup.isInGroup(new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", "/griddev")));
        assertFalse(userGroup.isInGroup(new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", "/griddev/subgriddev")));
        assertFalse(userGroup.isInGroup(new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", "/griddev/Role=production")));
        assertTrue(userGroup.isInGroup(new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", "/griddev/subgriddev/Role=griddevrole")));
        assertFalse(userGroup.isInGroup(new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", "/griddev/subgriddev/Role=otherrole")));
    }

}
