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
    
	public void deleteBackupConfiguration(String dateStr) {
		
	}
	
	public Collection getBackupConfigDates() {
		return null;
	}
	
	public Date getLastModification() {
		return null;
	}
	
	public boolean isActive() {
		return false;
	}

	public String restoreConfiguration(String dateStr) {
		return null;
	}
	
	public String retrieveCurrentConfiguration() {
		return null;
	}
	
	public void setConfiguration(String text, String dateStr, boolean backupCopy) {
		
	} 
}
