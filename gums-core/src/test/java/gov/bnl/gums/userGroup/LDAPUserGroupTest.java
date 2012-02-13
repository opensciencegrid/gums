/*
 * VOMSGroupTest.java
 * JUnit based test
 *
 * Created on May 25, 2004, 11:41 AM
 */

package gov.bnl.gums.userGroup;

import gov.bnl.gums.configuration.Configuration;
import gov.bnl.gums.persistence.LDAPPersistenceFactory;
import gov.bnl.gums.persistence.LDAPPersistenceFactoryTest;
import gov.bnl.gums.persistence.MockPersistenceFactory;
import gov.bnl.gums.persistence.PersistenceFactory;
import gov.bnl.gums.GridUser;

import java.util.*;
import javax.naming.*;
import javax.naming.directory.*;

import junit.framework.*;

/**
 *
 * @author carcassi
 */
public class LDAPUserGroupTest extends TestCase {

	UserGroup group;
	UserGroup group2;
	Configuration configuration = new Configuration();
	String server;
	String peopleTree;
	String principal;

	public LDAPUserGroupTest(java.lang.String testName) {
		super(testName);
	}

	public static Test suite() {
		TestSuite suite = new TestSuite(LDAPUserGroupTest.class);
		return suite;
	}

	public void setUp() {
		LDAPUserGroup ldapGroup = new LDAPUserGroup(configuration, "group1");
		group = ldapGroup;
		configuration.addUserGroup(group);
		PersistenceFactory persistenceFactory = new MockPersistenceFactory(configuration, "mock");
		ldapGroup.setPersistenceFactory(persistenceFactory.getName());
		configuration.addPersistenceFactory(persistenceFactory);

		Properties properties = LDAPPersistenceFactoryTest.readLdapProperties();
		String url = properties.getProperty("java.naming.provider.url");
		server = url.substring(url.indexOf("//")+2).split("/")[0].split(":")[0];
		principal = url.substring(url.indexOf("//")+2).split("/")[1];
		ldapGroup.setServer(server);
		peopleTree = "ou=People," + principal;
		//ldapGroup.setQuery(peopleTree);
		ldapGroup.setPeopleTree(peopleTree);
		ldapGroup.setGroupTree("cn=griddevGroup,ou=Group,dc=griddev,dc=org");
	}

	public void testUpdateMembers() {
		group.updateMembers();
		assertTrue(group.isInGroup(new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", null)));
		assertTrue(group.isInGroup(new GridUser("/DC=org/DC=griddev/OU=People/CN=Jane Doe 12345", null)));
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

	public void testRetrieveMap() throws NamingException {
		java.util.Properties jndiProperties = new java.util.Properties();
		jndiProperties.put("java.naming.provider.url","ldap://"+server);
		jndiProperties.put("java.naming.factory.initial","com.sun.jndi.ldap.LdapCtxFactory");
		javax.naming.directory.DirContext jndiCtx = new javax.naming.directory.InitialDirContext(jndiProperties);

		// The group has a member attribute with a list of people of the LDAP present in the VO Group
		DirContext ctx = (DirContext) jndiCtx.lookup(principal);
		LDAPUserGroup group = new LDAPUserGroup(configuration, "group1");
		Map map = group.retrievePeopleMap(ctx);
		assertEquals("/DC=org/DC=griddev/OU=People/CN=Jane Doe 12345", map.get("jdoe"));
	}

	public void testUpdateMembers2() {
		((LDAPUserGroup)group).setGroupTree("");
		group.updateMembers();
		assertTrue(group.isInGroup(new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", null)));
		assertTrue(group.isInGroup(new GridUser("/DC=org/DC=griddev/OU=People/CN=Jane Doe 12345", null)));
	}

	public void testGetMemberList2() {
		group.updateMembers();
		List members = group.getMemberList();
		assertTrue(members.size() > 0);
		Iterator iter = members.iterator();
		while (iter.hasNext()) {
			GridUser dn = (GridUser) iter.next();
			assertTrue(group.isInGroup(dn));
		}
	}

}
