/*
 * ConfigurationToolkit.java
 *
 * Created on June 4, 2004, 10:51 AM
 */

package gov.bnl.gums.configuration;

import gov.bnl.gums.account.AccountPoolMapper;
import gov.bnl.gums.account.GecosAccountMapper;
import gov.bnl.gums.account.GecosLdapAccountMapper;
import gov.bnl.gums.account.LdapAccountMapper;
import gov.bnl.gums.account.GecosNisAccountMapper;
import gov.bnl.gums.account.GroupAccountMapper;
import gov.bnl.gums.account.ManualAccountMapper;
import gov.bnl.gums.groupToAccount.GroupToAccountMapping;
import gov.bnl.gums.hostToGroup.CertificateHostToGroupMapping;
import gov.bnl.gums.persistence.HibernatePersistenceFactory;
import gov.bnl.gums.persistence.LDAPPersistenceFactory;
import gov.bnl.gums.persistence.LocalPersistenceFactory;
import gov.bnl.gums.persistence.PersistenceFactory;
import gov.bnl.gums.userGroup.LDAPUserGroup;
import gov.bnl.gums.userGroup.ManualUserGroup;
import gov.bnl.gums.userGroup.VOMSUserGroup;
import gov.bnl.gums.userGroup.VomsServer;

import java.io.IOException;
import java.io.StringBufferInputStream;
import java.util.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;
import org.xml.sax.SAXException;
import org.apache.commons.beanutils.MethodUtils;
import org.apache.commons.digester.*;
import org.apache.log4j.Logger;

/** 
 * Contains the logic on how to parse an XML configuration file to create a
 * correctly built Configuration object.
 *
 * @author Gabriele Carcassi, Jay Packard
 */
class ConfigurationToolkit {
	static private Logger log = Logger.getLogger(ConfigurationToolkit.class); 
    
    /**
     * Simple error handler that logs errors
     * 
     * @author jpackard
     */
    public class SimpleErrorHandler implements ErrorHandler {
        public boolean error = false;
    	
        public void error(SAXParseException exception) {
        	log.error(exception.getMessage());
        	error = true;
        }
             
        public void fatalError(SAXParseException exception) {
        	log.fatal(exception.getMessage());
        	error = true;
        }
             
        public void warning(SAXParseException exception) {
        	log.warn(exception.getMessage());
        }
    }
    
    // Rule for handling the list of accountMappers within a groupToAccountMapping
    private static class AccountMapperListRule extends Rule {
        
        public void begin(String str, String str1, org.xml.sax.Attributes attributes) throws java.lang.Exception {
            if (attributes.getValue("accountMappers") != null) {
                Configuration conf = (Configuration) getDigester().getRoot();
                GroupToAccountMapping gTAMapping = (GroupToAccountMapping)getDigester().peek();
                StringTokenizer tokens = new StringTokenizer(attributes.getValue("accountMappers"), ",");
                while (tokens.hasMoreTokens()) {
                    String accountMapperName = tokens.nextToken().trim();
                    Object accountMapper = conf.getAccountMappers().get(accountMapperName);
                    if (accountMapper == null) {
                        throw new IllegalArgumentException("The accountMapper '" + accountMapperName + "' is used within a groupToAccountMapping, but it was not defined.");
                    }
                    MethodUtils.invokeMethod(gTAMapping, "addAccountMapper", accountMapperName);
                }
            }
        }
        
    }

    // Rule for handling the list of groupToAccountMappings within a hostToGroupMapping
    private static class GroupListRule extends Rule {
        
        public void begin(String str, String str1, org.xml.sax.Attributes attributes) throws java.lang.Exception {
            if (attributes.getValue("groupToAccountMappings") != null) {
                Configuration conf = (Configuration) getDigester().getRoot();
                Object obj = getDigester().peek();
                StringTokenizer tokens = new StringTokenizer(attributes.getValue("groupToAccountMappings"), ",");
                while (tokens.hasMoreTokens()) {
                    String groupToAccountMappingName = tokens.nextToken().trim();
                    Object groupToAccountMapping = conf.getGroupToAccountMappings().get(groupToAccountMappingName);
                    if (groupToAccountMapping == null) {
                        throw new IllegalArgumentException("The groupToAccountMapping '" + groupToAccountMappingName + "' is used within a hostToGroupMapping, but it was not defined.");
                    }
                    MethodUtils.invokeMethod(obj, "addGroupToAccountMapping", groupToAccountMappingName);
                }
            }
        }
        
    }    
    
