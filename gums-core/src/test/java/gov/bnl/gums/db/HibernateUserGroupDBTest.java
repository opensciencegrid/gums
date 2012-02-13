/*
 * HibernateUserGroupDBTest.java
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
import gov.bnl.gums.persistence.HibernatePersistenceFactory;
import org.hibernate.*;
import org.apache.log4j.Logger;

/**
 *
 * @author carcassi
 */
public class HibernateUserGroupDBTest extends UserGroupDBTest {
    protected SessionFactory sessions;
    
    public HibernateUserGroupDBTest(String testName) {
        super(testName);
    }

    public void setUp() throws Exception {
    	HibernatePersistenceFactory hibernate = new HibernatePersistenceFactory(new Configuration(), "hibPers1");
        hibernate.setConnectionFromHibernateProperties();
        db = hibernate.retrieveUserGroupDB("test");
        sessions = hibernate.retrieveSessionFactory();
        Session session = sessions.openSession();
        Transaction tx = session.beginTransaction();
        Statement stmt = session.connection().createStatement();
        stmt.execute("DELETE FROM USER where GROUP_NAME = 'test'");
        tx.commit();
        session.close();
        initDB();
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(HibernateUserGroupDBTest.class);
        
        return suite;
    }
    
    private int threadCount = 0;
    public void testConcurrency() throws Exception {
        Runnable task = new Runnable() {
            public void run() {
                try {
	                List members = new ArrayList();
	                Random rand = new Random();
	                char[] chars = new char[] {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'l'};
	                for (int n = 0; n<100; n++) {
	                    StringBuffer buff = new StringBuffer();
	                    for (int i = 0; i<20; i++) {
	                        buff.append(chars[rand.nextInt(10)]);
	                    }
	                    GridUser user = new GridUser(buff.toString(), null);
	                    members.add(user);
	                }
	                db.loadUpdatedList(members);
                } finally {
                        threadCount++;
                }
            }
        };
        threadCount = 0;
        for (int i = 0; i < 10; i++) {
            new Thread(task).start();
        }
        int wait = 0;
        while (threadCount != 10) {
            Thread.sleep(250);
            wait += 250;
            if (wait > 10000) {
                fail("Waiting for threads exceeded 10 seconds... Must be a problem with thread synchronization");
            }
        }
        List list = db.retrieveMembers();
        assertEquals(100, list.size());
        Session session = sessions.openSession();
        assertFalse(session.connection().getTransactionIsolation() == session.connection().TRANSACTION_SERIALIZABLE);
        session.close();
    }
}
