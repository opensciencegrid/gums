/*
 * ConfigurationToolkit.java
 *
 * Created on June 4, 2004, 10:51 AM
 */

package gov.bnl.gums.configuration;

import gov.bnl.gums.GUMS;
import gov.bnl.gums.GridUser;
import gov.bnl.gums.account.AccountMapper;
import gov.bnl.gums.account.AccountPoolMapper;
import gov.bnl.gums.account.GecosAccountMapper;
import gov.bnl.gums.account.GecosLdapAccountMapper;
import gov.bnl.gums.account.LdapAccountMapper;
import gov.bnl.gums.account.GecosNisAccountMapper;
import gov.bnl.gums.account.GroupAccountMapper;
import gov.bnl.gums.account.ManualAccountMapper;
import gov.bnl.gums.admin.CertCache;
import gov.bnl.gums.groupToAccount.GroupToAccountMapping;
import gov.bnl.gums.hostToGroup.CertificateHostToGroupMapping;
import gov.bnl.gums.hostToGroup.HostToGroupMapping;
import gov.bnl.gums.persistence.HibernatePersistenceFactory;
import gov.bnl.gums.persistence.LDAPPersistenceFactory;
import gov.bnl.gums.persistence.LocalPersistenceFactory;
import gov.bnl.gums.persistence.PersistenceFactory;
import gov.bnl.gums.userGroup.LDAPUserGroup;
import gov.bnl.gums.userGroup.ManualUserGroup;
import gov.bnl.gums.userGroup.UserGroup;
import gov.bnl.gums.userGroup.VOMSUserGroup;
import gov.bnl.gums.userGroup.BannedUserGroup;
import gov.bnl.gums.userGroup.VomsServer;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringBufferInputStream;
import java.net.URL;
import java.util.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;
import org.apache.commons.beanutils.MethodUtils;
import org.apache.commons.digester.*;
import org.apache.log4j.Logger;

/** 
 * Contains the logic on how to parse an XML configuration file to create a
 * correctly built Configuration object.
 *
 * @author Gabriele Carcassi, Jay Packard
 */
public class ConfigurationToolkit {
	static private Logger log = Logger.getLogger(ConfigurationToolkit.class); 
	static private Logger adminLog = Logger.getLogger(GUMS.gumsAdminLogName);
	static String schemaPath;
	static String transformPath;

	static
	{
		String configDir;
		try {
			configDir = CertCache.getResourceDir();
		}
		catch (Exception e) {
			URL resource = new ConfigurationToolkit().getClass().getClassLoader().getResource("gums.config");
			configDir = resource.getPath().replace("/gums.config", "");
		}
		schemaPath = configDir+"/gums.config.schema";
		transformPath = configDir+"/gums.config.transform";
	}

	/**
	 * Simple error handler that logs errors
	 * 
	 * @author jpackard
	 */
	public class SimpleErrorHandler implements ErrorHandler {
		public String error = null;

		public void error(SAXParseException exception) {
			log.error(exception.getMessage());
			adminLog.error(exception.getMessage());
			error = exception.getMessage();
		}

		public void fatalError(SAXParseException exception) {
			log.fatal(exception.getMessage());
			adminLog.fatal(exception.getMessage());
			error = exception.getMessage();
		}

