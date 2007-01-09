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
import gov.bnl.gums.persistence.LDAPPersistenceFactory;
import net.sf.hibernate.*;
import org.apache.commons.logging.*;

/**
 *
 * @author carcassi
 */
public class LDAPUserGroupDBTest extends UserGroupDBTest {
    
    public LDAPUserGroupDBTest(String testName) {
        super(testName);
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
        db = factory.retrieveUserGroupDB("testManual");
        initDB();
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(LDAPUserGroupDBTest.class);
        
        return suite;
    }
    
}
