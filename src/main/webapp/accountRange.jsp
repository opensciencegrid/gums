<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@page import="gov.bnl.gums.*"%>
<%@page import="gov.bnl.gums.account.*"%>
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
<% try {
		gums.addAccountRange(
			((AccountPoolMapper)gums.getConfiguration().getAccountMappers().get(request.getParameter("accountMapper"))).getPersistenceFactory(), 
			((AccountPoolMapper)gums.getConfiguration().getAccountMappers().get(request.getParameter("accountMapper"))).getAccountPool(), 
			request.getParameter("range")); 
		out.println("Accounts have been successfully added to the pool!");
	} catch(Exception e) {
		out.println("<div class=\"failure\">Error saving pool account range: " + e.getMessage() + "</div>");
	}
%>
</p>
<BR>
</div>
<%@include file="bottomNav.jspf"%>
</body>
</html>