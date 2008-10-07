/*
 * GroupAccountMapperTest.java
 * JUnit based test
 *
 * Created on May 25, 2004, 2:18 PM
 */

package gov.bnl.gums.account;

import gov.bnl.gums.configuration.Configuration;
import gov.bnl.gums.persistence.MockPersistenceFactory;
import gov.bnl.gums.persistence.PersistenceFactory;
import gov.bnl.gums.userGroup.UserGroup;
import gov.bnl.gums.userGroup.VOMSUserGroup;

import java.util.HashSet;
import java.util.Iterator;
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
    	System.getProperty("os.name");
    	Configuration configuration = new Configuration();
        AccountPoolMapper accountMapper = new AccountPoolMapper(configuration, "myAccountPoolMapper");
        configuration.addAccountMapper(accountMapper);
        PersistenceFactory persistenceFactory = new MockPersistenceFactory(configuration, "myPersistenceFactory");
        configuration.addPersistenceFactory(persistenceFactory);
        mapper = accountMapper;
        accountMapper.setPersistenceFactory(persistenceFactory.getName());
    }
    
    public void testMapUser() {
        int i = 0;
        Set accounts = new HashSet();
        String account = mapper.mapUser("/DC=org/DC=griddev/OU=People/CN=User "+i, true);
        while (account != null) {
            i++;
            assertFalse(accounts.contains(account));
            accounts.add(account);
            account = mapper.mapUser("/DC=org/DC=griddev/OU=People/CN=User "+i, true);
        }
    }
    
    public void testMapUser2() {
        String account1 = mapper.mapUser("/DC=org/DC=griddev/OU=People/CN=User 1", true);
        String account2 = mapper.mapUser("/DC=org/DC=griddev/OU=People/CN=User 2", true);
        assertEquals(account1, mapper.mapUser("/DC=org/DC=griddev/OU=People/CN=User 1", true));
        assertEquals(account2, mapper.mapUser("/DC=org/DC=griddev/OU=People/CN=User 2", true));
        assertEquals(account2, mapper.mapUser("/DC=org/DC=griddev/OU=People/CN=User 2", true));
        assertEquals(account1, mapper.mapUser("/DC=org/DC=griddev/OU=People/CN=User 1", true));
    }
    
}
