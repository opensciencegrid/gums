/*
 * LDAPQueryBugTest.java
 * JUnit based test
 *
 * Created on June 16, 2005, 10:17 AM
 */

package gov.bnl.gums.unused;

import junit.framework.*;
import gov.bnl.gums.*;
import gov.bnl.gums.configuration.Configuration;
import gov.bnl.gums.persistence.LDAPPersistenceFactory;
import gov.bnl.gums.persistence.LDAPPersistenceFactoryTest;
import gov.bnl.gums.db.UserGroupDB;

/**
 *
 * @author carcassi
 */
public class LDAPQueryBugTest extends TestCase {
    
    public LDAPQueryBugTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(LDAPQueryBugTest.class);
        
        return suite;
    }
    
    public void testAdmins() {
        LDAPPersistenceFactory factory = new LDAPPersistenceFactory(new Configuration(), "ldapPers1");
        factory.setProperties(LDAPPersistenceFactoryTest.readLdapProperties());
        factory.setDefaultGumsOU("ou=GUMS");
        UserGroupDB db2 = factory.retrieveUserGroupDB("admins");
        assertTrue(db2.isMemberInGroup(new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", null)));
    }
    
}
