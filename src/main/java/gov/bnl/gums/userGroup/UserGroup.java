/*
 * UserGroup.java
 *
 * Created on May 24, 2004, 2:37 PM
 */

package gov.bnl.gums.userGroup;

import gov.bnl.gums.GridUser;
import gov.bnl.gums.configuration.Configuration;

import java.util.*;

/** An interface that defines a group of people, which GUMS will associate to a 
 * mapping policy. An implementation could take/manage a list of users in
 * any way it wanted, or it could combine different groups.
 *
 * @author  Gabriele Carcassi
 */
public abstract class UserGroup {
	private String name = "";
	protected String[] accessTypes = {"write", "read all", "read self"};
	protected int accessIndex = 2;
	private Configuration configuration = null;
	
	static public String getType() {
		return "abstract";
	}
	
	/**
	 * This empty constructor is needed by XML Digestor
	 */
	public UserGroup() {
	}

	/**
	 * @param configuration
	 * @param name
	 */
	public UserGroup(Configuration configuration) {
		this.configuration = configuration;
	}
	
	/**
	 * @param configuration
	 * @param name
	 */
	public UserGroup(Configuration configuration, String name) {
		this.configuration = configuration;
		this.name = name;
	}

	public abstract UserGroup clone(Configuration configuration);

	public String getAccess() {
    	return accessTypes[accessIndex];
    }
	
	public Configuration getConfiguration() {
		return configuration;
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
    public abstract List getMemberList();
	
    public String getName() {
		return name;
	}
    
    public boolean hasReadAllAccess() {
    	return (accessIndex<=1);
    }
    
    public boolean hasReadSelfAccess() {
    	return (accessIndex<=2);
    }
    
    public boolean hasWriteAccess() {
    	return (accessIndex==0);
    }
	
    /**
     * Determines whether the given user identity is part of the group.
     * @param userDN the certificate DN.
     * @return true if it's in the group
     */
    public abstract boolean isInGroup(GridUser user);
    
    public void setAccess(String access) {
    	for(int i=0; i<accessTypes.length; i++) {
    		if ( accessTypes[i].equalsIgnoreCase(access) ) {
    			accessIndex = i;
    			return;
    		}
    	}
    	throw new RuntimeException("Invalid access type: "+access);
    }
    
    public void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}
    
    public void setName(String name) {
		this.name = name;
	}
    
    public abstract String toString(String bgColor);

    public abstract String toXML();
    
    /** Updates the local list of the users from the source of the group.
     * <p>
     * Most user groups will get the information from a separate database
     * accessible via WAN. For that reason, the user group will maintain a
     * local cache with the list of members, which can be updated through
     * this method.
     */
    public abstract void updateMembers();
}
