/*
 * GUMS2MapUser.java
 *
 * Created on June 9, 2004, 1:44 PM
 */
package gov.bnl.gums.admin;

import org.apache.commons.cli.*;

/**
 * @author Gabriele Carcassi, Jay Packard
 */
public class ManualGroupRemove extends RemoteCommand {
    static {
        command = new ManualGroupRemove();
    }

    /**
     * Creates a new ManualGroupRemove object.
     */
    public ManualGroupRemove() {
        syntax = "[-g GUMSURL] [-f FQAN] MANUALUSERGROUP USERDN1 [USERDN2] ...";
        description = "Removes a user from a manually managed group. " +
            "USERGROUP is the name of the manual user group.";
    }

    protected org.apache.commons.cli.Options buildOptions() {
        Options options = new Options();

        Option gumsUrl = new Option("g", "gumsUrl", true,
        "Fully Qualified GUMS URL to override gums.location within the gums-client.properties file");
        options.addOption(gumsUrl);
        
        Option fqan = new Option("f", "fqan", true,
        "Fully Qualified Attribute Name");
        options.addOption(fqan);
        
        return options;
    }

    protected void execute(org.apache.commons.cli.CommandLine cmd)
        throws Exception {
        if (cmd.getArgs().length < 2) {
            failForWrongParameters("Missing parameters...");
        }

        String userGroup = cmd.getArgs()[0];

        String gumsUrl = (cmd.getOptionValue("g", null));
        String fqan = (cmd.getOptionValue("f", null));
        
    	if (fqan==null) {
	        for (int nArg = 1; nArg < cmd.getArgs().length; nArg++) {
	            getGums(gumsUrl).manualGroupRemove2(userGroup, cmd.getArgs()[nArg]);
	        }
    	}
    	else {
	        for (int nArg = 1; nArg < cmd.getArgs().length; nArg++) {
	            getGums(gumsUrl).manualGroupRemove3(userGroup, cmd.getArgs()[nArg], fqan);
	        }   		
    	}
    }
}
