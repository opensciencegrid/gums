package gov.bnl.gums.db;

import java.text.DateFormat;
import java.util.Collection;
import java.util.Date;

public interface ConfigurationDB {
    /**
     * Delete backup configuration
     * 
     * @param A name string
     */	
	public boolean deleteBackupConfiguration(String name);
	
    /**
     * Get a list of config date strings that have been stored
     * 
     * @return collection of date strings.
     */	
	public Collection getBackupNames(DateFormat format);
	
    /**
     * Get last modified
     * 
     * @return Date
     */
    public Date getLastModification();
	
    /**
     * Defines whether a configuration can be retrieved from the database.
     * This should only check whether configuration information is accessible,
     * not if it is inconsistent. For example, it should check whether
     * the configuration file is present, not if contains valid information.
     * 
     * @return true if the store is configured correctly.
     */	
	public boolean isActive();
	
    /**
     * Restores configuration in memory. If the configuration cannot be loaded
     * due to an inconsistency in the store, it should throw an exception.
     * 
     * @param A name string
     * @return configuration text.
     */	
	public String restoreConfiguration(String name);
	
    /**
     * Loads the configuration text. If the configuration cannot be loaded
     * due to an inconsistency in the database, it should throw an exception.
     * 
     * @return configuration text.
     */	
	public String retrieveCurrentConfiguration();
	
    /**
     * Set and store the configuration.
     * 
     * @param text as configuration text
     * @param date as Date
     * @param backupCopy as boolean
     * @param name as name
     */	
	public void setConfiguration(String text, boolean backupCopy, String name, Date date);
}
