<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@page import="gov.bnl.gums.*"%>
<jsp:useBean id="gums" scope="application" class="gov.bnl.gums.admin.GUMSAPIImpl" />
<%@ page import="gov.bnl.gums.account.*" %>
<%@ page import="gov.bnl.gums.configuration.*" %>
<%@ page import="gov.bnl.gums.service.ConfigurationWebToolkit" %>
<%@ page import="java.util.*" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
<head>
 	<title>manual accounts</title>
 	<link href="gums.css" type="text/css" rel="stylesheet"/>
</head>
<body>
<%@include file="topNav.jspf"%>
<div id="title">
<h1><span>GUMS</span></h1>
<h2><span>GUMS User Management</span></h2>
</div>
<%@include file="sideNav.jspf"%>
<div id="body">
<p>
<%-- <jsp:useBean id="beanInstanceName" scope="session" class="beanPackage.BeanClassName" /> --%>
<%-- <jsp:getProperty name="beanInstanceName"  property="propertyName" /> --%>

Configures manual account mappings.
</p>

<%

Configuration configuration = gums.getConfiguration();
HashMap accountMappers = configuration.getAccountMappers();
String message = null;

// Retrieve users in Manual User Group
ArrayList manualUsers = new ArrayList();
Iterator it = accountMappers.values().iterator();
while (it.hasNext()) {
	AccountMapper accountMapper = (AccountMapper)it.next();	
	if (accountMapper instanceof ManualAccountMapper)
		manualUsers.addAll( ((ManualAccountMapper)accountMapper).getMemberList() );
}

if (request.getParameter("action")==null || 
	"save".equals(request.getParameter("action")) || 
	"delete".equals(request.getParameter("action"))) {
	
	if ("save".equals(request.getParameter("action"))) {
		ManualAccountMapper manualAccountMapper = (ManualAccountMapper)accountMappers.get(request.getParameter("accountMapper"));
		try{
			gums.manualMappingAdd(manualAccountMapper.getPersistenceFactory(), manualAccountMapper.getAccountName(), request.getParameter("DN"), request.getParameter("account"));
			message = "<div class=\"success\">User has been saved.</div>";
		}catch(Exception e){
			message = "<div class=\"failure\">Error saving user: " + e.getMessage() + "</div>";
		}
	}

	if ("delete".equals(request.getParameter("action"))) {
		ManualAccountMapper manualAccountMapper = (ManualAccountMapper)accountMappers.get(request.getParameter("accountMapper"));
		try{
			gums.manualGroupRemove(manualAccountMapper.getPersistenceFactory(), manualAccountMapper.getGroup(), request.getParameter("DN"));
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
				
	Iterator accountMappersIt = accountMappers.values().iterator();
	while(accountMappersIt.hasNext()) {
		AccountMapper accountMapper = (AccountMapper)accountMappersIt.next();
		if (accountMapper instanceof ManualAccountMapper) {
			ManualAccountMapper manualAccountMapper = (ManualAccountMapper)accountMappersIt.next();
			
			List users = manualAccountMapper.getMemberList();
			if (users==null)
				continue;
				
			Iterator usersIt = users.iterator();
			while (usersIt.hasNext()) {
				GridUser user = (GridUser)usersIt;

				out.write(
   	"<tr>"+
		"<td width=\"50\" valign=\"top\">"+
			"<form action=\"manualAccountMappers.jsp\" method=\"get\">"+
				"<input type=\"image\" src=\"images/Remove24.gif\" name=\"action\" value=\"delete\" onclick=\"if(!confirm('Are you sure you want to delete this user?'))return false;\">"+
				"<input type=\"hidden\" name=\"dn\" value=\"" + user.getCertificateDN() + "\">"+
				"<input type=\"hidden\" name=\"accountMapper\" value=\"" + manualAccountMapper.getName() + "\">"+
			"</form>"+
		"</td>"+
  		"<td align=\"left\">"+
	   		"<table class=\"userElement\" width=\"100%\">"+
	  			"<tr>"+
		    		"<td>"+
			    		"When mapped by manual account mapper <span style=\"color:blue\">" + manualAccountMapper.getName() + "</span>," +
			    		" map user with DN <span style=\"color:blue\">" + user.getCertificateDN() + "</span>" +
			    		" to account <span style=\"color:blue\">" + manualAccountMapper.getAccountName() + "</span>." +
		    		"</td>"+
	  			"</tr>"+
			"</table>"+
		"</td>"+
		"<td width=\"10\"></td>"+		
	"</tr>");
			}
		}
	}

	out.write(
	"<tr>"+
		"<td colspan=2>"+
			"<form action=\"manualAccounts.jsp\" method=\"get\">"+
				"<div style=\"text-align: center;\"><button type=\"submit\" name=\"action\" value=\"add\">Add</button></div>"+
			"</form>"+
	    "</td>"+
	"</tr>"+
  "</table>"+
"</form>");
}

else if ("add".equals(request.getParameter("action"))) {
/*	GridUser user = new GridUser();
		
	out.write(
"<form action=\"manualAccounts.jsp\" method=\"get\">"+
	"<input type=\"hidden\" name=\"action\" value=\"\">"+
	"<table id=\"form\" border=\"0\" cellpadding=\"2\" cellspacing=\"2\" align=\"center\">"+
		"<tr>"+
			"<td nowrap style=\"text-align: right;\">"+
				"When mapped by manual account mapper  "+
			"</td>"+
			"<td>"+ 
				ConfigurationWebToolkit.createSelectBox("accountMapper", accountMappers.values(), null, null, accountMappers.size()>1)+" ,"
			"</td>"+
		"</tr>"+
		"<tr>"+
    		"<td nowrap style=\"text-align: right;\">"+
	    		"map user with DN "+
		    "</td>"+
		    "<td nowrap>"+
			    "<input maxlength=\"256\" size=\"64\" name=\"dn\" value=\"\"/>"+
		    "</td>"+
		"</tr>"+
		"<tr>"+
    		"<td nowrap style=\"text-align: right;\">"+
	    		"i.e."+
		    "</td>"+
		    "<td nowrap>"+
			    "/DC=org/DC=doegrids/OU=People/CN=Joe Smith 12345"+
		    "</td>"+
		"</tr>"+		
		"<tr>"+
			"<td nowrap style=\"text-align: right;\">"+
				"to account "+
			"</td>"+
			"<td>"+ 
				"<input maxlength=\"256\" size=\"32\" name=\"account\" value=\"\"/>"+
			"</td>"+
		"</tr>"+
		"<tr>"+
	        "<td colspan=2>"+
	        	"<div style=\"text-align: center;\">"+
	        		"<button type=\"submit\" onclick=\"document.forms[0].elements['action'].value='save'; return true;\">Add</button>"+
	        	"</div>"+
	        "</td>"+
		"</tr>"+
	"</table>"+
"</form>");*/
}

%>

</div>
<%@include file="bottomNav.jspf"%>
</body>
</html>
		