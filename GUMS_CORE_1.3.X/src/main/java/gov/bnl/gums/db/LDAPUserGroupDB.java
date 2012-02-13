/*
 * LDAPUserGroupDB.java
 *
 * Created on August 1, 2005, 10:32 AM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package gov.bnl.gums.db;

import gov.bnl.gums.FQAN;
import gov.bnl.gums.GridUser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import javax.naming.*;
import javax.naming.directory.*;
import org.apache.log4j.Logger;

import gov.bnl.gums.persistence.LDAPPersistenceFactory;

/**
 *
 * @author Gabriele Carcassi, Jay Packard
 */
public class LDAPUserGroupDB implements UserGroupDB, ManualUserGroupDB {
    private Logger log = Logger.getLogger(LDAPUserGroupDB.class);
    private LDAPPersistenceFactory factory;
    private String group;
    private String groupDN;
    private List addedMembers;
    private List removedMembers;
    
    /** Creates a new instance of LDAPUserGroupDB */
    public LDAPUserGroupDB(LDAPPersistenceFactory factory, String group) {
        this.factory = factory;
        this.group = group;
        this.groupDN = "group=" + group + "," + factory.getGumsObject();
	createGroupIfNotExists();
        log.trace("LDAPUserGroupDB object create: group '" + group + "' factory " + factory);
    }
    
    public void addMember(gov.bnl.gums.GridUser user) {
        factory.addUserGroupEntry(gridID(user), group, groupDN);
    }
    
    public boolean isMemberInGroup(gov.bnl.gums.GridUser user) {
        DirContext context = factory.retrieveGumsDirContext();
        try {
            SearchControls ctrls = new SearchControls();
            ctrls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            NamingEnumeration result = context.search(factory.getGumsObject(), "(&(group={0})(user={1}))", new Object[] {group, gridID(user)}, ctrls);
            log.trace("Checking whether user '" + user + "' belongs to group '" + group + "': " + result.hasMore());
            return result.hasMore();
        } catch (Exception e) {
            log.info("Couldn't check whether user '" + user + "' belongs to group '" + group + "'", e);
            throw new RuntimeException("Couldn't check whether user '" + user + "' belongs to group ': " + e.getMessage(), e);
        } finally {
            factory.releaseContext(context);
        }
    }

    public void loadUpdatedList(java.util.List members) {
        Exception lastException = null;

        try {
            // Retrieving the current list of members
            List currentMembers = retrieveMembers();

            // Calculating the deltas
            List newMembers = new ArrayList(members);
            newMembers.removeAll(currentMembers);
            List oldMembers = new ArrayList(currentMembers);
            oldMembers.removeAll(members);

            // Removing old members
            if (log.isTraceEnabled()) {
                log.trace("Removing old members from group " + group + ": " + oldMembers);
            }
            Iterator iter = oldMembers.iterator();
            while (iter.hasNext()) {
                GridUser user = (GridUser) iter.next();
                try {
                    removeMember(user);
                } catch (Exception e) {
                    lastException = e;
                }
            }

            // Adding new members
            if (log.isTraceEnabled()) {
                log.trace("Adding new members from group " + group + ": " + newMembers);
            }
            iter = newMembers.iterator();
            while (iter.hasNext()) {
                GridUser user = (GridUser) iter.next();
                try {
                    addMember(user);
                } catch (Exception e) {
                    lastException = e;
                }
            }

            addedMembers = newMembers;
            removedMembers = oldMembers;
            
        } catch (Exception e) {
            log.error("Updating member list in LDAP group '" + group + "' failed", e);
            throw new RuntimeException("Updating member list in LDAP group '" + group + "' failed", e);
        }
        
        if (lastException != null) {
            throw new RuntimeException("Updating member list in LDAP group '" + group + "' wasn't completely successful: " + lastException.getMessage(), lastException);
        }
    }
    
    public boolean removeMember(gov.bnl.gums.GridUser user) {
        try {
            factory.removeUserGroupEntry(gridID(user), group, groupDN);
            return true;
        } catch (RuntimeException e) {
            if (e.getCause() instanceof NoSuchAttributeException) {
                log.trace("No entry to remove for user '" + user + "' from group '" + group + "'");
                return false;
            }
            throw e;
        }
    }

    public java.util.List retrieveMembers() {
        DirContext context = factory.retrieveGumsDirContext();
        try {
            DirContext groupContext = (DirContext) context.lookup(groupDN);
            Attributes atts = groupContext.getAttributes("");
            Attribute users = atts.get("user");
            List members = new ArrayList();
            if (users == null)
                return members;
            NamingEnumeration result = users.getAll();
            while (result.hasMore()) {
                GridUser user = new GridUser();
                String userGridID = (String) result.next();
                StringTokenizer tokens = new StringTokenizer(userGridID, "[]");
                user.setCertificateDN(tokens.nextToken());
                if (tokens.hasMoreTokens())
                    user.setVoFQAN(new FQAN(tokens.nextToken()));
                members.add(user);
            }
            if (log.isTraceEnabled()) {
                log.trace("Retrieved full list of members from LDAP group '" + group + "': " + members);
            }
            return members;
        } catch (Exception e) {
            log.info("Couldn't retrieve full list of members from LDAP group '" + group + "'", e);
            throw new RuntimeException("Couldn't retrieve full list of members from LDAP group '" + group + "': " + e.getMessage(), e);
        } finally {
            factory.releaseContext(context);
        }
    }

    public java.util.List retrieveNewMembers() {
        return addedMembers;
    }

    public java.util.List retrieveRemovedMembers() {
        return removedMembers;
    }

    private void createGroupIfNotExists() {
        if (!doesGroupExist())
            factory.createUserGroup(group, groupDN);
    }

    private boolean doesGroupExist() {
        DirContext context = factory.retrieveGumsDirContext();
        try {
            SearchControls ctrls = new SearchControls();
            ctrls.setSearchScope(SearchControls.SUBTREE_SCOPE);
	    NamingEnumeration result = context.search(factory.getGumsObject(), "(group={0})", new Object[] {group}, ctrls);
	    log.trace("Checking whether group '" + group + "' exists: " + result.hasMore());
            return result.hasMore();
        } catch (Exception e) {
            log.info("Couldn't determine whether group '" + group + "' exists", e);
            throw new RuntimeException("Couldn't determine whether group '" + group + "' exists: " + e.getMessage(), e);
        } finally {
            factory.releaseContext(context);
        }
    }

    private String gridID(GridUser user) {
        if (user.getVoFQAN() == null)
            return user.getCertificateDN();
        else
            return user.getCertificateDN()+"["+user.getVoFQAN()+"]";
    }
    
}
