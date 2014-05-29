/*
 * AccountMapper.java
 *
 * Created on March 30, 2004, 5:56 PM
 */

package gov.bnl.gums.account;

import java.lang.ref.SoftReference;

import gov.bnl.gums.configuration.Configuration;
import gov.bnl.gums.AccountInfo;
import gov.bnl.gums.GridUser;

/** 
 * Defines the logic with which a user will be mapped to a local account.
 * As of now, the logic is a simple certificate subject mapped to a user
 * account. In the future this interface will be extended to map the credential
 * of a full proxy (DN, vo, role, group) to a user and group account.
 *
 * @author  Gabriele Carcassi, Jay Packard
 */
public abstract class AccountMapper {
    /**
     * @return user friendly string representation of the property type called statically 
     */
	static public String getTypeStatic() {
		return "abstract";
	}
	
	private String name = "";
	private String description = "";
	private SoftReference configurationRef = null;
	
	/**
	 * Create an account mapper object - empty constructor needed by XML Digestor
	 */
	public AccountMapper() {
    }
 
	/**
	 * Create an account mapper object with a given configuration
	 * 
	 * @param configuration
	 */
	public AccountMapper(Configuration configuration) {
    	this.configurationRef = new SoftReference(configuration);
    }
	
	/**
	 * Create an account mapper object with a given configuration and name
	 * 
	 * @param configuration
	 * @param name
	 */
	public AccountMapper(Configuration configuration, String name) {
    	this.configurationRef = new SoftReference(configuration);
    	this.name = name;
    }
    
	/**
	 * Create a clone of itself
	 * 
	 * @param configuration
	 * @return
	 */
	public abstract AccountMapper clone(Configuration configuration);

	/**
	 * @return Configuration object
	 */
	public Configuration getConfiguration() {
		if (configurationRef==null)
			return null;
		return (Configuration)configurationRef.get();
	}
	
	/**
	 * @return description as string
	 */
	public String getDescription() {
		return description;
	}
	
	/**
	 * @return name as string
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * @return string representation of type of account mapper
	 */
	public String getType() {
		return "abstract";
	}
	
	/**
     * Maps a grid identity to a local account name.
     * @param userDN the certificate DN (i.e. '/DC=org/DC=doegrids/OU=People/CN=John Smith').
     * @return a user account (i.e. 'AccountInfo[user=atlas, group=atlas]').
     */
    public abstract AccountInfo mapUser(GridUser user, boolean createNew);
    
    public AccountInfo mapUser(String userDN, boolean createNew) {
    	GridUser user = new GridUser();
    	user.setCertificateDN(userDN);
    	return mapUser(user, createNew);
    }

    /**
     * @param configuration
     */
    public void setConfiguration(Configuration configuration) {
		this.configurationRef = new SoftReference(configuration);
	}
	
    /**
     * @param name
     */
    public void setName(String name) {
		this.name = name;
	}
    
    /**
     * @param description
     */
    public void setDescription(String description) {
    	this.description = description;
    }

    /**
     * Get string representation of this object for displaying in the 
     * diagnostic summary web page
     * 
     * @param bgColor back ground color
     * @return
     */
    public abstract String toString(String bgColor);
    
    /**
     * Get XML representation of this object for writing to gums.config
     * 
     * @return xml as string
     */
    public abstract String toXML();
}
