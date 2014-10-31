<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@page import="gov.bnl.gums.*"%>
<jsp:useBean id="gums" scope="application" class="gov.bnl.gums.admin.GUMSAPIImpl" />
<%@ page import="gov.bnl.gums.account.*" %>
<%@ page import="gov.bnl.gums.userGroup.*" %>
<%@ page import="gov.bnl.gums.configuration.*" %>
<%@ page import="gov.bnl.gums.service.ConfigurationWebToolkit" %>
<%@ page import="java.util.*" %>
<%@ page import="org.apache.commons.lang.StringEscapeUtils" %>
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
<h3><span>GRID User Management System</span></h3>
<h2><span>Ban Users</span></h2>
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
Configure the server's banned users.
</p>

<%
String message = null;

if (request.getParameter("command")==null || 
	"save".equals(request.getParameter("command")) || 
	"delete".equals(request.getParameter("command"))) {
	
	if ("save".equals(request.getParameter("command"))) {
		try {
			ManualUserGroup group = configuration.getDefaultBannedGroup();
			group.addMember(new GridUser(request.getParameter("dn"), null));
			message = "<div class=\"success\">User " + StringEscapeUtils.escapeHtml(request.getParameter("dn")) + " has been added to the ban list.</div>";
		} catch(Exception e) {
			message = "<div class=\"failure\">" + StringEscapeUtils.escapeHtml("Error adding user " + request.getParameter("dn") + " to the ban list: " + e.getMessage()) + "</div>";
		}
	}

	if ("delete".equals(request.getParameter("command"))) {
		try {
			ManualUserGroup group = configuration.getDefaultBannedGroup();
			group.removeMember(new GridUser(request.getParameter("dn"), null));
			message = "<div class=\"success\">User " + StringEscapeUtils.escapeHtml(request.getParameter("dn")) + " has been removed from ban list.</div>";
		}catch(Exception e){
			message = "<div class=\"failure\">" + StringEscapeUtils.escapeHtml("Error deleting user " + request.getParameter("dn") + " from the ban list: " + e.getMessage()) + "</div>";
		}
	}

	Set<UserGroup> bannedGroups = new HashSet<UserGroup>();
	Set<UserGroup> manualUserGroups = new HashSet<UserGroup>();
	Set<UserGroup> bannedUserGroups = new HashSet<UserGroup>();
	List<String> groups = configuration.getBannedUserGroupList();
	for (String groupName : groups) {
		UserGroup group = configuration.getUserGroup(groupName);
		if (group != null) {
			bannedGroups.add(group);
			manualUserGroups.add(group);
		}
	}
	for (UserGroup group : configuration.getUserGroups().values()) {
		if (group instanceof BannedUserGroup) {
			bannedGroups.add(group);
			bannedUserGroups.add(group);
		}
	}

%>
<table id="form" cellpadding="2" cellspacing="2">
<%

	if (message != null) {
		out.write( "<tr><td colspan=\"2\">" + message + "</td></tr>" );
	}
%>

	<tr>
		<form action="banning.jsp" method="get">
			<td>
				<div style="text-align: center;"><input style="width:80px" type="submit" name="command" value="save" /></div>
			</td>
			<td align="left">
				Add a new DN to the banned user list here:<br/>
				<input maxlength="256" size="100" name="dn"/>
			</td>
			<td width="10"></td>
		</form>
	</tr>

<%
	for (UserGroup group : bannedGroups) {
		for (GridUser user : group.getMemberList()) {
			if (group.getName().equals(UserGroup.getDefaultBannedGroupName()))
			{
%>
				<tr>
					<td width="1" valign="top">
						<form action="banning.jsp" method="get">
							<input type="submit" style="width:80px" name="command" value="delete"
							 onclick="if(!confirm('Are you sure you want to delete this user from the banned list?'))return false;"/>
							<input type="hidden" name="dn" value="<%=user.getCertificateDN()%>"/>
						</form>
					</td>
					<td align="left">
						<table class="configElement" width="100%">
							<tr>
								<td>
									User: <%=user.getCertificateDN()%><br/>
									Group: <%=group.getName()%><br/>
								</td>
							</tr>
						</table>
					</td>
					<td width="10"></td>
				</tr>
<%
			}
		}
	}

%>
</table>

<table id="form2" cellpadding="2" cellspacing="2">
<%
	for (UserGroup group : manualUserGroups) {
		for (GridUser user : group.getMemberList()) {
%>
			<tr>
				<td width="1" valign="top">
					<form action="userGroups.jsp" method="get">
						<input type="submit" style="width:80px" name="command" value="edit">
					</form>
				</td>
				<td align="left">
					<table class="configElement" width="100%">
						<tr>
							<td>
								User: <%=user.getCertificateDN()%><br/>
								Group: <%=group.getName()%><br/>
							</td>
						</tr>
					</table>
				</td>
				<td width="10"></td>
			</tr>
<%
		}
	}
%>
</table>

<table id="form3" cellpadding="2" cellspacing="2">
<%
	for (UserGroup group : bannedUserGroups) {
		for (GridUser user : group.getMemberList()) {
%>
			<tr>
				<td width="1" valign="top">
					<form action="userGroups.jsp" method="get">
						<input type="submit" style="width:80px" name="command" value="edit">
					</form>
				</td>
				<td align="left">
					<table class="configElement" width="100%">
						<tr>
							<td>
								User: <%=user.getCertificateDN()%><br/>
								Group: <%=group.getName()%><br/>
							</td>
						</tr>
					</table>
				</td>
				<td width="10"></td>
			</tr>
<%
		}
	}
%>
</table>
<%
}
%>

</div>
<%@include file="bottomNav.jspf"%>
</body>
</html>
