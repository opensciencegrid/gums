<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@page import="gov.bnl.gums.*"%>
<%@page import="gov.bnl.gums.service.ConfigurationWebToolkit"%>
<%@ page import="java.util.*" %>
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
<h2><span>Back Up/Restore Configuration</span></h2>
</div>
<%@include file="sideNav.jspf"%>
<div id="body">
<p>
<%-- <jsp:useBean id="beanInstanceName" scope="session" class="beanPackage.BeanClassName" /> --%>
<%-- <jsp:getProperty name="beanInstanceName"  property="propertyName" /> --%>

<%
Collection backupNames = null;
try {
	backupNames = gums.getBackupNames();
}catch(Exception e){
%>

<p><div class="failure">Error getting backup configuration dates: <%= e.getMessage() %></div></p>
</div>
<%@include file="bottomNav.jspf"%>
</body>
</html>

<%
	return;
}
%>

Backs up or restores configuration.
</p>

<form action="backupRestoreConfiguration.jsp" method="get">
  <input type="hidden" name="command" value="backup">
  <table id="form" border="0" cellpadding="2" cellspacing="2">
    <tbody>
      <tr>
        <td style="text-align: center;">
        	Name: <input maxlength="128" size="32" name="newName">
        </td>
      </tr>  
      <tr>
        <td>
			<div style="text-align: center;"><button type="submit" onclick="document.forms[0].elements['command'].value='backup'">backup</button></div>
        </td>
      </tr>
<%
	if (backupNames.size()>0) {
%>
      <tr>
        <td>
			<div style="text-align: center;">------</div>
        </td>
      </tr>
      <tr>       
        <td>
	        <div style="text-align: center;"><%=ConfigurationWebToolkit.createSelectBox("name", backupNames, null, null, false)%></div>
        </td>
      </tr>
      <tr>       
        <td>
        	<div style="text-align: center;">
        		<button type="submit" onclick="document.forms[0].elements['command'].value='restore'">restore</button>
        		<button type="submit" onclick="if(!confirm('Are you sure you want to delete this configuration?'))return false; document.forms[0].elements['command'].value='delete'">delete</button>
        	</div>
        </td>
      </tr>
<%
	}
%>
    </tbody>
  </table>

</div>
<%@include file="bottomNav.jspf"%>
</body>
</html>
