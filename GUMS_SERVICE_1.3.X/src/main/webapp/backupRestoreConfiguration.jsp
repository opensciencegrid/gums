<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@page import="gov.bnl.gums.*"%>
<%@page import="java.util.Date"%>
<%@page import="gov.bnl.gums.configuration.ConfigurationStore"%>
<%  String command = request.getParameter("command");%>
<%  String name = request.getParameter("name");%>
<%  String newName = request.getParameter("newName");%>
<jsp:useBean id="gums" scope="application" class="gov.bnl.gums.admin.GUMSAPIImpl" />
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
<head>
  <title>GUMS</title>
  <link href="gums.css" type="text/css" rel="stylesheet">
</head>
<body>
<%@include file="topNav.jspf"%>
<div id="title">
<h1><span>GUMS <%=gums.getVersion()%></span></h1>
<h3><span>GRID User Management System</h3>
<h2><span>Back Up/Restore Configuration</span></h2>
</div>
<%@include file="sideNav.jspf"%>
<div id="body">
<p>
<%
	if ("backup".equals(command)) {
		if (newName == null || newName.length()==0)
			newName = ConfigurationStore.getFormat().format(new Date());
		try {
			gums.backupConfiguration(newName);
			out.println("Configuration successfully backed up!");
		} catch(Exception e) {
			out.println("<div class=\"failure\">Error backing up configuration: " + e.getMessage() + "</div>");
		}
	} 
	else if ("restore".equals(command)) {
		try {
			gums.restoreConfiguration(name);
			out.println("Configuration successfully restored!");
		} catch(Exception e) {
			out.println("<div class=\"failure\">Error restoring configuration: " + e.getMessage() + "</div>");
		}	
	}
	else if ("delete".equals(command)) {
		try {
			gums.deleteBackupConfiguration(name);
			out.println("Configuration successfully deleted!");
		} catch(Exception e) {
			out.println("<div class=\"failure\">Error deleting configuration: " + e.getMessage() + "</div>");
		}	
	}
	else {
		out.println("<div class=\"failure\">Unknown error</div>");
	}
%>
</p>
</div>
<%@include file="bottomNav.jspf"%>
</body>
</html>
