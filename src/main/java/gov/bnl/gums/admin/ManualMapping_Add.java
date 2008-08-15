/*
 * GUMS2MapUser.java
 *
 * Created on June 9, 2004, 1:44 PM
 */
package gov.bnl.gums.admin;


import org.apache.commons.cli.*;

/**
 * @author carcassi
 * @depricated
 */
public class ManualMapping_Add extends RemoteCommand {
    static {
        command = new ManualMapping_Add();
    }

    /**
     * Creates a new ManualMapping_Add object.
     */
    public ManualMapping_Add() {
        syntax = "PERSISTANCE GROUP USERDN USERNAME";
        description = "Maps a DN to a user in a manually managed mapping. " +
            "PERSISTANCE is the 'persistenceFactory' as defined in the configuration for the group." +
            "GROUP is the 'name' as defined in the configuration for the group." +
            "NOTE: This command is depricated in favor of ManualMappingAdd; required with 1.1 server";
    }

    protected org.apache.commons.cli.Options buildOptions() {
        Options options = new Options();

        return options;
    }

    protected void execute(org.apache.commons.cli.CommandLine cmd)
        throws Exception {
        if (cmd.getArgs().length < 4) {
            failForWrongParameters("Missing parameters...");
        }

        String persistenceFactory = cmd.getArgs()[0];
        String groupName = cmd.getArgs()[1];
        String userDN = cmd.getArgs()[2];
        String username = cmd.getArgs()[3];

        getGums().manualMappingAdd(persistenceFactory, groupName, userDN, username);
    }
}
