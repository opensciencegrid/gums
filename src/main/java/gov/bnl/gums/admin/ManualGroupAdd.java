/*
 * GUMS2MapUser.java
 *
 * Created on June 9, 2004, 1:44 PM
 */
package gov.bnl.gums.admin;

import org.apache.commons.cli.*;

/**
 * @author carcassi
 */
public class ManualGroupAdd extends RemoteCommand {
    static {
        command = new ManualGroupAdd();
    }

    /**
     * Creates a new ManualGroupAdd object.
     */
    public ManualGroupAdd() {
        syntax = "[-g GUMSURL] [-f FQAN] [-e EMAIL] MANUALUSERGROUP USERDN0 USERDN1...";
        description = "Adds a user to a manually managed group. " +
            "MANUALUSERGROUP is the name of the manual user group. " +
            "Only one USERDN allowed at a time if email is specified.";
    }

    protected org.apache.commons.cli.Options buildOptions() {
        Options options = new Options();

        Option gumsUrl = new Option("g", "gumsUrl", true,
        "Fully Qualified GUMS URL to override gums.location within the gums-client.properties file");
        options.addOption(gumsUrl);
        
        Option fqan = new Option("f", "fqan", true,
        "Fully Qualified Attribute Name");
        options.addOption(fqan);
        
        Option email = new Option("e", "email", true,
        "email Address");
        options.addOption(email);
        
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
        String email = (cmd.getOptionValue("e", null));
        
    	if (email==null && fqan==null) {
	        for (int nArg = 1; nArg < cmd.getArgs().length; nArg++) {
			getGums(gumsUrl).manualGroupAdd2(userGroup, cmd.getArgs()[nArg]);
	        }
    	}
    	else if (email==null) {
	        for (int nArg = 1; nArg < cmd.getArgs().length; nArg++) {
        		getGums(gumsUrl).manualGroupAdd3(userGroup, cmd.getArgs()[nArg], fqan, email);
	        }
    	}
    	else {
    		if (cmd.getArgs().length>2)
    			failForWrongParameters("Only one USERDN allowed at a time if email is specified.");
    		getGums(gumsUrl).manualGroupAdd3(userGroup, cmd.getArgs()[1], fqan, email);
    	}
    }
}
