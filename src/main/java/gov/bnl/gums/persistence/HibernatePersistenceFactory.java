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
import gov.bnl.gums.db.HibernateMappingDB;
import gov.bnl.gums.db.HibernateUserGroupDB;
import gov.bnl.gums.db.ManualAccountMapperDB;
import gov.bnl.gums.db.ManualUserGroupDB;
import gov.bnl.gums.db.UserGroupDB;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import net.sf.hibernate.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author Gabriele Carcassi, Jay Packard
 */
public class HibernatePersistenceFactory extends PersistenceFactory {
    static public String getTypeStatic() {
		return "hibernate";
	}
	
	private Log log = LogFactory.getLog(HibernatePersistenceFactory.class);
    private SessionFactory sessions;

	public HibernatePersistenceFactory() {
    	log.trace("HibernatePersistenceFactory instanciated");
    }
    
    public HibernatePersistenceFactory(Configuration configuration) {
    	super(configuration);
    	log.trace("HibernatePersistenceFactory instanciated");
    }    

    public HibernatePersistenceFactory(Configuration configuration, String name) {
    	super(configuration, name);
    	log.trace("HibernatePersistenceFactory instanciated");
    }
    
    public PersistenceFactory clone(Configuration configuration) {
    	HibernatePersistenceFactory persistenceFactory = new HibernatePersistenceFactory(configuration, getName());
    	persistenceFactory.setDescription(getDescription());
    	persistenceFactory.setProperties((Properties)getProperties().clone());
    	return persistenceFactory;
    }
    
    public String getType() {
		return "hibernate";
	}
    
    public AccountPoolMapperDB retrieveAccountPoolMapperDB(String name) {
        return new HibernateMappingDB(this, name);
    }

    public ManualAccountMapperDB retrieveManualAccountMapperDB(String name) {
        return new HibernateMappingDB(this, name);
    }

    public ManualUserGroupDB retrieveManualUserGroupDB(String name) {
        return new HibernateUserGroupDB(this, name);
    }
    
    public synchronized SessionFactory retrieveSessionFactory() {
        if (sessions != null) return sessions;
        sessions = buildSessionFactory();
        return sessions;
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
    		"\t\t\tdescription='"+getDescription()+"'\n";
    	
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
    
    private SessionFactory buildSessionFactory() {
        try {
            log.trace("Creating Hibernate Session Factory with the following properties: " + getProperties());
            net.sf.hibernate.cfg.Configuration cfg = new net.sf.hibernate.cfg.Configuration()
            // Properties for the hibernate session are taken from the properties specified either in the
            // gums.config file, or set programmatically to the class (when unit testing)
                .setProperties(getProperties())
                .addClass(gov.bnl.gums.db.HibernateMapping.class)
                .addClass(gov.bnl.gums.db.HibernateUser.class);
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
