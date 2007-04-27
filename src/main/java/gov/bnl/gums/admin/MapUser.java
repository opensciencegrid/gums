/*
 * GUMS2MapUser.java
 *
 * Created on June 9, 2004, 1:44 PM
 */
package gov.bnl.gums.admin;

import gov.bnl.gums.command.Configuration;
import org.apache.commons.cli.*;
import org.opensciencegrid.authz.client.GRIDIdentityMappingServiceClient;
import org.opensciencegrid.authz.common.GridId;

/**
 * @author Gabriele Carcassi, Jay Packard
 */
public class MapUser extends RemoteCommand {
    static {
        command = new MapUser();
    }

    /**
     * Creates a new MapUser object.
     */
    public MapUser() {
        syntax = "[-s SERVICEDN] [-n TIMES] [-t NREQUESTS] [-b] [-f FQAN] [-i FQANISSUER] USERDN1 [USERDN2] ... ";
        description = "Maps the grid identity to the local account.";
    }

    protected org.apache.commons.cli.Options buildOptions() {
        Options options = new Options();
        Option host = new Option("s", "service", true,
                "DN of the service. When using gums-host, it defaults to the host credential DN.");

        options.addOption(host);

        Option number = new Option("n", "ntimes", true,
                "number of times the request will be repeated");

        options.addOption(number);

        Option timing = new Option("t", "timing", true,
                "enables timing, grouping the requests. For example, \"-t 100\" will give you timing information on 100 requests at a time");

        options.addOption(timing);

        Option bypass = new Option("b", "bypassCallout", false,
                "connects directly to GUMS instead of using the callout");

        options.addOption(bypass);
        
        Option fqan = new Option("f", "fqan", true,
                "Fully Qualified Attribute " +
                "Name, as it would be selected using voms-proxy-init; no extended information by default");

        options.addOption(fqan);

        Option issuer = new Option("i", "issuer", true,
                "Fully Qualified Attribute " +
                "Name Issuer, that is the DN of the VOMS service that issued the attribute certificate");

        options.addOption(issuer);

        return options;
    }

    protected void execute(org.apache.commons.cli.CommandLine cmd)
        throws Exception {
        if (cmd.getArgs().length < 1) {
            failForWrongParameters("Missing parameters...");
        }

        String hostname = (cmd.getOptionValue("s", null)); /* get hostname, default value is null */

        if (hostname == null) {
            if (isUsingProxy()) {
                failForWrongParameters("No service specified: please use the -s option followed by the DN of the service.");
            }
                
            try {
                hostname = getClientDN();
            } catch (Exception e) {
                System.err.print("Couldn't retrieve the DN of the service/host");
                System.exit(-1);
            }
        }

//        String fqan = cmd.getOptionValue("f", null);
        String[] userDN = (cmd.getArgs());
        
        String times = (cmd.getOptionValue("n", "1"));
        int nTimes = 0;
        try {
            nTimes = Integer.parseInt(times);
        } catch (NumberFormatException e) {
            System.err.println("-n argument should be an integer, and was '" + times + "'");
            System.exit(-1);
        }
        
        String timing = (cmd.getOptionValue("t", null));
        int requestInGroup = 0;
        if (timing != null) {
            try {
                requestInGroup = Integer.parseInt(timing);
            } catch (NumberFormatException e) {
                System.err.println("-t argument should be an integer, and was '" + timing + "'");
                System.exit(-1);
            }
        }
        
        boolean bypass = cmd.hasOption("b");
        
        long overall = System.currentTimeMillis();
        long start = System.currentTimeMillis();
        GRIDIdentityMappingServiceClient client = new GRIDIdentityMappingServiceClient(Configuration.getInstance().getGUMSAuthZLocation());
        GridId id = new GridId();
        id.setHostDN(hostname);
        id.setUserFQAN(cmd.getOptionValue("f", null));
        id.setUserFQANIssuer(cmd.getOptionValue("i", null));
        long end = System.currentTimeMillis();
        if ((timing != null) && (!bypass)) {
            System.out.println("Initialization time: " + (end - start) + "ms");
        }

        start = System.currentTimeMillis();
        for (int n = 0; n < nTimes; n++) {
            for (int i = 0; i < userDN.length; i++) {
                if (!bypass) {
                    id.setUserDN(userDN[i]);
                    System.out.println(client.mapCredentials(id));
                } else {
                    System.out.println(getGums().mapUser(hostname, userDN[i], id.getUserFQAN()));
                }
            }
            if ((timing != null) && (((n+1) % requestInGroup) == 0)) {
                end = System.currentTimeMillis();
                double delta = end - start;
                double freq = (delta * (double) userDN.length) / (double) requestInGroup;
                System.out.println("Requests [" + (n - requestInGroup + 1) + ", " + n + "]: " + delta + "ms, " + freq + "ms/req");
                start = System.currentTimeMillis();
            }
        }
        
        end = System.currentTimeMillis();
        double delta = end - overall;
        double freq = (delta * (double) userDN.length) / (double) nTimes;
        
        if (timing != null) {
            System.out.println("Overall: " + delta + "ms, " + freq + "ms/req");
        }
    }
}
