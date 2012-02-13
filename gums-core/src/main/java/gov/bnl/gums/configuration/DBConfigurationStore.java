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
import gov.bnl.gums.persistence.HibernatePersistenceFactory;
import gov.bnl.gums.persistence.LocalPersistenceFactory;
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
	private Date mostRecentModification = null;

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

	public Collection getBackupNames() {
		return configDB.getBackupNames(format);
	}

	public Date getLastModification() {
		Date date = configDB.getLastModification();
		if (log.isTraceEnabled())
			log.trace("Last modification from database: "+date.toString());
		return date;
	}

	public synchronized Configuration retrieveConfiguration() throws Exception {
		ByteArrayInputStream stream = null;
		try {
			Date lastModification = getLastModification(); 
			if (mostRecentModification==null || mostRecentModification.before(lastModification)) {
				String configText = configDB.retrieveCurrentConfiguration();
				this.conf = ConfigurationToolkit.parseConfiguration(configText, false);
				log.debug("Configuration "+conf+" reloaded from database");
				mostRecentModification = lastModification;
				return conf;
			}
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		} finally {
			if (stream != null)
				stream.close();
		}
		return conf;
	}

	public synchronized Configuration restoreConfiguration(String name) throws Exception {
		ByteArrayInputStream stream = null;
		try {
			String configText = configDB.restoreConfiguration(name);
			this.conf = ConfigurationToolkit.parseConfiguration(configText, false);
			log.debug("Configuration '" + name + "', "+conf+" restored from database");
			mostRecentModification = getLastModification();
			return conf;
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		} finally {
			if (stream != null)
				stream.close();
		}
	}

	public synchronized void setConfiguration(Configuration conf, boolean backupCopy, String name, Date date) throws Exception {
                if (date==null)
                        date = new Date();

		if (backupCopy && (name==null || name.length()==0))
			name = format.format(date); 
		
		configDB.setConfiguration(conf.toXml(), backupCopy, name, date);
		log.debug("Configuration "+conf+" saved to database");
		this.conf = conf;
		mostRecentModification = date;
	}

}
