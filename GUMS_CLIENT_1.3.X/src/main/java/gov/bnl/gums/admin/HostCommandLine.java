/*
 * GUMSCommandLine.java
 *
 * Created on November 4, 2004, 1:40 PM
 */
package gov.bnl.gums.admin;

import gov.bnl.gums.command.GUMSCommandLine;

/**
 * @author Gabriele Carcassi, Jay Packard
 */
public class HostCommandLine extends GUMSCommandLine {
    static {
        GUMSCommandLine.command = "gums-host";
        GUMSCommandLine.addCommand("gov.bnl.gums.admin.GenerateOsgUserVoMap",
    		"Generate OSG-user-VO-map.txt for this host.");
        GUMSCommandLine.addCommand("gov.bnl.gums.admin.MapUser",
        	"Local credential used for a particular user.");
        GUMSCommandLine.addCommand("gov.bnl.gums.admin.GenerateGridMapfile",
            "Generate grid-mapfile for this host.");
        GUMSCommandLine.addCommand("gov.bnl.gums.admin.GenerateVoGridMapfile",
    		"Generate a VO grid-mapfile for a given service/host.");
        GUMSCommandLine.addCommand("gov.bnl.gums.admin.GenerateFqanMapfile",
    		"Generate FQAN-mapfile for a given service/host .");        
        GUMSCommandLine.addCommand("gov.bnl.gums.admin.GenerateEmailMapfile",
			"Generate an Email-mapfile for a given service/host.");
        GUMSCommandLine.addCommand("gov.bnl.gums.admin.ServerVersion",
        	"Retrieve GUMS server version.");
	    GUMSCommandLine.addCommand("gov.bnl.gums.admin.ClientVersion",
	    	"Retrieve GUMS client version.");
	    
	    // depricated
        GUMSCommandLine.addCommand("gov.bnl.gums.admin.GenerateGrid3UserVoMap",
        	"Generate Grid3-user-VO-map for a given service/host (depricated; required with 1.1 server).");
    }

}
