/*
 * ConfigurationWebToolkit.java
 *
 * Created on Oct 16, 2006, 2:03 PM
 */

package gov.bnl.gums.service;

import gov.bnl.gums.account.AccountMapper;
import gov.bnl.gums.account.AccountPoolMapper;
import gov.bnl.gums.account.GecosAccountMapper;
import gov.bnl.gums.account.GecosLdapAccountMapper;
import gov.bnl.gums.account.GroupAccountMapper;
import gov.bnl.gums.account.ManualAccountMapper;
import gov.bnl.gums.configuration.Configuration;
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
import gov.bnl.gums.userGroup.VirtualOrganization;

import javax.servlet.http.HttpServletRequest;
import java.rmi.Remote;
import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;

public class ConfigurationWebToolkit implements Remote {
	static public GroupToAccountMapping parseGroupToAccountMapping(HttpServletRequest request) {
		return null;
	}
	
	static public CertificateHostToGroupMapping parseHostToGroupMapping(Configuration configuration, HttpServletRequest request) throws Exception {
		CertificateHostToGroupMapping hostToGroupMapping = new CertificateHostToGroupMapping();

		String type = request.getParameter("type");
		if(type.equals("cn"))
			hostToGroupMapping.setCn( request.getParameter("name") );
		else if(type.equals("dn"))
			hostToGroupMapping.setDn( request.getParameter("name") );
		
		int counter = 0;
		while(request.getParameter("g2AM" + counter)!=null) {
			String g2AMName = request.getParameter("g2AM" + counter);
			if (!g2AMName.equals("")) {
				GroupToAccountMapping g2AM = (GroupToAccountMapping)configuration.getGroupToAccountMappings().get( g2AMName );
				hostToGroupMapping.addGroupToAccountMapping(g2AM);
			}
			counter++;
		}
		
		return hostToGroupMapping;
	}	
	
	static public GroupToAccountMapping parseGroupToAccountMapping(Configuration configuration, HttpServletRequest request) throws Exception {
		GroupToAccountMapping groupToAccountMapping = new GroupToAccountMapping();

		groupToAccountMapping.setName( request.getParameter("name") );
		
		int counter = 0;
		while(request.getParameter("aM" + counter)!=null) {
			String accountMapperName = request.getParameter("aM" + counter);
			if (!accountMapperName.equals("")) {
				AccountMapper accountMapper = (AccountMapper)configuration.getAccountMappers().get( accountMapperName );
				groupToAccountMapping.addAccountMapper(accountMapper);
			}
			counter++;
		}
		
		counter = 0;
		while(request.getParameter("uG" + counter)!=null) {
			String userGroupName = request.getParameter("uG" + counter);
			if (!userGroupName.equals("")) {
				UserGroup userGroup = (UserGroup)configuration.getUserGroups().get( userGroupName );
				groupToAccountMapping.addUserGroup(userGroup);
			}
			counter++;
		}
		
		return groupToAccountMapping;
	}		

	static public AccountMapper parseAccountMapper(Configuration configuration, HttpServletRequest request) throws Exception {
		AccountMapper accountMapper = null;
		
		String className = request.getParameter("className");
		
		if (className.equals("gov.bnl.gums.account.GroupAccountMapper")) {
			accountMapper = new GroupAccountMapper();
			accountMapper.setName( request.getParameter("name") );
			if (request.getParameter("accountName")!=null)
				((GroupAccountMapper)accountMapper).setAccountName( request.getParameter("accountName") );
		}
		else if (className.equals("gov.bnl.gums.account.ManualAccountMapper")) {
			accountMapper = new ManualAccountMapper();
			accountMapper.setName( request.getParameter("name") );
			if (request.getParameter("accountName")!=null)
				((ManualAccountMapper)accountMapper).setAccountName( request.getParameter("accountName") );
			if (request.getParameter("persistenceFactory")!=null)
				((ManualAccountMapper)accountMapper).setPersistenceFactory( (PersistenceFactory)configuration.getPersistenceFactories().get( request.getParameter("persistenceFactory") ) );
		}
		else if (className.equals("gov.bnl.gums.account.AccountPoolMapper")) {
			accountMapper = new AccountPoolMapper();
			accountMapper.setName( request.getParameter("name") );
			if (request.getParameter("accountPool")!=null)
				((AccountPoolMapper)accountMapper).setAccountPool( request.getParameter("accountPool") );
			if (request.getParameter("persistenceFactory")!=null)
				((AccountPoolMapper)accountMapper).setPersistenceFactory( (PersistenceFactory)configuration.getPersistenceFactories().get( request.getParameter("persistenceFactory") ) );
		}
		else if (className.equals("gov.bnl.gums.account.GecosLdapAccountMapper")) {
			accountMapper = new GecosLdapAccountMapper();
			accountMapper.setName( request.getParameter("name") );
			if (request.getParameter("serviceUrl")!=null)
				((GecosLdapAccountMapper)accountMapper).setJndiLdapUrl( request.getParameter("serviceUrl") );
		}

		return accountMapper;
	}		

