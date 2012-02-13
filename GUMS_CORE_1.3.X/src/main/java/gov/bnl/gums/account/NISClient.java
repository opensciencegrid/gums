/*
 * NisClient.java
 *
 * Created on March 25, 2004, 4:11 PM
 */

package gov.bnl.gums.account;

import gov.bnl.gums.configuration.ReadWriteLock;
import gov.bnl.gums.GUMS;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.naming.NamingEnumeration;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchResult;

import org.apache.commons.collections.MultiHashMap;
import org.apache.commons.collections.MultiMap;
import org.apache.log4j.Logger;

/** 
 * Retrieves the map from the NIS server and provide a logic to match name and
 * surname to an account.
 *
 * @author  Gabriele Carcassi, Jay Packard
 */
public class NISClient {
    private Logger log = Logger.getLogger(NISClient.class);
    private Logger gumsAdminLog = Logger.getLogger(GUMS.gumsAdminLogName);
    private Map accountToGecos;
    private Map accountToName;
    private Map accountToSurname;
    private String nisJndiUrl;
    private MultiMap nameToAccount;
    private MultiMap surnameToAccount;
    private ReadWriteLock lock = new ReadWriteLock("nis");
    private Date lastUpdate = null;
    
    /**
     * Creates a new instance of NisClient
     * 
     * @param nisJndiUrl
     */
    public NISClient(String nisJndiUrl) {
        this.nisJndiUrl = nisJndiUrl;
    }
    
