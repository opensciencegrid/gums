/*
 * LDAPPersistenceFactoryTest.java
 * JUnit based test
 *
 * Created on January 24, 2005, 3:35 PM
 */

package gov.bnl.gums.persistence;

import junit.framework.*;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

import org.apache.commons.logging.LogFactory;

import gov.bnl.gums.*;
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
    	LogFactory.getLog(LDAPPersistenceFactory.class).trace("Retrieving LDAP properties from ldap.properties in the classpath");
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
        factory.setProperties(readLdapProperties());
        factory.setDefaultGumsOU("ou=GUMS,dc=griddev,dc=org");
        try {
        factory.getLDAPContext().destroySubcontext("group=test,ou=GUMS,dc=griddev,dc=org");
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
        factory.destroyMap("test", "map=test,ou=GUMS,dc=griddev,dc=org");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void tearDown() throws java.lang.Exception {
    }

    public static junit.framework.Test suite() {
        junit.framework.TestSuite suite = new junit.framework.TestSuite(LDAPPersistenceFactoryTest.class);
        
        return suite;
    }

    public void testGetLDAPContext() {
        DirContext context = factory.getLDAPContext();
    }
    
    public void testCreateUserGroup() {
        factory.createUserGroup("test", "group=test,ou=GUMS,dc=griddev,dc=org");
    }
    
    public void testCreateMap() {
        factory.createMap("test", "map=test,ou=GUMS,dc=griddev,dc=org");
    }
    
    public void testAddMapEntry() {
        factory.createMap("test", "map=test,ou=GUMS,dc=test");
        factory.addMapEntry("/DC=org/DC=griddev/OU=People/CN=John Smith", "grid001", "test",  "map=test,ou=GUMS,dc=griddev,dc=org");
    }
    
    public void testRemoveMapEntry() {
        factory.createMap("test", "map=test,ou=GUMS,dc=test");
        factory.addMapEntry("/DC=org/DC=griddev/OU=People/CN=John Smith", "grid001", "test",  "map=test,ou=GUMS,dc=griddev,dc=org");
        factory.addMapEntry("/DC=org/DC=griddev/OU=People/CN=Jane Doe 12345", "grid001", "test",  "map=test,ou=GUMS,dc=griddev,dc=org");
        factory.removeMapEntry("/DC=org/DC=griddev/OU=People/CN=John Smith", "test",  "map=test,ou=GUMS,dc=test");
    }
    
    public void testAddUserGroupEntry() {
        factory.createUserGroup("test", "group=test,ou=GUMS,dc=test");
        factory.addUserGroupEntry("/DC=org/DC=griddev/OU=People/CN=John Smith", "test", "group=test,ou=GUMS,dc=griddev,dc=org");
        factory.addUserGroupEntry("/DC=org/DC=griddev/OU=People/CN=Jane Doe 12345", "test", "group=test,ou=GUMS,dc=griddev,dc=org");
    }
    
    public void testRemoveUserGroupEntry() {
        factory.createUserGroup("test", "group=test,ou=GUMS,dc=test");
        factory.addUserGroupEntry("/DC=org/DC=griddev/OU=People/CN=John Smith", "test", "group=test,ou=GUMS,dc=griddev,dc=org");
        factory.addUserGroupEntry("/DC=org/DC=griddev/OU=People/CN=Jane Doe 12345", "test", "group=test,ou=GUMS,dc=griddev,dc=org");
        factory.removeUserGroupEntry("/DC=org/DC=griddev/OU=People/CN=John Smith", "test", "group=test,ou=GUMS,dc=griddev,dc=org");
    }
    
//    public static void main(String[] args) {
//        LDAPPersistenceFactory factory = new LDAPPersistenceFactory();
//        java.util.Properties jndiProperties = new java.util.Properties();
//        jndiProperties.put("java.naming.provider.url","ldap://rldap.rcf.bnl.gov/dc=usatlas,dc=bnl,dc=gov");
//        jndiProperties.put("java.naming.factory.initial","com.sun.jndi.ldap.LdapCtxFactory");
//        jndiProperties.put(Context.SECURITY_AUTHENTICATION, "simple");
//        jndiProperties.put(Context.SECURITY_PRINCIPAL, "uid=gumsAdmin,ou=People,dc=rhic,dc=bnl,dc=gov");
//        jndiProperties.put(Context.SECURITY_CREDENTIALS, "hk&1etf");
//        factory.setProperties(jndiProperties);
//        System.out.println(factory.findGID("gridgr00"));
//        factory.updateGID("grid0122", "69900");
////        ManualUserGroupDB db = factory.retrieveManualUserGroupDB("atlasDev");
////        System.out.println(db.retrieveMembers());
////        db.addMember(new GridUser("aTest",null));
////        db.addMember(new GridUser("/DC=org/DC=griddev/OU=People/CN=Gabriele Carcassi 12345",null));
////        DirContext context = factory.retrieveContext();
////        try {
////            System.out.println(context.getNameInNamespace());
////            NamingEnumeration result = context.search("userGroup=atlasDev", "(objectclass=*)", null);
////            List members = new ArrayList();
////            while (result.hasMore()) {
////                GridUser user = new GridUser();
////                SearchResult item = (SearchResult) result.next();
////                Attributes atts = item.getAttributes();
////                Attribute dn = atts.get("userDN");
////                if (dn != null) {
////                    user.setCertificateDN((String)dn.get());
////                }
////                Attribute fqan = atts.get("userFQAN");
////                if (fqan != null) {
////                    user.setVoFQAN(new FQAN((String)fqan.get()));
////                }
////                members.add(user);
////            }
////            System.out.println(members);
////        } catch (Exception e) {
////            e.printStackTrace();
////        }
//    }
    
//        public static void main(String[] args) {
//        LDAPPersistenceFactory factory = new LDAPPersistenceFactory();
//        java.util.Properties jndiProperties = new java.util.Properties();
//        jndiProperties.put("java.naming.provider.url","ldap://rldap.rcf.bnl.gov/dc=usatlas,dc=bnl,dc=gov");
//        jndiProperties.put("java.naming.factory.initial","com.sun.jndi.ldap.LdapCtxFactory");
//        jndiProperties.put(Context.SECURITY_AUTHENTICATION, "simple");
//        jndiProperties.put(Context.SECURITY_PRINCIPAL, "uid=gumsAdmin,ou=People,dc=rhic,dc=bnl,dc=gov");
//        jndiProperties.put(Context.SECURITY_CREDENTIALS, "hk&1etf");
//        factory.setProperties(jndiProperties);
//        System.out.println(factory.findGID("gridgr00"));
//        factory.addToSecondaryGroup("grid0087", "usatlas");
//        
//    }

}
