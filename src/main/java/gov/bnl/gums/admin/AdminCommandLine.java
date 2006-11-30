/*
 * GUMSCommandLine.java
 *
 * Created on November 4, 2004, 1:40 PM
 */
package gov.bnl.gums.admin;

import gov.bnl.gums.command.GUMSCommandLine;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import gov.bnl.gums.admin.*;


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
        GUMSCommandLine.addCommand("gov.bnl.gums.admin.ManualGroup_Add",
            "Includes a DN in a group.");
        GUMSCommandLine.addCommand("gov.bnl.gums.admin.ManualGroup_Remove",
            "Removes a DN from a group.");
        GUMSCommandLine.addCommand("gov.bnl.gums.admin.ManualMapping_Add",
            "Adds a DN-to-username in a mapping.");
        GUMSCommandLine.addCommand("gov.bnl.gums.admin.ManualMapping_Remove",
            "Removes a DN from a mapping.");
        GUMSCommandLine.addCommand("gov.bnl.gums.admin.MapUser",
            "Local credential used for a particular user.");
        GUMSCommandLine.addCommand("gov.bnl.gums.admin.MapfileCache_Refresh",
            "Regerates mapfiles in the cache.");
        GUMSCommandLine.addCommand("gov.bnl.gums.admin.Pool_AddRange",
            "Adds accounts to an account pool.");
        GUMSCommandLine.addCommand("gov.bnl.gums.admin.UpdateGroups",
            "Contact VO servers and retrieve user lists.");
        GUMSCommandLine.addCommand("gov.bnl.gums.admin.Version",
            "Retrieve GUMS client version.");
    }

}
