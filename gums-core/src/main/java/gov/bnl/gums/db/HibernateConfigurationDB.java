package gov.bnl.gums.db;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import gov.bnl.gums.persistence.HibernatePersistenceFactory;

import net.sf.hibernate.Query;
import net.sf.hibernate.Session;
import net.sf.hibernate.Transaction;
import net.sf.hibernate.type.DateType;
import net.sf.hibernate.type.Type;

import org.apache.log4j.Logger;

public class HibernateConfigurationDB implements ConfigurationDB {
    private Logger log = Logger.getLogger(LDAPUserGroupDB.class);
    private HibernatePersistenceFactory persistenceFactory;
    
    public HibernateConfigurationDB(HibernatePersistenceFactory persistenceFactory) {
        this.persistenceFactory = persistenceFactory;
        log.trace("LDAPConfigurationDB object create: factory " + persistenceFactory);
    }
    
	public boolean deleteBackupConfiguration(Date date) {
		Session session = null;
		Transaction tx = null;
		try {
			session = persistenceFactory.retrieveSessionFactory().openSession();
            tx = session.beginTransaction();		
            Query q = session.createQuery("FROM HibernateConfig c");
        	Iterator it = q.list().iterator();
        	boolean deleted = false;
        	while (it.hasNext()) {
        		HibernateConfig hibernateConfig = (HibernateConfig)it.next();
        		if (hibernateConfig.getTimestamp().compareTo(date)==0) {
        			session.delete(hibernateConfig);
        			deleted = true;
        		}
        	}
	        tx.commit();
	        return deleted;
        // Handles when transaction goes wrong...
        } catch (Exception e) {
            log.error("Couldn't retrieve backup configuration", e);
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
	
	public Collection getBackupConfigDates(DateFormat format) {
		Session session = null;
		Transaction tx = null;
		ArrayList configDates = new ArrayList();
		try {
		    // Checks whether the value is present in the database
			session = persistenceFactory.retrieveSessionFactory().openSession();
            tx = session.beginTransaction();			
		    Query q;
	        q = session.createQuery("FROM HibernateConfig c WHERE c.current = FALSE");
		    Iterator it = q.list().iterator();
		    if (q.list().size() > 0) {
			    while (it.hasNext()) {
			    	HibernateConfig config = (HibernateConfig)it.next();
			    	configDates.add(format.format(config.getTimestamp()));
			    }
		    }
		    tx.commit();
		    return configDates;
        // Handles when transaction goes wrong...
        } catch (Exception e) {
            log.error("Couldn't retrive backup configuration dates", e);
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
	
	public Date getLastModification() {
		Session session = null;
		Transaction tx = null;
		try {
		    // Checks whether the value is present in the database
			session = persistenceFactory.retrieveSessionFactory().openSession();
            tx = session.beginTransaction();			
		    Query q;
	        q = session.createQuery("FROM HibernateConfig c WHERE c.current = TRUE");
		    if (q.list().size() != 1) {
		    	log.error("None or more than one configuration is set to current in the database");
		    	throw new RuntimeException("None or more than one configuration is set to current in the database");
		    }
		    tx.commit();
			Date date = (Date)((HibernateConfig)q.list().get(0)).getTimestamp().clone();
			return date;
        // Handles when transaction goes wrong...
        } catch (Exception e) {
            log.error("Couldn't retrive current configuration", e);
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
	
	public boolean isActive() {
		return true;
	}

	public String restoreConfiguration(Date date) {
		Session session = null;
		Transaction tx = null;
		try {
		    // Checks whether the value is present in the database
			session = persistenceFactory.retrieveSessionFactory().openSession();
            tx = session.beginTransaction();			
		    Query q;
	        q = session.createQuery("FROM HibernateConfig c");
	        Iterator it = q.list().iterator();
	        HibernateConfig hibernateConfig = null;
          	while (it.hasNext()) {
        		hibernateConfig = (HibernateConfig)it.next();
        		if (hibernateConfig.getTimestamp().compareTo(date)==0)
        			break;
        	}
		    if (hibernateConfig == null) {
		    	log.error("None or more than one configuration is stored for one date " + date.toLocaleString());
		    	throw new RuntimeException("None or more than one configuration is stored for date " + date.toLocaleString());
		    }
		    tx.commit();
		    return new String(hibernateConfig.getXml());
        // Handles when transaction goes wrong...
        } catch (Exception e) {
            log.error("Couldn't retrive current configuration", e);
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
	
	public String retrieveCurrentConfiguration() {
		Session session = null;
		Transaction tx = null;
		try {
		    // Checks whether the value is present in the database
			session = persistenceFactory.retrieveSessionFactory().openSession();
            tx = session.beginTransaction();			
		    Query q;
	        q = session.createQuery("FROM HibernateConfig c WHERE c.current = TRUE");
		    if (q.list().size() != 1) {
		    	log.error("None or more than one configuration is set to current in the database");
		    	throw new RuntimeException("None or more than one configuration is set to current in the database");
		    }
		    tx.commit();
		    return new String(((HibernateConfig)q.list().get(0)).getXml());
        // Handles when transaction goes wrong...
        } catch (Exception e) {
            log.error("Couldn't retrive current configuration", e);
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
	
	public void setConfiguration(String text, Date date, boolean backupCopy) {
        Session session = null;
        Transaction tx = null;
        try {
            // Checks whether the value is present in the database
        	session = persistenceFactory.retrieveSessionFactory().openSession();
            tx = session.beginTransaction();

            if (backupCopy) {
            	// delete any configurations with the same timestamp
                Query q = session.createQuery("FROM HibernateConfig c");
            	Iterator it = q.list().iterator();
              	while (it.hasNext()) {
            		HibernateConfig hibernateConfig = (HibernateConfig)it.next();
            		if (hibernateConfig.getTimestamp().compareTo(date)==0)
            			session.delete(hibernateConfig);
            	}
            }
            else
            	// delete current configuration
        		session.delete("FROM HibernateConfig c WHERE c.current = TRUE");
            
            if (!backupCopy) {
	            // deactivate current config if one exists
	            Query q = session.createQuery("FROM HibernateConfig c WHERE c.current = TRUE");
			    if (q.list().size() > 1) {
			    	log.error("More than one configuration is set to current in the database");
			    	throw new RuntimeException("More than one configuration is set to current in the database");
			    }
			    if (q.list().size() == 1)
			    	((HibernateConfig)q.list().get(0)).setCurrent(new Boolean(false));
            }
            
            // add new configuration
            HibernateConfig config = new HibernateConfig();
            config.setXml(text);
            config.setTimestamp(date);
            config.setCurrent(new Boolean(!backupCopy));
            session.save(config);

            tx.commit();
        // Handles when transaction goes wrong...
        } catch (Exception e) {
            log.error("Couldn't set configuration", e);
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

}
