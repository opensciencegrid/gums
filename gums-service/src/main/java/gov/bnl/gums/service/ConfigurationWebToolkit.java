/*
 *
 * Created on Oct 16, 2006, 2:03 PM
 */

package gov.bnl.gums.service;

import gov.bnl.gums.account.*;
import gov.bnl.gums.configuration.Configuration;
import gov.bnl.gums.groupToAccount.*;
import gov.bnl.gums.hostToGroup.*;
import gov.bnl.gums.persistence.*;
import gov.bnl.gums.userGroup.*;

import javax.servlet.http.HttpServletRequest;

import java.rmi.Remote;
import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;

/** 
 * Toolkit for providing configuration functionality for the web pages.
 * @author Jay Packard
 */
public class ConfigurationWebToolkit implements Remote {
	static public CertificateHostToGroupMapping parseHostToGroupMapping(HttpServletRequest request) throws Exception {
		CertificateHostToGroupMapping hostToGroupMapping = new CertificateHostToGroupMapping();
		String type = request.getParameter("hg_type").trim();
		if(type.equals("dn"))
			hostToGroupMapping.setDn( request.getParameter("name").trim() );
		else
			hostToGroupMapping.setCn( request.getParameter("name").trim() );			
		if (request.getParameter("description")!=null)	
			hostToGroupMapping.setDescription( request.getParameter("description").trim() );
		int counter = 0;
		while(request.getParameter("g2AM" + counter)!=null) {
			String g2AMName = request.getParameter("g2AM" + counter).trim();
			if (!g2AMName.equals(""))
				hostToGroupMapping.addGroupToAccountMapping(g2AMName);
			counter++;
		}

		return hostToGroupMapping;
	}	

	static public GroupToAccountMapping parseGroupToAccountMapping(HttpServletRequest request) throws Exception {
		GroupToAccountMapping groupToAccountMapping = new GroupToAccountMapping();

		groupToAccountMapping.setName( request.getParameter("name").trim() );
		if (request.getParameter("description")!=null)
			groupToAccountMapping.setDescription( request.getParameter("description").trim() );

		int counter = 0;
		while(request.getParameter("aM" + counter)!=null) {
			String accountMapperName = request.getParameter("aM" + counter).trim();
			if (!accountMapperName.equals(""))
				groupToAccountMapping.addAccountMapper(accountMapperName);
			counter++;
		}

		counter = 0;
		while(request.getParameter("uG" + counter)!=null) {
			String userGroupName = request.getParameter("uG" + counter).trim();
			if (!userGroupName.equals(""))
				groupToAccountMapping.addUserGroup(userGroupName);
			counter++;
		}

		if (request.getParameter("accVoSub")!=null && !request.getParameter("accVoSub").equals("")) {
			String trimmed = request.getParameter("accVoSub").trim();
			if (trimmed.indexOf(" ")!=-1)
				throw new RuntimeException("There cannot be a space in the accounting VO Subgroup");
			if (request.getParameter("accVo").equals(""))
				throw new RuntimeException("You must specify neither or both accounting VO subgroup and account VO");
			groupToAccountMapping.setAccountingVoSubgroup(trimmed);

		}

		if (request.getParameter("accVo")!=null && !request.getParameter("accVo").equals("")) {
			String trimmed = request.getParameter("accVo").trim();
			if (trimmed.indexOf(" ")!=-1)
				throw new RuntimeException("There cannot be a space in the accounting VO");
			if (request.getParameter("accVoSub").equals(""))
				throw new RuntimeException("You must specify neither or both accounting VO subgroup and account VO");
			groupToAccountMapping.setAccountingVo(trimmed);

		}

		return groupToAccountMapping;
	}		

