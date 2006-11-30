<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@page import="gov.bnl.gums.*"%>
<jsp:useBean id="gums" scope="application" class="gov.bnl.gums.admin.GUMSAPIImpl" />
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
<head>
  <title>addAccountRange Output</title>
  <link href="gums.css" type="text/css" rel="stylesheet">
</head>
<body>
<%@include file="topNav.jspf"%>
<div id="title">
<h1><span>GUMS</span></h1>
<h2><span>Manual Group Mappings</span></h2>
</div>
<%@include file="sideNav.jspf"%>
<div id="body">
<p>

<%  
	if (action.equals("add"))
		gums.manualGroupAdd(persistenceFactory, groupName, DN);
	if (action.equals("remove"))
		gums.manualGroupAdd(persistenceFactory, groupName, DN);
	if (action.equals("add"))
		out.write("Account(s) have been successfully added to the pool!");
	else if (action.equals("remove"))
		out.write("Account(s) have been successfully removed from the pool!");
%>
</p>

</div>
<%@include file="bottomNav.jspf"%>
</body>
</html>