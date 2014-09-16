/*
 * UserGroup.java
 *
 * Created on May 24, 2004, 2:37 PM
 */

package gov.bnl.gums.userGroup;

import gov.bnl.gums.GridUser;
import gov.bnl.gums.configuration.Configuration;

import java.lang.ref.SoftReference;
import java.util.*;

/** 
 * An interface that defines a group of people, which GUMS will associate to a 
 * mapping policy. An implementation could take/manage a list of users in
 * any way it wanted, or it could combine different groups.
 *
 * @author Gabriele Carcassi, Jay Packard
 */
public abstract class UserGroup {
    /**
     * @return user friendly string representation of the property type called statically 
     */
    static public String getTypeStatic() {
		return "abstract";
	}

	private static final String DEFAULT_BANNED_GROUP = "gums-banned";
	public static String getDefaultBannedGroupName() {return DEFAULT_BANNED_GROUP;}
	
	private String name = "";
	private String description = "";
	private SoftReference configurationRef = null;
	protected String[] accessTypes = {"write", "read all", "read self"};
	protected int accessIndex = 2;
	
	/**
	 * Create a new user group.  This empty constructor is needed by the XML Digestor.
	 */
	public UserGroup() {
	}
	
	/**
	 * Create a new user group with a configuration.
	 * 
	 * @param configuration
	 * @param name
	 */
	public UserGroup(Configuration configuration) {
		this.configurationRef = new SoftReference(configuration);
	}

	/**
	 * Create a new user group with a configuration and a name.
	 * 
	 * @param configuration
	 * @param name
	 */
	public UserGroup(Configuration configuration, String name) {
		this.configurationRef = new SoftReference(configuration);
		this.name = name;
	}
	
	/**
	 * Create a clone of itself
	 * 
	 * @param configuration
	 * @return
	 */
	public abstract UserGroup clone(Configuration configuration);

	/**
	 * Getter for property access, that determines what a member of this
	 * user group has access to in GUMS.
	 * 
	 * @return access as string
	 */
	public String getAccess() {
    	return accessTypes[accessIndex];
    }

	/**
	 * Getter for property configuration.
	 * 
	 * @return Configuration object
	 */
	public Configuration getConfiguration() {
		if (configurationRef==null)
			return null;
		return (Configuration)configurationRef.get();
	}
	
	/**
	 * Getter for property description.
	 * 
	 * @return Description as string
	 */
	public String getDescription() {
		return description;
	}
	
	/**
     * Returns the list of user identities that are part of the group.
     * <p>
     * Some UserGroups, however, could be defined by a rule that doesn't
     * allow listing. For example, a group could be 'all the users
     * with a DOEGrids certificate'. Though one could argue whether or
     * not is a good idea to have such a group, one can implement one
     * and throw an UnsupportedOperationException. This will make it
     * impossible for GUMS to create a grid-mapfile, but would still
     * allow direct user to account mapping through a call-out.
     * @return a List of GridUser objects representing the user certificate DN.
     */
    public abstract List<GridUser> getMemberList();

	/**
	 * Getter for property name.
	 * 
	 * @return name as string
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Getter for property type.
	 * 
	 * @return type as string
	 */
    public String getType() {
		return "abstract";
	}
    
    /**
     * @return true if this group allows at least read all access
     */
    public boolean hasReadAllAccess() {
    	return (accessIndex<=1);
    }
    
    /**
     * @return true if this group allows at least read self access
     */
    public boolean hasReadSelfAccess() {
    	return (accessIndex<=2);
    }
    
    /**
     * @return true if this group allows write access (admin privileges)
     */
    public boolean hasWriteAccess() {
    	return (accessIndex==0);
    }
	
    /**
     * Determines whether the given user identity is part of the group.
     * @param user the grid user.
     * @return true if it's in the group
     */
    public abstract boolean isInGroup(GridUser user);

    /**
     * Determines whether the given user DN is part of the group.
     * This should only match on DNs and ignore FQANs; the example
     * use case is banning: you want to ban the certificate, not
     * the certificate + FQAN.
     * @param user The grid user.
     * @return true if the user's DN is in the group.
     */
    public abstract boolean isDNInGroup(GridUser user);

    /**
     * Setter for property access
     * 
     * @param access
     */
    public void setAccess(String access) {
    	for(int i=0; i<accessTypes.length; i++) {
    		if ( accessTypes[i].equalsIgnoreCase(access) ) {
    			accessIndex = i;
    			return;
    		}
    	}
    	throw new RuntimeException("Invalid access type: "+access);
    }
    
    /**
     * Setter for property configuration.
     * 
     * @param configuration
     */
    public void setConfiguration(Configuration configuration) {
		this.configurationRef = new SoftReference(configuration);
	}
    
    /**
     * Setter for property description.
     * 
     * @param description
     */
    public void setDescription(String description) {
    	this.description = description;
    }
    
    /**
     * Setter for property name.
     * 
     * @param name
     */
    public void setName(String name) {
		this.name = name;
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
	 * Create a clone of itself
	 * 
	 * @param configuration
	 * @return
	 */
    public abstract String toXML();
    
    /** 
     * Updates the local list of the users from the source of the group.
     * <p>
     * Most user groups will get the information from a separate database
     * accessible via WAN. For that reason, the user group will maintain a
     * local cache with the list of members, which can be updated through
     * this method.
     */
    public abstract void updateMembers();
}
