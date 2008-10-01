<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@page import="gov.bnl.gums.*"%>
<%@ page import="gov.bnl.gums.configuration.*" %>
<%@ page import="gov.bnl.gums.service.ConfigurationWebToolkit" %>
<%@ page import="java.util.*" %>
<jsp:useBean id="gums" scope="application" class="gov.bnl.gums.admin.GUMSAPIImpl" />
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
<head>
 	<title>GUMS</title>
 	<link href="gums.css" type="text/css" rel="stylesheet"/>
</head>
<body>
<%@include file="topNav.jspf"%>
<div id="title">
<h1><span>GUMS <%=gums.getVersion()%></span></h1>
<h3><span>GRID User Management System</h3>
<h2><span>Configuration</span></h2>
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

<p><div class="failure"><%= e.getMessage() %></div></p>
</div>
<%@include file="bottomNav.jspf"%>
</body>
</html>

<%
	return;
}
%>

<p>
Sets global configuration attributes.
</p>

<%

String message = null;

if (request.getParameter("command")==null || 
	"save".equals(request.getParameter("command"))) {
	
	if ("save".equals(request.getParameter("command"))) {
		Configuration newConfiguration = (Configuration)configuration.clone();
		try{
			newConfiguration.setAllowGridmapFiles("on".equals(request.getParameter("allowGMP")));
			newConfiguration.setBannedUserGroup(request.getParameter("bannedUserGroup"));
			gums.setConfiguration(newConfiguration);
			configuration = gums.getConfiguration();
			message = "<div class=\"success\">Configuration has been saved.</div>";
		}catch(Exception e){
			message = "<div class=\"failure\">Error saving configuration: " + e.getMessage() + "</div>";
		}
	}
	
	if(message!=null)
		out.write( 
			"<tr><td colspan=\"2\">" + message + "</td></tr>" );
}	
%>
<form action="globalConfiguration.jsp" method="get">
	<input type="hidden" name="command" value="save">
	<table id="form" cellpadding="2" cellspacing="2">
	   	<tr>
			<td colspan=2 style="text-align: left;">
				<input type="checkbox" name="allowGMP" <%=configuration.getAllowGridmapFiles()?"CHECKED":""%>> Allow grid mapfile generation (uncheck if not needed so that users that don't use your site will not use up pool accounts)
			</td>
		</tr>
		<tr>
			<td nowrap style="text-align: left;">
				Banned User Group: <%
				out.write( 
					ConfigurationWebToolkit.createSelectBox("bannedUserGroup", 
						configuration.getUserGroups().values(), 
						configuration.getBannedUserGroup(),
						null,
						true) );			
			%> (optional) </td>
		</tr>
		<tr>
			<td>
	        	<div style="text-align: center;">
	        		<button type="submit">save</button>
	        	</div>
	        </td>
		</tr>
	  </table>
</form>
</div>
<%@include file="bottomNav.jspf"%>
</body>
</html>
