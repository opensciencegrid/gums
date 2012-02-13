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
public class PoolAddRange extends RemoteCommand {
    static {
        command = new PoolAddRange();
    }

    /**
     * Creates a new PoolAddRange object.
     */
    public PoolAddRange() {
        syntax = "[-g GUMSURL] POOLACCOUNTMAPPER RANGE";
        description = "Adds range of accounts to a pool. " +
        	"ACCOUNTMAPPER is the name of the account mapper. " +
            "RANGE is the group of accounts to be added (i.e. grid0050-0125).";
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
        
        String gumsUrl = (cmd.getOptionValue("g", null));

        for (int nArg = 1; nArg < cmd.getArgs().length; nArg++) {
        	getGums(gumsUrl).addAccountRange2(accountMapper, cmd.getArgs()[nArg]);
        }
    }
}