    // Simple override rule that ignores missing properties
    // and excludes given properties
    private static class PassRule extends SetPropertiesRule {
    	
    	public PassRule(String [] excludes) {
    		super(excludes, new String[]{});
    		setIgnoreMissingProperty(false);
    	}
    	
    }

    // Rule for handling a reference to a persistent Factory
    private static class PersistenceFactoryRule extends Rule {
        
        public void begin(String str, String str1, org.xml.sax.Attributes attributes) throws java.lang.Exception {
            if (attributes.getValue("persistenceFactory") != null) {
                Configuration conf = (Configuration) getDigester().getRoot();
                Object mapper = getDigester().peek();
                String persistenceFactoryName = attributes.getValue("persistenceFactory").trim();
                Object persistenceFactory = conf.getPersistenceFactories().get(persistenceFactoryName);
                if (persistenceFactory == null) {
                    throw new IllegalArgumentException("The persistence factory '" + persistenceFactoryName + "' is used, but it was not defined.");
                }
                MethodUtils.invokeMethod(mapper, "setPersistenceFactory", new Object[] {persistenceFactoryName});
            }
        }
        
    }
    
    // Rule for distinguishing between 3rd party properties and GUMS properties
    private static class PersistencePropertiesRule extends Rule {
        
        public void begin(String str, String str1, org.xml.sax.Attributes attributes) throws java.lang.Exception {
            Object digestor = getDigester().peek();
            Properties properties = new Properties();
            for (int nAtt = 0; nAtt < attributes.getLength(); nAtt++) {
                String name = attributes.getQName(nAtt);
                String value = attributes.getValue(nAtt);
                log.trace("Adding " + name + " " + value + " property");
                /*if (name.equals("name"))
                    MethodUtils.invokeMethod(digestor, "setName", new Object[] {value});
                else if (name.equals("description"))
                    MethodUtils.invokeMethod(digestor, "setDescription", new Object[] {value});
                else if (name.equals("synchGroups"))
                    MethodUtils.invokeMethod(digestor, "setSynchGroups", new Object[] {new Boolean(value.equals("true"))});
                else if (name.equals("caCertFile"))
                    MethodUtils.invokeMethod(digestor, "setCaCertFile", new Object[] {value});
                else if (name.equals("groupIdField"))
                    MethodUtils.invokeMethod(digestor, "setGroupIdField", new Object[] {value});
                else if (name.equals("accountField"))
                    MethodUtils.invokeMethod(digestor, "setAccountField", new Object[] {value});
                else if (name.equals("memberAccountField"))
                    MethodUtils.invokeMethod(digestor, "setMemberAccountField", new Object[] {value});
                else if (name.equals("trustStorePassword"))
                    MethodUtils.invokeMethod(digestor, "setTrustStorePassword", new Object[] {value});
                else if (!name.equals("className"))*/
                if (name.indexOf(".")!=-1)
                	properties.setProperty(name, value);
            }
            MethodUtils.invokeMethod(digestor, "setProperties", properties);
        }
        
    };
    
    // Rule for handling the list of userGroups within a groupToAccountMapping
    private static class UserGroupListRule extends Rule {
        
        public void begin(String str, String str1, org.xml.sax.Attributes attributes) throws java.lang.Exception {
            if (attributes.getValue("userGroups") != null) {
                Configuration conf = (Configuration) getDigester().getRoot();
                Object mapping = getDigester().peek();
                StringTokenizer tokens = new StringTokenizer(attributes.getValue("userGroups"), ",");
                while (tokens.hasMoreTokens()) {
                    String userGroupName = tokens.nextToken().trim();
                    Object userGroup = conf.getUserGroups().get(userGroupName);
                    if (userGroup == null) {
                        throw new IllegalArgumentException("The userGroup '" + userGroupName + "' is used within a groupToAccountMapping, but it was not defined.");
                    }
                    MethodUtils.invokeMethod(mapping, "addUserGroup", userGroupName);
                }
            }
        }
        
    }
    
