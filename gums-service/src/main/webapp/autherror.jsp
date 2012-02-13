<%@page isErrorPage="true" %>
<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@page import="java.io.PrintWriter"%>
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
<h1><span>GUMS <%=gums.getVersion()%></span></h1>
<h2><span>BNL GUMS</span></h2>
</div>
<%@include file="sideNav.jspf"%>
<div id="body">
<h1>Authorization denied</h1>
<p>The certificate you are using is not authorized to perform the requested operation.
Most probably, the DN is not included in the group of administrators for this GUMS server.</p>
</div>
<%@include file="bottomNav.jspf"%>
</body>
</html>
