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

if (request.getParameter("action")==null || 
	"saveAndReturn".equals(request.getParameter("action")) || 
	"delete".equals(request.getParameter("action")) ||
	"up".equals(request.getParameter("action")) ||
	"down".equals(request.getParameter("action"))) {
	if ("saveAndReturn".equals(request.getParameter("action"))) {
		try{
			HostToGroupMapping h2GMapping = configuration.getHostToGroupMapping( request.getParameter("name") );
			int index = configuration.getHostToGroupMappings().indexOf(h2GMapping);
			if (index!=-1) {
				configuration.getHostToGroupMappings().remove(index);
				configuration.getHostToGroupMappings().add(index, ConfigurationWebToolkit.parseHostToGroupMapping(request));
			}
			else
				configuration.getHostToGroupMappings().add( ConfigurationWebToolkit.parseHostToGroupMapping(request) );
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
		}catch(Exception e){
			message = "<div class=\"failure\">Error moving down: " + e.getMessage() + "</div>";
		}
	}	

	Collection h2GMappings = configuration.getHostToGroupMappings();
	
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
				"<form action=\"hostToGroup.jsp\" method=\"get\">"+
					"<input type=\"image\" src=\"images/Up24.gif\" name=\"action\" value=\"up\">"+
					"<input type=\"image\" src=\"images/Edit24.gif\" name=\"action\" value=\"edit\">"+
					"<input type=\"image\" src=\"images/Down24.gif\" name=\"action\" value=\"down\">"+
					"<input type=\"image\" src=\"images/Remove24.gif\" name=\"action\" value=\"delete\" onclick=\"if(!confirm('Are you sure you want to delete this host to group mapping?'))return false;\">"+
					"<input type=\"hidden\" name=\"name\" value=\"" + cH2GMapping.getName() + "\">"+
				"</form>"+
			"</td>"+
	  		"<td align=\"left\">"+
		   		"<table class=\"configElement\" width=\"100%\">"+
		  			"<tr>"+
			    		"<td>"+
				    		"For hosts matching ");
				    		
			if( cH2GMapping.getCn()!=null ) {
				out.write(
					"CN = <span style=\"font-style:italic\">"+cH2GMapping.getCn()+"</span>, ");
			}
			else {
				out.write(
					"DN = <span style=\"font-style:italic\">"+cH2GMapping.getDn()+"</span>, ");
			}
				
			out.write(
			    	"perform mappings using group(s) (try in order): ");
			
			Iterator g2AMappingsIt = cH2GMapping.getGroupToAccountMappings().iterator();
			while(g2AMappingsIt.hasNext())
			{
				GroupToAccountMapping g2AMapping = (GroupToAccountMapping)g2AMappingsIt.next();
				out.write( "<span style=\"font-style:italic\">"+g2AMapping.getName()+"</span>" );
				if( g2AMappingsIt.hasNext() )
					out.write(", ");
			}
			
			out.write(	
						"</td>"+
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
	|| "save".equals(request.getParameter("action"))) {
	HostToGroupMapping h2GMapping = null;
	if ("edit".equals(request.getParameter("action"))) {
		try {
			h2GMapping = (HostToGroupMapping)configuration.getHostToGroupMapping( request.getParameter("name") );
		} catch(Exception e) {
			out.write( "<div class=\"failure\">Error getting host to group mapping: " + e.getMessage() + "</div>" );
			return;
		}
	}

	if ("save".equals(request.getParameter("action"))) {
		try{
			HostToGroupMapping h2GMapping = configuration.getHostToGroupMapping( request.getParameter("name") );
			int index = configuration.getHostToGroupMappings().indexOf(h2GMapping);
			if (index!=-1) {
				configuration.getHostToGroupMappings().remove(index);
				configuration.getHostToGroupMappings().add(index, ConfigurationWebToolkit.parseHostToGroupMapping(request));
			}
			else
				configuration.getHostToGroupMappings().add( ConfigurationWebToolkit.parseHostToGroupMapping(request) );
			gums.setConfiguration(configuration);
			message = "<div class=\"success\">Host to group mapping has been saved.</div>";
		}catch(Exception e){
			message = "<div class=\"failure\">Error saving host to group mapping: " + e.getMessage() + "</div>";
		}
	}
	
	if (h2GMapping==null || h2GMapping instanceof CertificateHostToGroupMapping) {
		CertificateHostToGroupMapping cH2GMapping = (CertificateHostToGroupMapping)h2GMapping;
		
		out.write(
"<form action=\"hostToGroup.jsp\" method=\"get\">"+
	"<input type=\"hidden\" name=\"action\" value=\"\">"+
	"<table id=\"form\" border=\"0\" cellpadding=\"2\" cellspacing=\"2\" align=\"center\">");

		out.write(
		"<tr>"+
    		"<td nowrap width=\"1px\">"+
	    		"For hosts matching"+
		    "</td>"+
		    "<td nowrap>");
			
		if (cH2GMapping == null) {
			boolean isCN = true;
			out.write(
			    	"<input maxlength=\"256\" size=\"64\" name=\"name\" value=\"\"/>"+
					" cn<input type=\"radio\" name=\"hostType\" value=\"cn\" checked>"+
				    " dn<input type=\"radio\" name=\"hostType\" value=\"dn\">");
		}
		else {
			if( cH2GMapping.getCn()!=null )
				out.write(
					"CN = <span style=\"font-style:italic\">" + cH2GMapping.getCn() + "</span>,");
			else
				out.write(
					"DN = <span style=\"font-style:italic\">" + cH2GMapping.getDn() + "</span>,");
		}

		out.write(
		    "</td>"+
		"</tr>"+
		"<tr>"+
			"<td nowrap>perform mappings using<br>group(s) (try in order)</td>"+
			"<td>");
		
		int counter = 0;
		if (cH2GMapping!=null) {
			Collection g2AMappings = cH2GMapping.getGroupToAccountMappings();
			Iterator g2AMappingsIt = g2AMappings.iterator();
			while(g2AMappingsIt.hasNext())
			{
				GroupToAccountMapping g2AMapping = (GroupToAccountMapping)g2AMappingsIt.next();
				out.write( ConfigurationWebToolkit.createSelectBox("g2AM"+counter, 
					configuration.getGroupToAccountMappings().values(), 
					g2AMapping.getName(),
					"onchange=\"document.forms[0].elements['action'].value='save';document.forms[0].submit();\"") );
				counter++;
			}
		}
		out.write( ConfigurationWebToolkit.createSelectBox("g2AM"+counter, 
			configuration.getGroupToAccountMappings().values(), 
			null,
			"onchange=\"document.forms[0].elements['action'].value='save';document.forms[0].submit();\"") );
		
		out.write(
		"</td>"+
		"<td width=\"25\"></td>"+
	"</tr>");

		out.write(
	"<tr>"+
        "<td colspan=2>"+
        	"<div style=\"text-align: center;\">"+
        		"<button type=\"submit\" onclick=\"document.forms[0].elements['action'].value='saveAndReturn';document.forms[0].submit();\">Save</button>"+
        	"</div>"+
        "</td>"+
	"</tr>"+
  "</table>"+
"</form>");
	}
}

%>

</div>
<%@include file="bottomNav.jspf"%>
</body>
</html>
