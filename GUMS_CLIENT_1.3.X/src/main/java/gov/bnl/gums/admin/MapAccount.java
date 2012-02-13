/*
 * GUMS2MapUser.java
 *
 * Created on June 9, 2004, 1:44 PM
 */
package gov.bnl.gums.admin;

import org.apache.commons.cli.Option;
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
        syntax = "[-g GUMSURL] ACCOUNTNAME";
        description = "Determines which grid identities are mapped to a local account. " +
            "ACCOUNTNAME is the local account. ";
    }

    protected org.apache.commons.cli.Options buildOptions() {
        Options options = new Options();

        Option gumsUrl = new Option("g", "gumsUrl", true,
        "Fully Qualified GUMS URL to override gums.location within the gums-client.properties file");
        options.addOption(gumsUrl);
        
        return options;
    }
    
    protected void execute(org.apache.commons.cli.CommandLine cmd)
	    throws Exception {
	    if (cmd.getArgs().length < 1) {
	        failForWrongParameters("Missing parameters...");
	    }
	
	    String accountName = cmd.getArgs()[0];

        String gumsUrl = (cmd.getOptionValue("g", null));
	    
        String dNs = getGums(gumsUrl).mapAccount(accountName);
        if (dNs == null) {
            System.err.println("No map was found for account " + accountName);
   			System.out.print("The GUMS server configuration may not be correct.  ");
   			System.out.print("Please contact your administrator, or if you are the administrator, make sure you have the following elements in your gums.config (which can be easily configured from the web interface):\n");
   			System.out.print("\t1) A hostToGroupMapping element which matches the requesting host name\n");
   			System.out.print("\t2) A groupToAccountMapping (referenced by the hostToGroupMapping) element which contains a user group and account mapper\n");
   			System.out.print("\t3) A userGroup element (referenced by the groupToAccountMapping) to validate membership of the requested DN\n");
   			System.out.print("\t4) A accountMapper element (referenced by the groupToAccountMapping) to return the account for the requested DN\n");
            System.exit(-1);
        }
        System.out.println(dNs);
	}
}
