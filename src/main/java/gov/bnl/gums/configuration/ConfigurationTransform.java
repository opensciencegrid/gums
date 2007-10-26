package gov.bnl.gums.configuration;

import gov.bnl.gums.GUMS;
import gov.bnl.gums.GridUser;
import gov.bnl.gums.account.AccountMapper;
import gov.bnl.gums.account.GroupAccountMapper;
import gov.bnl.gums.groupToAccount.GroupToAccountMapping;
import gov.bnl.gums.hostToGroup.CertificateHostToGroupMapping;
import gov.bnl.gums.hostToGroup.HostToGroupMapping;
import gov.bnl.gums.persistence.PersistenceFactory;
import gov.bnl.gums.userGroup.ManualUserGroup;
import gov.bnl.gums.userGroup.UserGroup;
import gov.bnl.gums.userGroup.VOMSUserGroup;
import gov.bnl.gums.userGroup.VomsServer;

import java.io.File;
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
	static public Configuration doTransform(String configPath, String transformPath) {
        log.trace("Transforming configuration file '" + configPath + "' using transform '" + transformPath);
		
		try {
	    	String configFileTemp = configPath + "~";
        	
	        XMLReader reader = XMLReaderFactory.createXMLReader();
	        Source source = new SAXSource(reader, new InputSource(configPath));

	        StreamResult result = new StreamResult(configFileTemp);
	        Source style = new StreamSource(transformPath);

	        TransformerFactory transFactory = TransformerFactory.newInstance();
	        Transformer trans = transFactory.newTransformer(style);
	        
	        trans.transform(source, result);
	        
        	// Reload it to get rid of duplicates that the transform couldn't handle
        	// as well as to clean up the formatting
        	Digester digester = ConfigurationToolkit.retrieveDigester();
        	Configuration configuration = new Configuration();
            digester.push(configuration);
            digester.parse("file://"+configFileTemp);
            
            new File(configFileTemp).delete();
            
            // Clean up VOMS server names
            Iterator it = new ArrayList(configuration.getVomsServers().keySet()).iterator();
            while (it.hasNext()) {
            	String name = (String)it.next();
            	String origName = new String(name);
            	if (name.startsWith("http")) {
            		name = name.replaceAll("^http.*://","");
            		name = name.replaceAll(".*/voms/","");
            		name = name.replaceAll(".*/edg-voms-admin/","");
            		name = name.replaceAll("[:|/].*","");
            		name = name.toLowerCase();
            		if (configuration.getVomsServer(name)!=null) {
	            		int count = 1;
	            		while (configuration.getVomsServer(name + Integer.toString(count)) != null) 
	            			count++;
            			name += Integer.toString(count);
            		}
            		VomsServer vo = configuration.getVomsServer(origName);
            		if (vo!=null) {
	            		vo.setName(name);
	            		configuration.removeVomsServer(origName);
	            		configuration.addVomsServer(vo);
	            		Iterator it2 = configuration.getUserGroups().values().iterator();
	            		while (it2.hasNext()) {
	            			UserGroup userGroup = (UserGroup)it2.next();
	            			if (userGroup instanceof VOMSUserGroup && ((VOMSUserGroup)userGroup).getVomsServer().equals(origName))
	            				((VOMSUserGroup)userGroup).setVomsServer(name);
	            		}
            		}
            	}
            }
            
            // Clean up account mapper names
            it = new ArrayList(configuration.getAccountMappers().keySet()).iterator();
            while (it.hasNext()) {
            	String name = (String)it.next();
            	String origName = new String(name);
            	if (name.indexOf("://")!=-1) {
            		name = name.replaceAll(".*://","");
            		name = name.replaceAll(".*/dc=","");
            		name = name.replaceAll("dc=","");
            		name = name.toLowerCase();
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
	            					index = 0;
	            				}
	            				else
	            					index++;
	            			}
	            		}
            		}
            	}
            }
       
            // Insert GIP probe
            PersistenceFactory persistenceFactory = configuration.getPersistenceFactory("mysql");
            if (persistenceFactory==null && configuration.getPersistenceFactories().size()>0)
            	persistenceFactory = (PersistenceFactory)configuration.getPersistenceFactories().values().iterator().next();
            if (persistenceFactory != null) {
            	// Add UserGroup
            	UserGroup userGroup = configuration.getUserGroup("gums-test");
            	if (userGroup==null || !(userGroup instanceof ManualUserGroup)) {
                	int index = 1;
            		while (configuration.getUserGroup("gums-test"+(index==1?"":Integer.toString(index)))!=null)
            			index++;
            		userGroup = new ManualUserGroup(configuration, "gums-test"+(index==1?"":Integer.toString(index)));
            		userGroup.setDescription("Testing GUMS-status with GIP Probe");
            		((ManualUserGroup)userGroup).setPersistenceFactory(persistenceFactory.getName());
            		configuration.addUserGroup(userGroup);
            	}
            	
            	// Add member to usergroup's database
            	GridUser user = new GridUser();
        		user.setCertificateDN("/GIP-GUMS-Probe-Identity");
            	if(((ManualUserGroup)userGroup).getMemberList().indexOf(user)==-1) {
            		((ManualUserGroup)userGroup).addMember(user);
            	}
            	
            	// Add AccountMapper
            	AccountMapper accountMapper = configuration.getAccountMapper("gums-test");
            	if (accountMapper==null || !(accountMapper instanceof GroupAccountMapper) || !((GroupAccountMapper)accountMapper).getAccountName().equals("GumsTestUserMappingSuccessful")) {
                	int index = 1;
            		while (configuration.getAccountMapper("gums-test"+(index==1?"":Integer.toString(index)))!=null)
            			index++;
            		accountMapper = new GroupAccountMapper(configuration, "gums-test"+(index==1?"":Integer.toString(index)));
            		accountMapper.setDescription("Testing GUMS-status with GIP Probe");
            		((GroupAccountMapper)accountMapper).setAccountName("GumsTestUserMappingSuccessful");
            		configuration.addAccountMapper(accountMapper);
            	}
            	
            	// Add GroupToAccountMapping
            	GroupToAccountMapping g2aMapping = configuration.getGroupToAccountMapping("gums-test");
            	if (g2aMapping==null) {
                	int index = 1;
            		while (configuration.getGroupToAccountMapping("gums-test"+(index==1?"":Integer.toString(index)))!=null)
            			index++;
            		g2aMapping = new GroupToAccountMapping(configuration, "gums-test"+(index==1?"":Integer.toString(index)));
            		g2aMapping.setDescription("Testing GUMS-status with GIP Probe");
            		configuration.addGroupToAccountMapping(g2aMapping);
            	}
            	if (g2aMapping.getAccountMappers().indexOf(accountMapper.getName())==-1)
            		g2aMapping.addAccountMapper(accountMapper.getName());
            	if (g2aMapping.getUserGroups().indexOf(userGroup.getName())==-1)
            		g2aMapping.addUserGroup(userGroup.getName());     
            	
            	// add HostToGroupMapping
            	String domainName = java.net.InetAddress.getLocalHost().getCanonicalHostName();
            	if (domainName!=null && domainName.indexOf(".")!=-1)
            		domainName = domainName.substring(domainName.indexOf("."),domainName.length());
           		domainName = "*/?*" + (domainName!=null?domainName:".localdomain");
            	HostToGroupMapping h2gMapping = configuration.getHostToGroupMapping(domainName);
            	if (h2gMapping==null || h2gMapping.getName().indexOf(domainName)==-1) {
            		h2gMapping = new CertificateHostToGroupMapping(configuration);
            		h2gMapping.setDescription("Testing GUMS-status with GIP Probe");
            		((CertificateHostToGroupMapping)h2gMapping).setCn(domainName);
            		configuration.addHostToGroupMapping(h2gMapping);
            	}
            	if (h2gMapping.getGroupToAccountMappings().indexOf(g2aMapping.getName())==-1)
            		h2gMapping.addGroupToAccountMapping(g2aMapping.getName()); 
            }
            
            return configuration;
	     } catch (Exception e) {
	        gumsResourceAdminLog.fatal("Could not convert older version of gums.config: " + e.getMessage());
	        log.info("Could not convert older version of gums.config", e);
	        throw new RuntimeException("Could not convert older version of gums.config");	    	 
	     } 
	}

}
