/*
 * FileConfigurationStore.java
 *
 * Created on October 20, 2004, 12:48 PM
 */

package gov.bnl.gums.configuration;

import gov.bnl.gums.GUMS;
import gov.bnl.gums.persistence.HibernatePersistenceFactory;
import gov.bnl.gums.persistence.LocalPersistenceFactory;
import gov.bnl.gums.persistence.PersistenceFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;

import org.apache.log4j.Logger;

/** Implements the logic to retrieve the configuration from the gums.config file
 * taken from the classpath. The file will be reloaded as soon as if it changes,
 * on demand (no polling).
 *
 * @author Gabriele Carcassi, Jay Packard
 */
public class FileConfigurationStore extends ConfigurationStore {
	private Logger log = Logger.getLogger(FileConfigurationStore.class);
	private Configuration conf;
	private Date curModification = null;
	private String configBackupDir = null;
	private String configPath = null;

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
	public FileConfigurationStore(String configDir) {
		this.configPath = configDir+"/gums.config";
		this.configBackupDir = configDir+"/backup";
	}

	public void deleteBackupConfiguration(String name) {
		if (!new File(configBackupDir+"/gums.config."+name).delete())
			throw new RuntimeException("Could not delete backup configuration '"+name+"' from file");
	}

	public Collection getBackupNames() {
		ArrayList backupConfigDates = new ArrayList();
		File dir = new File(configBackupDir);
		String[] children = dir.list();
		if (children!=null) {
			for (int i=0; i<children.length; i++) {
				String dateStr = children[i].substring("gums.config.".length());
				backupConfigDates.add( dateStr );
			}    	
		}
		Collections.sort(backupConfigDates);
		return backupConfigDates;
	}

	public Date getLastModification() {
		try {
			File file = new File(configPath);
			Date date = new Date(file.lastModified());
			if (log.isTraceEnabled())
				log.trace("Last modification from file: "+date.toString());
			return date;
		} catch (Exception e) {
			log.error("Could not determine last modification time of configuration.", e);
			return null;
		}
	}

	public boolean isActive() {
		log.debug("Checking whether gums.config is present");
		return new File(configPath).exists();
	}
	
	public synchronized Configuration restoreConfiguration(String name) {
		String path = "/gums.config." + name;
		File file = new File(configBackupDir+path);
		if (!file.exists())
			throw new RuntimeException("Backup configuration " + configBackupDir + path + " does not exist");
//		moveFile(configPath, configBackupDir + "/gums.config~");
		copyFile(configBackupDir + path, configPath);
//		moveFile(configBackupDir + "/gums.config~", configBackupDir + "/gums.config.prev" );
		log.debug("Configuration '" + name + "', "+conf+" restored from file");
		return retrieveConfiguration();
	}
	
	public synchronized Configuration retrieveConfiguration() {
		try {
			if (curModification==null || curModification.before(getLastModification())) {
				reloadConfiguration();
				log.debug("Configuration "+conf+" reloaded from file");
			}
		} catch (Exception e) {
			System.out.println("Excpetion thrown");
			e.printStackTrace();
			throw new RuntimeException(e.getMessage(), e);
		}
		return conf;
	}

	public synchronized void setConfiguration(Configuration conf, boolean backupCopy, String name, Date date) throws Exception {
		if (conf == null)
			throw new RuntimeException("Configuration cannot be null");
                if (date==null)
                        date = new Date();

                if (backupCopy && (name==null || name.length()==0))
                        name = format.format(date);

		String tempGumsConfigPath = configPath+"~";
		
		BufferedWriter out;
		out = new BufferedWriter(new FileWriter(tempGumsConfigPath));
		conf.write(out);
		out.close();

		// Make sure configuration is valid
		FileInputStream fileInputStream = new FileInputStream(tempGumsConfigPath);
		try {
			StringBuffer configBuffer = new StringBuffer();
			int ch;
			while ((ch = fileInputStream.read()) != -1)
				configBuffer.append((char)ch);
	    	ConfigurationToolkit.parseConfiguration(configBuffer.toString(), true);
		} catch (Exception e) {
			log.error(e.getMessage());
			throw new Exception(e.getMessage());
		} finally {
			fileInputStream.close();
		}
		
		// copy gums.config to gums.config.prev
		File file = new File(configBackupDir);
		file.mkdir();
//		if (!backupCopy && new File(configPath).exists())
//			copyFile(configPath, configBackupDir+"/gums.config.prev" );

		// move temp file to gums.config or gums.config.date
		if (name != null)
			moveFile(tempGumsConfigPath, (backupCopy?configBackupDir+"/gums.config."+name:configPath));
		else
			moveFile(tempGumsConfigPath, (backupCopy?configBackupDir+"/gums.config.":configPath));

		this.conf = conf;
		
		log.debug("Configuration "+conf+" set to file");

		// set timestamps
		if (!backupCopy) {
			new File(configPath).setLastModified(date.getTime());
			curModification = date;
		}
	}

	private void reloadConfiguration() {
		try {
			FileInputStream fileInputStream = new FileInputStream(configPath);
			try {
				StringBuffer configBuffer = new StringBuffer();
				int ch;
				while ((ch = fileInputStream.read()) != -1)
					configBuffer.append((char)ch);
				this.conf = ConfigurationToolkit.parseConfiguration(configBuffer.toString(), true);
				curModification = new Date(new File(configPath).lastModified());
			} catch (Exception e) {
				throw new RuntimeException(e.getMessage(), e);
			} finally {
				fileInputStream.close();
			}
		
			fileInputStream.close();
		} catch (Exception e) {
			log.error("The configuration wasn't read correctly.", e);
			throw new RuntimeException("The configuration wasn't read correctly: " + e.getMessage(), e);
		}
	}

}
