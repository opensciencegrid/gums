/*
 * AccountPoolMapperDB.java
 *
 * Created on June 14, 2004, 3:18 PM
 */

package gov.bnl.gums.db;

import java.util.Date;
import java.util.List;
import java.util.Map;

/** Provides the set of accounts for the AccountPoolMapper class which
 * assignes accounts from a pool.
 * <p>
 * The implementation shouldn't buffer the information, as it the
 * AccountPoolMapper responsability to do so.
 *
 * @author  Gabriele Carcassi
 */
public interface AccountPoolMapperDB {
    
    /** Adds an account to the pool of free accounts.
     * @param account the account to be added
     */
    void addAccount(String account);
    
    /** Assigns a new account from the pool to the user. If the user is already
     * mapped, will throw an exception.
     * @todo decide which exception are thrown when
     * @param userDN the user to be mapped
     * @return the account or null if no more accounts are available
     */
    String assignAccount(String userDN);
    
    /**
     * Return number of unassigned mappings
     * @return
     */
    int getNumberUnassignedMappings();
    
    /** Retrieves the account associated to the Grid identity.
     * @param userDN the certificate DN
     * @return the account or null if the user wasn't mapped
     */
    String retrieveAccount(String userDN);
    
    /** Retrieves the accounts that are already mapped to a user.
     * @return a Map between the userDN (String) as the key and the account (String).
     */
    Map retrieveAccountMap();
    
    /** Retrieve the list of account not in use since the given date/
     * @param date the time since the accounts haven't been used.
     * @return a list of String with the accounts
     */
    List retrieveUsersNotUsedSince(Date date);

    /** Removes an account from the mapping, and renders it available to the pool.
     * @param user the user that shouldn't be mapped anymore
     */
    void unassignUser(String user);
}
