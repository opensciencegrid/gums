<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@page import="gov.bnl.gums.*"%>
<%@ page import="gov.bnl.gums.userGroup.*" %>
<%@ page import="gov.bnl.gums.service.ConfigurationWebToolkit" %>
<%@ page import="java.util.*" %>
<jsp:useBean id="gums" scope="application" class="gov.bnl.gums.admin.GUMSAPIImpl" />
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
<head>
  <title>manualAccountMappings Form</title>
  <link href="gums.css" type="text/css" rel="stylesheet">
</head>
<body>
<%@include file="topNav.jspf"%>
<div id="title">
<h1><span>GUMS</span></h1>
<h2><span>Manual Account Mappings</span></h2>
</div>
<%@include file="sideNav.jspf"%>
<div id="body">
<p>
<%-- <jsp:useBean id="beanInstanceName" scope="session" class="beanPackage.BeanClassName" /> --%>
<%-- <jsp:getProperty name="beanInstanceName"  property="propertyName" /> --%>

Adds/removes users from manual user group.
</p>

<form action="manualAccountMappings.jsp" method="post">
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
        <td style="text-align: right;">Manual User group: </td>
        <td style="text-align: left;">
<%
		ArrayList manualUserGroups = new ArrayList();
		Iterator it = gums.getConfiguration().getUserGroups().values().iterator();
		while(it.hasNext()) {
			UserGroup manualUserGroup = (UserGroup)it.next();
			if (manualUserGroup instanceof ManualUserGroup)
				manualUserGroups.add( ((ManualUserGroup)manualUserGroup).getName() );
		}

		out.write(
			ConfigurationWebToolkit.createSelectBox("userGroup", 
				manualUserGroups, 
				null,
				null,
				manualUserGroups.size()>1) );
%>   
	</td>
      </tr>
	  <tr>
        <td style="text-align: right;">DN: </td>
        <td style="text-align: left;"><input type="text" name="DN" maxlength="256" size="64"/></td>
      </tr>
      <tr>  	
        <td style="text-align: right;">i.e. </td>
        <td style="text-align: left;" nowrap>/DC=org/DC=doegrids/OU=Services/CN=host/mygk.mysite1.com</td>
      </tr>
	  <tr>
        <td style="text-align: right;">Account: </td>
        <td style="text-align: left;"><input type="text" name="account" maxlength="256" size="32"/></td>
      </tr>      
      <tr>
        <td colspan="2" style="text-align: center;">
        	<button type="submit" name="action" value="add">Add Mapping</button>
        	<button type="submit" name="action" value="remove">Remove Mapping</button>
        </td>
      </tr>
    </tbody>
  </table>
</form>

</div>
<%@include file="bottomNav.jspf"%>
</body>
</html>
