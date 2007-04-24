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
public class PoolGetAssignments extends RemoteCommand {
    static {
        command = new PoolGetAssignments();
    }

    /**
     * Creates a new ManualGroup_Add object.
     */
    public PoolGetAssignments() {
        syntax = "ACCOUNTMAPPER";
        description = "Gets printout of current pool account assignments. " +
            "ACCOUNTMAPPER is the name of the account mapper. ";
    }

    protected org.apache.commons.cli.Options buildOptions() {
        Options options = new Options();

        return options;
    }

    protected void execute(org.apache.commons.cli.CommandLine cmd)
        throws Exception {
        if (cmd.getArgs().length < 1) {
            failForWrongParameters("Missing parameters...");
        }

        String accountMapper = cmd.getArgs()[0];

        System.out.println( getGums().getPoolAccountAssignments(accountMapper) );
    }
}
