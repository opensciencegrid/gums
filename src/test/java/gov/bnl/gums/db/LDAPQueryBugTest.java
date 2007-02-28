/*
 * LDAPQueryBugTest.java
 * JUnit based test
 *
 * Created on June 16, 2005, 10:17 AM
 */

package gov.bnl.gums.db;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import junit.framework.*;
import gov.bnl.gums.*;
import gov.bnl.gums.configuration.Configuration;
import gov.bnl.gums.persistence.LDAPPersistenceFactory;
import net.sf.hibernate.*;
import org.apache.commons.logging.*;

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
        factory.setConnectionFromLdapProperties();
        factory.setDefaultGumsOU("ou=GUMS,dc=test");
        UserGroupDB db2 = factory.retrieveUserGroupDB("admins");
        assertTrue(db2.isMemberInGroup(new GridUser("/DC=org/DC=doegrids/OU=People/CN=John Smith 12345", null)));
    }
    
}
