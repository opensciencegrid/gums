<%@page isErrorPage="true" %>
<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@page import="java.io.PrintWriter"%>
<%@page import="gov.bnl.gums.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
<head>
  <title>GUMS Error</title>
  <link href="gums.css" type="text/css" rel="stylesheet">
</head>
<body>
<%@include file="topNav.jspf"%>
<div id="title">
<h1><span>GUMS <%=gums.getVersion()%></span></h1>
<h3><span>GRID User Management System</h3>
<h2><span>Error</span></h2>
</div>
<%@include file="sideNav.jspf"%>
<div id="body">
<h1>GUMS encountered an error</h1>
<p>Error Type: <%= exception.getClass().getName() %></p>
<p>Error Message: <%= exception.getMessage() %></p>
<p><i>We want GUMS to have helpful error messages. If you feel this message wasn't
helpful, or that it was due to a bug in GUMS, please report
the following information to <a href="mailto:gums-users-l@lists.bnl.gov">gums-users-l@lists.bnl.gov</a>, as it might help them.</i></p>
<pre><% exception.printStackTrace(new PrintWriter(out)); %>
</pre>
</div>
<%@include file="bottomNav.jspf"%>
</body>
</html>
