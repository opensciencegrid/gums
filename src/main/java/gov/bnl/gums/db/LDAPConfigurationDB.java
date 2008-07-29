package gov.bnl.gums.db;

import java.util.Collection;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import gov.bnl.gums.persistence.HibernatePersistenceFactory;
import gov.bnl.gums.persistence.LDAPPersistenceFactory;

public class LDAPConfigurationDB implements ConfigurationDB {
    private Log log = LogFactory.getLog(LDAPUserGroupDB.class);
    private LDAPPersistenceFactory factory;
    
    public LDAPConfigurationDB(LDAPPersistenceFactory factory) {
        this.factory = factory;
        log.trace("LDAPConfigurationDB object create: factory " + factory);
    }
    
	public void deleteBackupConfiguration(Date date) {
		throw new RuntimeException("LDAP Configuration DB not yet supported");
	}
	
	public Collection getBackupConfigDates() {
		throw new RuntimeException("LDAP Configuration DB not yet supported");
	}
	
	public Date getLastModification() {
		throw new RuntimeException("LDAP Configuration DB not yet supported");
	}
	
	public boolean isActive() {
		return false;
	}

	public String restoreConfiguration(Date date) {
		throw new RuntimeException("LDAP Configuration DB not yet supported");
	}
	
	public String retrieveCurrentConfiguration() {
		throw new RuntimeException("LDAP Configuration DB not yet supported");
	}
	
	public void setConfiguration(String text, Date date, boolean backupCopy) {
		throw new RuntimeException("LDAP Configuration DB not yet supported");
	} 
}
