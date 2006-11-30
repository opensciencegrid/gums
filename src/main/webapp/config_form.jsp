<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@page import="gov.bnl.gums.*"%>
<jsp:useBean id="gums" scope="application" class="gov.bnl.gums.admin.GUMSAPIImpl" />
<%@ page import="gov.bnl.gums.CertificateHostGroup" %>
<%@ page import="gov.bnl.gums.WildcardHostGroup" %>
<%@ page import="gov.bnl.gums.service.ConfigurationWebToolkit" %>
<%@ page import="java.util.*" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
<head>
 	<title>GUMS Configuration</title>
 	<link href="gums.css" type="text/css" rel="stylesheet"/>
</head>
<body>
<%@include file="topNav.jspf"%>
<div id="title">
<h1><span>GUMS</span></h1>
<h2><span>GUMS Configuration</span></h2>
</div>
<%@include file="sideNav.jspf"%>
<div id="body">
<p>
<%-- <jsp:useBean id="beanInstanceName" scope="session" class="beanPackage.BeanClassName" /> --%>
<%-- <jsp:getProperty name="beanInstanceName"  property="propertyName" /> --%>

Configures host to group mappings, group to account manager mappings, and persistence managers.
</p>

<%
String errorMessage = null;
Configuration configuration;
if( request.getParameter("save")!=null )
{
	try{
		configuration = ConfigurationWebToolkit.loadConfiguration(request);
		gums.setConfiguration(configuration);
		out.write(
"<p>Configuration has been successfully saved!</p>");
	}catch(Exception e){
		errorMessage =
"<p>" + e.getMessage() + "</p>";
		out.write(errorMessage);
	}
}

