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

Configures user groups.
</p>

<%

Configuration configuration = gums.getConfiguration();
String message = null;
Collection userGroups = configuration.getUserGroups().values();

if (request.getParameter("action")==null || 
	"save".equals(request.getParameter("action")) || 
	"delete".equals(request.getParameter("action"))) {
	
	if ("save".equals(request.getParameter("action"))) {
		try{
			configuration.getUserGroups().put(request.getParameter("name"), ConfigurationWebToolkit.parseUserGroup(configuration, request));
			gums.setConfiguration(configuration);
			message = "<div class=\"success\">User group has been saved.</div>";
		}catch(Exception e){
			message = "<div class=\"failure\">Error saving user group: " + e.getMessage() + "</div>";
		}
	}

	if ("delete".equals(request.getParameter("action"))) {
		try{
			String references = ConfigurationWebToolkit.getGroupToAccountMappingReferences(configuration, request.getParameter("name"), "gov.bnl.gums.userGroup.UserGroup");
			if( references==null ) {
				if (configuration.getUserGroups().remove( request.getParameter("name") )!=null) {
					gums.setConfiguration(configuration);
					message = "<div class=\"success\">User group has been deleted.</div>";
				}
				else
					message = "<div class=\"failure\">Error deleting user group</div>";
			}
			else
				message = "<div class=\"failure\">You cannot delete this item until removing references to it within group to account mapping(s): " + references + "</div>";
		}catch(Exception e){
			message = "<div class=\"failure\">Error deleting user group: " + e.getMessage() + "</div>";
		}
	}

	out.write(
"<table id=\"form\" cellpadding=\"2\" cellspacing=\"2\">");

	if(message!=null)
		out.write( "<tr><td colspan=\"2\">" + message + "</td></tr>" );
				
	Iterator userGroupsIt = userGroups.iterator();
	while(userGroupsIt.hasNext()) {
		UserGroup userGroup = userGroupsIt.hasNext() ? (UserGroup)userGroupsIt.next() : null;
		
		out.write(
	   	"<tr>"+
			"<td width=\"50\" valign=\"top\">"+
				"<form action=\"userGroups.jsp\" method=\"get\">"+
					"<input type=\"image\" src=\"images/Edit24.gif\" name=\"action\" value=\"edit\">"+
					"<input type=\"image\" src=\"images/Remove24.gif\" name=\"action\" value=\"delete\" onclick=\"if(!confirm('Are you sure you want to delete this user group?'))return false;\">"+
					"<input type=\"hidden\" name=\"name\" value=\"" + userGroup.getName() + "\">"+
				"</form>"+
			"</td>"+
	  		"<td align=\"left\">"+
		   		"<table class=\"configElement\" width=\"100%\">"+
		  			"<tr>"+
			    		"<td>"+
				    		"Check if member of user group"+
				    		" <span style=\"color:blue\">" + userGroup.getName() + "</span>");
				    		
		if (userGroup instanceof ManualUserGroup) {
			out.write(		" by searching within this group in persistence factory" + 
							" <span style=\"color:blue\">" + ((ManualUserGroup)userGroup).getPersistenceFactory() + "</span>");
		} else if (userGroup instanceof LDAPUserGroup) {
			out.write(		" by querying LDAP server"+ 
							" <span style=\"color:blue\">" + ((LDAPUserGroup)userGroup).getServer() + "</span>"+
							" with query "+ 
							" <span style=\"color:blue\">" + ((LDAPUserGroup)userGroup).getQuery() + "</span>"+
							" and caching results in persistence factory "+ 
							" <span style=\"color:blue\">" + ((LDAPUserGroup)userGroup).getPersistenceFactory() + "</span>");
		} else if (userGroup instanceof VOMSUserGroup) {
			out.write(		" by querying virtual organization " + "<span style=\"color:blue\">" + ((VOMSUserGroup)userGroup).getVirtualOrganization() + "</span>");

			if ( !((VOMSUserGroup)userGroup).getRemainderUrl().equals("") )
				out.write(	" at URL <span style=\"font-style:italic\">{base URL}</span><span style=\"color:blue\">" + ((VOMSUserGroup)userGroup).getRemainderUrl() + "</span>");

			if ( ((VOMSUserGroup)userGroup).getVoGroup().equals("") )
				out.write(	" where non-VOMS certificates are " + (((VOMSUserGroup)userGroup).isAcceptProxyWithoutFQAN() ? "<span style=\"color:blue\">" : "<span style=\"color:blue\">not") + "accepted</span>");
							
			if ( !((VOMSUserGroup)userGroup).getVoGroup().equals("") )
				out.write(	" where certificate matches group" + 
							" <span style=\"color:blue\">" + ((VOMSUserGroup)userGroup).getVoGroup() + "</span>");
	
			if ( !((VOMSUserGroup)userGroup).getVoRole().equals("") )
				out.write(	" and role" + 
							" <span style=\"color:blue\">" + ((VOMSUserGroup)userGroup).getVoRole() + "</span>");
		}

		out.write(			". Members have <span style=\"color:blue\">" + userGroup.getAccess() + "</span> access to GUMS.");
		
		out.write(	
						"</td>"+
			      	"</tr>"+
				"</table>"+
			"</td>"+
			"<td width=\"10\"></td>"+		
		"</tr>");
	}

	out.write(
		"<tr>"+
	        "<td colspan=2>"+
	        	"<form action=\"userGroups.jsp\" method=\"get\">"+
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
	
	UserGroup userGroup = null;
	
	ArrayList userGroupClasses = new ArrayList();
	userGroupClasses.add("gov.bnl.gums.userGroup.LDAPUserGroup");
	userGroupClasses.add("gov.bnl.gums.userGroup.ManualUserGroup");
	userGroupClasses.add("gov.bnl.gums.userGroup.VOMSUserGroup");
	
	if ("edit".equals(request.getParameter("action"))) {
		try {
			userGroup = (UserGroup)configuration.getUserGroups().get( request.getParameter("name") );
		} catch(Exception e) {
			out.write( "<div class=\"failure\">Error getting user group: " + e.getMessage() + "</div>" );
			return;
		}
	}

	if ("reload".equals(request.getParameter("action"))) {
		try{
			userGroup = ConfigurationWebToolkit.parseUserGroup(configuration, request);
		} catch(Exception e) {
			out.write( "<div class=\"failure\">Error reloading user group: " + e.getMessage() + "</div>" );
			return;
		}
	}
		
	else if ("add".equals(request.getParameter("action"))) {
		userGroup = new VOMSUserGroup();
	}		
		
	out.write(
"<form action=\"userGroups.jsp\" method=\"get\">"+
	"<input type=\"hidden\" name=\"action\" value=\"\">"+
	"<input type=\"hidden\" name=\"originalAction\" value=\""+ 
		("reload".equals(request.getParameter("action")) ? request.getParameter("originalAction") : request.getParameter("action")) +
	"\">"+
	"<table id=\"form\" border=\"0\" cellpadding=\"2\" cellspacing=\"2\" align=\"center\">"+
		"<tr>"+
    		"<td nowrap style=\"text-align: right;\">"+
	    		"Check if member of user group"+
		    "</td>"+
		    "<td nowrap>");

	if ("add".equals(request.getParameter("action")) || "add".equals(request.getParameter("originalAction")))
		out.write(
		    	"<input maxlength=\"256\" size=\"32\" name=\"name\" value=\"" + (userGroup.getName()!=null ? userGroup.getName() : "") + "\"/>");
	else
		out.write(
		    	userGroup.getName()+
		    	"<input type=\"hidden\" name=\"name\" value=\"" + userGroup.getName() + "\"/>");	

	out.write(
		    "</td>"+
		"</tr>"+
		"<tr>"+
    		"<td nowrap style=\"text-align: right;\">"+
	    		"of class "+
		    "</td>"+
		    "<td nowrap>"+
			ConfigurationWebToolkit.createSelectBox("className", 
				userGroupClasses, 
				userGroup.getClass().toString().substring(6),
				"onchange=\"document.forms[0].elements['action'].value='reload';document.forms[0].submit();\"",
				false)+
		    "</td>"+
		"</tr>");

	if (userGroup instanceof ManualUserGroup) {
		out.write(	
		"<tr>"+
			"<td nowrap style=\"text-align: right;\">"+
				"by searching within this group in persistence factory "+
			"</td>"+
			"<td>"+ 
				ConfigurationWebToolkit.createSelectBox("persistenceFactory", 
						configuration.getPersistenceFactories().values(), 
						((ManualUserGroup)userGroup).getPersistenceFactory(),
						null,
						true)+
				".</td>"+
		"</tr>");
	} else if (userGroup instanceof LDAPUserGroup) {
		out.write(	
		"<tr>"+
			"<td nowrap style=\"text-align: right;\">"+
				"by querying LDAP server"+
			"</td>"+
			"<td>"+ 
				"<input maxlength=\"256\" size=\"32\" name=\"server\" value=\"" + ((LDAPUserGroup)userGroup).getServer() + "\"/>"+
			"</td>"+
		"</tr>"+
		"<tr>"+
			"<td nowrap style=\"text-align: right;\">"+
				"with query"+
			"</td>"+
			"<td>"+ 
				"<input maxlength=\"256\" size=\"32\" name=\"query\" value=\"" + ((LDAPUserGroup)userGroup).getQuery() + "\"/>"+
			"</td>"+
		"</tr>"+
		"<tr>"+
			"<td nowrap style=\"text-align: right;\">"+
				"and caching results in persistence factory"+
			"</td>"+
			"<td>"+ 
				ConfigurationWebToolkit.createSelectBox("persistenceFactory", 
						configuration.getPersistenceFactories().values(), 
						((LDAPUserGroup)userGroup).getPersistenceFactory(),
						null,
						true)+
			".</td>"+
		"</tr>");
	} else if (userGroup instanceof VOMSUserGroup) {
		out.write(	
			"<tr>"+
				"<td nowrap style=\"text-align: right;\">"+
					"by querying virtual organization"+
				"</td>"+
				"<td>"+ 
					ConfigurationWebToolkit.createSelectBox("vo", 
							configuration.getVirtualOrganizations().values(), 
							((VOMSUserGroup)userGroup).getVirtualOrganization(),
							null,
							true)+
				"</td>"+
			"</tr>"+		
			"<tr>"+
				"<td nowrap style=\"text-align: right;\">"+
					"at URL "+
				"</td>"+
				"<td>"+ 
					"{base URL}<input maxlength=\"256\" size=\"32\" name=\"url\" value=\"" + ((VOMSUserGroup)userGroup).getRemainderUrl() + "\"/>"+
				"</td>"+
			"</tr>"+
			"<tr>"+
				"<td nowrap style=\"text-align: right;\">"+
					"where non-VOMS certificates are"+
				"</td>"+
				"<td>"+ 
					"<select name=\"nVOMS\" onchange=\"document.forms[0].elements['action'].value='reload';document.forms[0].submit();\">"+
						"<option " + (((VOMSUserGroup)userGroup).isAcceptProxyWithoutFQAN()?"selected":"") + ">allowed</option>"+
						"<option " + (((VOMSUserGroup)userGroup).isAcceptProxyWithoutFQAN()?"":"selected") + ">not allowed</option>"+
					"</select>" + (((VOMSUserGroup)userGroup).isAcceptProxyWithoutFQAN() ? " ." : "") +
				"</td>"+
			"</tr>");
			
			if( !((VOMSUserGroup)userGroup).isAcceptProxyWithoutFQAN() ) {
				out.write(
			"<tr>"+
				"<td nowrap style=\"text-align: right;\">"+
					"where certificate matches group (optional)"+
				"</td>"+
				"<td>"+ 
					"<input name=\"group\" value=\"" + ((VOMSUserGroup)userGroup).getVoGroup() + "\"/>"+
				"</td>"+
			"</tr>"+
			"<tr>"+
				"<td nowrap style=\"text-align: right;\">"+
					"and role (optional)"+
				"</td>"+
				"<td>"+ 
					"<input name=\"role\" value=\"" + ((VOMSUserGroup)userGroup).getVoRole() + "\"/>."+
				"</td>"+
			"</tr>");
			}
	}
			
	out.write(
			"<tr>"+
				"<td nowrap style=\"text-align: right;\">"+
					"Members have "+
				"</td>"+
				"<td>"+ 
					"<select name=\"access\">"+
						"<option " + (userGroup.getAccess().equals("read self")?"selected":"") + ">read self</option>"+
						"<option " + (userGroup.getAccess().equals("read all")?"selected":"") + ">read all</option>"+
						"<option " + (userGroup.getAccess().equals("write")?"selected":"") + ">write</option>"+
					"</select> access."+
				"</td>"+
			"</tr>"+
			"<tr>"+
	        "<td colspan=2>"+
				ConfigurationWebToolkit.createDoSubmit(userGroups, request)+
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
