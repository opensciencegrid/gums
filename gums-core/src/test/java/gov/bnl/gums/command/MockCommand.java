/*
 * MockCommand.java
 *
 * Created on November 4, 2004, 1:45 PM
 */
package gov.bnl.gums.command;

import gov.bnl.gums.admin.*;



/**
 * @author carcassi
 */
public class MockCommand {
    static String[] arguments;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        arguments = args;
    }
}
