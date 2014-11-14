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
<h1><span>GUMS <%=gums.getVersion()%></span></h1>
<h3><span>GRID User Management System</h3>
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
Configures users in manual user groups.
</p>

<%

Map userGroups = configuration.getUserGroups();
String message = null;

if (request.getParameter("command")==null || 
	"save".equals(request.getParameter("command")) || 
	"delete".equals(request.getParameter("command"))) {
	
	if ("save".equals(request.getParameter("command"))) {
		ManualUserGroup manualUserGroup = (ManualUserGroup)userGroups.get(request.getParameter("userGroup"));
		try{
			gums.manualGroupAdd3(manualUserGroup.getName(), request.getParameter("dn"), request.getParameter("fqan"), request.getParameter("email"));
			message = "<div class=\"success\">User has been saved.</div>";
		}catch(Exception e){
			message = "<div class=\"failure\">Error saving user: " + e.getMessage() + "</div>";
		}
	}

	if ("delete".equals(request.getParameter("command"))) {
		ManualUserGroup manualUserGroup = (ManualUserGroup)userGroups.get(request.getParameter("userGroup"));
		try{
			gums.manualGroupRemove3(manualUserGroup.getName(), request.getParameter("dn"), request.getParameter("fqan"));
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
		if ((userGroup instanceof ManualUserGroup) && !userGroup.getName().equals(UserGroup.getDefaultBannedGroupName())) {
			ManualUserGroup manualUserGroup = (ManualUserGroup)userGroup;
			
			List users = manualUserGroup.getMemberList();
			if (users==null)
				continue;
				
			Iterator usersIt = users.iterator();
			while (usersIt.hasNext()) {
				GridUser user = (GridUser)usersIt.next();
				
				if ("/DC=com/DC=example/OU=People/CN=Example User 12345".equals(user.getCertificateDN()))
					continue;
%>
   	<tr>
		<td width="25" valign="top">
			<form action="manualUserGroups.jsp" method="get">
				<input type="submit" style="width:80px"  name="command" value="delete" onclick="if(!confirm('Are you sure you want to delete this user?'))return false;">
				<input type="hidden" name="dn" value="<%=user.getCertificateDN()%>">
				<input type="hidden" name="fqan" value="<%=user.getVoFQAN()!=null?user.getVoFQAN():""%>">
				<input type="hidden" name="email" value="<%=user.getEmail()!=null?user.getEmail():""%>">
				<input type="hidden" name="userGroup" value="<%=manualUserGroup.getName()%>">
			</form>
		</td>
  		<td align="left">
	   		<table class="userElement" width="100%">
	  			<tr>
		    		<td>
			    		DN: <%=user.getCertificateDN()%><br>
			    		User Group: <a href="userGroups.jsp?name=<%=manualUserGroup.getName()%>&command=edit"><%=manualUserGroup.getName()%></a><br>
			    		FQAN: <%=user.getVoFQAN()!=null?user.getVoFQAN():""%><br>
			    		Email: <%=user.getEmail()!=null?user.getEmail():""%><br>
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
				<div style="text-align: center;"><input type="submit" name="command" value="add"></div>
			</form>
	    </td>
	</tr>
  </table>
</form>
<%
}

else if ("add".equals(request.getParameter("command"))) {
	
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
	<input type="hidden" name="command" value="">
	<table id="form" border="0" cellpadding="2" cellspacing="2" align="center">
		<tr>
			<td nowrap style="text-align: right;">
				User group:
			</td>
			<td>
				<%=ConfigurationWebToolkit.createSelectBox("userGroup", manualUserGroups, request.getParameter("name"), null, manualUserGroups.size()>1)%> (required)
			</td>
		</tr>
		<tr>
    		<td nowrap style="text-align: right;">
	    		DN:
		    </td>
		    <td nowrap>
			    <input maxlength="256" size="64" name="dn" value=""/>  (regular expression)
		    </td>
		</tr>
		<tr>
    		<td nowrap style="text-align: right;">
	    		i.e.
		    </td>
		    <td nowrap>
			    /DC=org/DC=doegrids/OU=People/CN=Jane Doe 12345<br>
			    /DC=org/DC=doegrids/OU=People/CN=.* (match against all doegrids users)
		    </td>
		</tr>
		<tr>
    		<td nowrap style="text-align: right;">
	    		FQAN:
		    </td>
		    <td nowrap>
			    <input maxlength="256" size="64" name="fqan" value=""/> (optional regular expression)
		    </td>
		</tr>			
		<tr>
    		<td nowrap style="text-align: right;">
	    		i.e.
		    </td>
		    <td nowrap>
		        /myvo/mysubvo/Role=myrole<br>
			    {blank} (match against grid proxies only)<br>
			    .* (match against grid and extended proxies)<br>
				/myvo/.* (match against 'myvo' VO)<br>
				/myvo/myvogroup/.* (match against 'myvo' VO and 'myvogroup' group)
		    </td>
		</tr>		
		<tr>
    		<td nowrap style="text-align: right;">
	    		email:
		    </td>
		    <td nowrap>
			    <input maxlength="256" size="64" name="email" value=""/> (optional)
		    </td>
		</tr>		
		<tr>
	        <td colspan=2>
	        	<div style="text-align: center;">
	        		<button type="submit" onclick="document.forms[0].elements['command'].value='save'; return true;">save</button>
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
		
