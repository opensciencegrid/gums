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
Configures user groups.
</p>

<%
String message = null;

if (request.getParameter("command")==null || 
	"save".equals(request.getParameter("command")) || 
	"delete".equals(request.getParameter("command"))) {
	
	if ("save".equals(request.getParameter("command"))) {
		Configuration newConfiguration = (Configuration)configuration.clone();
		try{
			newConfiguration.removeUserGroup( request.getParameter("name") );
			newConfiguration.addUserGroup( ConfigurationWebToolkit.parseUserGroup(request) );
			gums.setConfiguration(newConfiguration);
			configuration = gums.getConfiguration();
			message = "<div class=\"success\">User group has been saved.</div>";
		}catch(Exception e){
			e.printStackTrace();
			message = "<div class=\"failure\">Error saving user group: " + e.getMessage() + "</div>";
		}
	}

	if ("delete".equals(request.getParameter("command"))) {
		Configuration newConfiguration = (Configuration)configuration.clone();
		try{
			String references = ConfigurationWebToolkit.getGroupToAccountMappingReferences(newConfiguration, request.getParameter("name"), "gov.bnl.gums.userGroup.UserGroup");
			if( references==null ) {
				if (!configuration.getBannedUserGroupList().contains(request.getParameter("name"))) {
					if (newConfiguration.removeUserGroup( request.getParameter("name") )!=null) {
						gums.setConfiguration(newConfiguration);
						configuration = gums.getConfiguration();
						message = "<div class=\"success\">User group has been deleted.</div>";
					}
					else
						message = "<div class=\"failure\">Error deleting user group</div>";
				}
				else
					message = "<div class=\"failure\">You cannot delete this item until removing it from the banned user list</div>";
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
			<td width="1" valign="top">
				<form action="userGroups.jsp" method="get">
					<input type="submit" style="width:80px" name="command" value="edit">
					<input type="submit" style="width:80px" name="command" value="delete" onclick="if(!confirm('Are you sure you want to delete this user group?'))return false;">
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
				    		<a href="userGroups.jsp?command=edit&name=<%=userGroup.getName()%>">
				    			<%=userGroup.getName()%>
				    		</a><br>
				    		Description: <%=userGroup.getDescription()%><br>	
				    		Persistence Factory:
							<a href="persistenceFactories.jsp?command=edit&name=<%=((ManualUserGroup)userGroup).getPersistenceFactory()%>">
								<%=((ManualUserGroup)userGroup).getPersistenceFactory()%>
							</a><br>
							<%
							if (((ManualUserGroup)userGroup).getMembersUri()!=null) {
								out.write("Members URI: " + ((ManualUserGroup)userGroup).getMembersUri() + "<br>");
							}
							if (((ManualUserGroup)userGroup).getNonMembersUri()!=null) {
								out.write("Non-members URI: " + ((ManualUserGroup)userGroup).getNonMembersUri() + "<br>");
							}%>
<%
		} else if (userGroup instanceof LDAPUserGroup) {
%>
				    		LDAP User Group:
				    		<a href="userGroups.jsp?command=edit&name=<%=userGroup.getName()%>">
				    			<%=userGroup.getName()%>
				    		</a><br>
				    		Description: <%=userGroup.getDescription()%><br>	
							LDAP Server: <%=((LDAPUserGroup)userGroup).getServer()%><br>	
							People Tree: <%=((LDAPUserGroup)userGroup).getPeopleTree()%><br>
							Group Tree: <%=((LDAPUserGroup)userGroup).getGroupTree()%><br>
							Persistence factory:
							<a href="persistenceFactories.jsp?command=edit&name=<%=((LDAPUserGroup)userGroup).getPersistenceFactory()%>">
								<%=((LDAPUserGroup)userGroup).getPersistenceFactory()%>
							</a><br>
<%
		} else if (userGroup instanceof VOMSUserGroup) {
%>
				    		VOMS User Group:
				    		<a href="userGroups.jsp?command=edit&name=<%=userGroup.getName()%>">
				    			<%=userGroup.getName()%>
				    		</a><br>
				    		Description: <%=userGroup.getDescription()%><br>	
							VOMS Server:
							<a href="vomsServers.jsp?command=edit&name=<%=((VOMSUserGroup)userGroup).getVomsServer()%>">
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
		} else if (userGroup instanceof ArgusBannedUserGroup) {
%>
						Argus-based banned user group:
						<a href="userGroups.jsp?command=edit&name=<%=userGroup.getName()%>">
							<%=userGroup.getName()%>
						</a><br>
						Description: <%=userGroup.getDescription()%><br>
						Persistence Factory:
						<a href="persistenceFactories.jsp?command=edit&name=<%=((ArgusBannedUserGroup)userGroup).getPersistenceFactory()%>">
							<%=((ArgusBannedUserGroup)userGroup).getPersistenceFactory()%>
						</a><br>
						Argus endpoint: <%=((ArgusBannedUserGroup)userGroup).getArgusEndpoint()%><br>
<!--
						SSL Key: <%=((ArgusBannedUserGroup)userGroup).getSslKey()%><br>
						SSL Cert File: <%=((ArgusBannedUserGroup)userGroup).getSslCertfile()%><br>
						SSL Key Password: <%=((ArgusBannedUserGroup)userGroup).getSslKeyPasswd()%><br>
						SSL CA Files: <%=((ArgusBannedUserGroup)userGroup).getSslCAFiles()%><br>
-->
<%
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
	        		<div style="text-align: center;"><input type="submit" name="command" value="add"></div>
	        	</form>
	        </td>
		</tr>
	</table>
</form>
<%
}

else if ("edit".equals(request.getParameter("command"))
	|| "add".equals(request.getParameter("command"))
	|| "reload".equals(request.getParameter("command"))) {
	
	Collection userGroups = configuration.getUserGroups().values();
	
	UserGroup userGroup = null;
	
	ArrayList userGroupTypes = new ArrayList();
	userGroupTypes.add(LDAPUserGroup.getTypeStatic());
	userGroupTypes.add(ManualUserGroup.getTypeStatic());
	userGroupTypes.add(VOMSUserGroup.getTypeStatic());
	userGroupTypes.add(ArgusBannedUserGroup.getTypeStatic());
	
	if ("edit".equals(request.getParameter("command"))) {
		try {
			userGroup = (UserGroup)configuration.getUserGroups().get( request.getParameter("name") );
		} catch(Exception e) {
			out.write( "<div class=\"failure\">Error getting user group: " + e.getMessage() + "</div>" );
			return;
		}
	}

	if ("reload".equals(request.getParameter("command"))) {
		try{
			userGroup = ConfigurationWebToolkit.parseUserGroup(request);
		} catch(Exception e) {
			out.write( "<div class=\"failure\">Error reloading user group: " + e.getMessage() + "</div>" );
			return;
		}
	}
		
	else if ("add".equals(request.getParameter("command"))) {
		userGroup = new VOMSUserGroup(configuration);
	}		
		
%>
<form action="userGroups.jsp" method="get">
	<input type="hidden" name="command" value="">
	<input type="hidden" name="originalCommand" value="<%=("reload".equals(request.getParameter("command")) ? request.getParameter("originalCommand") : request.getParameter("command"))%>">
	<table id="form" border="0" cellpadding="2" cellspacing="2" align="center">
		<tr>
    		<td nowrap style="text-align: right;">
	    		Name:
		    </td>
		    <td nowrap>
<%
	if ("add".equals(request.getParameter("command")) || "add".equals(request.getParameter("originalCommand"))) {
%>
		    	<input maxlength="256" size="32" name="name" value="<%=(userGroup.getName()!=null ? userGroup.getName() : "")%>"/>
		    </td>
		</tr>
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
			<%=ConfigurationWebToolkit.createSelectBox("ug_type", 
				userGroupTypes, 
				userGroup.getType(),
				"onchange=\"document.forms[0].elements['command'].value='reload';document.forms[0].submit();\"",
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
		<tr>
			<td nowrap style="text-align: right;">
				Members URI:
			</td>
			<td> 
				<input maxlength="256" size="32" name="membersUri" value="<%=((ManualUserGroup)userGroup).getMembersUri()%>"/> (optional)
			</td>
		</tr>
	    <tr>
    		<td nowrap style="text-align: right;">
	    		i.e.
		    </td>
		    <td nowrap>
		    	file:///etc/grid-security/banned_users (only file URIs currently supported)
		    </td>
		</tr>	
		<tr>
			<td nowrap style="text-align: right;">
				Non-members URI:
			</td>
			<td> 
				<input maxlength="256" size="32" name="nonMembersUri" value="<%=((ManualUserGroup)userGroup).getNonMembersUri()%>"/> (optional)
			</td>
		</tr>
	    <tr>
    		<td nowrap style="text-align: right;">
	    		i.e.
		    </td>
		    <td nowrap>
		    	file:///etc/grid-security/unbanned_users (only file URIs currently supported)
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
				People Tree:
			</td>
			<td> 
				<input maxlength="256" size="64" name="peopleTree" value="<%=((LDAPUserGroup)userGroup).getPeopleTree()%>"/>
			</td>
		</tr>
	    <tr>
    		<td nowrap style="text-align: right;">
	    		i.e.
		    </td>
		    <td nowrap>
		    	ou=People,ou=usatlas,o=atlas,dc=eu-datagrid,dc=org
		    </td>
		</tr>
                <tr>
                        <td nowrap style="text-align: right;">
                                Group Tree:
                        </td>
                        <td>
                                <input maxlength="256" size="64" name="groupTree" value="<%=((LDAPUserGroup)userGroup).getGroupTree()%>"/>
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
                                LDAP Member Field
                        </td>
                        <td>
                                <input maxlength="256" size="16" name="memberUidField" value="<%=((LDAPUserGroup)userGroup).getMemberUidField()%>"/>  (which field contains member UID within group)
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
							true)%> (Choose none if FQAN is cryptographically validated at the gatekeeper)
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
			    	/atlas  i.e. the VO name.
			    </td>
			</tr>
			<tr>
				<td nowrap style="text-align: right;">
					Accept non-VOMS certificates:
				</td>
				<td>
					<select name="nVOMS" onchange="document.forms[0].elements['command'].value='reload';document.forms[0].submit();">
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
					<input name="vogroup" value="<%=((VOMSUserGroup)userGroup).getVoGroup()%>"/> (Must be specified unless above is "ignore")
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
	else if (userGroup instanceof ArgusBannedUserGroup) {
%>
			<tr>
				<td nowrap style="text-align: right;">
					Argus Endpoint:
				</td>
				<td>
					<input maxlength="256" size="100" name="url" value="<%=((ArgusBannedUserGroup)userGroup).getArgusEndpoint()%>"/>
				</td>
			</tr>
			<tr>
				<td nowrap style="text-align: right;">
					i.e.
				</td>
				<td>
					https://argus.example.com:8150/pap/services/XACMLPolicyManagementService
				</td>
			</tr>
		<tr>
			<td nowrap style="text-align: right;">
				Persistence Factory:
			</td>
			<td>
				<%=ConfigurationWebToolkit.createSelectBox("persistenceFactory", 
						configuration.getPersistenceFactories().values(), 
						((ArgusBannedUserGroup)userGroup).getPersistenceFactory(),
						null,
						false)%> (for caching individual users)
			</td>
		</tr>
		<tr>
			<td nowrap style="text-align: right;">
				SSL Key:
			</td>
			<td>
				<input maxlength="256" size="64" name="sslKey" value="<%=((ArgusBannedUserGroup)userGroup).getSslKey()%>"/>
			</td>
		</tr>
		<tr>
			<td nowrap style="text-align: right;">
				i.e.
			</td>
			<td nowrap>
				/etc/grid-security/http/httpkey.pem
			</td>
		</tr>	
		<tr>
			<td nowrap style="text-align: right;">
				SSL Cert File:
			</td>
			<td>
				<input maxlength="256" size="64" name="sslCert" value="<%=((ArgusBannedUserGroup)userGroup).getSslCertfile()%>"/>
			</td>
		</tr>
		<tr>
			<td nowrap style="text-align: right;">
				i.e.
			</td>
			<td nowrap>
				/etc/grid-security/http/httpcert.pem
			</td>
		</tr>
		<tr>
			<td nowrap style="text-align: right;">
				SSL Key Password:
			</td>
			<td>
				<input type="password" maxlength="256" size="64" name="sslKeyPW" value="<%=((ArgusBannedUserGroup)userGroup).getSslKeyPasswd()%>"/> (leave blank if none)
			</td>
		</tr>
		<tr>
			<td nowrap style="text-align: right;">
				SSL CA Files:
			</td>
			<td>
				<input maxlength="256" size="64" name="sslCA" value="<%=((ArgusBannedUserGroup)userGroup).getSslCAFiles()%>"/>
			</td>
		</tr>
		<tr>
			<td nowrap style="text-align: right;">
				i.e.
			</td>
			<td nowrap>
				/etc/grid-security/certificates/*.0
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
	        		<button type="submit" onclick="return doSubmit()">save</button>
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
