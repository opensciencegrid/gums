/*
 * ConfigurationWebToolkit.java
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

import net.sf.hibernate.collection.Map;

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
			((HibernatePersistenceFactory)persistenceFactory).setProperties( getHibernateProperties(persistenceFactory, request, false) );
		} 
		else if (className.equals("gov.bnl.gums.persistence.LDAPPersistenceFactory")) {
			persistenceFactory = new LDAPPersistenceFactory();
			persistenceFactory.setName( request.getParameter("name") );
			((LDAPPersistenceFactory)persistenceFactory).setSynchGroups( request.getParameter("synchGroups")!=null ? request.getParameter("synchGroups").equals("true") : false );
			((LDAPPersistenceFactory)persistenceFactory).setProperties( getLdapProperties(persistenceFactory, request, false) );
		} 
		else if (className.equals("gov.bnl.gums.persistence.LocalPersistenceFactory")) {
			persistenceFactory = new LocalPersistenceFactory();
			persistenceFactory.setName( request.getParameter("name") );
			((LocalPersistenceFactory)persistenceFactory).setSynchGroups( request.getParameter("synchGroups")!=null ? request.getParameter("synchGroups").equals("true") : false );
			Properties properties = getHibernateProperties(persistenceFactory, request, true);
			properties.putAll(getLdapProperties(persistenceFactory, request, true));
			((LocalPersistenceFactory)persistenceFactory).setProperties( properties );
		}

		return persistenceFactory;
	}
	
	static public Properties getHibernateProperties(PersistenceFactory persistenceFactory, HttpServletRequest request, boolean includeMySql) {
		Properties properties = new Properties();
		properties.put((includeMySql?"mysql.":"") + "hibernate.connection.url", (request.getParameter("mySqlUrl")!=null ? request.getParameter("mySqlUrl") : ""));
		properties.put((includeMySql?"mysql.":"") + "hibernate.connection.username", (request.getParameter("mySqlUsername")!=null ? request.getParameter("mySqlUsername") : ""));
		properties.put((includeMySql?"mysql.":"") + "hibernate.connection.password", (request.getParameter("mySqlPassword")!=null ? request.getParameter("mySqlPassword") : ""));
		properties.put((includeMySql?"mysql.":"") + "hibernate.connection.driver_class", "com.mysql.jdbc.Driver");
		properties.put((includeMySql?"mysql.":"") + "hibernate.dialect", "net.sf.hibernate.dialect.MySQLDialect");
		properties.put((includeMySql?"mysql.":"") + "hibernate.c3p0.min_size", "3");
		properties.put((includeMySql?"mysql.":"") + "hibernate.c3p0.max_size", "20");
		properties.put((includeMySql?"mysql.":"") + "hibernate.c3p0.timeout", "180");
		properties.put((includeMySql?"mysql.":"") + "hibernate.connection.autoReconnect", "true");
		return properties;
	}

	static public Properties getLdapProperties(PersistenceFactory persistenceFactory, HttpServletRequest request, boolean includeLdap) {
		Properties properties = new Properties();
		properties.put((includeLdap?"ldap.":"") + "java.naming.security.authentication", (request.getParameter("ldapAuthentication")!=null ? request.getParameter("ldapAuthentication") : "simple"));
		properties.put((includeLdap?"ldap.":"") + "java.naming.security.principal", (request.getParameter("ldapPrincipal")!=null ? request.getParameter("ldapPrincipal") : ""));
		properties.put((includeLdap?"ldap.":"") + "java.naming.security.credentials", (request.getParameter("ldapCredentials")!=null ? request.getParameter("ldapCredentials") : ""));
		properties.put((includeLdap?"ldap.":"") + "java.naming.provider.url", "com.mysql.jdbc.Driver");
		properties.put((includeLdap?"ldap.":"") + "java.naming.factory.initial", "net.sf.hibernate.dialect.MySQLDialect");
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
		Iterator it;
		
		it = configuration.getVirtualOrganizations().values().iterator();
		while (it.hasNext()) {
			VirtualOrganization vo = (VirtualOrganization)it.next();
			if(vo.getPersistenceFactory().equals(persistenceFactory)) {
				if (retStr==null) 
					retStr = "";
				retStr += "virtual organization " + vo.getName() + ", ";
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
