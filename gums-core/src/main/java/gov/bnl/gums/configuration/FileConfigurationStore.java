/*
 * FileConfigurationStore.java
 *
 * Created on October 20, 2004, 12:48 PM
 */

package gov.bnl.gums.configuration;

import gov.bnl.gums.GUMS;
import gov.bnl.gums.account.AccountMapper;
import gov.bnl.gums.groupToAccount.GroupToAccountMapping;
import gov.bnl.gums.hostToGroup.HostToGroupMapping;
import gov.bnl.gums.persistence.PersistenceFactory;
import gov.bnl.gums.userGroup.UserGroup;
import gov.bnl.gums.userGroup.VomsServer;

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
import java.util.Iterator;

import org.apache.commons.logging.*;

/** Implements the logic to retrieve the configuration from the gums.config file
 * taken from the classpath. The file will be reloaded as soon as if it changes,
 * on demand (no polling).
 *
 * @author Gabriele Carcassi, Jay Packard
 */
public class FileConfigurationStore implements ConfigurationStore {
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
	
    private Log log = LogFactory.getLog(FileConfigurationStore.class);
    private Log gumsSiteAdminLog = LogFactory.getLog(GUMS.siteAdminLog);
    private Log gumsResourceAdminLog = LogFactory.getLog(GUMS.resourceAdminLog);
    private Configuration conf;
    private Date lastRetrival;
    private String configBackupDir = null;
    private String configPath = null;
    private String schemaPath = null;
    private String transformPath = null;
    private DateFormat format = new SimpleDateFormat("yyyy_MM_dd_HHmm");
    private String version;

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
				    			"\t\t\thibernate.connection.url='jdbc:mysql://localhost.localdomain:3306/GUMS_1_1'\n"+
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
	        	backupConfigDates.add( children[i].substring(children[i].lastIndexOf(".")+1) );
	        }    	
    	}
    	Collections.sort(backupConfigDates);
    	return backupConfigDates;
    }
    
    public boolean isActive() {
        log.debug("Checking whether gums.config is present");
        return new File(configPath).exists();
    }
    
    public boolean isReadOnly() {
        return false;
    }
    
    public synchronized Configuration retrieveConfiguration() {
        try {
			if ((lastRetrival == null) || (lastRetrival.before(lastModification())))
			    reloadConfiguration();
		} catch (Exception e) {
	        throw new RuntimeException(e.getMessage());
		}
        return conf;
    }
    
    public synchronized Configuration restoreConfiguration(String dateStr) {
       	moveFile(configPath, configBackupDir + "/gums.config~");
       	copyFile(configBackupDir + "/gums.config." + dateStr, configPath);
       	moveFile(configBackupDir + "/gums.config~", configBackupDir + "/gums.config.prev");
        return retrieveConfiguration();
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
       	
        out.write("<?xml version='1.0' encoding='UTF-8'?>\n\n");
        
        out.write("<gums version='"+version+"'>\n\n");
        
        // Write persistence factories
        if( conf.getPersistenceFactories().size()>0 ) {
            out.write("\t<persistenceFactories>\n\n");
        	Iterator it = conf.getPersistenceFactories().values().iterator();
        	while( it.hasNext() ) {
        		PersistenceFactory persistenceFactory = (PersistenceFactory)it.next();
        		out.write( persistenceFactory.toXML() );
        	}
            out.write("\t</persistenceFactories>\n\n");
        }
        
        // Write Voms Servers
        if( conf.getVomsServers().size()>0 ) {
            out.write("\t<vomsServers>\n\n");
        	Iterator it = conf.getVomsServers().values().iterator();
        	while( it.hasNext() ) {
        		VomsServer vo = (VomsServer)it.next();
        		out.write( vo.toXML() );
        	}
            out.write("\t</vomsServers>\n\n");
        }           

        // Write User Groups
        if( conf.getUserGroups().size()>0 ) {
            out.write("\t<userGroups>\n\n");
        	Iterator it = conf.getUserGroups().values().iterator();
        	while( it.hasNext() ) {
        		UserGroup userGroup = (UserGroup)it.next();
        		out.write( userGroup.toXML() );
        	}
            out.write("\t</userGroups>\n\n");
        }                

        // Write Account Mappers
        if( conf.getAccountMappers().size()>0 ) {
            out.write("\t<accountMappers>\n\n");
        	Iterator it = conf.getAccountMappers().values().iterator();
        	while( it.hasNext() ) {
        		AccountMapper accountMapper = (AccountMapper)it.next();
        		out.write( accountMapper.toXML() );
        	}
            out.write("\t</accountMappers>\n\n");
        }             

        // Write Group To Account Mappings
        if( conf.getAccountMappers().size()>0 ) {
            out.write("\t<groupToAccountMappings>\n\n");
        	Iterator it = conf.getGroupToAccountMappings().values().iterator();
        	while( it.hasNext() ) {
        		GroupToAccountMapping groupToAccountMapping = (GroupToAccountMapping)it.next();
        		out.write( groupToAccountMapping.toXML() );
        	}
            out.write("\t</groupToAccountMappings>\n\n");
        }                

        // Write Host To Group Mappings
        if( conf.getAccountMappers().size()>0 ) {
            out.write("\t<hostToGroupMappings>\n\n");
        	Iterator it = conf.getHostToGroupMappings().iterator();
        	while( it.hasNext() ) {
        		HostToGroupMapping hostToGroupMapping = (HostToGroupMapping)it.next();
        		out.write( hostToGroupMapping.toXML() );
        	}
            out.write("\t</hostToGroupMappings>\n\n");
        }                
        
        out.write("</gums>");
        
        out.close();

		// Make sure configuration is valid
	    this.conf = ConfigurationToolkit.loadConfiguration(tempGumsConfigPath, schemaPath);
	
        // copy gums.config to gums.config.prev
		new File(configBackupDir).mkdir();
		if (!backupCopy && new File(configPath).exists())
        	copyFile(configPath, configBackupDir+"/gums.config.prev");

        // move temp file to gums.config or gums.config.date
    	moveFile(tempGumsConfigPath, (backupCopy?configBackupDir+"/gums.config."+format.format(new Date()):configPath));
    }
    
    private Date lastModification() {
        try {
            File file = new File(configPath);
            return new Date(file.lastModified());
        } catch (Exception e) {
            gumsResourceAdminLog.fatal("The configuration wasn't read properly. GUMS is not operational.", e);
            return null;
        }
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
    		conf = ConfigurationToolkit.loadConfiguration(configPath, schemaPath);
            log.trace("Configuration reloaded from '" + configPath + "'");
            gumsResourceAdminLog.info("Configuration reloaded from '" + configPath + "'");
            gumsSiteAdminLog.info("Configuration reloaded from '" + configPath + "'");
            lastRetrival = new Date();
        } catch (Exception e) {
            gumsResourceAdminLog.error("The configuration wasn't read correctly: " + e.getMessage());
            log.error("The configuration wasn't read correctly.", e);
            throw new RuntimeException("The configuration wasn't read correctly: " + e.getMessage());
        }
    }
}
