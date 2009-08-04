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
import gov.bnl.gums.userGroup.UserGroup;

import java.util.Date;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
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
	static public QuietLog gumsAdminEmailLog = new QuietLog("gums.gumsAdminEmail");
	static private Timer timer;
	static private String version;

	private CoreLogic resMan = new CoreLogic(this);
	protected ConfigurationStore confStore;
	protected DBConfigurationStore dbConfStore = null;
	protected Configuration lastConf = null;

	/**
	 * Create a thread that updates user group membership every so often
	 * 
	 * @param gums
	 */
	static private synchronized void startWorkerThread(final GUMS gums) {
		// If JNDI property is set, run the update every x minutes
		if (timer == null) {
			timer = new Timer();

			Integer updateMinutes = null;
			Integer updateBannedMinutes = null;
			Integer emailWarningHours = null;
			try {
				Context env = (Context) new InitialContext().lookup("java:comp/env");
				try {
					updateMinutes = (Integer) env.lookup("updateGroupsMinutes");
				} catch (Exception e) {}
				try {
					updateBannedMinutes = (Integer) env.lookup("updateBannedGroupsMinutes");
				} catch (Exception e) {}
				try {
					emailWarningHours = (Integer) env.lookup("emailWarningHours");
				} catch (Exception e) {}
			} catch (NamingException e) {
				log.warn("Couldn't set up JNDI context: " + e.getMessage(), e);
			}	
			
			if (updateMinutes != null) {
				TimerTask updateTask = 
					new TimerTask() {
					public void run() {
						synchronized(this) {
							try {
								gumsAdminLog.info("Starting automatic updateGroups");
								gums.getCoreLogic().updateGroups();
								gumsAdminLog.info("Automatic updateGroups ended");
							} catch (Exception e) {
								gumsAdminLog.warn("Automatic group update had failures - " + e.getMessage());
								gumsAdminEmailLog.put("updateUserGroup", e.getMessage(), false);
							}
						}
					}
				};

				timer.scheduleAtFixedRate(updateTask, 0, updateMinutes.intValue()*60*1000);
				gumsAdminLog.info("Automatic group update set: will refresh every " + updateMinutes.intValue() + " minutes starting now.");
			}
			else {
				gumsAdminLog.warn("Didn't start the automatic group update: 'updateGroupsMinutes' was set to null.");
			}
			
			if (updateBannedMinutes != null) {
				TimerTask updateBannedTask = new TimerTask() {
					public void run() {
						synchronized(this) {
							try {
								gumsAdminLog.info("Starting automatic updateBannedGroups");
								gums.getCoreLogic().updateBannedGroups();
								gumsAdminLog.info("Automatic updateBannedGroups ended");
							} catch (Exception e) {
								gumsAdminLog.warn("Automatic banned group update had failures - " + e.getMessage());
								gumsAdminEmailLog.put("updateBannedGroups", e.getMessage(), false);
							}
						}
					}
				};

				timer.scheduleAtFixedRate(updateBannedTask, 0, updateBannedMinutes.intValue()*60*1000);
				gumsAdminLog.info("Automatic banned group update set: will refresh every " + updateBannedMinutes.intValue() + " minutes starting now.");
			}
			else {
				gumsAdminLog.warn("Didn't start the automatic banned group update: 'updateBannedMinutes' was set to null.");
			}			

			if (emailWarningHours != null) {
				TimerTask emailWarningTask = new TimerTask() {
					public void run() {
						synchronized(this) {
							if (gumsAdminEmailLog.hasMessages()) {
								gumsAdminEmailLog.logMessages();
							}
						}
					}
				};      

				timer.scheduleAtFixedRate(emailWarningTask, 5*60*1000, emailWarningHours.intValue()*60*60*1000);
				gumsAdminLog.info("Automatic email warning set: will refresh every " + emailWarningHours.intValue() + " hours starting in 5 minutes.");
			}
			else {
				gumsAdminLog.warn("Didn't start the email warning task: 'emailWarningTask' was set to null.");
			}
		}
	}

	/**
	 * Creates and initilializes a new instance of GUMS
	 */
	public GUMS() {
		confStore = new FileConfigurationStore();
		if (!confStore.isActive()) {
			String message = "Couldn't read GUMS configuration file (gums.config)";
			gumsAdminLog.error(message);
			gumsAdminEmailLog.put("Configuration", message, true);
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
			gumsAdminEmailLog.put("Configuration", message, true);
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
		if (dbConfStore!=null)  {
			dbConfStore.deleteBackupConfiguration(dateStr);
			gumsAdminLog.info("Deleted backup configuration "+dateStr+" from database");
		}
		else {
			confStore.deleteBackupConfiguration(dateStr);
			gumsAdminLog.info("Deleted backup configuration "+dateStr+" from file");
		}
	}

	/**
	 * Get a list of dates for which a backup gums.config exists
	 * 
	 * @return Collection of date strings
	 */
	public Collection getBackupNames() throws Exception {
		if (dbConfStore != null)
			return dbConfStore.getBackupNames();
		else
			return confStore.getBackupNames();
	}

	/**
	 * Retrieves the configuration being used by GUMS. The configuration might
	 * change from one call to the other. Therefore, the business logic needs
	 * to cache the value returned for the duration of a whole call, and not
	 * further. 
	 * 
	 * @return current configuration or null.
	 */
	public synchronized Configuration getConfiguration() throws Exception {
		Configuration conf;
		
		// Load from most recent confStore and update other
		if (dbConfStore != null) {
			Date confStoreLastMod = confStore.getLastModification();
			Date dbConfStoreLastMod = dbConfStore.getLastModification();
			if (confStoreLastMod.after(dbConfStoreLastMod)) {
				conf = confStore.retrieveConfiguration();
				if (lastConf != conf) {
					gumsAdminLog.info("Reloaded configuration "+conf+" from file, which was modified on " + confStoreLastMod + ", and setting to database");
					updateConfStoreTypes(conf);
					if (dbConfStore!=null)
						dbConfStore.setConfiguration(conf, false, null, confStoreLastMod);
					System.gc();
				}
			}
			else if (lastConf==null || dbConfStoreLastMod.after(confStoreLastMod)) {
				conf = dbConfStore.retrieveConfiguration();
				if (lastConf != conf) {
					gumsAdminLog.info("Reloaded configuration "+conf+" from database, which was modified on " + dbConfStoreLastMod + ", and setting to file");
					updateConfStoreTypes(conf);
					confStore.setConfiguration(conf, false, null, dbConfStoreLastMod);
					System.gc();
				}
			}
			else
				conf = lastConf;
		}
		else {
			conf = confStore.retrieveConfiguration();
			if (lastConf != conf) {
				gumsAdminLog.info("Reloaded configuration from file");
				updateConfStoreTypes(conf);
				System.gc();
			}
		}
		
		lastConf = conf;
		
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
				gumsAdminLog.warn("Cannot get GUMS version from "+pomFile);
				return "?";
			}
			if (versionCls == null) {
				gumsAdminLog.warn("Cannot get GUMS version from "+pomFile);
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
			UserGroup userGroup = config.getUserGroup((String)it.next());
			return userGroup.isInGroup(user);
		}
		return false;
	}

	public void mergeConfiguration(Configuration configuration, String persistenceFactory, String hostToGroupMapping) throws Exception
	{
		getConfiguration().mergeConfiguration(configuration, persistenceFactory, hostToGroupMapping);
	}

	/**
	 * Restore a configuration from a certain date
	 * 
	 * @param dateStr
	 */
	public void restoreConfiguration(String name) throws Exception {
		if (dbConfStore!=null)
		{
			dbConfStore.restoreConfiguration(name);
			gumsAdminLog.info("Restored configuration from db: " + name);
		}
		else
		{
			confStore.restoreConfiguration(name);
			gumsAdminLog.info("Restored configuration from file: " + name);
		}
	}

	/**
	 * Changes the configuration used by GUMS.
	 * 
	 * @param conf the new configuration
	 */
	public void setConfiguration(Configuration conf, boolean backup, String name) throws Exception {
		Date date = new Date();
		if (backup) {
			if (dbConfStore!=null) {
				dbConfStore.setConfiguration(conf, true, name, date); 
				gumsAdminLog.info("Backed up configuration in database: "+name);
			}  			
			else {
				confStore.setConfiguration(conf, true, name, date);
				gumsAdminLog.info("Backed up configuration on file: "+name);
			}
		}
		else {
			confStore.setConfiguration(conf, false, null, date);
			gumsAdminLog.info("Set configuration on file");
		}
	}

	private void updateConfStoreTypes(Configuration conf) {
		Iterator it = conf.getPersistenceFactories().values().iterator();
		boolean storeConfigFound = false;
		while (it.hasNext()) {
			PersistenceFactory persFact = (PersistenceFactory)it.next();
			if (persFact.getStoreConfig()) {
				// create database configuration store
				if (storeConfigFound) {
					String message = "Configuration may only contain one persistence factory set to store the configuration";
					log.error(message);
					throw new RuntimeException(message);
				}
				dbConfStore = new DBConfigurationStore(persFact.retrieveConfigurationDB());
				String message = "Created database configuration store for persistence factory "+persFact.getName();
				gumsAdminLog.debug(message);
				log.debug(message);
				storeConfigFound = true;
			}
		}
		
		if (!storeConfigFound) {
			// eliminate database configuration store 
			if (dbConfStore!=null) {
				String message = "Eliminated database configuration store";
				gumsAdminLog.debug(message);
				log.debug(message);
			}
			dbConfStore = null;
		}
	}

}
