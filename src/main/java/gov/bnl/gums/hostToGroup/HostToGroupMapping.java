/*
 * AbstractHostGroup.java
 *
 * Created on May 10, 2005, 3:44 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package gov.bnl.gums.hostToGroup;

import gov.bnl.gums.configuration.Configuration;

import java.lang.ref.SoftReference;
import java.util.*;

/** It defines a a group of hosts that will be using the same mappings for user
 * authorization. An object of this class links a series of hostnames to a list
 * of group mappings.
 * <p>
 * This class does not return the list of hosts that will be affected since 
 * the group of hosts can be defined by a rule, without knowledge of which
 * systems exist. For example, all the machines in the 130.199.*.* subnet,
 * or all machines in the usatlas.bnl.gov subdomain.
 *
 * @author Gabriele Carcassi, Jay Packard
 */
public abstract class HostToGroupMapping {
    private List groupToAccountMappers = new ArrayList();
    private SoftReference configurationRef = null;
    private String name = "";
	private String description = "";
	
    
	/**
	 * Create a nwe HostToGroupMapping object.
	 * 
	 * This empty constructor needed by XML Digestor
	 */
    public HostToGroupMapping() {
    }

    /**
     * Create a new HostToGroupMapping object with a configuration.
     * 
     * @param configuration
     */
    public HostToGroupMapping(Configuration configuration) {
    	this.configurationRef = new SoftReference(configuration);
    }
    
	/**
	 * Create a new HostToGroupMapping object with a configuration and a name.
	 * 
	 * @param configuration
	 */
    public HostToGroupMapping(Configuration configuration, String name) {
    	this.configurationRef = new SoftReference(configuration);
    	this.name = name;
    }

    /** Changes the list of group mapping associated with this mapping.
     *
     * @param groupMapper A list of GroupMapper objects.
     */
    public void addGroupToAccountMapping(String groupToAccountMapping) {
        this.groupToAccountMappers.add(groupToAccountMapping);
    }
    
	/**
	 * Create a clone of itself for specified configuration.
	 * 
	 * @param configuration
	 * @return
	 */
    public abstract HostToGroupMapping clone(Configuration configuration);
    
    /**
     * Determines if this host to group mapping contains a group to account mapping.
     * 
     * @return returns true if group to account mapping is contained.
     */
    public boolean containsGroupToAccountMapping(String groupToAccountMappingQuery) {
    	Iterator groupMapperIt = groupToAccountMappers.iterator();
    	while(groupMapperIt.hasNext()) {
    		String groupToAccountMapping = (String)groupMapperIt.next();
    		if(groupToAccountMapping.equals(groupToAccountMappingQuery))
    			return true;
    	}
    	return false;
    }
    
    /**
     * Getter for property configuration.
     * 
     * @return Configuration as string.
     */
    public Configuration getConfiguration() {
    	if (configurationRef==null)
    		return null;
    	return (Configuration)configurationRef.get();
    }
    
    /**
     * Getter for property description.
     * 
     * @return Description as string.
     */
	public String getDescription() {
		return description;
	}
    
    /** Returns the list of group mapping associated with this mapping.
     * @return A list of GroupMapper objects.
     */
    public List getGroupToAccountMappings() {
        return Collections.unmodifiableList(groupToAccountMappers);
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
     * Determines if hostname is matched within this host to group mapping.
     * 
     * @param hostname
     * @return true if hostname matches
     */
    public abstract boolean isInGroup(String hostname);
    
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
     * Get XML representation of this object for writing to gums.config
     * 
     * @return xml as string
     */
    public abstract String toXML();
}
