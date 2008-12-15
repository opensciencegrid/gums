/*
 * MockHostnameMapping.java
 *
 * Created on May 24, 2004, 2:54 PM
 */

package gov.bnl.gums.hostToGroup;


import gov.bnl.gums.configuration.Configuration;

import java.util.*;

/**
 *
 * @author  carcassi
 */
public class MockHostToGroupMapping extends HostToGroupMapping {
    String name;
    /**
     * Holds value of property groupMappers.
     */
    private List groupMappers;
    
    /** Creates a new instance of MockHostnameMapping */
    public MockHostToGroupMapping(Configuration configuration) {
    	super(configuration);
    }
    
    public void addGroupMapper(MockHostToGroupMapping groupMapper) {
        this.groupMappers.add(groupMapper);
    }
    
    public void setGroupMappers(List groupMappers) {
        this.groupMappers = groupMappers;
    }
    
    public List getGroupMappers() {
        return this.groupMappers;
    }
    
    public boolean isInGroup(String hostname) {
        return hostname.equals("known.site.com");
    }
    
    public HostToGroupMapping clone(Configuration configuration) {
    	return null;
    }
    
    public String toXML(){
    	return "";
    }
    
    public String getName() {
    	return "known.site.com";
    }
    
}
