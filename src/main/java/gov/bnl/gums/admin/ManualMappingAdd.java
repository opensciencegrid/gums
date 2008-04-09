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
public class ManualMappingAdd extends RemoteCommand {
    static {
        command = new ManualMappingAdd();
    }

    /**
     * Creates a new ManualMappingAdd object.
     */
    public ManualMappingAdd() {
        syntax = "[-g GUMSURL] ACCOUNTMAPPER USERDN USERNAME";
        description = "Maps a DN to a user in a manually managed mapping. " +
            "ACCOUNTMAPPER is the name of the manual account mapper.";
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
        if (cmd.getArgs().length < 3) {
            failForWrongParameters("Missing parameters...");
        }

        String accountMapper = cmd.getArgs()[0];
        String userDN = cmd.getArgs()[1];
        String username = cmd.getArgs()[2];
        
        String gumsUrl = (cmd.getOptionValue("g", null));

        getGums(gumsUrl).manualMappingAdd2(accountMapper, userDN, username);
    }
}
