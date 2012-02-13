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
<h2><span>Group To Account</span></h2>
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
if (configuration.getUserGroups().size()==0) {
%>
	<p><div class="failure">You must first create a user group.</div></p>
	</div>
	<%@include file="bottomNav.jspf"%>
	</body>
	</html>
<%
	return;
}
if (configuration.getAccountMappers().size()==0) {
%>
	<p><div class="failure">You must first create an account mapper.</div></p>
	</div>
	<%@include file="bottomNav.jspf"%>
	</body>
	</html>
<%
	return;
}
%>

<p>
Configures group to account mappings.
</p>

<%
String message = null;

if (request.getParameter("command")==null || 
	"save".equals(request.getParameter("command")) || 
	"delete".equals(request.getParameter("command"))) {
	
	if ("save".equals(request.getParameter("command"))) {
		Configuration newConfiguration = (Configuration)configuration.clone();
		try{
			newConfiguration.removeGroupToAccountMapping( request.getParameter("name") );
			newConfiguration.addGroupToAccountMapping( ConfigurationWebToolkit.parseGroupToAccountMapping(request) );
			gums.setConfiguration(newConfiguration);
			configuration = gums.getConfiguration();
			message = "<div class=\"success\">Group to account mapping has been saved.</div>";
		}catch(Exception e){
			message = "<div class=\"failure\">Error saving group to account mapping: " + e.getMessage() + "</div>";
		}
	}

	if ("delete".equals(request.getParameter("command"))) {
		Configuration newConfiguration = (Configuration)configuration.clone();
		try{
			String references = ConfigurationWebToolkit.getHostToGroupReferences(newConfiguration, request.getParameter("name"));
			if( references==null ) {
				if (newConfiguration.removeGroupToAccountMapping( request.getParameter("name") )!=null) {
					gums.setConfiguration(newConfiguration);
					configuration = gums.getConfiguration();
					message = "<div class=\"success\">Group to account mapping has been deleted.</div>";
				}
				else
					message = "<div class=\"failure\">Error deleting group to account mapping</div>";
			}
			else
				message = "<div class=\"failure\">You cannot delete this item until removing references to it within host to group mapping(s) that match against " + references + "</div>";
		}catch(Exception e){
			message = "<div class=\"failure\">Error deleting group to account mapping: " + e.getMessage() + "</div>";
		}
	}

	Collection g2AMappings = configuration.getGroupToAccountMappings().values();

	out.write(
"<table id=\"form\" cellpadding=\"2\" cellspacing=\"2\">");

	if(message!=null)
		out.write( "<tr><td colspan=\"2\">" + message + "</td></tr>" );
				
	Iterator g2AMappingsIt = g2AMappings.iterator();
	while(g2AMappingsIt.hasNext()) {
		GroupToAccountMapping g2AMapping = g2AMappingsIt.hasNext() ? (GroupToAccountMapping)g2AMappingsIt.next() : null;
		
		if ("_test".equals(g2AMapping.getName()))
			continue;
%>	
	   	<tr>
			<td width="1" valign="top">
				<form action="groupToAccountMappings.jsp" method="get">
					<input type="submit" style="width:80px" name="command" value="edit">
					<input type="submit" style="width:80px" name="command" value="delete" onclick="if(!confirm('Are you sure you want to delete this group to account mapping?'))return false;">
					<input type="hidden" name="name" value="<%=g2AMapping.getName()%>">
				</form>
			</td>
	  		<td align=left>
		   		<table class="configElement" width="100%">
		  			<tr>
			    		<td>
							Name:
							<a href="groupToAccountMappings.jsp?command=edit&name=<%=g2AMapping.getName()%>">
								<%=g2AMapping.getName()%>
							</a><br>
							Description: <%=g2AMapping.getDescription()%><br>		    		
<%				    		
		out.write(			"User Group" + (g2AMapping.getUserGroups().size()>1 ? "s: " : ": ") );
		
		Iterator userGroupsIt = g2AMapping.getUserGroups().iterator();
		while(userGroupsIt.hasNext())
		{
			String userGroup = (String)userGroupsIt.next();
			out.write( "<a href=\"userGroups.jsp?command=edit&name=" + userGroup + "\">" + userGroup + "</a>" );
			if( userGroupsIt.hasNext() )
				out.write(", ");
		}
%>
		<br>
<%
		out.write(			"Account Mapper" + (g2AMapping.getAccountMappers().size()>1 ? "s: " : ": ") );
		
		Iterator accountMappersIt = g2AMapping.getAccountMappers().iterator();
		while(accountMappersIt.hasNext())
		{
			String accountMapper = (String)accountMappersIt.next();
			out.write( "<a href=\"accountMappers.jsp?command=edit&name=" + accountMapper + "\">" + accountMapper + "</a>" );
			if( accountMappersIt.hasNext() )
				out.write(", ");
		}
%>	
						</td>
			      	</tr>
				</table>
			</td>
			<td width=\"10\"></td>		
		</tr>
<%
	}
%>
		<tr>
	        <td colspan=2>
	        	<form action="groupToAccountMappings.jsp" method="get">
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
	
	Collection g2AMappings = configuration.getGroupToAccountMappings().values();
	
	GroupToAccountMapping g2AMapping = null;
	
	if ("edit".equals(request.getParameter("command"))) {
		try {
			g2AMapping = (GroupToAccountMapping)configuration.getGroupToAccountMappings().get( request.getParameter("name") );
		} catch(Exception e) {
			out.write( "<div class=\"failure\">Error getting group to account mapping: " + e.getMessage() + "</div>" );
			return;
		}
	}

	if ("reload".equals(request.getParameter("command"))) {
		try{
			g2AMapping = ConfigurationWebToolkit.parseGroupToAccountMapping(request);
		} catch(Exception e) {
			out.write( "<div class=\"failure\">Error reloading group to account mapping: " + e.getMessage() + "</div>" );
			return;
		}
	}
		
	else if ("add".equals(request.getParameter("command"))) {
		g2AMapping = new GroupToAccountMapping(configuration);
	}
%>

<form action="groupToAccountMappings.jsp" method="get">
	<input type="hidden" name="command" value="">
	<input type="hidden" name="originalCommand" value="<%=("reload".equals(request.getParameter("command")) ? request.getParameter("originalCommand") : request.getParameter("command"))%>">
	<input type="hidden" name="uGInsertCounter">
	<input type="hidden" name="aMInsertCounter">
	<table id="form" border="0" cellpadding="2" cellspacing="2" align="center">
		<tr>
    		<td nowrap style="text-align: right;">
	    		Name:
		    </td>
		    <td nowrap>
<%

	if ("add".equals(request.getParameter("command")) || "add".equals(request.getParameter("originalCommand"))) {
%>
		    	<input maxlength="256" size="32" name="name" value="<%=(g2AMapping.getName()!=null ? g2AMapping.getName() : "")%>"/>
		    </td>
		</tr>
		<tr>
			<td nowrap style="text-align: right;">
	    		i.e.
		    </td>
		    <td nowrap>
				myGroupToAccountMapping
		    </td>
		</tr>
<%
	}
	else {
%>
		    	<%=g2AMapping.getName()%>
		    	<input type="hidden" name="name" value="<%=g2AMapping.getName()%>"/>
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
				<input name="description" size="64" value="<%=g2AMapping.getDescription()%>"/>
		    </td>
		</tr>	
		<tr>
			<td nowrap style="text-align: right;">User Group(s): </td>
			<td>
<%
	
	// Create multiple user groups
	int counter = 0;
	Collection userGroups = g2AMapping.getUserGroups();
	Iterator userGroupsIt = userGroups.iterator();
	int lastCounter = userGroups.size();
	int insertCounter = lastCounter;
	if (request.getParameter("uGInsertCounter")!=null && !request.getParameter("uGInsertCounter").equals(""))
		insertCounter = Integer.parseInt(request.getParameter("uGInsertCounter"));
	while ( userGroupsIt.hasNext())
	{
		if (counter!=insertCounter)
			out.write("<a href=\"javascript:void(0)\" onclick=\"document.forms[0].elements['command'].value='reload';document.forms[0].elements['uGInsertCounter'].value='"+counter+"';document.forms[0].submit();\" title=\"insert here\">+</a> ");
		int numRepetitions = (counter==insertCounter)?2:1;
        for (int i=0; i<numRepetitions; i++) {
			out.write( 
				ConfigurationWebToolkit.createSelectBox("uG"+counter, 
					configuration.getUserGroups().values(), 
					(counter==insertCounter) ? null : (String)userGroupsIt.next(),
					"onchange=\"document.forms[0].elements['command'].value='reload';document.forms[0].submit();\"",
					true) );
 			counter++;
 		}
	}
	if (insertCounter==lastCounter) {
		out.write( 
			ConfigurationWebToolkit.createSelectBox("uG"+lastCounter, 
				configuration.getUserGroups().values(), 
				null,
				"onchange=\"document.forms[0].elements['command'].value='reload';document.forms[0].submit();\"",
				true) );
	}
	else
		out.write("<a href=\"javascript:void(0)\" onclick=\"document.forms[0].elements['command'].value='reload';document.forms[0].elements['uGInsertCounter'].value='"+lastCounter+"';document.forms[0].submit();\" title=\"insert here\">+</a> ");
%>
			 (validate membership from first successful user group)
		    </td>
		</tr>
		<tr>
			<td nowrap style="text-align: right;">Account Mapper(s): </td>
			<td>
<%	
	// Create multiple account mappings
	counter = 0;
	Collection accountMappers = g2AMapping.getAccountMappers();
	Iterator accountMappersIt = accountMappers.iterator();
 	lastCounter = accountMappers.size();
	insertCounter = lastCounter;
	if (request.getParameter("aMInsertCounter")!=null && !request.getParameter("aMInsertCounter").equals(""))
		insertCounter = Integer.parseInt(request.getParameter("aMInsertCounter"));
	while ( accountMappersIt.hasNext())
	{
		if (counter!=insertCounter)
			out.write("<a href=\"javascript:void(0)\" onclick=\"document.forms[0].elements['command'].value='reload';document.forms[0].elements['aMInsertCounter'].value='"+counter+"';document.forms[0].submit();\" title=\"insert here\">+</a> ");
		int numRepetitions = (counter==insertCounter)?2:1;
        for (int i=0; i<numRepetitions; i++) {
			out.write( 
				ConfigurationWebToolkit.createSelectBox("aM"+counter, 
					configuration.getAccountMappers().values(), 
					(counter==insertCounter) ? null : (String)accountMappersIt.next(),
					"onchange=\"document.forms[0].elements['command'].value='reload';document.forms[0].submit();\"",
					true) );
 			counter++;
 		}
	}
	if (insertCounter==lastCounter) {
		out.write( 
			ConfigurationWebToolkit.createSelectBox("aM"+lastCounter, 
				configuration.getAccountMappers().values(), 
				null,
				"onchange=\"document.forms[0].elements['command'].value='reload';document.forms[0].submit();\"",
				true) );
	}
	else
		out.write("<a href=\"javascript:void(0)\" onclick=\"document.forms[0].elements['command'].value='reload';document.forms[0].elements['aMInsertCounter'].value='"+lastCounter+"';document.forms[0].submit();\" title=\"insert here\">+</a> ");
%>
                         (map using first successful account mapper)
			</td>
		</tr>
		<tr>
    		<td nowrap style="text-align: right;">Accounting VO Subgroup: </td>
		    <td nowrap><input maxlength="256" size="16" name="accVoSub" value="<%=g2AMapping.getAccountingVoSubgroup()%>"/> (to be included in OSG-user-VO-map)</td>
		</tr>
		<tr>
    		<td nowrap style="text-align: right;">Accounting VO: </td>
		    <td nowrap><input maxlength="256" size="16" name="accVo" value="<%=g2AMapping.getAccountingVo()%>"/> (to be included in OSG-user-VO-map)</td>
		</tr>
		<tr>
	        <td colspan=2>
	        	<div style="text-align: center;">
				<%=ConfigurationWebToolkit.createDoSubmit(g2AMappings, request)%>
	        	<div style="text-align: center;">
	        		<button type="submit" onclick="return doSubmit()">save</button>
	        	</div>
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
