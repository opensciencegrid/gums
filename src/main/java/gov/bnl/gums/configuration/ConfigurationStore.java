/*
 * ConfigurationStore.java
 *
 * Created on October 20, 2004, 12:47 PM
 */

package gov.bnl.gums.configuration;

import gov.bnl.gums.GUMS;
import gov.bnl.gums.GridUser;
import gov.bnl.gums.persistence.PersistenceFactory;
import gov.bnl.gums.userGroup.ManualUserGroup;
import gov.bnl.gums.userGroup.UserGroup;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;

/** 
 * Encapsulate the logic of retrieving the configuration from where it is stored.
 * This will allow to retrieve the configuration from a File, from a database,
 * or from whenever we will need to.
 *
 * @author Gabriele Carcassi, Jay Packard
 */
public abstract class ConfigurationStore {
	static protected DateFormat format = new SimpleDateFormat("yyyy_MM_dd_HHmmss");
	
    /**
     * Delete backup configuration
     * 
     * @param A date string
     */	
	public abstract void deleteBackupConfiguration(String name);
	
    /**
     * Get a list of config date strings that have been backed up
     * 
     * @return collection of date strings.
     */
	public abstract Collection getBackupNames();
    
    /**
     * Get last modified
     * 
     * @return Date
     */
	public abstract Date getLastModification();
    
    /**
     * Defines whether a configuration can be retrieved from the store.
     * This should only check whether configuration information is accessible,
     * not if it is inconsistent. For example, it should check whether
     * the configuration file is present, not if contains valid information.
     * 
     * @return true if the store is configured correctly.
     */
	public abstract boolean isActive();
    
    /**
     * Restores configuration in memory. If the configuration cannot be loaded
     * due to an inconsistency in the store, it should throw an exception.
     * 
     * @param A date string
     * @return A configuration object.
     */
	public abstract Configuration restoreConfiguration(String dateStr) throws Exception;
    
    /**
     * Loads the configuration in memory if or from storage based on reload. This is
     * useful if needsReload was called previously and you don't want to look it
     * up again for performance's sake.  If the configuration cannot be loaded due to an 
     * inconsistency in the store, it should throw an exception.
     * 
     * @return A configuration object.
     */
	public abstract Configuration retrieveConfiguration() throws Exception;
    
    /**
     * Set and store the configuration.  A configuration may specify to store the configuration
     * using another configuration store, so this function also returns itself or a updated 
     * configuration store
     * 
     * @param conf 
     * @param backupCopy 
     */
	public abstract void setConfiguration(Configuration conf, boolean backupCopy, String name, Date date) throws Exception;

	static public DateFormat getFormat() {
		return format;
	}
    
}
