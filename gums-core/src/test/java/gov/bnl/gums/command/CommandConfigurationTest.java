/*
 * AdminConfigurationTest.java
 * JUnit based test
 *
 * Created on November 3, 2004, 11:04 AM
 */
package gov.bnl.gums.command;

import junit.framework.*;

import java.net.URL;

/**
 * @author carcassi
 */
public class CommandConfigurationTest extends TestCase {
    /**
     * Creates a new AdminConfigurationTest object.
     *
     * @param testName DOCUMENT ME!
     */
    public CommandConfigurationTest(java.lang.String testName) {
        super(testName);
    }

    /**
     * TODO: write doc
     *
     * @return TODO: write doc
     */
    public static Test suite() {
        TestSuite suite = new TestSuite(CommandConfigurationTest.class);

        return suite;
    }

    /**
     * TODO: write doc
     */
    public void testGetGUMSLocation() {
        URL location = Configuration.getInstance().getGUMSLocation();
        
        assertEquals("https://localhost.localdomain:8443/gums/services/GUMSAdmin",
            location.toString());
    }

    // TODO add test methods here, they have to start with 'test' name.
    // for example:
    // public void testHello() {}
}
