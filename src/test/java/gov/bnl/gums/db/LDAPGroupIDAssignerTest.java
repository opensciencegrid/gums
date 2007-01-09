/*
 * LDAPGroupIDAssignerTest.java
 * JUnit based test
 *
 * Created on October 3, 2005, 10:35 AM
 */

package gov.bnl.gums.db;

import java.util.Arrays;
import junit.framework.*;
import gov.bnl.gums.GUMS;
import gov.bnl.gums.persistence.LDAPPersistenceFactory;

import java.util.Iterator;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author carcassi
 */
public class LDAPGroupIDAssignerTest extends TestCase {
    
    LDAPPersistenceFactory factory;
    LDAPGroupIDAssigner assigner;
    
    public LDAPGroupIDAssignerTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        factory = new LDAPPersistenceFactory("ldapPers1");
        factory.setConnectionFromLdapProperties();
        factory.setUpdateGIDdomains("dc=test");
        assigner = factory.retrieveAssigner();
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(LDAPGroupIDAssignerTest.class);
        
        return suite;
    }

    public void testAssignGroups() {
        assigner.assignGroups("grid0001", "gridgr07", null);
        assigner.assignGroups("grid0001", "gridgr07", Arrays.asList(new String[] {"usastlas"}));
    }
    
}