    //  Rule for handling a reference to a VO
    private static class VomsServerRule extends Rule {
        
        public void begin(String str, String str1, org.xml.sax.Attributes attributes) throws java.lang.Exception {
            if (attributes.getValue("vomsServer")!=null && !attributes.getValue("vomsServer").trim().equals("")) {
                Configuration conf = (Configuration) getDigester().getRoot();
                Object obj = getDigester().peek();
            	String vomsServerName = attributes.getValue("vomsServer").trim();
                Object vomsServer = conf.getVomsServers().get(vomsServerName);
                if (vomsServer==null) {
                    throw new IllegalArgumentException("The VOMS server '" + vomsServerName + "' is used, but it was not defined.");
                }
                MethodUtils.invokeMethod(obj, "setVomsServer", new Object[] {vomsServerName});
            }
        }
        
    }          
    
    /**
     * Get the gums.config version for the given file
     * 
     * @param filename
     * @return
     * @throws IOException
     * @throws SAXException
     */
    public static String getVersion(String filename) throws IOException, SAXException {
        Digester digester = new Digester();
        digester.setValidating(false);
        digester.addObjectCreate("gums", Version.class);
        digester.addSetProperties("gums");
        log.trace("Loading the version from configuration file '" + filename + "'");
        digester.parse("file://"+filename);
        String version = ((Version)digester.getRoot()).getVersion();
        log.trace("Loaded gums.config is version " + version );
        if (version == null)
        	return "1.1";
        return version;
    }
    
