/*
 * CompositeAccountMapperTest.java
 * JUnit based test
 *
 * Created on May 25, 2004, 3:26 PM
 */

package gov.bnl.gums.account;

import gov.bnl.gums.configuration.Configuration;
import gov.bnl.gums.persistence.MockPersistenceFactory;
import gov.bnl.gums.persistence.PersistenceFactory;

import java.util.*;

import junit.framework.*;

/**
 *
 * @author carcassi
 */
public class ManualAccountMapperTest extends TestCase {
    
    AccountMapper mapper;
    
    public ManualAccountMapperTest(java.lang.String testName) {
        super(testName);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(ManualAccountMapperTest.class);
        return suite;
    }
    
    public void setUp() {
    	Configuration configuration = new Configuration();
        ManualAccountMapper mMapper = new ManualAccountMapper(configuration, "myManualAccountMapper");
        configuration.addAccountMapper(mMapper);
        PersistenceFactory persistenceFactory = new MockPersistenceFactory(configuration, "myMockPersistenceFactory");
        configuration.addPersistenceFactory(persistenceFactory);
        mMapper.setPersistenceFactory( persistenceFactory.getName() );
        mapper = mMapper;
    }
    
    public void testMap() {
        ((ManualAccountMapper) mapper).createMapping("/DC=org/DC=griddev/OU=People/CN=John Smith", "jsmith");
        assertEquals("jsmith", mapper.mapUser("/DC=org/DC=griddev/OU=People/CN=John Smith", true));
        assertEquals(null, mapper.mapUser("/DC=org/DC=griddev/OU=People/CN=Evil Persons", true));
    }
    
    public void testRemove() {
        ((ManualAccountMapper) mapper).createMapping("/DC=org/DC=griddev/OU=People/CN=John Smith", "jsmith");
        assertEquals("jsmith", mapper.mapUser("/DC=org/DC=griddev/OU=People/CN=John Smith", true));
        assertTrue(((ManualAccountMapper) mapper).removeMapping("/DC=org/DC=griddev/OU=People/CN=John Smith"));
        assertEquals(null, mapper.mapUser("/DC=org/DC=griddev/OU=People/CN=John Smith", true));
        assertFalse(((ManualAccountMapper) mapper).removeMapping("/DC=org/DC=griddev/OU=People/CN=Evil Person"));
        assertEquals(null, mapper.mapUser("/DC=org/DC=griddev/OU=People/CN=Evil Persons", true));
    }
    
}
