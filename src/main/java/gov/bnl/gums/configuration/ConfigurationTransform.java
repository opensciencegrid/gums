package gov.bnl.gums.configuration;

import gov.bnl.gums.GUMS;
import gov.bnl.gums.account.AccountMapper;
import gov.bnl.gums.admin.CertCache;
import gov.bnl.gums.groupToAccount.GroupToAccountMapping;
import gov.bnl.gums.userGroup.UserGroup;
import gov.bnl.gums.userGroup.VOMSUserGroup;
import gov.bnl.gums.userGroup.VirtualOrganization;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import javax.xml.transform.*;
import javax.xml.transform.sax.*;
import javax.xml.transform.stream.*;

import org.apache.commons.digester.Digester;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.helpers.XMLReaderFactory;
import org.xml.sax.XMLReader;
import org.xml.sax.InputSource;

/**
 *
 * @author Jay Packard
 */
public class ConfigurationTransform {
	static private Log log = LogFactory.getLog(FileConfigurationStore.class);
	static private Log gumsResourceAdminLog = LogFactory.getLog(GUMS.resourceAdminLog);
	
	/**
	 * Transforms gums.config 1.1 to 1.2
	 * 
	 * @param configFile
	 * @param transformFile
	 */
	static public void doTransform(String configFile, String transformFile) {
	    try {
	    	String configFileTemp = configFile + "~";
        	
	        XMLReader reader = XMLReaderFactory.createXMLReader();
	        Source source = new SAXSource(reader, new InputSource(configFile));

	        StreamResult result = new StreamResult(configFileTemp);
	        Source style = new StreamSource(transformFile);

	        TransformerFactory transFactory = TransformerFactory.newInstance();
	        Transformer trans = transFactory.newTransformer(style);
	        
	        trans.transform(source, result);
	        
        	// Reload it to get rid of duplicates that the transform couldn't handle
        	// as well as to clean up the formatting
        	Digester digester = ConfigurationToolkit.retrieveDigester();
        	Configuration configuration = new Configuration();
            digester.push(configuration);
            digester.parse(configFileTemp);
            
            FileConfigurationStore.moveFile(configFile, configFile + "_1.1");

            FileConfigurationStore.moveFile(configFileTemp, configFile);
            
            // Clean up names
            Iterator it = new ArrayList(configuration.getVirtualOrganizations().keySet()).iterator();
            while (it.hasNext()) {
            	String name = (String)it.next();
            	String origName = new String(name);
            	if (name.startsWith("http")) {
            		name = name.replaceAll("^http.*://","");
            		name = name.replaceAll("[:|/].*","");
            		name = name.replaceAll("\\.","-");
            		if (configuration.getVirtualOrganization(name)!=null) {
	            		int count = 1;
	            		while (configuration.getVirtualOrganization(name + Integer.toString(count)) != null) 
	            			count++;
            			name += Integer.toString(count);
            		}
            		VirtualOrganization vo = configuration.getVirtualOrganization(origName);
            		if (vo!=null) {
	            		vo.setName(name);
	            		configuration.removeVirtualOrganization(origName);
	            		configuration.addVirtualOrganization(vo);
	            		Iterator it2 = configuration.getUserGroups().values().iterator();
	            		while (it2.hasNext()) {
	            			UserGroup userGroup = (UserGroup)it2.next();
	            			if (userGroup instanceof VOMSUserGroup && ((VOMSUserGroup)userGroup).getVirtualOrganization().equals(origName))
	            				((VOMSUserGroup)userGroup).setVirtualOrganization(name);
	            		}
            		}
            	}
            }
            
            it = new ArrayList(configuration.getAccountMappers().keySet()).iterator();
            while (it.hasNext()) {
            	String name = (String)it.next();
            	String origName = new String(name);
            	if (name.indexOf("://")!=-1) {
            		name = name.replaceAll(".*://","");
            		name = name.replaceAll("[:|/].*","");
            		name = name.replaceAll("\\.","-");
            		if (configuration.getAccountMapper(name)!=null) {
	            		int count = 1;
	            		while (configuration.getAccountMapper(name + Integer.toString(count)) != null) 
	            			count++;
            			name += Integer.toString(count);
            		}
            		AccountMapper accountMapper = configuration.getAccountMapper(origName);
            		if (accountMapper!=null) {
            			accountMapper.setName(name);
	            		configuration.removeAccountMapper(origName);
	            		configuration.addAccountMapper(accountMapper);
	            		Iterator it2 = configuration.getGroupToAccountMappings().values().iterator();
	            		while (it2.hasNext()) {
	            			GroupToAccountMapping groupToAccountMapping = (GroupToAccountMapping)it2.next();
	            			Iterator it3 = groupToAccountMapping.getAccountMappers().iterator();
	            			int index = 0;
	            			while (it3.hasNext()) {
	            				String str = (String)it3.next();
	            				if (str.equals(origName)) {
	            					groupToAccountMapping.getAccountMappers().remove(index);
	            					groupToAccountMapping.getAccountMappers().add(index, name);
	            					it3 = groupToAccountMapping.getAccountMappers().iterator();
	            				}
	            				index++;
	            			}
	            		}
            		}
            	}
            }
           	
            new FileConfigurationStore().setConfiguration(configuration, false);
	     } catch (Exception e) {
	        gumsResourceAdminLog.fatal("Could not convert older version of gums.config: " + e.getMessage());
	        log.info("Could not convert older version of gums.config", e);
	        throw new RuntimeException("Could not convert older version of gums.config");	    	 
	     }  	
	}

}
