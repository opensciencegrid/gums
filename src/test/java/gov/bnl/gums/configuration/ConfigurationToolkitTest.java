/*
 * ConfigurationToolkitTest.java
 * JUnit based test
 *
 * Created on June 4, 2004, 2:38 PM
 */

package gov.bnl.gums.configuration;

import gov.bnl.gums.account.*;
import gov.bnl.gums.admin.CertCache;
import gov.bnl.gums.groupToAccount.*;
import gov.bnl.gums.hostToGroup.*;
import gov.bnl.gums.persistence.*;
import gov.bnl.gums.userGroup.*;

import java.io.StringReader;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

import junit.framework.*;
import org.apache.commons.digester.Digester;
import org.xml.sax.SAXParseException;

/**
 *
 * @author carcassi
 */
public class ConfigurationToolkitTest extends TestCase {
    
    ConfigurationToolkit toolkit = new ConfigurationToolkit();
    
    public ConfigurationToolkitTest(java.lang.String testName) {
        super(testName);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(ConfigurationToolkitTest.class);
        return suite;
    }
    
    public void testGumsAttributes() throws Exception {
        String xml =             
        "<gums errorOnMissedMapping='true'>" +
		"</gums>";
        Digester digester = toolkit.retrieveDigester(null);
        Configuration conf = new Configuration();
        digester.push(conf);
        Configuration acc = (Configuration) digester.parse(new StringReader(xml));
        assertSame(conf, acc);
        assertTrue(acc.isErrorOnMissedMapping());
    }
    
    public void testSimpleUserGroup() throws Exception {
        String xml = 
        "<gums>" +
	        "<userGroups>" +
	        	"<userGroup className='gov.bnl.gums.userGroup.ManualUserGroup' name='myGroupA' persistenceFactory='mysql' access='read self'/>" +
	        	"<userGroup className='gov.bnl.gums.userGroup.ManualUserGroup' name='myGroupB' persistenceFactory='mysql' access='read all'/>" +
	        	"<userGroup className='gov.bnl.gums.userGroup.ManualUserGroup' name='myGroupC' persistenceFactory='mysql' access='write'/>" +
	        "</userGroups>" +
        "</gums>";
        Digester digester = toolkit.retrieveDigester(getSchemaFile());
        Configuration conf = new Configuration();
        MySQLPersistenceFactory factory = new MySQLPersistenceFactory("mysql");
        conf.addPersistenceFactory(factory);
        digester.push(conf);
        Configuration acc = (Configuration) digester.parse(new StringReader(xml));
        assertSame(conf, acc);
        assertTrue(acc.getUserGroups().size()==3);
        
        assertTrue( ((UserGroup)acc.getWriteUserGroups().get(0)).getName().equals("myGroupC") );
        
        assertTrue( ((UserGroup)acc.getReadAllUserGroups().get(0)).getName().equals("myGroupB") );
        assertTrue( ((UserGroup)acc.getReadAllUserGroups().get(1)).getName().equals("myGroupC") );
        
        assertTrue( ((UserGroup)acc.getReadSelfUserGroups().get(0)).getName().equals("myGroupA") );
        assertTrue( ((UserGroup)acc.getReadSelfUserGroups().get(1)).getName().equals("myGroupB") );
        assertTrue( ((UserGroup)acc.getReadSelfUserGroups().get(2)).getName().equals("myGroupC") );
    }

    public void testUserGroupWrongClass() throws Exception {
        String xml = 
        "<gums>" +
	        "<userGroups>" +
	        	"<userGroup className='gov.bnl.gums.userGroup.WrongUserGroup' name='myGroup' persistenceFactory='mysql'/>" +
	        "</userGroups>" +
        "</gums>";
        Digester digester = toolkit.retrieveDigester(getSchemaFile());
        Configuration conf = new Configuration();
        MySQLPersistenceFactory factory = new MySQLPersistenceFactory("mysql");
        conf.addPersistenceFactory(factory);
        digester.push(conf);
        try {
            Configuration acc = (Configuration) digester.parse(new StringReader(xml));
        } catch (SAXParseException e) {
            assertEquals(java.lang.ClassNotFoundException.class, e.getException().getClass());
            assertEquals("gov.bnl.gums.userGroup.WrongUserGroup", e.getException().getMessage());
            return;
        }
        fail("Configuration had a wrong class but parsing didn't fail");
    }

    public void testUserGroupWrongProperty() throws Exception {
        String xml = 
        "<gums>" +
	        "<userGroups>" +
	        	"<userGroup className='gov.bnl.gums.userGroup.ManualUserGroup' name='myGroup' persistenceFactory='mysql' wrongProp='blah'/>" +
	        "</userGroups>" +
        "</gums>";
        Digester digester = toolkit.retrieveDigester(getSchemaFile());
        Configuration conf = new Configuration();
        MySQLPersistenceFactory factory = new MySQLPersistenceFactory("mysql");
        conf.addPersistenceFactory(factory);
        digester.push(conf);
        try {
            Configuration acc = (Configuration) digester.parse(new StringReader(xml));
        } catch (SAXParseException e) {
            assertEquals(java.lang.NoSuchMethodException.class, e.getException().getClass());
            assertEquals("Property wrongProp can't be set", e.getException().getMessage());
            return;
        }
        fail("Configuration had a wrong property but parsing didn't fail");
    }

