/*
 * HibernateAccountPoolMapperDBTest.java
 * JUnit based test
 *
 * Created on June 16, 2005, 10:17 AM
 */

package gov.bnl.gums.db;

import java.sql.*;
import junit.framework.*;
import gov.bnl.gums.configuration.Configuration;
import gov.bnl.gums.persistence.HibernatePersistenceFactory;
import org.hibernate.*;

/**
 *
 * @author carcassi
 */
public class HibernateAccountPoolMapperDBTest extends AccountPoolMapperDBTest {
	SessionFactory sessions;
	
    public HibernateAccountPoolMapperDBTest(String testName) {
        super(testName);
    }

    public void setUp() throws Exception {
        HibernatePersistenceFactory hibernate = new HibernatePersistenceFactory(new Configuration(), "hibPers1");
        hibernate.setConnectionFromHibernateProperties();
        db = hibernate.retrieveAccountPoolMapperDB("test");
        sessions = hibernate.retrieveSessionFactory();
        Session session = sessions.openSession();
        Transaction tx = session.beginTransaction();
        Statement stmt = session.connection().createStatement();
        stmt.execute("DELETE FROM MAPPING where MAP = 'test'");
        tx.commit();
        session.close();
        initDB();
        lastUsedDelay = 1000;
    }
    
    public void tearDown() throws Exception {
        Session session = sessions.openSession();
        Transaction tx = session.beginTransaction();
        Statement stmt = session.connection().createStatement();
        stmt.execute("DELETE FROM MAPPING where MAP = 'test'");
        tx.commit();
        session.close();
        sessions.close();
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(HibernateAccountPoolMapperDBTest.class);
        
        return suite;
    }
}
