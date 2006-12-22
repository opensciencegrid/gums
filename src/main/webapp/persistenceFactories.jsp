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
				message = "<div class=\"failure\">You cannot delete this item until removing references to it within " + references + ".</div>";
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
			out.write(	"Store user information in hibernate persistence factory <span style=\"color:blue\">" + persistenceFactory.getName() + "</span>"+
						" at MySQL URL " + "<span style=\"color:blue\">" + 
    					((HibernatePersistenceFactory)persistenceFactory).getProperties().getProperty("hibernate.connection.url") +
    					"</span>" );
		}
		else if (persistenceFactory instanceof LDAPPersistenceFactory) {
			out.write(	"Store user information in LDAP persistence factory <span style=\"color:blue\">" + persistenceFactory.getName() + "</span>"+
						" at LDAP URL " + "<span style=\"color:blue\">" + 
    					((LDAPPersistenceFactory)persistenceFactory).getProperties().getProperty("java.naming.provider.url") +
    					"</span>." +
    					(((LDAPPersistenceFactory)persistenceFactory).isSynchGroups()?" Update GID":" Do not update GID") + " with every access.");
		}
		else if (persistenceFactory instanceof LocalPersistenceFactory) {
			out.write( 	"Store user information in local persistence factory <span style=\"color:blue\">" + persistenceFactory.getName() + "</span>"+
						" at MySQL URL " + "<span style=\"color:blue\">"+ 
    					((LocalPersistenceFactory)persistenceFactory).getMySQLProperties().getProperty("hibernate.connection.url")+
    					"</span>"+
    					" and LDAP URL " + "<span style=\"color:blue\">"+ 
    					((LocalPersistenceFactory)persistenceFactory).getLDAPProperties().getProperty("java.naming.provider.url")+
    					"</span>."+
    					(((LocalPersistenceFactory)persistenceFactory).isSynchGroups()?" Update GID":" Do not update GID") + " with every access.");
		}
		
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

	ArrayList persistenceFactoryClasses = new ArrayList();
	persistenceFactoryClasses.add("gov.bnl.gums.persistence.HibernatePersistenceFactory");
	persistenceFactoryClasses.add("gov.bnl.gums.persistence.LDAPPersistenceFactory");
	persistenceFactoryClasses.add("gov.bnl.gums.persistence.LocalPersistenceFactory");
	
	ArrayList authenticationTypes = new ArrayList();
	authenticationTypes.add("simple");
	authenticationTypes.add("none");
	
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
		((HibernatePersistenceFactory)persistenceFactory).setProperties( ConfigurationWebToolkit.getHibernateProperties(persistenceFactory, request, false) );
	}		
			
	out.write(	
"<form action=\"persistenceFactories.jsp\" method=\"get\">"+
	"<input type=\"hidden\" name=\"action\" value=\"\">"+
	"<input type=\"hidden\" name=\"originalAction\" value=\""+ 
		("reload".equals(request.getParameter("action")) ? request.getParameter("originalAction") : request.getParameter("action")) +
	"\">"+
	"<table id=\"form\" border=\"0\" cellpadding=\"2\" cellspacing=\"2\" align=\"center\">"+
		"<tr>"+
    		"<td nowrap style=\"text-align: right;\">"+
	    		"Store user information in persistence factory "+
		    "</td>"+
		    "<td nowrap>");

	if ("add".equals(request.getParameter("action")) || "add".equals(request.getParameter("originalAction")))
		out.write(
		    	"<input maxlength=\"256\" size=\"32\" name=\"name\" value=\"" + (persistenceFactory.getName()!=null ? persistenceFactory.getName() : "") + "\"/>");
	else
		out.write(
		    	persistenceFactory.getName()+
		    	"<input type=\"hidden\" name=\"name\" value=\"" + persistenceFactory.getName() + "\"/>");	
		    	
	out.write(
		"<tr>"+
			"<td nowrap style=\"text-align: right;\">"+
	    		"of class"+
		    "</td>"+
		    "<td nowrap>"+
			ConfigurationWebToolkit.createSelectBox("className", 
				persistenceFactoryClasses, 
				persistenceFactory.getClass().toString().substring(6),
				"onchange=\"document.forms[0].elements['action'].value='reload';document.forms[0].submit();\"",
				false)+
		    "</td>"+
		"</tr>");
	
	if (persistenceFactory instanceof HibernatePersistenceFactory) {
		out.write( 
	    "<tr>"+
    		"<td nowrap style=\"text-align: right;\">"+
	    		"at MySQL URL"+
		    "</td>"+
		    "<td nowrap>"+
		    	"<input maxlength=\"256\" size=\"64\" name=\"mySqlUrl\" value=\"" + ((HibernatePersistenceFactory)persistenceFactory).getProperties().getProperty("hibernate.connection.url") + "\"/>"+
		    "</td>"+
		"</tr>"+
		"<tr>"+
    		"<td nowrap style=\"text-align: right;\">"+
	    		"with MySQL username"+
		    "</td>"+
		    "<td nowrap>"+
		    	"<input maxlength=\"256\" size=\"32\" name=\"mySqlUsername\" value=\"" + ((HibernatePersistenceFactory)persistenceFactory).getProperties().getProperty("hibernate.connection.username") + "\"/>"+
		    "</td>"+
		"</tr>"+
		"<tr>"+
    		"<td nowrap style=\"text-align: right;\">"+
	    		"and MySQL password"+
		    "</td>"+
		    "<td nowrap>"+
		    	"<input type=\"password\" maxlength=\"256\" size=\"32\" name=\"mySqlPassword\" value=\"" + ((HibernatePersistenceFactory)persistenceFactory).getProperties().getProperty("hibernate.connection.password") + "\"/>"+
		    "</td>"+
		"</tr>");
	}
	else if (persistenceFactory instanceof LDAPPersistenceFactory) {
		out.write( 
		"<tr>"+
    		"<td nowrap style=\"text-align: right;\">"+
	    		"at LDAP URL"+
		    "</td>"+
		    "<td nowrap>"+
		    	"<input maxlength=\"256\" size=\"64\" name=\"ldapUrl\" value=\"" + ((LDAPPersistenceFactory)persistenceFactory).getProperties().getProperty("java.naming.provider.url") + "\"/>"+
		    "</td>"+
		"</tr>"+
		"<tr>"+
    		"<td nowrap style=\"text-align: right;\">"+
	    		"with authentication type "+
		    "</td>"+
		    "<td nowrap>"+
				ConfigurationWebToolkit.createSelectBox("ldapAuthentication", 
					authenticationTypes, 
					((LDAPPersistenceFactory)persistenceFactory).getProperties().getProperty("java.naming.security.authentication"),
					"onchange=\"document.forms[0].elements['action'].value='reload';document.forms[0].submit();\"",
					false)+
					(((LDAPPersistenceFactory)persistenceFactory).getProperties().getProperty("java.naming.security.authentication").equals("simple") ? "" : ".")+
		    "</td>"+
		"</tr>");
		
		if (((LDAPPersistenceFactory)persistenceFactory).getProperties().getProperty("java.naming.security.authentication").equals("simple")) {
			out.write(
		"<tr>"+
    		"<td nowrap style=\"text-align: right;\">"+
	    		"and principle "+
		    "</td>"+
		    "<td nowrap>"+
		    	"<input maxlength=\"256\" size=\"64\" name=\"ldapPrincipal\" value=\"" + ((LDAPPersistenceFactory)persistenceFactory).getProperties().getProperty("java.naming.security.principal") + "\"/>"+
		    "</td>"+
		"</tr>"+
		"<tr>"+
    		"<td nowrap style=\"text-align: right;\">"+
	    		"and credentials "+
		    "</td>"+
		    "<td nowrap>"+
		    	"<input type=\"password\" maxlength=\"256\" size=\"32\" name=\"ldapCredentials\" value=\"" + ((LDAPPersistenceFactory)persistenceFactory).getProperties().getProperty("java.naming.security.credentials") + "\"/> ."+
		    "</td>"+
		"</tr>");
		}
		
		out.write(
		"<tr>"+
    		"<td nowrap style=\"text-align: right;\">"+
	    		"Update GID for every access? "+
		    "</td>"+
		    "<td nowrap>"+
				"<select name=\"synchGroups\"><option "+
				(((LDAPPersistenceFactory)persistenceFactory).isSynchGroups()?"selected":"")+
				">true</option><option "+
				(((LDAPPersistenceFactory)persistenceFactory).isSynchGroups()?"":"selected")+
				">false</option></select>"+
		    "</td>"+
		"</tr>"
		);
	}
	else if (persistenceFactory instanceof LocalPersistenceFactory) {
		out.write( 
		"<tr>"+
    		"<td nowrap style=\"text-align: right;\">"+
	    		"at MySQL URL "+
		    "</td>"+
		    "<td nowrap>"+
		    	"<input maxlength=\"256\" size=\"64\" name=\"mySqlUrl\" value=\"" + ((LocalPersistenceFactory)persistenceFactory).getMySQLProperties().getProperty("hibernate.connection.url") + "\"/>"+
		    "</td>"+
		"</tr>"+
		"<tr>"+
    		"<td nowrap style=\"text-align: right;\">"+
	    		"with username "+
		    "</td>"+
		    "<td nowrap>"+
		    	"<input maxlength=\"256\" size=\"32\" name=\"mySqlUsername\" value=\"" + ((LocalPersistenceFactory)persistenceFactory).getMySQLProperties().getProperty("hibernate.connection.username") + "\"/>"+
		    "</td>"+
		"</tr>"+
		"<tr>"+
    		"<td nowrap style=\"text-align: right;\">"+
	    		"and password "+
		    "</td>"+
		    "<td nowrap>"+
		    	"<input type=\"password\" maxlength=\"256\" size=\"32\" name=\"mySqlPassword\" value=\"" + ((LocalPersistenceFactory)persistenceFactory).getMySQLProperties().getProperty("hibernate.connection.password") + "\"/>"+
		    "</td>"+
		"</tr>"+
		"<tr>"+
    		"<td nowrap style=\"text-align: right;\">"+
	    		"and at LDAP URL "+
		    "</td>"+
		    "<td nowrap>"+
		    	"<input maxlength=\"256\" size=\"64\" name=\"ldapUrl\" value=\"" + ((LocalPersistenceFactory)persistenceFactory).getLDAPProperties().getProperty("java.naming.provider.url") + "\"/>"+
		    "</td>"+
		"</tr>"+
		"<tr>"+
    		"<td nowrap style=\"text-align: right;\">"+
	    		"with authentication type "+
		    "</td>"+
		    "<td nowrap>"+
				ConfigurationWebToolkit.createSelectBox("ldapAuthentication", 
					authenticationTypes, 
					((LocalPersistenceFactory)persistenceFactory).getLDAPProperties().getProperty("java.naming.security.authentication"),
					"onchange=\"document.forms[0].elements['action'].value='reload';document.forms[0].submit();\"",
					false)+
					(((LocalPersistenceFactory)persistenceFactory).getLDAPProperties().getProperty("java.naming.security.authentication").equals("simple") ? "" : ".")+
		    "</td>"+
		"</tr>");
		
		if (((LocalPersistenceFactory)persistenceFactory).getLDAPProperties().getProperty("java.naming.security.authentication").equals("simple")) {
			out.write(
		"<tr>"+
    		"<td nowrap style=\"text-align: right;\">"+
	    		"and principle"+
		    "</td>"+
		    "<td nowrap>"+
		    	"<input maxlength=\"256\" size=\"64\" name=\"ldapPrincipal\" value=\"" + ((LocalPersistenceFactory)persistenceFactory).getLDAPProperties().getProperty("java.naming.security.principal") + "\"/>"+
		    "</td>"+
		"</tr>"+
		"<tr>"+
    		"<td nowrap style=\"text-align: right;\">"+
	    		"and credentials"+
		    "</td>"+
		    "<td nowrap>"+
		    	"<input type=\"ldapCredentials\" maxlength=\"256\" size=\"32\" name=\"ldapCredentials\" value=\"" + ((LocalPersistenceFactory)persistenceFactory).getLDAPProperties().getProperty("java.naming.security.credentials") + "\"/> ."+
		    "</td>"+
		"</tr>");
		}
		
		out.write(
		"<tr>"+
    		"<td nowrap style=\"text-align: right;\">"+
	    		"Update GID for every access? "+
		    "</td>"+
		    "<td nowrap>"+
				"<select name=\"synchGroups\"><option "+
				(((LocalPersistenceFactory)persistenceFactory).isSynchGroups()?"selected":"")+
				">true</option><option "+
				(((LocalPersistenceFactory)persistenceFactory).isSynchGroups()?"":"selected")+
				">false</option></select>"+
		    "</td>"+
		"</tr>"
		);
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
