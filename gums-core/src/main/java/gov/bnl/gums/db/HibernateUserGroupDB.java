/*
 * HibernateUserGroupDB.java
 *
 * Created on June 15, 2005, 5:02 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package gov.bnl.gums.db;

import gov.bnl.gums.*;

import gov.bnl.gums.persistence.HibernatePersistenceFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.hibernate.*;
import org.hibernate.type.StringType;
import org.hibernate.type.Type;

import org.apache.log4j.Logger;

/**
 *
 * @author Gabriele Carcassi, Jay Packard
 */
public class HibernateUserGroupDB implements UserGroupDB, ManualUserGroupDB {
    private Logger log = Logger.getLogger(HibernateUserGroupDB.class);
    private HibernatePersistenceFactory persistenceFactory;
    private String group;
    private List addedMembers;
    private List removedMembers;
    
    /** Creates a new instance of HibernateUserGroupDB */
    public HibernateUserGroupDB(HibernatePersistenceFactory persistenceFactory, String group) {
        log.trace("HibernateUserGroupDB created for group " + group);
        this.persistenceFactory = persistenceFactory;
        this.group = group;
    }

    public String getGroup(){return group;};
    
    public void addMember(GridUser user) {
        Session session = null;
        Transaction tx = null;
        try {
            // Checks whether the value is present in the database
            session = persistenceFactory.retrieveSessionFactory().openSession();
            tx = session.beginTransaction();
            if (isMemberInGroup(user)) {
                throw new Exception("User " + user + " is already present in group '" + group + "'");
            }
            addMember(session, tx, user);
            tx.commit();
        // Handles when transaction goes wrong...
        } catch (Exception e) {
            log.error("Couldn't add member to '" + group + "'", e);
            if (tx != null) {
                try {
                    tx.rollback();
                } catch (Exception e1) {
                    log.error("Hibernate error: rollback failed", e1);
                    throw new RuntimeException("Database errors: " + e.getMessage() + " - " + e1.getMessage(), e);
                }
            }
            throw new RuntimeException("Database error: " + e.getMessage(), e);
        } finally {
            if (session != null) {
                try {
                    session.close();
                } catch (Exception e1) {
                    log.error("Hibernate error: couldn't close session", e1);
                    throw new RuntimeException("Database error: " + e1.getMessage(), e1);
                }
            }
        }
    }
    
    public boolean isMemberInGroup(GridUser user) {
        Session session = null;
        Transaction tx = null;
        try {
            log.trace("Checking whether user " + user + " is in group " + group);
            // Checks whether the value is present in the database
            session = persistenceFactory.retrieveSessionFactory().openSession();
            tx = session.beginTransaction();
            boolean result = isMemberInGroup(session, tx, user);
            tx.commit();
            return result;
        // Handles when transaction goes wrong...
        } catch (Exception e) {
            log.error("Couldn't determine whether member is in group '" + group + "'", e);
            if (tx != null) {
                try {
                    tx.rollback();
                } catch (Exception e1) {
                    log.error("Hibernate error: rollback failed", e1);
                    throw new RuntimeException("Database errors: " + e.getMessage() + " - " + e1.getMessage(), e);
                }
            }
            throw new RuntimeException("Database error: " + e.getMessage(), e);
        } finally {
            if (session != null) {
                try {
                    session.close();
                } catch (Exception e1) {
                    log.error("Hibernate error: couldn't close session", e1);
                    throw new RuntimeException("Database error: " + e1.getMessage(), e1);
                }
            }
        }
    }

