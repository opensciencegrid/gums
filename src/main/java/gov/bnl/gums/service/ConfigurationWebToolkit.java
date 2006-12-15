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
import gov.bnl.gums.persistence.PersistenceFactory;
import gov.bnl.gums.userGroup.UserGroup;
import gov.bnl.gums.userGroup.VirtualOrganization;

import javax.servlet.http.HttpServletRequest;
import java.rmi.Remote;
import java.util.Collection;
import java.util.Iterator;

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

		return userGroup;
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
	
	static public String getGroupToAccountMapperReferences(Configuration configuration, String accountMapperName) {
		String retStr = null;
		Collection g2AMappings = configuration.getGroupToAccountMappings().values();
		Iterator it = g2AMappings.iterator();
		while (it.hasNext()) {
			GroupToAccountMapping g2AMapping = (GroupToAccountMapping)it.next();
			Iterator it2 = g2AMapping.getAccountMappers().iterator();
			while (it2.hasNext()) {
				AccountMapper thisAccountMapper = (AccountMapper)it2.next();
				if (thisAccountMapper.getName().equals(accountMapperName)) {
					if (retStr==null) 
						retStr = "";
					retStr += g2AMapping.getName() + ", ";
					break;
				}
			}
		}
		if(retStr!=null)
			retStr = retStr.substring(0, retStr.length()-2);
		return retStr;
	}
	
	static public String getUserGroupReferences(Configuration configuration, String accountMapperName) {
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
