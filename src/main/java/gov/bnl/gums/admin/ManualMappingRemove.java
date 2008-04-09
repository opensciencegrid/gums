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
public class ManualMappingRemove extends RemoteCommand {
    static {
        command = new ManualMappingRemove();
    }

    /**
     * Creates a new ManualMappingRemove object.
     */
    public ManualMappingRemove() {
        syntax = "[-g GUMSURL] MANUALACCOUNTMAPPER USERDN";
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
        if (cmd.getArgs().length < 2) {
            failForWrongParameters("Missing parameters...");
        }

        String accountMapper = cmd.getArgs()[0];
        String userDN = cmd.getArgs()[1];
        
        String gumsUrl = (cmd.getOptionValue("g", null));

        getGums(gumsUrl).manualMappingRemove2(accountMapper, userDN);
    }
}
