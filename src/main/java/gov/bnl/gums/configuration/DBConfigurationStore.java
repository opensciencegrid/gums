package gov.bnl.gums.configuration;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
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
	private Date lastRetrieval = null;
	
	public DBConfigurationStore(ConfigurationDB configDB) {
		this.configDB = configDB;
	}
	
	public void deleteBackupConfiguration(String name) {
		if (!configDB.deleteBackupConfiguration(name))
			throw new RuntimeException("Could not delete backup configuration '"+name+"' from database");
	}
	
    public boolean isActive() {
    	return configDB.isActive();
    }
    
    public boolean isReadOnly() {
    	return false;
    }
    
    public Collection getBackupNames() {
    	return configDB.getBackupNames(format);
    }
    
    public Date getLastModification() {
    	Date date = configDB.getLastModification();
    	if (log.isTraceEnabled())
    		log.trace("Last database configuration modification is " + date);
    	return date;
    }
    
    public boolean needsReload() {
    	return (lastRetrieval == null) || (lastRetrieval.before(getLastModification()));
    }
    
    public Configuration retrieveConfiguration() throws Exception {
    	ByteArrayInputStream stream = null;
    	try {
			if (needsReload()) {
		    	String configText = configDB.retrieveCurrentConfiguration();
		    	conf = ConfigurationToolkit.parseConfiguration(configText, false);
		    	lastRetrieval = new Date();
			}
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		} finally {
			if (stream != null)
				stream.close();
		}
		return conf;
    }
    
    public Configuration retrieveConfiguration(boolean reload) throws Exception {
    	ByteArrayInputStream stream = null;
    	try {
			if (reload) {
		    	String configText = configDB.retrieveCurrentConfiguration();
		    	conf = ConfigurationToolkit.parseConfiguration(configText, false);
		    	lastRetrieval = new Date();
			}
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		} finally {
			if (stream != null)
				stream.close();
		}
		return conf;
    }
    
    public Configuration restoreConfiguration(String name) throws Exception {
    	ByteArrayInputStream stream = null;
    	try {
	    	String configText = configDB.restoreConfiguration(name);
	    	Configuration configuration = ConfigurationToolkit.parseConfiguration(configText, false);
	    	return configuration;
    	} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		} finally {
			if (stream != null)
				stream.close();
		}
    }
    
    public void setConfiguration(Configuration conf, boolean backupCopy, String name) throws Exception {
    	configDB.setConfiguration(conf.toXml(), new Date(), backupCopy, name);
    }
	    
}
