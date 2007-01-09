/*
 * NisClient.java
 *
 * Created on March 25, 2004, 4:11 PM
 */

package gov.bnl.gums.account;


import gov.bnl.gums.GUMS;
import gov.bnl.gums.NISClient;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections.MultiHashMap;
import org.apache.commons.collections.MultiMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/** Retrieves the map from the NIS server and provide a logic to match name and
 * surname to an account.
 *
 * @author  Gabriele Carcassi
 */
public class GecosMap {
    private Log log = LogFactory.getLog(NISClient.class);
    private Log resourceAdminLog = LogFactory.getLog(GUMS.resourceAdminLog);
    private Map accountToGecos = new Hashtable();
    private Map accountToName = new Hashtable();
    private Map accountToSurname = new Hashtable();
    private MultiMap nameToAccount = new MultiHashMap();
    private MultiMap surnameToAccount = new MultiHashMap();
    
    public String findAccount(String name, String surname) {
        Collection accountsWithName = (Collection) nameToAccount.get(name.toLowerCase());
        Collection accountsWithSurname = (Collection) surnameToAccount.get(surname.toLowerCase());
        log.trace("Account matching. Name: " + accountsWithName + "- Surname: " + accountsWithSurname);
        if ((accountsWithName != null) && (accountsWithSurname != null)) {
            // Accounts for both name and surname were found
            List commonAccounts = new ArrayList(accountsWithName);
            commonAccounts.retainAll(accountsWithSurname);
            if (commonAccounts.size() == 1) {
                // Only one account matching both name and surname was found
                // Pretty likely is the correct one
                String username = (String) commonAccounts.get(0);
                log.trace("NIS account Name/Surname single match. Name: " + name + " - Surname: " + surname + " - username: " + username);
                return username;
            } else if (commonAccounts.size() > 1) {
                // More than one account with the matching username has been found
                // It might be that only one account is really for the user, and the
                // other are group/system accounts
                // Check whether only one username contains the surname
                Iterator iter = commonAccounts.iterator();
                String matchingAccount = null;
                while (iter.hasNext()) {
                    String account = (String) iter.next();
                    if (account.indexOf(surname.toLowerCase()) != -1) {
                        if (matchingAccount == null) {
                            matchingAccount = account;
                        } else {
                            // Two accounts matched. Can't decide, return null.
                            log.trace("NIS account Name/Surname multiple match, multiple username with surname." +
                            " Name: " + name + " - Surname: " + surname + " - username: not defined");
                            resourceAdminLog.warn("NIS mapping: couldn't find single match for surname='" + surname + "' name='" + name + "'. Undecided between " + commonAccounts);
                            return null;
                        }
                    }
                }
                if (matchingAccount != null) {
                    // Only one matching account was found. There is a chance
                    // this is the right account
                    log.trace("NIS account Name/Surname multiple match, single username with surname." +
                    " Name: " + name + " - Surname: " + surname + " - username: " + matchingAccount);
                    return matchingAccount;
                }
                // Can't decide which account is the correct one
                log.trace("NIS account Name/Surname multiple match, no username with surname." +
                " Name: " + name + " - Surname: " + surname + " - username: not defined");
                resourceAdminLog.warn("NIS mapping: couldn't find single match for surname='" + surname + "' name='" + name + "'. Undecided between " + commonAccounts);
                return null;
            }
            // Common Accounts has no items, disregarding the name
        }
        if (accountsWithSurname != null) {
            if (accountsWithSurname.size() == 1) {
                String username = (String) accountsWithSurname.iterator().next();
                log.trace("NIS account Surname single match, no match on Name. Name: " + name + " - Surname: " + surname + " - username: " + username);
                return username;
            } else {
                log.trace("NIS account Surname multiple match, no match on Name. Name: " + name + " - Surname: " + surname + " - username: undefined");
                resourceAdminLog.warn("NIS mapping: couldn't find single match for surname='" + surname + "' name='" + name + "'. Undecided between " + accountsWithSurname);
                return null;
            }
        }
        log.trace("NIS account no match on Surname. Name: " + name + " - Surname: " + surname + " - username: undefined");
        // Try reversing name and surname
        accountsWithName = (Collection) nameToAccount.get(surname.toLowerCase());
        accountsWithSurname = (Collection) surnameToAccount.get(name.toLowerCase());
        if ((accountsWithName != null) && (accountsWithSurname != null)) {
            // Accounts for both name and surname were found
            List commonAccounts = new ArrayList(accountsWithName);
            commonAccounts.retainAll(accountsWithSurname);
            if (commonAccounts.size() == 1) {
                // Only one account matching both name and surname was found
                // Pretty likely is the correct one
                String username = (String) commonAccounts.get(0);
                log.trace("NIS account inverted Name/Surname single match. Name: " + surname + " - Surname: " + name + " - username: " + username);
                return username;
            }
        }
        return null;
    }
    
