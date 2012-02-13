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
<h2><span>Generate FQAN-Mapfile</span></h2>
</div>
<%@include file="sideNav.jspf"%>
<div id="body">

<p>
<%-- <jsp:useBean id="beanInstanceName" scope="session" class="beanPackage.BeanClassName" /> --%>
<%-- <jsp:getProperty name="beanInstanceName"  property="propertyName" /> --%>

Generates the FQAN-mapfile relative to the given service.
</p>

<form action="generateFqanMapfile.jsp" method="get">
  <table id="form" border="0" cellpadding="2" cellspacing="2">
    <tbody>
      <tr>
        <td style="text-align: right;">DN (Distinguished Name) for service:<br>
        </td>
        <td><input maxlength="256" size="64" name="host"><br>
        </td>
      </tr>
      <tr>
        <td style="text-align: right;">i.e.</td>
        <td>/DC=com/DC=example/OU=Services/CN=example.site.com</td>
      </tr>  
      <tr>
        <td style="text-align: right;">NOTE: </td>
        <td>Only VOMS user groups and group/pool account mappers will be included</td>
      </tr>
      <tr>
        <td style="text-align: right;">NOTE: </td>
        <td>This operation will NOT assign pool accounts</td>
      </tr>
      <tr>
        <td colspan="2" rowspan="1">
        <div style="text-align: center;"><button type="submit">generate FQAN-mapfile</button></div>
        </td>
      </tr>
    </tbody>
  </table>
</form>

</div>
<%@include file="bottomNav.jspf"%>
</body>
</html>
