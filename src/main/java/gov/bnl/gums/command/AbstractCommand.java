/*
 * AbstractWebCommand.java
 *
 * Created on November 4, 2004, 10:07 AM
 */
package gov.bnl.gums.command;

import gov.bnl.gums.CertToolkit;
import gov.bnl.gums.admin.*;

import java.security.cert.X509Certificate;
import java.net.ConnectException;

import javax.net.ssl.X509KeyManager;

import org.apache.axis.AxisFault;
import org.apache.commons.cli.*;
import org.apache.log4j.Logger;
import org.apache.log4j.Level;

import org.glite.security.trustmanager.ContextWrapper;
import org.glite.security.voms.VOMSValidator;
import org.glite.security.voms.BasicVOMSTrustStore;
import org.glite.security.util.DirectoryList;

/**
 * Class to provide general command line utilities such as
 * argument parsing and authentication
 * 
 * @author Gabriele Carcassi, Jay Packard
 */
public abstract class AbstractCommand {
	static protected AbstractCommand command;

	/*static {
		String vomsdir;
		Logger.getLogger(org.glite.security.trustmanager.CRLFileTrustManager.class.getName()).setLevel(Level.ERROR);
		Logger.getLogger("org.glite.security.trustmanager.axis.AXISSocketFactory").setLevel(Level.OFF);
		Logger.getLogger("org.glite.security.util.DirectoryList").setLevel(Level.OFF);
		VOMSValidator.setTrustStore(new BasicVOMSTrustStore(System.getProperty("sslCAFiles"), 12*3600*1000));
	}*/
	
    static public void main(String[] args) {
        command.execute(args);
    }
    
	private Logger log = Logger.getLogger(AbstractCommand.class);
    private String clientDN;
    private boolean usingProxy;
    protected String commandName;
    protected String syntax;
    protected String description;
    protected boolean failOnArguments;
    
    /**
     * Creates a new AbstractWebCommand object.
     */
    public AbstractCommand() {
        String className = getClass().getName();

        commandName = CommandLineToolkit.getCommandName(className);
    }
    
    /**
     * Extracting the client DN
     */
    private void initClientCred() {
        // If DN is already found, no need to find it again...
        if (clientDN != null) return;
        
        log.trace("Retrieving client credentials");
        
        try {
            ContextWrapper wrapper = new ContextWrapper(System.getProperties());
            X509KeyManager manager = wrapper.getKeyManager();
            String[] aliases = manager.getClientAliases("RSA", null);
            
            if ((aliases == null) || (aliases.length == 0)) {
                System.err.println("\nThe user credentials loading failed");
                System.exit(-1);
            }
            
            X509Certificate[] chain = manager.getCertificateChain(aliases[0]);
            
            X509Certificate cert = chain[0];
            log.trace("Certificate retrieved: " + cert);
            clientDN = CertToolkit.getUserDN(cert);
            String commaDN = cert.getSubjectX500Principal().toString();
            String certDN = CertToolkit.convertDN(commaDN);
            log.trace("Certificate subject: " + certDN);
            log.trace("Client DN: " + clientDN);
            if (!clientDN.equals(certDN)) {
                log.trace("Using proxy");
                usingProxy = true;
            } else {
                log.trace("Not using proxy");
                usingProxy = false;
            }
        } catch (Exception e) {
            log.error("Couldn't retrieve client credentials", e);
            return;
        }
    }
    
    protected abstract Options buildOptions();

    protected abstract void execute(CommandLine cmd) throws Exception;

