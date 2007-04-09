/*
 * GUMSCommandLine.java
 *
 * Created on November 4, 2004, 1:40 PM
 */
package gov.bnl.gums.admin;

import gov.bnl.gums.command.GUMSCommandLine;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author carcassi
 */
public class HostCommandLine extends GUMSCommandLine {
    private static Log log = LogFactory.getLog(HostCommandLine.class);

    static {
        GUMSCommandLine.command = "gums-host";
        GUMSCommandLine.addCommand("gov.bnl.gums.admin.GenerateGrid3UserVoMap",
            "Generate grid3-user-vo-map.txt for this host.");
        GUMSCommandLine.addCommand("gov.bnl.gums.admin.GenerateGridMapfile",
            "Generate grid-mapfile for this host.");
        GUMSCommandLine.addCommand("gov.bnl.gums.admin.GenerateVoGridMapfile",
    		"Generate a VO grid-mapfile for a given service/host.");
        GUMSCommandLine.addCommand("gov.bnl.gums.admin.MapUser",
            "Local credential used for a particular user.");
        GUMSCommandLine.addCommand("gov.bnl.gums.admin.Version",
            "Retrieve GUMS client version.");
    }

}
