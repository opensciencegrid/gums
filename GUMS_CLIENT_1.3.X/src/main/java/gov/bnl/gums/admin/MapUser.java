/*
 * GUMS2MapUser.java
 *
 * Created on June 9, 2004, 1:44 PM
 */
package gov.bnl.gums.admin;

import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;

import gov.bnl.gums.command.Configuration;
import org.apache.commons.cli.*;
import org.apache.log4j.Logger;
import org.apache.log4j.Level;
import org.opensciencegrid.authz.client.GRIDIdentityMappingServiceClient;
import org.opensciencegrid.authz.common.GridId;
import org.opensciencegrid.authz.common.LocalId;
import org.opensciencegrid.authz.xacml.client.MapCredentialsClient;
import org.opensaml.xml.XMLObjectBuilderFactory;


/**
 * @author Gabriele Carcassi, Jay Packard
 */
public class MapUser extends RemoteCommand {
	private static Logger log = Logger.getLogger(RemoteCommand.class);
	static XMLObjectBuilderFactory builderFactory;
	static PrintStream printStreamOriginal = System.err;
	
    static {
    	command = new MapUser();
        
        // Disable system error output since getBuilderFactory is outputting a bunch of information messages to it
    	System.setErr(new PrintStream(new OutputStream() {
    		public void write(int b) {
    		}
    	}));
    	
    	Logger.getLogger(org.glite.security.trustmanager.CRLFileTrustManager.class.getName()).setLevel(Level.ERROR);
        try {
            org.apache.xml.security.Init.init();
            org.opensaml.DefaultBootstrap.bootstrap();
        } catch (Exception e) {
            String err = "xacmlInitFailed";
            log.error(err, e);
            throw new RuntimeException(err, e);
        }
		
    	builderFactory = org.opensaml.Configuration.getBuilderFactory();
    	
        // Re-enable system error output
    	System.setErr(printStreamOriginal);
    }

    /**
     * Creates a new MapUser object.
     */
    public MapUser() {
        syntax = "[-g GUMSURL] [-s SERVICEDN] [-n TIMES] [-d] [-t NREQUESTS] [-b] [-f FQAN] [-i FQANISSUER] USERDN1 [USERDN2] ... ";
        description = "Maps the grid identity to the local account.";
    }

    protected org.apache.commons.cli.Options buildOptions() {
        Options options = new Options();
        
        Option gumsUrl = new Option("g", "gumsUrl", true,
                "Fully Qualified GUMS URL to override gums.location within the gums-client.properties file");

        options.addOption(gumsUrl);
        
        Option host = new Option("s", "serv", true,
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
        
        Option debug = new Option("d", "debug", false,
        		"Show debug information in case of null mapping");

        options.addOption(debug);
        
        return options;
    }

    protected void execute(org.apache.commons.cli.CommandLine cmd) throws Exception {
        if (cmd.getArgs().length < 1) {
            failForWrongParameters("Missing parameters...");
        }
        
        // Disable system error output since getBuilderFactory is outputting a bunch of information messages to it
    	PrintStream printStreamOriginal = System.err;
    	System.setErr(new PrintStream(new OutputStream() {
    		public void write(int b) {
    		}
    	}));
        
        boolean bypass = cmd.hasOption("b");
        
        boolean debug = cmd.hasOption("d");
        
        String serviceDn = (cmd.getOptionValue("s", null)); /* get hostname, default value is null */
        
        String gumsUrlStr = (cmd.getOptionValue("g", null));
        URL gumsUrl = null;
        if (gumsUrlStr!=null)
        	gumsUrl = new URL(gumsUrlStr);
        else {
        	if (bypass)
        		gumsUrl = Configuration.getInstance().getGUMSLocation();
        	else
        		gumsUrl = Configuration.getInstance().getGUMSAuthZLocation();
        }

        if (serviceDn == null) {
            if (isUsingProxy()) {
                failForWrongParameters("No service specified: please use the -s option followed by the DN of the service.");
            }
                
            try {
            	serviceDn = getClientDN();
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
        
        long overall = System.currentTimeMillis();
        long start = System.currentTimeMillis();
        long end = System.currentTimeMillis();
        if ((timing != null) && (!bypass)) {
            System.out.println("Initialization time: " + (end - start) + "ms");
        }
        
        Object client = null;
        start = System.currentTimeMillis();
        for (int n = 0; n < nTimes; n++) {
            for (int i = 0; i < userDN.length; i++) {
            	String account = null;
                if (!bypass) {
                	if (gumsUrl.getPath().contains("GUMSXACMLAuthorizationServicePort")) {
                        // Disable system error output since opensaml is outputting a bunch of information messages to it
                    	System.setErr(new PrintStream(new OutputStream() {
                    		public void write(int b) {
                    		}
                    	}));
                    	
                		if (client == null)
                			client = new MapCredentialsClient();
                		((MapCredentialsClient)client).setFqan(cmd.getOptionValue("f", null));
                		((MapCredentialsClient)client).setX509Subject(userDN[i]);
                		((MapCredentialsClient)client).setResourceX509ID(serviceDn);
                		account = ((MapCredentialsClient)client).mapCredentials(gumsUrl.toString()).getUserName();
                        
                		// Reenable system error output
                		System.setErr(printStreamOriginal);
                	}
                	else {
                		if (client == null)
                			client = new GRIDIdentityMappingServiceClient(gumsUrl);
                		GridId id = new GridId();
                        id.setHostDN(serviceDn);
                        id.setUserFQAN(cmd.getOptionValue("f", null));
                        id.setUserFQANIssuer(cmd.getOptionValue("i", null));
	                    id.setUserDN(userDN[i]);
	                    LocalId localId = ((GRIDIdentityMappingServiceClient)client).mapCredentials(id);
	                    if (localId!=null)
	                    	account = localId.toString();
                	}
                } else {
                	account = getGums(gumsUrl.toString()).mapUser(serviceDn, userDN[i], cmd.getOptionValue("f", null));
                }
           		if (debug && (account==null || account.equals(""))) {
           			System.out.print("Could not map user.\n");
           			System.out.print("The GUMS server configuration may not be correct.  ");
           			System.out.print("Please contact your administrator, or if you are the administrator, make sure you have the following elements in your gums.config (which can be easily configured from the web interface):\n");
           			System.out.print("\t1) A hostToGroupMapping element which matches the requesting host name: "+serviceDn+"\n");
           			System.out.print("\t2) A groupToAccountMapping (referenced by the hostToGroupMapping) element which contains a user group and account mapper\n");
           			System.out.print("\t3) A userGroup element (referenced by the groupToAccountMapping) to validate membership of the requested DN\n");
           			System.out.print("\t4) A accountMapper element (referenced by the groupToAccountMapping) to return the account for the requested DN\n");
           		}
           		else
               		System.out.println(account);
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
