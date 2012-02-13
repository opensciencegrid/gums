/*
 * HibernateMappingTest.java
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
public class HibernateMappingTest extends HibernateDBTestBase {
    
    public HibernateMappingTest(String testName) {
        super(testName);
    }
    
    public void testCreateRetrieveDelete() throws Exception {
        Session sess = sessions.openSession();
        Transaction trans = sess.beginTransaction();
        HibernateMapping mapping = new HibernateMapping();
        mapping.setDn("/DC=org/DC=griddev/OU=People/CN=John Smith");
        mapping.setAccount("jsmith");
        mapping.setMap("test");
        sess.save(mapping);
        Long id = mapping.getId();
        trans.commit();
        trans = sess.beginTransaction();
        HibernateMapping mapping2 = (HibernateMapping) sess.load(HibernateMapping.class, id);
        assertEquals("/DC=org/DC=griddev/OU=People/CN=John Smith", mapping2.getDn());
        sess.delete(mapping2);
        trans.commit();
        trans = sess.beginTransaction();
    	assertEquals(sess.get(HibernateMapping.class, id), null);
        trans.commit();
        sess.close();
    }
    
}
