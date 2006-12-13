<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@page import="gov.bnl.gums.*"%>
<jsp:useBean id="gums" scope="application" class="gov.bnl.gums.admin.GUMSAPIImpl" />
<%@ page import="gov.bnl.gums.account.*" %>
<%@ page import="gov.bnl.gums.groupToAccount.*" %>
<%@ page import="gov.bnl.gums.userGroup.*" %>
<%@ page import="gov.bnl.gums.configuration.*" %>
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

Configures group to account mapper mappings.
</p>

<%
Configuration configuration = gums.getConfiguration();

if ( request.getParameter("action")==null || "save".equals(request.getParameter("action")) ) {
	if ( "save".equals(request.getParameter("action")) ) {
		try{
			configuration.addGroupToAccountMapping( ConfigurationWebToolkit.parseGroupToAccountMapping(request) );
			out.write(
"<p>Group to account mapping has been successfully saved!</p>");
		}catch(Exception e){
			out.write(
"<p>Error saving group to account mapping: " + e.getMessage() + "</p>");
		}
	}
	
	Collection g2AMappings = configuration.getGroupToAccountMappings().values();
	
	out.write(
"<form action=\"groupToAccount.jsp\" method=\"get\">"+
  "<table id=\"form\" cellpadding=\"2\" cellspacing=\"2\">");

	Iterator g2AMappingIt = g2AMappings.iterator();
	int counter = 0;
	while(counter<10 || g2AMappingIt.hasNext())
	{
		GroupToAccountMapping g2AMapping = g2AMappingIt.hasNext()?(GroupToAccountMapping)g2AMappingIt.next():null;     
	
		out.write(
	"<tr>"+
		"<td width=\"25\" valign=\"top\">"+
			"<input type=\"image\" src=\"images/Edit24.gif\" name=\"action\" value=\"Edit"+counter+"\">"+
			"<input type=\"image\" src=\"images/Remove24.gif\" name=\"action\" value=\"Delete"+counter+"\">"+
		"</td>"+
   		"<td>"+
    		"<table class=\"configElement\" width=\"100%\">"+
   				"<tr>"+
   					"<td>"+
			    		"For users mapped to group <span style=\"font-style:italic\">"+(g2AMapping!=null?g2AMapping.getName():"")+
			    		"</span>, obtain local account from account mapper (try in order) <span style=\"font-style:italic\">");

		Iterator accountMapperIt =  g2AMapping.getAccountMappers().iterator();
		while(accountMapperIt.hasNext())
		{
			AccountMapper accountMapper = (AccountMapper)accountMapperIt.next();
			out.write( accountMapper.getName() );
			if (accountMapperIt.hasNext())
				out.write(", ");
		}
		
		out.write(		"</span> if member of VO (try in order): <span style=\"font-style:italic\">");
				    	
		Iterator userGroupIt = g2AMapping.getUserGroups().iterator();
		while(userGroupIt.hasNext())
		{
			UserGroup userGroup = (UserGroup)userGroupIt.next();
			out.write( userGroup.getName() );
			if (userGroupIt.hasNext())
				out.write(", ");
		}
		
		out.write(	"</span></td>"+
		      	"</tr>"+
			"</table>"+
		"</td>"+
		"<td width=\"25\"></td>"+
     "</tr>\n");
	
		counter++;
	} // end of group mapper while loop
	
	out.write(
	"<tr>"+
        "<td colspan=2><div style=\"text-align: center;\"><button type=\"submit\" name=\"action\" value=\"add\">Add new 'host to group mapping'</button></div>"+
        "</td>"+
	"</tr>"+
  "</table>"+
"</form>");
} // end of non-postback else

else if ( "add".equals(request.getParameter("action")) || "edit".equals(request.getParameter("action")) ) {
	GroupToAccountMapping groupToAccountMapping = null; 
	if( "add".equals(request.getParameter("action")) ) {
		groupToAccountMapping = new GroupToAccountMapping();
	} else {
		try{
			groupToAccountMapping = configuration.getGroupToAccountMapping( ConfigurationWebToolkit.parseGroupToAccountMapping(request).getName() );
		}catch(Exception e){
			out.write(
"<p>Error loading group to account mapping: " + e.getMessage() + "</p>");
		}		
	}
	
		out.write(
	"<table id=\"form\" cellpadding=\"2\" cellspacing=\"2\">");
		"<tr>"+
	   		"<td>");
	
	
			
			out.write(
			"</td>"+
	     "</tr>"+
		"<tr>"+
	        "<td colspan=2><div style=\"text-align: center;\"><button type=\"submit\" name=\"action\" value=\"save\">Save</button></div>"+
	        "</td>"+
		"</tr>"+
	"</table>");	
}

else if ( "delete".equals(request.getParameter("action")) ) {
		try{
			configuration.deleteGroupToAccountMapping( ConfigurationWebToolkit.parseGroupToAccountMapping(request).getName() );
			out.write(
"<p>Group to account mapping has been successfully saved!</p>");
		}catch(Exception e){
			out.write(
"<p>Error saving group to account mapping: " + e.getMessage() + "</p>");
		}
}

%>

</div>
<%@include file="bottomNav.jspf"%>
</body>
</html>
