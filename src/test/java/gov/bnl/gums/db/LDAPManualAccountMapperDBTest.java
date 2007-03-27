/*
 * HibernateUserGroupDBTest.java
 * JUnit based test
 *
 * Created on June 16, 2005, 10:17 AM
 */

package gov.bnl.gums.db;

import java.sql.*;
import junit.framework.*;
import gov.bnl.gums.*;
import gov.bnl.gums.configuration.Configuration;
import gov.bnl.gums.persistence.LDAPPersistenceFactory;
import gov.bnl.gums.persistence.LDAPPersistenceFactoryTest;
import net.sf.hibernate.*;
import org.apache.commons.logging.*;

/**
 *
 * @author carcassi
 */
public class LDAPManualAccountMapperDBTest extends ManualAccountMapperDBTest {
    
    public LDAPManualAccountMapperDBTest(String testName) {
        super(testName);
    }

    public void setUp() throws Exception {
        LDAPPersistenceFactory factory = new LDAPPersistenceFactory(new Configuration(), "ldapPers1");
        factory.setProperties(LDAPPersistenceFactoryTest.readLdapProperties());
        factory.setDefaultGumsOU("ou=GUMS,dc=griddev,dc=org");
        try {
            factory.destroyMap("testManual", "map=testManual,ou=GUMS,dc=griddev,dc=org");
        } catch (Exception e) {
            e.printStackTrace();
        }
        db = factory.retrieveManualAccountMapperDB("testManual");
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(LDAPManualAccountMapperDBTest.class);
        
        return suite;
    }
}
