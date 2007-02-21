<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@page import="gov.bnl.gums.*"%>
<jsp:useBean id="gums" scope="application" class="gov.bnl.gums.admin.GUMSAPIImpl" />
<%@ page import="gov.bnl.gums.account.*" %>
<%@ page import="gov.bnl.gums.groupToAccount.*" %>
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
<h2><span>User Groups</span></h2>
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
Configures user groups.
</p>

<%
String message = null;

if (request.getParameter("action")==null || 
	"save".equals(request.getParameter("action")) || 
	"delete".equals(request.getParameter("action"))) {
	
	if ("save".equals(request.getParameter("action"))) {
		Configuration newConfiguration = (Configuration)configuration.clone();
		try{
			newConfiguration.removeUserGroup( request.getParameter("name") );
			newConfiguration.addUserGroup( ConfigurationWebToolkit.parseUserGroup(request) );
			gums.setConfiguration(newConfiguration);
			configuration = gums.getConfiguration();
			message = "<div class=\"success\">User group has been saved.</div>";
		}catch(Exception e){
			message = "<div class=\"failure\">Error saving user group: " + e.getMessage() + "</div>";
		}
	}

	if ("delete".equals(request.getParameter("action"))) {
		Configuration newConfiguration = (Configuration)configuration.clone();
		try{
			String references = ConfigurationWebToolkit.getGroupToAccountMappingReferences(newConfiguration, request.getParameter("name"), "gov.bnl.gums.userGroup.UserGroup");
			if( references==null ) {
				if (newConfiguration.removeUserGroup( request.getParameter("name") )!=null) {
					gums.setConfiguration(newConfiguration);
					configuration = gums.getConfiguration();
					message = "<div class=\"success\">User group has been deleted.</div>";
				}
				else
					message = "<div class=\"failure\">Error deleting user group</div>";
			}
			else
				message = "<div class=\"failure\">You cannot delete this item until removing references to it within group to account mapping(s): " + references + "</div>";
		}catch(Exception e){
			message = "<div class=\"failure\">Error deleting user group: " + e.getMessage() + "</div>";
		}
	}
	
	Collection userGroups = configuration.getUserGroups().values();

	out.write(
	"<table id=\"form\" cellpadding=\"2\" cellspacing=\"2\">");

	if(message!=null)
		out.write( 
		"<tr><td colspan=\"2\">" + message + "</td></tr>" );
				
	Iterator userGroupsIt = userGroups.iterator();
	while(userGroupsIt.hasNext()) {
		UserGroup userGroup = userGroupsIt.hasNext() ? (UserGroup)userGroupsIt.next() : null;
		
%>
	   	<tr>
			<td width="50" valign="top">
				<form action="userGroups.jsp" method="get">
					<input type="image" src="images/Edit24.gif" name="action" value="edit">
					<input type="image" src="images/Remove24.gif" name="action" value="delete" onclick="if(!confirm('Are you sure you want to delete this user group?'))return false;">
					<input type="hidden" name="name" value="<%=userGroup.getName()%>">
				</form>
			</td>
	  		<td align="left">
		   		<table class="configElement" width="100%">
		  			<tr>
			    		<td>
				    		Check if member of user group
				    		 <span style="color:blue"><%=userGroup.getName()%></span>
<%
		if (userGroup instanceof ManualUserGroup) {
			out.write(		" by searching within this group in persistence factory" + 
							" <span style=\"color:blue\">" + ((ManualUserGroup)userGroup).getPersistenceFactory() + "</span>");
		} else if (userGroup instanceof LDAPUserGroup) {
			out.write(		" by querying LDAP server"+ 
							" <span style=\"color:blue\">" + ((LDAPUserGroup)userGroup).getServer() + "</span>"+
							" with query "+ 
							" <span style=\"color:blue\">" + ((LDAPUserGroup)userGroup).getQuery() + "</span>"+
							" and caching results in persistence factory "+ 
							" <span style=\"color:blue\">" + ((LDAPUserGroup)userGroup).getPersistenceFactory() + "</span>.");
		} else if (userGroup instanceof VOMSUserGroup) {
			out.write(		" by querying virtual organization " + "<span style=\"color:blue\">" + ((VOMSUserGroup)userGroup).getVirtualOrganization() + "</span>");

			if ( !((VOMSUserGroup)userGroup).getRemainderUrl().equals("") )
				out.write(	" at URL <span style=\"font-style:italic\">{base URL}</span><span style=\"color:blue\">" + ((VOMSUserGroup)userGroup).getRemainderUrl() + "</span>");

			out.write(	" where non-VOMS certificates are " + (((VOMSUserGroup)userGroup).isAcceptProxyWithoutFQAN() ? "<span style=\"color:blue\">" : "<span style=\"color:blue\">not ") + "accepted</span>");

			out.write(	" and VOMS certificate's VO " + (!((VOMSUserGroup)userGroup).isIgnoreFQAN() ? "<span style=\"color:blue\">must match" : "<span style=\"color:blue\">is ignored") + "</span>");

			if( !((VOMSUserGroup)userGroup).isIgnoreFQAN() ) {
				if ( !((VOMSUserGroup)userGroup).getVoGroup().equals("") )
					out.write(	" and group must match " + 
								" <span style=\"color:blue\">" + ((VOMSUserGroup)userGroup).getVoGroup() + "</span>");
		
				if ( !((VOMSUserGroup)userGroup).getVoRole().equals("") )
					out.write(	" and role must match " + 
								" <span style=\"color:blue\">" + ((VOMSUserGroup)userGroup).getVoRole() + "</span>");
			}
		}
%>
								. Members have <span style="color:blue"><%=userGroup.getAccess()%></span> access to GUMS.
						</td>
			      	</tr>
				</table>
			</td>
			<td width="10"></td>	
		</tr>
<%
	}
%>
		<tr>
	        <td colspan=2>
	        	<form action="userGroups.jsp" method="get">
	        		<div style="text-align: center;"><button type="submit" name="action" value="add">Add</button></div>
	        	</form>
	        </td>
		</tr>
	</table>
</form>
<%
}

