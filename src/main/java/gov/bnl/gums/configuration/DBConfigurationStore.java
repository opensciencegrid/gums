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

import org.apache.log4j.Logger;
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
	private Logger log = Logger.getLogger(FileConfigurationStore.class);
	private ConfigurationDB configDB;
	private Configuration conf;
	private String schemaPath = null;
	private Date lastRetrieval = null;
	
	public DBConfigurationStore(ConfigurationDB configDB, String schemaPath) {
		this.configDB = configDB;
		this.schemaPath = schemaPath;
	}
	
	public boolean deleteBackupConfiguration(String dateStr) {
		Date date;
		try {
			date = format.parse(dateStr);
		} catch (ParseException e) {
			log.error(e);
			throw new RuntimeException(e);
		}
		return configDB.deleteBackupConfiguration(date);
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
    	Date date = configDB.getLastModification();
    	log.debug("Last database configuration modification is  " + date);
    	return date;
    }
    
    public boolean needsReload() {
    	return (lastRetrieval == null) || (lastRetrieval.before(getLastModification()));
    }
    
    public Configuration retrieveConfiguration() throws Exception {
    	try {
			if (needsReload()) {
		    	String configText = configDB.retrieveCurrentConfiguration();
		    	conf = ConfigurationToolkit.loadConfiguration(null, configText, schemaPath);
		    	lastRetrieval = new Date();
			}
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		return conf;
    }
    
    public Configuration retrieveConfiguration(boolean reload) throws Exception {
    	try {
			if (reload) {
		    	String configText = configDB.retrieveCurrentConfiguration();
		    	conf = ConfigurationToolkit.loadConfiguration(null, configText, schemaPath);
		    	lastRetrieval = new Date();
			}
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		return conf;
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
