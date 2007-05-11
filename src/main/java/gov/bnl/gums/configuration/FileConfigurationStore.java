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
import gov.bnl.gums.userGroup.VirtualOrganization;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
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
    private String filename = null;
    private DateFormat format = new SimpleDateFormat("yyyy_MM_dd_HHmm");

    /**
     * Creates a new FileConfigurationStore object
     */
    public FileConfigurationStore() {
    }
    
    /**
     * Creates a new FileConfigurationStore object
     * Allows for specify the absolute name of the configuration file
     * 
     * @param filename
     * @param create if true, a new barbones configuration file will be created
     * at given filename if no file currently exists at that filename
     */
    public FileConfigurationStore(String filename, boolean create) {
        this.filename = filename;
        
        if (create && !(new File(filename).exists())) {
    		try {
    			BufferedWriter out = new BufferedWriter(new FileWriter(filename));
    			out.write("<?xml version='1.0' encoding='UTF-8'?>\n\n"+
	    			"<gums version='"+GUMS.getVersion()+"'>\n\n"+
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
    	new File(getConfigBackupPath()+"/gums.config."+dateStr).delete();
    }
    
    /**
     * @return gums.config path
     */
    public String getConfigPath() {
    	if (filename != null)
        	return filename;
        else 
        	return getClass().getClassLoader().getResource("gums.config").getPath();
    }
    
    /**
     * @return backup directory path
     */
    public String getConfigBackupPath() {
    	String configPath = getConfigPath();
    	int index = configPath.lastIndexOf("/");
       	return configPath.substring(0, index) + "/backup";
    }
    
    public Collection getBackupConfigDates() {
    	ArrayList backupConfigDates = new ArrayList();
    	File dir = new File(getConfigBackupPath());
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
        return getConfigPath()!=null;
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
    	String configPath = getConfigPath();
    	moveFile(configPath, getConfigBackupPath() + "/gums.config." + format.format(new Date()));
    	copyFile(getConfigBackupPath() + "/gums.config." + dateStr, configPath);
        return retrieveConfiguration();
    }
    
    public synchronized void setConfiguration(Configuration conf, boolean backupCopy) throws IOException {
        this.conf = conf;
        log.trace("Configuration set programically");
        gumsResourceAdminLog.info("configuration set programically");
        gumsSiteAdminLog.info("configuration set programically");
        if (conf == null)
            throw new RuntimeException("Configuration has not been loaded");
        
        log.debug("Attempting to store configuration");
        String configPath = getConfigPath();
        String tempGumsConfigPath = configPath+"~";

        BufferedWriter out;
        out = new BufferedWriter(new FileWriter(tempGumsConfigPath));
       	
        out.write("<?xml version='1.0' encoding='UTF-8'?>\n\n");
        
        out.write("<gums version='"+GUMS.getVersion()+"'>\n\n");
        
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
        
        // Write Virtual Organizations (VOs)
        if( conf.getVirtualOrganizations().size()>0 ) {
            out.write("\t<virtualOrganizations>\n\n");
        	Iterator it = conf.getVirtualOrganizations().values().iterator();
        	while( it.hasNext() ) {
        		VirtualOrganization vo = (VirtualOrganization)it.next();
        		out.write( vo.toXML() );
        	}
            out.write("\t</virtualOrganizations>\n\n");
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
        
        // copy gums.config to gums.config.old
        if (!backupCopy && new File(configPath).exists())
        	copyFile(configPath, getConfigPath()+".old");

        // move temp file to gums.config or gums.config.date
        new File(getConfigBackupPath()).mkdir();
    	moveFile(tempGumsConfigPath, (backupCopy?getConfigBackupPath()+"/gums.config."+format.format(new Date()):configPath));
    }
    
    private Date lastModification() {
        try {
            File file = new File(getConfigPath());
            return new Date(file.lastModified());
        } catch (Exception e) {
            gumsResourceAdminLog.fatal("The configuration wasn't read properly. GUMS is not operational.", e);
            return null;
        }
    }
    
    private void reloadConfiguration() {
		conf = null;
        try {
            log.debug("Attempting to load configuration from gums.config");
    		conf = ConfigurationToolkit.loadConfiguration(getConfigPath());
            log.trace("Configuration reloaded from '" + getConfigPath() + "'");
            gumsResourceAdminLog.info("Configuration reloaded from '" + getConfigPath() + "'");
            gumsSiteAdminLog.info("Configuration reloaded from '" + getConfigPath() + "'");
            lastRetrival = new Date();
        } catch (Exception e) {
            gumsResourceAdminLog.error("The configuration wasn't read correctly: " + e.getMessage());
            log.error("The configuration wasn't read correctly.", e);
            throw new RuntimeException("The configuration wasn't read correctly: " + e.getMessage());
        }
    }
}