	static public AccountMapper parseAccountMapper(HttpServletRequest request) throws Exception {
		AccountMapper accountMapper = null;

		String type = request.getParameter("am_type").trim();

		if (type.equals(GroupAccountMapper.getTypeStatic())) {
			accountMapper = new GroupAccountMapper();
			accountMapper.setName( request.getParameter("name").trim() );
			if (request.getParameter("description")!=null)
				accountMapper.setDescription( request.getParameter("description").trim() );
			if (request.getParameter("accountName")!=null)
				((GroupAccountMapper)accountMapper).setAccountName( request.getParameter("accountName").trim() );
			if (request.getParameter("groupName")!=null)
				((GroupAccountMapper)accountMapper).setGroupName( request.getParameter("groupName").trim() );
		}
		else if (type.equals(ManualAccountMapper.getTypeStatic())) {
			accountMapper = new ManualAccountMapper();
			accountMapper.setName( request.getParameter("name").trim() );
			if (request.getParameter("description")!=null)
				accountMapper.setDescription( request.getParameter("description").trim() );
			if (request.getParameter("persistenceFactory")!=null)
				((ManualAccountMapper)accountMapper).setPersistenceFactory( request.getParameter("persistenceFactory").trim() );
			if (request.getParameter("groupName")!=null)
				((ManualAccountMapper)accountMapper).setGroupName( request.getParameter("groupName").trim() );
			if (request.getParameter("accountPool")!=null)
				((ManualAccountMapper)accountMapper).setAccountPool( request.getParameter("accountPool").trim() );
		}
		else if (type.equals(AccountPoolMapper.getTypeStatic())) {
			accountMapper = new AccountPoolMapper();
			accountMapper.setName( request.getParameter("name").trim() );
			if (request.getParameter("description")!=null)
				accountMapper.setDescription( request.getParameter("description").trim() );
			if (request.getParameter("accountPool")!=null)
				((AccountPoolMapper)accountMapper).setAccountPool( request.getParameter("accountPool").trim() );
			if (request.getParameter("persistenceFactory")!=null)
				((AccountPoolMapper)accountMapper).setPersistenceFactory( request.getParameter("persistenceFactory").trim() );
		}
		else if (type.equals(GecosLdapAccountMapper.getTypeStatic())) {
			accountMapper = new GecosLdapAccountMapper();
			accountMapper.setName( request.getParameter("name").trim() );
			if (request.getParameter("description")!=null)
				accountMapper.setDescription( request.getParameter("description").trim() );
			if (request.getParameter("serviceUrl")!=null)
				((GecosLdapAccountMapper)accountMapper).setJndiLdapUrl( request.getParameter("serviceUrl").trim() );
			if (request.getParameter("gecos")!=null)
				((GecosLdapAccountMapper)accountMapper).setGecosField( request.getParameter("gecos").trim() );
			if (request.getParameter("account")!=null)
				((GecosLdapAccountMapper)accountMapper).setAccountField( request.getParameter("account").trim() );
			if (request.getParameter("peopleObject")!=null)
				((GecosLdapAccountMapper)accountMapper).setPeopleObject( request.getParameter("peopleObject").trim() );
		}
		else if (type.equals(LdapAccountMapper.getTypeStatic())) {
			accountMapper = new LdapAccountMapper();
			accountMapper.setName( request.getParameter("name").trim() );
			if (request.getParameter("description")!=null)
				accountMapper.setDescription( request.getParameter("description").trim() );
			if (request.getParameter("serviceUrl")!=null)
				((LdapAccountMapper)accountMapper).setJndiLdapUrl( request.getParameter("serviceUrl").trim() );
			if (request.getParameter("dn")!=null)
				((LdapAccountMapper)accountMapper).setDnField( request.getParameter("dn").trim() );
			if (request.getParameter("account")!=null)
				((LdapAccountMapper)accountMapper).setAccountField( request.getParameter("account").trim() );
			if (request.getParameter("peopleObject")!=null)
				((LdapAccountMapper)accountMapper).setPeopleObject( request.getParameter("peopleObject").trim() );
		}
		else if (type.equals(GecosNisAccountMapper.getTypeStatic())) {
			accountMapper = new GecosNisAccountMapper();
			accountMapper.setName( request.getParameter("name").trim() );
			if (request.getParameter("description")!=null)
				accountMapper.setDescription( request.getParameter("description").trim() );
			if (request.getParameter("serviceUrl")!=null)
				((GecosNisAccountMapper)accountMapper).setJndiNisUrl( request.getParameter("serviceUrl").trim() );
			if (request.getParameter("gecos")!=null)
				((GecosNisAccountMapper)accountMapper).setGecosField( request.getParameter("gecos").trim() );
			if (request.getParameter("account")!=null)
				((GecosNisAccountMapper)accountMapper).setAccountField( request.getParameter("account").trim() );
		}

		return accountMapper;
	}		