    public void testPersistenceFactory() throws Exception {
        String xml = 
        "<gums>" +
	        "<persistenceFactories>" +
	        	"<persistenceFactory name='mysql' className='gov.bnl.gums.persistence.MySQLPersistenceFactory' jdbcDriver='com.mysql.jdbc.Driver' property.with.dot='test' />" +
	        "</persistenceFactories>" +
        "</gums>";
        Digester digester = toolkit.retrieveDigester(getSchemaFile());
        Configuration conf = new Configuration();
        digester.push(conf);
        Configuration acc = (Configuration) digester.parse(new StringReader(xml));
        assertSame(conf, acc);
        assertNotNull(conf.getPersistenceFactories().get("mysql"));
        MySQLPersistenceFactory factory = (MySQLPersistenceFactory) conf.getPersistenceFactories().get("mysql");
        assertEquals("com.mysql.jdbc.Driver", factory.getProperties().getProperty("jdbcDriver"));
        assertEquals("test", factory.getProperties().getProperty("property.with.dot"));
     }
    
    public void testUserGroupWithPersistenceFactory() throws Exception {
        String xml = 
        "<gums>" +
	        "<persistenceFactories>" +
	        	"<persistenceFactory name='mysql' className='gov.bnl.gums.persistence.MySQLPersistenceFactory' />" +
	        "</persistenceFactories>" +
	        "<userGroups>" +
	        	"<userGroup className='gov.bnl.gums.userGroup.ManualUserGroup' name='myGroup' persistenceFactory='mysql'/>" +
	        "</userGroups>" +
        "</gums>";
        Digester digester = toolkit.retrieveDigester(getSchemaFile());
        Configuration conf = new Configuration();
        digester.push(conf);
        Configuration acc = (Configuration) digester.parse(new StringReader(xml));
        assertSame(conf, acc);
    }
    
    public void testVOMSUserGroup() throws Exception {
        String xml = 
        "<gums>" +
	        "<persistenceFactories>" +
	        	"<persistenceFactory name='mysql' className='gov.bnl.gums.persistence.MySQLPersistenceFactory' />" +
	        "</persistenceFactories>" +
	        "<virtualOrganizations>" +
	        	"<virtualOrganization name='myvo' persistenceFactory='mysql'/>" +
	        "</virtualOrganizations>" +
        "</gums>";
        Digester digester = toolkit.retrieveDigester(getSchemaFile());
        Configuration conf = new Configuration();
        digester.push(conf);
        Configuration acc = (Configuration) digester.parse(new StringReader(xml));
        assertSame(conf, acc);
    }    
    
    public void testGroupToAccountMapping() throws Exception {
        String xml = 
        "<gums>" +
	        "<persistenceFactories>" +
	        	"<persistenceFactory name='mysql' className='gov.bnl.gums.persistence.MySQLPersistenceFactory' />" +
	        "</persistenceFactories>" +
	        "<userGroups>" +
	        	"<userGroup className='gov.bnl.gums.userGroup.ManualUserGroup' name='myUserGroup' persistenceFactory='mysql'/>" +
	        "</userGroups>" +
	        "<accountMappers>" +
	        	"<accountMapper className='gov.bnl.gums.account.ManualAccountMapper' name='myAccountMapper1' persistenceFactory='mysql'/>" +
	        	"<accountMapper className='gov.bnl.gums.account.ManualAccountMapper' name='myAccountMapper2' persistenceFactory='mysql'/>" +
	        "</accountMappers>" +        
	        "<groupToAccountMappings>" +
	        	"<groupToAccountMapping name='myMapping' userGroups='myUserGroup' accountMappers='myAccountMapper1, myAccountMapper2'/>" +
	        "</groupToAccountMappings>" +
        "</gums>";
        Digester digester = toolkit.retrieveDigester(getSchemaFile());
        Configuration conf = new Configuration();
        digester.push(conf);
        Configuration acc = (Configuration) digester.parse(new StringReader(xml));
        assertSame(conf, acc);
        GroupToAccountMapping gMap = (GroupToAccountMapping) conf.getGroupToAccountMappings().get("myMapping");
        assertNotNull(gMap);
        ManualUserGroup group = (ManualUserGroup) gMap.getUserGroups().get(0);
        assertEquals("myUserGroup", group.getName());
        assertEquals("mysql", group.getPersistenceFactory());
        ManualAccountMapper accounts = (ManualAccountMapper) gMap.getAccountMappers().get(0);
        assertEquals("myAccountMapper1", accounts.getName());
        assertEquals("mysql", accounts.getPersistenceFactory());
        accounts = (ManualAccountMapper) gMap.getAccountMappers().get(1);
        assertEquals("myAccountMapper2", accounts.getName());
        assertEquals("mysql", accounts.getPersistenceFactory());
    }
    
