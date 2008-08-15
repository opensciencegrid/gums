/*
 * GUMS2MapUser.java
 *
 * Created on June 9, 2004, 1:44 PM
 */
package gov.bnl.gums.admin;

import org.apache.commons.cli.*;

/**
 * @author carcassi
 * @depricated
 */
public class Pool_AddRange extends RemoteCommand {
    static {
        command = new Pool_AddRange();
    }

    /**
     * Creates a new ManualGroup_Add object.
     */
    public Pool_AddRange() {
        syntax = "PERSISTANCE GROUP RANGE";
        description = "Adds range of accounts to a pool. " +
            "PERSISTANCE is the 'persistenceFactory' as defined in the configuration for the group. " +
            "GROUP is the 'name' as defined in the configuration for the pool. " +
            "RANGE is the group of accounts to be added (i.e. grid0050-125)." +
            "NOTE: This command is depricated in favor of PoolAddRange; required with 1.1 server";
    }

    protected org.apache.commons.cli.Options buildOptions() {
        Options options = new Options();

        return options;
    }

    protected void execute(org.apache.commons.cli.CommandLine cmd)
        throws Exception {
        if (cmd.getArgs().length < 3) {
            failForWrongParameters("Missing parameters...");
        }

        String[] userDN = (cmd.getArgs());
        String persistenceFactory = cmd.getArgs()[0];
        String groupName = cmd.getArgs()[1];

        for (int nArg = 2; nArg < cmd.getArgs().length; nArg++) {
            addRange(persistenceFactory, groupName,
                cmd.getArgs()[nArg]);
        }
    }

    private void addRange(String persistenceFactory, String groupName, String range) throws Exception {
        String firstAccount = range.substring(0, range.indexOf('-'));
        String lastAccountN = range.substring(range.indexOf('-') + 1);
        String firstAccountN = firstAccount.substring(firstAccount.length() - lastAccountN.length());
        String accountBase = firstAccount.substring(0, firstAccount.length() - lastAccountN.length());
        int nFirstAccount = Integer.parseInt(firstAccountN);
        int nLastAccount = Integer.parseInt(lastAccountN);

        StringBuffer last = new StringBuffer(firstAccount);
        String nLastAccountString = Integer.toString(nLastAccount);
        last.replace(firstAccount.length() - nLastAccountString.length(), firstAccount.length(), nLastAccountString);
        
        System.out.println("Adding accounts between '" + firstAccount + "' and '" + last.toString() + "' to pool '" + groupName + "'");
        
        StringBuffer buf = new StringBuffer(firstAccount);
        int len = firstAccount.length();
        for (int account = nFirstAccount; account <= nLastAccount; account++) {
            String nAccount = Integer.toString(account);
            buf.replace(len - nAccount.length(), len, nAccount);
            getGums().poolAddAccount(persistenceFactory, groupName, buf.toString());
            System.out.println(buf.toString() + " added");
        }
    }
}
