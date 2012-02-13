/*
 * HibernateUserTest.java
 * JUnit based test
 *
 * Created on October 11, 2004, 1:47 PM
 */
package gov.bnl.gums.db;

import org.hibernate.*;

/**
 *
 * @author carcassi
 */
public class HibernateUserTest extends HibernateDBTestBase {

    public HibernateUserTest(String testName) {
        super(testName);
    }

    public void testCreateRetrieveDelete() throws Exception {
        try {
            Session sess = sessions.openSession();

            Transaction trans = sess.beginTransaction();
            HibernateUser user = new HibernateUser();
            user.setDn("/DC=org/DC=griddev/OU=People/CN=John Smith");
            user.setGroup("test");
            sess.save(user);
            System.out.println("drs - 7");
            Long id = user.getId();
            System.out.println("drs - 8");
            trans.commit();
            System.out.println("drs - 9");
            trans = sess.beginTransaction();
            System.out.println("drs - 10");
            HibernateUser user2 = (HibernateUser) sess.load(HibernateUser.class, id);
            System.out.println("drs - 11");
            assertEquals("/DC=org/DC=griddev/OU=People/CN=John Smith", user2.getDn());
            System.out.println("drs - 12");
            sess.delete(user2);
            System.out.println("drs - 13");
            trans.commit();
            System.out.println("drs - 14");
            trans = sess.beginTransaction();
            System.out.println("drs - 15");
            assertEquals(sess.get(HibernateUser.class, id), null);
            System.out.println("drs - 16");
            trans.commit();
            System.out.println("drs - 17");
            sess.close();
            System.out.println("drs - 18");
        } catch (Exception e) {
            e.printStackTrace();
            fail("Hibernate exception " + e);
        }
    }
}
