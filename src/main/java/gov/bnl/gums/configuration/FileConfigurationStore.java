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
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Date;
import java.util.Iterator;

import org.apache.commons.logging.*;

/** Implements the logic to retrieve the configuration from the gums.config file
 * taken from the classpath. The file will be reloaded as soon as if it changes,
 * on demand (no polling).
 *
 * @author  Gabriele Carcassi
 */
public class FileConfigurationStore implements ConfigurationStore {
    private Log log = LogFactory.getLog(FileConfigurationStore.class);
    private Log gumsSiteAdminLog = LogFactory.getLog(GUMS.siteAdminLog);
    private Log gumsResourceAdminLog = LogFactory.getLog(GUMS.resourceAdminLog);
    private Exception configError;
    
    private Configuration conf;
    private Date lastRetrival;
    private String filename = null;
    
    public FileConfigurationStore() {
    }
    
    /** Allows to specify the absolute name of the configuration file
     */
    public FileConfigurationStore(String filename, boolean create) {
        this.filename = filename;
        
        if (create) {
    		try {
    			BufferedWriter out = new BufferedWriter(new FileWriter(filename));
    			out.write("<?xml version='1.0' encoding='UTF-8'?>\n\n"+
	    			"<gums>\n\n"+
	    				"\t<persistenceFactories>"+
	    					"\t<persistenceFactory"+
				    			"\t\tclassName='gov.bnl.gums.persistence.HibernatePersistenceFactory'"+
				    			"\t\tname='mysql'"+
				    			"\t\thibernate.connection.driver_class='com.mysql.jdbc.Driver'"+
				    			"\t\thibernate.dialect='net.sf.hibernate.dialect.MySQLDialect'"+
				    			"\t\thibernate.connection.url='jdbc:mysql://localhost.localdomain:3306/GUMS_1_1'"+
				    			"\t\thibernate.connection.username='gums'"+
				    			"\t\thibernate.connection.password=''"+
				    			"\t\thibernate.connection.autoReconnect='true'"+
				    			"\t\thibernate.c3p0.min_size='3'"+
				    			"\t\thibernate.c3p0.max_size='20'"+
				    			"\t\thibernate.c3p0.timeout='180' />"+
				    	"\t</persistenceFactories>"+
	    				"\t<userGroups>"+
				    		"\t<userGroup"+
				    			"\t\tclassName='gov.bnl.gums.userGroup.ManualUserGroup'"+
				    			"\t\tname='admin'"+
				    			"\t\tpersistenceFactory='mysql'"+
				    			"\t\taccess='write'/>"+
	    				"\t</userGroups>"+
	    			"</gums>");
    			out.close();
    		} catch (IOException e1) {
    			gumsResourceAdminLog.error("Could not create gums.config: " + e1.getMessage());
    		}
        }
    }
    
    public boolean isActive() {
        log.debug("Checking whether gums.config is present");
        return ((filename != null) || (getConfURL() != null));
    }
    
    public boolean isReadOnly() {
        return false;
    }
    
    public URL getConfURL() {
    	return getClass().getClassLoader().getResource("gums.config");
    }
    
    public synchronized Configuration retrieveConfiguration() {
        if ((lastRetrival == null) || (lastRetrival.before(lastModification())))
            reloadConfiguration();
        if (configError != null) {
            throw new RuntimeException(filename+"  "+" is misconfigured: please check the resource admin log for errors, and the gums.config file.");
        }
        return conf;
    }
    
    private Date lastModification() {
        try {
            if (filename != null) {
                // If conf filename was specified
                File file = new File(filename);
                return new Date(file.lastModified());
            } else {
                // Conf filename not specified, getting it from the
                // classpath
                URL confURL = getConfURL();
                URI uri = new URI(confURL.toString());
                File file = new File(uri);
                return new Date(file.lastModified());
            }
        } catch (Exception e) {
            gumsResourceAdminLog.fatal("The configuration wasn't read properly. GUMS is not operational.", e);
            return null;
        }
    }
    
    private void reloadConfiguration() {
		conf = null;
        configError = null;
        try {
            log.debug("Attempting to load configuration from gums.config");
            if (filename != null) {
        		conf = ConfigurationToolkit.loadConfiguration(filename);
                log.trace("Configuration reloaded from '" + filename + "'");
                gumsResourceAdminLog.info("Configuration reloaded from '" + filename + "'");
                gumsSiteAdminLog.info("Configuration reloaded from '" + filename + "'");
            } else {
    			URL confURL = getConfURL();
                conf = ConfigurationToolkit.loadConfiguration(confURL.getPath());
                log.trace("Configuration reloaded from classpath '" + confURL + "'");
                gumsResourceAdminLog.info("Configuration reloaded '" + confURL + "'");
                gumsSiteAdminLog.info("Configuration reloaded '" + confURL + "'");
            }
            lastRetrival = new Date();
        } catch (Exception e) {
            configError = e;
            gumsResourceAdminLog.fatal("The configuration wasn't read properly. GUMS is not operational: " + e.getMessage());
            log.info("Configuration wasn't read correctly.", e);
        }
    }
    
    public void setConfiguration(Configuration conf) throws IOException {
        this.conf = conf;
        log.trace("Configuration set programically");
        gumsResourceAdminLog.info("configuration set programically");
        gumsSiteAdminLog.info("configuration set programically");
        if (conf == null)
            throw new RuntimeException("Configuration has not been loaded");
        
        log.debug("Attempting to store configuration");
        URL confURL = getConfURL();
        BufferedWriter out;

        String tempGumsConfigFile = confURL.getPath()+"~";
       	out = new BufferedWriter(new FileWriter(tempGumsConfigFile));
        
        out.write("<?xml version='1.0' encoding='UTF-8'?>\n\n");
        
        out.write("<gums>\n\n");
        
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
        
        // copy gums.config to gums.config_old
        if (filename != null)
        	copyFile(filename, filename+"_old");
        else 
        	copyFile(confURL.getPath(), confURL.getPath()+"_old");
        
        // copy temp file to gums.config
        if (filename != null)
        	copyFile(tempGumsConfigFile, filename);
        else 
        	copyFile(tempGumsConfigFile, confURL.getPath());
        
        // delete temp file
        new File(tempGumsConfigFile).delete();
       
    }
    
    private void copyFile(String source, String target) {
        try {
			FileInputStream fis  = new FileInputStream(source);
			FileOutputStream fos = new FileOutputStream(target);
			byte[] buf = new byte[1024];
			int i = 0;
			while((i=fis.read(buf))!=-1)
			  fos.write(buf, 0, i);
			fis.close();
			fos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