if( request.getParameter("save")==null || errorMessage!=null )
{
	configuration = gums.getConfiguration();
	List hostMappers = configuration.getHostGroup();
	Set groupMappers = configuration.getGroupMapping().keySet();
	List accountManagers = configuration.getAccountManagers(); 
	
	out.write(
"<form action=\"config.jsp\" method=\"get\">"+
  "<table>");

	Iterator hostMapperIt = hostMappers.iterator();
	int counter = 0;
	while(counter==0 || hostMapperIt.hasNext())
	{
		HostGroup hostMapper = hostMapperIt.hasNext()?(HostGroup)hostMapperIt.next():null;
		if(counter==0 || hostMapper instanceof WildcardHostGroup)
		{
			out.write(
   	"<tr>"+
		"<td width=\"25\">"+
			"<input type=\"submit\" name=\"HM_Add"+counter+"\" value=\"+\" style=\"font-family:fixed;\">"+
			"<input type=\"submit\" name=\"HM_Remove"+counter+"\" value=\"-\" style=\"font-family:fixed;\">"+
		"</td>"+
  		"<td>"+
	   		"<table id=\"form\" cellpadding=\"2\" cellspacing=\"2\">"+
	  			"<tr>"+
		    		"<td nowrap>"+
			    		"Map incoming host<br>"+
				    "</td>"+
				    "<td nowrap>"+
				    	"<input maxlength=\"256\" size=\"64\" name=\"HM_hostName" + counter + "\" value=\"");
			
			boolean isCN = true;
			if(hostMapper instanceof CertificateHostGroup) {
				if( ((CertificateHostGroup)hostMapper).getCn()!=null ) {
					isCN = true;
					out.write(	((CertificateHostGroup)hostMapper).getCn() );
				}
				else {
					isCN = false;
					out.write(	((CertificateHostGroup)hostMapper).getDn() );
				}
			}
			else
				throw new Exception("Only certificateHostGroup class is supported for a host group");
			
			out.write(
				    	"\"/>"+
						" cn<input type=\"radio\" name=\"filter_type" + counter + "\" value=\"cn\" " + (isCN?"checked":"") + ">"+
					    " dn<input type=\"radio\" name=\"filter_type" + counter + "\" value=\"dn\" " + (!isCN?"checked":"") + ">"+
				    "</td>"+
				"</tr>"+
				"<tr>"+
					"<td nowrap>to group mapper(s)</td>"+
					"<td>"+
				    	"<select maxlength=\"32\" name=\"HM_groups" + counter + "\" size=\"5\" multiple>");
		
			Iterator groupMapperIt = groupMappers.iterator();
			while(hostMapper!=null && groupMapperIt.hasNext())
			{
				String groupMapperName = groupMappers!=null?(String)groupMapperIt.next():null;

				out.write(
						"<option " + (hostMapper.containsGroupMapper(groupMapperName)?"selected":"") + ">" +
							groupMapperName+
						"</option>");
			}
		
			out.write(
   						"</select>"+
  				     "</td>"+
		      	"</tr>"+
			"</table>"+
		"</td>"+
		"<td width=\"25\"></td>"+
	"</tr>");
	
			counter++;
		}
		else{
			throw new Exception("Currently only supports wildcard host types");		
		}
	} // end of host mapper while loop

/*	out.write(
    "<tr><td colspan=\"2\"><h3>Group to Account Mappings</h3></td></tr>");

	Iterator groupMapperIt = groupMappers.iterator();
	counter = 0;
	while(counter==0 || groupMapperIt.hasNext())
	{
		GroupMapper groupMapper = groupMapperIt.hasNext()?(GroupMapper)groupMapperIt.next():null;     
	
		out.write(
	"<tr>"+
		"<td width=\"25\">"+
			"<input type=\"submit\" name=\"HM_Add"+counter+"\" value=\"+\" style=\"font-family:fixed;\">"+
			"<input type=\"submit\" name=\"HM_Remove"+counter+"\" value=\"-\" style=\"font-family:fixed;\">"+
		"</td>"+
   		"<td>"+
    		"<table id=\"subform\" border=\"0\" cellpadding=\"2\" cellspacing=\"0\">"+
   				"<tr>"+
		    		"<td style=\"text-align: left;wrap: none\">"+
			    		"Map group <input maxlength=\"32\" name=\"GM_name" + counter + "\">"+
			    		groupMapper!=null?groupMapper.getName():""+
			    		"</input><br/>"+
			    		"where certificate matches <input maxlength=\"32\" name=\"GM_certificate" + counter + "\">"+
			    		groupMapper!=null?groupMapper.getCertificate():""+
			    		"</input><br/>"+
				    	"&nbsp;&nbsp;&nbsp;and VO membership approved by VO manager "+
				    	"<select maxlength=\"32\" name=\"GM_vOManager" + counter + "\">");
	
		Iterator managerIt = managers.iterator();
		while(managerIt.hasNext())
		{
			Manager manager = (Manager)managerIt.next();
			if(manager instanceof UserGroup)
			{
				out.write(	
							"<option "+(groupMapper!=null && groupMapper.isVOManager(manager.getName()))?"selected":""+">"+
								manager.getName()+
							"</option>");
			}
		}
			
		out.write(
			    		"</select><br/>"+
				    	"to account obtained from account manager(s) ");
		
		List curGroupAccountManagers = groupMapper!=null?groupMapper.getAccountManagers():null;
		Iterator curGroupAccountManagersIt = curGroupAccountManagers!=null?thisGroupMappers.iterator():null;
		int subCounter = 0;
		while(true)
		{
			Manager curGroupAccountManager = curGroupAccountManagers!=null?curGroupAccountManagersIt.next():null;

			if(subCounter==0)
				out.write(", ");
			out.write(	"<select maxlength=\"32\" name=\"GM_accountManagers" + counter + "_" + subCounter + "\" onchange=\"submit()\">");
			
			Iterator managerIt = managers.iterator();
			while(managerIt.hasNext())
			{
				out.write(	"<option "+curGroupAccountManager==null?"selected":""+"></option>");

				Manager manager = groupMapperIt.next()
				if(manager instanceof AccountManager)
				{
					out.write(
							"<option "+(curGroupAccountManager && curGroupAccountManager.getName().equals(manager.getName()))?"selected":""+">"+
								groupMapper.getName()+
							"</option>");
				}
			}

			out.write(	"</select>");
			
			if(curGroupAccountManager==null)
				break;
		}

		out.write(
				      	"using persistence manager "+
				      	"<select maxlength=\"32\" name=\"GM_persistenceManager" + counter + "\">");
	
		Iterator managerIt = managers.iterator();
		while(managerIt.hasNext())
		{
			Manager manager = (Manager)managerIt.next();
			if(manager instanceof PersistenceFactory)
			{
				out.write(
							"<option "+(groupMapper!=null && groupMapper.isPersistenceManager(manager.getName()))?"selected":""+">"+
								manager.getName()+
							"</option>");
			}
		}
	
		out.write(	
						"</select>"+
				       	"to cache VO memberships"+
				   	"</td>"+
		      	"</tr>"+
			"</table>"+
		"</td>"+
		"<td width=\"25\"></td>"+
     "</tr>");
	
		counter++;
	} // end of group mapper while loop
	
	out.write("<tr><td colspan="2"><h3>Managers</h3></td></tr>");
   	
	Iterator managerIt = managers.iterator();
	counter = 0;
	while(counter==0 || managerIt.hasNext())
	{
		UserGroup manager = managerIt.hasNext()?(UserGroup)managerIt.next():null;     
	
		out.write(
	"<tr>"+
		"<td width=\"25\">"+
			"<input type=\"submit\" name=\"HM_Add"+counter+"\" value=\"+\" style=\"font-family:fixed;\">"+
			"<input type=\"submit\" name=\"HM_Remove"+counter+"\" value=\"-\" style=\"font-family:fixed;\">"+
		"</td>"+
   		"<td>"+
    		"<table id=\"subform\" border=\"0\" cellpadding=\"2\" cellspacing=\"0\">"+
   				"<tr>"+
   					"<td>"+
   						"<table border=\"0\" cellpadding=\"2\" cellspacing=\"0\">"+
   							
   							"<tr><td><table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\"><tr>"+
								"<td width=\"150\" style=\"text-align: left;wrap: none\">Name:</td>"+
								"<td style=\"text-align: left;wrap: none\">"+
									"<input maxlength=\"32\" name=\"M_name" + counter + "\">"+
										manager.getName()+								
									"</input>"+
								"</td>"+											
							"</tr></table></td></tr>"+
							
							"<tr><td><table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\"><tr>"+
								"<td width=\"150\" style=\"text-align: left;wrap: none\">Type:</td>"+
								"<td style=\"text-align: left;wrap: none\">"+
									"<select maxlength=\"32\" name=\"M_type" + counter + "\" onchange=\"managerTypeChanged()\">"+
										"<option "+(manager instanceof UserGroup)?"selected":""+">VO manager</option>");
										"<option "+(manager instanceof AccountMapper)?"selected":""+">account manager</option>");
										"<option "+(manager instanceof PersistenceFactory)?"selected":""+">persistence manager</option>");
									"</select>"+
								"</td>"+										
							"</tr></table></td></tr>"+
							"<tr><td><table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\"><tr>"+
								"<td width=\"150\" style=\"text-align: left;wrap: none\">Subtype:</td>"+
								"<td style=\"text-align: left;wrap: none\">"+
									"<select maxlength=\"32\" name=\"M_subType" + counter + "\">");

		if(manager instanceof UserGroup)
		{
			out.write(					"<option "+(manager instanceof VOMSGroup)?"selected":""+">VOMS</option>"+
										"<option "+(manager instanceof LDAPGroup)?"selected":""+">LDAP</option>");
		}
		else if(manager instanceof AccountMapper)
		{
			out.write(					"<option "+(manager instanceof ManualAccountMapper)?"selected":""+">Manual</option>"+
										"<option "+(manager instanceof NISAccountMapper)?"selected":""+">NIS</option>"+
										"<option "+(manager instanceof GecosAccountMapper)?"selected":""+">Gecos</option>"+
										"<option "+(manager instanceof GroupMapper)?"selected":""+">Group</option>");
		}
		if(manager instanceof PersistenceFactory)
		{
			out.write(					"<option "+(manager instanceof BNLPersistenceFactory)?"selected":""+">BNL</option>"+
										"<option "+(manager instanceof MySQLPersistenceFactory)?"selected":""+">MySQL</option>");
		}

		out.write(					"</select>"+
								"</td>"+		
							"</tr></table></td></tr>");

		if( manager.getURL() )
		{
			out.write(		"<tr><td><table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\"><tr>"+
					   		"<td width=\"150\" style=\"text-align: left;wrap: none\">URL:</td>"+
							"<td style=\"text-align: left;wrap: none\">"+"<input maxlength=\"32\" name=\"M_uRL" + counter "\">"+manager.getURL()+"</input>"+
							"</td></tr></table></td></tr>");
		}
		
		if( manager.getUsername() )
		{
			out.write(		"<tr><td><table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\"><tr>"+
					   		"<td width=\"150\" style=\"text-align: left;wrap: none\">URL:</td>"+
							"<td style=\"text-align: left;wrap: none\">"+"<input maxlength=\"32\" name=\"M_username" + counter "\">"+manager.getUsername()+"</input>"+
							"</td></tr></table></td></tr>");
		}
	
		if( manager.getPassword() )
		{
			out.write(		"<tr><td><table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\"><tr>"+
					   		"<td width=\"150\" style=\"text-align: left;wrap: none\">Manager:</td>"+
							"<td style=\"text-align: left;wrap: none\">"+"<input maxlength=\"32\" name=\"M_password" + counter "\">"+manager.getPassword()+"</input>"+
							"</td></tr></table></td></tr>");
		}
			
		if( manager.getPersistenceManager() )
		{
			out.write(		"<tr><td><table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\"><tr>"+
					   		"<td width=\"150\" style=\"text-align: left;wrap: none\">Username:</td>"+
							"<td style=\"text-align: left;wrap: none\">"+"<input maxlength=\"32\" name=\"M_persistenceManager" + counter "\">"+manager.getPersistenceManager()+"</input>"+
							"</td></tr></table></td></tr>");
		}

		out.write(		"</table>"+
   					"</td>"+
		      	"</tr>"+
			"</table>"+
		"</td>"+
		"<td width=\"25\"></td>"+
     "</tr>");
	} // end of manager while loop
	*/
	out.write(
	"<tr>"+
        "<td colspan=2><div style=\"text-align: center;\"><button type=\"submit\" name=\"save\">Save</button></div>"+
        "</td>"+
	"</tr>"+
  "</table>"+
"</form>");
} // end of non-postback else

%>

</div>
<%@include file="bottomNav.jspf"%>
</body>
</html>
