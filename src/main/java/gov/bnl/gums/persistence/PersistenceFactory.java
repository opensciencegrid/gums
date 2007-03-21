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

import java.util.Iterator;
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
 * entity beans, simple JDBC, Hibernate or any other favourite technology.
 *
 * @author  Gabriele Carcassi
 */
public abstract class PersistenceFactory {
    static public String getTypeStatic() {
		return "abstract";
	}
    
    private String name;
    private Properties properties;
    private Configuration configuration;
    
	/**
	 * This empty constructor needed by XML Digestor
	 */
	public PersistenceFactory() {
	}
    
	/**
	 * @param configuration
	 */
	public PersistenceFactory(Configuration configuration) {
		this.configuration = configuration;
	}	
    
	/**
	 * @param configuration
	 * @param name
	 */
	public PersistenceFactory(Configuration configuration, String name) {
		this.name = name;
		this.configuration = configuration;
	}
	
	public abstract PersistenceFactory clone(Configuration configuration);

	public Configuration getConfiguration() {
		return configuration;
	}

	public String getName() {
		return name;
	}
	
	public Properties getProperties() {
		return properties;
	}

	public String getType() {
		return "abstract";
	}

	public abstract AccountPoolMapperDB retrieveAccountPoolMapperDB(String name);
	
	public abstract ManualAccountMapperDB retrieveManualAccountMapperDB(String name);
	
	public abstract ManualUserGroupDB retrieveManualUserGroupDB(String name);
	
	public abstract UserGroupDB retrieveUserGroupDB(String name);
	
	public void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setProperties(Properties properties) {
		this.properties = properties;
	}
	
	public abstract String toXML();
}
