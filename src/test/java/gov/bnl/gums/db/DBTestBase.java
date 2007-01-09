/*
 * MachineTest.java
 * JUnit based test
 *
 * Created on October 11, 2004, 3:52 PM
 */

package gov.bnl.gums.db;

import java.sql.*;
import junit.framework.TestCase;
import net.sf.hibernate.*;
import net.sf.hibernate.cfg.*;

/**
 *
 * @author carcassi
 */
public class DBTestBase extends TestCase {
    SessionFactory sessions;
    
    public DBTestBase(String testName) {
        super(testName);
    }

    protected void setUp() throws java.lang.Exception {
        Configuration cfg = new Configuration()
            .addClass(HibernateMapping.class)
            .addClass(HibernateUser.class);
        sessions = cfg.buildSessionFactory();
        Session session = sessions.openSession();
        Statement stmt = session.connection().createStatement();
        stmt.execute("DELETE FROM USER WHERE GROUP_NAME='test'");
        stmt.execute("DELETE FROM MAPPING WHERE MAP='test'");
        session.close();
    }

    protected void tearDown() throws java.lang.Exception {
    }
    
}