    private Date lastUpdate = null;
    private long expiration = 60*60*1000;
    public boolean isValid() {
        if (lastUpdate == null) return false;
        if ((System.currentTimeMillis() - lastUpdate.getTime()) > expiration) return false;
        return true;
    }
    
    private String retrieveName(String gecos) {
        gecos = gecos.trim();
        int comma = gecos.indexOf(',');
        if (comma != -1) {
            gecos = gecos.substring(0, comma);
        }
        int index = gecos.lastIndexOf(' ');
        if (index == -1) return "";
        return gecos.substring(0, gecos.indexOf(' '));
    }
    
    private String retrieveSurname(String gecos) {
        gecos = gecos.trim();
        int comma = gecos.indexOf(',');
        if (comma != -1) {
            gecos = gecos.substring(0, comma);
        }
        int index = gecos.lastIndexOf(' ');
        if (index == -1) return gecos;
        return gecos.substring(gecos.lastIndexOf(' ')+1);
    }
    
    public void addEntry(String username, String gecos) {
        String name = retrieveName(gecos);
        String surname = retrieveSurname(gecos);
        log.trace("Adding user '" + username + "': GECOS='" + gecos + "' name='" + name + "' surname='" + surname + "'");
        accountToGecos.put(username, gecos);
        if (name != null) {
            accountToName.put(username, name);
            nameToAccount.put(name.toLowerCase(), username);
        }
        accountToSurname.put(username, surname);
        surnameToAccount.put(surname.toLowerCase(), username);
        lastUpdate = new Date();
    }
    
    public void printMaps(PrintStream out) {
        out.println("username to gecos map");
        out.println("---------------------");
        out.println();
        Iterator accounts = accountToGecos.keySet().iterator();
        while (accounts.hasNext()) {
            String username = (String) accounts.next();
            String gecos = (String) accountToGecos.get(username);
            String name = (String) accountToName.get(username);
            String surname = (String) accountToSurname.get(username);
            out.print(username);
            for (int n = username.length(); n < 15; n++) {
                out.print(' ');
            }
            out.print(gecos);
            for (int n = gecos.length(); n < 30; n++) {
                out.print(' ');
            }
            out.print(name);
            for (int n = name.length(); n < 15; n++) {
                out.print(' ');
            }
            out.println(surname);
        }
    }
    
    private void printMap(PrintStream out, Map map, int offset) {
        Iterator accounts = map.keySet().iterator();
        while (accounts.hasNext()) {
            String username = (String) accounts.next();
            String gecos = (String) map.get(username);
            out.print(username);
            for (int n = username.length(); n < offset; n++) {
                out.print(' ');
            }
            out.println(gecos);
            
        }
    }

    /**
     * How long the map will be valid since the last change.
     * <p>
     * By default is 60*60*1000 (one hour).
     * @return Time of validity in ms.
     */
    public long getExpiration() {

        return this.expiration;
    }

    /**
     * How long the map will be valid since the last change.
     * @param expiration Time of validity in ms.
     */
    public void setExpiration(long expiration) {

        this.expiration = expiration;
    }
    
}
