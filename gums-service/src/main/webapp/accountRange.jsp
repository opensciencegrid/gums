<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@page import="gov.bnl.gums.*"%>
<%@page import="gov.bnl.gums.account.*"%>
<%  String command = request.getParameter("command");%>
<%  String accountMapper = request.getParameter("accountMapper");%>
<%  String range = request.getParameter("range"); if (range!=null)range = range.trim();%>
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
<h2><span>Manage Pool Accounts</span></h2>
</div>
<%@include file="sideNav.jspf"%>
<div id="body">
<p>
<BR>
<% 
	if ("add".equals(command)) {
		try {
			gums.addAccountRange2( accountMapper, range ); 
			out.println("Accounts have been added to the pool.");
		} catch(Exception e) {
			out.println("<div class=\"failure\">Error saving pool account range: " + e.getMessage() + "</div>");
		}
	}
	else if ("remove".equals(command)) {	
		try {
			gums.removeAccountRange( accountMapper, range ); 
			out.println("Accounts have been removed from the pool.");
		} catch(Exception e) {
			out.println("<div class=\"failure\">Error removing pool account range: " + e.getMessage() + "</div>");
		}
	}
	else if ("unassign".equals(command)) {	
		try {
			gums.unassignAccountRange( accountMapper, range ); 
			out.println("Accounts have been unassigned.");
		} catch(Exception e) {
			out.println("<div class=\"failure\">Error unassigning accounts: " + e.getMessage() + "</div>");
		}
	}
%>
</p>
<BR>
</div>
<%@include file="bottomNav.jspf"%>
</body>
</html>