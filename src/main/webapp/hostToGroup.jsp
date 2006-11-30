<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@page import="gov.bnl.gums.*"%>
<jsp:useBean id="gums" scope="application" class="gov.bnl.gums.admin.GUMSAPIImpl" />
<%@ page import="gov.bnl.gums.hostToGroup.*" %>
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
	List h2GMappings = configuration.getHostToGroupMappings().values();
	Set g2AMappings = configuration.getGroupToAccountMapping().values();
	
	out.write(
"<form action=\"hostToGroup.jsp\" method=\"get\">"+
  "<table>");

	Iterator h2GMappingsIt = h2GMappings.iterator();
	int counter = 0;
	while(counter==0 || h2GMappingsIt.hasNext())
	{
		HostToGroupMapping h2GMapping = h2GMappingsIt.hasNext()?(HostToGroupMapping)h2GMappingsIt.next():null;
		if(counter==0 || h2GMapping instanceof WildcardHostToGroupMapping)
		{
			out.write(
   	"<tr>"+
		"<td width=\"25\">"+
			"<input type=\"submit\" name=\"Add"+counter+"\" value=\"+\" style=\"font-family:fixed;\">"+
			"<input type=\"submit\" name=\"Remove"+counter+"\" value=\"-\" style=\"font-family:fixed;\">"+
		"</td>"+
  		"<td>"+
	   		"<table id=\"form\" cellpadding=\"2\" cellspacing=\"2\">"+
	  			"<tr>"+
		    		"<td nowrap>"+
			    		"Map incoming host<br>"+
				    "</td>"+
				    "<td nowrap>"+
				    	"<input maxlength=\"256\" size=\"64\" name=\"host" + counter + "\" value=\"");
			
			boolean isCN = true;
			if(h2GMapping instanceof CertificateHostToGroupMapping) {
				if( ((CertificateHostToGroupMapping)h2GMapping).getCn()!=null ) {
					isCN = true;
					out.write(	((CertificateHostToGroupMapping)h2GMapping).getCn() );
				}
				else {
					isCN = false;
					out.write(	((CertificateHostToGroupMapping)h2GMapping).getDn() );
				}
			}
			else
				throw new Exception("Only CertificateHostToGroupMapping class is supported for a host group");
			
			out.write(
				    	"\"/>"+
						" cn<input type=\"radio\" name=\"filter_type" + counter + "\" value=\"cn\" " + (isCN?"checked":"") + ">"+
					    " dn<input type=\"radio\" name=\"filter_type" + counter + "\" value=\"dn\" " + (!isCN?"checked":"") + ">"+
				    "</td>"+
				"</tr>"+
				"<tr>"+
					"<td nowrap>to group(s)</td>"+
					"<td>"+
				    	"<select maxlength=\"32\" name=\"HM_groups" + counter + "\" size=\"5\" multiple>");
		
			Iterator g2AMappingsIt = g2AMappings.iterator();
			while(g2AMappingsIt!=null && g2AMappingsIt.hasNext())
			{
				String g2AMappingName = g2AMappings!=null?(String)g2AMappingsIt.next():null;

				out.write(
						"<option " + (h2GMapping.getGroupToAccountMappings().get(g2AMappingName)!=null?"selected":"") + ">" +
							g2AMappingName+
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
