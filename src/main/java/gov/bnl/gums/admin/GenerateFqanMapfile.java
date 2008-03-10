/*
 * GenerateOsgUserVoMap.java
 *
 * Created on June 9, 2004, 1:44 PM
 */
package gov.bnl.gums.admin;

import gov.bnl.gums.configuration.Configuration;

/**
 * @author Gabriele Carcassi, Jay Packard
 */
public class GenerateFqanMapfile extends GenerateMap {
    static {
        command = new GenerateFqanMapfile();
    }

    /**
     * Creates a new GenerateOsgUserVoMap object.
     */
    public GenerateFqanMapfile() {
        syntax = "[-g GUMSURL] [-f FILENAME] [SERVICEDN]";
        description = "Generates the FQAN-mapfile for a service/host.";
//            "When using ./bin/gums, SERVICEDN must be specified. " +
//            "When using ./bin/gums-host, SERVICEDN defaults to the host certificate DN.";
    }

    protected String generateMap(String hostname, String gumsUrl) throws Exception {
        String map = null;
        map = getGums(gumsUrl).generateFqanMapfile(hostname);
        if (map == null) {
            System.err.println("Could not create FQAN-mapfile.");
   			System.out.print("The GUMS server configuration may not be correct.  ");
   			System.out.print("Please contact your administrator, or if you are the administrator, make sure you have the following elements in your gums.config (which can be easily configured from the web interface):\n");
   			System.out.print("\t1) A hostToGroupMapping element which matches the requesting host name: "+hostname+"\n");
   			System.out.print("\t2) A groupToAccountMapping (referenced by the hostToGroupMapping) element which contains a user group and account mapper\n");
   			System.out.print("\t3) A userGroup element (referenced by the groupToAccountMapping) to validate membership of the requested DN\n");
   			System.out.print("\t4) A accountMapper element (referenced by the groupToAccountMapping) to return the account for the requested DN\n");
            System.exit(-1);
        }
        return map;
    }
}
