/*
 * GUMSCommandLineTest.java
 * JUnit based test
 *
 * Created on November 4, 2004, 1:48 PM
 */
package gov.bnl.gums.command;

import junit.framework.*;


import gov.bnl.gums.admin.*;


/**
 * @author carcassi
 */
public class GUMSCommandLineTest extends TestCase {
    /**
     * Creates a new GUMSCommandLineTest object.
     *
     * @param testName DOCUMENT ME!
     */
    public GUMSCommandLineTest(java.lang.String testName) {
        super(testName);
    }

    protected void tearDown() throws java.lang.Exception {
        GUMSCommandLine.clearCommands();
    }

    protected void setUp() throws java.lang.Exception {
        GUMSCommandLine.addCommand("gov.bnl.gums.command.MockCommand",
            "A useless command");
    }

    /**
     * TODO: write doc
     *
     * @return TODO: write doc
     */
    public static Test suite() {
        TestSuite suite = new TestSuite(GUMSCommandLineTest.class);

        return suite;
    }

    /**
     * TODO: write doc
     */
    public void testRunCommand() {
        String[] args = new String[] { "This", "is", "a", "test" };

        MockCommand.arguments = null;
        assertEquals(0, GUMSCommandLine.runCommand("mockCommand", args));
        assertNotNull(MockCommand.arguments);
        assertEquals(4, MockCommand.arguments.length);
        assertEquals("is", MockCommand.arguments[1]);
    }
}
