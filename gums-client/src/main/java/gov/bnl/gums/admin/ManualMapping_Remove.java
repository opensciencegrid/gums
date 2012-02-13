/*
 * GUMS2MapUser.java
 *
 * Created on June 9, 2004, 1:44 PM
 */
package gov.bnl.gums.admin;


import org.apache.axis.AxisFault;

import org.apache.commons.cli.*;

/**
 * @author carcassi
 *  @depricated
 */
public class ManualMapping_Remove extends RemoteCommand {
    static {
        command = new ManualMapping_Remove();
    }

    /**
     * Creates a new ManualMapping_Remove object.
     */
    public ManualMapping_Remove() {
        syntax = "PERSISTANCE GROUP USERDN";
        description = "Maps a DN to a user in a manually managed mapping. " +
            "PERSISTANCE is the 'persistenceFactory' as defined in the configuration for the group." +
            "GROUP is the 'name' as defined in the configuration for the group." +
            "NOTE: This command is depricated in favor of ManualMappingRemove; required with 1.1 server";
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

        String persistenceFactory = cmd.getArgs()[0];
        String groupName = cmd.getArgs()[1];
        String userDN = cmd.getArgs()[2];

        getGums().manualMappingRemove(persistenceFactory, groupName, userDN);
    }
}
