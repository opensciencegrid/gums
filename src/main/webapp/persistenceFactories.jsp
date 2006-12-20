<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@page import="gov.bnl.gums.*"%>
<jsp:useBean id="gums" scope="application" class="gov.bnl.gums.admin.GUMSAPIImpl" />
<%@ page import="gov.bnl.gums.persistence.*" %>
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

Configures persistence factories.
</p>

<%

Configuration configuration = gums.getConfiguration();
String message = null;
Collection persistenceFactories = configuration.getPersistenceFactories().values();

if (request.getParameter("action")==null || 
	"save".equals(request.getParameter("action")) || 
	"delete".equals(request.getParameter("action"))) {
	
	if ("save".equals(request.getParameter("action"))) {
		try{
			configuration.getPersistenceFactories().put(request.getParameter("name"), ConfigurationWebToolkit.parsePersistenceFactory(configuration, request));
			gums.setConfiguration(configuration);
			message = "<div class=\"success\">Persistence factory has been saved.</div>";
		}catch(Exception e){
			message = "<div class=\"failure\">Error saving persistence factory: " + e.getMessage() + "</div>";
		}
	}

	if ("delete".equals(request.getParameter("action"))) {
		try{
			String references = ConfigurationWebToolkit.getReferencesForPersistenceFactory(configuration, request.getParameter("name"));
			if( references==null ) {
				if (configuration.getPersistenceFactories().remove( request.getParameter("name") )!=null) {
					gums.setConfiguration(configuration);
					message = "<div class=\"success\">Persistence factory has been deleted.</div>";
				}
				else
					message = "<div class=\"failure\">Error deleting persistence factory</div>";
			}
			else
				message = "<div class=\"failure\">You cannot delete this item until removing references to it within: " + references + "</div>";
		}catch(Exception e){
			message = "<div class=\"failure\">Error deleting persistence factory: " + e.getMessage() + "</div>";
		}
	}

	out.write(
"<table id=\"form\" cellpadding=\"2\" cellspacing=\"2\">");

	if(message!=null)
		out.write( "<tr><td colspan=\"2\">" + message + "</td></tr>" );
				
	Iterator persistenceFactoriesIt = persistenceFactories.iterator();
	while(persistenceFactoriesIt.hasNext()) {
		PersistenceFactory persistenceFactory = persistenceFactoriesIt.hasNext() ? (PersistenceFactory)persistenceFactoriesIt.next() : null;
		
		out.write(
	   	"<tr>"+
			"<td width=\"50\" valign=\"top\">"+
				"<form action=\"persistenceFactories.jsp\" method=\"get\">"+
					"<input type=\"image\" src=\"images/Edit24.gif\" name=\"action\" value=\"edit\">"+
					"<input type=\"image\" src=\"images/Remove24.gif\" name=\"action\" value=\"delete\" onclick=\"if(!confirm('Are you sure you want to delete this persistence factory?'))return false;\">"+
					"<input type=\"hidden\" name=\"name\" value=\"" + persistenceFactory.getName() + "\">"+
				"</form>"+
			"</td>"+
	  		"<td align=\"left\">"+
		   		"<table class=\"configElement\" width=\"100%\">"+
		  			"<tr>"+
			    		"<td>");
			    		
		if (persistenceFactory instanceof HibernatePersistenceFactory) {
			out.write( 		"Use <span style=\"color:blue\">hibernate</span> persistence factory <span style=\"color:blue\">" + persistenceFactory.getName() + "</span> to store user and account mapping information");
			if( ((HibernatePersistenceFactory)persistenceFactory).getProperties().getProperty("hibernate.connection.url")!=null ) {
	    		out.write( 
	    					" at MySQL URL " + "<span style=\"color:blue\">" + 
	    					((HibernatePersistenceFactory)persistenceFactory).getProperties().getProperty("hibernate.connection.url") +
	    					"</span>" );
			}
		}
		else if (persistenceFactory instanceof LDAPPersistenceFactory) {
			out.write( 		"Use <span style=\"color:blue\">LDAP</span> persistence factory <span style=\"color:blue\">" + persistenceFactory.getName() + "</span> to store user and account mapping information");
			if( ((LDAPPersistenceFactory)persistenceFactory).getProperties().getProperty("ldap.java.naming.provider.url")!=null ) {
	    		out.write( 
	    					" at URL " + "<span style=\"color:blue\">" + 
	    					((LDAPPersistenceFactory)persistenceFactory).getProperties().getProperty("ldap.java.naming.provider.url") +
	    					"</span>" );
			}
		}
		else if (persistenceFactory instanceof LocalPersistenceFactory) {
			out.write( 		"Use <span style=\"color:blue\">local</span> persistence factory <span style=\"color:blue\">" + persistenceFactory.getName() + "</span> to store user and account mapping information");
			if( ((LocalPersistenceFactory)persistenceFactory).getMySQLProperties().getProperty("mmysql.hibernate.connection.url")!=null ) {
	    		out.write( 
	    					" at MySQL URL " + "<span style=\"color:blue\">" + 
	    					((LocalPersistenceFactory)persistenceFactory).getProperties().getProperty("mysql.hibernate.connection.url") +
	    					"</span>" );
			}
			if( ((LocalPersistenceFactory)persistenceFactory).getLDAPProperties().getProperty("ldap.java.naming.provider.url")!=null ) {
	    		out.write( 
	    					" at LDAP URL " + "<span style=\"color:blue\">" + 
	    					((LocalPersistenceFactory)persistenceFactory).getProperties().getProperty("ldap.java.naming.provider.url") +
	    					"</span>" );
			}			
		}
		
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
	        	"<form action=\"persistenceFactories.jsp\" method=\"get\">"+
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
	
	PersistenceFactory persistenceFactory = null;
	
	if ("edit".equals(request.getParameter("action"))) {
		try {
			persistenceFactory = (PersistenceFactory)configuration.getPersistenceFactories().get( request.getParameter("name") );
		} catch(Exception e) {
			out.write( "<div class=\"failure\">Error getting persistence factory: " + e.getMessage() + "</div>" );
			return;
		}
	}

	if ("reload".equals(request.getParameter("action"))) {
		try{
			persistenceFactory = ConfigurationWebToolkit.parsePersistenceFactory(configuration, request);
		} catch(Exception e) {
			out.write( "<div class=\"failure\">Error reloading persistence factory: " + e.getMessage() + "</div>" );
			return;
		}
	}
		
	else if ("add".equals(request.getParameter("action"))) {
		persistenceFactory = new HibernatePersistenceFactory();
	}		
		

						
	out.write(
			"<tr>"+
	        "<td colspan=2>"+
				ConfigurationWebToolkit.createDoSubmit(persistenceFactories, request)+
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
