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
import gov.bnl.gums.account.MappedAccountInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import java.sql.Timestamp;

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
    private Set<String> recentAccounts;
    private final boolean recyclable;
 
    /** Creates a new instance of HibernateMapping */
    public HibernateAccountMapperDB(HibernatePersistenceFactory persistenceFactory, String map, boolean recyclable) {
        this.persistenceFactory = persistenceFactory;
        this.map = map;
        this.recyclable = recyclable;
        this.recentAccounts = Collections.synchronizedSet(new HashSet<String>());
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
            mapping.setRecycle(recyclable);
            mapping.setLastuse(new java.sql.Timestamp(System.currentTimeMillis()));
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
            recordAccess(session, mapping);
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
                recordAccess(session, mapping);
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
            recordAccess(session, map);
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
    
    public Map<String, String> retrieveReverseAccountMap() {
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

    public List<? extends MappedAccountInfo> retrieveAccountInfo() {
        Session session = null;
        Transaction tx = null;
        try {
            log.trace("Retrieving account info for pool '" + map + "'");
            session = persistenceFactory.retrieveSessionFactory().openSession();
            tx = session.beginTransaction();
            Query q;
            q = session.createQuery("FROM HibernateMapping m WHERE m.map = ? AND m.account is not null");
            q.setCacheable(true).setCacheRegion("gumsmapper");
            q.setString(0, map);
            List<HibernateMapping> mappings = (List) q.list();
            tx.commit();
            return mappings;
        // Handles when transaction goes wrong...
        } catch (Exception e) {
            log.error("Couldn't retrieve account info for pool '" + map + "'", e);
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
            hMapping.setLastuse(new java.sql.Timestamp(System.currentTimeMillis()));
            hMapping.setRecycle(recyclable);
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
        HibernateMapping hMapping = (HibernateMapping) q.uniqueResult();
        recordAccess(session, hMapping);
        return hMapping;
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
        HibernateMapping hMapping = (HibernateMapping) q.uniqueResult();
        recordAccess(session, hMapping);
        return hMapping;
    }

    private void recordAccess(Session session, HibernateMapping hMapping) {

        if (hMapping == null) {return;}

        String account = hMapping.getAccount();
        if (account != null) {recentAccounts.add(account);}

        // IMPORTANT: If we are not a recyclable pool group and the
        // account was indeed recyclable, then mark is as not recyclable.
        if (!recyclable && hMapping.getRecycle()) {
            hMapping.setRecycle(false);
            session.save(hMapping);
        }
    }

    public void setAccountRecyclable(String account, boolean recycle) {

        Session session = null;
        Transaction tx = null;
        try {
            log.trace("Setting account '" + account + "' recyclable setting to " + recycle);
            session = persistenceFactory.retrieveSessionFactory().openSession();
            tx = session.beginTransaction();

            Query q = session.createQuery("UPDATE HibernateMapping SET recycle = ? WHERE map = ? AND account = ?");
            q.setBoolean(0, recycle);
            q.setString(1, this.map);
            q.setString(2, account);
            q.executeUpdate();
            tx.commit();
            setNeedsCacheRefresh(true);

        // Handles when transaction goes wrong...
        } catch (Exception e) {
            log.error("Couldn't set account '" + account + "' recyclable setting to " + recycle, e);
            if (tx != null) {
                try {
                    tx.rollback();
                } catch (Exception e1) {
                    log.error("Hibernate error: rollback failed", e1);
                    throw new RuntimeException("Database errors: " + e.getMessage() + " - " + e1.getMessage(), e);
                }
            }
            throw new RuntimeException("Database error: " + map + ": " + e.getMessage(), e);
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

    public boolean cleanAccounts(int days) {
        boolean successful = true;

        Session session = null;
        Transaction tx = null;
        int userCount = 0;
        try {
            log.trace("Cleaning accounts unused in the last " + days + " days and updating usage for " + recentAccounts.size() + " users.");
            session = persistenceFactory.retrieveSessionFactory().openSession();
            tx = session.beginTransaction();

            Timestamp expiry = new Timestamp(System.currentTimeMillis());
            Query q = session.createQuery("UPDATE HibernateMapping SET lastuse = ? WHERE map = ? AND account = ?");
            q.setTimestamp(0, expiry);
            q.setString(1, this.map);
            synchronized (recentAccounts) {
                for (String account : recentAccounts) {
                    q.setString(2, account);
                    successful = (q.executeUpdate() > 0) && successful;
                    userCount += 1;
                }
                recentAccounts.clear();
            }

            Query q2;
            q2 = session.createQuery("UPDATE HibernateMapping SET dn = null WHERE map = ? AND recycle = true AND lastuse < ? AND lastuse > 0");
            expiry = new Timestamp(System.currentTimeMillis() - days*86400*1000);
            q2.setString(0, this.map);
            q2.setTimestamp(1, expiry);
            if (days > 0) {
                q2.executeUpdate();
            }
            tx.commit();
            setNeedsCacheRefresh(true);

        // Handles when transaction goes wrong...
        } catch (Exception e) {
            log.error("Couldn't clean from last " + days + " and update usage for the " + userCount + " users.", e);
            if (tx != null) {
                try {
                    tx.rollback();
                } catch (Exception e1) {
                    log.error("Hibernate error: rollback failed", e1);
                    throw new RuntimeException("Database errors: " + e.getMessage() + " - " + e1.getMessage(), e);
                }
            }
            throw new RuntimeException("Database error: " + map + ": " + e.getMessage(), e);
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

        return successful;
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
