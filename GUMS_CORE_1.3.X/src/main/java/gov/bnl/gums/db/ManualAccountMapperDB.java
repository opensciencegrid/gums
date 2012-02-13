/*
 * ManualAccountMapperDB.java
 *
 * Created on May 25, 2004, 5:09 PM
 */

package gov.bnl.gums.db;

import java.util.List;
import java.util.Map;

/** The persistant layer for the ManualAccontMapper. Store a set of mappings
 * to be used by the ManualAccountMapper itself. This interface allows the 
 * mapping to be stored in different ways (i.e. LDAP, database, file, ...).
 * <P>
 * The persistance layer shouldn't be doing any kind of caching, which will be
 * handled by the ManualAccontMapper itself.
 *
 * @author Gabriele Carcassi, Jay Packard
 */
public interface ManualAccountMapperDB {
    /**
     * Saves in the DB the new mapping between the userDN and the account.
     * If a mapping for the given user is already present, an exception should
     * be thrown.
     * 
     * @todo should decide which excpetion to throw if the account was found,
     * and should modify the unit tests to test the error condition
     * @param userDN a certificate DN
     * @param account a UNIX account name
     */
    void createMapping(String userDN, String account);
    
    /**
     * Removes the mapping for the given user.
     * 
     * @param userDN a certificate DN
     * @return true if a mapping was deleted
     * @todo should probabily test the result value in unit tests
     */
    boolean removeMapping(String userDN);
    
    /**
     * Retrieves a user mapping from the database.
     * 
     * @param userDN a certificate DN
     * @return the UNIX account provided by the mapping
     */
    String retrieveMapping(String userDN);
    
    /**
     * Retrieves user to account map.
     * 
     * @return a Map object
     */
    public Map retrieveAccountMap();
 
    /**
     * Retrieves account to user map.
     * 
     * @return a Map object
     */
    public Map retrieveReverseAccountMap();
}
