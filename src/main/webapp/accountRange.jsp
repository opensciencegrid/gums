<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@page import="gov.bnl.gums.*"%>
<%@page import="gov.bnl.gums.account.*"%>
<%  String action = request.getParameter("action");%>
<%  String accountMapper = request.getParameter("accountMapper");%>
<%  String range = request.getParameter("range");%>
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
<h1><span>GUMS</span></h1>
<h2><span>Add Pool Account Range</span></h2>
</div>
<%@include file="sideNav.jspf"%>
<div id="body">
<p>
<BR>
<% 
	if (action.equals("add")) {
		try {
			gums.addAccountRange(
				accountMapper, 
				range ); 
			out.println("Accounts have been successfully added to the pool!");
		} catch(Exception e) {
			out.println("<div class=\"failure\">Error saving pool account range: " + e.getMessage() + "</div>");
		}
	else if (action.equals("remove")) {	
		try {
			gums.removeAccountRange(
				accountMapper, 
				range ); 
			out.println("Accounts have been successfully removed from the pool!");
		} catch(Exception e) {
			out.println("<div class=\"failure\">Error removing pool account range: " + e.getMessage() + "</div>");
		}
	else if (action.equals("unassign")) {	
		try {
			gums.unassignAllUsers(
				accountMapper); 
			out.println("All users have been deassigned from pool!");
		} catch(Exception e) {
			out.println("<div class=\"failure\">Error unassigning users: " + e.getMessage() + "</div>");
		}
	}
%>
</p>
<BR>
</div>
<%@include file="bottomNav.jspf"%>
</body>
</html>