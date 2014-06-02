/*
 * UserGroupDB.java
 *
 * Created on May 25, 2004, 10:22 AM
 */

package gov.bnl.gums.db;

import gov.bnl.gums.GridUser;

import java.util.*;

/** A persistance layer for a group of users, used to cache user information
 * on the GUMS server/site instead of taking it always directly from the source.
 * A list of users from a VO will be taken typically few times a day, through 
 * the updateMembers() in the group. The UserGroup will typically save the
 * information somewhere. This interface is provided to allow for the
 * cache to reside on different mediums (i.e. Database, LDAP, file, ...)
 *
 * @author Gabriele Carcassi, Jay Packard
 */
public interface UserGroupDB {
    /**
     * Retrieves all the members of the griven group.
     * @return A List of GridUser objects representing all the members in the group.
     */
    List retrieveMembers();
    /**
     * Determines whether a member is in the group. It must be a direct query to
     * the store, not on a cached value.
     * @param user A grid credential
     * @return True if the credential was in the list of members
     */
    boolean isMemberInGroup(GridUser user);
    /**
     * Determines whether a DN is in the group.  See use case explained in UserGroup.java
     * @param user A grid user
     * @return True if the user's DN was in the list of members
     */
    boolean isDNInGroup(GridUser user);
    /** 
     * Sets the list of members as the one given. The method should change what
     * was stored to the content of the list. It should also perform a diff,
     * so that calls to retrieveNewMembers and retrieveRemovedMembers will
     * return the changes.
     * @param members A list of GridUser objects.
     */
    void loadUpdatedList(List members);
    /**
     * Returns the members added after a loadUpdatedList
     * @return A list of GridUser objects.
     */
    List retrieveNewMembers();
    /**
     * Returns the members removed after a loadUpdatedList
     * @return A list of GridUser objects.
     */
    List retrieveRemovedMembers();
}