		public void warning(SAXParseException exception) {
			log.warn(exception.getMessage());
			adminLog.warn(exception.getMessage());
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
	public static String getVersion(String configText) throws IOException, SAXException {
		Digester digester = new Digester();
		digester.setValidating(false);
		digester.addObjectCreate("gums", Version.class);
		digester.addSetProperties("gums");
		log.trace("Loading the version from configuration");
		digester.parse(new ByteArrayInputStream(configText.getBytes()));
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

		digester.addObjectCreate("gums/userGroups/bannedUserGroup", BannedUserGroup.class);
		digester.addRule("gums/userGroups/bannedUserGroup", new PassRule(new String[] {"className", "persistenceFactory", "vomsServer"}));
		digester.addRule("gums/userGroups/bannedUserGroup", new PersistenceFactoryRule());
		digester.addRule("gums/userGroups/bannedUserGroup", new VomsServerRule());
		digester.addSetNext("gums/userGroups/bannedUserGroup", "addUserGroup", "gov.bnl.gums.userGroup.UserGroup");

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
	public static void validate(String configText) throws ParserConfigurationException, SAXException, IOException {
		System.setProperty("javax.xml.parsers.DocumentBuilderFactory", "org.apache.xerces.jaxp.DocumentBuilderFactoryImpl");
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		log.trace("DocumentBuilderFactory: "+ factory.getClass().getName());

		factory.setNamespaceAware(true);
		factory.setValidating(true);
		factory.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaLanguage", "http://www.w3.org/2001/XMLSchema");
		factory.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaSource", "file:"+schemaPath);

		DocumentBuilder builder = factory.newDocumentBuilder();
		SimpleErrorHandler errorHandler = new ConfigurationToolkit().new SimpleErrorHandler();
		builder.setErrorHandler( errorHandler );

		builder.parse(new ByteArrayInputStream(configText.getBytes()));

		if (errorHandler.error != null)
			throw new ParserConfigurationException(errorHandler.error);
	}

	/** new StringBufferInputStream(configText)
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
	public static synchronized Configuration parseConfiguration(String configText, boolean insertTest) throws ParserConfigurationException, IOException, SAXException {
		Configuration configuration;
		if (ConfigurationToolkit.getVersion(configText).equals("1.1")) {
			log.trace("Loading the configuration required configuration using schema '" + transformPath);
			configuration = doTransform(configText);
			if (insertTest)
				insertGipProbe(configuration);
		}
		else {
			log.trace("Loading the configuration using schema '" + schemaPath);
			configuration = new Configuration();
			validate(configText);
			Digester digester = retrieveDigester();
			digester.push(configuration);
			digester.parse(new ByteArrayInputStream(configText.getBytes()));
		}

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

		if (insertTest) {
			// Add test user and test configuration
			Map userGroups = configuration.getUserGroups();
			it = userGroups.values().iterator();
			while (it.hasNext()) {
				UserGroup userGroup = (UserGroup)it.next();
				if (userGroup instanceof ManualUserGroup) {
					// Add test user to manual user group
					ManualUserGroup manualUserGroup = (ManualUserGroup)userGroup;
					String persFactory = manualUserGroup.getPersistenceFactory();
					GridUser testUser = new GridUser("/DC=com/DC=example/OU=People/CN=Example User 12345");
					if (!manualUserGroup.isInGroup(testUser))
						manualUserGroup.addMember(testUser);

					// Add test account mapper
					ManualAccountMapper manualAccountMapper;
					AccountMapper accountMapper = configuration.getAccountMapper("_test");
					if (accountMapper != null && accountMapper instanceof ManualAccountMapper)
						manualAccountMapper = (ManualAccountMapper)accountMapper;
					else {
						String name = "_test";
						while (configuration.getAccountMapper(name)!=null)
							name = name + "_";
						manualAccountMapper = new ManualAccountMapper(configuration);
						manualAccountMapper.setName(name);
						manualAccountMapper.setPersistenceFactory(persFactory);
						configuration.addAccountMapper(manualAccountMapper);
					}
					if (!manualAccountMapper.getAccountMap().containsKey(testUser.getCertificateDN()))
						manualAccountMapper.addMapping(testUser.getCertificateDN(), "test");

					// Add test groupToAccountMapping
					GroupToAccountMapping g2AMapping = configuration.getGroupToAccountMapping("_test");
					if (g2AMapping == null) {
						g2AMapping = new GroupToAccountMapping(configuration);
						g2AMapping.setName("_test");
						configuration.addGroupToAccountMapping(g2AMapping);
					}
					if (!g2AMapping.containsUserGroup(manualUserGroup.getName()))
						g2AMapping.addUserGroup(manualUserGroup.getName());
					if (!g2AMapping.containsAccountMapper(manualAccountMapper.getName()))
						g2AMapping.addAccountMapper(manualAccountMapper.getName());

					// Add test hostToGroupMapping
					HostToGroupMapping h2GMapping = configuration.getHostToGroupMapping("/DC=com/DC=example/OU=Services/CN=example.site.com");
					if (h2GMapping == null) {
						h2GMapping = new CertificateHostToGroupMapping(configuration);
						((CertificateHostToGroupMapping)h2GMapping).setDn("/DC=com/DC=example/OU=Services/CN=example.site.com");
						configuration.addHostToGroupMapping(0, h2GMapping);
					}
					if (!h2GMapping.containsGroupToAccountMapping(g2AMapping.getName()))
						h2GMapping.addGroupToAccountMapping(g2AMapping.getName());

					break;
				}
			}
		}

		return configuration;
	}

	static public Configuration doTransform(String configText) {
		log.trace("Transforming configuration file using transform '" + transformPath);

		try {
			File configFileTemp = File.createTempFile("gums", "config");

			XMLReader reader = XMLReaderFactory.createXMLReader();
			Source source = new SAXSource(reader, new InputSource(new ByteArrayInputStream(configText.getBytes())));

			StreamResult result = new StreamResult(configFileTemp);
			Source style = new StreamSource(transformPath);

			TransformerFactory transFactory = TransformerFactory.newInstance();
			Transformer trans = transFactory.newTransformer(style);

			trans.transform(source, result);

			// Reload it to get rid of duplicates that the transform couldn't handle
			// as well as to clean up the formatting
			Digester digester = ConfigurationToolkit.retrieveDigester();
			Configuration configuration = new Configuration(true);
			digester.push(configuration);
			digester.parse("file://"+configFileTemp.getAbsolutePath());

			configFileTemp.delete();

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

			return configuration;
		} catch (Exception e) {
			String message = "Could not convert older version of gums.config";
			log.error(message, e);
			adminLog.error(message);
			throw new RuntimeException(message);	    	 
		} 
	}

	static public void insertGipProbe(Configuration configuration) {
		try {
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

				// add or alter HostToGroupMapping
				String domainName = java.net.InetAddress.getLocalHost().getCanonicalHostName();
				if (domainName!=null && domainName.indexOf(".")!=-1)
					domainName = domainName.substring(domainName.indexOf("."),domainName.length());
				String cn = "*/?*" + (domainName!=null?domainName:".localdomain");
				List h2gMappings = configuration.getHostToGroupMappings();
				boolean foundCn = false;
				for (int i=0; i<h2gMappings.size(); i++) {
					// add groupToAccountMapping to each hostToGroupMapping
					HostToGroupMapping h2gMapping = (HostToGroupMapping)h2gMappings.get(i);
					h2gMapping.addGroupToAccountMapping(g2aMapping.getName()); 
					if (h2gMapping.getName().indexOf(cn)!=-1)
						foundCn = true;
				}
				if (!foundCn) {
					// create a new hostToGroupMapping
					HostToGroupMapping h2gMapping = new CertificateHostToGroupMapping(configuration);
					h2gMapping.setDescription("Testing GUMS-status with GIP Probe");
					((CertificateHostToGroupMapping)h2gMapping).setCn(cn);
					configuration.addHostToGroupMapping(h2gMapping);
					h2gMapping.addGroupToAccountMapping(g2aMapping.getName()); 
				}
			}
		} catch (Exception e) {
			String message = "Could not insert GIP probe";
			log.warn(message, e);
			adminLog.warn(message);
		} 
	}

}