else if ("edit".equals(request.getParameter("action"))
	|| "add".equals(request.getParameter("action"))
	|| "reload".equals(request.getParameter("action"))) {
	
	Collection userGroups = configuration.getUserGroups().values();
	
	UserGroup userGroup = null;
	
	ArrayList userGroupTypes = new ArrayList();
	userGroupTypes.add(LDAPUserGroup.getType());
	userGroupTypes.add(ManualUserGroup.getType());
	userGroupTypes.add(VOMSUserGroup.getType());
	
	if ("edit".equals(request.getParameter("action"))) {
		try {
			userGroup = (UserGroup)configuration.getUserGroups().get( request.getParameter("name") );
		} catch(Exception e) {
			out.write( "<div class=\"failure\">Error getting user group: " + e.getMessage() + "</div>" );
			return;
		}
	}

	if ("reload".equals(request.getParameter("action"))) {
		try{
			userGroup = ConfigurationWebToolkit.parseUserGroup(request);
		} catch(Exception e) {
			out.write( "<div class=\"failure\">Error reloading user group: " + e.getMessage() + "</div>" );
			return;
		}
	}
		
	else if ("add".equals(request.getParameter("action"))) {
		userGroup = new VOMSUserGroup(configuration);
	}		
		
%>
<form action="userGroups.jsp" method="get">
	<input type="hidden" name="action" value="">
	<input type="hidden" name="originalAction" value="<%=("reload".equals(request.getParameter("action")) ? request.getParameter("originalAction") : request.getParameter("action"))%>">
	<table id="form" border="0" cellpadding="2" cellspacing="2" align="center">
		<tr>
    		<td nowrap style="text-align: right;">
	    		Check if member of user group
		    </td>
		    <td nowrap>
<%
	if ("add".equals(request.getParameter("action")) || "add".equals(request.getParameter("originalAction"))) {
%>
		    	<input maxlength="256" size="32" name="name" value="<%=(userGroup.getName()!=null ? userGroup.getName() : "")%>"/>
		    </td>
		</tr
		<tr>
			<td nowrap style="text-align: right;">
	    		i.e.
		    </td>
		    <td nowrap>
				myUserGroup
		    </td>
		</tr>
<%
	}
	else {
%>
		    	<%=userGroup.getName()%>
		    	<input type="hidden" name="name" value="<%=userGroup.getName()%>"/>
		    </td>
		</tr>
		<tr>
    		<td nowrap style="text-align: right;">
	    		of type 
		    </td>
		    <td nowrap>
			<%=ConfigurationWebToolkit.createSelectBox("type", 
				userGroupTypes, 
				userGroup.getType(),
				"onchange=\"document.forms[0].elements['action'].value='reload';document.forms[0].submit();\"",
				false)%>
		    </td>
		</tr>
<%
	}
	if (userGroup instanceof ManualUserGroup) {
%>
		<tr>
			<td nowrap style=\"text-align: right;\">
				by searching within this group in persistence factory
			</td>
			<td>
				<%=ConfigurationWebToolkit.createSelectBox("persistenceFactory", 
						configuration.getPersistenceFactories().values(), 
						((ManualUserGroup)userGroup).getPersistenceFactory(),
						null,
						configuration.getPersistenceFactories().size()>1)%>
			</td>
		</tr>
<%
	} 
	else if (userGroup instanceof LDAPUserGroup) {
%>
		<tr>
			<td nowrap style="text-align: right;">
				by querying LDAP server
			</td>
			<td> 
				<input maxlength="256" size="32" name="server" value="<%=((LDAPUserGroup)userGroup).getServer()%>"/>
			</td>
		</tr>
	    <tr>
    		<td nowrap style="text-align: right;">
	    		i.e.
		    </td>
		    <td nowrap>
		    	grid-vo.nikhef.nl
		    </td>
		</tr>		
		<tr>
			<td nowrap style="text-align: right;">
				with query
			</td>
			<td> 
				<input maxlength="256" size="32" name="query" value="<%=((LDAPUserGroup)userGroup).getQuery()%>"/>
			</td>
		</tr>
	    <tr>
    		<td nowrap style="text-align: right;">
	    		i.e.
		    </td>
		    <td nowrap>
		    	ou=usatlas,o=atlas,dc=eu-datagrid,dc=org
		    </td>
		</tr>
		<tr>
			<td nowrap style="text-align: right;">
				and caching results in persistence factory
			</td>
			<td> 
				<%=ConfigurationWebToolkit.createSelectBox("persistenceFactory", 
						configuration.getPersistenceFactories().values(), 
						((LDAPUserGroup)userGroup).getPersistenceFactory(),
						null,
						configuration.getPersistenceFactories().values().size()>1)%>
			.</td>
		</tr>
		<tr>
			<td nowrap style="text-align: right;">
				Use SSL keystore (optional) 
			</td>
			<td> 
				<input maxlength="256" size="64" name="keyStore" value="<%=((LDAPUserGroup)userGroup).getKeyStore()%>"/>
			</td>
		</tr>
	    <tr>
    		<td nowrap style="text-align: right;">
	    		i.e.
		    </td>
		    <td nowrap>
		    	/absolute/path/to/keystore
		    </td>
		</tr>
		<tr>
			<td nowrap style="text-align: right;">
				with keystore password (optional)
			</td>
			<td> 
				<input maxlength="256" size="32" type="password" name="keyPassword" value="<%=((LDAPUserGroup)userGroup).getKeyPassword()%>"/>
			.</td>
		</tr>	
<%
	} 
	else if (userGroup instanceof VOMSUserGroup) {
%>
			<tr>
				<td nowrap style="text-align: right;">
					by querying virtual organization
				</td>
				<td>"
					<%=ConfigurationWebToolkit.createSelectBox("vo", 
							configuration.getVirtualOrganizations().values(), 
							((VOMSUserGroup)userGroup).getVirtualOrganization(),
							null,
							configuration.getVirtualOrganizations().values().size()>1)%>
				</td>
			</tr>
			<tr>
				<td nowrap style="text-align: right;">
					at URL
				</td>
				<td>
					{base URL}<input maxlength="256" size="32" name="url" value="<%=((VOMSUserGroup)userGroup).getRemainderUrl()%>"/>
				</td>
			</tr>
		    <tr>
	    		<td nowrap style="text-align: right;">
		    		i.e.
			    </td>
			    <td nowrap>
			    	/atlas/services/VOMSAdmin
			    </td>
			</tr>	
			<tr>
				<td nowrap style="text-align: right;">
					where non-VOMS certificates are
				</td>
				<td>
					<select name="nVOMS" onchange="document.forms[0].elements['action'].value='reload';document.forms[0].submit();">
						<option <%=(((VOMSUserGroup)userGroup).isAcceptProxyWithoutFQAN()?"selected":"")%>>allowed</option>
						<option <%=(((VOMSUserGroup)userGroup).isAcceptProxyWithoutFQAN()?"":"selected")%>>not allowed</option>
					</select>
				</td>
			</tr>
			<tr>
				<td nowrap style="text-align: right;">
					and VOMS certificate's VO
				</td>
				<td>
					<select name="VOMS" onchange="document.forms[0].elements['action'].value='reload';document.forms[0].submit();">
						<option <%=(((VOMSUserGroup)userGroup).isIgnoreFQAN()?"selected":"")%>>is ignored</option>
						<option <%=(((VOMSUserGroup)userGroup).isIgnoreFQAN()?"":"selected")%>>must match</option>
					</select><%=(((VOMSUserGroup)userGroup).isIgnoreFQAN() ? " ." : "")%>
				</td>
			</tr>

<%
		if( !((VOMSUserGroup)userGroup).isIgnoreFQAN() ) {
%>
			<tr>
				<td nowrap style="text-align: right;">
					and group must match (optional)
				</td>
				<td>"
					<input name="group" value="<%=((VOMSUserGroup)userGroup).getVoGroup()%>"/>
				</td>
			</tr>
			<tr>
				<td nowrap style="text-align: right;">
					and role must match (optional)
				</td>
				<td>"
					<input name="role" value="<%=((VOMSUserGroup)userGroup).getVoRole()%>"/>.
				</td>
			</tr>
<%
		}
	}
%>
			
			<tr>
				<td nowrap style="text-align: right;">
					Members have 
				</td>
				<td>
					<select name="access">
						<option <%=(userGroup.getAccess().equals("read self")?"selected":"")%>>read self</option>
						<option <%=(userGroup.getAccess().equals("read all")?"selected":"")%>>read all</option>
						<option <%=(userGroup.getAccess().equals("write")?"selected":"")%>>write</option>
					</select> access to GUMS.
				</td>
			</tr>
			<tr>
	        <td colspan=2>
				ConfigurationWebToolkit.createDoSubmit(userGroups, request)
	        	<div style="text-align: center;">
	        		<button type="submit" onclick="return doSubmit()">Save</button>
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
