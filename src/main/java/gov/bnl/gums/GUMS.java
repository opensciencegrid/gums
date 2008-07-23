/*
 * GUMS.java
 *
 * Created on June 3, 2004, 10:39 AM
 */

package gov.bnl.gums;

import gov.bnl.gums.configuration.Configuration;
import gov.bnl.gums.configuration.ConfigurationStore;
import gov.bnl.gums.configuration.DBConfigurationStore;
import gov.bnl.gums.configuration.FileConfigurationStore;
import gov.bnl.gums.persistence.PersistenceFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.logging.*;

/** 
 * Facade for the whole business logic available in GUMS. Using GUMS means
 * instanciating an object of this class, and use it to reach the rest of the
 * functionalities.
 *
 * @author  Gabriele Carcassi, Jay Packard
 */
public class GUMS {
    static final public String siteAdminLog = "gums.siteAdmin";
    static final public String resourceAdminLog = "gums.resourceAdmin";
    static private Log log = LogFactory.getLog(GUMS.class);
    static private Log gumsResourceAdminLog = LogFactory.getLog(GUMS.resourceAdminLog);
    static private Timer timer;
 
    private Configuration testConf;
    private ResourceManager resMan = new ResourceManager(this);
    protected ConfigurationStore confStore;
    protected DBConfigurationStore dbConfStore = null;
 
    /**
     * Create a thread that updates user group membership every so often
     * 
     * @param gums
     */
    static private synchronized void startUpdateThread(final GUMS gums) {
        // If JNDI property is set, run the update every x minutes
        if (timer == null) {
            try {
                Context env = (Context) new InitialContext().lookup("java:comp/env");
                Integer minutes = (Integer) env.lookup("updateGroupsMinutes");
                if (minutes != null) {
                    timer = new Timer();
                    TimerTask updateTask = new TimerTask() {
                        public void run() {
                            try {
                                gumsResourceAdminLog.info("Starting automatic updateGroups");
                                gums.getResourceManager().updateGroups();
                                gumsResourceAdminLog.info("Automatic updateGroups ended");
                            } catch (Exception e) {
                                gumsResourceAdminLog.error("Automatic updateGroups failed - " + e.getMessage());
                                log.info("Automatic updateGroups failed", e);
                            }
                        }
                    };
                    timer.scheduleAtFixedRate(updateTask, 0, minutes.intValue()*60*1000);
                    gumsResourceAdminLog.info("Automatic updateGroups set: will refresh every " + minutes.intValue() + " minutes starting now.");
                } else {
                    gumsResourceAdminLog.warn("Didn't start the automatic updateGroups: 'updateGroupsMinutes' was set to null.");
                }
            } catch (NamingException e) {
                gumsResourceAdminLog.warn("Didn't start the automatic updateGroups: " + e.getMessage());
                log.warn("Couldn't read JNDI property: " + e.getMessage(), e);
            }
        }
    }
    
    /**
     * Creates and initilializes a new instance of GUMS (should only be used for testing).
     */
    public GUMS(Configuration conf) {
    	this.testConf = conf;
    }
    
    /**
     * Creates and initilializes a new instance of GUMS
     */
    public GUMS() {
    	confStore = new FileConfigurationStore();
        if (!confStore.isActive()) {
            gumsResourceAdminLog.fatal("Couldn't read GUMS policy file (gums.config)");
        }
        
        startUpdateThread(this);
    }
    
    /**
     * Creates and initilializes a new instance of GUMS with a specified configuration store
     * 
     * @param confStore
     */
    public GUMS(ConfigurationStore confStore) {
        this.confStore = confStore;
        if (!confStore.isActive()) {
            gumsResourceAdminLog.fatal("Couldn't read GUMS policy file (gums.config)");
        }
        
        startUpdateThread(this);
    }
    
    /**
     * Delete a backup configuration by date
     * 
     * @param dateStr
     */
    public void deleteBackupConfiguration(String dateStr) {
    	confStore.deleteBackupConfiguration(dateStr);	
    	if (dbConfStore!=null) 
    		dbConfStore.deleteBackupConfiguration(dateStr);	
    }
    
