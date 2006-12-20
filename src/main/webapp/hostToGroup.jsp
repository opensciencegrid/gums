<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@page import="gov.bnl.gums.*"%>
<jsp:useBean id="gums" scope="application" class="gov.bnl.gums.admin.GUMSAPIImpl" />
<%@ page import="gov.bnl.gums.hostToGroup.*" %>
<%@ page import="gov.bnl.gums.groupToAccount.GroupToAccountMapping" %>
<%@ page import="gov.bnl.gums.configuration.*" %>
<%@ page import="java.lang.Math" %>
<%@ page import="gov.bnl.gums.service.ConfigurationWebToolkit" %>
<%@ page import="java.util.*" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
<head>
 	<title>GUMS Configuration</title>
 	<link href="gums.css" type="text/css" rel="stylesheet"/>
</head>
<body>
<%@include file="topNav.jspf"%>
<div id="title">
<h1><span>GUMS</span></h1>
<h2><span>GUMS Configuration</span></h2>
</div>
<%@include file="sideNav.jspf"%>
<div id="body">
<p>
<%-- <jsp:useBean id="beanInstanceName" scope="session" class="beanPackage.BeanClassName" /> --%>
<%-- <jsp:getProperty name="beanInstanceName"  property="propertyName" /> --%>

Configure host to group mappings.
</p>

<%

Configuration configuration = gums.getConfiguration();
String message = null;
String movedName = null;
Collection h2GMappings = configuration.getHostToGroupMappings();

