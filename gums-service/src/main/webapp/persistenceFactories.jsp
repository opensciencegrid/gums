<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@page import="gov.bnl.gums.*"%>
<jsp:useBean id="gums" scope="application" class="gov.bnl.gums.admin.GUMSAPIImpl" />
<%@ page import="gov.bnl.gums.persistence.*" %>
<%@ page import="gov.bnl.gums.configuration.*" %>
<%@ page import="gov.bnl.gums.service.ConfigurationWebToolkit" %>
<%@ page import="java.util.*" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
<head>
 	<title>Persistence Factories</title>
 	<link href="gums.css" type="text/css" rel="stylesheet"/>
</head>
<body>
<%@include file="topNav.jspf"%>
<div id="title">
<h1><span>GUMS <%=gums.getVersion()%></span></h1>
<h3><span>GRID User Management System</h3>
<h2><span>Persistence Factories</span></h2>
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
Configures persistence factories.
</p>

<%
String message = null;

if (request.getParameter("command")==null || 
	"save".equals(request.getParameter("command")) || 
	"delete".equals(request.getParameter("command"))) {
	if ("save".equals(request.getParameter("command"))) {
		Configuration newConfiguration = (Configuration)configuration.clone();
		try{
			newConfiguration.removePersistenceFactory( request.getParameter("name") );
			newConfiguration.addPersistenceFactory( ConfigurationWebToolkit.parsePersistenceFactory(request) );
			gums.setConfiguration(newConfiguration);
			configuration = gums.getConfiguration();
			message = "<div class=\"success\">Persistence factory has been saved.</div>";
		}catch(Exception e){
			message = "<div class=\"failure\">Error saving persistence factory: " + e.getMessage() + "</div>";
		}
	}

	if ("delete".equals(request.getParameter("command"))) {
		Configuration newConfiguration = (Configuration)configuration.clone();
		try{
			String references = ConfigurationWebToolkit.getReferencesForPersistenceFactory(newConfiguration, request.getParameter("name"));
			if( references==null ) {
				if (newConfiguration.removePersistenceFactory( request.getParameter("name") )!=null) {
					gums.setConfiguration(newConfiguration);
					configuration = gums.getConfiguration();
					message = "<div class=\"success\">Persistence factory has been deleted.</div>";
				}
				else
					message = "<div class=\"failure\">Error deleting persistence factory</div>";
			}
			else
				message = "<div class=\"failure\">You cannot delete this item until removing references to it within " + references + ".</div>";
		}catch(Exception e){
			message = "<div class=\"failure\">Error deleting persistence factory: " + e.getMessage() + "</div>";
		}
	}
	
	Collection persistenceFactories = configuration.getPersistenceFactories().values();

	out.write(
"<table id=\"form\" cellpadding=\"2\" cellspacing=\"2\">");

	if(message!=null)
		out.write( "<tr><td colspan=\"2\">" + message + "</td></tr>" );
				
	Iterator persistenceFactoriesIt = persistenceFactories.iterator();
	while(persistenceFactoriesIt.hasNext()) {
		PersistenceFactory persistenceFactory = persistenceFactoriesIt.hasNext() ? (PersistenceFactory)persistenceFactoriesIt.next() : null;
%>
	   	<tr>
			<td width="1" valign="top">
				<form action="persistenceFactories.jsp" method="get">
					<input type="submit" style="width:80px" name="command" value="edit">
					<input type="submit" style="width:80px" name="command" value="delete" onclick="if(!confirm('Are you sure you want to delete this persistence factory?'))return false;">
					<input type="hidden" name="name" value="<%=persistenceFactory.getName()%>">
				</form>
			</td>
	  		<td align="left">
		   		<table class="configElement" width="100%">
		  			<tr>
			    		<td>
<%		    		
		if (persistenceFactory instanceof HibernatePersistenceFactory) {
%>
				    		Hibernate Persistence Factory:
				    		<a href="persistenceFactories.jsp?command=edit&name=<%=persistenceFactory.getName()%>">
				    			<%=persistenceFactory.getName()%>
				    		</a><br>
				    		Description: <%=persistenceFactory.getDescription()%><br>	
				    		MySQL URL: <%=((HibernatePersistenceFactory)persistenceFactory).getProperties().getProperty("hibernate.connection.url")%><br>
<%
		}
		else if (persistenceFactory instanceof LDAPPersistenceFactory) {
%>
				    		LDAP Persistence Factory:
				    		<a href="persistenceFactories.jsp?command=edit&name=<%=persistenceFactory.getName()%>">
				    			<%=persistenceFactory.getName()%>
				    		</a><br>
				    		Description: <%=persistenceFactory.getDescription()%><br>	
				    		LDAP URL: <%=((LDAPPersistenceFactory)persistenceFactory).getProperties().getProperty("java.naming.provider.url")%><br>
<%
		}
		else if (persistenceFactory instanceof LocalPersistenceFactory) {
%>
				    		Local Persistence Factory:
				    		<a href="persistenceFactories.jsp?command=edit&name=<%=persistenceFactory.getName()%>">
				    			<%=persistenceFactory.getName()%>
				    		</a><br>
				    		Description: <%=persistenceFactory.getDescription()%><br>
				    		MySQL URL: <%=((LocalPersistenceFactory)persistenceFactory).getMySQLProperties().getProperty("hibernate.connection.url")%><br>
				    		LDAP URL: <%=((LocalPersistenceFactory)persistenceFactory).getLDAPProperties().getProperty("java.naming.provider.url")%><br>
<%
		}
%>
						</td>
			      	</tr>
				</table>
			</td
			<td width="10"></td>
		</tr>
<%
	}
%>
		<tr>
	       <td colspan=2>
	        	<form action="persistenceFactories.jsp" method="get">
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
	
	Collection persistenceFactories = configuration.getPersistenceFactories().values();
	
	PersistenceFactory persistenceFactory = null;

	ArrayList persistenceFactoryTypes = new ArrayList();
	persistenceFactoryTypes.add(HibernatePersistenceFactory.getTypeStatic());
	persistenceFactoryTypes.add(LDAPPersistenceFactory.getTypeStatic());
	persistenceFactoryTypes.add(LocalPersistenceFactory.getTypeStatic());
	
	ArrayList authenticationTypes = new ArrayList();
	authenticationTypes.add("simple");
	authenticationTypes.add("none");
	
	ArrayList trueFalse = new ArrayList();
	trueFalse.add("true");
	trueFalse.add("false");
	
	if ("edit".equals(request.getParameter("command"))) {
		try {
			persistenceFactory = (PersistenceFactory)configuration.getPersistenceFactories().get( request.getParameter("name") );
		} catch(Exception e) {
			out.write( "<div class=\"failure\">Error getting persistence factory: " + e.getMessage() + "</div>" );
			return;
		}
	}

	if ("reload".equals(request.getParameter("command"))) {
		try{
			persistenceFactory = ConfigurationWebToolkit.parsePersistenceFactory(request);
		} catch(Exception e) {
			out.write( "<div class=\"failure\">Error reloading persistence factory: " + e.getMessage() + "</div>" );
			return;
		}
	}
		
	else if ("add".equals(request.getParameter("command"))) {
		persistenceFactory = new HibernatePersistenceFactory(configuration);
		((HibernatePersistenceFactory)persistenceFactory).setProperties( ConfigurationWebToolkit.getHibernateProperties(persistenceFactory, request, false) );
	}		
			
%>
<form action="persistenceFactories.jsp" method="get">
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
		    	<input maxlength="256" size="32" name="name" value="<%=(persistenceFactory.getName()!=null ? persistenceFactory.getName() : "")%>"/>
		    </td>
		</tr>
		<tr>
			<td nowrap style="text-align: right;">
	    		i.e.
		    </td
		    <td nowrap>
				myPersistenceFactory
		    </td>
		</tr>
<%
	}
	else {
%>
		    	<%=persistenceFactory.getName()%>
		    	<input type="hidden" name="name" value="<%=persistenceFactory.getName()%>"/>
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
				<input name="description" size="64" value="<%=persistenceFactory.getDescription()%>"/>
		    </td>
		</tr>	
		<tr>
			<td nowrap style="text-align: right;">
	    		Store Configuration:
		    </td>
		    <td nowrap>
				<input type="checkbox" name="storeConfig" <%=persistenceFactory.getStoreConfig()?"CHECKED":""%>>
		    </td>		    
		</tr>		
		<tr>
			<td nowrap style="text-align: right;">
	    		Type:
		    </td>
		    <td nowrap>
			<%=ConfigurationWebToolkit.createSelectBox("type", 
				persistenceFactoryTypes, 
				persistenceFactory.getType(),
				"onchange=\"document.forms[0].elements['command'].value='reload';document.forms[0].submit();\"",
				false)%>
		    </td>
		</tr>
<%	
	if (persistenceFactory instanceof HibernatePersistenceFactory) {
%>
	    <tr>
    		<td nowrap style="text-align: right;">
	    		JDBC MySQL URL:
		    </td>
		    <td nowrap>
		    	<input maxlength="256" size="64" name="mySqlUrl" value="<%=((HibernatePersistenceFactory)persistenceFactory).getProperties().getProperty("hibernate.connection.url")%>"/>
		    </td>
		</tr>
	    <tr>
    		<td nowrap style="text-align: right;">
	    		i.e.
		    </td>
		    <td nowrap>
		    	jdbc:mysql://localhost:3306/GUMS_1_3
		    </td>
		</tr>
		<tr>
    		<td nowrap style="text-align: right;">
	    		MySQL Username:
		    </td>
		    <td nowrap>
		    	<input maxlength="256" size="32" name="mySqlUsername" value="<%=((HibernatePersistenceFactory)persistenceFactory).getProperties().getProperty("hibernate.connection.username")%>"/>
		    </td>
		</tr>
	    <tr>
    		<td nowrap style="text-align: right;">
	    		i.e.
		    </td>
		    <td nowrap>
		    	gums
		    </td>
		</tr>		
		<tr>
    		<td nowrap style="text-align: right;">
	    		MySQL Password:
		    </td>
		    <td nowrap>
		    	<input type="password" maxlength="256" size="32" name="mySqlPassword" value="<%=((HibernatePersistenceFactory)persistenceFactory).getProperties().getProperty("hibernate.connection.password")%>"/>
		    </td>
		</tr>
<%
	}
	else if (persistenceFactory instanceof LDAPPersistenceFactory) {
%>
		<tr>
    		<td nowrap style="text-align: right;">
	    		LDAP URL:
		    </td>
		    <td nowrap>
		    	<input maxlength="256" size="64" name="ldapUrl" value="<%=((LDAPPersistenceFactory)persistenceFactory).getProperties().getProperty("java.naming.provider.url")%>"/>
		    </td>
		</tr>
	    <tr>
    		<td nowrap style="text-align: right;">
	    		i.e.
		    </td>
		    <td nowrap>
		    	ldaps://localhost/dc=usatlas,dc=bnl,dc=gov
		    </td>
		</tr>	
		<tr>
    		<td nowrap style="text-align: right;">
	    		LDAP Principle:
		    </td>
		    <td nowrap>
		    	<input maxlength="256" size="64" name="ldapPrincipal" value="<%=((LDAPPersistenceFactory)persistenceFactory).getProperties().getProperty("java.naming.security.principal")%>"/>
		    </td>
		</tr>
	    <tr>
    		<td nowrap style="text-align: right;">
	    		i.e.
		    </td>
		    <td nowrap>
		    	uid=gumsAdmin,ou=People,dc=usatlas,dc=bnl,dc=gov
		    </td>
		</tr>
		<tr>
    		<td nowrap style="text-align: right;">
	    		LDAP Password:
		    </td>
		    <td nowrap>
		    	<input type="password" maxlength="256" size="32" name="ldapCredentials" value="<%=((LDAPPersistenceFactory)persistenceFactory).getProperties().getProperty("java.naming.security.credentials")%>"/>
		    </td>
		</tr>
		<tr>
    		<td nowrap style="text-align: right;">
	    		Update group and email for every access: 
		    </td>
		    <td nowrap>
				<%=ConfigurationWebToolkit.createSelectBox("synch", 
					trueFalse, 
					((LDAPPersistenceFactory)persistenceFactory).isSynch()?"true":"false",
					null,
					false)%>
		    </td>
		</tr>
		<tr>
            <td nowrap style="text-align: right;">
                LDAP GUMS Tree:
            </td>
            <td nowrap>
                <input maxlength="256" size="32" name="gumsTree" value="<%=((LDAPPersistenceFactory)persistenceFactory).getGumsTree()%>"/> (relative to context in LDAP URL - optional)
            </td>
        </tr>
        <tr>
            <td nowrap style="text-align: right;">
                i.e.
            </td>
            <td nowrap>
                ou=GUMS,dc=usatlas,dc=bnl,dc=gov
            </td>
        </tr>  		
		<tr>
            <td nowrap style="text-align: right;">
                LDAP Group Tree:
            </td>
            <td nowrap>
                <input maxlength="256" size="32" name="groupTree" value="<%=((LDAPPersistenceFactory)persistenceFactory).getGroupTree()%>"/> (relative to context in LDAP URL - optional)
            </td>
        </tr>
        <tr>
            <td nowrap style="text-align: right;">
                i.e.
            </td>
            <td nowrap>
                ou=Group,dc=usatlas,dc=bnl,dc=gov
            </td>
        </tr>
		<tr>
            <td nowrap style="text-align: right;">
                LDAP People Tree:
            </td>
            <td nowrap>
                <input maxlength="256" size="32" name="peopleTree" value="<%=((LDAPPersistenceFactory)persistenceFactory).getPeopleTree()%>"/> (relative to context in LDAP URL - optional)
            </td>
        </tr>
        <tr>
            <td nowrap style="text-align: right;">
                i.e.
            </td>
            <td nowrap>
                ou=People,dc=usatlas,dc=bnl,dc=gov
            </td>
        </tr>
		<tr>
    		<td nowrap style="text-align: right;">
	    		LDAP GID Number Field:
		    </td>
		    <td nowrap>
		    	<input maxlength="256" size="16" name="gidNumberField" value="<%=((LDAPPersistenceFactory)persistenceFactory).getGidNumberField()%>"/> (group ID number field in 'People' object)
		    </td>
		</tr>
	    <tr>
    		<td nowrap style="text-align: right;">
	    		i.e.
		    </td>
		    <td nowrap>
		    	gidNumber
		    </td>
		</tr>
		<tr>
			<td nowrap style="text-align: right;">
                LDAP Group CN Field:
            </td>
            <td nowrap>
                <input maxlength="256" size="16" name="groupCnField" value="<%=((LDAPPersistenceFactory)persistenceFactory).getGroupCnField()%>"/> (group common name field)
            </td>
		</tr>
        <tr>
            <td nowrap style="text-align: right;">
                i.e.
            </td>
            <td nowrap>
                cn
            </td>
        </tr>
		<tr>
    		<td nowrap style="text-align: right;">
	    		LDAP UID Field:
		    </td>
		    <td nowrap>
		    	<input maxlength="256" size="16" name="uidField" value="<%=((LDAPPersistenceFactory)persistenceFactory).getUidField()%>"/> (user ID field)
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
		<tr>
    		<td nowrap style="text-align: right;">
	    		LDAP Member UID Field:
		    </td>
		    <td nowrap>
		    	<input maxlength="256" size="16" name="memberUidField" value="<%=((LDAPPersistenceFactory)persistenceFactory).getMemberUidField()%>"/> (fields containing user ID in 'Group' object)
		    </td>
		</tr>
	    <tr>
    		<td nowrap style="text-align: right;">
	    		i.e.
		    </td>
		    <td nowrap>
		    	memberUid
		    </td>
		</tr>
		<tr>
    		<td nowrap style="text-align: right;">
	    		LDAP Email Field:
		    </td>
		    <td nowrap>
		    	<input maxlength="256" size="16" name="emailField" value="<%=((LDAPPersistenceFactory)persistenceFactory).getEmailField()%>"/> (leave blank for none)
		    </td>
		</tr>
	    <tr>
    		<td nowrap style="text-align: right;">
	    		i.e.
		    </td>
		    <td nowrap>
		    	mail
		    </td>
		</tr>			
	    <tr>
    		<td nowrap style="text-align: right;">
	    		NOTE:
		    </td>
		    <td nowrap>
		    	For SSL access to ldap, ldap can made to be trusted by adding its certificate to $JAVA_HOME/lib/security/cacerts using keytool
		    </td>
		</tr>	
<%
	}
	else if (persistenceFactory instanceof LocalPersistenceFactory) {
%>
		<tr>
    		<td nowrap style="text-align: right;">
	    		JDBC MySQL URL:
		    </td>
		    <td nowrap>
		    	<input maxlength="256" size="64" name="mySqlUrl" value="<%=((LocalPersistenceFactory)persistenceFactory).getMySQLProperties().getProperty("hibernate.connection.url")%>"/>
		    </td>
		</tr>
	    <tr>
    		<td nowrap style="text-align: right;">
	    		i.e.
		    </td>
		    <td nowrap>
		    	jdbc:mysql://localhost:3306/GUMS_3
		    </td>
		</tr>
		<tr>
    		<td nowrap style="text-align: right;">
	    		MySQL Username:
		    </td>
		    <td nowrap>
		    	<input maxlength="256" size="32" name="mySqlUsername" value="<%=((LocalPersistenceFactory)persistenceFactory).getMySQLProperties().getProperty("hibernate.connection.username")%>"/>
		    </td>
		</tr>
	    <tr>
    		<td nowrap style="text-align: right;">
	    		i.e.
		    </td>
		    <td nowrap>
		    	gums
		    </td>
		</tr>
		<tr>
    		<td nowrap style="text-align: right;">
	    		MySQL Password:
		    </td>
		    <td nowrap>
		    	<input type="password" maxlength="256" size="32" name="mySqlPassword" value="<%=((LocalPersistenceFactory)persistenceFactory).getMySQLProperties().getProperty("hibernate.connection.password")%>"/>
		    </td>
		</tr>
		<tr>
    		<td nowrap style="text-align: right;">
	    		LDAP URL:
		    </td>
		    <td nowrap>
		    	<input maxlength="256" size="64" name="ldapUrl" value="<%=((LocalPersistenceFactory)persistenceFactory).getLDAPProperties().getProperty("java.naming.provider.url")%>"/>
		    </td>
		</tr>
	    <tr>
    		<td nowrap style="text-align: right;">
	    		i.e.
		    </td>
		    <td nowrap>
		    	ldaps://localhost/dc=usatlas,dc=bnl,dc=gov
		    </td>
		</tr>
		<tr>
    		<td nowrap style="text-align: right;">
	    		LDAP Principle:
		    </td>
		    <td nowrap>
		    	<input maxlength="256" size="64" name="ldapPrincipal" value="<%=((LocalPersistenceFactory)persistenceFactory).getLDAPProperties().getProperty("java.naming.security.principal")%>"/>
		    </td>
		</tr>
	    <tr>
    		<td nowrap style="text-align: right;">
	    		i.e.
		    </td>
		    <td nowrap>
		    	uid=gumsAdmin,ou=People,dc=usatlas,dc=bnl,dc=gov
		    </td>
		</tr>		
		<tr>
    		<td nowrap style="text-align: right;">
	    		LDAP Password:
		    </td>
		    <td nowrap>
		    	<input type="password" maxlength="256" size="32" name="ldapCredentials" value="<%=((LocalPersistenceFactory)persistenceFactory).getLDAPProperties().getProperty("java.naming.security.credentials")%>"/>
		    </td>
		</tr>
		<tr>
    		<td nowrap style="text-align: right;">
	    		Update group and email for every access:
		    </td>
		    <td nowrap>
			<%=ConfigurationWebToolkit.createSelectBox("synch", 
				trueFalse, 
				((LocalPersistenceFactory)persistenceFactory).isSynch()?"true":"false",
				null,
				false)%>
		    </td>
		</tr>
		<tr>
            <td nowrap style="text-align: right;">
                LDAP GUMS Tree:
            </td>
            <td nowrap>
                <input maxlength="256" size="32" name="gumsTree" value="<%=((LocalPersistenceFactory)persistenceFactory).getGumsTree()%>"/> (relative to context in LDAP URL - optional)
            </td>
        </tr>
        <tr>
            <td nowrap style="text-align: right;">
                i.e.
            </td>
            <td nowrap>
                ou=GUMS,dc=usatlas,dc=bnl,dc=gov
            </td>
        </tr>  		
		<tr>
            <td nowrap style="text-align: right;">
                LDAP Group Tree:
            </td>
            <td nowrap>
                <input maxlength="256" size="32" name="groupTree" value="<%=((LocalPersistenceFactory)persistenceFactory).getGroupTree()%>"/> (relative to context in LDAP URL - optional)
            </td>
		</tr>
        <tr>
            <td nowrap style="text-align: right;">
                i.e.
            </td>
            <td nowrap>
                ou=Group,dc=usatlas,dc=bnl,dc=gov
            </td>
        </tr>
        <tr>
        <td nowrap style="text-align: right;">
                LDAP People Tree:
            </td>
            <td nowrap>
                <input maxlength="256" size="32" name="peopleTree" value="<%=((LocalPersistenceFactory)persistenceFactory).getPeopleTree()%>"/> (relative to context in LDAP URL - optional)
            </td>
        </tr>
        <tr>
            <td nowrap style="text-align: right;">
                i.e.
            </td>
            <td nowrap>
                ou=People,dc=usatlas,dc=bnl,dc=gov
            </td>
        </tr>
		<tr>
    		<td nowrap style="text-align: right;">
	    		LDAP GID Number Field:
		    </td>
		    <td nowrap>
		    	<input maxlength="256" size="16" name="gidNumberField" value="<%=((LocalPersistenceFactory)persistenceFactory).getGidNumberField()%>"/> (group ID number field in 'People' object)
		    </td>
		</tr>
	    <tr>
    		<td nowrap style="text-align: right;">
	    		i.e.
		    </td>
		    <td nowrap>
		    	gidNumber
		    </td>
		</tr>
		<tr>
			<td nowrap style="text-align: right;">
                LDAP Group CN Field:
            </td>
            <td nowrap>
                <input maxlength="256" size="16" name="groupCnField" value="<%=((LocalPersistenceFactory)persistenceFactory).getGroupCnField()%>"/> (group common name field)
            </td>
		</tr>
        <tr>
            <td nowrap style="text-align: right;">
                i.e.
            </td>
            <td nowrap>
                cn
            </td>
        </tr>
		<tr>
    		<td nowrap style="text-align: right;">
	    		LDAP UID Field:
		    </td>
		    <td nowrap>
		    	<input maxlength="256" size="16" name="uidField" value="<%=((LocalPersistenceFactory)persistenceFactory).getUidField()%>"/> (user ID field)
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
		<tr>
    		<td nowrap style="text-align: right;">
	    		LDAP Member UID Field:
		    </td>
		    <td nowrap>
		    	<input maxlength="256" size="16" name="memberUidField" value="<%=((LocalPersistenceFactory)persistenceFactory).getMemberUidField()%>"/> (fields containing user ID in 'Group' object)
		    </td>
		</tr>
	    <tr>
    		<td nowrap style="text-align: right;">
	    		i.e.
		    </td>
		    <td nowrap>
		    	memberUid
		    </td>
		</tr>
		<tr>
    		<td nowrap style="text-align: right;">
	    		LDAP Email Field:
		    </td>
		    <td nowrap>
		    	<input maxlength="256" size="16" name="emailField" value="<%=((LocalPersistenceFactory)persistenceFactory).getEmailField()%>"/> (leave blank for none)
		    </td>
		</tr>
	    <tr>
    		<td nowrap style="text-align: right;">
	    		i.e.
		    </td>
		    <td nowrap>
		    	mail
		    </td>
		</tr>		
	    <tr>
			<td nowrap style="text-align: right;">
				NOTE:
			</td>
		    <td nowrap>
		    	For SSL access to ldap, ldap can made to be trusted by adding its certificate to $JAVA_HOME/lib/security/cacerts using keytool
		    </td>
		</tr>	
<%
	}
%>	
		<tr>
	        <td colspan=2>
				<%=ConfigurationWebToolkit.createDoSubmit(persistenceFactories, request)%>
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
