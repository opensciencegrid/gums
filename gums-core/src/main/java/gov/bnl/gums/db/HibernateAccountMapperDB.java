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


import gov.bnl.gums.GridUser;
import gov.bnl.gums.persistence.HibernatePersistenceFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.type.StringType;
import org.hibernate.type.Type;

import org.apache.log4j.Logger;

/**
 *
 * @author Gabriele Carcassi, Jay Packard
 */
public class HibernateAccountMapperDB implements ManualAccountMapperDB, AccountPoolMapperDB {
    private Logger log = Logger.getLogger(HibernateAccountMapperDB.class);
    private HibernatePersistenceFactory persistenceFactory;
    private String map;
    private static Map needsCacheRefresh = Collections.synchronizedMap(new HashMap());
    
    /** Creates a new instance of HibernateMapping */
    public HibernateAccountMapperDB(HibernatePersistenceFactory persistenceFactory, String map) {
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
            HibernateMapping map = retrieveMapping(session, tx, null, account);
            if (map != null) {
                return;
            }
            createMapping(session, tx, null, account);
            tx.commit();
            setNeedsCacheRefresh(true);
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
    
    public String assignAccount(GridUser user) {
        Session session = null;
        Transaction tx = null;
        try {
            log.trace("Assing an account to '" + user.getCertificateDN() + "' from pool '" + map + "'");
            session = persistenceFactory.retrieveSessionFactory().openSession();
            tx = session.beginTransaction();
            Query q;
            q = session.createQuery("FROM HibernateMapping m WHERE m.map = ? AND m.dn is null ORDER BY m.account");
            q.setMaxResults(1);
            q.setString(0, map);
            HibernateMapping mapping = (HibernateMapping) q.uniqueResult();
            if (mapping == null) {
                tx.commit();
                return null;
            }
            mapping.setDn(user.getCertificateDN());
            tx.commit();
            setNeedsCacheRefresh(true);
            return mapping.getAccount();
        // Handles when transaction goes wrong...
        } catch (Exception e) {
            log.error("Couldn't assign account to '" + user.getCertificateDN() + "' from pool '" + map + "'", e);
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
            HibernateMapping map = retrieveMapping(session, tx, userDN, account);
            if (map != null) {
                return;
            }
            createMapping(session, tx, userDN, account);
            tx.commit();
            setNeedsCacheRefresh(true);
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
    
    public String getMap() {
    	return map;
    }
    
    public boolean needsCacheRefresh() {
		if (needsCacheRefresh.get(map) != null)
			return ((Boolean)needsCacheRefresh.get(map)).booleanValue();
		else
			return true;
    }

    public boolean removeAccount(String account) {
        Session session = null;
        Transaction tx = null;
        try {
            log.trace("Removing mapping from map '" + map + "' for account '" + account + "'");
            session = persistenceFactory.retrieveSessionFactory().openSession();
            tx = session.beginTransaction();
            boolean result = removeAccount(session, tx, account);
            tx.commit();
            setNeedsCacheRefresh(true);
            return result;
        // Handles when transaction goes wrong...
        } catch (Exception e) {
            log.error("Couldn't remove mapping from '" + map + "' for account '" + account + "'", e);
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
            setNeedsCacheRefresh(true);
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

    public String retrieveAccount(GridUser user) {
        Session session = null;
        Transaction tx = null;
        String userDN = user.getCertificateDN();
        try {
            log.trace("Retrieving account for user '" + userDN + "' from pool '" + map + "'");
            session = persistenceFactory.retrieveSessionFactory().openSession();
            tx = session.beginTransaction();
            Query q;
            q = session.createQuery("FROM HibernateMapping m WHERE m.map = ? AND m.dn = ? ORDER BY m.account");
            q.setMaxResults(1);
            q.setString(0, map);
            q.setString(1, userDN);
            q.setCacheable(true).setCacheRegion("gumsmapper");
            HibernateMapping mapping = (HibernateMapping) q.uniqueResult();
            tx.commit();
            if (mapping == null) 
            	return null;
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
            q.setCacheable(true).setCacheRegion("gumsmapper");
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
            if (map == null) 
            	return null;
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
    
    public java.util.Map retrieveReverseAccountMap() {
        Session session = null;
        Transaction tx = null;
        try {
            log.trace("Retrieving reverse map for pool '" + map + "'");
            session = persistenceFactory.retrieveSessionFactory().openSession();
            tx = session.beginTransaction();
            Query q;
            q = session.createQuery("FROM HibernateMapping m WHERE m.map = ? AND m.account is not null");
            q.setCacheable(true).setCacheRegion("gumsmapper");
            q.setString(0, map);
            List mappings = (List) q.list();
            Iterator iter = mappings.iterator();
            Map reverseMap = new Hashtable();
            while (iter.hasNext()) {
                HibernateMapping mapping = (HibernateMapping) iter.next();
                reverseMap.put(mapping.getAccount(), mapping.getDn()!=null?mapping.getDn():"");
            }
            tx.commit();
            return reverseMap;
        // Handles when transaction goes wrong...
        } catch (Exception e) {
            log.error("Couldn't retrieve reverse map for pool '" + map + "'", e);
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

    public void setCacheRefreshed() {
    	setNeedsCacheRefresh(false);
    }
    
    public void unassignAccount(String account) {
        Session session = null;
        Transaction tx = null;
        try {
            log.trace("Unassign account '" + account + "' from pool '" + map + "'");
            session = persistenceFactory.retrieveSessionFactory().openSession();
            tx = session.beginTransaction();
            Query q;
            q = session.createQuery("FROM HibernateMapping m WHERE m.map = ? AND m.account = ?");
            q.setString(0, map);
            q.setString(1, account);
            List mappings = (List) q.list();
            Iterator iter = mappings.iterator();
            while (iter.hasNext()) {
                HibernateMapping mapping = (HibernateMapping) iter.next();
                mapping.setDn(null);
            }
            tx.commit();
            setNeedsCacheRefresh(true);
        // Handles when transaction goes wrong...
        } catch (Exception e) {
            log.error("Couldn't unassign account '" + account + "' from pool '" + map + "'", e);
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
    
    public void unassignUser(String userDN) {
        Session session = null;
        Transaction tx = null;
        try {
            log.trace("Unassign account for user '" + userDN + "' from pool '" + map + "'");
            session = persistenceFactory.retrieveSessionFactory().openSession();
            tx = session.beginTransaction();
            Query q;
            q = session.createQuery("FROM HibernateMapping m WHERE m.map = ? AND m.dn = ? ORDER BY m.account");
            q.setMaxResults(1);
            q.setString(0, map);
            q.setString(1, userDN);
            HibernateMapping mapping = (HibernateMapping) q.uniqueResult();
            mapping.setDn(null);
            tx.commit();
            setNeedsCacheRefresh(true);
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
            throw new RuntimeException("Database error: " + map + " " + userDN + " " + e.getMessage(), e);
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

    private boolean removeAccount(Session session, Transaction tx, String account) throws Exception {
    	Query q;
        q = session.createQuery("DELETE FROM HibernateMapping WHERE map = ? AND account = ?");
        q.setString(0, map);
        q.setString(1, account);
        return q.executeUpdate() > 0;
    }

    private boolean removeMapping(Session session, Transaction tx, String userDN) throws Exception {
    	Query q;
        q = session.createQuery("DELETE FROM HibernateMapping WHERE map = ? AND dn = ?");
        q.setString(0, map);
        q.setString(1, userDN);
        return q.executeUpdate() > 0;
    }    
    
    private HibernateMapping retrieveMapping(Session session, Transaction tx, String userDN) throws Exception{
        Query q;
        q = session.createQuery("FROM HibernateMapping m WHERE m.map = ? AND m.dn = ?");
        q.setString(0, map);
        q.setString(1, userDN);
        q.setCacheable(true).setCacheRegion("gumsmapper");
        return (HibernateMapping) q.uniqueResult();
    }
    
    private HibernateMapping retrieveMapping(Session session, Transaction tx, String userDN, String account) throws Exception{
        Query q;
        if (userDN!=null) {
	        q = session.createQuery("FROM HibernateMapping m WHERE m.map = ? AND m.dn = ? AND m.account = ? ");
	        q.setString(0, map);
	        q.setString(1, userDN);
	        q.setString(2, account);
                q.setCacheable(true).setCacheRegion("gumsmapper");
        }
        else {
	        q = session.createQuery("FROM HibernateMapping m WHERE m.map = ? AND m.dn is null AND m.account = ? ");
	        q.setString(0, map);
	        q.setString(1, account);
                q.setCacheable(true).setCacheRegion("gumsmapper");
        }
        return (HibernateMapping) q.uniqueResult();
    }
    
    private void setNeedsCacheRefresh(boolean value) {
        try {
            persistenceFactory.retrieveSessionFactory().getCache().evictQueryRegion("gumsmapper");
        } catch (Exception e) {
            log.error("Failed to clear manual user mapping cache.", e);
        }
    	needsCacheRefresh.put(map, new Boolean(value));
    }
    
}
