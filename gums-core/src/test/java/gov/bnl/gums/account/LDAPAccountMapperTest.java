/*
 * GecosLdpaAccountMapperTest.java
 * JUnit based test
 *
 * Created on May 10, 2005, 11:58 AM
 */

package gov.bnl.gums.account;

import gov.bnl.gums.configuration.Configuration;
import gov.bnl.gums.persistence.LDAPPersistenceFactory;
import gov.bnl.gums.persistence.LDAPPersistenceFactoryTest;
import junit.framework.*;
import java.util.Properties;
import javax.naming.NamingEnumeration;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchResult;

/**
 *
 * @author carcassi
 */
public class LDAPAccountMapperTest extends TestCase {
    LdapAccountMapper mapper;
    
    public LDAPAccountMapperTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        mapper = new LdapAccountMapper(new Configuration(), "ldapAccountMapper");
        mapper.setJndiLdapUrl( LDAPPersistenceFactoryTest.readLdapProperties().getProperty("java.naming.provider.url") );
    }

    protected void tearDown() throws Exception {
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(GecosLDAPAccountMapperTest.class);
        
        return suite;
    }

    public void testMap() {
    	String account = mapper.mapUser("/DC=org/DC=griddev/OU=People/CN=John Smith", false);
    	assertEquals(account, "jsmith");
    	account = mapper.mapUser("/DC=org/DC=griddev/OU=People/CN=Jane Doe 12345", false);
    	assertEquals(account, "jdoe");
    }
    
}
