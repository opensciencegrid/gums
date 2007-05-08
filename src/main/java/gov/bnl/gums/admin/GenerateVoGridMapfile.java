/*
 * GenerateVoGridMapfile.java
 *
 * Created on June 9, 2004, 1:44 PM
 */
package gov.bnl.gums.admin;

/**
 * @author Gabriele Carcassi, Jay Packard
 */
public class GenerateVoGridMapfile extends GenerateMap {
    static {
        command = new GenerateVoGridMapfile();
    }

    /**
     * Creates a new GenerateGridMapfile object.
     */
    public GenerateVoGridMapfile() {
        syntax = "[-f FILENAME] [SERVICEDN]";
        description = "Generates the grid-vo-mapfile for a service/host. ";
//            "When using ./bin/gums, SERVICEDN must be specified. " +
//            "When using ./bin/gums-host, SERVICEDN defaults to the host certificate DN.";
    }

    protected String generateMap(String hostname) throws Exception {
        String map = null;
        map = getGums().generateVoGridMapfile(hostname);
        if (map == null) {
            System.err.println("No map was found for the service/host " + hostname);
            System.exit(-1);
        }
        return map;
    }
}
