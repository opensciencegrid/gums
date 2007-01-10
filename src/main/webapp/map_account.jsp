<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@page import="gov.bnl.gums.*"%>
<%@ page import="java.util.*" %>
<jsp:useBean id="gums" scope="application" class="gov.bnl.gums.admin.GUMSAPIImpl" />
<%  String accountName = request.getParameter("accountName");%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
<head>
  <title>Map User Output</title>
  <link href="gums.css" type="text/css" rel="stylesheet">
</head>
<body>
<%@include file="topNav.jspf"%>
<div id="title">
<h1><span>GUMS</span></h1>
<h2><span>Inverse Map user</span></h2>
</div>
<%@include file="sideNav.jspf"%>
<div id="body">
<% 
	String DNs = null;
	try {
		DNs = gums.mapAccount(accountName); 
%>
<p>Account '<%= accountName %>' is mapped to grid identity(s):</p>
  <table id="form" >
    <tbody>
      <tr>
        <td>
<pre>
<% 
	try {
		out.println(DNs); 
	} catch(Exception e) {
		out.println("<div class=\"failure\">Error mapping account: " + e.getMessage() + "</div>");
	}
%>
</pre>
        </td>
      </tr>
    </tbody>
  </table>
<BR><BR>
<%
	} catch(Exception e) {
		out.println("<BR><div class=\"failure\">Error mapping account: " + e.getMessage() + "</div>");
	}
%>
</div>
<%@include file="bottomNav.jspf"%>
</body>
</html>