    protected void execute(String[] args) {
        Options options = buildOptions();
        Option help = OptionBuilder.withLongOpt("help")
                                   .withDescription("print this message")
                                   .create();

        //new Option("?", "help",false,"print this message");
        options.addOption(help);

        CommandLine cmd = parse(options, args);

        try {
            execute(cmd);
        } catch (AxisFault e) {
            if (e.getCause() != null) {
                log.info("An error ocurred when connecting to GUMS", e);

                if (e.getCause() instanceof ConnectException) {
                    System.err.println(
                        "Couldn't connect to the GUMS server. " +
                        "Check that your gums-client.properties configuration is pointing to the correct server, " +
                        "that there are no firewall or network problem, and that the server is up.");
                    System.err.println(
                        "[Error message: " +
                        e.getCause().getMessage() + "]");
                } else if (e.getCause() instanceof java.security.cert.CertificateExpiredException) {
                    System.err.println("Please renew your proxy, or renew your certificate if expired.");
                    System.err.println(
                        "[Error message: " +
                        e.getCause().getMessage() + "]");
                } else if ((e.getCause() instanceof java.security.cert.CertificateException) && 
                           (e.getCause().getMessage().indexOf("(No such file or directory)") != -1) &&
                           (e.getCause().getMessage().indexOf("/tmp/x509up_u") != -1)) {
                    System.err.println("Couldn't find your proxy: please generate your proxy");
                    System.err.println(
                        "[Error message: " +
                        e.getCause().getMessage() + "]");
                } else if ((e.getCause() instanceof java.security.cert.CertificateException) &&
                           (e.getCause().getMessage().indexOf("(Permission denied)") != -1) &&
                           (e.getCause().getMessage().indexOf("key.pem") != -1)) {
                    System.err.println("Couldn't read the private key: check that you are running as the correct user and check private key file ownership");
                    System.err.println(
                        "[Error message: " +
                        e.getCause().getMessage() + "]");
                } else {
                    System.err.println(
                        "An error occurred when connecting to GUMS: " +
                        e.getCause().getMessage());
                }
                
            } else {
                if (e.getFaultString().indexOf("(0)null") != -1) {
                    System.err.println("There was a problem connecting to the GUMS server.");
                    System.err.println("Try using an old-style proxy (i.e. 'grid-proxy-init -old').");
                    System.err.println(
                        "[Error message: " +
                        e.getMessage() + "]");
                } else {
                    System.err.println(
                        "An error occurred when connecting to GUMS: " +
                        e.getMessage());
                    log.info("An error occurred when connecting to GUMS", e);
                }
            }
            System.exit(-1);
        } catch (Exception e) {
            System.out.println("Unexpected error: " + e.getMessage());
            System.out.println("Full stack trace follows (give it to support):");
            System.out.println("-------------------------");
            e.printStackTrace(System.out);
            log.error("Unexpected error", e);
            System.exit(-1);
        }
    }

    protected void failForWrongParameters(String message) {
        System.out.println(message);
        System.out.println("Try `gums " + commandName +
            " --help' for more information.");
        System.exit(-1);
    }

    protected String getClientDN() {
        initClientCred();
        return clientDN;
    }

    protected abstract GUMSAPI getGums(String gumsUrl);
    
    protected boolean isUsingProxy() {
        initClientCred();
        return usingProxy;
    }

    protected CommandLine parse(Options options, String[] args) {
        CommandLineParser parser = new BasicParser();
        CommandLine commands = null;

        try {
            commands = parser.parse(options, args);

            if (commands.hasOption("help")) {
                printHelp(options);
                System.exit(0);
            }

            if (failOnArguments && (commands.getArgs() != null) &&
                    (commands.getArgs().length > 0)) {
                System.out.println("The command doesn't accept arguments");
                printHelp(options);
                System.exit(-1);
            }
        } catch (UnrecognizedOptionException e) {
            System.out.println(e.getMessage());
            printHelp(options);
            log.debug("Bogus option", e);
            System.exit(-1);
        } catch (ParseException pe) {
            System.out.println("Command line error:" + pe.getMessage());
            printHelp(options);
            log.info("Command line error", pe);
            System.exit(-1);
        }

        return commands;
    }

    protected void printHelp(Options options) {
        HelpFormatter formatter = new HelpFormatter();

        formatter.printHelp("gums " + commandName + " " + syntax,
            description + "\n\nOptions:", options, "");
    }
}
