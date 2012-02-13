<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@page import="java.io.*"%>
<%@page import="gov.bnl.gums.*"%>
<jsp:useBean id="gums" scope="application" class="gov.bnl.gums.admin.GUMSAPIImpl" />
<%  String hostname = request.getParameter("host"); if (hostname!=null) hostname=hostname.trim();%>
<%  String fqan = request.getParameter("fqan"); if (fqan!=null) fqan=fqan.trim();%>
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
<h2><span>Generate Email-Mapfile</span></h2>
</div>
<%@include file="sideNav.jspf"%>
<div id="body">
<%
	String result = null;
	try {
		result = gums.generateEmailMapfile(hostname); 
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
		out.println("<p><div class=\"failure\">Error generating email-mapfile: " + e.getMessage() + "</div></p>");
	}
%>
</div>
<%@include file="bottomNav.jspf"%>
</body>
</html>
