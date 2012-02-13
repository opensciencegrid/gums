<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@page import="gov.bnl.gums.*"%>
<jsp:useBean id="gums" scope="application" class="gov.bnl.gums.admin.GUMSAPIImpl" />
<%@ page import="gov.bnl.gums.account.*" %>
<%@ page import="gov.bnl.gums.db.*" %>
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
<h2><span>Manual Account Mappings</span></h2>
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
Configures manual account mappings.
</p>

<%

Map accountMappers = configuration.getAccountMappers();
String message = null;

if (request.getParameter("command")==null || 
	"save".equals(request.getParameter("command")) || 
	"delete".equals(request.getParameter("command"))) {
	
	if ("save".equals(request.getParameter("command"))) {
		ManualAccountMapper manualAccountMapper = (ManualAccountMapper)accountMappers.get(request.getParameter("accountMapper"));
		try{
			gums.manualMappingAdd2(manualAccountMapper.getName(), request.getParameter("dn"), request.getParameter("account"));
			message = "<div class=\"success\">Mapping has been saved.</div>";
		}catch(Exception e){
			message = "<div class=\"failure\">Error saving mapping: " + e.getMessage() + "</div>";
		}
	}

	if ("delete".equals(request.getParameter("command"))) {
		ManualAccountMapper manualAccountMapper = (ManualAccountMapper)accountMappers.get(request.getParameter("accountMapper"));
		try{
			gums.manualMappingRemove2(manualAccountMapper.getName(), request.getParameter("dn"));
			message = "<div class=\"success\">Mapping has been deleted.</div>";
		}catch(Exception e){
			message = "<div class=\"failure\">Error deleting mapping: " + e.getMessage() + "</div>";
		}
	}

	out.write(
"<table id=\"form\" cellpadding=\"2\" cellspacing=\"2\">");

	if(message!=null)
		out.write( 
	"<tr><td colspan=\"2\">" + message + "</td></tr>" );
				
	Iterator accountMappersIt = accountMappers.values().iterator();
	while(accountMappersIt.hasNext()) {
		AccountMapper accountMapper = (AccountMapper)accountMappersIt.next();
		if (accountMapper instanceof ManualAccountMapper) {
			ManualAccountMapper manualAccountMapper = (ManualAccountMapper)accountMapper;
			
			Map accountMap = manualAccountMapper.getAccountMap();
			if (accountMap==null)
				continue;
				
			Iterator dnIt = accountMap.keySet().iterator();
			while (dnIt.hasNext()) {
				String dn = (String)dnIt.next();
				
				if ("/DC=com/DC=example/OU=People/CN=Example User 12345".equals(dn))
					continue;

%>
   	<tr>
		<td width="25" valign="top">
			<form action="manualAccounts.jsp" method="get">
				<input type="submit" style="width:80px" name="command" value="delete" onclick="if(!confirm('Are you sure you want to delete this mapping?'))return false;">
				<input type="hidden" name="dn" value="<%=dn%>">
				<input type="hidden" name="accountMapper" value="<%=manualAccountMapper.getName()%>">
			</form>
		</td>
  		<td align="left">
	   		<table class="userElement" width="100%">
	  			<tr>
		    		<td>
			    		DN: <%=dn%><br>
			    		Account Mapper: <a href="accountMappers.jsp?name=<%=manualAccountMapper.getName()%>&command=edit"><%=manualAccountMapper.getName()%></a><br>
			    		Account: <%=accountMap.get(dn)%><br>
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
			<form action="manualAccounts.jsp" method="get">
				<div style="text-align: center;"><input type="submit" name="command" value="add"></div>
			</form>
	    </td>
	</tr>
  </table>
</form>
<%
}

else if ("add".equals(request.getParameter("command"))) {
	HibernateMapping mapping = new HibernateMapping();

	// Retrieve mappings in Manual Account Mappers
	ArrayList manualAccountMappers = new ArrayList();
	Iterator accountMappersIt = accountMappers.values().iterator();
	while (accountMappersIt.hasNext()) {
		AccountMapper accountMapper = (AccountMapper)accountMappersIt.next();	
		if (accountMapper instanceof ManualAccountMapper)
			manualAccountMappers.add( accountMapper.getName() );
	}
		
%>
<form action="manualAccounts.jsp" method="get">
	<input type="hidden" name="command" value="">
	<table id="form" border="0" cellpadding="2" cellspacing="2" align="center">
		<tr>
    		<td nowrap style="text-align: right;">
	    		DN:
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
				Account Mapper: 
			</td>
			<td>
				<%=ConfigurationWebToolkit.createSelectBox("accountMapper", manualAccountMappers, null, null, manualAccountMappers.size()>1)%>
			</td>
		</tr>
		<tr>
			<td nowrap style="text-align: right;">
				Account: 
			</td>
			<td>
				<input maxlength="256" size="32" name="account" value=""/>
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
		