    public void loadUpdatedList(java.util.List members) {
        Session session = null;
        Transaction tx = null;
        int isolation = -1;
        try {
            if (log.isTraceEnabled()) {
                log.trace("Changing the list of users in group " + group + " to " + members);
            }

            session = persistenceFactory.retrieveSessionFactory().openSession();
            Connection conn = session.connection();
            isolation = conn.getTransactionIsolation();
            conn.setTransactionIsolation(conn.TRANSACTION_SERIALIZABLE);
            tx = session.beginTransaction();
            
            // Retrieving the current list of members
            List currentMembers = retrieveMembers(session, tx);
            
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
                removeMember(session, tx, user);
            }
            
            // Adding new members
            if (log.isTraceEnabled()) {
                log.trace("Adding new members from group " + group + ": " + newMembers);
            }
            iter = newMembers.iterator();
            while (iter.hasNext()) {
                GridUser user = (GridUser) iter.next();
                addMember(session, tx, user);
            }
            
            tx.commit();
            
            addedMembers = newMembers;
            removedMembers = oldMembers;
        // Handles when transaction goes wrong...
        } catch (Exception e) {
            log.error("Couldn't update members of group '" + group + "'", e);
            if (tx != null) {
                try {
                    tx.rollback();
                } catch (Exception e1) {
                    log.error("Hibernate error: rollback failed", e1);
                    throw new RuntimeException("Database errors: " + e.getMessage() + " - " + e1.getMessage(), e);
                }
            }
            if ((e instanceof JDBCException) && 
                    (e.getCause() != null) && 
                    (e.getCause() instanceof SQLException) &&
                    (e.getCause().getMessage().indexOf("transaction") != -1)) {
                throw new RuntimeException("Update failed: probably there is another update running simultaneously.");
            }
            throw new RuntimeException("Database error: " + e.getMessage(), e);
        } finally {
            if (session != null) {
                try {
                    if (isolation != -1) {
                        session.connection().setTransactionIsolation(isolation);
                    }
                    session.close();
                } catch (Exception e1) {
                    log.error("Hibernate error: couldn't close session", e1);
                    throw new RuntimeException("Database error: " + e1.getMessage(), e1);
                }
            }
        }
    }

    public boolean removeMember(GridUser user) {
        Session session = null;
        Transaction tx = null;
        try {
            // Checks whether the value is present in the database
            session = persistenceFactory.retrieveSessionFactory().openSession();
            tx = session.beginTransaction();
            boolean result = removeMember(session, tx, user);
            tx.commit();
            return result;
        // Handles when transaction goes wrong...
        } catch (Exception e) {
            log.error("Couldn't remove member from group '" + group + "'", e);
            if (tx != null) {
                try {
                    tx.rollback();
                } catch (Exception e1) {
                    log.error("Hibernate error: rollback failed", e1);
                    throw new RuntimeException("Database errors: " + e.getMessage() + " - " + e1.getMessage(), e);
                }
            }
            throw new RuntimeException("Database error: " + e.getMessage(), e);
        } finally {
            if (session != null) {
                try {
                    session.close();
                } catch (Exception e1) {
                    log.error("Hibernate error: couldn't close session", e1);
                    throw new RuntimeException("Database error: " + e1.getMessage(), e1);
                }
            }
        }
    }

    public java.util.List retrieveMembers() {
        Session session = null;
        Transaction tx = null;
        try {
            // Retrieving members from the db
            log.trace("Retrieving members from group " + group);
            session = persistenceFactory.retrieveSessionFactory().openSession();
            tx = session.beginTransaction();
            List members = retrieveMembers(session, tx);
            tx.commit();
            return members;
        // Handles when transaction goes wrong...
        } catch (Exception e) {
            log.error("Couldn't retrieve members from group '" + group + "'", e);
            if (tx != null) {
                try {
                    tx.rollback();
                } catch (Exception e1) {
                    log.error("Hibernate error: rollback failed", e1);
                    throw new RuntimeException("Database errors: " + e.getMessage() + " - " + e1.getMessage(), e);
                }
            }
            throw new RuntimeException("Database error: " + e.getMessage(), e);
        } finally {
            if (session != null) {
                try {
                    session.close();
                } catch (Exception e1) {
                    log.error("Hibernate error: couldn't close session", e1);
                    throw new RuntimeException("Database error: " + e1.getMessage(), e1);
                }
            }
        }
    }

    public java.util.List retrieveNewMembers() {
        return addedMembers;
    }
    
    public java.util.List retrieveRemovedMembers() {
        return removedMembers;
    }
    
    private void addMember(Session session, Transaction tx, GridUser user) throws Exception {
        HibernateUser hUser = new HibernateUser();
        hUser.setGroup(group);
        hUser.setDn(user.getCertificateDN());
        hUser.setEmail(user.getEmail());
        if (user.getVoFQAN() != null) {
            hUser.setFqan(user.getVoFQAN().toString());
        }
        session.save(hUser);
    }
    
    private boolean isMemberInGroup(Session session, Transaction tx, GridUser user) throws Exception {
        Query q;
        if (user.getVoFQAN() == null) {
            q = session.createQuery("FROM HibernateUser u WHERE u.group = ? AND u.dn = ? AND u.fqan is null");
        } else {
            q = session.createQuery("FROM HibernateUser u WHERE u.group = ? AND u.dn = ? AND u.fqan = ?");
            q.setString(2, user.getVoFQAN().toString());
        }
        q.setString(0, group);
        q.setString(1, user.getCertificateDN());
        List result = q.list();
        return result.size() > 0;
    }

    private boolean removeMember(Session session, Transaction tx, GridUser user) throws Exception {
    	Query q;
    	if (user.getVoFQAN() == null) {
            q = session.createQuery("DELETE FROM HibernateUser WHERE group = ? AND dn = ? AND fqan is null");
        } else {
            q = session.createQuery("DELETE FROM HibernateUser WHERE group = ? AND dn = ? AND fqan = ?");
            q.setString(2, user.getVoFQAN().toString());
        }
        q.setString(0, group);
        q.setString(1, user.getCertificateDN());
        return q.executeUpdate() > 0;
    }

    private java.util.List retrieveMembers(Session session, Transaction tx) throws Exception {
        Query q;
        q = session.createQuery("FROM HibernateUser u WHERE u.group = ?");
        q.setString(0, group);
        List hibernateUsers = q.list();
        List members = new ArrayList(hibernateUsers.size());
        Iterator iter = hibernateUsers.iterator();
        while (iter.hasNext()) {
            HibernateUser user = (HibernateUser) iter.next();
            members.add(new GridUser(user.getDn(), user.getFqan(), user.getEmail(), false));
        }
        return members;
    }
}
