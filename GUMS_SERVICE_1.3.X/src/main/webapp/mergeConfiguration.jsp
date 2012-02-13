<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@page import="gov.bnl.gums.*"%>
<%@page import="gov.bnl.gums.service.*"%>
<%@page import="gov.bnl.gums.configuration.*"%>
<jsp:useBean id="gums" scope="application" class="gov.bnl.gums.admin.GUMSAPIImpl" />
<%  String persistenceFactory = request.getParameter("persistenceFactory"); if (persistenceFactory!=null) persistenceFactory=persistenceFactory.trim();%>
<%  String h2gMapping = request.getParameter("h2gMapping"); if (h2gMapping!=null) h2gMapping=h2gMapping.trim();%>
<%  String uri = request.getParameter("uri"); if (uri!=null) uri=uri.trim();%>
<%@ page import="java.util.*" %>
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
<h2><span>Merge Configuration</span></h2>
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
Merge configuration into current configuration, replacing user groups, account mappers, and group to account mappings with the same name.
</p>

<%
if (configuration.getPersistenceFactories().size()==0) {
%>
	<p><div class="failure">You must first create a persistence factory.</div></p>
	</div>
	<%@include file="bottomNav.jspf"%>
	</body>
	</html>
<%
	return;
}
if (configuration.getHostToGroupMappings().size()==0) {
%>
	<p><div class="failure">You must first create a host to group mapping.</div></p>
	</div>
	<%@include file="bottomNav.jspf"%>
	</body>
	</html>
<%
	return;
}
String message = null;
if ( "merge".equals(request.getParameter("command")) ) {
	
	Configuration clonedConfiguration = (Configuration)configuration.clone();
	try{
		gums.mergeConfiguration(clonedConfiguration, uri, persistenceFactory, h2gMapping);
		gums.setConfiguration(clonedConfiguration);
		message = "<div class=\"success\">Configuration has been merged. It is recommended to update VO members.</div>";
	}catch(Exception e){
		message = "<div class=\"failure\">Error merging configuration: " + e.getMessage() + "</div>";
	}

	out.write(
		"<table id=\"form\" cellpadding=\"2\" cellspacing=\"2\">");

	if(message!=null)
		out.write( "<tr><td colspan=\"2\">" + message + "</td></tr>" );		
		
	out.write(
		"</table></form>");
}
else {
%>
<form action="mergeConfiguration.jsp" method="get">
	<input type="hidden" name="command" value="">
	<table id="form" border="0" cellpadding="2" cellspacing="2" align="center">
        <tr>
            <td nowrap style="text-align: right;">
				Persistence Factory:
            </td>
            <td>
                <%=ConfigurationWebToolkit.createSelectBox("persistenceFactory",
                    configuration.getPersistenceFactories().values(),
                    null,
                    null,
                    false)%> (used in all elements requiring a persistence factory)
			</td>
        </tr>
        
		<tr>
            <td nowrap style="text-align: right;">
				Host to Group Mapping:
            </td>
            <td>
                <%=ConfigurationWebToolkit.createSelectBox("h2gMapping",
                    configuration.getHostToGroupMappings(),
                    null,
                    null,
                    false)%> (in which all group to account mappings will be added)
			</td>
        </tr>
        
        <tr>
            <td nowrap style="text-align: right;">
				Configuration URI:
            </td>
            <td>
				<input maxlength="256" size="64" name="uri"/>
            </td>
        </tr>
        <tr>
            <td nowrap style="text-align: right;">
                i.e.
            </td>
            <td nowrap>
                http://software.grid.iu.edu/pacman/tarballs/vo-version/gums.template
            </td>
        </tr>

		<tr>
	        <td colspan=2>
	        	<div style="text-align: center;">
	        		<button type="submit" onclick="document.forms[0].elements['command'].value='merge'; return true;">merge</button>
	        	</div>
	        </td>
		</tr>
        
	</table>
</form>
<%
}
%>

</div>
<%@include file="bottomNav.jspf"%>
</body>
</html>
