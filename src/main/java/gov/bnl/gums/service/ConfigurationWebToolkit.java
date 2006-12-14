/*
 * ConfigurationWebToolkit.java
 *
 * Created on Oct 16, 2006, 2:03 PM
 */

package gov.bnl.gums.service;

import gov.bnl.gums.account.AccountMapper;
import gov.bnl.gums.configuration.Configuration;
import gov.bnl.gums.groupToAccount.GroupToAccountMapping;
import gov.bnl.gums.hostToGroup.CertificateHostToGroupMapping;
import gov.bnl.gums.hostToGroup.HostToGroupMapping;
import gov.bnl.gums.userGroup.UserGroup;

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
	
	static public String getReferencesStr(Configuration configuration, String g2AMappingName) {
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
	
	static public String createSelectBox(String name, Collection items, String selected, String javascript) {
		String retStr = new String("<select name=\""+name+"\" " + (javascript!=null?javascript:"") + ">"+
			"<option " + (selected==null?"selected":"") + "></option>");
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
		if(obj instanceof CertificateHostToGroupMapping)
			return ((CertificateHostToGroupMapping)obj).getName();
		if(obj instanceof GroupToAccountMapping)
			return ((GroupToAccountMapping)obj).getName();
		if(obj instanceof AccountMapper)
			return ((AccountMapper)obj).getName();
		if(obj instanceof UserGroup)
			return ((UserGroup)obj).getName();
		else
			return "";
	}
}
