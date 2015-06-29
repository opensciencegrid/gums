<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@page import="gov.bnl.gums.*"%>
<%@ page import="gov.bnl.gums.account.*" %>
<%@ page import="gov.bnl.gums.configuration.*" %>
<%@ page import="gov.bnl.gums.service.ConfigurationWebToolkit" %>
<%@ page import="java.util.*" %>
<%  String command = request.getParameter("command");%>
<%  String accountMapperName = request.getParameter("accountMapper");%>
<%  String range = request.getParameter("range"); if (range!=null)range = range.trim();%>
<%  boolean recycle = Boolean.parseBoolean(request.getParameter("recycle"));%>
<jsp:useBean id="gums" scope="application" class="gov.bnl.gums.admin.GUMSAPIImpl" />
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
<head>
  <title>GUMS</title>
  <link href="gums.css" type="text/css" rel="stylesheet">
</head>
<body>
<%@include file="topNav.jspf"%>
<div id="title">
<h1><span>GUMS <%=gums.getVersion()%></span></h1>
<h3><span>GRID User Management System</h3>
<h2><span>Manage Pool Accounts</span></h2>
</div>
<%@include file="sideNav.jspf"%>
<div id="body">
<%-- <jsp:useBean id="beanInstanceName" scope="session" class="beanPackage.BeanClassName" /> --%>
<%-- <jsp:getProperty name="beanInstanceName"  property="propertyName" /> --%>
<p>
<%
	if ("add".equals(command)) {
		try {
			gums.addAccountRange2( accountMapperName, range );
			out.println("<div class=\"success\">Accounts have been added to the pool.</div>");
		} catch(Exception e) {
			out.println("<div class=\"failure\">Error saving pool account range: " + e.getMessage() + "</div>");
		}
	}
	else if ("remove".equals(command)) {
		try {
			gums.removeAccountRange( accountMapperName, range );
			out.println("<div class=\"success\">Accounts have been removed from the pool.</div>");
		} catch(Exception e) {
			out.println("<div class=\"failure\">Error removing pool account range: " + e.getMessage() + "</div>");
		}
	}
	else if ("unassign".equals(command)) {
		try {
			gums.unassignAccountRange( accountMapperName, range );
			out.println("<div class=\"success\">Accounts have been unassigned.</div>");
		} catch(Exception e) {
			out.println("<div class=\"failure\">Error unassigning accounts: " + e.getMessage() + "</div>");
		}
	}
	else if ("recycle".equals(command)) {
		try {
			gums.setRecyclable(accountMapperName, range, recycle);
			out.println("<div class=\"success\">Account recycle setting has been updated.</div>");
		} catch (Exception e) {
			out.println("<div class=\"failure\">Error setting recycle attribute: " + e.getMessage() + "</div>");
		}
	}
%>
</p>

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
Adds, removes, or unassigns accounts in pool.
</p>

<form action="accountRange_form.jsp" method="get">
  <input type="hidden" name="command">
  <table id="form" border="0" cellpadding="2" cellspacing="2" align="center">
    <tbody>
	  <tr>
        <td style="text-align: right;">Account Pool Mapper: </td>
        <td style="text-align: left;">
<%
		ArrayList accountMappers = new ArrayList();
                for (AccountMapper accountMapper : configuration.getAccountMappers().values()) {
			if (accountMapper instanceof AccountPoolMapper)
				accountMappers.add( ((AccountPoolMapper)accountMapper).getName()  );
		}

		out.write(
			ConfigurationWebToolkit.createSelectBox("accountMapper", 
				accountMappers, 
				(request.getParameter("accountMapper")!=null ? request.getParameter("accountMapper") : null),
				"onchange=\"document.forms[0].action='accountRange_form.jsp';document.forms[0].submit();\"",
				false) );
%>   
		</td>
	</tr>
