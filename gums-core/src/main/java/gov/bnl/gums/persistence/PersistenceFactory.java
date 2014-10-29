/*
 * PersistanceManager.java
 *
 * Created on June 3, 2004, 4:46 PM
 */

package gov.bnl.gums.persistence;

import gov.bnl.gums.configuration.Configuration;
import gov.bnl.gums.db.AccountPoolMapperDB;
import gov.bnl.gums.db.ManualAccountMapperDB;
import gov.bnl.gums.db.ManualUserGroupDB;
import gov.bnl.gums.db.UserGroupDB;
import gov.bnl.gums.db.ConfigurationDB;

import java.lang.ref.SoftReference;
import java.util.Properties;

/** Represent a factory for all the classes that take care of the persistance of
 * the AccountMappers, the UserGroups or any other components.
 * <p>
 * Implementing a new PersistanceManager class allows to complete redefined how
 * the user, group and accounting information is stored. This will allow to keep
 * the information integrated in the site accounting system, being it any RDBMS,
 * LDAP, or even a combination of these.
 * <p>
 * It also allow to use different mapping technologies, allowing to use either
 * entity beans, simple JDBC, Hibernate or any other favorite technology.
 *
 * @author Gabriele Carcassi, Jay Packard
 */
public abstract class PersistenceFactory {
    /**
     * @return user friendly string representation of the property type called statically 
     */
    static public String getTypeStatic() {
		return "abstract";
	}
    
    private String name = "";
    private boolean storeConfig = false;
    private String description = "";
    private Properties properties;
    private SoftReference configurationRef;
    
	/**
	 * Create a persistence factory. This empty constructor is needed by the XML Digestor.
	 */
	public PersistenceFactory() {
	}
    
	/**
	 * Create a persistence factory with a configuration.
	 * 
	 * @param configuration
	 */
	public PersistenceFactory(Configuration configuration) {
		this.configurationRef = new SoftReference(configuration);
	}	
    
	/**
	 * Create a persistence factory with a configuration and a name.
	 * 
	 * @param configuration
	 * @param name
	 */
	public PersistenceFactory(Configuration configuration, String name) {
		this.name = name;
		this.configurationRef = new SoftReference(configuration);
	}
	
	/**
	 * Create a clone of itself
	 * 
	 * @param configuration
	 * @return
	 */
	public abstract PersistenceFactory clone(Configuration configuration);

	/**
     * Getter for property configuration.
     * 
     * @return Value of property configuration.
     */
	public Configuration getConfiguration() {
		if (configurationRef==null)
			return null;
		return (Configuration)configurationRef.get();
	}
	
    /**
     * Getter for property description.
     * 
     * @return Value of property description.
     */
	public String getDescription() {
		return description;
	}

    /**
     * Getter for property name.
     * 
     * @return Value of property name.
     */
	public String getName() {
		return name;
	}
	
    /**
     * Getter for the list of properties for the particular technology
     * deployed by the inhereted classes.
     * 
     * @return properties.
     */
	public Properties getProperties() {
		return properties;
	}

	/**
	 * @return string representation of type of persistence factory
	 */
	public String getType() {
		return "abstract";
	}

	public boolean getStoreConfig() {
		return storeConfig;
	}

	public abstract AccountPoolMapperDB retrieveAccountPoolMapperDB(String name);
	
	public abstract ConfigurationDB retrieveConfigurationDB();
	
	public abstract ManualAccountMapperDB retrieveManualAccountMapperDB(String name);
	
	public abstract ManualUserGroupDB retrieveManualUserGroupDB(String name);

	public abstract UserGroupDB retrieveUserGroupDB(String name);
	
    /**
     * Setter for property configuration.
     * 
     * @param configuration.
     */
	public void setConfiguration(Configuration configuration) {
		this.configurationRef = new SoftReference(configuration);
	}
	
    /**
     * Setter for property description.
     * 
     * @param description.
     */
    public void setDescription(String description) {
    	this.description = description;
    }

    /**
     * Setter for property name.
     * 
     * @param name.
     */
	public void setName(String name) {
		this.name = name;
	}
	
    /**
     * Setter for the list of properties for the particular technology
     * deployed by the inhereted classes.
     * 
     * @param properties.
     */
	public void setProperties(Properties properties) {
		this.properties = properties;
	}

	public void setStoreConfig(boolean storeConfig) {
		this.storeConfig = storeConfig;
	}

	/**
     * Get XML representation of this object for writing to gums.config
     * 
     * @return xml as string
     */
	public abstract String toXML();
}
