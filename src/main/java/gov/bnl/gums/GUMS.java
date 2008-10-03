/*
 * GUMS.java
 *
 * Created on June 3, 2004, 10:39 AM
 */

package gov.bnl.gums;

import gov.bnl.gums.admin.CertCache;
import gov.bnl.gums.configuration.Configuration;
import gov.bnl.gums.configuration.ConfigurationStore;
import gov.bnl.gums.configuration.DBConfigurationStore;
import gov.bnl.gums.configuration.FileConfigurationStore;
import gov.bnl.gums.configuration.Version;
import gov.bnl.gums.persistence.PersistenceFactory;
import gov.bnl.gums.userGroup.ManualUserGroup;
import gov.bnl.gums.userGroup.UserGroup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Date;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.digester.Digester;
import org.apache.log4j.Logger;

/** 
 * Facade for the whole business logic available in GUMS. Using GUMS means
 * instanciating an object of this class, and use it to reach the rest of the
 * functionalities.
 *
 * @author  Gabriele Carcassi, Jay Packard
 */
public class GUMS {
    static final public String siteAdminLogName = "gums.siteAdmin";
    static final public String gumsAdminLogName = "gums.gumsAdmin";
    static private Logger log = Logger.getLogger(GUMS.class); // only use this log for particularly tricky aspects of this class - otherwise log within lower level classes
    static private Logger gumsAdminLog = Logger.getLogger(GUMS.gumsAdminLogName);
    static public QuietWarnLog gumsAdminEmailLog = new QuietWarnLog("gums.gumsAdminEmail");
    static private Timer timer;
    static private String version;
    
    private Configuration testConf;
    private CoreLogic resMan = new CoreLogic(this);
    protected ConfigurationStore confStore;
    protected DBConfigurationStore dbConfStore = null;
 
