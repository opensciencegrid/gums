<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@page import="gov.bnl.gums.*"%>
<jsp:useBean id="gums" scope="application" class="gov.bnl.gums.admin.GUMSAPIImpl" />
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
<h2><span>VOMS Servers</span></h2>
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
Configures VOMS servers.
</p>

<%
String message = null;

if (request.getParameter("command")==null || 
	"save".equals(request.getParameter("command")) || 
	"delete".equals(request.getParameter("command"))) {
	
	if ("save".equals(request.getParameter("command"))) {
		Configuration newConfiguration = (Configuration)configuration.clone();
		try{
			newConfiguration.removeVomsServer( request.getParameter("name") );
			newConfiguration.addVomsServer( ConfigurationWebToolkit.parseVomsServer(request) );
			gums.setConfiguration(newConfiguration);
			configuration = gums.getConfiguration();
			message = "<div class=\"success\">VOMS server has been saved.</div>";
		}catch(Exception e){
			message = "<div class=\"failure\">Error saving VOMS server: " + e.getMessage() + "</div>";
		}
	}

	if ("delete".equals(request.getParameter("command"))) {
		Configuration newConfiguration = (Configuration)configuration.clone();
		try{
			String references = ConfigurationWebToolkit.getVOMSUserGroupReferences(newConfiguration, request.getParameter("name"));
			if( references==null ) {
				if (newConfiguration.removeVomsServer( request.getParameter("name") )!=null) {
					gums.setConfiguration(newConfiguration);
					configuration = gums.getConfiguration();
					message = "<div class=\"success\">VOMS server has been deleted.</div>";
				}
				else
					message = "<div class=\"failure\">Error deleting VOMS server</div>";
			}
			else
				message = "<div class=\"failure\">You cannot delete this item until removing references to it within group to account mapping(s): " + references + "</div>";
		}catch(Exception e){
			message = "<div class=\"failure\">Error deleting VOMS server: " + e.getMessage() + "</div>";
		}
	}
	
	Collection vomsServers = configuration.getVomsServers().values();

	out.write(
"<table id=\"form\" cellpadding=\"2\" cellspacing=\"2\">");

	if(message!=null)
		out.write( "<tr><td colspan=\"2\">" + message + "</td></tr>" );
				
	Iterator vomsServersIt = vomsServers.iterator();
	while(vomsServersIt.hasNext()) {
		VomsServer vomsServer = vomsServersIt.hasNext() ? (VomsServer)vomsServersIt.next() : null;
		
%>
	   	<tr>
			<td width="1" valign="top">
				<form action="vomsServers.jsp" method="get">
					<input type="submit" style="width:80px" name="command" value="edit">
					<input type="submit" style="width:80px" name="command" value="delete" onclick="if(!confirm('Are you sure you want to delete this VOMS server?'))return false;">
					<input type="hidden" name="name" value="<%=vomsServer.getName()%>">
				</form>
			</td>
	  		<td align="left">
		   		<table class="configElement" width="100%">
		  			<tr>
			    		<td>
				    		VOMS Server:
				    		<a href="vomsServers.jsp?command=edit&name=<%=vomsServer.getName()%>">
				    			<%=vomsServer.getName()%>
				    		</a><br>			    	
				    		Description: <%=vomsServer.getDescription()%><br>		
				    		Base URL: <%=vomsServer.getBaseUrl()%>{remainder url}<br>	
							Persistence Factory:
				    		<a href="persistenceFactories.jsp?command=edit&name=<%=vomsServer.getPersistenceFactory()%>">
				    			<%=vomsServer.getPersistenceFactory()%>
				    		</a><br>
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
	        	<form action="vomsServers.jsp" method="get">
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
	
	Collection vomsServers = configuration.getVomsServers().values();
	
	VomsServer vomsServer = null;
	
	if ("edit".equals(request.getParameter("command"))) {
		try {
			vomsServer = (VomsServer)configuration.getVomsServers().get( request.getParameter("name") );
		} catch(Exception e) {
			out.write( "<div class=\"failure\">Error getting VOMS server: " + e.getMessage() + "</div>" );
			return;
		}
	}

	if ("reload".equals(request.getParameter("command"))) {
		try{
			vomsServer = ConfigurationWebToolkit.parseVomsServer(request);
		} catch(Exception e) {
			out.write( "<div class=\"failure\">Error reloading VOMS server: " + e.getMessage() + "</div>" );
			return;
		}
	}
		
	else if ("add".equals(request.getParameter("command"))) {
		vomsServer = new VomsServer(configuration);
	}		
%>
<form action="vomsServers.jsp" method="get">
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
		    	<input maxlength="256" size="32" name="name" value="<%=(vomsServer.getName()!=null ? vomsServer.getName() : "")%>"/>
		    </td>
		</tr>
		<tr>
			<td nowrap style="text-align: right;">
	    		i.e.
		    </td>
		    <td nowrap>
				myVomsServer
		    </td>
		</tr>
<%
	}
	else {
%>		
		    	<%=vomsServer.getName()%>
		    	<input type="hidden" name="name" value="<%=vomsServer.getName()%>"/>
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
				<input name="description" size="64" value="<%=vomsServer.getDescription()%>"/>
		    </td>
		</tr>	
		<tr>
			<td nowrap style="text-align: right;">
				Base URL:
			</td>
			<td>
				<input maxlength="256" size="64" name="baseURL" value="<%=vomsServer.getBaseUrl()%>"/>{remainder URL}
			</td>
		</tr>
	    <tr>
    		<td nowrap style="text-align: right;">
	    		i.e.
		    </td>
		    <td nowrap>
		    	https://lcg-voms.cern.ch:8443/voms
		    </td>
		</tr>
		<tr>
			<td nowrap style="text-align: right;">
				Persistence Factory:
			</td>
			<td>
				<%=ConfigurationWebToolkit.createSelectBox("persistenceFactory", 
						configuration.getPersistenceFactories().values(), 
						vomsServer.getPersistenceFactory(),
						null,
						false)%> (for caching individual users)
			</td>
		</tr>
		<tr>
			<td nowrap style="text-align: right;">
				SSL Key:
			</td>
			<td>
				<input maxlength="256" size="32" name="sslKey" value="<%=vomsServer.getSslKey()%>"/>
			</td>
		</tr>
	    <tr>
    		<td nowrap style="text-align: right;">
	    		i.e.
		    </td>
		    <td nowrap>
		    	/etc/grid-security/gumskey.pem
		    </td>
		</tr>	
		<tr>
			<td nowrap style="text-align: right;">
				SSL Cert File:
			</td>
			<td>
				<input maxlength="256" size="32" name="sslCert" value="<%=vomsServer.getSslCertfile()%>"/>
			</td>
		</tr>
	    <tr>
    		<td nowrap style="text-align: right;">
	    		i.e.
		    </td>
		    <td nowrap>
		    	/etc/grid-security/gumscert.pem
		    </td>
		</tr>
		<tr>
			<td nowrap style="text-align: right;">
				SSL Key Password:
			</td>
			<td>
				<input type="password" maxlength="256" size="32" name="sslKeyPW" value="<%=vomsServer.getSslKeyPasswd()%>"/> (leave blank if none)
			</td>
		</tr>
		<tr>
			<td nowrap style="text-align: right;">
				SSL CA Files:
			</td>
			<td>
				<input maxlength="256" size="32" name="sslCA" value="<%=vomsServer.getSslCAFiles()%>"/>
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
		<tr>
	        <td colspan=2>
				<%=ConfigurationWebToolkit.createDoSubmit(vomsServers, request)%>
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
