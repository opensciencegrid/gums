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
<h2><span>Account Mappers</span></h2>
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
Configures account mappers.
</p>

<%

String message = null;

if (request.getParameter("action")==null || 
	"save".equals(request.getParameter("action")) || 
	"delete".equals(request.getParameter("action"))) {
	
	if ("save".equals(request.getParameter("action"))) {
		Configuration newConfiguration = (Configuration)configuration.clone();
		try{
			newConfiguration.removeAccountMapper( request.getParameter("name") );
			newConfiguration.addAccountMapper( ConfigurationWebToolkit.parseAccountMapper(request) );
			gums.setConfiguration(newConfiguration);
			configuration = gums.getConfiguration();
			message = "<div class=\"success\">Account mapper has been saved.</div>";
		}catch(Exception e){
			message = "<div class=\"failure\">Error saving account mapper: " + e.getMessage() + "</div>";
		}
	}

	if ("delete".equals(request.getParameter("action"))) {
		Configuration newConfiguration = (Configuration)configuration.clone();
		try{
			String references = ConfigurationWebToolkit.getGroupToAccountMappingReferences(newConfiguration, request.getParameter("name"), "gov.bnl.gums.account.AccountMapper");
			if( references==null ) {
				if (newConfiguration.removeAccountMapper( request.getParameter("name") )!=null) {
					gums.setConfiguration(newConfiguration);
					configuration = gums.getConfiguration();
					message = "<div class=\"success\">Account mapper has been deleted.</div>";
				}
				else
					message = "<div class=\"failure\">Error deleting account mapper</div>";
			}
			else
				message = "<div class=\"failure\">You cannot delete this item until removing references to it within group to account mapping(s): " + references + "</div>";
		}catch(Exception e){
			message = "<div class=\"failure\">Error deleting account mapper: " + e.getMessage() + "</div>";
		}
	}
	
	Collection accountMappers = configuration.getAccountMappers().values();

	out.write(
	"<table id=\"form\" cellpadding=\"2\" cellspacing=\"2\">");

	if(message!=null)
		out.write( 
		"<tr><td colspan=\"2\">" + message + "</td></tr>" );
				
	Iterator accountMappersIt = accountMappers.iterator();
	while(accountMappersIt.hasNext()) {
		AccountMapper accountMapper = accountMappersIt.hasNext() ? (AccountMapper)accountMappersIt.next() : null;
		
%>
	   	<tr>
			<td width="55" valign="top">
				<form action="accountMappers.jsp" method="get">
					<input type="image" src="images/Edit24.gif" name="action" value="edit">
					<input type="image" src="images/Remove24.gif" name="action" value="delete" onclick="if(!confirm('Are you sure you want to delete this account mapper?'))return false;">
					<input type="hidden" name="name" value="<%=accountMapper.getName()%>">
				</form>
			</td>
	  		<td align="left">
		   		<table class="configElement" width="100%">
		  			<tr>
			    		<td>
<%
		if (accountMapper instanceof GroupAccountMapper) {
%>
						    Group account mapper:
				    		<a href="accountMappers.jsp?action=edit&name=<%=accountMapper.getName()%>">
				    			<%=accountMapper.getName()%>
				    		</a><br>
				    		Description: <%=accountMapper.getDescription()%><br>	
<%
		} else if (accountMapper instanceof ManualAccountMapper) {
%>
				    		Manual account mapper:
				    		<a href="accountMappers.jsp?action=edit&name=<%=accountMapper.getName()%>">
				    			<%=accountMapper.getName()%>
				    		</a><br>	
				    		Description: <%=accountMapper.getDescription()%><br>	
				    		Persistence factory:
				    		<a href="persistenceFactories.jsp?action=edit&name=<%=((ManualAccountMapper)accountMapper).getPersistenceFactory()%>">
				    			<%=((ManualAccountMapper)accountMapper).getPersistenceFactory()%>
				    		</a><br>	
<%
		} else if (accountMapper instanceof AccountPoolMapper) {
%>
				    		Account pool mapper:
				    		<a href="accountMappers.jsp?action=edit&name=<%=accountMapper.getName()%>">
				    			<%=accountMapper.getName()%>
				    		</a><br>	
				    		Description: <%=accountMapper.getDescription()%><br>	
							Pool: <%=((AccountPoolMapper)accountMapper).getAccountPool()%><br>
				    		Persistence factory:
				    		<a href="persistenceFactories.jsp?action=edit&name=<%=((AccountPoolMapper)accountMapper).getPersistenceFactory()%>">
				    			<%=((AccountPoolMapper)accountMapper).getPersistenceFactory()%>
				    		</a><br>
<%
		} else if (accountMapper instanceof GecosLdapAccountMapper) {
%>
							GECOS LDAP account mapper:
				    		<a href="accountMappers.jsp?action=edit&name=<%=accountMapper.getName()%>">
				    			<%=accountMapper.getName()%>
				    		</a><br>	
				    		Description: <%=accountMapper.getDescription()%><br>	
							JNDI LDAP service: <%=((GecosLdapAccountMapper)accountMapper).getJndiLdapUrl()%>
<%
		} else if (accountMapper instanceof GecosNisAccountMapper) {
%>
							GECOS NIS account mapper:
				    		<a href="accountMappers.jsp?action=edit&name=<%=accountMapper.getName()%>">
				    			<%=accountMapper.getName()%>
				    		</a><br>	
				    		Description: <%=accountMapper.getDescription()%><br>	
							JNDI NIS service: <%=((GecosNisAccountMapper)accountMapper).getJndiNisUrl()%>
<%
		} else if (accountMapper instanceof NISAccountMapper) {
%>
							NIS account mapper:
				    		<a href="accountMappers.jsp?action=edit&name=<%=accountMapper.getName()%>">
				    			<%=accountMapper.getName()%>
				    		</a><br>	
				    		Description: <%=accountMapper.getDescription()%><br>	
							JNDI NIS service: <%=((NISAccountMapper)accountMapper).getJndiNisUrl()%>
<%
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
%>	
		<tr>
	        <td colspan=2>
	        	<form action="accountMappers.jsp" method="get">
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
	
	Collection accountMappers = configuration.getAccountMappers().values();
	
	AccountMapper accountMapper = null;
	
	ArrayList accountMapperTypes = new ArrayList();
	accountMapperTypes.add(GroupAccountMapper.getTypeStatic());
	accountMapperTypes.add(ManualAccountMapper.getTypeStatic());
	accountMapperTypes.add(AccountPoolMapper.getTypeStatic());
	accountMapperTypes.add(GecosLdapAccountMapper.getTypeStatic());
	
	if ("edit".equals(request.getParameter("action"))) {
		try {
			accountMapper = (AccountMapper)configuration.getAccountMappers().get( request.getParameter("name") );
		} catch(Exception e) {
			out.write( "<div class=\"failure\">Error getting account mapper: " + e.getMessage() + "</div>" );
			return;
		}
	}

	if ("reload".equals(request.getParameter("action"))) {
		try{
			accountMapper = ConfigurationWebToolkit.parseAccountMapper(request);
		} catch(Exception e) {
			out.write( "<div class=\"failure\">Error reloading account mapper: " + e.getMessage() + "</div>" );
			return;
		}
	}
		
	else if ("add".equals(request.getParameter("action"))) {
		accountMapper = new GroupAccountMapper(configuration);
	}		
		
%>
<form action="accountMappers.jsp" method="get">
	<input type="hidden" name="action" value="">
	<input type="hidden" name="originalAction" value="<%=("reload".equals(request.getParameter("action")) ? request.getParameter("originalAction") : request.getParameter("action"))%>">
	<table id="form" border="0" cellpadding="2" cellspacing="2" align="center">
		<tr>
    		<td nowrap style="text-align: right;">
	    		For requests routed to account mapper
		    </td>
		    <td nowrap>
<%
	if ("add".equals(request.getParameter("action")) || "add".equals(request.getParameter("originalAction"))) {
%>
		    	<input maxlength="256" size="32" name="name" value="<%=(accountMapper.getName()!=null ? accountMapper.getName() : "")%>"/>
		    </td>
		</tr>
		<tr>
			<td nowrap style="text-align: right;">
	    		i.e.
		    </td>
		    <td nowrap>
				myAccountMapper
		    </td>
		</tr>
<%
	}
	else {
%>
		    	<%=accountMapper.getName()%>
		    	<input type="hidden" name="name" value="<%=accountMapper.getName()%>"/>
		    </td>
		</tr>
<%
	}
%>
		<tr>
			<td nowrap style="text-align: right;">
	    		with description:
		    </td>
		    <td nowrap>
				<input name="description" size="64" value="<%=accountMapper.getDescription()%>"/>
		    </td>
		</tr>	
		<tr>
    		<td nowrap style="text-align: right;">
	    		of type
		    </td>
		    <td nowrap>
			<%=ConfigurationWebToolkit.createSelectBox("type", 
				accountMapperTypes, 
				accountMapper.getType(),
				"onchange=\"document.forms[0].elements['action'].value='reload';document.forms[0].submit();\"",
				false)%>
		    </td>
		</tr>
<%
	if (accountMapper instanceof GroupAccountMapper) {
%>
		<tr>
			<td nowrap style="text-align: right;">
				map to account
			</td>
			<td>
				<input maxlength="256" size="32" name="accountName" value="<%=((GroupAccountMapper)accountMapper).getAccountName()%>"/>
			</td>
		</tr>
		<tr>
			<td nowrap style="text-align: right;">
	    		i.e.
		    </td>
		    <td nowrap>
				myAccount
		    </td>
		</tr>
<%
	} else if (accountMapper instanceof ManualAccountMapper) {
%>
		<tr>
			<td style="text-align: right;">
				search within this group in persistence factory 
			</td>
			<td>
				<%=ConfigurationWebToolkit.createSelectBox("persistenceFactory", 
					configuration.getPersistenceFactories().values(), 
					((ManualAccountMapper)accountMapper).getPersistenceFactory(),
					null,
					false)%>
			</td> .
		</tr>
<%
	} else if (accountMapper instanceof AccountPoolMapper) {
%>
		<tr>
			<td nowrap style="text-align: right;">
				map to free or previously assigned account within pool
			</td>
			<td>
				<input maxlength="256" size="32" name="accountPool" value="<%=((AccountPoolMapper)accountMapper).getAccountPool()%>"/>
			</td>
		</tr>
		<tr>
			<td nowrap style="text-align: right;">
	    		i.e.
		    </td>
		    <td nowrap>
				myPool
		    </td>
		</tr>
		<tr>
			<td nowrap style="text-align: right;">
				in persistence factory
			</td>
			<td>
				<%=ConfigurationWebToolkit.createSelectBox("persistenceFactory", 
						configuration.getPersistenceFactories().values(), 
						((AccountPoolMapper)accountMapper).getPersistenceFactory(),
						null,
						false)%> .
			</td>
		</tr>
<%
		if ("add".equals(request.getParameter("action")) || "add".equals(request.getParameter("originalAction")))
%>
		<tr>
			<td colspan="2" nowrap style="text-align: center;">
				NOTE: After saving, you should also add a range of accounts for this pool (click "Add Pool Account Range")
		    </td>
		</tr>
<%
	} else if (accountMapper instanceof GecosLdapAccountMapper) {
%>
		<tr>
			<td nowrap style="text-align: right;">
				map to account assigned by JNDI LDAP service
			</td>
			<td>
				<input maxlength="256" size="32" name="serviceUrl" value="<%=((GecosLdapAccountMapper)accountMapper).getJndiLdapUrl()%>"/> .
			</td>
		</tr>
		<tr>
			<td nowrap style="text-align: right;">
	    		i.e.
		    </td>
		    <td nowrap>
				ldap://localhost/dc=usatlas,dc=bnl,dc=gov
		    </td>
		</tr>
		<tr>
			<td nowrap style="text-align: right;">
				Look for name in LDAP field 
			</td>
			<td> 
				<input maxlength="256" size="16" name="gecosField" value="<%=((GecosLdapAccountMapper)accountMapper).getGecosField()%>"/> .
			</td>
		</tr>
	    <tr>
    		<td nowrap style="text-align: right;">
	    		i.e.
		    </td>
		    <td nowrap>
		    	gecos
		    </td>
		</tr>
		<tr>
			<td nowrap style="text-align: right;">
				Look for account in LDAP field 
			</td>
			<td> 
				<input maxlength="256" size="16" name="accountField" value="<%=((GecosLdapAccountMapper)accountMapper).getAccountField()%>"/> .
			</td>
		</tr>
	    <tr>
    		<td nowrap style="text-align: right;">
	    		i.e.
		    </td>
		    <td nowrap>
		    	uid
		    </td>
		</tr>
<%
	}	
%>
		<tr>
	        <td colspan=2>
				<%=ConfigurationWebToolkit.createDoSubmit(accountMappers, request)%>
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
