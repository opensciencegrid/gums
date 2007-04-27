/*
 * GUMS2MapUser.java
 *
 * Created on June 9, 2004, 1:44 PM
 */
package gov.bnl.gums.admin;

import org.apache.commons.cli.Options;

/**
 * @author Gabriele Carcassi, Jay Packard
 */
public class MapAccount extends RemoteCommand {
    static {
        command = new MapAccount();
    }

    /**
     * Creates a new MapAccount object.
     */
    public MapAccount() {
        syntax = "ACCOUNTNAME";
        description = "Determines which grid identities are mapped to a local account. " +
            "ACCOUNTNAME is the local account. ";
    }

    protected org.apache.commons.cli.Options buildOptions() {
        Options options = new Options();

        return options;
    }
    
    protected void execute(org.apache.commons.cli.CommandLine cmd)
	    throws Exception {
	    if (cmd.getArgs().length < 1) {
	        failForWrongParameters("Missing parameters...");
	    }
	
	    String accountName = cmd.getArgs()[0];

        String dN = getGums().mapAccount(accountName);
        if (dN == null) {
            System.err.println("No map was found for account " + accountName);
            System.exit(-1);
        }
        System.out.println(accountName);
	}
}