    /**
     * Create a thread that updates user group membership every so often
     * 
     * @param gums
     */
    static private synchronized void startWorkerThread(final GUMS gums) {
        // If JNDI property is set, run the update every x minutes
        if (timer == null) {
            try {
                Context env = (Context) new InitialContext().lookup("java:comp/env");
                Integer updateMinutes = (Integer) env.lookup("updateGroupsMinutes");
                Integer emailWarningHours = (Integer) env.lookup("emailWarningHours");
                if (updateMinutes != null) {
                	timer = new Timer();
                	
                    TimerTask updateTask = new TimerTask() {
                        public void run() {
                            try {
                                gumsAdminLog.info("Starting automatic updateGroups");
                                gums.getCoreLogic().updateGroups();
                                gumsAdminLog.info("Automatic updateGroups ended");
                            } catch (Exception e) {
                                gumsAdminLog.warn("Automatic group update had failures - " + e.getMessage());
                                gumsAdminEmailLog.put("updateUserGroup", e.getMessage(), false);
                            }
                        }
                    };
                    
                    TimerTask emailWarningTask = new TimerTask() {
                        public void run() {
                     	   if (gumsAdminEmailLog.hasMessages())
 	                    	   gumsAdminEmailLog.warn();
                        }
                    };      
                    
                    timer.scheduleAtFixedRate(updateTask, 0, updateMinutes.intValue()*60*1000);
                    gumsAdminLog.info("Automatic group update set: will refresh every " + updateMinutes.intValue() + " minutes starting now.");

                    timer.scheduleAtFixedRate(emailWarningTask, 5*60*1000, emailWarningHours.intValue()*60*60*1000);
                    gumsAdminLog.info("Automatic email warning set: will refresh every " + emailWarningHours.intValue() + " hours starting in 5 minutes.");
                } else {
                    gumsAdminLog.warn("Didn't start the automatic group update: 'updateGroupsMinutes' was set to null.");
                }
            } catch (NamingException e) {
                gumsAdminLog.warn("Didn't start the automatic updateGroups: " + e.getMessage());
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
        	String message = "Couldn't read GUMS configuration file (gums.config)";
            gumsAdminLog.error(message);
            throw new RuntimeException(message);
        }
        
        startWorkerThread(this);
    }
    
    /**
     * Creates and initilializes a new instance of GUMS with a specified configuration store
     * 
     * @param confStore
     */
    public GUMS(ConfigurationStore confStore) {
        this.confStore = confStore;
        if (!confStore.isActive()) {
        	String message = "Couldn't read GUMS configuration file";
            gumsAdminLog.error(message);
            throw new RuntimeException(message);
        }
        
        startWorkerThread(this);
    }
    
    /**
     * Delete a backup configuration by date
     * 
     * @param dateStr
     */
    public void deleteBackupConfiguration(String dateStr) {
    	if (confStore.deleteBackupConfiguration(dateStr) );
    		gumsAdminLog.info("Deleted backup configuration "+dateStr+" (from file)");
    	if (dbConfStore!=null) 
    		if (dbConfStore.deleteBackupConfiguration(dateStr))
    			gumsAdminLog.info("Deleted backup configuration "+dateStr+" (from database)");
    }
    
    /**
     * Get a list of dates for which a backup gums.config exists
     * 
     * @return Collection of date strings
     */
    public Collection getBackupConfigDates() throws Exception {
    	if (dbConfStore != null)
    		 return dbConfStore.getBackupConfigDates();
    	else
        	return confStore.getBackupConfigDates();
    }

    /**
     * Retrieves the configuration being used by GUMS. The configuration might
     * change from one call to the other. Therefore, the business logic needs
     * to cache the value returned for the duration of a whole call, and not
     * further. 
     * 
     * @return current configuration or null.
     */
    public Configuration getConfiguration() throws Exception {
    	Configuration conf = null;
    	boolean needsReload = confStore.needsReload() || (dbConfStore!=null && dbConfStore.needsReload());
    	
    	// Load from most recent confStore and set other to be updated
    	ConfigurationStore confStoreToUpdate = null;
		if (dbConfStore!=null) {
			if (confStore.getLastModification().after(dbConfStore.getLastModification())) {
				String message = "Retrieving configuration from file configuration store";
				gumsAdminLog.debug(message);
				log.debug(message);
				conf = confStore.retrieveConfiguration(needsReload);
				confStoreToUpdate = dbConfStore;
			}
			else {
				String message = "Retrieving configuration from database configuration store";
				gumsAdminLog.debug("Retrieving configuration from database configuration store");
				log.debug(message);
				conf = dbConfStore.retrieveConfiguration(needsReload);
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
						if (storeConfigFound) {
							String message = "Configuration may only contain one persistence factory set to store the configuration";
							log.error(message);
							throw new RuntimeException(message);
						}
						String schemaPath = (confStore instanceof FileConfigurationStore) ? ((FileConfigurationStore)confStore).getSchemaPath() : null;
						dbConfStore = new DBConfigurationStore(persFact.retrieveConfigurationDB(), schemaPath);
						String message = "Added database configuration store for persistence factory "+persFact.getName();
						gumsAdminLog.debug(message);
						log.debug(message);
						storeConfigFound = true;
						try {
							if (confStore.getLastModification().after(dbConfStore.getLastModification()) &&
								(conf.getUserGroups().values().size()>0 || 
								conf.getAccountMappers().values().size()>0 ||
								conf.getGroupToAccountMappings().values().size()>0 || 
								conf.getHostToGroupMappings().size()>0 || 
								conf.getVomsServers().values().size()>0))
								dbConfStore.setConfiguration(conf, false);
						}
						catch(Exception e) {
							dbConfStore.setConfiguration(conf, false);
						}
					}
				}
			}
			
			// if no persistence factories are set to store the configuration,
			// eliminate db configuration store
			else if (dbConfStore!=null) {
				Iterator it = conf.getPersistenceFactories().values().iterator();
				boolean storeConfigFound = false;
				while (it.hasNext()) {
					PersistenceFactory persFact = (PersistenceFactory)it.next();
					if (persFact.getStoreConfig())
						storeConfigFound = true;
				}
				if (!storeConfigFound) {
					if (confStoreToUpdate==dbConfStore)
						confStoreToUpdate = null;
					dbConfStore = null;
					String message = "Eliminated database configuration store";
					gumsAdminLog.debug(message);
					log.debug(message);
				}
			}
			
			if (confStoreToUpdate!=null) {
				confStoreToUpdate.setConfiguration(conf, false);
				if (confStoreToUpdate instanceof DBConfigurationStore) {
					String message = "Updated Configuration in database";
					gumsAdminLog.debug(message);
					log.debug(message);
				}
				else if (confStoreToUpdate instanceof DBConfigurationStore) {
					String message = "Updated Configuration in database";
					gumsAdminLog.debug(message);
					log.debug(message);
				}
			}
		}
        return conf;
    }
    
    /**
     * Retrieve the ResourceManager to perform actions on the business logic.
     * 
     * @return the resource manager.
     */
    public CoreLogic getCoreLogic() {
        return resMan;
    }
    
    static public String getVersion() {
    	if (version==null) {
			String pomFile = CertCache.getMetaDir()+"/maven/gums/gums-service/pom.xml";
			Digester digester = new Digester();
			digester.addObjectCreate("project/version", Version.class);
			digester.addCallMethod("project/version","setVersion",0);
			Version versionCls = null;
			try {
				versionCls = (Version)digester.parse("file://"+pomFile);
			} catch (Exception e) {
				gumsAdminLog.error("Cannot get GUMS version from "+pomFile);
				return "?";
			}
			if (versionCls == null) {
				gumsAdminLog.error("Cannot get GUMS version from "+pomFile);
				return "?";
			}
			else
				gumsAdminLog.debug("Loaded GUMS version " + versionCls.getVersion() + " from '" + pomFile + "'" );
			version = versionCls.getVersion();
		}
		return version;
    }
    
    public boolean isUserBanned(GridUser user) throws Exception {
    	Configuration config = getConfiguration();
    	List bannedUserGroups = config.getBannedUserGroupList();
    	Iterator it = bannedUserGroups.iterator();
    	while (it.hasNext()) {
    		UserGroup userGroup = (ManualUserGroup)config.getUserGroup((String)it.next());
    		return userGroup.isInGroup(user);
    	}
    	return false;
    }
    
    /**
     * Restore a configuration from a certain date
     * 
     * @param dateStr
     */
    public void restoreConfiguration(String dateStr) throws Exception {
    	if (dbConfStore!=null && !dbConfStore.isReadOnly())
    		dbConfStore.restoreConfiguration(dateStr);
    	else if (!confStore.isReadOnly())
        	confStore.restoreConfiguration(dateStr);
        else {
        	throw new RuntimeException("Could not restore configuration because there are no writable configuration stores");
        }
    	gumsAdminLog.info("Restored configuration " + dateStr);
    }
    
    /**
     * Changes the configuration used by GUMS.
     * 
     * @param conf the new configuration
     */
    public void setConfiguration(Configuration conf, boolean backup) throws Exception {
    	// If backup configuration and there is a database configuration store, only backup in database
    	// If backup configuration and there is not database configuration store, backup on file
    	// If not backup configuration store on file and in database if there is a database configuration store
		if (backup) {
	        if (dbConfStore!=null) {
		        if (!dbConfStore.isReadOnly()) {
		        	dbConfStore.setConfiguration(conf, backup); 
		        	gumsAdminLog.info("Backed up configuration in database");
		        }
		        else {
		        	throw new RuntimeException("cannot write configuration to database because it is read-only");
		        }
	        }  			
	        else {
		        if (!confStore.isReadOnly()) {
		        	confStore.setConfiguration(conf, backup);
		        	gumsAdminLog.info("Backed up configuration on file");
		        }
		        else {
		        	throw new RuntimeException("cannot write configuration to file because it is read-only");
		        }
	        }
		}
		else {
	        if (!confStore.isReadOnly()) {
	        	confStore.setConfiguration(conf, backup);
	        	gumsAdminLog.info("Set configuration on file");
	        }
	        else {
	        	throw new RuntimeException("cannot write configuration to file because it is read-only");
	        }

	        if (dbConfStore!=null) {
		        if (!dbConfStore.isReadOnly()) {
		        	dbConfStore.setConfiguration(conf, backup);
		        	gumsAdminLog.info("Set configuration in database");
	        	}
		        else {
		        	throw new RuntimeException("cannot write configuration to database because it is read-only");
		        }
	        }			
		}
    }
   
}
