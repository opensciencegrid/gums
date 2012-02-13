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
import gov.bnl.gums.configuration.Configuration;
import gov.bnl.gums.persistence.LDAPPersistenceFactory;
import gov.bnl.gums.persistence.LDAPPersistenceFactoryTest;

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
        LDAPPersistenceFactory factory = new LDAPPersistenceFactory(new Configuration(), "ldapPers1");
        factory.setProperties(LDAPPersistenceFactoryTest.readLdapProperties());
        try {
            factory.retrieveGumsDirContext().destroySubcontext("group=testManualUserGroup,ou=GUMS");
        } catch (Exception e) {}
        db = factory.retrieveManualUserGroupDB("testManualUserGroup");
    }
    
    public void tearDown() {
    	LDAPPersistenceFactory factory = new LDAPPersistenceFactory(new Configuration(), "ldapPers1");
        factory.setProperties(LDAPPersistenceFactoryTest.readLdapProperties());
    	try {
    		factory.retrieveGumsDirContext().destroySubcontext("group=testManualUserGroup,ou=GUMS");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}