<%
		if (accountMappers.size()>0) {
%>
	<tr>
        <td style="text-align: right;">Account Pool: </td>
        <td style="text-align: left;"><%=((AccountPoolMapper)configuration.getAccountMapper(request.getParameter("accountMapper")!=null ? request.getParameter("accountMapper") : (String)accountMappers.get(0))).getAccountPoolRoot()%>
		</td>
	</tr>
<%
	      	String assignments = gums.getPoolAccountAssignments(request.getParameter("accountMapper")!=null ? request.getParameter("accountMapper") : (String)accountMappers.get(0));
			if (!assignments.equals("")) {
%>
	<tr>
		<td style="text-align: right;">Current Assignments: </td>
		<td style="text-align: left;">
			<%=assignments%>
		</td>
    </tr>	
<%
			}
		}
%>
	<tr>
        <td style="text-align: right;">Range: </td>
        <td style="text-align: left;"><input type="text" name="range" maxlength="256" size="20"/></td>
	</tr>
	<tr>
        <td style="text-align: right;">i.e.: </td>
        <td style="text-align: left;">myAccount001-100</td>
	</tr>
	<tr>
		<td colspan="2" style="text-align: center;">
        	<button type="submit" onclick="if(document.forms[0].elements['range'].value==''){ alert('You must enter a range'); return false;} document.forms[0].elements['command'].value='add'">add</button>
        	<button type="submit" onclick="if(document.forms[0].elements['range'].value==''){ alert('You must enter a range'); return false;} if(!confirm('Are you sure you want to remove this account range?'))return false; document.forms[0].elements['command'].value='remove'">remove</button>
			<button type="submit" onclick="if(document.forms[0].elements['range'].value==''){ alert('You must enter a range'); return false;} if(!confirm('Are you sure you want to unassign accounts?'))return false; document.forms[0].elements['command'].value='unassign'">unassign</button>
		</td>
	</tr>
    </tbody>
  </table>
</form>

<%
if (accountMappers.size()>0) {
    String mapper = request.getParameter("accountMapper")!=null ? request.getParameter("accountMapper") : (String)accountMappers.get(0);
    List<? extends MappedAccountInfo> accountInfo = gums.getAccountInfo(mapper);
    if (accountInfo.size() > 0) {
%>
<p>
View assigned account information
</p>

<form action="accountRange_form.jsp" method="get">
  <input type="hidden" name="command">
  <input type="hidden" name="recycle">
  <input type="hidden" name="range">
  <input type="hidden" name="accountMapper" value="<%=mapper%>">
  <table id="form" border="1" cellpadding="2" cellspacing="2" align="center">
    <thead>
      <tr>
        <th>Account</th><th>DN</th><th>Last Use</th><th>Recyclable</th><th>Actions</th>
      </tr>
    </thead>
    <tbody>
<%
    for (MappedAccountInfo info : accountInfo) {
      if (info.getDn() != null) {
%>
      <tr>
        <td><%=info.getAccount()%></td>
        <td><%=info.getDn()%></td>
        <td><%=info.getLastuse()==null?"(Unknown)":info.getLastuse()%></td>
        <td><%=info.getRecycle()%></td>
        <td>
          <button type="submit" onclick="document.forms[1].elements['command'].value='unassign'; document.forms[1].elements['range'].value='<%=info.getAccount()%>';">unassign</button>
<%
          if (info.getRecycle()) {
%>
            <button type="submit" onclick="document.forms[1].elements['command'].value='recycle'; document.forms[1].elements['range'].value='<%=info.getAccount()%>'; document.forms[1].elements['recycle'].value='false';">disable recycle</button>
<%
          } else {
%>
            <button type="submit" onclick="document.forms[1].elements['command'].value='recycle'; document.forms[1].elements['range'].value='<%=info.getAccount()%>'; document.forms[1].elements['recycle'].value='true';">enable recycle</button>
<%
          }
%>
        </td>
      </tr>
<%
      }
    }
%>
    </tbody>
  </table>
</form>
<%
    } else {
%>
<p>
No accounts are assigned in this pool.
</p>
<%
    }
}
%>

</div>
<%@include file="bottomNav.jspf"%>
</body>
</html>