if (request.getParameter("action")==null || 
	"save".equals(request.getParameter("action")) || 
	"delete".equals(request.getParameter("action")) ||
	"up".equals(request.getParameter("action")) ||
	"down".equals(request.getParameter("action"))) {
	
	if ("save".equals(request.getParameter("action"))) {
		try{
			HostToGroupMapping h2GMapping = configuration.getHostToGroupMapping( request.getParameter("name") );
			int index = configuration.getHostToGroupMappings().indexOf(h2GMapping);
			if (index!=-1) {
				configuration.getHostToGroupMappings().remove(index);
				configuration.getHostToGroupMappings().add(index, ConfigurationWebToolkit.parseHostToGroupMapping(configuration, request));
			}
			else
				configuration.getHostToGroupMappings().add( ConfigurationWebToolkit.parseHostToGroupMapping(configuration, request) );
			gums.setConfiguration(configuration);
			message = "<div class=\"success\">Host to group mapping has been saved.</div>";
		}catch(Exception e){
			message = "<div class=\"failure\">Error saving host to group mapping: " + e.getMessage() + "</div>";
		}
	}

	if ("delete".equals(request.getParameter("action"))) {
		try{
			if( configuration.removeHostToGroupMapping( request.getParameter("name") )!=null ) {
				gums.setConfiguration(configuration);
				message = "<div class=\"success\">Host to group mapping has been deleted.</div>";
			}
			else
				message = "<div class=\"failure\">Error deleting host to group mapping</div>";
		}catch(Exception e){
			message = "<div class=\"failure\">Error deleting host to group mapping: " + e.getMessage() + "</div>";
		}
	}

	if ("up".equals(request.getParameter("action"))) {
		try{
			HostToGroupMapping h2GMapping = configuration.getHostToGroupMapping( request.getParameter("name") );
			int index = configuration.getHostToGroupMappings().indexOf(h2GMapping);
			configuration.getHostToGroupMappings().remove(index);
			configuration.getHostToGroupMappings().add(Math.max(0, index-1), h2GMapping);
			gums.setConfiguration(configuration);
			movedName = request.getParameter("name");
		}catch(Exception e){
			message = "<div class=\"failure\">Error moving up: " + e.getMessage() + "</div>";
		}
	}
	
	if ("down".equals(request.getParameter("action"))) {
		try{
			HostToGroupMapping h2GMapping = configuration.getHostToGroupMapping( request.getParameter("name") );
			int index = configuration.getHostToGroupMappings().indexOf(h2GMapping);
			configuration.getHostToGroupMappings().remove(index);
			configuration.getHostToGroupMappings().add(Math.min(configuration.getHostToGroupMappings().size(), index+1), h2GMapping);
			gums.setConfiguration(configuration);
			movedName = request.getParameter("name");
		}catch(Exception e){
			message = "<div class=\"failure\">Error moving down: " + e.getMessage() + "</div>";
		}
	}	

	out.write(
"<table id=\"form\" cellpadding=\"2\" cellspacing=\"2\">");

	if(message!=null)
		out.write( "<tr><td colspan=\"2\">" + message + "</td></tr>" );
				
	Iterator h2GMappingsIt = h2GMappings.iterator();
	while(h2GMappingsIt.hasNext()) {
		HostToGroupMapping h2GMapping = h2GMappingsIt.hasNext() ? (HostToGroupMapping)h2GMappingsIt.next() : null;
		
		if(h2GMapping instanceof CertificateHostToGroupMapping) {
			CertificateHostToGroupMapping cH2GMapping = (CertificateHostToGroupMapping)h2GMapping;
			
			out.write(
	   	"<tr>"+
			"<td width=\"50\" valign=\"top\">"+
				"<form action=\"hostToGroup.jsp#" + cH2GMapping.getName() + "\" method=\"get\">"+
					"<a name=\"" + cH2GMapping.getName() + "\">"+
						"<input type=\"image\" src=\"images/Up24.gif\" name=\"action\" value=\"up\">"+
						"<input type=\"image\" src=\"images/Edit24.gif\" name=\"action\" value=\"edit\">"+
						"<input type=\"image\" src=\"images/Down24.gif\" name=\"action\" value=\"down\" onclick=\"\">"+
						"<input type=\"image\" src=\"images/Remove24.gif\" name=\"action\" value=\"delete\" onclick=\"if(!confirm('Are you sure you want to delete this host to group mapping?'))return false;\">"+
						"<input type=\"hidden\" name=\"name\" value=\"" + cH2GMapping.getName() + "\">"+
					"</a>"+
				"</form>"+
			"</td>"+
	  		"<td align=\"left\">"+
		   		"<table class=\"" + (cH2GMapping.getName().equals(movedName)?"configMovedElement":"configElement") + "\" width=\"100%\">"+
		  			"<tr>"+
			    		"<td>"+
				    		"For requests from hosts matching "+
				    		"<span style=\"color:blue\">" + cH2GMapping.getName() + "</span>, "+
				    		"route request to group" + (cH2GMapping.getGroupToAccountMappings().size()>1?"s":"") + " ");
			
			Iterator g2AMappingsIt = cH2GMapping.getGroupToAccountMappings().iterator();
			while(g2AMappingsIt.hasNext())
			{
				GroupToAccountMapping g2AMapping = (GroupToAccountMapping)g2AMappingsIt.next();
				out.write( "<span style=\"color:blue\">"+g2AMapping.getName()+"</span>" );
				if( g2AMappingsIt.hasNext() )
					out.write(", ");
			}
			
			if (cH2GMapping.getGroupToAccountMappings().size()>1)
				out.write(" (try in order)");
						
			out.write(	
						".</td>"+
			      	"</tr>"+
				"</table>"+
			"</td>"+
			"<td width=\"10\"></td>"+		
		"</tr>");
		}
	}

	out.write(
		"<tr>"+
	        "<td colspan=2>"+
	        	"<form action=\"hostToGroup.jsp\" method=\"get\">"+
	        		"<div style=\"text-align: center;\"><button type=\"submit\" name=\"action\" value=\"add\">Add</button></div>"+
	        	"</form>"+
	        "</td>"+
		"</tr>"+
	  "</table>"+
"</form>");
}

