/*
 * ConfigurationWebToolkit.java
 *
 * Created on Oct 16, 2006, 2:03 PM
 */

package gov.bnl.gums.service;

import gov.bnl.gums.configuration.Configuration;
import gov.bnl.gums.groupToAccount.GroupToAccountMapping;
import gov.bnl.gums.hostToGroup.CertificateHostToGroupMapping;
import javax.servlet.http.HttpServletRequest;
import java.rmi.Remote;
import java.util.Collection;
import java.util.Iterator;

public class ConfigurationWebToolkit implements Remote {
	static public GroupToAccountMapping parseGroupToAccountMapping(HttpServletRequest request) {
		return null;
	}
	
	static public CertificateHostToGroupMapping parseHostToGroupMapping(Configuration configuration, HttpServletRequest request) throws Exception {
		String name = request.getParameter("name");
		CertificateHostToGroupMapping hostToGroupMapping = new CertificateHostToGroupMapping();
		String type = request.getParameter("type");
		if(type.equals("cn"))
			hostToGroupMapping.setCn(name);
		else if(type.equals("dn"))
			hostToGroupMapping.setDn(name);
		int counter = 0;
		while(request.getParameter("g2AM" + counter)!=null) {
			if (request.getParameter("g2AM" + counter)!="") {
				GroupToAccountMapping g2AM = (GroupToAccountMapping)configuration.getGroupToAccountMappings().get( request.getParameter("g2AM" + counter) );
				if (g2AM!=null)
					hostToGroupMapping.addGroupToAccountMapping(g2AM);
				else
					throw new Exception("group to account mapping " + request.getParameter("g2AM" + counter) + "does not exist");
			}
			counter++;
		}
		return hostToGroupMapping;
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
		else
			return "";
	}
}
