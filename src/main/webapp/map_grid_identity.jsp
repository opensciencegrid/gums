<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@page import="gov.bnl.gums.*"%>
<jsp:useBean id="gums" scope="application" class="gov.bnl.gums.admin.GUMSAPIImpl" />
<%  String hostname = request.getParameter("host"); if (hostname!=null) hostname=hostname.trim();
  String DN = request.getParameter("DN"); if (DN!=null) DN=DN.trim();
  String FQAN = request.getParameter("FQAN"); if (FQAN!=null) FQAN=FQAN.trim();
  if ("".equals(FQAN)) FQAN = null;%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
<head>
  <title>GUMS <%=gums.getVersion()%></title>
  <link href="gums.css" type="text/css" rel="stylesheet">
</head>
<body>
<%@include file="topNav.jspf"%>
<div id="title">
<h1><span>GUMS</span></h1>
<h3><span>GRID User Management System</h3>
<h2><span>Map Grid Identity</span></h2>
</div>
<%@include file="sideNav.jspf"%>
<div id="body">
<% 
	String account = null;
	try {
		account = gums.mapUser(hostname, DN, FQAN);
%>

<p>On '<%= hostname %>', '<%= DN %>' is mapped to:</p>
  <table id="form" >
    <tbody>
      <tr>
        <td>
<% 
			out.println(account);
%>
        </td>
      </tr>
    </tbody>
  </table>
<BR><BR>
<%
	} catch(Exception e) {
		out.println("<p><div class=\"failure\">Error mapping grid identity: " + e.getMessage() + "</div></p>");
	}
%>
</div>
<%@include file="bottomNav.jspf"%>
</body>
</html>
