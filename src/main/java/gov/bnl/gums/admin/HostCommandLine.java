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
        GUMSCommandLine.addCommand("gov.bnl.gums.admin.MapUser",
            "Local credential used for a particular user.");
        GUMSCommandLine.addCommand("gov.bnl.gums.admin.Version",
            "Retrieve GUMS client version.");
    }

}