    /**
     * Find an account based on name and surname
     * 
     * @param name
     * @param surname
     * @return
     */
    public String findAccount(String name, String surname) {
        fillMaps();
        lock.obtainReadLock();
        try {
        log.trace("NIS findAccount. Name: " + name + " - Surname: " + surname + " - NIS: " + nisJndiUrl);
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
                String account = (String) commonAccounts.get(0);
                log.trace("NIS account Name/Surname single match. Name: " + name + " - Surname: " + surname + " - account: " + account);
                return account;
            } else if (commonAccounts.size() > 1) {
                // More than one account with the matching account has been found
                // It might be that only one account is really for the user, and the
                // other are group/system accounts
                // Check whether only one account contains the surname
                Iterator iter = commonAccounts.iterator();
                String matchingAccount = null;
                while (iter.hasNext()) {
                    String account = (String) iter.next();
                    if (account.indexOf(surname.toLowerCase()) != -1) {
                        if (matchingAccount == null) {
                            matchingAccount = account;
                        } else {
                            // Two accounts matched. Can't decide, return null.
                            log.trace("NIS account Name/Surname multiple match, multiple account with surname." +
                            " Name: " + name + " - Surname: " + surname + " - account: not defined");
                            gumsAdminLog.debug("NIS mapping: couldn't find single match for surname='" + surname + "' name='" + name + "'. Undecided between " + commonAccounts);
                            return null;
                        }
                    }
                }
                if (matchingAccount != null) {
                    // Only one matching account was found. There is a chance
                    // this is the right account
                    log.trace("NIS account Name/Surname multiple match, single account with surname." +
                    " Name: " + name + " - Surname: " + surname + " - account: " + matchingAccount);
                    return matchingAccount;
                }
                // Can't decide which account is the correct one
                log.trace("NIS account Name/Surname multiple match, no account with surname." +
                " Name: " + name + " - Surname: " + surname + " - account: not defined");
                gumsAdminLog.debug("NIS mapping: couldn't find single match for surname='" + surname + "' name='" + name + "'. Undecided between " + commonAccounts);
                return null;
            }
            // Common Accounts has no items, disregarding the name
        }
        if (accountsWithSurname != null) {
            if (accountsWithSurname.size() == 1) {
                String account = (String) accountsWithSurname.iterator().next();
                log.trace("NIS account Surname single match, no match on Name. Name: " + name + " - Surname: " + surname + " - account: " + account);
                return account;
            } else {
                log.trace("NIS account Surname multiple match, no match on Name. Name: " + name + " - Surname: " + surname + " - account: undefined");
                gumsAdminLog.debug("NIS mapping: couldn't find single match for surname='" + surname + "' name='" + name + "'. Undecided between " + accountsWithSurname);
                return null;
            }
        }
        log.trace("NIS account no match on Surname. Name: " + name + " - Surname: " + surname + " - account: undefined");
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
                String account = (String) commonAccounts.get(0);
                log.trace("NIS account inverted Name/Surname single match. Name: " + surname + " - Surname: " + name + " - account: " + account);
                return account;
            }
        }
        return null;
        } finally {
            lock.releaseReadLock();
        }
    }
    
    /**
     * Creates a new instance of NisClient
     * 
     * @param out
     */
    public void printMaps(PrintStream out) {
        fillMaps();
        out.println("account to gecos map");
        out.println("---------------------");
        out.println();
        Iterator accounts = accountToGecos.keySet().iterator();
        while (accounts.hasNext()) {
            String account = (String) accounts.next();
            String gecos = (String) accountToGecos.get(account);
            String name = (String) accountToName.get(account);
            String surname = (String) accountToSurname.get(account);
            out.print(account);
            for (int n = account.length(); n < 15; n++) {
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
    
    private void fillMaps() {
        if (mapsExpired()) {
            lock.obtainWriteLock();
            try {
            accountToSurname = new Hashtable();
            accountToName = new Hashtable();
            accountToGecos = new Hashtable();
            nameToAccount = new MultiHashMap();
            surnameToAccount = new MultiHashMap();
            fillMaps(retrieveJndiProperties(), accountToGecos, accountToName, accountToSurname);
            lastUpdate = new Date();
            gumsAdminLog.info("NIS map refreshed for '" + nisJndiUrl + "'");
            log.debug("NIS map refreshed for '" + nisJndiUrl + "'");
            } finally {
                lock.releaseWriteLock();
            }
        }
    }
    private void fillMaps(Properties jndiProperties, Map accountToGecos,
    Map accountToName, Map accountToSurname) {
        int nTries = 5;
        Exception lastException = null;
        int i = 0;
        for (; i < nTries; i++) {
            log.debug("Attemp " + i + " to retrieve map for '" + nisJndiUrl + "'");
            accountToGecos.clear();
            accountToName.clear();
            accountToSurname.clear();
            try {
                DirContext jndiCtx = new InitialDirContext(jndiProperties);
                NamingEnumeration map = jndiCtx.search("system/passwd.byname", "(cn=*)", null);
                log.trace("Server responded");
                while (map.hasMore()) {
                    SearchResult res = (SearchResult) map.next();
                    Attributes atts = res.getAttributes();
                    String account = (String) atts.get("cn").get();
                    Attribute gecosAtt = atts.get("gecos");
                    if (gecosAtt != null) {
                        String gecos = gecosAtt.get().toString();
                        String name = retrieveName(gecos);
                        String surname = retrieveSurname(gecos);
                        log.trace("Adding user '" + account + "': GECOS='" + gecos + "' name='" + name + "' surname='" + surname + "'");
                        accountToGecos.put(account, gecos);
                        if (name != null) {
                            accountToName.put(account, name);
                            nameToAccount.put(name.toLowerCase(), account);
                        }
                        accountToSurname.put(account, surname);
                        surnameToAccount.put(surname.toLowerCase(), account);
                    } else {
                        log.trace("Found user '" + account + "' with no GECOS field");
                    }
                }
                jndiCtx.close();
                return;
            } catch (javax.naming.NamingException ne) {
                log.warn("Error filling the maps for NIS "+nisJndiUrl, ne);
                lastException = ne;
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    log.warn("Interrupted", e);
                }
            } catch (Exception e) {
                log.warn("Error filling the maps for NIS "+nisJndiUrl, e);
                lastException = e;
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ie) {
                    log.warn("Interrupted", e);
                }
            }
        }
        if (i == nTries) {
            throw new RuntimeException("Couldn't retrieve NIS maps from " + nisJndiUrl, lastException);
        }
    }
    
    private boolean mapsExpired() {
        if (lastUpdate == null) return true;
        if ((System.currentTimeMillis() - lastUpdate.getTime()) > 60*60*1000) return true;
        return false;
    }
    
    private void printMap(PrintStream out, Map map, int offset) {
        Iterator accounts = map.keySet().iterator();
        while (accounts.hasNext()) {
            String account = (String) accounts.next();
            String gecos = (String) map.get(account);
            out.print(account);
            for (int n = account.length(); n < offset; n++) {
                out.print(' ');
            }
            out.println(gecos);
            
        }
    }
    
    private Properties retrieveJndiProperties() {
        Properties jndiProperties = new java.util.Properties();
        jndiProperties.put("java.naming.provider.url", nisJndiUrl);
        jndiProperties.put("java.naming.factory.initial","com.sun.jndi.nis.NISCtxFactory");
        return jndiProperties;
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
    
}
