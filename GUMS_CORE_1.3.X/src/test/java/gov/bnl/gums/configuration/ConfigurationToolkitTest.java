/*
 * ConfigurationToolkitTest.java
 * JUnit based test
 *
 * Created on June 4, 2004, 2:38 PM
 */
package gov.bnl.gums.configuration;

import gov.bnl.gums.GUMS;
import gov.bnl.gums.account.*;
import gov.bnl.gums.admin.CertCache;
import gov.bnl.gums.groupToAccount.*;
import gov.bnl.gums.hostToGroup.*;
import gov.bnl.gums.persistence.*;
import gov.bnl.gums.userGroup.*;

import java.io.FileInputStream;
import java.io.StringReader;
import java.net.URL;
import java.util.Date;
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

    protected void setUp() throws java.lang.Exception {

        // create and initialize an ad-hoc database.

        Derby.init();

    }

    protected void tearDown() throws Exception {
        Derby.shutdown();
    }

    public void testGumsAttributes() throws Exception {
        String xml =
                "<gums allowGridmapFiles='true'>"
                + "</gums>";
        Digester digester = toolkit.retrieveDigester();
        Configuration conf = new Configuration();
        digester.push(conf);
        Configuration acc = (Configuration) digester.parse(new StringReader(xml));
        assertSame(conf, acc);
        assertTrue(acc.getAllowGridmapFiles());
    }

    public void testSimpleUserGroup() throws Exception {
        String xml =
                "<gums version='1.3'>"
                + "<userGroups>"
                + "<manualUserGroup name='myGroupA' persistenceFactory='mysql' access='read self'/>"
                + "<manualUserGroup name='myGroupB' persistenceFactory='mysql' access='read all'/>"
                + "<manualUserGroup name='myGroupC' persistenceFactory='mysql' access='write'/>"
                + "</userGroups>"
                + "</gums>";
        Digester digester = toolkit.retrieveDigester();
        Configuration conf = new Configuration();
        conf.addPersistenceFactory(new HibernatePersistenceFactory(conf, "mysql"));
        digester.push(conf);
        Configuration acc = (Configuration) digester.parse(new StringReader(xml));
        assertSame(conf, acc);
        assertTrue(acc.getUserGroups().size() == 3);

        assertTrue(((UserGroup) acc.getWriteUserGroups().get(0)).getName().equals("myGroupC"));

        assertTrue(((UserGroup) acc.getReadAllUserGroups().get(0)).getName().equals("myGroupB"));
        assertTrue(((UserGroup) acc.getReadAllUserGroups().get(1)).getName().equals("myGroupC"));

        assertTrue(((UserGroup) acc.getReadSelfUserGroups().get(0)).getName().equals("myGroupA"));
        assertTrue(((UserGroup) acc.getReadSelfUserGroups().get(1)).getName().equals("myGroupB"));
        assertTrue(((UserGroup) acc.getReadSelfUserGroups().get(2)).getName().equals("myGroupC"));
    }

    public void testUserGroupWrongProperty() throws Exception {
        String xml =
                "<gums version='1.3'>"
                + "<userGroups>"
                + "<manualUserGroup name='myGroup' persistenceFactory='mysql' wrongProp='blah'/>"
                + "</userGroups>"
                + "</gums>";
        Digester digester = toolkit.retrieveDigester();
        Configuration conf = new Configuration();
        HibernatePersistenceFactory factory = new HibernatePersistenceFactory(conf, "mysql");
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
                "<gums version='1.3'>"
                + "<persistenceFactories>"
                + getPersistenceFactory("mysql")
                + "</persistenceFactories>"
                + "</gums>";
        Digester digester = toolkit.retrieveDigester();
        Configuration conf = new Configuration();
        digester.push(conf);
        Configuration acc = (Configuration) digester.parse(new StringReader(xml));
        assertSame(conf, acc);
        assertNotNull(conf.getPersistenceFactories().get("mysql"));
        HibernatePersistenceFactory factory = (HibernatePersistenceFactory) conf.getPersistenceFactories().get("mysql");
    }

    public void testUserGroupWithPersistenceFactory() throws Exception {
        String xml =
                "<gums version='1.3'>"
                + "<persistenceFactories>"
                + getPersistenceFactory("mysql")
                + "</persistenceFactories>"
                + "<userGroups>"
                + "<manualUserGroup name='myGroup' persistenceFactory='mysql'/>"
                + "</userGroups>"
                + "</gums>";
        Digester digester = toolkit.retrieveDigester();
        Configuration conf = new Configuration();
        digester.push(conf);
        Configuration acc = (Configuration) digester.parse(new StringReader(xml));
        assertSame(conf, acc);
    }

    public void testVOMSUserGroup() throws Exception {
        String xml =
                "<gums version='1.3'>"
                + "<persistenceFactories>"
                + getPersistenceFactory("mysql")
                + "</persistenceFactories>"
                + "<vomsServers>"
                + "<vomsServer name='myvo' persistenceFactory='mysql'/>"
                + "</vomsServers>"
                + "</gums>";
        Digester digester = toolkit.retrieveDigester();
        Configuration conf = new Configuration();
        digester.push(conf);
        Configuration acc = (Configuration) digester.parse(new StringReader(xml));
        assertSame(conf, acc);
    }

    public void testGroupToAccountMapping() throws Exception {
        String xml =
                "<gums version='1.3'>"
                + "<persistenceFactories>"
                + getPersistenceFactory("mysql")
                + "</persistenceFactories>"
                + "<userGroups>"
                + "<manualUserGroup name='myUserGroup' persistenceFactory='mysql'/>"
                + "</userGroups>"
                + "<accountMappers>"
                + "<manualAccountMapper name='myAccountMapper1' persistenceFactory='mysql'/>"
                + "<accountPoolMapper name='myAccountMapper2' accountPool='myPool' persistenceFactory='mysql'/>"
                + "</accountMappers>"
                + "<groupToAccountMappings>"
                + "<groupToAccountMapping name='myMapping' userGroups='myUserGroup' accountMappers='myAccountMapper1, myAccountMapper2'/>"
                + "</groupToAccountMappings>"
                + "</gums>";
        Digester digester = toolkit.retrieveDigester();
        Configuration conf = new Configuration();
        digester.push(conf);
        Configuration acc = (Configuration) digester.parse(new StringReader(xml));
        assertSame(conf, acc);
        GroupToAccountMapping gMap = (GroupToAccountMapping) conf.getGroupToAccountMappings().get("myMapping");
        assertNotNull(gMap);
        ManualUserGroup group = (ManualUserGroup) conf.getUserGroup((String) gMap.getUserGroups().get(0));
        assertEquals("myUserGroup", group.getName());
        assertEquals("mysql", group.getPersistenceFactory());
        AccountMapper accounts = (ManualAccountMapper) conf.getAccountMapper((String) gMap.getAccountMappers().get(0));
        assertEquals("myAccountMapper1", accounts.getName());
        assertEquals("mysql", ((ManualAccountMapper) accounts).getPersistenceFactory());
        accounts = (AccountPoolMapper) conf.getAccountMapper((String) gMap.getAccountMappers().get(1));
        assertEquals("myAccountMapper2", accounts.getName());
        assertEquals("mysql", ((AccountPoolMapper) accounts).getPersistenceFactory());
    }

    public void testHostGroup() throws Exception {
        String xml =
                "<gums version='1.3'>"
                + "<persistenceFactories>"
                + getPersistenceFactory("mysql")
                + "</persistenceFactories>"
                + "<userGroups>"
                + "<manualUserGroup name='myUserGroup' persistenceFactory='mysql' access='read all'/>"
                + "</userGroups>"
                + "<accountMappers>"
                + "<manualAccountMapper name='myAccountMapper1' persistenceFactory='mysql'/>"
                + "<manualAccountMapper name='myAccountMapper2' persistenceFactory='mysql'/>"
                + "</accountMappers>"
                + "<groupToAccountMappings>"
                + "<groupToAccountMapping name='atlas' userGroups='myUserGroup' accountMappers='myAccountMapper1, myAccountMapper2'/>"
                + "<groupToAccountMapping name='rhic' userGroups='myUserGroup' accountMappers='myAccountMapper1'/>"
                + "<groupToAccountMapping name='star' userGroups='myUserGroup' accountMappers='myAccountMapper1'/>"
                + "</groupToAccountMappings>"
                + "<hostToGroupMappings>"
                + "<hostToGroupMapping dn='atlas*.*.bnl.gov' groupToAccountMappings='atlas'/>"
                + "<hostToGroupMapping cn='*.rhic.bnl.gov' groupToAccountMappings='rhic, star'/>"
                + "</hostToGroupMappings>"
                + "</gums>";
        Digester digester = toolkit.retrieveDigester();
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
        assertEquals("atlas", (String) hostToGroupMapping.getGroupToAccountMappings().get(0));
        CertificateHostToGroupMapping certificateHostToGroupMapping = (CertificateHostToGroupMapping) hostToGroupMappings.get(1);
        assertTrue(certificateHostToGroupMapping.isInGroup("/DC=org/DC=griddev/OU=Services/CN=stargrid01.rhic.bnl.gov"));
        assertFalse(certificateHostToGroupMapping.isInGroup("atlasgrid01.usatlas.bnl.gov"));
        assertEquals(2, certificateHostToGroupMapping.getGroupToAccountMappings().size());
        assertEquals("rhic", (String) certificateHostToGroupMapping.getGroupToAccountMappings().get(0));
    }

    public void testHibernateWithPool() throws Exception {
        String xml = "<gums version='1.3'>"
                + "<persistenceFactories>"
                + getPersistenceFactory("mysql")
                + "</persistenceFactories>"
                + "</gums>";
        Digester digester = toolkit.retrieveDigester();
        Configuration conf = new Configuration();
        digester.push(conf);
        Configuration acc = (Configuration) digester.parse(new StringReader(xml));
        assertSame(conf, acc);
        assertNotNull(conf.getPersistenceFactories().get("mysql"));
        HibernatePersistenceFactory factory = (HibernatePersistenceFactory) conf.getPersistenceFactories().get("mysql");
        assertEquals("3", factory.getProperties().getProperty("hibernate.c3p0.min_size"));
    }

    public void testTransform() throws Exception {
        FileConfigurationStore confStore = new FileConfigurationStore();
        URL url = getClass().getClassLoader().getResource("gums.config");
        FileConfigurationStore.moveFile(url.getPath(), url.getPath() + ".temp");
        FileConfigurationStore.copyFile(url.getPath() + ".1.1", url.getPath());
        FileInputStream fileInputStream = new FileInputStream(url.getPath());
        StringBuffer configBuffer = new StringBuffer();
        try {
            int ch;
            while ((ch = fileInputStream.read()) != -1) {
                configBuffer.append((char) ch);
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        } finally {
            fileInputStream.close();
        }
        String drsView = configBuffer.toString();
        
        Configuration configuration = ConfigurationToolkit.parseConfiguration(configBuffer.toString(), false);
        confStore.setConfiguration(configuration, false, null, new Date());
        configuration = confStore.retrieveConfiguration();
        FileConfigurationStore.moveFile(url.getPath() + ".temp", url.getPath()); // This is used in later unit tests
    }

    private String getPersistenceFactory(String name) {
        return "<hibernatePersistenceFactory name=\"" + name + "\" hibernate.connection.driver_class=\"com.mysql.jdbc.Driver\" hibernate.dialect=\"org.hibernate.dialect.MySQLDialect\" hibernate.connection.url=\"jdbc:mysql://localhost/GUMS_1_3\" hibernate.connection.username=\"gums\" hibernate.connection.password=\"mysecret\" hibernate.c3p0.min_size=\"3\" hibernate.c3p0.max_size=\"20\" hibernate.c3p0.timeout=\"1800\"/>";
    }
}
