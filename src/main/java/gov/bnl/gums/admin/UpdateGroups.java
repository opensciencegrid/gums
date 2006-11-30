/*
 * GUMS2MapUser.java
 *
 * Created on June 9, 2004, 1:44 PM
 */
package gov.bnl.gums.admin;


import org.apache.axis.AxisFault;

import org.apache.commons.cli.*;

import java.net.ConnectException;
import java.net.InetAddress;
import java.net.URL;
import gov.bnl.gums.command.AbstractCommand;


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
        syntax = "";
        description = "Contact all VO servers and update the local lists of users.";
    }

    protected org.apache.commons.cli.Options buildOptions() {
        Options options = new Options();

        return options;
    }

    protected void execute(org.apache.commons.cli.CommandLine cmd)
        throws Exception {
        getGums().updateGroups();
    }
}