    /**
     * @return a Digestor object for parsing gums.config
     */
    public static Digester retrieveDigester() {
        Digester digester = new Digester();
        digester.setValidating(false);
       
        digester.addSetProperties("gums");
        
        digester.addObjectCreate("gums/persistenceFactories/hibernatePersistenceFactory", HibernatePersistenceFactory.class);
        digester.addSetProperties("gums/persistenceFactories/hibernatePersistenceFactory");
        digester.addRule("gums/persistenceFactories/hibernatePersistenceFactory", new PersistencePropertiesRule());
        digester.addSetNext("gums/persistenceFactories/hibernatePersistenceFactory", "addPersistenceFactory", "gov.bnl.gums.persistence.PersistenceFactory");

        digester.addObjectCreate("gums/persistenceFactories/ldapPersistenceFactory", LDAPPersistenceFactory.class);
        digester.addSetProperties("gums/persistenceFactories/ldapPersistenceFactory");
        digester.addRule("gums/persistenceFactories/ldapPersistenceFactory", new PersistencePropertiesRule());
        digester.addSetNext("gums/persistenceFactories/ldapPersistenceFactory", "addPersistenceFactory", "gov.bnl.gums.persistence.PersistenceFactory");
        
        digester.addObjectCreate("gums/persistenceFactories/localPersistenceFactory", LocalPersistenceFactory.class);
        digester.addSetProperties("gums/persistenceFactories/localPersistenceFactory");
        digester.addRule("gums/persistenceFactories/localPersistenceFactory", new PersistencePropertiesRule());
        digester.addSetNext("gums/persistenceFactories/localPersistenceFactory", "addPersistenceFactory", "gov.bnl.gums.persistence.PersistenceFactory");

        digester.addObjectCreate("gums/vomsServers/vomsServer", VomsServer.class);
        digester.addSetProperties("gums/vomsServers/vomsServer");
        digester.addRule("gums/vomsServers/vomsServer", new PassRule(new String[] {"persistenceFactory"}));
        digester.addRule("gums/vomsServers/vomsServer", new PersistenceFactoryRule());
        digester.addSetNext("gums/vomsServers/vomsServer", "addVomsServer", "gov.bnl.gums.userGroup.VomsServer");
        
        digester.addObjectCreate("gums/userGroups/ldapUserGroup", LDAPUserGroup.class);
        digester.addRule("gums/userGroups/ldapUserGroup", new PassRule(new String[] {"className", "persistenceFactory", "vomsServer"}));
        digester.addRule("gums/userGroups/ldapUserGroup", new PersistenceFactoryRule());
        digester.addRule("gums/userGroups/ldapUserGroup", new VomsServerRule());
        digester.addSetNext("gums/userGroups/ldapUserGroup", "addUserGroup", "gov.bnl.gums.userGroup.UserGroup");

        digester.addObjectCreate("gums/userGroups/manualUserGroup", ManualUserGroup.class);
        digester.addRule("gums/userGroups/manualUserGroup", new PassRule(new String[] {"className", "persistenceFactory", "vomsServer"}));
        digester.addRule("gums/userGroups/manualUserGroup", new PersistenceFactoryRule());
        digester.addRule("gums/userGroups/manualUserGroup", new VomsServerRule());
        digester.addSetNext("gums/userGroups/manualUserGroup", "addUserGroup", "gov.bnl.gums.userGroup.UserGroup");
        
        digester.addObjectCreate("gums/userGroups/vomsUserGroup", VOMSUserGroup.class);
        digester.addRule("gums/userGroups/vomsUserGroup", new PassRule(new String[] {"className", "persistenceFactory", "vomsServer"}));
        digester.addRule("gums/userGroups/vomsUserGroup", new PersistenceFactoryRule());
        digester.addRule("gums/userGroups/vomsUserGroup", new VomsServerRule());
        digester.addSetNext("gums/userGroups/vomsUserGroup", "addUserGroup", "gov.bnl.gums.userGroup.UserGroup");
        
        digester.addObjectCreate("gums/accountMappers/accountPoolMapper", AccountPoolMapper.class);
        digester.addSetProperties("gums/accountMappers/accountPoolMapper");
        digester.addRule("gums/accountMappers/accountPoolMapper", new PersistenceFactoryRule());
        digester.addSetNext("gums/accountMappers/accountPoolMapper", "addAccountMapper", "gov.bnl.gums.account.AccountMapper");
        
        digester.addObjectCreate("gums/accountMappers/gecosAccountMapper", GecosAccountMapper.class);
        digester.addSetProperties("gums/accountMappers/gecosAccountMapper");
        digester.addRule("gums/accountMappers/gecosAccountMapper", new PersistenceFactoryRule());
        digester.addSetNext("gums/accountMappers/gecosAccountMapper", "addAccountMapper", "gov.bnl.gums.account.AccountMapper");
        
        digester.addObjectCreate("gums/accountMappers/gecosLdapAccountMapper", GecosLdapAccountMapper.class);
        digester.addSetProperties("gums/accountMappers/gecosLdapAccountMapper");
        digester.addSetNext("gums/accountMappers/gecosLdapAccountMapper", "addAccountMapper", "gov.bnl.gums.account.AccountMapper");

        digester.addObjectCreate("gums/accountMappers/ldapAccountMapper", LdapAccountMapper.class);
        digester.addSetProperties("gums/accountMappers/ldapAccountMapper");
        digester.addSetNext("gums/accountMappers/ldapAccountMapper", "addAccountMapper", "gov.bnl.gums.account.AccountMapper");
        
        digester.addObjectCreate("gums/accountMappers/gecosNisAccountMapper", GecosNisAccountMapper.class);
        digester.addSetProperties("gums/accountMappers/gecosNisAccountMapper");
        digester.addSetNext("gums/accountMappers/gecosNisAccountMapper", "addAccountMapper", "gov.bnl.gums.account.AccountMapper");

        digester.addObjectCreate("gums/accountMappers/groupAccountMapper", GroupAccountMapper.class);
        digester.addSetProperties("gums/accountMappers/groupAccountMapper");
        digester.addSetNext("gums/accountMappers/groupAccountMapper", "addAccountMapper", "gov.bnl.gums.account.AccountMapper");
        
        digester.addObjectCreate("gums/accountMappers/manualAccountMapper", ManualAccountMapper.class);
        digester.addSetProperties("gums/accountMappers/manualAccountMapper");
        digester.addRule("gums/accountMappers/manualAccountMapper", new PersistenceFactoryRule());
        digester.addSetNext("gums/accountMappers/manualAccountMapper", "addAccountMapper", "gov.bnl.gums.account.AccountMapper");
        
        digester.addObjectCreate("gums/groupToAccountMappings/groupToAccountMapping", GroupToAccountMapping.class);
        digester.addSetProperties("gums/groupToAccountMappings/groupToAccountMapping");
        digester.addRule("gums/groupToAccountMappings/groupToAccountMapping", new PassRule(new String[] {"userGroups", "accountMappers"}));
        digester.addRule("gums/groupToAccountMappings/groupToAccountMapping", new UserGroupListRule());
        digester.addRule("gums/groupToAccountMappings/groupToAccountMapping", new AccountMapperListRule());
        digester.addSetNext("gums/groupToAccountMappings/groupToAccountMapping", "addGroupToAccountMapping", "gov.bnl.gums.groupToAccount.GroupToAccountMapping");

        digester.addObjectCreate("gums/hostToGroupMappings/hostToGroupMapping", CertificateHostToGroupMapping.class);
        digester.addSetProperties("gums/hostToGroupMappings/hostToGroupMapping");
        digester.addRule("gums/hostToGroupMappings/hostToGroupMapping", new PassRule(new String[] {"groupToAccountMappings"}));
        digester.addRule("gums/hostToGroupMappings/hostToGroupMapping", new GroupListRule());
        digester.addSetNext("gums/hostToGroupMappings/hostToGroupMapping", "addHostToGroupMapping", "gov.bnl.gums.hostToGroup.HostToGroupMapping");
       
        return digester;
    }
    
