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
<h2><span>Manual Group Mappings</span></h2>
</div>
<%@include file="sideNav.jspf"%>
<div id="body">
<p>
<%-- <jsp:useBean id="beanInstanceName" scope="session" class="beanPackage.BeanClassName" /> --%>
<%-- <jsp:getProperty name="beanInstanceName"  property="propertyName" /> --%>

Adds/removes manual DN to account mappings.
</p>

<form action="addAccountRange.jsp" method="post">
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
        <td style="text-align: right;">DN: </td>
        <td style="text-align: left;"><input type="text" name="DNs" maxlength="256" size="64"/></td>
      </tr>
      <tr>  	
        <td style="text-align: right;">i.e. </td>
        <td style="text-align: left;" nowrap>/DC=org/DC=doegrids/OU=Services/CN=host/mygk.mysite1.com</td>
      </tr>
	  <tr>
        <td style="text-align: right;">Account: </td>
        <td style="text-align: left;"><input type="text" name="account" maxlength="256" size="16"/></td>
      </tr>      
      <tr>
        <td colspan="2" style="text-align: center;">
        	<button type="submit" name="action" value="add">Add mapping(s)</button>
        	<button type="submit" name="action" value="remove">Remove mapping(s)</button>
        </td>
      </tr>
    </tbody>
  </table>
</form>

</div>
<%@include file="bottomNav.jspf"%>
</body>
</html>
