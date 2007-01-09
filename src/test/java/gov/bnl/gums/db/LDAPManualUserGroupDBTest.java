/*
 * LDAPManualUserGroupDBTest.java
 * JUnit based test
 *
 * Created on May 25, 2004, 4:41 PM
 */

package gov.bnl.gums.db;

import java.sql.*;
import java.util.*;
import junit.framework.*;
import gov.bnl.gums.*;
import gov.bnl.gums.persistence.LDAPPersistenceFactory;

/**
 *
 * @author carcassi
 */
public class LDAPManualUserGroupDBTest extends ManualUserGroupDBTest {
    
    public LDAPManualUserGroupDBTest(java.lang.String testName) {
        super(testName);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(LDAPManualUserGroupDBTest.class);
        return suite;
    }
     
    public void setUp() throws Exception {
        LDAPPersistenceFactory factory = new LDAPPersistenceFactory("ldapPers1");
        factory.setConnectionFromLdapProperties();
        factory.setDefaultGumsOU("ou=GUMS,dc=test");
        try {
            factory.getLDAPContext().destroySubcontext("group=testManual,ou=GUMS,dc=test");
        } catch (Exception e) {
            e.printStackTrace();
        }
        db = factory.retrieveManualUserGroupDB("testManual");
    }
    
}
