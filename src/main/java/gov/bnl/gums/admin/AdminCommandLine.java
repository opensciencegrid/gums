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
public class AdminCommandLine extends GUMSCommandLine {
    static {
        GUMSCommandLine.addCommand("gov.bnl.gums.admin.GenerateOsgUserVoMap",
            "Generate OSG-user-VO-map for a given service/host.");
        GUMSCommandLine.addCommand("gov.bnl.gums.admin.GenerateGridMapfile",
            "Generate grid-mapfile for a given service/host.");
        GUMSCommandLine.addCommand("gov.bnl.gums.admin.GenerateVoGridMapfile",
    		"Generate a VO grid-mapfile for a given service/host.");
        GUMSCommandLine.addCommand("gov.bnl.gums.admin.GenerateFqanMapfile",
        	"Generate FQAN-mapfile for a given service/host .");
        GUMSCommandLine.addCommand("gov.bnl.gums.admin.GenerateEmailMapfile",
        	"Generate an Email-mapfile for a given service/host.");
        GUMSCommandLine.addCommand("gov.bnl.gums.admin.ManualGroupAdd",
            "Includes a DN in a group.");
        GUMSCommandLine.addCommand("gov.bnl.gums.admin.ManualGroupRemove",
            "Removes a DN from a group.");
        GUMSCommandLine.addCommand("gov.bnl.gums.admin.ManualMappingAdd",
            "Adds a DN-to-account mapping.");
        GUMSCommandLine.addCommand("gov.bnl.gums.admin.ManualMappingRemove",
            "Removes mapping for DN.");
        GUMSCommandLine.addCommand("gov.bnl.gums.admin.MapUser",
            "Maps a grid identity to a local account.");
        GUMSCommandLine.addCommand("gov.bnl.gums.admin.PoolAddRange",
            "Adds accounts to an account pool.");
        GUMSCommandLine.addCommand("gov.bnl.gums.admin.PoolRemoveRange",
        	"Removes accounts from an account pool.");
        GUMSCommandLine.addCommand("gov.bnl.gums.admin.PoolUnassignRange",
    		"Unassigns accounts from an account pool.");
        GUMSCommandLine.addCommand("gov.bnl.gums.admin.PoolGetAssignments",
        	"Get printout of current pool account assignments.");
        GUMSCommandLine.addCommand("gov.bnl.gums.admin.UpdateGroups",
            "Contact VO servers and retrieve user lists.");
        GUMSCommandLine.addCommand("gov.bnl.gums.admin.ServerVersion",
            "Retrieve GUMS server version.");
        GUMSCommandLine.addCommand("gov.bnl.gums.admin.ClientVersion",
        	"Retrieve GUMS client version.");
        GUMSCommandLine.addCommand("gov.bnl.gums.admin.MapAccount",
        	"Maps a local account to a grid identity.");
        
        // depricated
        GUMSCommandLine.addCommand("gov.bnl.gums.admin.ManualGroup_Add",
	        "Includes a DN in a group (depricated; required with 1.1 server).");
	    GUMSCommandLine.addCommand("gov.bnl.gums.admin.ManualGroup_Remove",
	        "Removes a DN from a group  (depricated; required with 1.1 server).");
	    GUMSCommandLine.addCommand("gov.bnl.gums.admin.ManualMapping_Add",
	        "Adds a DN-to-username in a mapping  (depricated; required with 1.1 server).");
	    GUMSCommandLine.addCommand("gov.bnl.gums.admin.ManualMapping_Remove",
	        "Removes a DN from a mapping (depricated; required with 1.1 server).");
        GUMSCommandLine.addCommand("gov.bnl.gums.admin.Pool_AddRange",
        	"Adds accounts to an account pool (depricated; required with 1.1 server).");
        GUMSCommandLine.addCommand("gov.bnl.gums.admin.GenerateGrid3UserVoMap",
        	"Generate Grid3-user-VO-map for a given service/host (depricated; required with 1.1 server).");
    }

}
