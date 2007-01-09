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
        GridUser carcassi = new GridUser();
        carcassi.setCertificateDN("/DC=org/DC=doegrids/OU=People/CN=Gabriele Carcassi");
        currentMembers.add(carcassi);
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
