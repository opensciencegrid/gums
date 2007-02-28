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
    
    /** Creates a new instance of MockUserGroup */
    public MockUserGroup(Configuration configuration, String name) {
    	super(configuration, name);
        members = new ArrayList();
        GridUser user = new GridUser();
        user.setCertificateDN("/DC=org/DC=doegrids/OU=People/CN=John Smith");
        members.add(user);
    }
    
    public java.util.List getMemberList() {
        return Collections.unmodifiableList(members);
    }
    
    public boolean isInGroup(GridUser user) {
        if (members.contains(user))
            return true;
        return false;
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
    	return null;
    }
    
    public UserGroup clone(Configuration configuration) {
    	return null;
    }
    
}
