/*
 * LDAPPersistenceFactoryTest.java
 * JUnit based test
 *
 * Created on January 24, 2005, 3:35 PM
 */

package gov.bnl.gums.persistence;

import junit.framework.*;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import gov.bnl.gums.*;

/**
 *
 * @author carcassi
 */
public class LDAPPersistenceFactoryTest extends TestCase {
    
    LDAPPersistenceFactory factory;
    
    public LDAPPersistenceFactoryTest(String testName) {
        super(testName);
    }

    protected void setUp() throws java.lang.Exception {
        factory = new LDAPPersistenceFactory(getName());
        factory.setConnectionFromLdapProperties();
        factory.setDefaultGumsOU("ou=GUMS,dc=test");
        try {
        factory.getLDAPContext().destroySubcontext("group=test,ou=GUMS,dc=test");
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
        factory.destroyMap("test", "map=test,ou=GUMS,dc=test");
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
        factory.createUserGroup("test", "group=test,ou=GUMS,dc=test");
    }
    
    public void testCreateMap() {
        factory.createMap("test", "map=test,ou=GUMS,dc=test");
    }
    
    public void testAddMapEntry() {
        factory.createMap("test", "map=test,ou=GUMS,dc=test");
        factory.addMapEntry("/DC=org/DC=doegrids/OU=People/CN=Gabriele Carcassi 1234", "grid001", "test",  "map=test,ou=GUMS,dc=test");
    }
    
    public void testRemoveMapEntry() {
        factory.createMap("test", "map=test,ou=GUMS,dc=test");
        factory.addMapEntry("/DC=org/DC=doegrids/OU=People/CN=Gabriele Carcassi 12345", "grid001", "test",  "map=test,ou=GUMS,dc=test");
        factory.addMapEntry("/DC=org/DC=doegrids/OU=People/CN=Dantong Yu 12345", "grid001", "test",  "map=test,ou=GUMS,dc=test");
        factory.removeMapEntry("/DC=org/DC=doegrids/OU=People/CN=Gabriele Carcassi 12345", "test",  "map=test,ou=GUMS,dc=test");
    }
    
    public void testAddUserGroupEntry() {
        factory.createUserGroup("test", "group=test,ou=GUMS,dc=test");
        factory.addUserGroupEntry("/DC=org/DC=doegrids/OU=People/CN=Gabriele Carcassi 12345", "test", "group=test,ou=GUMS,dc=test");
        factory.addUserGroupEntry("/DC=org/DC=doegrids/OU=People/CN=Dantong Yu 12345", "test", "group=test,ou=GUMS,dc=test");
    }
    
    public void testRemoveUserGroupEntry() {
        factory.createUserGroup("test", "group=test,ou=GUMS,dc=test");
        factory.addUserGroupEntry("/DC=org/DC=doegrids/OU=People/CN=Gabriele Carcassi 12345", "test", "group=test,ou=GUMS,dc=test");
        factory.addUserGroupEntry("/DC=org/DC=doegrids/OU=People/CN=Dantong Yu 12345", "test", "group=test,ou=GUMS,dc=test");
        factory.removeUserGroupEntry("/DC=org/DC=doegrids/OU=People/CN=Gabriele Carcassi 12345", "test", "group=test,ou=GUMS,dc=test");
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
////        db.addMember(new GridUser("/DC=org/DC=doegrids/OU=People/CN=Gabriele Carcassi 12345",null));
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
