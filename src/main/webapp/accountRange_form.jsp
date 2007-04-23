<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@page import="gov.bnl.gums.*"%>
<%@ page import="gov.bnl.gums.account.*" %>
<%@ page import="gov.bnl.gums.configuration.*" %>
<%@ page import="gov.bnl.gums.service.ConfigurationWebToolkit" %>
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
<h1><span>GUMS</span></h1>
<h2><span>Manage Pool Accounts</span></h2>
</div>
<%@include file="sideNav.jspf"%>
<div id="body">
<%-- <jsp:useBean id="beanInstanceName" scope="session" class="beanPackage.BeanClassName" /> --%>
<%-- <jsp:getProperty name="beanInstanceName"  property="propertyName" /> --%>

<%
Configuration configuration = null;
try {
	configuration = gums.getConfiguration();
}catch(Exception e){
%>

<p><div class="failure">Error getting configuration: <%= e.getMessage() %></div></p>
</div>
<%@include file="bottomNav.jspf"%>
</body>
</html>

<%
	return;
}
%>

<p>
Adds, removes, or unassigns accounts in pool.
</p>

<form action="accountRange.jsp" method="get">
  <input type="hidden" name="action">
  <table id="form" border="0" cellpadding="2" cellspacing="2" align="center">
    <tbody>
	  <tr>
        <td style="text-align: right;">Pool Account Mapper: </td>
        <td style="text-align: left;">
<%
		ArrayList poolMappers = new ArrayList();
		Iterator it = configuration.getAccountMappers().values().iterator();
		while(it.hasNext()) {
			AccountMapper accountMapper = (AccountMapper)it.next();
			if (accountMapper instanceof AccountPoolMapper)
				poolMappers.add( ((AccountPoolMapper)accountMapper).getName() );
		}

		out.write(
			ConfigurationWebToolkit.createSelectBox("accountMapper", 
				poolMappers, 
				(request.getParameter("accountMapper")!=null ? request.getParameter("accountMapper") : null),
				"onchange=\"document.forms[0].action='accountRange_form.jsp';document.forms[0].submit();\"",
				false) );
%>   
		</td>
	</tr>
<%
      	String selected = (String)poolMappers.get(0);
      	String assignments = ((AccountPoolMapper)configuration.getAccountMapper(selected)).getAssignments();
		if (selected!=null && !assignments.equals("")) {
%>
	<tr>
		<td style="text-align: right;">Current Assignments: </td>
		<td style="text-align: left;">
			<%=assignments%>
		</td>
    </tr>	
<%
	}
%>
	<tr>
        <td style="text-align: right;">Range: </td>
        <td style="text-align: left;"><input type="text" name="range" maxlength="256" size="16"/></td>
	</tr>
	<tr>
        <td style="text-align: right;">i.e.: </td>
        <td style="text-align: left;">myAccount001-100</td>
	</tr>
	<tr>
		<td colspan="2" style="text-align: center;">
        	<button type="submit" onclick="if(document.forms[0].elements['range'].value==''){ alert('You must enter a range'); return false;} document.forms[0].elements['action'].value='add'">Add Account Range</button>
        	<button type="submit" onclick="if(document.forms[0].elements['range'].value==''){ alert('You must enter a range'); return false;} if(!confirm('Are you sure you want to remove this account range?'))return false; document.forms[0].elements['action'].value='remove'">Remove Account Range</button>
			<button type="submit" onclick="if(document.forms[0].elements['range'].value==''){ alert('You must enter a range'); return false;} if(!confirm('Are you sure you want to unassign accounts?'))return false; document.forms[0].elements['action'].value='unassign'">Unassign Account Range</button>
		</td>
	</tr>
    </tbody>
  </table>
</form>

</div>
<%@include file="bottomNav.jspf"%>
</body>
</html>
