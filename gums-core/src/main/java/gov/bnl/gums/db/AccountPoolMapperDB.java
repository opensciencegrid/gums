/*
 * AccountPoolMapperDB.java
 *
 * Created on June 14, 2004, 3:18 PM
 */

package gov.bnl.gums.db;

import gov.bnl.gums.GridUser;

import java.util.Date;
import java.util.List;
import java.util.Map;

/** 
 * Provides the set of accounts for the AccountPoolMapper class which
 * assignes accounts from a pool.
 * <p>
 * The implementation shouldn't buffer the information, as it the
 * AccountPoolMapper responsability to do so.
 *
 * @author Gabriele Carcassi, Jay Packard
 */
/**
 * @author jpackard
 *
 */
public interface AccountPoolMapperDB {
    
    /** 
     * Adds an account to the pool of free accounts. If the account
     * already exists, will throw an exception
     * 
     * @param account the account to be added
     */
    void addAccount(String account);
    
    /** 
     * Assigns a new account from the pool to the user. If the user is already
     * mapped, will throw an exception.
     * 
     * @todo decide which exception are thrown when
     * @param userDN the user to be mapped
     * @return the account or null if no more accounts are available
     */
    String assignAccount(GridUser user);
    
    /**
     * @return the map this mapper is responsible for
     */
    String getMap();
    
    /**
     * This is a function meant to be used by a wrapper class that is caching some result from the database.
     * When any writing operation occurs, this should return true until set to false by the wrapper class.
     * 
     * @return whether changes require a cache refresh
     */
    boolean needsCacheRefresh();
    
    /** 
     * Removes account from the pool of free accounts.
     * 
     * @param account the account to be removed
     * @return if account was removed
     */
    boolean removeAccount(String account);
    
    /** Retrieves the account associated to the Grid identity.
     * 
     * @param userDN the certificate DN
     * @param email
     * @return the account or null if the user wasn't mapped
     */
    String retrieveAccount(GridUser user);
    
    /** Retrieves a user to account map.
     * 
     * @return a Map between the userDN (String) as the key and the account (String).
     */
    Map retrieveAccountMap();
    
    /** 
     * Retrieves an account to user DN map, including null DNs, where empty strings are returned
     * if the account is unassigned.
     * 
     * @return a Map between the userDN (String) as the key and the account (String).
     */
    Map retrieveReverseAccountMap();

    /** 
     * Retrieve the list of accounts not in use since the given date.
     * 
     * @param date the time since the accounts haven't been used.
     * @return a list of String with the accounts
     */
    List retrieveUsersNotUsedSince(Date date);
    
    /**
     * Call when a wrapper class using the DB object has updated its cache.
     */
    void setCacheRefreshed();
    
    /** 
     * Unassigns whatever user is assigned to this account from the account mapping
     * and renders that account available to the pool.
     * 
     * @param account that should be unassigned
     */
    void unassignAccount(String account);
    
    /** 
     * Removes user from the mapping, and renders it available to the pool.
     * 
     * @param user the user that shouldn't be mapped anymore
     */
    void unassignUser(String user);
}