    /**
     * Get a list of dates for which a backup gums.config exists
     * 
     * @return Collection of date strings
     */
    public Collection getBackupConfigDates() {
    	Collection backupConfigDates = confStore.getBackupConfigDates();
    	if (dbConfStore != null)
    		backupConfigDates.add( dbConfStore.getBackupConfigDates() );
        return backupConfigDates;
    }

    /**
     * Retrieves the configuration being used by GUMS. The configuration might
     * change from one call to the other. Therefore, the business logic needs
     * to cache the value returned for the duration of a whole call, and not
     * further. 
     * 
     * @return current configuration or null.
     */
    public Configuration getConfiguration() {
    	Configuration conf = null;
        try {
        	boolean needsReload = confStore.needsReload() || (dbConfStore!=null && dbConfStore.needsReload());
        	
        	ConfigurationStore confStoreToUpdate = null;
			if (dbConfStore!=null) {
				if (confStore.getLastModification().after(dbConfStore.getLastModification())) {
					conf = confStore.retrieveConfiguration();
					confStoreToUpdate = dbConfStore;
				}
				else {
					conf = dbConfStore.retrieveConfiguration();
					confStoreToUpdate = confStore;
				}
			}
			else
				conf = confStore.retrieveConfiguration();
			
			if (needsReload) {
				// if a persistence factory is set to store the configuration,
				// also create a db configuration store
				if (dbConfStore==null) {
					Iterator it = conf.getPersistenceFactories().values().iterator();
					boolean storeConfigFound = false;
					while (it.hasNext()) {
						PersistenceFactory persFact = (PersistenceFactory)it.next();
						if (persFact.getStoreConfig()) {
							if (storeConfigFound)
								throw new RuntimeException("Configuration may only contain one persistence factory set to store the configuration");
							String schemaPath = (confStore instanceof FileConfigurationStore) ? ((FileConfigurationStore)confStore).getSchemaPath() : null;
							dbConfStore = new DBConfigurationStore(persFact.retrieveConfigurationDB(), schemaPath);
							dbConfStore.setConfiguration(conf, false);
							storeConfigFound = true;
						}
					}
				}
				
				// if no persistence factories are set to store the configuration,
				// eliminate db persistence factory
				if (dbConfStore!=null) {
					Iterator it = conf.getPersistenceFactories().values().iterator();
					boolean storeConfigFound = false;
					while (it.hasNext()) {
						PersistenceFactory persFact = (PersistenceFactory)it.next();
						if (persFact.getStoreConfig())
							storeConfigFound = true;
					}
					if (!storeConfigFound)
						dbConfStore = null;
				}
				
				if (confStoreToUpdate!=null)
					confStoreToUpdate.setConfiguration(conf, false);

			}
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
        return conf;
    }
    
    /**
     * Retrieve the ResourceManager to perform actions on the business logic.
     * 
     * @return the resource manager.
     */
    public ResourceManager getResourceManager() {
        return resMan;
    }
    
    /**
     * Restore a configuration from a certain date
     * 
     * @param dateStr
     */
    public void restoreConfiguration(String dateStr) {
    	try {
	        if (!confStore.isReadOnly()) {
	        	Configuration newConf = confStore.restoreConfiguration(dateStr);
	        	if (newConf==null && dbConfStore!=null)
	        		newConf = dbConfStore.restoreConfiguration(dateStr);
	        	if (newConf==null)
	        		throw new RuntimeException("Configuration from "+dateStr+" does not exist");
	        }
	        else
	        	throw new RuntimeException("cannot write configuration to file because it is read-only");
    	} catch(Exception e) {
    		throw new RuntimeException("cannot write configuration: " + e.getMessage());
    	}   	
    }
    
    /**
     * Changes the configuration used by GUMS.
     * 
     * @param conf the new configuration
     */
    public void setConfiguration(Configuration conf, boolean backup) {
    	try {
	        if (!confStore.isReadOnly())
	        	confStore.setConfiguration(conf, backup);
	        else
	        	throw new RuntimeException("cannot write configuration to file because it is read-only");

	        if (dbConfStore!=null) {
		        if (!dbConfStore.isReadOnly()) 
		        	dbConfStore.setConfiguration(conf, backup); 
		        else
		        	throw new RuntimeException("cannot write configuration in DB because it is read-only");
	        }
    	} catch(Exception e) {
    		throw new RuntimeException("cannot write configuration: " + e.getMessage());
    	}
    }
   
}
