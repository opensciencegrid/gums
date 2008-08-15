/*
 * FileConfigurationStore.java
 *
 * Created on October 20, 2004, 12:48 PM
 */

package gov.bnl.gums.configuration;

import gov.bnl.gums.GUMS;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;

import org.apache.commons.logging.*;

/** Implements the logic to retrieve the configuration from the gums.config file
 * taken from the classpath. The file will be reloaded as soon as if it changes,
 * on demand (no polling).
 *
 * @author Gabriele Carcassi, Jay Packard
 */
public class FileConfigurationStore extends ConfigurationStore {
	private Log log = LogFactory.getLog(FileConfigurationStore.class);
	private Log gumsSiteAdminLog = LogFactory.getLog(GUMS.siteAdminLog);
	private Log gumsResourceAdminLog = LogFactory.getLog(GUMS.resourceAdminLog);
	private Configuration conf;
	private Date lastRetrieval = null;
	private String configBackupDir = null;
	private String configPath = null;
	private String schemaPath = null;
	private String transformPath = null;
	private String version;

	/**
	 * Copy source to target
	 * 
	 * @param source
	 * @param target
	 */
	static public void copyFile(String source, String target) {
		try {
			if (System.getProperty("os.name").indexOf("Linux")!=-1) {
				// This will preserve soft links
				String[] cpArgs = new String[4];
				cpArgs[0] = "/bin/cp";
				cpArgs[1] = "-f";
				cpArgs[2] = source;
				cpArgs[3] = target;
				if (Runtime.getRuntime().exec(cpArgs).waitFor() != 0)
					throw new RuntimeException("Error copying file");
			}
			else {
				FileInputStream fis  = new FileInputStream(source);
				FileOutputStream fos = new FileOutputStream(target);
				byte[] buf = new byte[1024];
				int i = 0;
				while((i=fis.read(buf))!=-1)
					fos.write(buf, 0, i);
				fis.close();
				fos.close();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Move source to target
	 * 
	 * @param source
	 * @param target
	 */
	static public void moveFile(String source, String target) {
		copyFile(source, target);
		new File(source).delete();
	}	
	
	/**
	 * Creates a new FileConfigurationStore object.
	 * Used to instantiate class when run as a unit test.
	 */
	public FileConfigurationStore() {
		URL resource = getClass().getClassLoader().getResource("gums.config");
		if (resource!=null) {
			String configDir = resource.getPath().replace("/gums.config", "");
			this.configPath = configDir+"/gums.config";
			this.schemaPath = configDir+"/gums.config.schema";
			this.transformPath = configDir+"/gums.config.transform";   
			this.configBackupDir = configDir+"/backup";
		}
	}

	/**
	 * Creates a new FileConfigurationStore object.
	 * Allows for specifying the absolute name of the configuration file.
	 * Used to instantiate class when GUMS is run within servlet.
	 * 
	 * @param filename
	 * @param create if true, a new barbones configuration file will be created
	 * at given filename if no file currently exists there
	 */
	public FileConfigurationStore(String configDir, String resourceDir, String version, boolean create) {
		this.version = version;
		this.configPath = configDir+"/gums.config";
		this.schemaPath = resourceDir+"/gums.config.schema";
		this.transformPath = resourceDir+"/gums.config.transform";
		this.configBackupDir = configDir+"/backup";

		if (create && !(new File(configPath).exists())) {
			try {
				BufferedWriter out = new BufferedWriter(new FileWriter(configPath));
				out.write("<?xml version='1.0' encoding='UTF-8'?>\n\n"+
						"<gums version='"+version+"'>\n\n"+
						"\t<persistenceFactories>\n\n"+
						"\t\t<hibernatePersistenceFactory\n"+
						"\t\t\tname='mysql'\n"+
						"\t\t\tdescription=''\n"+
						"\t\t\thibernate.connection.driver_class='com.mysql.jdbc.Driver'\n"+
						"\t\t\thibernate.dialect='net.sf.hibernate.dialect.MySQLDialect'\n"+
						"\t\t\thibernate.connection.url='jdbc:mysql://localhost.localdomain:3306/GUMS_1_3'\n"+
						"\t\t\thibernate.connection.username='gums'\n"+
						"\t\t\thibernate.connection.password=''\n"+
						"\t\t\thibernate.connection.autoReconnect='true'\n"+
						"\t\t\thibernate.c3p0.min_size='3'\n"+
						"\t\t\thibernate.c3p0.max_size='20'\n"+
						"\t\t\thibernate.c3p0.timeout='180' />\n\n"+
						"\t</persistenceFactories>\n\n"+
						"\t<userGroups>\n\n"+
						"\t\t<manualUserGroup\n"+
						"\t\t\tname='admins'\n"+
						"\t\t\tdescription=''\n"+
						"\t\t\tpersistenceFactory='mysql'\n"+
						"\t\t\taccess='write'/>\n\n"+
						"\t</userGroups>\n\n"+
				"</gums>");
				out.close();
			} catch (IOException e1) {
				gumsResourceAdminLog.error("Could not create gums.config: " + e1.getMessage());
			}
		}
	}

	public void deleteBackupConfiguration(String dateStr) {
		new File(configBackupDir+"/gums.config."+dateStr).delete();
	}

	public Collection getBackupConfigDates() {
		ArrayList backupConfigDates = new ArrayList();
		File dir = new File(configBackupDir);
		String[] children = dir.list();
		if (children!=null) {
			for (int i=0; i<children.length; i++) {
				String dateStr = children[i].substring(children[i].lastIndexOf(".")+1);
				backupConfigDates.add( dateStr );
			}    	
		}
		Collections.sort(backupConfigDates);
		return backupConfigDates;
	}

	public Date getLastModification() {
		try {
			File file = new File(configPath);
			return new Date(file.lastModified());
		} catch (Exception e) {
			gumsResourceAdminLog.fatal("The configuration wasn't read properly. GUMS is not operational.", e);
			return null;
		}
	}
	
	public String getSchemaPath() {
		return schemaPath;
	}

	public boolean isActive() {
		log.debug("Checking whether gums.config is present");
		return new File(configPath).exists();
	}

	public boolean isReadOnly() {
		return false;
	}
	
	public synchronized Configuration restoreConfiguration(String dateStr) {
		String path = "/gums.config." + dateStr;
		File file = new File(path);
		if (!file.exists())
			return null;
//		moveFile(configPath, configBackupDir + "/gums.config~");
		copyFile(configBackupDir + path, configPath);
//		moveFile(configBackupDir + "/gums.config~", configBackupDir + "/gums.config.prev" );
		return retrieveConfiguration();
	}

	public synchronized boolean needsReload() {
		return (lastRetrieval==null || lastRetrieval.before(getLastModification()));
	}
	
	public synchronized Configuration retrieveConfiguration() {
		try {
			if (needsReload())
				reloadConfiguration();
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		return conf;
	}

	public synchronized void setConfiguration(Configuration conf, boolean backupCopy) throws Exception {
		log.trace("Configuration set programically");
		gumsResourceAdminLog.info("Configuration set programically");
		gumsSiteAdminLog.info("Configuration set programically");
		if (conf == null)
			throw new RuntimeException("Configuration has not been loaded");
		log.debug("Attempting to store configuration");
		String tempGumsConfigPath = configPath+"~";
		
		BufferedWriter out;
		out = new BufferedWriter(new FileWriter(tempGumsConfigPath));
		conf.write(out);
		out.close();

		// Make sure configuration is valid
		this.conf = ConfigurationToolkit.loadConfiguration(tempGumsConfigPath, null, schemaPath);
		
		// copy gums.config to gums.config.prev
		File file = new File(configBackupDir);
		file.mkdir();
//		if (!backupCopy && new File(configPath).exists())
//			copyFile(configPath, configBackupDir+"/gums.config.prev" );

		// move temp file to gums.config or gums.config.date
		moveFile(tempGumsConfigPath, (backupCopy?configBackupDir+"/gums.config."+format.format(new Date()):configPath));
	}

	private void reloadConfiguration() {
		conf = null;
		try {
			if (ConfigurationToolkit.getVersion(configPath).equals("1.1")) {
				copyFile(configPath,configPath+".1.1");
				Configuration configuration = ConfigurationTransform.doTransform(configPath, transformPath);
				setConfiguration(configuration, false);
			}

			log.debug("Attempting to load configuration from gums.config");
			conf = ConfigurationToolkit.loadConfiguration(configPath, null, schemaPath);
			log.trace("Configuration reloaded from '" + configPath + "'");
			gumsResourceAdminLog.info("Configuration reloaded from '" + configPath + "'");
			gumsSiteAdminLog.info("Configuration reloaded from '" + configPath + "'");
			lastRetrieval = new Date();
		} catch (Exception e) {
			gumsResourceAdminLog.error("The configuration wasn't read correctly: " + e.getMessage());
			log.error("The configuration wasn't read correctly.", e);
			throw new RuntimeException("The configuration wasn't read correctly: " + e.getMessage());
		}
	}

}
