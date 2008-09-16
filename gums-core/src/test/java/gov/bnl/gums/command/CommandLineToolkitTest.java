/*
 * CommandLineToolkitTest.java
 * JUnit based test
 *
 * Created on November 4, 2004, 4:21 PM
 */
package gov.bnl.gums.command;

import junit.framework.*;

import gov.bnl.gums.admin.*;


/**
 * @author carcassi
 */
public class CommandLineToolkitTest extends TestCase {
    /**
     * Creates a new CommandLineToolkitTest object.
     *
     * @param testName DOCUMENT ME!
     */
    public CommandLineToolkitTest(java.lang.String testName) {
        super(testName);
    }

    /**
     * TODO: write doc
     *
     * @return TODO: write doc
     */
    public static Test suite() {
        TestSuite suite = new TestSuite(CommandLineToolkitTest.class);

        return suite;
    }

    /**
     * TODO: write doc
     */
    public void testCommandName() {
        assertEquals("mockCommand",
            CommandLineToolkit.getCommandName(MockCommand.class.getName()));
        assertEquals("mapfileCache-refresh",
            CommandLineToolkit.getCommandName("gov.bnl.gums.admin.MapfileCache_Refresh"));
    }
}
