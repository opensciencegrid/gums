/*
 * VOMSGroupTest.java
 * JUnit based test
 *
 * Created on May 25, 2004, 11:41 AM
 */

package gov.bnl.gums.userGroup;

import gov.bnl.gums.configuration.Configuration;
import gov.bnl.gums.persistence.MockPersistenceFactory;
import gov.bnl.gums.GridUser;

import java.util.*;
import javax.naming.*;
import javax.naming.directory.*;

import junit.framework.*;

/**
 *
 * @author carcassi
 */
public class LDAPGroupTest extends TestCase {
    
    UserGroup group;
    Configuration configuration = new Configuration();
    
    public LDAPGroupTest(java.lang.String testName) {
        super(testName);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(LDAPGroupTest.class);
        return suite;
    }
    
    public void setUp() {
        LDAPUserGroup ldapGroup = new LDAPUserGroup(configuration, "group1");
        group = ldapGroup;
        ldapGroup.setPersistenceFactory(new MockPersistenceFactory(configuration, "mock").getName());
        ldapGroup.setServer("grid-vo.nikhef.nl");
        ldapGroup.setQuery("ou=People,o=atlas,dc=eu-datagrid,dc=org");
    }
    
    public void testUpdateMembers() {
        group.updateMembers();
        assertTrue(group.isInGroup(new GridUser("/DC=org/DC=doegrids/OU=People/CN=Gabriele Carcassi 348273", null)));
        assertTrue(group.isInGroup(new GridUser("/DC=org/DC=doegrids/OU=People/CN=Wensheng Deng 90806", null)));
        assertTrue(group.isInGroup(new GridUser("/C=IT/O=INFN/OU=Personal Certificate/L=Roma 1/CN=Alessandro De Salvo", null)));
    }
    
    public void testUpdateMembers2() {
        ((LDAPUserGroup) group).setQuery("ou=usatlas,o=atlas,dc=eu-datagrid,dc=org");
        group.updateMembers();
        assertTrue(group.isInGroup(new GridUser("/DC=org/DC=doegrids/OU=People/CN=Gabriele Carcassi 348273", null)));
        assertTrue(group.isInGroup(new GridUser("/DC=org/DC=doegrids/OU=People/CN=Wensheng Deng 90806", null)));
        assertFalse(group.isInGroup(new GridUser("/C=IT/O=INFN/OU=Personal Certificate/L=Roma 1/CN=Alessandro De Salvo/Email=Alessandro.DeSalvo@roma1.infn.it", null)));
    }
    
    public void testUpdateMembers3() {
        ((LDAPUserGroup) group).setServer("lcg-vo.cern.ch");
        ((LDAPUserGroup) group).setQuery("ou=lcg1,o=dteam,dc=lcg,dc=org");
        group.updateMembers();
        assertTrue(group.isInGroup(new GridUser("/DC=org/DC=doegrids/OU=People/CN=Dantong Yu 127718", null)));
        assertFalse(group.isInGroup(new GridUser("/DC=org/DC=doegrids/OU=People/CN=Gabriele Carcassi 348273", null)));
    }
    
    public void testGetMemberList() {
        group.updateMembers();
        List members = group.getMemberList();
        assertTrue(members.size() > 0);
        Iterator iter = members.iterator();
        while (iter.hasNext()) {
            GridUser dn = (GridUser) iter.next();
            assertTrue(group.isInGroup(dn));
        }
    }
    
    public void testEquals() {
        MockPersistenceFactory factory = new MockPersistenceFactory(configuration, "test");
        LDAPUserGroup group1 = new LDAPUserGroup(configuration, "group1");
        LDAPUserGroup group2 = new LDAPUserGroup(configuration, "group2");
        assertEquals(group1, group2);
        group1.setPersistenceFactory(factory.getName());
        group2.setPersistenceFactory(factory.getName());
        assertEquals(group1, group2);
        group1.setServer("testServer");
        assertFalse(group1.equals(group2));
        group2.setServer("testServer");
        assertEquals(group1, group2);
        group1.setQuery("query1");
        group2.setQuery("query2");
        assertFalse(group1.equals(group2));
        group2.setQuery("query1");
        assertEquals(group1, group2);
    }
    
    public void testToString() {
        assertEquals("LDAPGroup: ldap://grid-vo.nikhef.nl/ou=People,o=atlas,dc=eu-datagrid,dc=org", group.toString());
    }
    
    public void testRetrieveMap() throws NamingException {
        java.util.Properties jndiProperties = new java.util.Properties();
        jndiProperties.put("java.naming.provider.url","ldap://lcg-vo.cern.ch");
        jndiProperties.put("java.naming.factory.initial","com.sun.jndi.ldap.LdapCtxFactory");
        javax.naming.directory.DirContext jndiCtx = new javax.naming.directory.InitialDirContext(jndiProperties);

        // The group has a member attribute with a list of people of the LDAP present in the VO Group
        DirContext ctx = (DirContext) jndiCtx.lookup("o=dteam,dc=lcg,dc=org");
        LDAPUserGroup group = new LDAPUserGroup(configuration, "group1");
        Map map = group.retrievePeopleMap(ctx);
        assertEquals("/DC=org/DC=doegrids/OU=People/CN=Dantong Yu 127718", map.get("cn=Dantong Yu 127718,ou=People,o=dteam,dc=lcg,dc=org"));
    }
    
    public static void main(String[] args) {
        TestResult res = new TestResult();
        suite().run(res);
    }
    
}
