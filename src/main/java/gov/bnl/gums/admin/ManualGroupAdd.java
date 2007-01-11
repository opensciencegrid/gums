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
     * Creates a new ManualGroup_Add object.
     */
    public ManualGroupAdd() {
        syntax = "PERSISTANCE GROUP USERDN1 [USERDN2] ...";
        description = "Adds a user to a manually managed group. " +
            "PERSISTANCE is the 'persistenceFactory' as defined in the configuration for the group." +
            "GROUP is the 'name' as defined in the configuration for the group.";
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
            getGums().manualGroupAdd(persistenceManager, groupName, cmd.getArgs()[nArg]);
        }
    }
}
