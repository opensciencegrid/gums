/*
 * LDAPPersistenceFactoryTest.java
 * JUnit based test
 *
 * Created on January 24, 2005, 3:35 PM
 */

package gov.bnl.gums.persistence;

import junit.framework.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import javax.naming.directory.DirContext;

import org.apache.log4j.Logger;

import gov.bnl.gums.configuration.Configuration;

/**
 *
 * @author carcassi
 */
public class LDAPPersistenceFactoryTest extends TestCase {

	LDAPPersistenceFactory factory;
	Configuration configuration;

	public LDAPPersistenceFactoryTest() {
		super("test");
		this.configuration = new Configuration();
	}

	public LDAPPersistenceFactoryTest(Configuration configuration, String testName) {
		super(testName);
		this.configuration = configuration;
	}

	static public Properties readLdapProperties() {
		Logger.getLogger(LDAPPersistenceFactory.class).trace("Retrieving LDAP properties from ldap.properties in the classpath");
		PropertyResourceBundle prop = (PropertyResourceBundle) ResourceBundle.getBundle("ldap");
		Properties prop2 = new Properties();
		Enumeration keys = prop.getKeys();
		while (keys.hasMoreElements()) {
			String key = (String) keys.nextElement();
			prop2.setProperty(key, prop.getString(key));
		}
		return prop2;
	}

