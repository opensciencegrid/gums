/*
 * GroupAccountMapperTest.java
 * JUnit based test
 *
 * Created on May 25, 2004, 2:18 PM
 */

package gov.bnl.gums.account;

import gov.bnl.gums.configuration.Configuration;
import junit.framework.*;

/**
 *
 * @author carcassi
 */
public class GroupAccountMapperTest extends TestCase {
    
    AccountMapper mapper;
    
    public GroupAccountMapperTest(java.lang.String testName) {
        super(testName);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(GroupAccountMapperTest.class);
        return suite;
    }
    
    public void setUp() {
    	mapper = new GroupAccountMapper(new Configuration(), "myGroupAccountMapper");
        ((GroupAccountMapper)mapper).setAccountName("myGroup");
    }
    
    public void testMapUser() {
        assertEquals("myGroup", mapper.mapUser("/DC=org/DC=griddev/OU=People/CN=John Smith", true));
        assertEquals("myGroup", mapper.mapUser("/DC=org/DC=griddev/OU=People/CN=Evil Person", true));
    }
    
}
