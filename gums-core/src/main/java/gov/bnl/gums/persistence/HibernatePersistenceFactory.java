/*
 * HibernatePersistenceFactory.java
 *
 * Created on June 15, 2005, 4:42 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package gov.bnl.gums.persistence;

import gov.bnl.gums.configuration.Configuration;
import gov.bnl.gums.db.AccountPoolMapperDB;
import gov.bnl.gums.db.ConfigurationDB;
import gov.bnl.gums.db.HibernateAccountMapperDB;
import gov.bnl.gums.db.HibernateUserGroupDB;
import gov.bnl.gums.db.HibernateConfigurationDB;
import gov.bnl.gums.db.ManualAccountMapperDB;
import gov.bnl.gums.db.ManualUserGroupDB;
import gov.bnl.gums.db.UserGroupDB;

import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

import org.hibernate.*;

import org.apache.log4j.Logger;

/**
 *
 * @author Gabriele Carcassi, Jay Packard
 */
public class HibernatePersistenceFactory extends PersistenceFactory {
    static public String getTypeStatic() {
		return "hibernate";
	}
    static final int expireTime = 300000;
	static Map propertyToPFSessionCreatorMap = new HashMap();
	static Configuration currentConfig = null;
    static int numSessions = 0;
    private Timer timer = new Timer();
	
	private Logger log = Logger.getLogger(HibernatePersistenceFactory.class);
	SessionFactory sessionFactory;
    
	TimerTask closeExpiredSessions = new TimerTask() {
		public void run() {
			synchronized(propertyToPFSessionCreatorMap) {
				Iterator it = propertyToPFSessionCreatorMap.keySet().iterator();
				while (it.hasNext()) {
					Properties p = (Properties)it.next();
					HibernatePersistenceFactory pf = (HibernatePersistenceFactory)propertyToPFSessionCreatorMap.get(p);
					if (pf.getConfiguration() != currentConfig && new Date().after(new Date(pf.getConfiguration().getCreated().getTime()+expireTime))) {
						pf.sessionFactory.close();
						propertyToPFSessionCreatorMap.remove(p);it = propertyToPFSessionCreatorMap.keySet().iterator();
						numSessions--;
			            log.debug("Closed Hibernate session factory "+pf.sessionFactory+" with configuration "+pf.getConfiguration()+" and properties " + p + " - "+numSessions+" current instance(s)");
					}
				}
			}
		}
	};
	
	/**
     * Create a new hibernate persistence factory.  This empty constructor is needed by the XML Digester.
	 */
	public HibernatePersistenceFactory() {
    	log.trace("HibernatePersistenceFactory instanciated");
    	timer.scheduleAtFixedRate(closeExpiredSessions, new Date(new Date().getTime()+expireTime), expireTime);
    }
    
    /**
     * Create a new hibernate persistence factory with a configuration.
     * 
     * @param configuration
     */
    public HibernatePersistenceFactory(Configuration configuration) {
    	super(configuration);
    	log.trace("HibernatePersistenceFactory instanciated");
    	timer.scheduleAtFixedRate(closeExpiredSessions, new Date(new Date().getTime()+expireTime), expireTime);
    }    

    /**
     * Create a new ldap persistence factory with a configuration and a name.
     * 
     * @param configuration
     * @param name
     */
    public HibernatePersistenceFactory(Configuration configuration, String name) {
    	super(configuration, name);
    	log.trace("HibernatePersistenceFactory instanciated");
    	timer.scheduleAtFixedRate(closeExpiredSessions, new Date(new Date().getTime()+60000), 60000);
    }
    
    public PersistenceFactory clone(Configuration configuration) {
    	HibernatePersistenceFactory persistenceFactory = new HibernatePersistenceFactory(configuration, new String(getName()));
    	persistenceFactory.setDescription(new String(getDescription()));
    	persistenceFactory.setStoreConfig(getStoreConfig());
    	persistenceFactory.setProperties((Properties)getProperties().clone());
    	return persistenceFactory;
    }
    
    public String getType() {
		return "hibernate";
	}
    
    public AccountPoolMapperDB retrieveAccountPoolMapperDB(String name) {
        return new HibernateAccountMapperDB(this, name);
    }

