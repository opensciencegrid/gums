/*
 * HibernateMapping.java
 *
 * Created on June 16, 2005, 4:22 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package gov.bnl.gums.db;


import java.util.*;

import gov.bnl.gums.persistence.HibernatePersistenceFactory;

import net.sf.hibernate.*;
import net.sf.hibernate.type.StringType;
import net.sf.hibernate.type.Type;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author Gabriele Carcassi, Jay Packard
 */
public class HibernateMappingDB implements ManualAccountMapperDB, AccountPoolMapperDB {
    private Log log = LogFactory.getLog(HibernateMappingDB.class);
    private HibernatePersistenceFactory persistenceFactory;
    private String map;
    
    /** Creates a new instance of HibernateMapping */
    public HibernateMappingDB(HibernatePersistenceFactory persistenceFactory, String map) {
        this.persistenceFactory = persistenceFactory;
        this.map = map;
    }

    public void addAccount(String account) {
        Session session = null;
        Transaction tx = null;
        try {
            log.trace("Adding account '" + account + "' to pool '" + map + "'");
            session = persistenceFactory.retrieveSessionFactory().openSession();
            tx = session.beginTransaction();
            /*Mapping map = retrieveMapping(session, tx, userDN);
            if (map != null) {
                return;
            }*/
            createMapping(session, tx, null, account);
            tx.commit();
        // Handles when transaction goes wrong...
        } catch (Exception e) {
            log.error("Couldn't add account to pool '" + map + "'", e);
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
    
    public String assignAccount(String userDN) {
        Session session = null;
        Transaction tx = null;
        try {
            log.trace("Assing an account to '" + userDN + "' from pool '" + map + "'");
            session = persistenceFactory.retrieveSessionFactory().openSession();
            tx = session.beginTransaction();
            Query q;
            q = session.createQuery("FROM HibernateMapping m WHERE m.map = ? AND m.dn is null ORDER BY m.account LIMIT 1");
            q.setString(0, map);
            HibernateMapping mapping = (HibernateMapping) q.uniqueResult();
            if (mapping == null) {
                tx.commit();
                return null;
            }
            mapping.setDn(userDN);
            tx.commit();
            return mapping.getAccount();
        // Handles when transaction goes wrong...
        } catch (Exception e) {
            log.error("Couldn't assign account to '" + userDN + "' from pool '" + map + "'", e);
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
    
    public void createMapping(String userDN, String account) {
        Session session = null;
        Transaction tx = null;
        try {
            log.trace("Creating mapping for map '" + map + "' DN '" + userDN + "' -> '" + account + "'");
            session = persistenceFactory.retrieveSessionFactory().openSession();
            tx = session.beginTransaction();
            /*Mapping map = retrieveMapping(session, tx, userDN);
            if (map != null) {
                return;
            }*/
            createMapping(session, tx, userDN, account);
            tx.commit();
        // Handles when transaction goes wrong...
        } catch (Exception e) {
            log.error("Couldn't create mapping to '" + map + "'", e);
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
   
    public int getNumberUnassignedMappings() {
        Session session = null;
        Transaction tx = null;
        try {
            log.trace("Retrieving number of unassigned mappings for pool '" + map + "'");
            session = persistenceFactory.retrieveSessionFactory().openSession();
            tx = session.beginTransaction();
            Query q;
            q = session.createQuery("FROM HibernateMapping m WHERE m.map = ? AND m.dn is null");
            q.setString(0, map);
            List mappings = (List) q.list();
            tx.commit();
            return mappings.size();
        // Handles when transaction goes wrong...
        } catch (Exception e) {
            log.error("Couldn't get number of unassigned mappings for pool '" + map + "'", e);
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

    public boolean removeMapping(String userDN) {
        Session session = null;
        Transaction tx = null;
        try {
            log.trace("Removing mapping from map '" + map + "' for DN '" + userDN + "'");
            session = persistenceFactory.retrieveSessionFactory().openSession();
            tx = session.beginTransaction();
            boolean result = removeMapping(session, tx, userDN);
            tx.commit();
            return result;
        // Handles when transaction goes wrong...
        } catch (Exception e) {
            log.error("Couldn't remove mapping from '" + map + "' for DN '" + userDN + "'", e);
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
    
    public String retrieveAccount(String userDN) {
        Session session = null;
        Transaction tx = null;
        try {
            log.trace("Retrieving account for user '" + userDN + "' from pool '" + map + "'");
            session = persistenceFactory.retrieveSessionFactory().openSession();
            tx = session.beginTransaction();
            Query q;
            q = session.createQuery("FROM HibernateMapping m WHERE m.map = ? AND m.dn = ? ORDER BY m.account LIMIT 1");
            q.setString(0, map);
            q.setString(1, userDN);
            HibernateMapping mapping = (HibernateMapping) q.uniqueResult();
            tx.commit();
            if (mapping == null) return null;
            return mapping.getAccount();
        // Handles when transaction goes wrong...
        } catch (Exception e) {
            log.error("Couldn't retrieve account for user '" + userDN + "' from pool '" + map + "'", e);
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

    public java.util.Map retrieveAccountMap() {
        Session session = null;
        Transaction tx = null;
        try {
            log.trace("Retrieving map for pool '" + map + "'");
            session = persistenceFactory.retrieveSessionFactory().openSession();
            tx = session.beginTransaction();
            Query q;
            q = session.createQuery("FROM HibernateMapping m WHERE m.map = ? AND m.dn is not null");
            q.setString(0, map);
            List mappings = (List) q.list();
            Iterator iter = mappings.iterator();
            Map map = new Hashtable();
            while (iter.hasNext()) {
                HibernateMapping mapping = (HibernateMapping) iter.next();
                map.put(mapping.getDn(), mapping.getAccount());
            }
            tx.commit();
            return map;
        // Handles when transaction goes wrong...
        } catch (Exception e) {
            log.error("Couldn't retrieve map for pool '" + map + "'", e);
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
        
    public String retrieveMapping(String userDN) {
        Session session = null;
        Transaction tx = null;
        try {
            log.trace("Retrieving mapping from map '" + map + "' for DN '" + userDN + "'");
            session = persistenceFactory.retrieveSessionFactory().openSession();
            tx = session.beginTransaction();
            HibernateMapping map = retrieveMapping(session, tx, userDN);
            tx.commit();
            if (map == null) return null;
            return map.getAccount();
        // Handles when transaction goes wrong...
        } catch (Exception e) {
            log.error("Couldn't retrieve mapping from '" + map + "' for DN '" + userDN + "'", e);
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

    public java.util.List retrieveMappings() {
        Session session = null;
        Transaction tx = null;
        try {
            // Retrieving members from the db
            log.trace("Retrieving mappings from map " + map);
            session = persistenceFactory.retrieveSessionFactory().openSession();
            tx = session.beginTransaction();
            List mappings = retrieveMappings(session, tx);
            tx.commit();
            return mappings;
        // Handles when transaction goes wrong...
        } catch (Exception e) {
            log.error("Couldn't retrieve members from map " + map, e);
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

    public java.util.List retrieveUsersNotUsedSince(java.util.Date date) {
        throw new UnsupportedOperationException("retrieveUsersNotUsedSince is not supported anymore");
    }

    public void unassignUser(String userDN) {
        Session session = null;
        Transaction tx = null;
        try {
            log.trace("Unassign account for user '" + userDN + "' from pool '" + map + "'");
            session = persistenceFactory.retrieveSessionFactory().openSession();
            tx = session.beginTransaction();
            Query q;
            q = session.createQuery("FROM HibernateMapping m WHERE m.map = ? AND m.dn = ? ORDER BY m.account LIMIT 1");
            q.setString(0, map);
            q.setString(1, userDN);
            HibernateMapping mapping = (HibernateMapping) q.uniqueResult();
            if (mapping == null) {
                tx.commit();
            }
            mapping.setDn(null);
            tx.commit();
        // Handles when transaction goes wrong...
        } catch (Exception e) {
            log.error("Couldn't unassign account for user '" + userDN + "' from pool '" + map + "'", e);
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

    private void createMapping(Session session, Transaction tx, String userDN, String account) throws Exception {
        HibernateMapping hMapping = new HibernateMapping();
        hMapping.setMap(map);
        hMapping.setDn(userDN);
        hMapping.setAccount(account);
        session.save(hMapping);
    }

    private boolean removeMapping(Session session, Transaction tx, String userDN) throws Exception {
        int n = session.delete("FROM HibernateMapping m WHERE m.map = ? AND m.dn = ?", new Object[] {map, userDN}, new Type[] {new StringType(), new StringType()});
        return n > 0;
    }

    private HibernateMapping retrieveMapping(Session session, Transaction tx, String userDN) throws Exception{
        Query q;
        q = session.createQuery("FROM HibernateMapping m WHERE m.map = ? AND m.dn = ?");
        q.setString(0, map);
        q.setString(1, userDN);
        return (HibernateMapping) q.uniqueResult();
    }
    
    private java.util.List retrieveMappings(Session session, Transaction tx) throws Exception {
        Query q;
        q = session.createQuery("FROM HibernateMapping m WHERE m.map = ?");
        q.setString(0, map);
        List hibernateMappings = q.list();
        return hibernateMappings;
    }
    
}
