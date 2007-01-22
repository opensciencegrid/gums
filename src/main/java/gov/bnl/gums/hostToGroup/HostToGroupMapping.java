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
 * @author  Gabriele Carcassi
 */
public abstract class HostToGroupMapping {
    private List groupToAccountMappers = new ArrayList();
    private Configuration configuration = null;
    
	/**
	 * This empty constructor needed by XML Digestor
	 */
    public HostToGroupMapping() {
    }
    
	/**
	 * Automatically adds itself to the configuration.
	 * @param configuration
	 */
    public HostToGroupMapping(Configuration configuration) {
    	this.configuration = configuration;
   		configuration.addHostToGroupMapping(this);
    }

	public void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}
    
    /** Returns name.
     * @return name or unique identifier.
     */
    public abstract String getName();
    
    public Configuration getConfiguration() {
    	return configuration;
    }
    
    /** Returns the list of group mapping associated with this mapping.
     * @return A list of GroupMapper objects.
     */
    public List getGroupToAccountMappings() {
        return Collections.unmodifiableList(groupToAccountMappers);
    }
    
    /** Changes the list of group mapping associated with this mapping.
     *
     * @param groupMapper A list of GroupMapper objects.
     */
    public void addGroupToAccountMapping(String groupToAccountMapping) {
        this.groupToAccountMappers.add(groupToAccountMapping);
    }
    
    /**
     * @return returns true if group2AccountMapperName is matched.
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
    
    public abstract boolean isInGroup(String hostname);
    
    public String toXML() {
    	String retStr = "\t\t<hostToGroupMapping\n"+
    		"\t\t\tgroupToAccountMappings='";
    	Iterator it = getGroupToAccountMappings().iterator();
    	while(it.hasNext()) {
    		String groupToAccountMapping = (String)it.next();
    		retStr += groupToAccountMapping + (it.hasNext()?", ":"");
    	}
    	retStr += "'\n";
    	
    	return retStr;
    }
    
    public abstract Object clone();
}
