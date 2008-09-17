<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@page import="gov.bnl.gums.*"%>
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
<h2><span>Welcome to GUMS</span></h2>
</div>
<%@include file="sideNav.jspf"%>
<div id="body">
<p>GUMS (Grid User Management System) allows for central management of grid identity to local account mappings.
This web application contains both the web service components and the web interface.
On your left you see a series of commands you can execute to configure the mappings, 
manage users, and test mappings.  GUMS documentation can be found at 
<a href="https://www.racf.bnl.gov/Facility/GUMS/1.3/index.html">https://www.racf.bnl.gov/Facility/GUMS/index.html</a>.</p>
</div>
<%@include file="bottomNav.jspf"%>
</body>
</html>
