<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@page import="gov.bnl.gums.*"%>
<jsp:useBean id="gums" scope="application" class="gov.bnl.gums.admin.GUMSAPIImpl" />
<%@ page import="gov.bnl.gums.account.*" %>
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

Configures virtual organizations.
</p>

<%

Configuration configuration = gums.getConfiguration();
String message = null;
Collection virtualOrganizations = configuration.getVirtualOrganizations().values();

if (request.getParameter("action")==null || 
	"save".equals(request.getParameter("action")) || 
	"delete".equals(request.getParameter("action"))) {
	
	if ("save".equals(request.getParameter("action"))) {
		try{
			configuration.getVirtualOrganizations().put(request.getParameter("name"), ConfigurationWebToolkit.parseVirtualOrganization(configuration, request));
			gums.setConfiguration(configuration);
			message = "<div class=\"success\">Virtual organization has been saved.</div>";
		}catch(Exception e){
			message = "<div class=\"failure\">Error saving virtual organization: " + e.getMessage() + "</div>";
		}
	}

	if ("delete".equals(request.getParameter("action"))) {
		try{
			String references = ConfigurationWebToolkit.getVOMSUserGroupReferences(configuration, request.getParameter("name"));
			if( references==null ) {
				if (configuration.getVirtualOrganizations().remove( request.getParameter("name") )!=null) {
					gums.setConfiguration(configuration);
					message = "<div class=\"success\">Virtual organization has been deleted.</div>";
				}
				else
					message = "<div class=\"failure\">Error deleting virtual organization</div>";
			}
			else
				message = "<div class=\"failure\">You cannot delete this item until removing references to it within group to account mapping(s): " + references + "</div>";
		}catch(Exception e){
			message = "<div class=\"failure\">Error deleting virtual organization: " + e.getMessage() + "</div>";
		}
	}

	out.write(
"<table id=\"form\" cellpadding=\"2\" cellspacing=\"2\">");

	if(message!=null)
		out.write( "<tr><td colspan=\"2\">" + message + "</td></tr>" );
				
	Iterator virtualOrganizationsIt = virtualOrganizations.iterator();
	while(virtualOrganizationsIt.hasNext()) {
		VirtualOrganization virtualOrganization = virtualOrganizationsIt.hasNext() ? (VirtualOrganization)virtualOrganizationsIt.next() : null;
		
		out.write(
	   	"<tr>"+
			"<td width=\"50\" valign=\"top\">"+
				"<form action=\"virtualOrganizations.jsp\" method=\"get\">"+
					"<input type=\"image\" src=\"images/Edit24.gif\" name=\"action\" value=\"edit\">"+
					"<input type=\"image\" src=\"images/Remove24.gif\" name=\"action\" value=\"delete\" onclick=\"if(!confirm('Are you sure you want to delete this virtual organization?'))return false;\">"+
					"<input type=\"hidden\" name=\"name\" value=\"" + virtualOrganization.getName() + "\">"+
				"</form>"+
			"</td>"+
	  		"<td align=\"left\">"+
		   		"<table class=\"configElement\" width=\"100%\">"+
		  			"<tr>"+
			    		"<td>"+
				    		"Check if member of virtual organization"+
				    		" <span style=\"color:blue\">" + virtualOrganization.getName() + "</span>");
				    		
		out.write(		" by querying VOMS server at base URL " + "<span style=\"color:blue\">" + virtualOrganization.getBaseUrl() + "</span> (additional path specified in user group)");

		if ( !virtualOrganization.getSslKey().equals("") ) {
			out.write(	" SSL key <span style=\"color:blue\">" + virtualOrganization.getSslKey() + "</span>");
			if ( !virtualOrganization.getSslCertfile().equals("") && !virtualOrganization.getSslCAFiles().equals("") )
				out.write(	
						"," );
			else if ( !virtualOrganization.getSslCertfile().equals("") || !virtualOrganization.getSslCAFiles().equals("") )
				out.write(	
						" and" );
		}

		if ( !virtualOrganization.getSslCertfile().equals("") ) {
			out.write(	" SSL certificate file <span style=\"color:blue\">" + virtualOrganization.getSslCertfile() + "</span>");
			if ( !virtualOrganization.getSslCAFiles().equals("") )
				out.write(	
						" and" );
		}
						
		if ( !virtualOrganization.getSslCAFiles().equals("") )
			out.write(	" SSL CA files <span style=\"color:blue\">" + virtualOrganization.getSslCAFiles() + "</span>");

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
	        	"<form action=\"virtualOrganizations.jsp\" method=\"get\">"+
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
	
	VirtualOrganization virtualOrganization = null;
	
	if ("edit".equals(request.getParameter("action"))) {
		try {
			virtualOrganization = (VirtualOrganization)configuration.getVirtualOrganizations().get( request.getParameter("name") );
		} catch(Exception e) {
			out.write( "<div class=\"failure\">Error getting virtual organization: " + e.getMessage() + "</div>" );
			return;
		}
	}

	if ("reload".equals(request.getParameter("action"))) {
		try{
			virtualOrganization = ConfigurationWebToolkit.parseVirtualOrganization(configuration, request);
		} catch(Exception e) {
			out.write( "<div class=\"failure\">Error reloading virtual organization: " + e.getMessage() + "</div>" );
			return;
		}
	}
		
	else if ("add".equals(request.getParameter("action"))) {
		virtualOrganization = new VirtualOrganization();
	}		
		
	out.write(
"<form action=\"virtualOrganizations.jsp\" method=\"get\">"+
	"<input type=\"hidden\" name=\"action\" value=\"\">"+
	"<input type=\"hidden\" name=\"originalAction\" value=\""+ 
		("reload".equals(request.getParameter("action")) ? request.getParameter("originalAction") : request.getParameter("action")) +
	"\">"+
	"<table id=\"form\" border=\"0\" cellpadding=\"2\" cellspacing=\"2\" align=\"center\">"+
		"<tr>"+
    		"<td nowrap style=\"text-align: right;\">"+
	    		"Check if member of virtual organization "+
		    "</td>"+
		    "<td nowrap>");

	if ("add".equals(request.getParameter("action")) || "add".equals(request.getParameter("originalAction")))
		out.write(
		    	"<input maxlength=\"256\" size=\"32\" name=\"name\" value=\"" + (virtualOrganization.getName()!=null ? virtualOrganization.getName() : "") + "\"/>" +
		    "</td>" +
		"</tr>"+
		"<tr>"+
			"<td nowrap style=\"text-align: right;\">"+
	    		"i.e."+
		    "</td>"+
		    "<td nowrap>"+
				"myVirtualOrganization"+
		    "</td>"+
		"</tr>");
	else
		out.write(
		    	virtualOrganization.getName()+
		    	"<input type=\"hidden\" name=\"name\" value=\"" + virtualOrganization.getName() + "\"/>" +
		    "</td>" +
		"</tr>");	
		    	
	out.write(	
		"<tr>"+
			"<td nowrap style=\"text-align: right;\">"+
				"by querying VOMS server at base URL"+
			"</td>"+
			"<td>"+ 
				"<input maxlength=\"256\" size=\"32\" name=\"baseURL\" value=\"" + virtualOrganization.getBaseUrl() + "\"/>(additional path in user group)"+
			"</td>"+
		"</tr>"+
	    "<tr>"+
    		"<td nowrap style=\"text-align: right;\">"+
	    		"i.e."+
		    "</td>"+
		    "<td nowrap>"+
		    	"https://lcg-voms.cern.ch:8443/voms"+
		    "</td>"+
		"</tr>"+	
		"<tr>"+
			"<td nowrap style=\"text-align: right;\">"+
				"using SSL key"+
			"</td>"+
			"<td>"+ 
				"<input maxlength=\"256\" size=\"32\" name=\"sslKey\" value=\"" + virtualOrganization.getSslKey() + "\"/> ,"+
			"</td>"+
		"</tr>"+
	    "<tr>"+
    		"<td nowrap style=\"text-align: right;\">"+
	    		"i.e."+
		    "</td>"+
		    "<td nowrap>"+
		    	"/etc/grid-security/gumskey.pem"+
		    "</td>"+
		"</tr>"+	
		"<tr>"+
			"<td nowrap style=\"text-align: right;\">"+
				"SSL cert file"+
			"</td>"+
			"<td>"+ 
				"<input maxlength=\"256\" size=\"32\" name=\"sslCert\" value=\"" + virtualOrganization.getSslCertfile() + "\"/> ,"+
			"</td>"+
		"</tr>"+
	    "<tr>"+
    		"<td nowrap style=\"text-align: right;\">"+
	    		"i.e."+
		    "</td>"+
		    "<td nowrap>"+
		    	"/etc/grid-security/gumscert.pem"+
		    "</td>"+
		"</tr>"+
		"<tr>"+
			"<td nowrap style=\"text-align: right;\">"+
				"SSL CA files (optional)"+
			"</td>"+
			"<td>"+ 
				"<input maxlength=\"256\" size=\"32\" name=\"sslCA\" value=\"" + virtualOrganization.getSslCAFiles() + "\"/> ,"+
			"</td>"+
		"</tr>"+
	    "<tr>"+
    		"<td nowrap style=\"text-align: right;\">"+
	    		"i.e."+
		    "</td>"+
		    "<td nowrap>"+
		    	"/etc/grid-security/certificates"+
		    "</td>"+
		"</tr>"+
		"<tr>"+
			"<td nowrap style=\"text-align: right;\">"+
				"SSL key password (optional)"+
			"</td>"+
			"<td>"+ 
				"<input type=\"password\" maxlength=\"256\" size=\"32\" name=\"sslKeyPW\" value=\"" + virtualOrganization.getSslKeyPasswd() + "\"/>"+
			"</td>"+
		"</tr>");
						
	out.write(
			"<tr>"+
	        "<td colspan=2>"+
				ConfigurationWebToolkit.createDoSubmit(virtualOrganizations, request)+
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
