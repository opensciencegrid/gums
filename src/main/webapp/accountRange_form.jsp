<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@page import="gov.bnl.gums.*"%>
<%@ page import="gov.bnl.gums.account.*" %>
<%@ page import="gov.bnl.gums.service.ConfigurationWebToolkit" %>
<%@ page import="java.util.*" %>
<jsp:useBean id="gums" scope="application" class="gov.bnl.gums.admin.GUMSAPIImpl" />
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
<head>
  <title>accountRange Form</title>
  <link href="gums.css" type="text/css" rel="stylesheet">
</head>
<body>
<%@include file="topNav.jspf"%>
<div id="title">
<h1><span>GUMS</span></h1>
<h2><span>GUMS configuration</span></h2>
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
<%
		out.write(
			ConfigurationWebToolkit.createSelectBox("persistenceFactory", 
				gums.getConfiguration().getPersistenceFactories().values(), 
				null,
				null,
				gums.getConfiguration().getPersistenceFactories().values().size()>1) );
%>  
		</td>
	  </tr>
	  <tr>
        <td style="text-align: right;">Pool Account Mapper: </td>
        <td style="text-align: left;">
<%
		ArrayList poolMappers = new ArrayList();
		Iterator it = gums.getConfiguration().getAccountMappers().values().iterator();
		while(it.hasNext()) {
			AccountMapper accountMapper = (AccountMapper)it.next();
			if (accountMapper instanceof AccountPoolMapper)
				poolMappers.add( ((AccountPoolMapper)accountMapper).getName() );
		}

		out.write(
			ConfigurationWebToolkit.createSelectBox("accountMapper", 
				poolMappers, 
				null,
				null,
				poolMappers.size()>1) );
%>   
	</td>
      </tr>
	  <tr>
        <td style="text-align: right;">Range: </td>
        <td style="text-align: left;"><input type="text" name="range" maxlength="256" size="16"/></td>
      </tr>
	  <tr>
        <td style="text-align: right;">i.e.: </td>
        <td style="text-align: left;">account1-100</td>
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