	static public UserGroup parseUserGroup(HttpServletRequest request) throws Exception {
		UserGroup userGroup = null;

		String type = request.getParameter("ug_type");

		if (type.equals(ManualUserGroup.getTypeStatic())) {
			userGroup = new ManualUserGroup();
			userGroup.setName( request.getParameter("name").trim() );
			if (request.getParameter("description")!=null)
				userGroup.setDescription( request.getParameter("description").trim() );
			if (request.getParameter("access")!=null)
				userGroup.setAccess( request.getParameter("access").trim() );
			if (request.getParameter("persistenceFactory")!=null)
				((ManualUserGroup)userGroup).setPersistenceFactory( request.getParameter("persistenceFactory").trim() );
			if (request.getParameter("membersUri")!=null)
				((ManualUserGroup)userGroup).setMembersUri( request.getParameter("membersUri").trim() );
			if (request.getParameter("nonMembersUri")!=null)
				((ManualUserGroup)userGroup).setNonMembersUri( request.getParameter("nonMembersUri").trim() );
		} else if (type.equals(LDAPUserGroup.getTypeStatic())) {
			userGroup = new LDAPUserGroup();
			userGroup.setName( request.getParameter("name").trim() );
			if (request.getParameter("description")!=null)
				userGroup.setDescription( request.getParameter("description").trim() );
			if (request.getParameter("access")!=null)
				userGroup.setAccess( request.getParameter("access").trim() );
			if (request.getParameter("server")!=null)
				((LDAPUserGroup)userGroup).setServer( request.getParameter("server").trim() );
			if (request.getParameter("peopleTree")!=null)
				((LDAPUserGroup)userGroup).setPeopleTree( request.getParameter("peopleTree").trim() );
			if (request.getParameter("groupTree")!=null)
				((LDAPUserGroup)userGroup).setGroupTree( request.getParameter("groupTree").trim() );
			if (request.getParameter("persistenceFactory")!=null)
				((LDAPUserGroup)userGroup).setPersistenceFactory( request.getParameter("persistenceFactory").trim() );
			if (request.getParameter("certDNField")!=null)
				((LDAPUserGroup)userGroup).setCertDNField( request.getParameter("certDNField").trim() );
			if (request.getParameter("memberUidField")!=null)
				((LDAPUserGroup)userGroup).setMemberUidField( request.getParameter("memberUidField").trim() );
		} else if (type.equals(VOMSUserGroup.getTypeStatic())) {
			userGroup = new VOMSUserGroup();
			userGroup.setName( request.getParameter("name").trim() );
			if (request.getParameter("description")!=null)
				userGroup.setDescription( request.getParameter("description").trim() );
			if (request.getParameter("access")!=null)
				userGroup.setAccess( request.getParameter("access").trim() );
			if (request.getParameter("vOrg")!=null)
				((VOMSUserGroup)userGroup).setVomsServer( request.getParameter("vOrg").trim() );
			else if(request.getParameter("baseUrl")!=null && !request.getParameter("baseUrl").equals(""))
				((VOMSUserGroup)userGroup).setVomsServer( request.getParameter("name").trim() );
			if (request.getParameter("url")!=null)
				((VOMSUserGroup)userGroup).setRemainderUrl( request.getParameter("url").trim() );
			if (request.getParameter("nVOMS")!=null)
				((VOMSUserGroup)userGroup).setAcceptProxyWithoutFQAN( request.getParameter("nVOMS").trim().equals("true") );
			if (request.getParameter("matchFQAN")!=null)
				((VOMSUserGroup)userGroup).setMatchFQAN( request.getParameter("matchFQAN") );
			if (request.getParameter("vogroup")!=null)
				((VOMSUserGroup)userGroup).setVoGroup( request.getParameter("vogroup") );
			if (request.getParameter("role")!=null)
				((VOMSUserGroup)userGroup).setRole( request.getParameter("role") );
		} else if (type.equals(BannedUserGroup.getTypeStatic())) {
			userGroup = new BannedUserGroup();
			userGroup.setName( request.getParameter("name").trim() );
			if (request.getParameter("description")!=null)
				userGroup.setDescription( request.getParameter("description").trim() );
			if (request.getParameter("access")!=null)
				userGroup.setAccess( request.getParameter("access").trim() );
			if (request.getParameter("url")!=null)
				((BannedUserGroup)userGroup).setEndpoint( request.getParameter("url").trim() );
			if (request.getParameter("persistenceFactory")!=null)
				((BannedUserGroup)userGroup).setPersistenceFactory( request.getParameter("persistenceFactory").trim() );
			if (request.getParameter("sslKey")!=null)
				((BannedUserGroup)userGroup).setSslKey( request.getParameter("sslKey").trim() );
			if (request.getParameter("sslCert")!=null)
				((BannedUserGroup)userGroup).setSslCertfile( request.getParameter("sslCert").trim() );
			if (request.getParameter("sslCA")!=null)
				((BannedUserGroup)userGroup).setSslCAFiles( request.getParameter("sslCA").trim() );
			if (request.getParameter("sslKeyPW")!=null)
				((BannedUserGroup)userGroup).setSslKeyPasswd( request.getParameter("sslKeyPW").trim() );

		}

		return userGroup;
	}

