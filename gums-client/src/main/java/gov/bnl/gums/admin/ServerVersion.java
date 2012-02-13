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

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

/**
 *
 * @author carcassi
 */
public class ServerVersion extends RemoteCommand {

    static {
        command = new ServerVersion();
    }

    /**
     * Creates a new Version object.
     */
    public ServerVersion() {
        syntax = "[-g GUMSURL]";
        description = "Returns the version of GUMS server being used.";
    }

    protected org.apache.commons.cli.Options buildOptions() {
        Options options = new Options();

        Option gumsUrl = new Option("g", "gumsUrl", true,
        "Fully Qualified GUMS URL to override gums.location within the gums-client.properties file");
        options.addOption(gumsUrl);
        
        return options;
    }

    protected void execute(org.apache.commons.cli.CommandLine cmd) throws Exception {
    	String gumsUrl = (cmd.getOptionValue("g", null));
    	
    	System.out.println("GUMS server version " + getGums(gumsUrl).getVersion());
    }
    
}