	static public UserGroup parseUserGroup(Configuration configuration, HttpServletRequest request) throws Exception {
		UserGroup userGroup = null;

		String className = request.getParameter("className");
		
		if (className.equals("gov.bnl.gums.userGroup.ManualUserGroup")) {
			userGroup = new ManualUserGroup();
			userGroup.setName( request.getParameter("name") );
			userGroup.setAccess( request.getParameter("access") );
			if (request.getParameter("persistenceFactory")!=null)
				((ManualUserGroup)userGroup).setPersistenceFactory( (PersistenceFactory)configuration.getPersistenceFactories().get( request.getParameter("persistenceFactory") ) );
		} else if (className.equals("gov.bnl.gums.userGroup.LDAPUserGroup")) {
			userGroup = new LDAPUserGroup();
			userGroup.setName( request.getParameter("name") );
			userGroup.setAccess( request.getParameter("access") );
			if (request.getParameter("server")!=null)
				((LDAPUserGroup)userGroup).setServer( request.getParameter("server") );
			if (request.getParameter("query")!=null)
				((LDAPUserGroup)userGroup).setQuery( request.getParameter("query") );
			if (request.getParameter("persistenceFactory")!=null)
				((LDAPUserGroup)userGroup).setPersistenceFactory( (PersistenceFactory)configuration.getPersistenceFactories().get( request.getParameter("persistenceFactory") ) );
		} else if (className.equals("gov.bnl.gums.userGroup.VOMSUserGroup")) {
			userGroup = new VOMSUserGroup();
			userGroup.setName( request.getParameter("name") );
			userGroup.setAccess( request.getParameter("access") );
			if (request.getParameter("vo")!=null)
				((VOMSUserGroup)userGroup).setVirtualOrganization( (VirtualOrganization)configuration.getVirtualOrganizations().get( request.getParameter("vo") ) );
			if (request.getParameter("url")!=null)
				((VOMSUserGroup)userGroup).setRemainderUrl( request.getParameter("url") );
			if (request.getParameter("nVOMS")!=null)
				((VOMSUserGroup)userGroup).setAcceptProxyWithoutFQAN( request.getParameter("nVOMS").equals("allowed") );
			if (request.getParameter("group")!=null)
				((VOMSUserGroup)userGroup).setVoGroup( request.getParameter("group") );
			if (request.getParameter("role")!=null)
				((VOMSUserGroup)userGroup).setVoGroup( request.getParameter("role") );
		}
		
		return userGroup;
	}

	static public VirtualOrganization parseVirtualOrganization(Configuration configuration, HttpServletRequest request) throws Exception {
		VirtualOrganization virtualOrganization = new VirtualOrganization();
		virtualOrganization.setName( request.getParameter("name") );
		if (request.getParameter("baseURL")!=null)
			virtualOrganization.setSslKey( request.getParameter("baseURL") );
		if (request.getParameter("sslKey")!=null)
			virtualOrganization.setSslKey( request.getParameter("sslKey") );
		if (request.getParameter("sslCert")!=null)
			virtualOrganization.setSslCertfile( request.getParameter("sslCert") );
		if (request.getParameter("sslCA")!=null)
			virtualOrganization.setSslCAFiles( request.getParameter("sslCA") );
		if (request.getParameter("sslKeyPW")!=null)
			virtualOrganization.setSslKeyPasswd( request.getParameter("sslKeyPW") );
		return virtualOrganization;
	}	
	