	static public VomsServer parseVomsServer(HttpServletRequest request) throws Exception {
		VomsServer vomsServer = new VomsServer();
		vomsServer.setName( request.getParameter("name").trim() );
		if (request.getParameter("description")!=null)
			vomsServer.setDescription( request.getParameter("description").trim() );
		if (request.getParameter("persistenceFactory")!=null)
			vomsServer.setPersistenceFactory( request.getParameter("persistenceFactory").trim() );
		if (request.getParameter("baseURL")!=null)
			vomsServer.setBaseUrl( request.getParameter("baseURL").trim() );
		if (request.getParameter("sslKey")!=null)
			vomsServer.setSslKey( request.getParameter("sslKey").trim() );
		if (request.getParameter("sslCert")!=null)
			vomsServer.setSslCertfile( request.getParameter("sslCert").trim() );
		if (request.getParameter("sslCA")!=null)
			vomsServer.setSslCAFiles( request.getParameter("sslCA").trim() );
		if (request.getParameter("sslKeyPW")!=null)
			vomsServer.setSslKeyPasswd( request.getParameter("sslKeyPW").trim() );
		return vomsServer;
	}	

	static public PersistenceFactory parsePersistenceFactory(HttpServletRequest request) throws Exception {
		PersistenceFactory persistenceFactory = null;

		String type = request.getParameter("type");

		if (type.equals(HibernatePersistenceFactory.getTypeStatic())) {
			persistenceFactory = new HibernatePersistenceFactory();
			persistenceFactory.setName( request.getParameter("name").trim() );
			if (request.getParameter("description")!=null)
				persistenceFactory.setDescription( request.getParameter("description").trim() );
			if (request.getParameter("storeConfig")!=null)
				persistenceFactory.setStoreConfig( request.getParameter("storeConfig").trim().equals("on") );
			((HibernatePersistenceFactory)persistenceFactory).setProperties( getHibernateProperties(persistenceFactory, request, false) );
		} 
		else if (type.equals(LDAPPersistenceFactory.getTypeStatic())) {
			persistenceFactory = new LDAPPersistenceFactory();
			persistenceFactory.setName( request.getParameter("name").trim() );
			if (request.getParameter("description")!=null)
				persistenceFactory.setDescription( request.getParameter("description").trim() );
			if (request.getParameter("storeConfig")!=null)
				persistenceFactory.setStoreConfig( request.getParameter("storeConfig").trim().equals("on") );
			((LDAPPersistenceFactory)persistenceFactory).setSynch( request.getParameter("synch")!=null ? request.getParameter("synch").trim().equals("true") : false );
			((LDAPPersistenceFactory)persistenceFactory).setCaCertFile( request.getParameter("caCertFile")!=null ? request.getParameter("caCertFile").trim() : "" );
			((LDAPPersistenceFactory)persistenceFactory).setTrustStorePassword( request.getParameter("tsPassword")!=null ? request.getParameter("tsPassword").trim() : "" );
			if (request.getParameter("groupTree")!=null)
				((LDAPPersistenceFactory)persistenceFactory).setGroupTree( request.getParameter("groupTree") );
			if (request.getParameter("peopleTree")!=null)
				((LDAPPersistenceFactory)persistenceFactory).setPeopleTree( request.getParameter("peopleTree") );			
			if (request.getParameter("gumsTree")!=null)
				((LDAPPersistenceFactory)persistenceFactory).setPeopleTree( request.getParameter("gumsTree") );				
			if (request.getParameter("gidNumberField")!=null)
				((LDAPPersistenceFactory)persistenceFactory).setGroupIdField( request.getParameter("gidNumberField") );
			if (request.getParameter("groupCnField")!=null)
				((LDAPPersistenceFactory)persistenceFactory).setGroupCnField( request.getParameter("groupCnField") );			
			if (request.getParameter("uidField")!=null)
				((LDAPPersistenceFactory)persistenceFactory).setUidField( request.getParameter("uidField") );
			if (request.getParameter("memberUidField")!=null)
				((LDAPPersistenceFactory)persistenceFactory).setMemberUidField( request.getParameter("memberUidField") );
			if (request.getParameter("emailField")!=null)
				((LDAPPersistenceFactory)persistenceFactory).setEmailField( request.getParameter("emailField") );
			((LDAPPersistenceFactory)persistenceFactory).setProperties( getLdapProperties(persistenceFactory, request, false) );
		} 
		else if (type.equals(LocalPersistenceFactory.getTypeStatic())) {
			persistenceFactory = new LocalPersistenceFactory();
			persistenceFactory.setName( request.getParameter("name").trim() );
			if (request.getParameter("description")!=null)
				persistenceFactory.setDescription( request.getParameter("description").trim() );
			if (request.getParameter("storeConfig")!=null)
				persistenceFactory.setStoreConfig( request.getParameter("storeConfig").trim().equals("on") );
			((LocalPersistenceFactory)persistenceFactory).setSynch( request.getParameter("synch")!=null ? request.getParameter("synch").trim().equals("true") : false );
			((LocalPersistenceFactory)persistenceFactory).setCaCertFile( request.getParameter("caCertFile")!=null ? request.getParameter("caCertFile").trim() : "" );
			((LocalPersistenceFactory)persistenceFactory).setTrustStorePassword( request.getParameter("tsPassword")!=null ? request.getParameter("tsPassword").trim() : "" );
			if (request.getParameter("groupTree")!=null)
				((LocalPersistenceFactory)persistenceFactory).setGroupTree( request.getParameter("groupTree") );
			if (request.getParameter("peopleTree")!=null)
				((LocalPersistenceFactory)persistenceFactory).setPeopleTree( request.getParameter("peopleTree") );			
			if (request.getParameter("gumsTree")!=null)
				((LocalPersistenceFactory)persistenceFactory).setPeopleTree( request.getParameter("gumsTree") );			
			if (request.getParameter("gidNumberField")!=null)
				((LocalPersistenceFactory)persistenceFactory).setGroupIdField( request.getParameter("gidNumberField") );
			if (request.getParameter("groupCnField")!=null)
				((LocalPersistenceFactory)persistenceFactory).setGroupCnField( request.getParameter("groupCnField") );			
			if (request.getParameter("uidField")!=null)
				((LocalPersistenceFactory)persistenceFactory).setUidField( request.getParameter("uidField") );
			if (request.getParameter("memberUidField")!=null)
				((LocalPersistenceFactory)persistenceFactory).setMemberUidField( request.getParameter("memberUidField") );
			if (request.getParameter("emailField")!=null)
				((LocalPersistenceFactory)persistenceFactory).setEmailField( request.getParameter("emailField") );
			Properties properties = getHibernateProperties(persistenceFactory, request, true);
			properties.putAll(getLdapProperties(persistenceFactory, request, true));
			((LocalPersistenceFactory)persistenceFactory).setProperties( properties );
		}

		return persistenceFactory;
	}

