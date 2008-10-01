/*
 * MockManualUserGroupDB.java
 *
 * Created on May 25, 2004, 4:38 PM
 */

package gov.bnl.gums.db;

import gov.bnl.gums.GridUser;

import java.util.*;

/**
 *
 * @author  carcassi
 */
public class MockManualUserGroupDB implements ManualUserGroupDB {
    List members = new ArrayList();
    
    /** Creates a new instance of MockManualUserGroupDB */
    public MockManualUserGroupDB() {
    }
    
    public void addMember(GridUser userDN) {
        if (isMemberInGroup(userDN)) {
            throw new RuntimeException("User already present in group.");
        }
        members.add(userDN);
    }
    
    public boolean isMemberInGroup(GridUser userDN) {
        return members.contains(userDN);
    }
    
    public boolean removeMember(GridUser userDN) {
        return members.remove(userDN);
    }
    
    public java.util.List retrieveMembers() {
        return Collections.unmodifiableList(members);
    }
    
}
