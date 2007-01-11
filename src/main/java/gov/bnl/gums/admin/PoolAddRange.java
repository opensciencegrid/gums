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
     * Creates a new ManualGroup_Add object.
     */
    public PoolAddRange() {
        syntax = "PERSISTANCE GROUP RANGE";
        description = "Adds range of accounts to a pool. " +
            "PERSISTANCE is the 'persistenceFactory' as defined in the configuration for the group. " +
            "GROUP is the 'name' as defined in the configuration for the pool. " +
            "RANGE is the group of accounts to be added (i.e. grid0050-125).";
    }

    protected org.apache.commons.cli.Options buildOptions() {
        Options options = new Options();

        return options;
    }

    protected void execute(org.apache.commons.cli.CommandLine cmd)
        throws Exception {
        if (cmd.getArgs().length < 3) {
            failForWrongParameters("Missing parameters...");
        }

        String persistenceManager = cmd.getArgs()[0];
        String groupName = cmd.getArgs()[1];

        for (int nArg = 2; nArg < cmd.getArgs().length; nArg++) {
        	getGums().addAccountRange(persistenceManager, groupName, cmd.getArgs()[nArg]);
        }
    }
}
