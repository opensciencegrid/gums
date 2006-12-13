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
	
	static public CertificateHostToGroupMapping parseHostToGroupMapping(HttpServletRequest request) {
		CertificateHostToGroupMapping hostToGroupMapping = new CertificateHostToGroupMapping();
		hostToGroupMapping.setCn("blah");
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
