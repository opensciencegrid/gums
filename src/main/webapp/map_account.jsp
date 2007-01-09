<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@page import="gov.bnl.gums.*"%>
<jsp:useBean id="gums" scope="application" class="gov.bnl.gums.admin.GUMSAPIImpl" />
<%  String hostname = request.getParameter("host");
  String DN = request.getParameter("DN");
  String FQAN = request.getParameter("FQAN");
  if ("".equals(FQAN)) FQAN = null;%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
<head>
  <title>Map User Output</title>
  <link href="gums.css" type="text/css" rel="stylesheet">
</head>
<body>
<%@include file="topNav.jspf"%>
<div id="title">
<h1><span>GUMS</span></h1>
<h2><span>Inverse Map user</span></h2>
</div>
<%@include file="sideNav.jspf"%>
<p>Account '<%= accountName %>' is inversely mapped to:</p>
  <table id="form" >
    <tbody>
      <tr>
        <td>
<% Collection dNs = gums.inverseMapUser(accountName));
	Iterator it = dNs.iterator();
	while(it.hasNext()) {
		String dN = (String)it.next();
		if(dNs.iterator().next()!=dN)
			out.write("<br>");
		out.write(dN);
	}		
%>
        </td>
      </tr>
    </tbody>
  </table>
<%

<BR><BR>
</div>
<%@include file="bottomNav.jspf"%>
</body>
</html>