	public ConfigurationDB retrieveConfigurationDB() {
		return new HibernateConfigurationDB(this);
	}	
    
    public ManualAccountMapperDB retrieveManualAccountMapperDB(String name) {
        return new HibernateAccountMapperDB(this, name);
    }

    public ManualUserGroupDB retrieveManualUserGroupDB(String name) {
        return new HibernateUserGroupDB(this, name);
    }
    
    public UserGroupDB retrieveUserGroupDB(String name) {
        return new HibernateUserGroupDB(this, name);
    }
    
    public void setConnectionFromHibernateProperties() {
        try {
            setProperties(readHibernateProperties());
        } catch (MissingResourceException e) {
            throw new RuntimeException("Couldn't find database configuration file (hibernate.properties)", e);
        }
    }
    
	public String toXML() {
    	String retStr = "\t\t<hibernatePersistenceFactory\n"+
    		"\t\t\tname='"+getName()+"'\n"+
    		"\t\t\tdescription='"+getDescription()+"'\n"+
			"\t\t\tstoreConfig='"+(getStoreConfig()?"true":"false")+"'\n";
    	
    	Iterator keyIt = getProperties().keySet().iterator();
    	while(keyIt.hasNext()) {
    		String key = (String)keyIt.next();
    		retStr += "\t\t\t"+key+"='"+getProperties().getProperty(key)+"'\n";
    	}

    	if (retStr.charAt(retStr.length()-1)=='\n')
    		retStr = retStr.substring(0, retStr.length()-1);    	
    	
    	retStr += "/>\n\n";
    	
    	return retStr;
	}

	public SessionFactory retrieveSessionFactory() throws Exception {
		if (sessionFactory == null) {
			synchronized(propertyToPFSessionCreatorMap) {
				// If there's a hibernate persistence factory with the same properties, use it, 
				// otherwise create a new one and register in propertyToPFSessionCreatorMap list
				HibernatePersistenceFactory pf = ((HibernatePersistenceFactory)propertyToPFSessionCreatorMap.get(getProperties()));
				if (pf!=null && !pf.sessionFactory.isClosed()) {
					sessionFactory = pf.sessionFactory;
					log.debug("Obtained previous hibernate session factory "+sessionFactory+" with configuration "+getConfiguration()+" and properties " + pf.getProperties() + " - "+numSessions+" current instance(s)");
				}
				else {
					sessionFactory = buildSessionFactory();
					numSessions++;
		            log.debug("Created new Hibernate session factory "+sessionFactory+" with configuration "+getConfiguration()+" and properties " + getProperties() + " - "+numSessions+" current instance(s)");
				}
				propertyToPFSessionCreatorMap.put(getProperties(), this);
				currentConfig = getConfiguration();
			}
		}
		
		return sessionFactory;
	}
	
    private SessionFactory buildSessionFactory() {

        try {
            log.trace("Creating Hibernate Session Factory with the following properties: " + getProperties());
            // Properties for the hibernate session are taken from the properties specified either in the
            // gums.config file, or set programmatically to the class (when unit testing)
            org.hibernate.cfg.Configuration cfg = new org.hibernate.cfg.Configuration()
                .setProperties(getProperties())
                .addClass(gov.bnl.gums.db.HibernateMapping.class)
                .addClass(gov.bnl.gums.db.HibernateUser.class)
                .addClass(gov.bnl.gums.db.HibernateConfig.class);
            return cfg.buildSessionFactory();
        } catch (Exception e) {
            log.error("Couldn't initialize Hibernate", e);
            throw new RuntimeException("An error occurred while initializing the database environment (hibernate): "+ e.getMessage(), e);
        }
    }
    
    private Properties readHibernateProperties() {
        log.trace("Retrieving hibernate properties from hibernate.properties in the classpath");
        PropertyResourceBundle prop = (PropertyResourceBundle) ResourceBundle.getBundle("hibernate");
        Properties prop2 = new Properties();
        Enumeration keys = prop.getKeys();
        while (keys.hasMoreElements()) {
            String key = (String) keys.nextElement();
            prop2.setProperty(key, prop.getString(key));
        }
        return prop2;
    }
    
}
