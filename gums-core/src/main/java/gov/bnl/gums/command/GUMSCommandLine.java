/*
 * GUMSCommandLine.java
 *
 * Created on November 4, 2004, 1:40 PM
 */
package gov.bnl.gums.command;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.log4j.Logger;

/**
 * @author Gabriele Carcassi, Jay Packard
 */
public class GUMSCommandLine {
	static public String command = "gums";
	static private Logger log = Logger.getLogger(GUMSCommandLine.class);
	static private Map commands = new Hashtable();
	static private SortedMap commandDescriptions = new TreeMap();

	/**
	 * @param className
	 * @param description
	 */
	static public void addCommand(String className, String description) {
        String command = CommandLineToolkit.getCommandName(className);

        commands.put(command, className);
        commandDescriptions.put(command, description);
    }

	/**
	 * 
	 */
	static public void clearCommands() {
        commands = new Hashtable();
    }

    /**
     * @param args the command line arguments
     */
	static public void main(String[] args) {
        if (args.length == 0) {
            printHelp();
            System.exit(1);
        }

        String command = args[0];

        if (commands.get(command) == null) {
            System.out.println("Unknown command '" + command + "'");
            printHelp();
            System.exit(-1);
        }

        String[] newArgs = new String[args.length - 1];

        System.arraycopy(args, 1, newArgs, 0, newArgs.length);

        int retCode = runCommand(command, newArgs);

        System.exit(retCode);
    }

	/**
	 * 
	 */
	static public void printHelp() {
        System.out.println("usage: " + command + " command [command-options] ");
        System.out.println("Commands:");

        Iterator iter = commandDescriptions.keySet().iterator();

        while (iter.hasNext()) {
            String command = (String) iter.next();

            System.out.println("  " + command + " - " +
                commandDescriptions.get(command));
        }

        System.out.println("For help on any command:");
        System.out.println("  " + command + " command --help");
    }

	/**
	 * @param command
	 * @param args
	 * @return
	 */
	static public int runCommand(String command, String[] args) {
        String className = (String) commands.get(command);

        if (className == null) {
            System.out.println("Unknown command '" + command + "'");
            printHelp();

            return -1;
        }

        try {
            Class clazz = Class.forName(className);
            Method main = clazz.getMethod("main",
                    new Class[] { args.getClass() });

            main.invoke(null, new Object[] { args });

            return 0;
        } catch (ClassNotFoundException e) {
            System.out.println("Internal error: command class '" + className +
                "' not found.");

            return -1;
        } catch (NoSuchMethodException e) {
            System.out.println("Internal error: command class '" + className +
                "' doesn't have a main method.");

            return -1;
        } catch (IllegalAccessException e) {
            System.out.println(
                "Internal error: main method for command class '" + className +
                "' is not accessible (check it it's public).");

            return -1;
        } catch (IllegalArgumentException e) {
            System.out.println(
                "Internal error: illegal argument for main method of command class '" +
                className + "'.");

            return -1;
        } catch (InvocationTargetException e) {
            System.out.println("Command terminated unexpectedly: " +
                e.getTargetException());
            log.error("Command terminated unexpectedly", e.getTargetException());

            return -1;
        }
    }
}
