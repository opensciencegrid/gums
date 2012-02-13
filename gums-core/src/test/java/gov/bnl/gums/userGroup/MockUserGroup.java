/*
 * MockUserGroup.java
 *
 * Created on May 24, 2004, 2:49 PM
 */

package gov.bnl.gums.userGroup;

import java.util.*;

import gov.bnl.gums.*;
import gov.bnl.gums.configuration.Configuration;

/**
 *
 * @author  carcassi
 */
public class MockUserGroup extends UserGroup {
    private List members;
    boolean updated;
    boolean matchFQAN;
    
    /** Creates a new instance of MockUserGroup */
    public MockUserGroup(Configuration configuration, String name, boolean matchFQAN) {
    	super(configuration, name);
    	this.matchFQAN = matchFQAN;
        members = new ArrayList();
    }
    
    public java.util.List getMemberList() {
        return Collections.unmodifiableList(members);
    }
    
    public boolean isInGroup(GridUser user) {
    	Iterator it = members.iterator();
    	while(it.hasNext()) {
    		GridUser curUser = (GridUser)it.next();
    		if (matchFQAN) {
    			if (curUser.equals(user)) {// assumes exact matching
    				return true;
    			}
    		}
    		else {
    			if (curUser.getCertificateDN().equals(user.getCertificateDN()))
    				return true;  			
    		}
    	}
        return false;
    }
    
    public void addMember(GridUser user) {
    	members.add(user);
    }
    
    public void updateMembers() {
        updated = true;
    }
    
    public boolean isUpdated() {
        return updated;
    }
    
    public String toString(String bgColor) {
    	return "";
    }
    
    public String toXML(){
    	return "";
    }
    
    public UserGroup clone(Configuration configuration) {
    	return null;
    }
    
}
