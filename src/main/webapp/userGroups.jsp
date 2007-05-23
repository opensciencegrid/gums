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
<h1><span>GUMS <%=gums.getVersion()%></span></h1>
<h3><span>GRID User Management System</h3>
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
			<td width="55" valign="top">
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
<%
		if (userGroup instanceof ManualUserGroup) {
%>
				    		Manual User Group:
				    		<a href="userGroups.jsp?action=edit&name=<%=userGroup.getName()%>">
				    			<%=userGroup.getName()%>
				    		</a><br>
				    		Description: <%=userGroup.getDescription()%><br>	
				    		Persistence Factory:
							<a href="persistenceFactories.jsp?action=edit&name=<%=((ManualUserGroup)userGroup).getPersistenceFactory()%>">
								<%=((ManualUserGroup)userGroup).getPersistenceFactory()%>
							</a><br>
<%
		} else if (userGroup instanceof LDAPUserGroup) {
%>
				    		LDAP User Group:
				    		<a href="userGroups.jsp?action=edit&name=<%=userGroup.getName()%>">
				    			<%=userGroup.getName()%>
				    		</a><br>
				    		Description: <%=userGroup.getDescription()%><br>	
							LDAP Server: <%=((LDAPUserGroup)userGroup).getServer()%><br>	
							Query: <%=((LDAPUserGroup)userGroup).getQuery()%><br>
							Persistence factory:
							<a href="persistenceFactories.jsp?action=edit&name=<%=((LDAPUserGroup)userGroup).getPersistenceFactory()%>">
								<%=((LDAPUserGroup)userGroup).getPersistenceFactory()%>
							</a><br>
<%
		} else if (userGroup instanceof VOMSUserGroup) {
%>
				    		VOMS User Group:
				    		<a href="userGroups.jsp?action=edit&name=<%=userGroup.getName()%>">
				    			<%=userGroup.getName()%>
				    		</a><br>
				    		Description: <%=userGroup.getDescription()%><br>	
							VOMS Server:
							<a href="vomsServers.jsp?action=edit&name=<%=((VOMSUserGroup)userGroup).getVomsServer()%>">
								<%=((VOMSUserGroup)userGroup).getVomsServer()%>
							</a><br>			
<%
			if ( !((VOMSUserGroup)userGroup).getRemainderUrl().equals("") ) {
%>
							URL: {base url}<%=((VOMSUserGroup)userGroup).getRemainderUrl()%><br>
<%
			}
			
			out.write(	"Accept non-VOMS certificates: " + (((VOMSUserGroup)userGroup).isAcceptProxyWithoutFQAN() ? "true<br>" : "false<br>"));

%>
			VOMS certificate's FQAN is matched as: <%=((VOMSUserGroup)userGroup).getMatchFQAN()%><br>
<%
			if (!((VOMSUserGroup)userGroup).getVoGroup().equals("")) {
%>
				VO/Group: <%=((VOMSUserGroup)userGroup).getVoGroup()%><br>
<%
			}
			if (!((VOMSUserGroup)userGroup).getRole().equals("")) {
%>
				Role: <%=((VOMSUserGroup)userGroup).getRole()%><br>
<%
			}
		}
%>
								GUMS Access: <%=userGroup.getAccess()%>
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
	userGroupTypes.add(LDAPUserGroup.getTypeStatic());
	userGroupTypes.add(ManualUserGroup.getTypeStatic());
	userGroupTypes.add(VOMSUserGroup.getTypeStatic());
	
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
	    		Name:
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
<%
	}