else if ("edit".equals(request.getParameter("action"))
	|| "add".equals(request.getParameter("action"))
	|| "reload".equals(request.getParameter("action"))) {
	
	HostToGroupMapping h2GMapping = null;
	
	if ("edit".equals(request.getParameter("action"))) {
		try {
			h2GMapping = (HostToGroupMapping)configuration.getHostToGroupMapping( request.getParameter("name") );
		} catch(Exception e) {
			out.write( "<div class=\"failure\">Error getting host to group mapping: " + e.getMessage() + "</div>" );
			return;
		}
	}

	if ("reload".equals(request.getParameter("action"))) {
		try{
			h2GMapping = ConfigurationWebToolkit.parseHostToGroupMapping(configuration, request);
		} catch(Exception e) {
			out.write( "<div class=\"failure\">Error reloading host to group mapping: " + e.getMessage() + "</div>" );
			return;
		}
	}
	
	else if ("add".equals(request.getParameter("action"))) {
		h2GMapping = new CertificateHostToGroupMapping();
	}
	
	CertificateHostToGroupMapping cH2GMapping = (CertificateHostToGroupMapping)h2GMapping;
		
	out.write(
"<form action=\"hostToGroup.jsp\" method=\"get\">"+
	"<input type=\"hidden\" name=\"action\" value=\"\">"+
	"<input type=\"hidden\" name=\"originalAction\" value=\""+ 
		("reload".equals(request.getParameter("action")) ? request.getParameter("originalAction") : request.getParameter("action")) +
	"\">"+
	"<table id=\"form\" border=\"0\" cellpadding=\"2\" cellspacing=\"2\" align=\"center\">"+
		"<tr>"+
    		"<td nowrap style=\"text-align: right;\">"+
	    		"For requests from hosts matching"+
		    "</td>"+
		    "<td nowrap>");

	if ("add".equals(request.getParameter("action")) || "add".equals(request.getParameter("originalAction")))
		out.write(
		    	"<input maxlength=\"256\" size=\"64\" name=\"name\" value=\"" + cH2GMapping.getName() + "\"/>"+
				" cn<input type=\"radio\" name=\"type\" value=\"cn\" " + (cH2GMapping.getDn()==null?"checked":"") + ">"+
			    " dn<input type=\"radio\" name=\"type\" value=\"dn\" " + (cH2GMapping.getDn()!=null?"checked":"") + ">");
	else
		out.write(
		    	cH2GMapping.getName()+
		    	"<input type=\"hidden\" name=\"name\" value=\"" + cH2GMapping.getName() + "\"/>"+
		    	"<input type=\"hidden\" name=\"type\" value=\"" + (cH2GMapping.getDn()!=null?"dn":"cn") + "\">");	

	out.write(
		    "</td>"+
		"</tr>"+
		"<tr>"+
			"<td nowrap style=\"text-align: right;\">route request to group(s)</td>"+
			"<td>");
	
	// Create multiple group to account mappings
	int counter = 0;
	if (cH2GMapping!=null) {
		Collection g2AMappings = cH2GMapping.getGroupToAccountMappings();
		Iterator g2AMappingsIt = g2AMappings.iterator();
		while(g2AMappingsIt.hasNext())
		{
			GroupToAccountMapping g2AMapping = (GroupToAccountMapping)g2AMappingsIt.next();
			out.write( 
				ConfigurationWebToolkit.createSelectBox("g2AM"+counter, 
					configuration.getGroupToAccountMappings().values(), 
					g2AMapping.getName(),
					"onchange=\"document.forms[0].elements['action'].value='reload';document.forms[0].submit();\"",
					true) );
			counter++;
		}
	}
	out.write( 
		ConfigurationWebToolkit.createSelectBox("g2AM"+counter, 
			configuration.getGroupToAccountMappings().values(), 
			null,
			"onchange=\"document.forms[0].elements['action'].value='reload';document.forms[0].submit();\"",
			true)+
			"(try in order)");
	
	out.write(
			"</td>"+
		"</tr>"+
		"<tr>"+
	        "<td colspan=2 style=\"text-align: right;\">"+
				ConfigurationWebToolkit.createDoSubmit(h2GMappings, request)+
	        	"<div style=\"text-align: center;\">"+
	        		"<button type=\"submit\" onclick=\"return doSubmit()\">Save</button>"+
	        	"</div>"+
	        "</td>"+
		"</tr>"+
	"</table>"+
"</form>");
}

%>

</div>
<%@include file="bottomNav.jspf"%>
</body>
</html>
