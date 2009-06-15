package gov.bnl.gums.configuration;

/*
 * FileConfigurationStoreTest.java
 * JUnit based test
 *
 * Created on October 20, 2004, 1:39 PM
 */

import java.nio.channels.*;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.util.Date;

import junit.framework.*;

/**
 *
 * @author carcassi
 */
public class FileConfigurationStoreTest extends TestCase {
    
    ConfigurationStore confStore;
    
    public FileConfigurationStoreTest(java.lang.String testName) {
        super(testName);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(FileConfigurationStoreTest.class);
        return suite;
    }
 
    protected void setUp() throws java.lang.Exception {
        confStore = new FileConfigurationStore();
    }    
    
    public void testFileChange() throws java.lang.Exception {
    	Configuration conf1 = confStore.retrieveConfiguration();
        Configuration conf2 = confStore.retrieveConfiguration();
        assertEquals(conf1, conf2);
        URL url = getClass().getClassLoader().getResource("gums.config");
        URI uri = new URI(url.toString());
        File file = new File(uri);
        Thread.sleep(1000);
        file.setLastModified(System.currentTimeMillis());
        conf2 = confStore.retrieveConfiguration();
        assertNotSame(conf1, conf2);
    }

    public void testFileStore() throws java.lang.Exception {
        Configuration conf = confStore.retrieveConfiguration();
        confStore.setConfiguration(conf, false, null, new Date());
        new FileConfigurationStore().retrieveConfiguration();
    }
    
}

