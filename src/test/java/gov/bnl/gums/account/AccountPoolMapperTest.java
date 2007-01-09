/*
 * GroupAccountMapperTest.java
 * JUnit based test
 *
 * Created on May 25, 2004, 2:18 PM
 */

package gov.bnl.gums.account;

import gov.bnl.gums.persistence.MockPersistenceFactory;

import java.util.HashSet;
import java.util.Set;

import junit.framework.*;

/**
 *
 * @author carcassi
 */
public class AccountPoolMapperTest extends TestCase {
    
    AccountMapper mapper;
    
    public AccountPoolMapperTest(java.lang.String testName) {
        super(testName);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(AccountPoolMapperTest.class);
        return suite;
    }
    
    public void setUp() {
        AccountPoolMapper gMapper = new AccountPoolMapper();
        mapper = gMapper;
        gMapper.setPersistenceFactory(new MockPersistenceFactory("myGroup"));
    }
    
    public void testMapUser() {
        int i = 0;
        Set accounts = new HashSet();
        String account = mapper.mapUser("/DC=org/DC=doegrids/OU=People/CN=User "+i);
        while (account != null) {
            i++;
            assertFalse(accounts.contains(account));
            accounts.add(account);
            account = mapper.mapUser("/DC=org/DC=doegrids/OU=People/CN=User "+i);
        }
    }
    
    public void testMapUser2() {
        String account1 = mapper.mapUser("/DC=org/DC=doegrids/OU=People/CN=User 1");
        String account2 = mapper.mapUser("/DC=org/DC=doegrids/OU=People/CN=User 2");
        assertEquals(account1, mapper.mapUser("/DC=org/DC=doegrids/OU=People/CN=User 1"));
        assertEquals(account2, mapper.mapUser("/DC=org/DC=doegrids/OU=People/CN=User 2"));
        assertEquals(account2, mapper.mapUser("/DC=org/DC=doegrids/OU=People/CN=User 2"));
        assertEquals(account1, mapper.mapUser("/DC=org/DC=doegrids/OU=People/CN=User 1"));
    }
    
}
