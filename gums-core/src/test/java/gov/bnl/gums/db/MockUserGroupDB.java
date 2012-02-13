/*
 * MockUserGroupDB.java
 *
 * Created on May 25, 2004, 10:29 AM
 */

package gov.bnl.gums.db;

import gov.bnl.gums.GridUser;

import java.util.*;

/**
 *
 * @author  carcassi
 */
public class MockUserGroupDB implements UserGroupDB {
    List currentMembers;
    List newMembers;
    List removedMembers;
    
    /** Creates a new instance of MockUserGroupDB */
    public MockUserGroupDB() {
        currentMembers = new ArrayList();
        GridUser user = new GridUser("/DC=org/DC=griddev/OU=People/CN=John Smith", "/griddev/subgriddev/Role=griddevrole");
        GridUser user2 = new GridUser("/DC=org/DC=griddev/OU=People/CN=Jane Doe 12345", "/griddev/subgriddev/Role=griddevrole");
        currentMembers.add(user);
        currentMembers.add(user2);
    }
    
    public boolean isMemberInGroup(GridUser dn) {
        return currentMembers.contains(dn);
    }
    
    public void loadUpdatedList(java.util.List members) {
        newMembers = new ArrayList(members);
        newMembers.removeAll(currentMembers);
        removedMembers = new ArrayList(currentMembers);
        removedMembers.removeAll(members);
        currentMembers = new ArrayList(members);
    }
    
    public java.util.List retrieveMembers() {
        return Collections.unmodifiableList(currentMembers);
    }
    
    public java.util.List retrieveNewMembers() {
        return newMembers;
    }
    
    public java.util.List retrieveRemovedMembers() {
        return removedMembers;
    }
    
}
