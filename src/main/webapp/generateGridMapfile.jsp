<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@page import="java.io.*"%>
<%@page import="gov.bnl.gums.*"%>
<jsp:useBean id="gums" scope="application" class="gov.bnl.gums.admin.GUMSAPIImpl" />
<%  String hostname = request.getParameter("host");%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
<head>
  <title>generateGridMapfile Output</title>
  <link href="gums.css" type="text/css" rel="stylesheet">
</head>
<body>
<%@include file="topNav.jspf"%>
<div id="title">
<h1><span>GUMS</span></h1>
<h2><span>Generate grid-mapfile</span></h2>
</div>
<%@include file="sideNav.jspf"%>
<div id="body">
<%
	String result = null;
	try {
		result = gums.generateGridMapfile(hostname); 
%>

<p>Grid-mapfile for <%= hostname %>: </p>
  <table id="form">
    <tbody>
      <tr>
        <td>
<pre>
<%
	out.println(result);
%>
</pre>
        </td>
      </tr>
    </tbody>
  </table>
<BR><BR>

<%
	} catch(Exception e) {
		out.println("<BR><div class=\"failure\">Error generating mapfile: " + e.getMessage() + "</div>");
	}
%>
</div>
<%@include file="bottomNav.jspf"%>
</body>
</html>