    /**
     * Validate gums.config given a config file and a schema file
     * 
     * @param configFile
     * @param schemaFile
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public static void validate(String configFile, String configText, String schemaFile) throws ParserConfigurationException, SAXException, IOException {
    	System.setProperty("javax.xml.parsers.DocumentBuilderFactory", "org.apache.xerces.jaxp.DocumentBuilderFactoryImpl");
    	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        log.trace("DocumentBuilderFactory: "+ factory.getClass().getName());
        
        factory.setNamespaceAware(true);
        factory.setValidating(true);
        factory.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaLanguage", "http://www.w3.org/2001/XMLSchema");
        factory.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaSource", "file:"+schemaFile);
        
        DocumentBuilder builder = factory.newDocumentBuilder();
        SimpleErrorHandler errorHandler = new ConfigurationToolkit().new SimpleErrorHandler();
        builder.setErrorHandler( errorHandler );

        if (configFile != null)
        	builder.parse(configFile); 
        else if (configText != null)
        	builder.parse(new StringBufferInputStream(configText));
        else
        	throw new RuntimeException("No configuration file or text specified");
        
        if (errorHandler.error){
        	throw new ParserConfigurationException();       
        }
    }

    /**
     * Load gums.config
     * 
     * @param configFile
     * @param configText
     * @param schemaPath
     * @return
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws SAXException
     * 
     * Either set configPath or configText
     */
    public static synchronized Configuration loadConfiguration(String configPath, String configText, String schemaPath) throws ParserConfigurationException, IOException, SAXException {
		validate(configPath, configText, schemaPath);
		Digester digester = retrieveDigester();
		Configuration configuration = new Configuration();
        digester.push(configuration);
        if (configPath!=null) {
	        log.trace("Loading the configuration from file '" + configPath + "' using schema '" + schemaPath);
	        digester.parse("file://"+configPath);
        }
        else if(configText!=null) {
	        log.trace("Loading the configuration using schema '" + schemaPath);
	        log.error(configText);
		digester.parse(new StringBufferInputStream(configText));
        }
        else
        	throw new RuntimeException("No config file or text specified");
        
        // Make sure storeConfig is set in only one persistence factory
        Iterator it = configuration.getPersistenceFactories().values().iterator();
        int storeConfigCount = 0;
        while (it.hasNext()) {
        	PersistenceFactory persFact = (PersistenceFactory)it.next();
        	if (persFact.getStoreConfig())
        		storeConfigCount++;
        }
        if (storeConfigCount>1)
        	throw new RuntimeException("Only one persistence factory may be set to store the configuration");
        
		return configuration;
    }
    
}
