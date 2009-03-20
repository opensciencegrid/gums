<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@page import="gov.bnl.gums.*"%>
<jsp:useBean id="gums" scope="application" class="gov.bnl.gums.admin.GUMSAPIImpl" />
<%@ page import="gov.bnl.gums.hostToGroup.*" %>
<%@ page import="gov.bnl.gums.groupToAccount.*" %>
<%@ page import="gov.bnl.gums.account.*" %>
<%@ page import="gov.bnl.gums.userGroup.*" %>
<%@ page import="gov.bnl.gums.configuration.*" %>
<%@ page import="gov.bnl.gums.service.ConfigurationWebToolkit" %>
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
<h2><span>Summary</span></h2>
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
Displays configuration summary.
</p>

<table id="form" cellpadding="2" cellspacing="2" border="1" width="95%">
<thead>
	<tr><th>Host To Group Mapping</th><th colspan="3">Group To Account Mapping</th><th colspan="6">User Group</th><th colspan="3">Account Mapper</th></tr>
	<tr><th>Name</th><th>Name</th><th>Acc. VO Subgroup</th><th>Accounting VO</th><th>Name</th><th>Type</th><th>Match FQAN</th><th>Accept Grid-Proxies</th><th>VO/Group</th><th>Role</th><th>Name</th><th>Type</th><th>Account</th></tr>
<thead>
<tbody>

<%
Collection h2GMappings = configuration.getHostToGroupMappings();
Iterator h2GMappingsIt = h2GMappings.iterator();
while (h2GMappingsIt.hasNext()) {
	HostToGroupMapping h2GMapping = (HostToGroupMapping)h2GMappingsIt.next();
	if ("/DC=com/DC=example/OU=Services/CN=example.site.com".equals(h2GMapping.getName()))
		continue;
	if (!(h2GMapping instanceof CertificateHostToGroupMapping)) {
		out.write("<tr><td colspan=\"2\">Unknown host to group mapping type</td></tr>");
		continue;
	}
	CertificateHostToGroupMapping cH2GMapping = (CertificateHostToGroupMapping)h2GMapping;
	Collection groupToAccountMappings = cH2GMapping.getGroupToAccountMappings();
	Iterator g2AMappingsIt = groupToAccountMappings.iterator();
	String lastH2GMapping = null;
	String lastG2AMapping = null;
	String lastUserGroup = null;
	while (g2AMappingsIt.hasNext()) {
		GroupToAccountMapping g2AMapping = (GroupToAccountMapping)configuration.getGroupToAccountMapping( (String)g2AMappingsIt.next() );
		Collection userGroups = g2AMapping.getUserGroups();
		Iterator userGroupsIt = userGroups.iterator();
		while (userGroupsIt.hasNext()) {
			UserGroup userGroup = (UserGroup)configuration.getUserGroup( (String)userGroupsIt.next() );
			Collection accountMappers = g2AMapping.getAccountMappers();
			Iterator accountMappersIt = accountMappers.iterator();
			while (accountMappersIt.hasNext()) {
				AccountMapper accountMapper = (AccountMapper)configuration.getAccountMapper( (String)accountMappersIt.next() );
%>

	<tr>
		<%= (!cH2GMapping.getName().equals(lastH2GMapping) ? "<td bgcolor=\"#FFEE77\"><a href=\"hostToGroupMappings.jsp?command=edit&name="+cH2GMapping.getName()+"\">"+cH2GMapping.getName().replaceAll(",","<br>")+"</a></td>" : "<td></td>") %>
		<%= (!g2AMapping.getName().equals(lastG2AMapping) ? g2AMapping.toString("#FFEE77") : "<td></td><td></td><td></td>") %>
		<%= (!userGroup.getName().equals(lastUserGroup) ? userGroup.toString("#FFEE77") : "<td></td><td></td><td></td><td></td><td></td><td></td>") %>
		<%= accountMapper.toString("#FFEE77") %>
	</tr>
	
<%
				lastH2GMapping = cH2GMapping.getName();
				lastG2AMapping = g2AMapping.getName();
				lastUserGroup = userGroup.getName();
			}
		}
	}
}
%>

</tbody>
</table>

</div>
<%@include file="bottomNav.jspf"%>
</body>
</html>
