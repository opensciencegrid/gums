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
<h2><span>Shortcut</span></h2>
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
Shortcut for adding multiple related elements in one step.
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
	<p><div class="failure">You must first create a 'host to group mapping'.</div></p>
	</div>
	<%@include file="bottomNav.jspf"%>
	</body>
	</html>
<%
	return;
}
String message = null;
if ( "save".equals(request.getParameter("command")) ) {
	
	if ("save".equals(request.getParameter("command"))) {
		Configuration newConfiguration = (Configuration)configuration.clone();
		try{
			newConfiguration.removeUserGroup( request.getParameter("name") );
			newConfiguration.addUserGroup( ConfigurationWebToolkit.parseUserGroup(request) );
			newConfiguration.removeAccountMapper( request.getParameter("name") );
			newConfiguration.addAccountMapper( ConfigurationWebToolkit.parseAccountMapper(request) );
			newConfiguration.removeGroupToAccountMapping( request.getParameter("name") );
			newConfiguration.addGroupToAccountMapping( ConfigurationWebToolkit.parseGroupToAccountMapping(request) );			
		 	newConfiguration.getHostToGroupMapping( request.getParameter("hg_name") ).addGroupToAccountMapping( request.getParameter("name") );
			gums.setConfiguration(newConfiguration);
			message = "<div class=\"success\">Elements have been saved.</div>";
		}catch(Exception e){
			message = "<div class=\"failure\">Error saving elements: " + e.getMessage() + "</div>";
		}
	}
}
else {
        ArrayList types = new ArrayList();
        types.add("VOMS / group account");	
%>
<form action="shortcut.jsp" method="get">
	<table id="form" border="0" cellpadding="2" cellspacing="2" align="center">
        <tr>
            <td nowrap style="text-align: right;">
				Multi-element Type:
            </td>
            <td>
                <%=ConfigurationWebToolkit.createSelectBox("types",
                    types,
                    null,
                    "onchange=\"document.forms[0].submit();\"",
                    false)%>
			</td>
        </tr>

<%
	if (request.getParameter("type")==null || request.getParameter("type").equals("VOMS with group account")) {
%>
		<input type="hidden" name="am_type" value="<%=GroupAccountMapper.getTypeStatic()%>">
		<input type="hidden" name="ug_type" value="<%=VOMSUserGroup.getTypeStatic()%>">
		<tr>
			<td nowrap style="text-align: right;">
	    		Name:
		    </td>
		    <td nowrap>
		    	<input maxlength="256" size="32" name="name"/>
		    </td>
		</tr>
		<tr>
            <td nowrap style="text-align: right;">
				VOMS URL:
            </td>
            <td>
				<input maxlength="256" size="64" name="baseURL"/> (Leave blank if FQAN is cryptographically validated at the gatekeeper)
            </td>
        </tr>
        <tr>
            <td nowrap style="text-align: right;">
                i.e.
            </td>
            <td nowrap>
                https://lcg-voms.cern.ch:8443/voms/atlas/services/VOMSAdmin
            </td>
        </tr>
        <tr>
            <td nowrap style="text-align: right;">
				Persistence Factory
            </td>
            <td>
                <%=ConfigurationWebToolkit.createSelectBox("persistenceFactory",
                    configuration.getPersistenceFactories().values(),
                    null,
                    null,
                    false)%> (where to cache users)
            </td>
        </tr>
        <tr>
            <td nowrap style="text-align: right;">
				VO/Group:
            </td>
            <td>
                   <input name="vogroup"/> (optional)
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
				<input name="role"/> (optional)
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
        <tr>
             <td nowrap style="text-align: right;">
				Accept non-VOMS certificates:
             </td>
             <td>
                <select name="nVOMS" onchange="document.forms[0].elements['command'].value='reload';document.forms[0].submit();">
                        <option selected>true</option>
                        <option>false</option>
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
                    null,
                    null,
                    false)%>
            </td>
        </tr>
        <tr>
			<td nowrap style="text-align: right;">Accounting VO Subgroup: </td>
            <td nowrap><input maxlength="256" size="16" name="accVoSub"/> (to be included in OSG-user-VO-map)</td>
        </tr>
        <tr>
			<td nowrap style="text-align: right;">Accounting VO: </td>
            <td nowrap><input maxlength="256" size="16" name="accVo"/> (to be included in OSG-user-VO-map)</td>
        </tr>
        <tr>
			<td nowrap style="text-align: right;">Host To Group Mapping: </td>
			<td>
			    <%=ConfigurationWebToolkit.createSelectBox("hg_name",
                    configuration.getHostToGroupMappings(),
                    null,
                    null,
                    true)%>
			</td>  
		</tr>
		</tr>
        <tr>
			<td nowrap style="text-align: right;">Account Name: </td>
            <td nowrap><input maxlength="256" size="16" name="accountName"/></td>
        </tr>			
		<tr>
            <td colspan=2>
				<div style="text-align: center;">
					<%=ConfigurationWebToolkit.createDoSubmit(configuration.getHostToGroupMappings(), request)%>
					<div style="text-align: center;">
					        <button type="submit" name="command" value="save">save</button>
					</div>
				</div>
            </td>	
<%
	}	
%>
	</table>
</form>
<%
}
%>

</div>
<%@include file="bottomNav.jspf"%>
</body>
</html>
