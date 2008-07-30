package gov.bnl.gums.configuration;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.SAXException;

import gov.bnl.gums.GUMS;
import gov.bnl.gums.configuration.Configuration;
import gov.bnl.gums.db.ConfigurationDB;
import gov.bnl.gums.persistence.PersistenceFactory;

/** Implements the logic to store the configuration in a database except for the bare
 * configuration to specify the persistence factory at which to store the configuration.
 *
 * @author Jay Packard
 */
public class DBConfigurationStore extends ConfigurationStore {
	private Log log = LogFactory.getLog(FileConfigurationStore.class);
	private Log gumsSiteAdminLog = LogFactory.getLog(GUMS.siteAdminLog);
	private Log gumsResourceAdminLog = LogFactory.getLog(GUMS.resourceAdminLog);
	private ConfigurationDB configDB;
	private String schemaPath = null;
	private Date lastRetrieval = null;
	private Date lastModification = null;
	
	public DBConfigurationStore(ConfigurationDB configDB, String schemaPath) {
		this.configDB = configDB;
		this.schemaPath = schemaPath;
	}
	
	public void deleteBackupConfiguration(String dateStr) {
		Date date;
		try {
			date = format.parse(dateStr);
		} catch (ParseException e) {
			log.error(e);
			throw new RuntimeException(e);
		}
		configDB.deleteBackupConfiguration(date);
	}
	
    public boolean isActive() {
    	return configDB.isActive();
    }
    
    public boolean isReadOnly() {
    	return false;
    }
    
    public Collection getBackupConfigDates() {
    	return configDB.getBackupConfigDates(format);
    }
    
    public Date getLastModification() {
    	return configDB.getLastModification();
    }
    
    public boolean needsReload() {
    	return (lastRetrieval == null) || (lastRetrieval.before(lastModification));
    }
    
    public Configuration retrieveConfiguration() throws Exception {
    	String configText = configDB.retrieveCurrentConfiguration();
    	Configuration configuration = ConfigurationToolkit.loadConfiguration(null, configText, schemaPath);
   		return configuration;
    }
    
    public Configuration restoreConfiguration(String dateStr) throws Exception {
		Date date;
		try {
			date = format.parse(dateStr);
		} catch (ParseException e) {
			log.error(e);
			throw new RuntimeException(e);
		}
   		String configText = configDB.restoreConfiguration(date);
   		Configuration configuration = ConfigurationToolkit.loadConfiguration(null, configText, schemaPath);
   		return configuration;
    }
    
    public void setConfiguration(Configuration conf, boolean backupCopy) throws Exception {
    	configDB.setConfiguration(conf.toXml(), new Date(), backupCopy);
    }
	    
}
