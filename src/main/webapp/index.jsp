<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@page import="gov.bnl.gums.*"%>
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
<h2><span>GUMS v@VERSION@</span></h2>
</div>
<%@include file="sideNav.jspf"%>
<div id="body">
<h1>Welcome to GUMS: Grid User Management System.</h1>
<p>GUMS allows to centrally manage
the access to gatekeepers and the policies for local account mapping.</p>
<p>This web application contains both the web service components and the web
interface, with which you can only access basic functionalities (the rest are
available throught the command line client).</p>
<p>On your left you see a series of commands you can execute to generate maps,
to map users to local accounts or to update the group information from the VO
servers. The mapping is estabilished according to an XML configuration file, which is not
accessible through the web interface.</p>
</div>
<%@include file="bottomNav.jspf"%>
</body>
</html>
