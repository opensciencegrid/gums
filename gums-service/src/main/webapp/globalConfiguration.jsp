<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@page import="gov.bnl.gums.*"%>
<%@ page import="gov.bnl.gums.configuration.*" %>
<%@ page import="gov.bnl.gums.service.ConfigurationWebToolkit" %>
<%@ page import="java.util.*" %>
<%@ page import="java.lang.StringBuffer" %>
<jsp:useBean id="gums" scope="application" class="gov.bnl.gums.admin.GUMSAPIImpl" />
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
<head>
 	<title>GUMS</title>
 	<link href="gums.css" type="text/css" rel="stylesheet"/>
</head>
<body>
<%@include file="topNav.jspf"%>
<div id="title">
<h1><span>GUMS <%=gums.getVersion()%></span></h1>
<h3><span>GRID User Management System</h3>
<h2><span>Configuration</span></h2>
</div>
<%@include file="sideNav.jspf"%>
<div id="body">
<%-- <jsp:useBean id="beanInstanceName" scope="session" class="beanPackage.BeanClassName" /> --%>
<%-- <jsp:getProperty name="beanInstanceName"  property="propertyName" /> --%>

<%
Configuration configuration = null;
try {
	configuration = gums.getConfiguration();
}catch(Exception e){
%>

<p><div class="failure"><%= e.getMessage() %></div></p>
</div>
<%@include file="bottomNav.jspf"%>
</body>
</html>

<%
	return;
}
%>

<p>
Sets global configuration attributes.
</p>

<%

String[] bannedUserGroups = new String[0];
boolean allowGridMapfiles = true;
String message = null;
if ("save".equals(request.getParameter("command"))) {
	Configuration newConfiguration = (Configuration)configuration.clone();
	try{
		newConfiguration.setAllowGridmapFiles("on".equals(request.getParameter("allowGMP")));
		StringBuffer buffer = new StringBuffer();
		int counter = 0;
		while(request.getParameter("bug" + counter)!=null) {
			String userGroupName = request.getParameter("bug" + counter).trim();
			if (!userGroupName.equals(""))
				buffer.append(userGroupName+",");
			counter++;
		}
		if (buffer.length()>0 && buffer.charAt(buffer.length()-1)==',')
			buffer.replace(buffer.length()-1, buffer.length(), "");
		newConfiguration.setBannedUserGroups(buffer.toString());
		gums.setConfiguration(newConfiguration);
		configuration = gums.getConfiguration();
		if (configuration.getBannedUserGroups().length()>0)
			bannedUserGroups = configuration.getBannedUserGroups().split(",");
		allowGridMapfiles = "on".equals(request.getParameter("allowGMP"));
		message = "<div class=\"success\">Configuration has been saved.</div>";
	}catch(Exception e){
		message = "<div class=\"failure\">Error saving configuration: " + e.getMessage() + "</div>";
	}
}

else if ("reload".equals(request.getParameter("command"))) {
	try{
		int counter = 0;
		ArrayList tempArray = new ArrayList();
		while(request.getParameter("bug" + counter)!=null) {
			String userGroupName = request.getParameter("bug" + counter).trim();
			if (!userGroupName.equals(""))
				tempArray.add(userGroupName);
			counter++;
		}
		bannedUserGroups = new String[tempArray.size()];
		Iterator it = tempArray.iterator();
		counter = 0;
		while (it.hasNext()) {
			bannedUserGroups[counter] = (String)it.next();
			counter++;
		}
		allowGridMapfiles = "on".equals(request.getParameter("allowGMP"));
	} catch(Exception e) {
		out.write( "<div class=\"failure\">Error reloading host to group mapping: " + e.getMessage() + "</div>" );
		return;
	}
}

else {
	try{
		if (configuration.getBannedUserGroups().length()>0)
			bannedUserGroups = configuration.getBannedUserGroups().split(",");
		allowGridMapfiles = configuration.getAllowGridmapFiles();
	} catch(Exception e) {
		out.write( "<div class=\"failure\">Error reloading host to group mapping: " + e.getMessage() + "</div>" );
		return;
	}
}

if(message!=null)
	out.write( 
		"<tr><td colspan=\"2\">" + message + "</td></tr>" );
	
%>
<form action="globalConfiguration.jsp" method="get">
	<input type="hidden" name="command" value="save">
	<input type="hidden" name="insertCounter">
	
	<table id="form" cellpadding="2" cellspacing="2">
	   	<tr>
			<td colspan=2 style="text-align: left;">
				<input type="checkbox" name="allowGMP" <%=allowGridMapfiles?"CHECKED":""%>> Allow grid mapfile generation (uncheck if not needed so that users that don't use your site will not use up pool accounts)
			</td>
		</tr>
		<tr>
			<td nowrap style="text-align: left;">
				Banned User Groups: 
<%
				// Create multiple group to account mappings
				int counter = 0;
			 	int lastCounter = bannedUserGroups.length;
				int insertCounter = lastCounter;
				if (request.getParameter("insertCounter")!=null && !request.getParameter("insertCounter").equals(""))
					insertCounter = Integer.parseInt(request.getParameter("insertCounter"));
				for (int i=0; i<lastCounter; i++) {
					String bannedUserGroup = bannedUserGroups[i].trim();
					if (counter!=insertCounter)
						out.write("<a href=\"javascript:void(0)\" onclick=\"document.forms[0].elements['command'].value='reload';document.forms[0].elements['insertCounter'].value='"+counter+"';document.forms[0].submit();\" title=\"insert here\">+</a> ");
					int numRepetitions = (counter==insertCounter)?2:1;
			        for (int j=0; j<numRepetitions; j++) {
						out.write( 
							ConfigurationWebToolkit.createSelectBox("bug"+counter,
								configuration.getUserGroups().values(),
								(counter==insertCounter) ? null : bannedUserGroup,
								"onchange=\"document.forms[0].elements['command'].value='reload';document.forms[0].submit();\"",
								true) );
			 			counter++;
			 		}
				}
				if (insertCounter==lastCounter) {
					out.write( 
						ConfigurationWebToolkit.createSelectBox("bug"+lastCounter, 
							configuration.getUserGroups().values(), 
							null,
							"onchange=\"document.forms[0].elements['command'].value='reload';document.forms[0].submit();\"",
							true) );
				}
				else
					out.write("<a href=\"javascript:void(0)\" onclick=\"document.forms[0].elements['command'].value='reload';document.forms[0].elements['insertCounter'].value='"+lastCounter+"';document.forms[0].submit();\" title=\"insert here\">+</a> ");
				%> 
			(optional) </td>
		</tr>
		<tr>
			<td>
	        	<div style="text-align: center;">
	        		<button type="submit">save</button>
	        	</div>
	        </td>
		</tr>
	  </table>
</form>
</div>
<%@include file="bottomNav.jspf"%>
</body>
</html>
