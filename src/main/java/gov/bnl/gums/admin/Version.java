/*
 * Version.java
 *
 * Created on May 11, 2005, 2:46 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package gov.bnl.gums.admin;

import java.io.File;
import org.apache.commons.cli.Options;

/**
 *
 * @author carcassi
 */
public class Version extends RemoteCommand {

    static {
        command = new Version();
    }

    /**
     * Creates a new Version object.
     */
    public Version() {
        syntax = "";
        description = "Returns the version of GUMS client being used.";
    }

    protected org.apache.commons.cli.Options buildOptions() {
        Options options = new Options();

        return options;
    }

    protected void execute(org.apache.commons.cli.CommandLine cmd) throws Exception {
    	System.out.println("GUMS version " + getGums().getVersion());
    }
    
}
