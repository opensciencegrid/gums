/*
 * ConfigurationStore.java
 *
 * Created on October 20, 2004, 12:47 PM
 */

package gov.bnl.gums.configuration;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;

/** 
 * Encapsulate the logic of retrieving the configuration from where it is stored.
 * This will allow to retrieve the configuration from a File, from a database,
 * or from whenever we will need to.
 *
 * @author Gabriele Carcassi, Jay Packard
 */
public interface ConfigurationStore {
    /**
     * Delete backup configuration
     * 
     * @param A date string
     */	
	void deleteBackupConfiguration(String dateStr);
	
    /**
     * Get a list of config date strings that have been backed up
     * 
     * @return collection of date strings.
     */
    Collection getBackupConfigDates();
    
    /**
     * Get last modified
     * 
     * @return Date
     */
    Date getLastModification();
    
    /**
     * Defines whether a configuration can be retrieved from the store.
     * This should only check whether configuration information is accessible,
     * not if it is inconsistent. For example, it should check whether
     * the configuration file is present, not if contains valid information.
     * 
     * @return true if the store is configured correctly.
     */
    boolean isActive();
    
    /**
     * Defines whether the configuration can be changed or not.
     * 
     * @return true if storeConfiguration is allowed.
     */
    boolean isReadOnly();
    
    /**
     * Defines whether the configuration needs to be reloaded.
     * 
     * @return true if configuration needs to be reloaded.
     */    
    boolean needsReload();
    
    /**
     * Restores configuration in memory. If the configuration cannot be loaded
     * due to an inconsistency in the store, it should throw an exception.
     * 
     * @param A date string
     * @return A configuration object.
     */
    Configuration restoreConfiguration(String dateStr) throws Exception;
    
    /**
     * Loads the configuration in memory. If the configuration cannot be loaded
     * due to an inconsistency in the store, it should throw an exception.
     * 
     * @return A configuration object.
     */
    Configuration retrieveConfiguration() throws Exception;
    
    /**
     * Set and store the configuration.  A configuration may specify to store the configuration
     * using another configuration store, so this function also returns itself or a updated 
     * configuration store
     * 
     * @param conf 
     * @param backupCopy 
     */
    void setConfiguration(Configuration conf, boolean backupCopy) throws Exception;
    
}
