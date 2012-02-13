/*
 * ManualUserGroupDB.java
 *
 * Created on May 25, 2004, 4:35 PM
 */

package gov.bnl.gums.db;

import gov.bnl.gums.GridUser;

import java.util.*;

/** The persistance layer of a ManualUserGroup, allowing to store a set
 * of users.
 * <p>
 * The persistance layer shouldn't be doing any kind of caching: it will be
 * done by the ManualUserGroup itself.
 *
 * @author Gabriele Carcassi, Jay Packard
 */
public interface ManualUserGroupDB {
    /**
     * Add member to group
     * 
     * @param userDN
     */
    void addMember(GridUser user);
    
    /**
     * Remove member from group
     * 
     * @param userDN
     * @return true if userDN removed
     */
    boolean removeMember(GridUser user);
    
    /**
     * Checks to see if member in group
     * 
     * @param user
     * @return true if user in group
     */
    boolean isMemberInGroup(GridUser user);
    
    /**
     * Get list of members
     * 
     * @return list of members
     */
    List retrieveMembers();
}
