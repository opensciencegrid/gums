/*
 * CompositeAccountMapperTest.java
 * JUnit based test
 *
 * Created on May 25, 2004, 3:26 PM
 */

package gov.bnl.gums.account;


import java.util.*;
import junit.framework.*;

/**
 *
 * @author carcassi
 */
public class CompositeAccountMapperTest extends TestCase {
    
    AccountMapper mapper;
    
    public CompositeAccountMapperTest(java.lang.String testName) {
        super(testName);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(CompositeAccountMapperTest.class);
        return suite;
    }
    
    public void setUp() {
        CompositeAccountMapper cMapper = new CompositeAccountMapper();
        AccountMapper mapper1 = new MockAccountMapper();
        GroupAccountMapper mapper2 = new GroupAccountMapper();
        mapper2.setAccountName("groupAccount");
        List mappers = new ArrayList();
        mappers.add(mapper1);
        mappers.add(mapper2);
        cMapper.setMappers(mappers);
        mapper = cMapper;
    }
    
    public void testMap() {
        assertEquals("carcassi", mapper.mapUser("/DC=org/DC=doegrids/OU=People/CN=Gabriele Carcassi"));
        assertEquals("groupAccount", mapper.mapUser("/DC=org/DC=doegrids/OU=People/CN=Evil Persons"));
    }
    
}
