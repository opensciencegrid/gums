/*
 * LDAPUserGroupDBTest.java
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
import gov.bnl.gums.persistence.LDAPPersistenceFactoryTest;
import org.hibernate.*;
import org.apache.log4j.Logger;

/**
 *
 * @author carcassi
 */
public class LDAPUserGroupDBTest extends UserGroupDBTest {
    
    public LDAPUserGroupDBTest(String testName) {
        super(testName);
    }

    public void setUp() throws Exception {
        LDAPPersistenceFactory factory = new LDAPPersistenceFactory(new Configuration(), "ldapPers1");
	factory.setProperties(LDAPPersistenceFactoryTest.readLdapProperties());
        try {
            factory.retrieveGumsDirContext().destroySubcontext("group=testUserGroup");
        } catch (Exception e) {}
	db = factory.retrieveUserGroupDB("testUserGroup");
	initDB();
    }
    
    public void tearDown() {
    	LDAPPersistenceFactory factory = new LDAPPersistenceFactory(new Configuration(), "ldapPers1");
        factory.setProperties(LDAPPersistenceFactoryTest.readLdapProperties());
    	try {
    		factory.retrieveGumsDirContext().destroySubcontext("group=testUserGroup,ou=GUMS");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(LDAPUserGroupDBTest.class);
        
        return suite;
    }
    
}
