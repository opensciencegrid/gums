/*
 * GUMS.java
 *
 * Created on June 3, 2004, 10:39 AM
 */

package gov.bnl.gums;

import gov.bnl.gums.configuration.Configuration;
import gov.bnl.gums.configuration.ConfigurationStore;
import gov.bnl.gums.configuration.FileConfigurationStore;

import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.logging.*;

/** Facade for the whole business logic available in GUMS. Using GUMS means
 * instanciating an object of this class, and use it to reach the rest of the
 * functionalities.
 *
 * @author  Gabriele Carcassi, Jay Packard
 */
public class GUMS {
    static final public String siteAdminLog = "gums.siteAdmin";
    static final public String resourceAdminLog = "gums.resourceAdmin";
    static final public String version = "1.2";
    static private Log log = LogFactory.getLog(GUMS.class);
    static private Log gumsResourceAdminLog = LogFactory.getLog(GUMS.resourceAdminLog);
    static private Timer timer;
    
    static public String getVersion() {
    	return version;
    }
    
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
    
    private Configuration conf;
    private ResourceManager resMan = new ResourceManager(this);
    protected ConfigurationStore confStore;
    
    /** Creates and initilializes a new instance of GUMS. */
    public GUMS() {
        confStore = new FileConfigurationStore();
        if (!confStore.isActive()) {
            gumsResourceAdminLog.fatal("Couldn't read GUMS policy file (gums.config)");
        }
        
        startUpdateThread(this);
    }
    
    /** @todo This constructor should probably go away... */
    public GUMS(Configuration conf) {
        this.conf = conf;
    }
    
    public GUMS(ConfigurationStore confStore) {
        this.confStore = confStore;
        if (!confStore.isActive()) {
            gumsResourceAdminLog.fatal("Couldn't read GUMS policy file (gums.config)");
        }
        
        startUpdateThread(this);
    }
    
    public void deleteBackupConfiguration(String dateStr) {
    	if (confStore != null) confStore.deleteBackupConfiguration(dateStr);	
    }
    
    public Collection getBackupConfigDates() {
    	if (confStore != null) return confStore.getBackupConfigDates();
        return null;
    }

    /**
     * Retrieves the configuration being used by GUMS. The configuration might
     * change from one call to the other. Therefore, the business logic needs
     * to cache the value returned for the duration of a whole call, and not
     * further. 
     * @return the current configuration or null.
     */
    public Configuration getConfiguration() {
        if (confStore != null) return confStore.retrieveConfiguration();
        return conf;
    }
    
    /**
     * Retrieve the ResourceManager to perform actions on the business logic.
     * @return the resource manager.
     */
    public ResourceManager getResourceManager() {
        return resMan;
    }
    
    public void restoreConfiguration(String dateStr) {
    	try {
	        this.conf = conf;
	        if (!confStore.isReadOnly()) {
	            confStore.restoreConfiguration(dateStr);
	        }
	        else
	        	throw new RuntimeException("cannot write configuration because it is read-only");
    	} catch(Exception e) {
    		throw new RuntimeException("cannot write configuration: " + e.getMessage());
    	}   	
    }
    
    /**
     * Changes the configuration used by GUMS.
     * <p>
     * @todo should this method actually save the configuration?
     * @param conf the new configuration
     */
    public void setConfiguration(Configuration conf, boolean backup) {
    	try {
	        this.conf = conf;
	        if (!confStore.isReadOnly()) {
	            confStore.setConfiguration(conf, backup);
	        }
	        else
	        	throw new RuntimeException("cannot write configuration because it is read-only");
    	} catch(Exception e) {
    		throw new RuntimeException("cannot write configuration: " + e.getMessage());
    	}
    }
   
}
