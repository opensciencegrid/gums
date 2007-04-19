<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@page import="gov.bnl.gums.*"%>
<jsp:useBean id="gums" scope="application" class="gov.bnl.gums.admin.GUMSAPIImpl" />
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
<h1><span>GUMS</span></h1>
<h2><span>Manual User Group Members</span></h2>
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
Configures users in manual user groups.
</p>

<%

Map userGroups = configuration.getUserGroups();
String message = null;

if (request.getParameter("action")==null || 
	"save".equals(request.getParameter("action")) || 
	"delete".equals(request.getParameter("action"))) {
	
	if ("save".equals(request.getParameter("action"))) {
		ManualUserGroup manualUserGroup = (ManualUserGroup)userGroups.get(request.getParameter("userGroup"));
		try{
			gums.manualGroupAdd(manualUserGroup.getName(), request.getParameter("dn"));
			message = "<div class=\"success\">User has been saved.</div>";
		}catch(Exception e){
			message = "<div class=\"failure\">Error saving user: " + e.getMessage() + "</div>";
		}
	}

	if ("delete".equals(request.getParameter("action"))) {
		ManualUserGroup manualUserGroup = (ManualUserGroup)userGroups.get(request.getParameter("userGroup"));
		try{
			gums.manualGroupRemove(manualUserGroup.getName(), request.getParameter("dn"));
			message = "<div class=\"success\">User has been deleted.</div>";
		}catch(Exception e){
			message = "<div class=\"failure\">Error deleting user: " + e.getMessage() + "</div>";
		}
	}

	out.write(
"<table id=\"form\" cellpadding=\"2\" cellspacing=\"2\">");

	if(message!=null)
		out.write( 
	"<tr><td colspan=\"2\">" + message + "</td></tr>" );
				
	Iterator userGroupsIt = userGroups.values().iterator();
	while(userGroupsIt.hasNext()) {
		UserGroup userGroup = (UserGroup)userGroupsIt.next();
		if (userGroup instanceof ManualUserGroup) {
			ManualUserGroup manualUserGroup = (ManualUserGroup)userGroup;
			
			List users = manualUserGroup.getMemberList();
			if (users==null)
				continue;
				
			Iterator usersIt = users.iterator();
			while (usersIt.hasNext()) {
				GridUser user = (GridUser)usersIt.next();
%>
   	<tr>
		<td width="25" valign="top">
			<form action="manualUserGroups.jsp" method="get">
				<input type="image" src="images/Remove24.gif" name="action" value="delete" onclick="if(!confirm('Are you sure you want to delete this user?'))return false;">
				<input type="hidden" name="dn" value="<%=user.getCertificateDN()%>">
				<input type="hidden" name="userGroup" value="<%=manualUserGroup.getName()%>">
			</form>
		</td>
  		<td align="left">
	   		<table class="userElement" width="100%">
	  			<tr>
		    		<td>
			    		DN: <%=user.getCertificateDN()%><br>
			    		user group: <a href="userGroups.jsp?name=<%=manualUserGroup.getName()%>&action=edit"><%=manualUserGroup.getName()%></a><br>
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
	<tr>
		<td colspan=2>
			<form action="manualUserGroups.jsp" method="get">
				<div style="text-align: center;"><button type="submit" name="action" value="add">Add</button></div>
			</form>
	    </td>
	</tr>
  </table>
</form>
<%
}

else if ("add".equals(request.getParameter("action"))) {
	
	GridUser user = new GridUser();
	
	ArrayList manualUserGroups = new ArrayList();
	Iterator userGroupIt = userGroups.values().iterator();
	while(userGroupIt.hasNext()) {
		UserGroup userGroup = (UserGroup)userGroupIt.next();
		if (userGroup instanceof ManualUserGroup)
			manualUserGroups.add(userGroup.getName());
	}
%>
<form action="manualUserGroups.jsp" method="get">
	<input type="hidden" name="action" value="">
	<table id="form" border="0" cellpadding="2" cellspacing="2" align="center">
		<tr>
    		<td nowrap style="text-align: right;">
	    		User with DN 
		    </td>
		    <td nowrap>
			    <input maxlength="256" size="64" name="dn" value=""/>
		    </td>
		</tr>
		<tr>
    		<td nowrap style="text-align: right;">
	    		i.e.
		    </td>
		    <td nowrap>
			    /DC=org/DC=doegrids/OU=People/CN=Jane Doe 12345
		    </td>
		</tr>
		<tr>
			<td nowrap style="text-align: right;">
				is member of user group
			</td>
			<td>
				<%=ConfigurationWebToolkit.createSelectBox("userGroup", manualUserGroups, null, null, manualUserGroups.size()>1)%>
			</td>
		</tr>
		<tr>
	        <td colspan=2>
	        	<div style="text-align: center;">
	        		<button type="submit" onclick="document.forms[0].elements['action'].value='save'; return true;">Add</button>
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
		