package gov.bnl.gums.db;

import java.text.DateFormat;
import java.util.Collection;
import java.util.Date;

import org.apache.log4j.Logger;

import gov.bnl.gums.persistence.HibernatePersistenceFactory;
import gov.bnl.gums.persistence.LDAPPersistenceFactory;

public class LDAPConfigurationDB implements ConfigurationDB {
    private Logger log = Logger.getLogger(LDAPUserGroupDB.class);
    private LDAPPersistenceFactory factory;
    
    public LDAPConfigurationDB(LDAPPersistenceFactory factory) {
        this.factory = factory;
        log.trace("LDAPConfigurationDB object create: factory " + factory);
    }
    
	public boolean deleteBackupConfiguration(String name) {
		throw new RuntimeException("LDAP Configuration DB not yet supported");
	}
	
	public Collection getBackupNames(DateFormat format) {
		throw new RuntimeException("LDAP Configuration DB not yet supported");
	}
	
	public Date getLastModification() {
		throw new RuntimeException("LDAP Configuration DB not yet supported");
	}
	
	public boolean isActive() {
		return false;
	}

	public String restoreConfiguration(String name) {
		throw new RuntimeException("LDAP Configuration DB not yet supported");
	}
	
	public String retrieveCurrentConfiguration() {
		throw new RuntimeException("LDAP Configuration DB not yet supported");
	}
	
	public void setConfiguration(String text, boolean backupCopy, String name, Date date) {
		throw new RuntimeException("LDAP Configuration DB not yet supported");
	} 
}
