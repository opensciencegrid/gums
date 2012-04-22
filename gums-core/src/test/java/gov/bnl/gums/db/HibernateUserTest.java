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
            Long id = user.getId();
            trans.commit();
            trans = sess.beginTransaction();
            HibernateUser user2 = (HibernateUser) sess.load(HibernateUser.class, id);
            assertEquals("/DC=org/DC=griddev/OU=People/CN=John Smith", user2.getDn());
            sess.delete(user2);
            trans.commit();
            trans = sess.beginTransaction();
            assertEquals(sess.get(HibernateUser.class, id), null);
            trans.commit();
            sess.close();
        } catch (Exception e) {
            e.printStackTrace();
            fail("Hibernate exception " + e);
        }
    }
}
