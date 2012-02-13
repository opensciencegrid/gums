package gov.bnl.gums.db;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import gov.bnl.gums.persistence.HibernatePersistenceFactory;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import org.apache.log4j.Logger;

public class HibernateConfigurationDB implements ConfigurationDB {
	private Logger log = Logger.getLogger(LDAPUserGroupDB.class);
	private HibernatePersistenceFactory persistenceFactory;

	public HibernateConfigurationDB(HibernatePersistenceFactory persistenceFactory) {
		this.persistenceFactory = persistenceFactory;
		log.trace("LDAPConfigurationDB object create: factory " + persistenceFactory);
	}

	public boolean deleteBackupConfiguration(String name) {
		Session session = null;
		Transaction tx = null;
		try {
			session = persistenceFactory.retrieveSessionFactory().openSession();
			tx = session.beginTransaction();		
			Query q = session.createQuery("FROM HibernateConfig c where c.name = ?");
			q.setString(0, name);
			if (q.list().size() == 1) {
				HibernateConfig config = (HibernateConfig)q.list().get(0);
				session.delete(config);
				tx.commit();
				return true;
			}
			else {
				log.error("None or more than one configuration is stored for name " + name);
				throw new RuntimeException("None or more than one configuration is stored name " + name);
			}        	
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

	public Collection getBackupNames(DateFormat format) {
		Session session = null;
		Transaction tx = null;
		ArrayList list = new ArrayList();
		try {
			// Checks whether the value is present in the database
			session = persistenceFactory.retrieveSessionFactory().openSession();
			tx = session.beginTransaction();			
			Query q;
			q = session.createQuery("FROM HibernateConfig c WHERE c.current = FALSE AND c.name != null");
			Iterator it = q.list().iterator();
			if (q.list().size() > 0) {
				while (it.hasNext()) {
					HibernateConfig config = (HibernateConfig)it.next();
					if (config.getName() != null)
						list.add(config.getName());
				}
			}
			tx.commit();
			return list;
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
			q = session.createQuery("select c.timestamp from HibernateConfig c where c.current = TRUE");
			if (q.list().size() == 0)
				return new Date(0);
			else {
				Date date = (Date)((Date)q.list().get(0)).clone();
				return date;
			}
			// Handles when transaction goes wrong...
		} catch (Exception e) {
			log.error("Couldn't retrieve current configuration", e);
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
			if (tx != null)
				tx.commit();
			if (session != null)
				session.close();
		}
	}

	public boolean isActive() {
		return true;
	}

	public String restoreConfiguration(String name) {
		Session session = null;
		Transaction tx = null;
		try {
			// Checks whether the value is present in the database
			session = persistenceFactory.retrieveSessionFactory().openSession();
			tx = session.beginTransaction();			
			Query q;
			q = session.createQuery("FROM HibernateConfig c WHERE c.name = ?");
			q.setString(0, name);
			if (q.list().size() == 1) {
				String xml = ((HibernateConfig)q.list().get(0)).getXml();
				tx.commit();
				return xml;
			}
			else {
				throw new RuntimeException("None or more than one configuration is stored name " + name);
			}
			// Handles when transaction goes wrong...
		} catch (Exception e) {
			log.error("Couldn't restore configuration", e);
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

	public void setConfiguration(String text, boolean backupCopy, String name, Date date) {
		Session session = null;
		Transaction tx = null;
		
		try {
			// Checks whether the value is present in the database
			session = persistenceFactory.retrieveSessionFactory().openSession();
			tx = session.beginTransaction();

			if (backupCopy) {
				// delete any configurations with the same name
				Query q = session.createQuery("FROM HibernateConfig c WHERE c.name = ?");
				q.setString(0, name);
				Iterator it = q.list().iterator();
				while (it.hasNext()) {
					HibernateConfig hibernateConfig = (HibernateConfig)it.next();
					if (hibernateConfig.getTimestamp().compareTo(date)==0)
						session.delete(hibernateConfig);
				}
			}
			else
			{
				// delete current configuration
				Query q = session.createQuery("DELETE FROM HibernateConfig WHERE current = TRUE");
				q.executeUpdate();
			}

			// add new configuration
			HibernateConfig config = new HibernateConfig();
			config.setXml(text);
			config.setTimestamp(date);
			config.setCurrent(new Boolean(!backupCopy));
			config.setName(name);
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