	static public Properties getHibernateProperties(PersistenceFactory persistenceFactory, HttpServletRequest request, boolean includeMySql) {
		Properties properties = new Properties();
		properties.put((includeMySql?"mysql.":"") + "hibernate.connection.url", (request.getParameter("mySqlUrl")!=null ? request.getParameter("mySqlUrl").trim() : ""));
		properties.put((includeMySql?"mysql.":"") + "hibernate.connection.username", (request.getParameter("mySqlUsername")!=null ? request.getParameter("mySqlUsername").trim() : ""));
		properties.put((includeMySql?"mysql.":"") + "hibernate.connection.password", (request.getParameter("mySqlPassword")!=null ? request.getParameter("mySqlPassword").trim() : ""));
		properties.put((includeMySql?"mysql.":"") + "hibernate.connection.driver_class", "com.mysql.jdbc.Driver");
		properties.put((includeMySql?"mysql.":"") + "hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
		properties.put((includeMySql?"mysql.":"") + "hibernate.c3p0.min_size", "3");
		properties.put((includeMySql?"mysql.":"") + "hibernate.c3p0.max_size", "20");
		properties.put((includeMySql?"mysql.":"") + "hibernate.c3p0.timeout", "180");
		properties.put((includeMySql?"mysql.":"") + "hibernate.connection.autoReconnect", "true");
		return properties;
	}

	static public Properties getLdapProperties(PersistenceFactory persistenceFactory, HttpServletRequest request, boolean includeLdap) {
		Properties properties = new Properties();
		properties.put((includeLdap?"ldap.":"") + "java.naming.security.authentication", (request.getParameter("ldapAuthentication")!=null ? request.getParameter("ldapAuthentication").trim() : "simple"));
		properties.put((includeLdap?"ldap.":"") + "java.naming.security.principal", (request.getParameter("ldapPrincipal")!=null ? request.getParameter("ldapPrincipal").trim() : ""));
		properties.put((includeLdap?"ldap.":"") + "java.naming.security.credentials", (request.getParameter("ldapCredentials")!=null ? request.getParameter("ldapCredentials").trim() : ""));
		properties.put((includeLdap?"ldap.":"") + "java.naming.provider.url", (request.getParameter("ldapUrl")!=null ? request.getParameter("ldapUrl").trim() : ""));
		properties.put((includeLdap?"ldap.":"") + "java.naming.factory.initial", "com.sun.jndi.ldap.LdapCtxFactory");
		return properties;
	}

	static public String getHostToGroupReferences(Configuration configuration, String g2AMappingName) {
		String retStr = null;
		Collection h2GMappings = configuration.getHostToGroupMappings();
		Iterator it = h2GMappings.iterator();
		while (it.hasNext()) {
			HostToGroupMapping h2GMapping = (HostToGroupMapping)it.next();
			Iterator it2 = h2GMapping.getGroupToAccountMappings().iterator();
			while (it2.hasNext()) {
				String thisG2AMapping = (String)it2.next();
				if (thisG2AMapping.equals(g2AMappingName)) {
					if (retStr==null) 
						retStr = "";
					retStr += "\"" + h2GMapping.getName() + "\", ";
					break;
				}
			}
		}
		if(retStr!=null)
			retStr = retStr.substring(0, retStr.length()-2);
		return retStr;
	}

	static public String getGroupToAccountMappingReferences(Configuration configuration, String name, String className) {
		String retStr = null;
		Collection g2AMappings = configuration.getGroupToAccountMappings().values();
		Iterator it = g2AMappings.iterator();
		while (it.hasNext()) {
			GroupToAccountMapping g2AMapping = (GroupToAccountMapping)it.next();
			if(className.equals("gov.bnl.gums.account.AccountMapper")) {
				Iterator it2 = g2AMapping.getAccountMappers().iterator();
				while (it2.hasNext()) {
					String thisAccountMapper = (String)it2.next();
					if (thisAccountMapper.equals(name)) {
						if (retStr==null) 
							retStr = "";
						retStr += g2AMapping.getName() + ", ";
						break;
					}
				}
			}
			else if(className.equals("gov.bnl.gums.userGroup.UserGroup")) {
				Iterator it2 = g2AMapping.getUserGroups().iterator();
				while (it2.hasNext()) {
					String thisUserGroup = (String)it2.next();
					if (thisUserGroup.equals(name)) {
						if (retStr==null) 
							retStr = "";
						retStr += g2AMapping.getName() + ", ";
						break;
					}
				}
			}
		}
		if(retStr!=null)
			retStr = retStr.substring(0, retStr.length()-2);
		return retStr;
	}

	static public String getVOMSUserGroupReferences(Configuration configuration, String vomsServer) {
		String retStr = null;
		Collection userGroups = configuration.getUserGroups().values();
		Iterator it = userGroups.iterator();
		while (it.hasNext()) {
			UserGroup userGroup = (UserGroup)it.next();
			if ( userGroup instanceof VOMSUserGroup && vomsServer.equals( ((VOMSUserGroup)userGroup).getVomsServer() ) ) {
				if (retStr==null) 
					retStr = "";
				retStr += userGroup.getName() + ", ";
				break;
			}
		}
		if(retStr!=null)
			retStr = retStr.substring(0, retStr.length()-2);
		return retStr;
	}	

	static public String getReferencesForPersistenceFactory(Configuration configuration, String persistenceFactory) {
		String retStr = null;
		Iterator it;

		it = configuration.getVomsServers().values().iterator();
		while (it.hasNext()) {
			VomsServer vomsServer = (VomsServer)it.next();
			if(vomsServer.getPersistenceFactory().equals(persistenceFactory)) {
				if (retStr==null) 
					retStr = "";
				retStr += "VOMS server " + vomsServer.getName() + ", ";
			}
		}

		it = configuration.getUserGroups().values().iterator();
		while (it.hasNext()) {
			UserGroup userGroup = (UserGroup)it.next();
			if (userGroup instanceof LDAPUserGroup) {
				if (((LDAPUserGroup)userGroup).getPersistenceFactory().equals(persistenceFactory)) {
					if (retStr==null) 
						retStr = "";
					retStr += "user group " + userGroup.getName() + ", ";
				}
			} else if (userGroup instanceof ManualUserGroup) {
				if (((ManualUserGroup)userGroup).getPersistenceFactory().equals(persistenceFactory)) {
					if (retStr==null) 
						retStr = "";
					retStr += "user group " + userGroup.getName() + ", ";
				}
			}
		}

		it = configuration.getAccountMappers().values().iterator();
		while (it.hasNext()) {
			AccountMapper accountMapper = (AccountMapper)it.next();
			if (accountMapper instanceof ManualAccountMapper) {
				if (((ManualAccountMapper)accountMapper).getPersistenceFactory().equals(persistenceFactory)) {
					if (retStr==null) 
						retStr = "";
					retStr += "account mapper " + accountMapper.getName() + ", ";
				}
			} else if (accountMapper instanceof AccountPoolMapper) {
				if (((AccountPoolMapper)accountMapper).getPersistenceFactory().equals(persistenceFactory)) {
					if (retStr==null) 
						retStr = "";
					retStr += "account mapper " + accountMapper.getName() + ", ";
				}
			}
		}

		if(retStr!=null)
			retStr = retStr.substring(0, retStr.length()-2);

		return retStr;
	}		

	static public String createSelectBox(String name, Collection items, String selected, String javascript, boolean includeEmptySlot) {
		String retStr = "<select name=\""+name+"\" " + (javascript!=null?javascript:"") + ">";
		if (includeEmptySlot)
			retStr += "<option " + (selected==null?"selected":"") + "></option>";
		Iterator it = items.iterator();
		while(it.hasNext())
		{
			String curName = getName(it.next());
			if (curName.equals("_test") || curName.equals("/DC=com/DC=example/OU=Services/CN=example.site.com"))
				continue;
			if (curName.equals(selected))
				retStr += "<option selected>" + curName + "</option>";
			else
				retStr += "<option>" + curName + "</option>";
		}
		retStr += "</select> \n";
		return retStr;
	}

	static public String createDoSubmit(Collection items, HttpServletRequest request) {
		String str = 
			"<script language=\"javascript\">"+
			"String.prototype.trim = function() { return this.replace(/^\\s+|\\s+$/g, \"\"); };"+
			"document.forms[0].elements['name'].value = document.forms[0].elements['name'].value.trim();"+
			"function doSubmit(str) {";

		if ("add".equals(request.getParameter("command")) || "add".equals(request.getParameter("originalCommand"))) {
			str += "if ( document.forms[0].elements['name'].value == '' ){ alert('First field cannot be empty'); return false; }";

			Iterator it = items.iterator();
			while(it.hasNext())
				str += "if ( document.forms[0].elements['name'].value == '" + getName(it.next()) + "'){ alert('Name already exists - please choose another name'); return false; }";
		}

		str += 
			"document.forms[0].elements['command'].value='save'; return true;"+
			"return false;"+
			"}"+
			"</script>";

		return str;
	}
	
	static public String createDoShortcutSubmit(Collection userGroups, Collection accountMappers, Collection g2aMappings, HttpServletRequest request) {
		String str = 
			"<script language=\"javascript\">"+
			"String.prototype.trim = function() { return this.replace(/^\\s+|\\s+$/g, \"\"); };"+
			"document.forms[0].elements['name'].value = document.forms[0].elements['name'].value.trim();"+
			"function doShortcutSubmit(str) {/*"+request.getParameter("command")+"*/";

		str += "if ( document.forms[0].elements['name'].value == '' ){ alert('First field cannot be empty'); return false; }";

		Iterator it = userGroups.iterator();
		while(it.hasNext())
			str += "if ( document.forms[0].elements['name'].value == '" + getName(it.next()) + "'){ alert('User group name already exists - please choose another name'); return false; }";

		it = accountMappers.iterator();
		while(it.hasNext())
			str += "if ( document.forms[0].elements['name'].value == '" + getName(it.next()) + "'){ alert('Account mapper name already exists - please choose another name'); return false; }";

		it = g2aMappings.iterator();
		while(it.hasNext())
			str += "if ( document.forms[0].elements['name'].value == '" + getName(it.next()) + "'){ alert('Group to account mapping name already exists - please choose another name'); return false; }";

		str += 
			"return true;"+
			"}"+
			"</script>";

		return str;
	}	

	static private String getName(Object obj) {
		if(obj instanceof String)
			return (String)obj;
		else if(obj instanceof CertificateHostToGroupMapping)
			return ((CertificateHostToGroupMapping)obj).getName();
		else if(obj instanceof GroupToAccountMapping)
			return ((GroupToAccountMapping)obj).getName();
		else if(obj instanceof AccountMapper)
			return ((AccountMapper)obj).getName();
		else if(obj instanceof UserGroup)
			return ((UserGroup)obj).getName();
		else if(obj instanceof PersistenceFactory)
			return ((PersistenceFactory)obj).getName();
		else if(obj instanceof VomsServer)
			return ((VomsServer)obj).getName();
		else
			return "";
	}

	static public String stripVo(String voGroup) {
		if(voGroup.length()>0 && voGroup.charAt(0)=='/') {
			String subStr = voGroup.substring(1);
			int index = subStr.indexOf("/");
			if (index!=-1)
				return subStr.substring(index+1);
		}
		return voGroup;
	}

	static public String stripGroup(String voGroup) {
		if(voGroup.length()>0 && voGroup.charAt(0)=='/') {
			String subStr = voGroup.substring(1);
			int index = subStr.indexOf("/");
			if (index!=-1)
				return subStr.substring(0, index);
		}
		return voGroup;
	}	
}
