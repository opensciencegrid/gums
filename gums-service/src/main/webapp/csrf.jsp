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
<h2><span>GUMS Cross-Site Request Forgery Prevention</span></h2>
</div>
<%@include file="sideNav.jspf"%>
<div id="body">
<p>Starting in GUMS 1.5.0, GUMS contains additional protections to prevent CSRF, where a malicious attacker attempts to trick
the GUMS sysadmin into clicking on a malicious link.
<br/>
GUMS only accepts changes if it detects the admin hit "submit" from the GUMS web-interface; otherwise, you see this page.
<br/>
If you were sent here via an external link, please report it to the OSG security team.

If you previously used home-grown, automated tools (such as curl) to administer GUMS, please contact the OSG software
team to find replacement tools.
</div>
<%@include file="bottomNav.jspf"%>
</body>
</html>
