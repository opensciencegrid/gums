<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@page import="gov.bnl.gums.*"%>
<%@page import="gov.bnl.gums.account.*"%>
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
<h2><span>Add range of accounts to a pool</span></h2>
</div>
<%@include file="sideNav.jspf"%>
<div id="body">
<p>

<% gums.poolAddAccount(persistenceFactory, ((AccountPoolMapper)gums.getConfiguration().getAccountMappers( accountMapper )).getAccountPool(), range); %>
Accounts have been successfully added to the pool!
</p>

</div>
<%@include file="bottomNav.jspf"%>
</body>
</html>