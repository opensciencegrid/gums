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
String message = null;

if (request.getParameter("action")==null || 
	"save".equals(request.getParameter("action")) || 
	"delete".equals(request.getParameter("action"))) {
	
	if ("save".equals(request.getParameter("action"))) {
		try{
			configuration.getGroupToAccountMappings().put(request.getParameter("name"), ConfigurationWebToolkit.parseGroupToAccountMapping(configuration, request));
			gums.setConfiguration(configuration);
			message = "<div class=\"success\">Group to account mapping has been saved.</div>";
		}catch(Exception e){
			message = "<div class=\"failure\">Error saving group to account mapping: " + e.getMessage() + "</div>";
		}
	}

	if ("delete".equals(request.getParameter("action"))) {
		try{
			String getReferencesStr = ConfigurationWebToolkit.getReferencesStr(configuration, request.getParameter("name"));
			if( getReferencesStr==null ) {
				if (configuration.getGroupToAccountMappings().remove( request.getParameter("name") )!=null) {
					gums.setConfiguration(configuration);
					message = "<div class=\"success\">Group to account mapping has been deleted.</div>";
				}
				else
					message = "<div class=\"failure\">Error deleting group to account mapping</div>";
			}
			else
				message = "<div class=\"failure\">You cannot delete this item until removing references to it within host to group mapping(s) that matches against " + getReferencesStr + "</div>";
		}catch(Exception e){
			message = "<div class=\"failure\">Error deleting group to account mapping: " + e.getMessage() + "</div>";
		}
	}

	Collection g2AMappings = configuration.getGroupToAccountMappings().values();
	
	out.write(
"<table id=\"form\" cellpadding=\"2\" cellspacing=\"2\">");

	if(message!=null)
		out.write( "<tr><td colspan=\"2\">" + message + "</td></tr>" );
				
	Iterator g2AMappingsIt = g2AMappings.iterator();
	while(g2AMappingsIt.hasNext()) {
		GroupToAccountMapping g2AMapping = g2AMappingsIt.hasNext() ? (GroupToAccountMapping)g2AMappingsIt.next() : null;
		
		out.write(
	   	"<tr>"+
			"<td width=\"25\" valign=\"top\">"+
				"<form action=\"groupToAccount.jsp\" method=\"get\">"+
					"<input type=\"image\" src=\"images/Edit24.gif\" name=\"action\" value=\"edit\">"+
					"<input type=\"image\" src=\"images/Remove24.gif\" name=\"action\" value=\"delete\" onclick=\"if(!confirm('Are you sure you want to delete this group to account mapping?'))return false;\">"+
					"<input type=\"hidden\" name=\"name\" value=\"" + g2AMapping.getName() + "\">"+
				"</form>"+
			"</td>"+
	  		"<td align=\"left\">"+
		   		"<table class=\"configElement\" width=\"100%\">"+
		  			"<tr>"+
			    		"<td>"+
				    		"For hosts mapped to group "+
				    		"<span style=\"color:blue\">" + g2AMapping.getName() + "</span>, "+
				    		"perform mappings using account mapper" + (g2AMapping.getAccountMappers().size()>1?"s":"") + " ");
			
		Iterator accountMappersIt = g2AMapping.getAccountMappers().iterator();
		while(accountMappersIt.hasNext())
		{
			AccountMapper accountMapper = (AccountMapper)accountMappersIt.next();
			out.write( "<span style=\"color:blue\">"+accountMapper.getName()+"</span>" );
			if( accountMappersIt.hasNext() )
				out.write(", ");
		}
		
		if (g2AMapping.getAccountMappers().size()>1)
			out.write(		" (try in order)");
		
		out.write(			" if member of user group" + (g2AMapping.getUserGroups().size()>1?"s":"") + " ");

		Iterator userGroupsIt = g2AMapping.getUserGroups().iterator();
		while(userGroupsIt.hasNext())
		{
			UserGroup userGroup = (UserGroup)userGroupsIt.next();
			out.write( "<span style=\"color:blue\">"+userGroup.getName()+"</span>" );
			if( userGroupsIt.hasNext() )
				out.write(", ");
		}
		
		if (g2AMapping.getUserGroups().size()>1)
			out.write(		" (try in order)");
		
		out.write(	
						".</td>"+
			      	"</tr>"+
				"</table>"+
			"</td>"+
			"<td width=\"10\"></td>"+		
		"</tr>");
	}

	out.write(
		"<tr>"+
	        "<td colspan=2>"+
	        	"<form action=\"groupToAccount.jsp\" method=\"get\">"+
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
	
	GroupToAccountMapping g2AMapping = null;
	
	if ("edit".equals(request.getParameter("action"))) {
		try {
			g2AMapping = (GroupToAccountMapping)configuration.getGroupToAccountMappings().get( request.getParameter("name") );
		} catch(Exception e) {
			out.write( "<div class=\"failure\">Error getting group to account mapping: " + e.getMessage() + "</div>" );
			return;
		}
	}

	if ("reload".equals(request.getParameter("action"))) {
		try{
			g2AMapping = ConfigurationWebToolkit.parseGroupToAccountMapping(configuration, request);
		} catch(Exception e) {
			out.write( "<div class=\"failure\">Error reloading group to account mapping: " + e.getMessage() + "</div>" );
			return;
		}
	}
		
	else if ("add".equals(request.getParameter("action"))) {
		g2AMapping = new GroupToAccountMapping();
	}		
		
	out.write(
"<form action=\"groupToAccount.jsp\" method=\"get\">"+
	"<input type=\"hidden\" name=\"action\" value=\"\">"+
	"<input type=\"hidden\" name=\"originalAction\" value=\""+ 
		("reload".equals(request.getParameter("action")) ? request.getParameter("originalAction") : request.getParameter("action")) +
	"\">"+
	"<table id=\"form\" border=\"0\" cellpadding=\"2\" cellspacing=\"2\" align=\"center\">"+
		"<tr>"+
    		"<td nowrap width=\"1px\">"+
	    		"For hosts mapped to group "+
		    "</td>"+
		    "<td nowrap>");

	if ("add".equals(request.getParameter("action")) || "add".equals(request.getParameter("originalAction")))
		out.write(
		    	"<input maxlength=\"256\" size=\"64\" name=\"name\" value=\"" + g2AMapping.getName() + "\"/>");
	else
		out.write(
		    	g2AMapping.getName()+
		    	"<input type=\"hidden\" name=\"name\" value=\"" + g2AMapping.getName() + "\"/>");	

	out.write(
		    "</td>"+
		"</tr>"+
		"<tr>"+
			"<td nowrap>perform mappings using account<br>mapper(s) (try in order)</td>"+
			"<td>");
	
	// Create multiple group to account mappings
	int counter = 0;
	if (g2AMapping!=null) {
		Collection accountMappers = g2AMapping.getAccountMappers();
		Iterator accountMappersIt = accountMappers.iterator();
		while(accountMappersIt.hasNext())
		{
			AccountMapper accountMapper = (AccountMapper)accountMappersIt.next();
			out.write( 
				ConfigurationWebToolkit.createSelectBox("aM"+counter, 
					configuration.getAccountMappers().values(), 
					accountMapper.getName(),
					"onchange=\"document.forms[0].elements['action'].value='reload';document.forms[0].submit();\"") );
			counter++;
		}
	}
	out.write( 
		ConfigurationWebToolkit.createSelectBox("aM"+counter, 
			configuration.getAccountMappers().values(), 
			null,
			"onchange=\"document.forms[0].elements['action'].value='reload';document.forms[0].submit();\"") );

	out.write(
		    "</td>"+
		"</tr>"+
		"<tr>"+
			"<td nowrap>if member of user group<br> (try in order)</td>"+
			"<td>");

	// Create multiple user groups
	counter = 0;
	if (g2AMapping!=null) {
		Collection userGroups = g2AMapping.getUserGroups();
		Iterator userGroupsIt = userGroups.iterator();
		while(userGroupsIt.hasNext())
		{
			UserGroup userGroup = (UserGroup)userGroupsIt.next();
			out.write( 
				ConfigurationWebToolkit.createSelectBox("uG"+counter, 
					configuration.getUserGroups().values(), 
					userGroup.getName(),
					"onchange=\"document.forms[0].elements['action'].value='reload';document.forms[0].submit();\"") );
			counter++;
		}
	}
	out.write( 
		ConfigurationWebToolkit.createSelectBox("uG"+counter, 
			configuration.getUserGroups().values(), 
			null,
			"onchange=\"document.forms[0].elements['action'].value='reload';document.forms[0].submit();\"") );
	
	out.write(
			"</td>"+
			"<td width=\"25\"></td>"+
		"</tr>"+
		"<tr>"+
	        "<td colspan=2>"+
	        	"<div style=\"text-align: center;\">"+
	        		"<button type=\"submit\" onclick=\"document.forms[0].elements['action'].value='save';document.forms[0].submit();\">Save</button>"+
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
