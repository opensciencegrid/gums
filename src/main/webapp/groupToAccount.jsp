<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@page import="gov.bnl.gums.*"%>
<jsp:useBean id="gums" scope="application" class="gov.bnl.gums.admin.GUMSAPIImpl" />
<%@ page import="gov.bnl.gums.account.*" %>
<%@ page import="gov.bnl.gums.groupToAccount.*" %>
<%@ page import="gov.bnl.gums.userGroup.*" %>
<%@ page import="gov.bnl.gums.configuration.*" %>
<%@ page import="gov.bnl.gums.admin.ConfigurationWebToolkit" %>
<%@ page import="java.util.*" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
<head>
 	<title>GUMS Configuration</title>
 	<link href="gums.css" type="text/css" rel="stylesheet"/>
</head>
<body>
<%@include file="topNav.jspf"%>
<div id="title">
<h1><span>GUMS</span></h1>
<h2><span>GUMS Configuration</span></h2>
</div>
<%@include file="sideNav.jspf"%>
<div id="body">
<p>
<%-- <jsp:useBean id="beanInstanceName" scope="session" class="beanPackage.BeanClassName" /> --%>
<%-- <jsp:getProperty name="beanInstanceName"  property="propertyName" /> --%>

Configures group to account manager mappings.
</p>

<%
String errorMessage = null;
Configuration configuration;
if( request.getParameter("save")!=null )
{
	try{
		configuration = ConfigurationWebToolkit.loadConfiguration(request);
		gums.setConfiguration(configuration);
		out.write(
"<p>Configuration has been successfully saved!</p>");
	}catch(Exception e){
		errorMessage =
"<p>" + e.getMessage() + "</p>";
		out.write(errorMessage);
	}
}

if( request.getParameter("save")==null || errorMessage!=null )
{
	configuration = gums.getConfiguration();
	Collection groupMappers = configuration.getGroupMapping().values();
	Collection userGroupManagers = configuration.getUserGroupManagers().getValues(); 
	Collection accountManagers = configuration.getAccountManagers().getValues(); 
	
	out.write(
"<form action=\"groupMapper.jsp\" method=\"get\">"+
  "<table>");

	Iterator groupMapperIt = groupMappers.iterator();
	int counter = 0;
	while(counter==0 || groupMapperIt.hasNext())
	{
		Group2AccountMapper group2AccountMapper = groupMapperIt.hasNext()?(GroupMapper)groupMapperIt.next():null;     
	
		out.write(
	"<tr>"+
		"<td width=\"25\">"+
			"<input type=\"submit\" name=\"HM_Add"+counter+"\" value=\"+\" style=\"font-family:fixed;\">"+
			"<input type=\"submit\" name=\"HM_Remove"+counter+"\" value=\"-\" style=\"font-family:fixed;\">"+
		"</td>"+
   		"<td>"+
    		"<table id=\"form\" cellpadding=\"2\" cellspacing=\"2\">"+
   				"<tr>"+
		    		"<td nowrap>"+
			    		"Map group <input maxlength=\"32\" name=\"GM_name" + counter + "\" value=\""+
			    		(groupMapper!=null?group2AccountMapper.getName():"")+
			    		"\"><br/>");
		out.write(
				    	"&nbsp;&nbsp;&nbsp;where group membership approved by user group manager "+
				    	"<select maxlength=\"32\" name=\"GM_vOManager" + counter + "\" size=\"3\">");
		Iterator userGroupManagerIt = userGroupManagers.iterator();
		while(userGroupManagerIt.hasNext())
		{
			UserGroupManager userGroupManager = (UserGroupManager)managerIt.next();
			out.write(	
							"<option + (userGroupManager.getName().equals(groupMapper.getUserManager()))?"selected":"" + ">"+
								userGroupManager.getName()+
							"</option>");
		}
			
		out.write(
			    		"</select><br/>"+
				    	"to account obtained from account manager(s) ");
		out.write(		"<select maxlength=\"32\" name=\"GM_accountManagers" + counter + "\" onchange=\"submit()\" multiple>");
		Collection accountManagers = groupMapper.getAccountManagers();
		Iterator accountManagersIt = accountManagers.iterator();
		int subCounter = 0;
		while(accountManagersIt.hasNext())
		{
			AccountManager accountManager = (AccountManager)accountManagersIt.next();
			out.write(
							"<option " + (accountManagers.getName().equals(groupMapper.getAccountManager()))?"selected":"" + ">" +
								groupMapper.getName()+
							"</option>");
	
		}
		out.write(		"</select>"+
				   	"</td>"+
		      	"</tr>"+
			"</table>"+
		"</td>"+
		"<td width=\"25\"></td>"+
     "</tr>");
	
		counter++;
	} // end of group mapper while loop
	
	out.write(
	"<tr>"+
        "<td colspan=2><div style=\"text-align: center;\"><button type=\"submit\" name=\"save\">Save</button></div>"+
        "</td>"+
	"</tr>"+
  "</table>"+
"</form>");
} // end of non-postback else

%>

</div>
<%@include file="bottomNav.jspf"%>
</body>
</html>