	protected void setUp() throws java.lang.Exception {
		factory = new LDAPPersistenceFactory(configuration, getName());
		Properties properties = readLdapProperties();
		properties.setProperty("java.naming.provider.url", properties.getProperty("java.naming.provider.url").substring(0, properties.getProperty("java.naming.provider.url").indexOf("/dc")));
		factory.setProperties(properties);
		factory.setGroupTree("ou=Group,dc=griddev,dc=org");
		factory.setPeopleTree("ou=People,dc=griddev,dc=org");
		factory.setGumsTree("ou=GUMS,dc=griddev,dc=org");
		try {
			factory.retrieveGumsDirContext().destroySubcontext("group=test,ou=GUMS");
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		try {
			factory.destroyMap("test", "map=test,ou=GUMS");
		} catch (Exception e) {
		}
	}

	protected void tearDown() throws java.lang.Exception {
		try {
			factory.retrieveGumsDirContext().destroySubcontext("group=test,ou=GUMS");
		} catch (Exception e) {
		}
		try {
			factory.destroyMap("test", "map=test,ou=GUMS");
		} catch (Exception e) {
		}
	}

	public static junit.framework.Test suite() {
		junit.framework.TestSuite suite = new junit.framework.TestSuite(LDAPPersistenceFactoryTest.class);

		return suite;
	}

	public void testGetLDAPContext() {
		DirContext context = factory.retrieveGumsDirContext();
	}

	public void testCreateUserGroup() {
		factory.createUserGroup("test", "group=test,ou=GUMS");
	}

	public void testCreateMap() {
		factory.createMap("test", "map=test,ou=GUMS");
	}

	public void testAddMapEntry() {
		factory.createMap("test", "map=test,ou=GUMS");
		factory.addMapEntry("/DC=org/DC=griddev/OU=People/CN=John Smith", "grid001", "test",  "map=test,ou=GUMS");
		factory.createAccountInMap("grid002", "test", "map=test,ou=GUMS");
		factory.addMapEntry("/DC=org/DC=griddev/OU=People/CN=Jane Doe 12345", "grid002", "test", "map=test,ou=GUMS");
	}

	public void testRemoveMapEntry() {
		factory.createMap("test", "map=test,ou=GUMS");
		factory.addMapEntry("/DC=org/DC=griddev/OU=People/CN=John Smith", "grid001", "test",  "map=test,ou=GUMS");
		factory.addMapEntry("/DC=org/DC=griddev/OU=People/CN=Jane Doe 12345", "grid002", "test",  "map=test,ou=GUMS");
		factory.removeMapEntry("/DC=org/DC=griddev/OU=People/CN=John Smith", "test",  "map=test,ou=GUMS");
		factory.removeMapEntry("/DC=org/DC=griddev/OU=People/CN=Jane Doe 12345", "test",  "map=test,ou=GUMS");
	}

	public void testAddUserGroupEntry() {
		factory.createUserGroup("test", "group=test,ou=GUMS");
		factory.addUserGroupEntry("/DC=org/DC=griddev/OU=People/CN=John Smith", "test", "group=test,ou=GUMS");
		factory.addUserGroupEntry("/DC=org/DC=griddev/OU=People/CN=Jane Doe 12345", "test", "group=test,ou=GUMS");
	}

	public void testRemoveUserGroupEntry() {
		factory.createUserGroup("test", "group=test,ou=GUMS");
		factory.addUserGroupEntry("/DC=org/DC=griddev/OU=People/CN=John Smith", "test", "group=test,ou=GUMS");
		factory.addUserGroupEntry("/DC=org/DC=griddev/OU=People/CN=Jane Doe 12345", "test", "group=test,ou=GUMS");
		factory.removeUserGroupEntry("/DC=org/DC=griddev/OU=People/CN=John Smith", "test", "group=test,ou=GUMS");
		factory.removeUserGroupEntry("/DC=org/DC=griddev/OU=People/CN=Jane Doe 12345", "test", "group=test,ou=GUMS");
	}

	public void testChangeGroupID() {
		factory.changeGroupID("jsmith", "griddevGroup");
	}

	public void testAddToSecondaryGroupEntry() {
		factory.addToSecondaryGroup("jdoe", "griddevGroup");
		factory.addToSecondaryGroup("jsmith", "griddevGroup");
	}

//	public static void main(String[] args) {
//	LDAPPersistenceFactory factory = new LDAPPersistenceFactory();
//	java.util.Properties jndiProperties = new java.util.Properties();
//	jndiProperties.put("java.naming.provider.url","ldap://rldap.rcf.bnl.gov/dc=usatlas,dc=bnl,dc=gov");
//	jndiProperties.put("java.naming.factory.initial","com.sun.jndi.ldap.LdapCtxFactory");
//	jndiProperties.put(Context.SECURITY_AUTHENTICATION, "simple");
//	jndiProperties.put(Context.SECURITY_PRINCIPAL, "uid=gumsAdmin,ou=People,dc=rhic,dc=bnl,dc=gov");
//	jndiProperties.put(Context.SECURITY_CREDENTIALS, "hk&1etf");
//	factory.setProperties(jndiProperties);
//	System.out.println(factory.findGID("gridgr00"));
//	factory.updateGID("grid0122", "69900");
////	ManualUserGroupDB db = factory.retrieveManualUserGroupDB("atlasDev");
////	System.out.println(db.retrieveMembers());
////	db.addMember(new GridUser("aTest",null));
////	db.addMember(new GridUser("/DC=org/DC=griddev/OU=People/CN=Gabriele Carcassi 12345",null));
////	DirContext context = factory.retrieveContext();
////	try {
////	System.out.println(context.getNameInNamespace());
////	NamingEnumeration result = context.search("userGroup=atlasDev", "(objectclass=*)", null);
////	List members = new ArrayList();
////	while (result.hasMore()) {
////	GridUser user = new GridUser();
////	SearchResult item = (SearchResult) result.next();
////	Attributes atts = item.getAttributes();
////	Attribute dn = atts.get("userDN");
////	if (dn != null) {
////	user.setCertificateDN((String)dn.get());
////	}
////	Attribute fqan = atts.get("userFQAN");
////	if (fqan != null) {
////	user.setVoFQAN(new FQAN((String)fqan.get()));
////	}
////	members.add(user);
////	}
////	System.out.println(members);
////	} catch (Exception e) {
////	e.printStackTrace();
////	}
//	}

//	public static void main(String[] args) {
//	LDAPPersistenceFactory factory = new LDAPPersistenceFactory();
//	java.util.Properties jndiProperties = new java.util.Properties();
//	jndiProperties.put("java.naming.provider.url","ldap://rldap.rcf.bnl.gov/dc=usatlas,dc=bnl,dc=gov");
//	jndiProperties.put("java.naming.factory.initial","com.sun.jndi.ldap.LdapCtxFactory");
//	jndiProperties.put(Context.SECURITY_AUTHENTICATION, "simple");
//	jndiProperties.put(Context.SECURITY_PRINCIPAL, "uid=gumsAdmin,ou=People,dc=rhic,dc=bnl,dc=gov");
//	jndiProperties.put(Context.SECURITY_CREDENTIALS, "hk&1etf");
//	factory.setProperties(jndiProperties);
//	System.out.println(factory.findGID("gridgr00"));
//	factory.addToSecondaryGroup("grid0087", "usatlas");

//	}

}
