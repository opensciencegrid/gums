/*
 * GUMSCommandLine.java
 *
 * Created on November 4, 2004, 1:40 PM
 */
package gov.bnl.gums.admin;

import gov.bnl.gums.command.GUMSCommandLine;
import gov.bnl.gums.configuration.Configuration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author carcassi
 */
public class AdminCommandLine extends GUMSCommandLine {
    private static Log log = LogFactory.getLog(AdminCommandLine.class);

    static {
        GUMSCommandLine.addCommand("gov.bnl.gums.admin.GenerateGrid3UserVoMap",
            "Generate grid3-user-vo-map.txt for a given service/host.");
        GUMSCommandLine.addCommand("gov.bnl.gums.admin.GenerateGridMapfile",
            "Generate grid-mapfile for a given service/host.");
        GUMSCommandLine.addCommand("gov.bnl.gums.admin.GenerateVoGridMapfile",
        	"Generate a VO grid-mapfile for a given service/host.");
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
        GUMSCommandLine.addCommand("gov.bnl.gums.admin.MapfileCacheRefresh",
            "Regerates mapfiles in the cache.");
        GUMSCommandLine.addCommand("gov.bnl.gums.admin.PoolAddRange",
            "Adds accounts to an account pool.");
        GUMSCommandLine.addCommand("gov.bnl.gums.admin.PoolRemoveRange",
        	"Removes accounts from an account pool.");
        GUMSCommandLine.addCommand("gov.bnl.gums.admin.UpdateGroups",
            "Contact VO servers and retrieve user lists.");
        GUMSCommandLine.addCommand("gov.bnl.gums.admin.Version",
            "Retrieve GUMS client version.");
        GUMSCommandLine.addCommand("gov.bnl.gums.admin.MapAccount",
        	"Maps a local account to a grid identity.");
    }

}
