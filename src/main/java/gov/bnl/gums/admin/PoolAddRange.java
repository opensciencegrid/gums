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
        syntax = "ACCOUNTMAPPER RANGE";
        description = "Adds range of accounts to a pool. " +
            "ACCOUNTMAPPER is the name of the account mapper. " +
            "RANGE is the group of accounts to be added (i.e. grid0050-125).";
    }

    protected org.apache.commons.cli.Options buildOptions() {
        Options options = new Options();

        return options;
    }

    protected void execute(org.apache.commons.cli.CommandLine cmd)
        throws Exception {
        if (cmd.getArgs().length < 2) {
            failForWrongParameters("Missing parameters...");
        }

        String accountMapper = cmd.getArgs()[0];

        for (int nArg = 2; nArg < cmd.getArgs().length; nArg++) {
        	getGums().addAccountRange(accountMapper, cmd.getArgs()[nArg]);
        }
    }
}
