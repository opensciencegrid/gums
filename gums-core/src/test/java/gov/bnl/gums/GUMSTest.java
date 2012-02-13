/*
 * GUMSTest.java
 * JUnit based test
 *
 * Created on June 9, 2004, 10:15 AM
 */

package gov.bnl.gums;

import java.util.Collection;

import junit.framework.*;

import gov.bnl.gums.configuration.*;
import gov.bnl.gums.groupToAccount.GroupToAccountMapping;
import gov.bnl.gums.userGroup.*;
import gov.bnl.gums.persistence.*;

/**
 *
 * @author carcassi
 */
public class GUMSTest extends TestCase {
    
    public GUMSTest(String testName) {
        super(testName);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(GUMSTest.class);
        return suite;
    }
    
    public void testConfiguration() {
		GUMS gums = new GUMS();
		Configuration conf = null;
		try {
			conf = gums.getConfiguration();
		} catch (Exception e) {
			e.printStackTrace();
		}
		assertEquals(1, conf.getPersistenceFactories().size());
		assertEquals("persistenceFactoryA", ((HibernatePersistenceFactory) conf.getPersistenceFactories().get("persistenceFactoryA")).getName());
		assertEquals(4, conf.getGroupToAccountMappings().size());
		GroupToAccountMapping groupToAccountMapping = (GroupToAccountMapping) conf.getGroupToAccountMappings().get("groupToAccountMappingA");
		Collection userGroups = (Collection) groupToAccountMapping.getUserGroups();
		String userGroupName = (String)userGroups.iterator().next();
		assertEquals("persistenceFactoryA", ((ManualUserGroup)conf.getUserGroup( userGroupName )).getPersistenceFactory() );
		assertEquals("admins", (String)userGroups.iterator().next());
		assertEquals("write", conf.getUserGroup( userGroupName ).getAccess() );
		Collection accountMappers = (Collection) groupToAccountMapping.getAccountMappers();
		assertEquals("accountMapperA", (String)accountMappers.iterator().next());
		groupToAccountMapping = (GroupToAccountMapping) conf.getGroupToAccountMappings().get("_test");
		accountMappers = (Collection) groupToAccountMapping.getAccountMappers();
		assertEquals("_test", (String)accountMappers.iterator().next());
		assertEquals(2, conf.getHostToGroupMappings().size());
    }
    
}
