/*
 * GUMS2MapUser.java
 *
 * Created on June 9, 2004, 1:44 PM
 */
package gov.bnl.gums.admin;

/**
 * @author Gabriele Carcassi, Jay Packard
 */
public class GenerateGrid3UserVoMap extends GenerateMap {
    static {
        command = new GenerateGrid3UserVoMap();
    }

    /**
     * Creates a new GenerateGrid3UserVoMap object.
     */
    public GenerateGrid3UserVoMap() {
        syntax = "[-f FILENAME] [SERVICEDN]";
        description = "Generates the grid3-user-vo-map.txt for a service/host. " +
            "When using ./bin/gums, SERVICEDN must be specified. " +
            "When using ./bin/gums-host, SERVICEDN defaults to the host certificate DN.";
    }

    protected String generateMap(String hostname) throws Exception {
        String map = null;
        map = getGums().generateGrid3UserVoMap(hostname);
        if (map == null) {
            System.err.println("No map was found for the service/host " + hostname);
            System.exit(-1);
        }
        return map;
    }
}
