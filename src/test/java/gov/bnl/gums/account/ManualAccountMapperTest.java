/*
 * CompositeAccountMapperTest.java
 * JUnit based test
 *
 * Created on May 25, 2004, 3:26 PM
 */

package gov.bnl.gums.account;

import gov.bnl.gums.persistence.MockPersistenceFactory;

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
        ManualAccountMapper mMapper = new ManualAccountMapper();
        mMapper.setPersistenceFactory( new MockPersistenceFactory("mock") );
        mapper = mMapper;
    }
    
    public void testMap() {
        ((ManualAccountMapper) mapper).createMapping("/DC=org/DC=doegrids/OU=People/CN=Gabriele Carcassi", "carcassi");
        assertEquals("carcassi", mapper.mapUser("/DC=org/DC=doegrids/OU=People/CN=Gabriele Carcassi"));
        assertEquals(null, mapper.mapUser("/DC=org/DC=doegrids/OU=People/CN=Evil Persons"));
    }
    
    public void testRemove() {
        ((ManualAccountMapper) mapper).createMapping("/DC=org/DC=doegrids/OU=People/CN=Gabriele Carcassi", "carcassi");
        assertEquals("carcassi", mapper.mapUser("/DC=org/DC=doegrids/OU=People/CN=Gabriele Carcassi"));
        assertTrue(((ManualAccountMapper) mapper).removeMapping("/DC=org/DC=doegrids/OU=People/CN=Gabriele Carcassi"));
        assertEquals(null, mapper.mapUser("/DC=org/DC=doegrids/OU=People/CN=Gabriele Carcassi"));
        assertFalse(((ManualAccountMapper) mapper).removeMapping("/DC=org/DC=doegrids/OU=People/CN=Evil Person"));
        assertEquals(null, mapper.mapUser("/DC=org/DC=doegrids/OU=People/CN=Evil Persons"));
    }
    
}
