/*
 * CommandLineToolkit.java
 *
 * Created on November 4, 2004, 4:20 PM
 */
package gov.bnl.gums.command;

/**
 * Helper class for command line classes
 * 
 * @author Gabriele Carcassi, Jay Packard
 */
public class CommandLineToolkit {
	/**
	 * Get a user friendly command name for a class
	 * 
	 * @param className
	 * @return
	 */
	static public String getCommandName(String className) {
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

    /**
     * Creates a new instance of CommandLineToolkit
     */
    private CommandLineToolkit() {
    }
}
