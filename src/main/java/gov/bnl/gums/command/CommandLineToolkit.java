/*
 * CommandLineToolkit.java
 *
 * Created on November 4, 2004, 4:20 PM
 */
package gov.bnl.gums.command;

import gov.bnl.gums.admin.*;

/**
 * @author carcassi
 */
public class CommandLineToolkit {
    /**
     * Creates a new instance of CommandLineToolkit
     */
    private CommandLineToolkit() {
    }

    /**
     * TODO: write doc
     *
     * @param className TODO: write doc
     *
     * @return TODO: write doc
     */
    public static String getCommandName(String className) {
        className = className.substring(className.lastIndexOf('.') + 1);

        StringBuffer buffer = new StringBuffer(className);
        char first = buffer.charAt(0);

        buffer.setCharAt(0, Character.toLowerCase(first));

        for (int pos = buffer.indexOf("_"); pos != -1;
                pos = buffer.indexOf("_")) {
            buffer.setCharAt(pos, '-');

            if (pos != (buffer.length() - 1)) {
                buffer.setCharAt(pos + 1,
                    Character.toLowerCase(buffer.charAt(pos + 1)));
            }
        }

        return buffer.toString();
    }
}
