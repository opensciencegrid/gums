<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@page import="gov.bnl.gums.*"%>
<jsp:useBean id="gums" scope="application" class="gov.bnl.gums.admin.GUMSAPIImpl" />
<%@ page import="gov.bnl.gums.hostToGroup.*" %>
<%@ page import="gov.bnl.gums.groupToAccount.GroupToAccountMapping" %>
<%@ page import="gov.bnl.gums.configuration.*" %>
<%@ page import="java.lang.Math" %>
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
<h2><span>Host To Group</span></h2>
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
Configures host to group mappings.
</p>

<%
String message = null;
String movedName = null;

if (request.getParameter("command")==null || 
	"save".equals(request.getParameter("command")) || 
	"delete".equals(request.getParameter("command")) ||
	"up".equals(request.getParameter("command")) ||
	"down".equals(request.getParameter("command"))) {
	
	if ("save".equals(request.getParameter("command"))) {
		Configuration newConfiguration = (Configuration)configuration.clone();
		try{
			int index = newConfiguration.getHostToGroupMappings().indexOf( newConfiguration.getHostToGroupMapping(request.getParameter("originalName")) );
			newConfiguration.removeHostToGroupMapping( request.getParameter("originalName") );
			if (index!=-1)
				newConfiguration.addHostToGroupMapping( index, ConfigurationWebToolkit.parseHostToGroupMapping(request) );
			else
				newConfiguration.addHostToGroupMapping( ConfigurationWebToolkit.parseHostToGroupMapping(request) );
			gums.setConfiguration(newConfiguration);
			configuration = gums.getConfiguration();
			message = "<div class=\"success\">Host to group mapping has been saved.</div>";
		}catch(Exception e){
			message = "<div class=\"failure\">Error saving host to group mapping: " + e.getMessage() + "</div>";
		}
	}

	if ("delete".equals(request.getParameter("command"))) {
		Configuration newConfiguration = (Configuration)configuration.clone();
		try{
			if( newConfiguration.removeHostToGroupMapping( request.getParameter("name") )!=null ) {
				gums.setConfiguration(newConfiguration);
				configuration = gums.getConfiguration();
				message = "<div class=\"success\">Host to group mapping has been deleted.</div>";
			}
			else
				message = "<div class=\"failure\">Error deleting host to group mapping</div>";
		}catch(Exception e){
			message = "<div class=\"failure\">Error deleting host to group mapping: " + e.getMessage() + "</div>";
		}
	}

	if ("up".equals(request.getParameter("command"))) {
		try{
			Configuration newConfiguration = (Configuration)configuration.clone();
			HostToGroupMapping h2GMapping = newConfiguration.getHostToGroupMapping( request.getParameter("name") );
			int index = newConfiguration.getHostToGroupMappings().indexOf(h2GMapping);
			newConfiguration.removeHostToGroupMapping(h2GMapping.getName());
			newConfiguration.addHostToGroupMapping(Math.max(0, index-1), h2GMapping);
			gums.setConfiguration(newConfiguration);
			configuration = newConfiguration;
			movedName = request.getParameter("name");
		}catch(Exception e){
			message = "<div class=\"failure\">Error moving up: " + e.getMessage() + "</div>";
		}
	}
	
	if ("down".equals(request.getParameter("command"))) {
		try{
			Configuration newConfiguration = (Configuration)configuration.clone();
			HostToGroupMapping h2GMapping = newConfiguration.getHostToGroupMapping( request.getParameter("name") );
			int index = newConfiguration.getHostToGroupMappings().indexOf(h2GMapping);
			newConfiguration.removeHostToGroupMapping(h2GMapping.getName());
			newConfiguration.addHostToGroupMapping(Math.min(newConfiguration.getHostToGroupMappings().size(), index+1), h2GMapping);
			gums.setConfiguration(newConfiguration);
			configuration = newConfiguration;
			movedName = request.getParameter("name");
		}catch(Exception e){
			message = "<div class=\"failure\">Error moving down: " + e.getMessage() + "</div>";
		}
	}	
	
	Collection h2GMappings = configuration.getHostToGroupMappings();

	out.write(
"<table id=\"form\" cellpadding=\"2\" cellspacing=\"2\">");

	if(message!=null)
		out.write( "<tr><td colspan=\"2\">" + message + "</td></tr>" );
				
	Iterator h2GMappingsIt = h2GMappings.iterator();
	while(h2GMappingsIt.hasNext()) {
		HostToGroupMapping h2GMapping = h2GMappingsIt.hasNext() ? (HostToGroupMapping)h2GMappingsIt.next() : null;
		
		if ("/DC=com/DC=example/OU=Services/CN=example.site.com".equals(h2GMapping.getName()))
			continue;
		
		if(h2GMapping instanceof CertificateHostToGroupMapping) {
			CertificateHostToGroupMapping cH2GMapping = (CertificateHostToGroupMapping)h2GMapping;
			
%>
	   	<tr>
			<td width="1" valign="top">
				<form action="hostToGroupMappings.jsp#<%=cH2GMapping.getName()%>" method="get">
					<a name="<%=cH2GMapping.getName()%>">
						<input type="submit" style="width:95px" name="command" value="edit">
						<input type="submit" style="width:95px" name="command" value="delete" onclick="if(!confirm('Are you sure you want to delete this host to group mapping?'))return false;">
						<input type="submit" style="width:47px" name="command" value="up" alt="move up"><input type="submit" style="width:47px" name="command" value="down" alt="move down">
						<input type="hidden" name="name" value="<%=cH2GMapping.getName()%>">
						<input type="hidden" name="dt" value="<%=new java.util.Date().getTime()%>">
					</a>
				</form>
			</td>
	  		<td align="left">
		   		<table class="<%=(cH2GMapping.getName().equals(movedName)?"configMovedElement":"configElement")%>" width="100%">
		  			<tr>
			    		<td>
							Host to Group Mapping:
							<a href="hostToGroupMappings.jsp?command=edit&name=<%=cH2GMapping.getName()%>">
								<%=cH2GMapping.getName()%>
							</a><br>
							Description: <%=cH2GMapping.getDescription()%><br>	
<%
		out.write(			"Group To Account Mapping" + (cH2GMapping.getGroupToAccountMappings().size()>1 ? "s: " : ": ") );
		
		Iterator g2AMappingsIt = cH2GMapping.getGroupToAccountMappings().iterator();
		while(g2AMappingsIt.hasNext())
		{
			String g2AMapping = (String)g2AMappingsIt.next();
			out.write( "<a href=\"groupToAccountMappings.jsp?command=edit&name=" + g2AMapping + "\">" + g2AMapping + "</a>" );
			if( g2AMappingsIt.hasNext() )
				out.write(", ");
		}
%>
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
		<tr>
	        <td colspan=2>
	        	<form action="hostToGroupMappings.jsp" method="get">
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
	
	Collection h2GMappings = configuration.getHostToGroupMappings();
	
	HostToGroupMapping h2GMapping = null;
	
	if ("edit".equals(request.getParameter("command"))) {
		try {
			h2GMapping = (HostToGroupMapping)configuration.getHostToGroupMapping( request.getParameter("name") );
		} catch(Exception e) {
			out.write( "<div class=\"failure\">Error getting host to group mapping: " + e.getMessage() + "</div>" );
			return;
		}
	}

	if ("reload".equals(request.getParameter("command"))) {
		try{
			h2GMapping = ConfigurationWebToolkit.parseHostToGroupMapping(request);
		} catch(Exception e) {
			out.write( "<div class=\"failure\">Error reloading host to group mapping: " + e.getMessage() + "</div>" );
			return;
		}
	}
	
	else if ("add".equals(request.getParameter("command"))) {
		h2GMapping = new CertificateHostToGroupMapping(configuration);
	}
	
	CertificateHostToGroupMapping cH2GMapping = (CertificateHostToGroupMapping)h2GMapping;
%>
<form action="hostToGroupMappings.jsp" method="get">
	<input type="hidden" name="command" value="">
	<input type="hidden" name="originalName" value="<%=("reload".equals(request.getParameter("command")) ? request.getParameter("originalName") : request.getParameter("name"))%>"/>
	<input type="hidden" name="originalCommand" value="<%=("reload".equals(request.getParameter("command")) ? request.getParameter("originalCommand") : request.getParameter("command"))%>">
	<input type="hidden" name="insertCounter">
	<table id="form" border="0" cellpadding="2" cellspacing="2" align="center">
		<tr>
    		<td nowrap style="text-align: right;">
	    		Hosts:
		    </td>
		    <td nowrap>
		    	<input maxlength="256" size="48" name="name" value="<%=cH2GMapping.getName()%>"/>
				cn<input type="radio" name="hg_type" value="cn" <%=(cH2GMapping.getDn()==null?"checked":"")%>>
			    dn<input type="radio" name="hg_type" value="dn" <%=(cH2GMapping.getDn()!=null?"checked":"")%>>
			    (only requests from matching hosts are accepted)
		    </td>
		</tr>
		<tr>
			<td nowrap style="text-align: right;">
	    		i.e.
		    </td>
		    <td nowrap>
				*.host1.com, *.host2.com
		    </td>
		</tr>
		<tr>
			<td nowrap style="text-align: right;">
	    		Description:
		    </td>
		    <td nowrap>
				<input name="description" size="64" value="<%=cH2GMapping.getDescription()%>"/>
		    </td>
		</tr>	
		<tr>
			<td nowrap style="text-align: right;">Group To Account Mapping(s): </td>
			<td>
<%
	// Create multiple group to account mappings
	int counter = 0;
	Collection g2AMappings = cH2GMapping.getGroupToAccountMappings();
	Iterator g2AMappingsIt = g2AMappings.iterator();
 	int lastCounter = g2AMappings.size();
	int insertCounter = lastCounter;
	if (request.getParameter("insertCounter")!=null && !request.getParameter("insertCounter").equals(""))
		insertCounter = Integer.parseInt(request.getParameter("insertCounter"));
	while ( g2AMappingsIt.hasNext())
	{
		if (counter!=insertCounter)
			out.write("<a href=\"javascript:void(0)\" onclick=\"document.forms[0].elements['command'].value='reload';document.forms[0].elements['insertCounter'].value='"+counter+"';document.forms[0].submit();\" title=\"insert here\">+</a> ");
		int numRepetitions = (counter==insertCounter)?2:1;
        for (int i=0; i<numRepetitions; i++) {
			out.write( 
				ConfigurationWebToolkit.createSelectBox("g2AM"+counter, 
					configuration.getGroupToAccountMappings().values(), 
					(counter==insertCounter) ? null : (String)g2AMappingsIt.next(),
					"onchange=\"document.forms[0].elements['command'].value='reload';document.forms[0].submit();\"",
					true) );
 			counter++;
 		}
	}
	if (insertCounter==lastCounter) {
		out.write( 
			ConfigurationWebToolkit.createSelectBox("g2AM"+lastCounter, 
				configuration.getGroupToAccountMappings().values(), 
				null,
				"onchange=\"document.forms[0].elements['command'].value='reload';document.forms[0].submit();\"",
				true) );
	}
	else
		out.write("<a href=\"javascript:void(0)\" onclick=\"document.forms[0].elements['command'].value='reload';document.forms[0].elements['insertCounter'].value='"+lastCounter+"';document.forms[0].submit();\" title=\"insert here\">+</a> ");

%>	
				(return account from first successful mapping)
			</td>
		</tr>
		<tr>
	        <td colspan=2 style="text-align: right;">
				<%=ConfigurationWebToolkit.createDoSubmit(h2GMappings, request)%>
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
