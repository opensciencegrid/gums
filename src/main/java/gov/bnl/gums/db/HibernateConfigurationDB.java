package gov.bnl.gums.db;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import gov.bnl.gums.persistence.HibernatePersistenceFactory;

import net.sf.hibernate.Query;
import net.sf.hibernate.Session;
import net.sf.hibernate.Transaction;
import net.sf.hibernate.type.DateType;
import net.sf.hibernate.type.Type;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class HibernateConfigurationDB implements ConfigurationDB {
    private Log log = LogFactory.getLog(LDAPUserGroupDB.class);
    private HibernatePersistenceFactory persistenceFactory;
    
    public HibernateConfigurationDB(HibernatePersistenceFactory persistenceFactory) {
        this.persistenceFactory = persistenceFactory;
        log.trace("LDAPConfigurationDB object create: factory " + persistenceFactory);
    }
    
	public void deleteBackupConfiguration(Date date) {
		Session session = null;
		Transaction tx = null;
		try {
			session = persistenceFactory.retrieveSessionFactory().openSession();
            tx = session.beginTransaction();			
	        int n = session.delete("FROM HibernateConfig c WHERE c.timestamp = ?", new Object[] {date}, new Type[] {new DateType()});
		    tx.commit();
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
	
	public Collection getBackupConfigDates() {
		Session session = null;
		Transaction tx = null;
		ArrayList configDates = null;
		try {
		    // Checks whether the value is present in the database
			session = persistenceFactory.retrieveSessionFactory().openSession();
            tx = session.beginTransaction();			
		    Query q;
	        q = session.createQuery("FROM HibernateConfig c WHERE c.current = FALSE");
		    if (q.list().size() != 1) {
		    	log.error("None or more than one configuration is set to current in the database");
		    	throw new RuntimeException("None or more than one configuration is set to current in the database");
		    }
		    configDates = new ArrayList(q.list());
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
	        q = session.createQuery("FROM HibernateConfig c WHERE c.timestamp = ?");
	        q.setDate(0, date);
		    if (q.list().size() != 1) {
		    	log.error("None or more than one configuration is stored for one date" + date.toString());
		    	throw new RuntimeException("None or mMore than one configuration is stored for date" + date.toString());
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

            if (backupCopy)
            	// delete any configurations with the same timestamp
            	session.delete("FROM HibernateConfig c WHERE c.timestamp = ?", new Object[] {date}, new Type[] {new DateType()});
            else
            	// delete current configuration
        		session.delete("FROM HibernateConfig c WHERE c.current = TRUE", new Object[] {}, new Type[] {});
            
            if (!backupCopy) {
	            // deactivate current config if one exists
	            Query q;
		        q = session.createQuery("FROM HibernateConfig c WHERE c.current = TRUE");
			    if (q.list().size() > 1) {
			    	log.error("More than one configuration is set to current in the database");
			    	throw new RuntimeException("More than one configuration is set to current in the database");
			    }
			    if (q.list().size() != 0)
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