	static public PersistenceFactory parsePersistenceFactory(Configuration configuration, HttpServletRequest request) throws Exception {
		PersistenceFactory persistenceFactory = null;
		
		String className = request.getParameter("className");
		
		if (className.equals("gov.bnl.gums.persistence.HibernatePersistenceFactory")) {
			persistenceFactory = new HibernatePersistenceFactory();
			persistenceFactory.setName( request.getParameter("name") );
			
			Properties properties = new Properties();
			properties.put("mysql.hibernate.connection.driver_class", request.getParameter("driverClass"));
			properties.put("mysql.hibernate.dialect", request.getParameter("dialect"));
			properties.put("mysql.hibernate.c3p0.min_size", request.getParameter("min_size"));
			properties.put("mysql.hibernate.c3p0.max_size", request.getParameter("max_size"));
			properties.put("mysql.hibernate.c3p0.timeout", request.getParameter("timeout"));
			properties.put("mysql.hibernate.connection.url", request.getParameter("url"));
			properties.put("mysql.hibernate.connection.username", request.getParameter("username"));
			properties.put("mysql.hibernate.connection.password", request.getParameter("password"));
			properties.put("mysql.hibernate.connection.autoReconnect", request.getParameter("autoReconnect"));
			((HibernatePersistenceFactory)persistenceFactory).setProperties(properties);
		} 
		else if (className.equals("gov.bnl.gums.persistence.LDAPPersistenceFactory")) {
			persistenceFactory = new LDAPPersistenceFactory();
			persistenceFactory.setName( request.getParameter("name") );
			((LDAPPersistenceFactory)persistenceFactory).setSynchGroups( request.getParameter("synchGroups").equals("true") );
			
			Properties properties = new Properties();
			properties.put("ldap.java.naming.provider.url", request.getParameter("ldapUrl"));
			properties.put("ldap.java.naming.factory.initial", request.getParameter("initial"));
			properties.put("ldap.java.naming.security.authentication", request.getParameter("auth"));
			properties.put("ldap.java.naming.security.principal", request.getParameter("auth"));
			properties.put("ldap.java.naming.security.credentials", request.getParameter("cred"));
			((LocalPersistenceFactory)persistenceFactory).setProperties(properties);
		} 
		else if (className.equals("gov.bnl.gums.persistence.LocalPersistenceFactory")) {
			persistenceFactory = new LocalPersistenceFactory();
			persistenceFactory.setName( request.getParameter("name") );
			((LocalPersistenceFactory)persistenceFactory).setSynchGroups( request.getParameter("synchGroups").equals("true") );
			
			Properties properties = new Properties();
			properties.put("mysql.hibernate.connection.driver_class", request.getParameter("driverClass"));
			properties.put("mysql.hibernate.dialect", request.getParameter("dialect"));
			properties.put("mysql.hibernate.c3p0.min_size", request.getParameter("min_size"));
			properties.put("mysql.hibernate.c3p0.max_size", request.getParameter("max_size"));
			properties.put("mysql.hibernate.c3p0.timeout", request.getParameter("timeout"));
			properties.put("mysql.hibernate.connection.url", request.getParameter("url"));
			properties.put("mysql.hibernate.connection.username", request.getParameter("username"));
			properties.put("mysql.hibernate.connection.password", request.getParameter("password"));
			properties.put("mysql.hibernate.connection.autoReconnect", request.getParameter("autoReconnect"));
			properties.put("ldap.java.naming.provider.url", request.getParameter("ldapUrl"));
			properties.put("ldap.java.naming.factory.initial", request.getParameter("initial"));
			properties.put("ldap.java.naming.security.authentication", request.getParameter("auth"));
			properties.put("ldap.java.naming.security.principal", request.getParameter("auth"));
			properties.put("ldap.java.naming.security.credentials", request.getParameter("cred"));
			((LocalPersistenceFactory)persistenceFactory).setProperties(properties);
		}

		return persistenceFactory;
	}		
	
	static public String getHostToGroupReferences(Configuration configuration, String g2AMappingName) {
		String retStr = null;
		Collection h2GMappings = configuration.getHostToGroupMappings();
		Iterator it = h2GMappings.iterator();
		while (it.hasNext()) {
			HostToGroupMapping h2GMapping = (HostToGroupMapping)it.next();
			Iterator it2 = h2GMapping.getGroupToAccountMappings().iterator();
			while (it2.hasNext()) {
				GroupToAccountMapping thisG2AMapping = (GroupToAccountMapping)it2.next();
				if (thisG2AMapping.getName().equals(g2AMappingName)) {
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
					AccountMapper thisAccountMapper = (AccountMapper)it2.next();
					if (thisAccountMapper.getName().equals(name)) {
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
					UserGroup thisUserGroup = (UserGroup)it2.next();
					if (thisUserGroup.getName().equals(name)) {
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
	
	static public String getVOMSUserGroupReferences(Configuration configuration, String virtualOrganization) {
		String retStr = null;
		Collection userGroups = configuration.getUserGroups().values();
		Iterator it = userGroups.iterator();
		while (it.hasNext()) {
			VOMSUserGroup userGroup = (VOMSUserGroup)it.next();
			if ( virtualOrganization.equals( userGroup.getVirtualOrganization() ) ) {
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
		
		if ("add".equals(request.getParameter("action")) || "add".equals(request.getParameter("originalAction"))) {
			str += "if ( document.forms[0].elements['name'].value == '' ){ alert('First field cannot be empty'); return false; }";
	    			
		    Iterator it = items.iterator();
		    while(it.hasNext())
		    	str += "if ( document.forms[0].elements['name'].value == '" + getName(it.next()) + "'){ alert('Name already exists - please choose another name'); return false; }";
		}

		str += 
	    			"document.forms[0].elements['action'].value='save'; return true;"+
					"return false;"+
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
		else if(obj instanceof VirtualOrganization)
			return ((VirtualOrganization)obj).getName();
		else
			return "";
	}
}
