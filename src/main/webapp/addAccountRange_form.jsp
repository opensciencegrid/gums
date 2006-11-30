<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@page import="gov.bnl.gums.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
<head>
  <title>updateMembers Form</title>
  <link href="gums.css" type="text/css" rel="stylesheet">
</head>
<body>
<%@include file="topNav.jspf"%>
<div id="title">
<h1><span>GUMS</span></h1>
<h2><span>Adds range of accounts to a pool</span></h2>
</div>
<%@include file="sideNav.jspf"%>
<div id="body">
<p>
<%-- <jsp:useBean id="beanInstanceName" scope="session" class="beanPackage.BeanClassName" /> --%>
<%-- <jsp:getProperty name="beanInstanceName"  property="propertyName" /> --%>

Adds range of accounts to a pool.
</p>

<form action="addAccountRange.jsp" method="get">
  <table id="form" border="0" cellpadding="2" cellspacing="2" align="center">
    <tbody>
      <tr colspan="2">
        <td style="text-align: right;">Persistence Factory: </td>
        <td style="text-align: left;">
        	<select name="persistenceFactory">
				<option>gov.bnl.gums.hibernate.HibernatePersistencefactory</option>
				<option>gov.bnl.gums.ldap.LDAPPersistenceFactory</option>
				<option>gov.bnl.gums.MySQLPersistenceFactory</option>
				<option>gov.bnl.gums.BNLPersistenceFactory</option>
			</select>
		</td>
	  </tr>
	  <tr>
        <td style="text-align: right;">Group Name: </td>
        <td style="text-align: left;"><input type="text" name="groupName" maxlength="256" size="16"/></td>
      </tr>
	  <tr>
        <td style="text-align: right;">Range: </td>
        <td style="text-align: left;"><input type="text" name="range" maxlength="256" size="16"/> i.e.: 1-100</td>
      </tr>
      <tr>
        <td colspan="2" style="text-align: center;"><button type="submit">Add Accounts</button></td>
      </tr>
    </tbody>
  </table>
</form>

</div>
<%@include file="bottomNav.jspf"%>
</body>
</html>
