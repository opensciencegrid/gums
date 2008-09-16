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
public class UpdateGroups extends RemoteCommand {
    static {
        command = new UpdateGroups();
    }

    /**
     * Creates a new UpdateGroups object.
     */
    public UpdateGroups() {
        syntax = "[-g GUMSURL]";
        description = "Contact all VO servers and update the local lists of users.";
    }

    protected org.apache.commons.cli.Options buildOptions() {
        Options options = new Options();

        Option gumsUrl = new Option("g", "gumsUrl", true,
        "Fully Qualified GUMS URL to override gums.location within the gums-client.properties file");
        options.addOption(gumsUrl);
        
        return options;
    }

    protected void execute(org.apache.commons.cli.CommandLine cmd) throws Exception {
        String gumsUrl = (cmd.getOptionValue("g", null));
    	
        getGums(gumsUrl).updateGroups();
    }
}
