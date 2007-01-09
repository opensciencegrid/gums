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
 * @author  Gabriele Carcassi
 */
public interface ManualUserGroupDB {
    void addMember(GridUser userDN);
    boolean removeMember(GridUser userDN);
    boolean isMemberInGroup(GridUser user);
    List retrieveMembers();
}
