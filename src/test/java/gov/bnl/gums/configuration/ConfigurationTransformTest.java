package gov.bnl.gums.configuration;

import java.io.File;
import java.io.StringReader;
import java.net.URI;
import java.net.URL;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/*
 * ConfigurationTransformTest.java
 * JUnit based test
 *
 * Created on may 15, 2007, 2:38 PM
 */

/**
*
* @author Jay Packard
*/
public class ConfigurationTransformTest extends TestCase {
    ConfigurationTransform transform = new ConfigurationTransform();
    
    public ConfigurationTransformTest(java.lang.String testName) {
        super(testName);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(ConfigurationTransformTest.class);
        return suite;
    }
    
    public void testTransform() throws Exception {
    	FileConfigurationStore confStore = new FileConfigurationStore();
    	URL url = getClass().getClassLoader().getResource("gums.config");
    	FileConfigurationStore.moveFile(url.getPath(), url.getPath()+".temp");
    	FileConfigurationStore.copyFile(url.getPath()+".1.1", url.getPath());
    	Configuration configuration = transform.doTransform(url.getPath(), url.getPath()+".transform");
    	confStore.setConfiguration(configuration, false, null);
		configuration = confStore.retrieveConfiguration();
		FileConfigurationStore.moveFile(url.getPath()+".temp", url.getPath());
    }
}
