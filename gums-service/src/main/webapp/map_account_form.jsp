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
<h2><span>Map Account</span></h2>
</div>
<%@include file="sideNav.jspf"%>
<div id="body">

<p>
Determines which grid identities are mapped to a local account.
</p>
<form action="map_account.jsp" method="get">
  <table id="form" border="0" cellpadding="2" cellspacing="2">
    <tbody>
      <tr>
        <td style="text-align: right;">Account Name:<br>
        </td>
        <td><input maxlength="128" size="64" name="accountName"><br>
        </td>
      </tr>
      <tr>
        <td style="text-align: right;">i.e.</td>
        <td>myAccount</td>
      </tr>
      <tr>
        <td colspan="2" rowspan="1">
        <div style="text-align: center;"><button type="submit">map account</button></div>
        </td>
      </tr>
    </tbody>
  </table>
</form> 

</div>
<%@include file="bottomNav.jspf"%>
</body>
</html>