%>
		<tr>
			<td nowrap style="text-align: right;">
	    		Description:
		    </td>
		    <td nowrap>
				<input name="description" size="64" value="<%=userGroup.getDescription()%>"/>
		    </td>
		</tr>	
		<tr>
    		<td nowrap style="text-align: right;">
	    		Type:
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

	if (userGroup instanceof ManualUserGroup) {
%>
		<tr>
			<td nowrap style="text-align: right;">
				persistence Factory:
			</td>
			<td>
				<%=ConfigurationWebToolkit.createSelectBox("persistenceFactory", 
						configuration.getPersistenceFactories().values(), 
						((ManualUserGroup)userGroup).getPersistenceFactory(),
						null,
						false)%> (where to search for individual users)
			</td>
		</tr>
<%
	} 
	else if (userGroup instanceof LDAPUserGroup) {
%>
		<tr>
			<td nowrap style="text-align: right;">
				LDAP server:
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
				Query:
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
				Persistence Factory
			</td>
			<td> 
				<%=ConfigurationWebToolkit.createSelectBox("persistenceFactory", 
						configuration.getPersistenceFactories().values(), 
						((LDAPUserGroup)userGroup).getPersistenceFactory(),
						null,
						false)%> (where to cache users)
			</td>
		</tr>
		<tr>
			<td nowrap style="text-align: right;">
				LDAP DN Field 
			</td>
			<td> 
				<input maxlength="256" size="16" name="certDNField" value="<%=((LDAPUserGroup)userGroup).getCertDNField()%>"/>  (which field certificate DN is in)
			</td>
		</tr>
	    <tr>
    		<td nowrap style="text-align: right;">
	    		i.e.
		    </td>
		    <td nowrap>
		    	description
		    </td>
		</tr>
<%
	} 
	else if (userGroup instanceof VOMSUserGroup) {
%>
			<tr>
				<td nowrap style="text-align: right;">
					VOMS Server:
				</td>
				<td>
					<%=ConfigurationWebToolkit.createSelectBox("vOrg", 
							configuration.getVomsServers().values(), 
							((VOMSUserGroup)userGroup).getVomsServer(),
							null,
							false)%>
				</td>
			</tr>
			<tr>
				<td nowrap style="text-align: right;">
					Remainder URL:
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
					Accept non-VOMS certificates:
				</td>
				<td>
					<select name="nVOMS" onchange="document.forms[0].elements['action'].value='reload';document.forms[0].submit();">
						<option <%=(((VOMSUserGroup)userGroup).isAcceptProxyWithoutFQAN()?"selected":"")%>>true</option>
						<option <%=(((VOMSUserGroup)userGroup).isAcceptProxyWithoutFQAN()?"":"selected")%>>false</option>
					</select>
				</td>
			</tr>
			<tr>
				<td nowrap style="text-align: right;">
					Match VOMS certificate's FQAN as:
				</td>
				<td>
					<%=ConfigurationWebToolkit.createSelectBox("matchFQAN", 
							VOMSUserGroup.getMatchFQANTypes(), 
							((VOMSUserGroup)userGroup).getMatchFQAN(),
							null,
							false)%>
				</td>
			</tr>
			<tr>
				<td nowrap style="text-align: right;">
					VO/Group:
				</td>
				<td>
					<input name="vogroup" value="<%=((VOMSUserGroup)userGroup).getVoGroup()%>"/> (optional)
				</td>
			</tr>
		    <tr>
	    		<td nowrap style="text-align: right;">
		    		i.e.
			    </td>
			    <td nowrap>
			    	/atlas/usatlas
			    </td>
			</tr>
			<tr>
				<td nowrap style="text-align: right;">
					Role:
				</td>
				<td>
					<input name="role" value="<%=((VOMSUserGroup)userGroup).getRole()%>"/> (optional)
				</td>
			</tr>
		    <tr>
	    		<td nowrap style="text-align: right;">
		    		i.e.
			    </td>
			    <td nowrap>
			    	production
			    </td>
			</tr>
<%
	}
%>
			
			<tr>
				<td nowrap style="text-align: right;">
					GUMS Access:
				</td>
				<td>
					<select name="access">
						<option <%=(userGroup.getAccess().equals("read self")?"selected":"")%>>read self</option>
						<option <%=(userGroup.getAccess().equals("read all")?"selected":"")%>>read all</option>
						<option <%=(userGroup.getAccess().equals("write")?"selected":"")%>>write</option>
					</select> (GUMS access by members of this user group)
				</td>
			</tr>
			<tr>
	        <td colspan=2>
				<%=ConfigurationWebToolkit.createDoSubmit(userGroups, request)%>
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
