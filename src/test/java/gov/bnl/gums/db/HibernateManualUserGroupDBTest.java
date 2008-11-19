/*
 * HibernateManualUserGroupDBTest.java
 * JUnit based test
 *
 * Created on June 16, 2005, 10:17 AM
 */

package gov.bnl.gums.db;

import java.sql.*;
import junit.framework.*;
import gov.bnl.gums.*;
import gov.bnl.gums.configuration.Configuration;
import gov.bnl.gums.persistence.HibernatePersistenceFactory;
import org.hibernate.*;
import org.apache.log4j.Logger;

/**
 *
 * @author carcassi
 */
public class HibernateManualUserGroupDBTest extends ManualUserGroupDBTest {
    
    public HibernateManualUserGroupDBTest(String testName) {
        super(testName);
    }

    public void setUp() throws Exception {
    	HibernatePersistenceFactory hibernate = new HibernatePersistenceFactory(new Configuration(), "hibPers1");
        hibernate.setConnectionFromHibernateProperties();
        db = hibernate.retrieveManualUserGroupDB("test");
        SessionFactory sessions = hibernate.retrieveSessionFactory();
        Session session = sessions.openSession();
        Transaction tx = session.beginTransaction();
        Statement stmt = session.connection().createStatement();
        stmt.execute("DELETE FROM USER where GROUP_NAME = 'test'");
        tx.commit();
        session.close();
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(HibernateManualUserGroupDBTest.class);
        
        return suite;
    }
}
