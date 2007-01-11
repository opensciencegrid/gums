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
public class MapfileCacheRefresh extends RemoteCommand {
    static {
        command = new MapfileCacheRefresh();
    }

    /**
     * Creates a new MapfileCache_Refresh object.
     */
    public MapfileCacheRefresh() {
        syntax = "";
        description = "Regenerate the maps for all hosts and saves them in the mapfile cache.";
    }

    protected org.apache.commons.cli.Options buildOptions() {
        Options options = new Options();

        return options;
    }

    protected void execute(org.apache.commons.cli.CommandLine cmd)
        throws Exception {
        getGums().mapfileCacheRefresh();
    }
}