    public void testHostGroup() throws Exception {
        String xml = 
        "<gums>" +
	        "<persistenceFactories>" +
	        	"<persistenceFactory name='mysql' className='gov.bnl.gums.persistence.MySQLPersistenceFactory' />" +
	        "</persistenceFactories>" +
	        "<userGroups>" +
	        	"<userGroup className='gov.bnl.gums.userGroup.ManualUserGroup' name='myUserGroup' persistenceFactory='mysql' access='read all'/>" +
	        "</userGroups>" +
	        "<accountMappers>" +
	        	"<accountMapper className='gov.bnl.gums.account.ManualAccountMapper' name='myAccountMapper1' persistenceFactory='mysql'/>" +
	        	"<accountMapper className='gov.bnl.gums.account.ManualAccountMapper' name='myAccountMapper2' persistenceFactory='mysql'/>" +
	        "</accountMappers>" +        
	        "<groupToAccountMappings>" +
	        	"<groupToAccountMapping name='atlas' userGroups='myUserGroup' accountMappers='myAccountMapper1, myAccountMapper2'/>" +
	        	"<groupToAccountMapping name='rhic' userGroups='myUserGroup' accountMappers='myAccountMapper1'/>" +
	        	"<groupToAccountMapping name='star' userGroups='myUserGroup' accountMappers='myAccountMapper1'/>" +
	        "</groupToAccountMappings>" +
	        "<hostToGroupMappings>" +
        		"<hostToGroupMapping dn='atlas*.*.bnl.gov' groupToAccountMappings='atlas'/>" +
        		"<hostToGroupMapping cn='*.rhic.bnl.gov' groupToAccountMappings='rhic, star'/>" +
        	"</hostToGroupMappings>" +
        "</gums>";
        Digester digester = toolkit.retrieveDigester(getSchemaFile());
        Configuration conf = new Configuration();
        digester.push(conf);
        Configuration acc = (Configuration) digester.parse(new StringReader(xml));
        assertSame(conf, acc);
        List hostToGroupMappings = conf.getHostToGroupMappings();
        assertNotNull(hostToGroupMappings);
        assertEquals(2, hostToGroupMappings.size());
        CertificateHostToGroupMapping hostToGroupMapping = (CertificateHostToGroupMapping) hostToGroupMappings.get(0);
        assertTrue(hostToGroupMapping.isInGroup("atlasgrid01.usatlas.bnl.gov"));
        assertFalse(hostToGroupMapping.isInGroup("stargrid01.rhic.bnl.gov"));
        assertEquals(1, hostToGroupMapping.getGroupToAccountMappings().size());
        assertEquals("atlas",((GroupToAccountMapping) hostToGroupMapping.getGroupToAccountMappings().get(0)).getName());
        CertificateHostToGroupMapping certificateHostToGroupMapping = (CertificateHostToGroupMapping) hostToGroupMappings.get(1);
        assertTrue(certificateHostToGroupMapping.isInGroup("/DC=org/DC=doegrids/OU=Services/CN=stargrid01.rhic.bnl.gov"));
        assertFalse(certificateHostToGroupMapping.isInGroup("atlasgrid01.usatlas.bnl.gov"));
        assertEquals(2, certificateHostToGroupMapping.getGroupToAccountMappings().size());
        assertEquals("rhic",((GroupToAccountMapping) certificateHostToGroupMapping.getGroupToAccountMappings().get(0)).getName());
    }

    public void testHibernateWithPool() throws Exception {
        String xml = "<gums>" +
        	"<persistenceFactories>" +
        		"<persistenceFactory name=\"mysql\" className=\"gov.bnl.gums.persistence.HibernatePersistenceFactory\" hibernate.connection.driver_class=\"com.mysql.jdbc.Driver\" hibernate.dialect=\"net.sf.hibernate.dialect.MySQLDialect\" hibernate.connection.url=\"jdbc:mysql://localhost/GUMS_1_1\" hibernate.connection.username=\"gums\" hibernate.connection.password=\"mysecret\" hibernate.c3p0.min_size=\"3\" hibernate.c3p0.max_size=\"20\" hibernate.c3p0.timeout=\"1800\"/>"+
        	"</persistenceFactories>" +
        "</gums>";
        Digester digester = toolkit.retrieveDigester(getSchemaFile());
        Configuration conf = new Configuration();
        digester.push(conf);
        Configuration acc = (Configuration) digester.parse(new StringReader(xml));
        assertSame(conf, acc);
        assertNotNull(conf.getPersistenceFactories().get("mysql"));
        HibernatePersistenceFactory factory = (HibernatePersistenceFactory) conf.getPersistenceFactories().get("mysql");
        assertEquals("3", factory.getProperties().getProperty("hibernate.c3p0.min_size"));
    }
    
    private String getSchemaFile() {
    	URL schemaFile = getClass().getClassLoader().getResource("gums.config.schema");
    	assertTrue(schemaFile!=null);
    	return schemaFile.getPath();
    }
    
}
