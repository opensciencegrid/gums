<%@page import="gov.bnl.gums.*"%><jsp:useBean id="gums" scope="application" class="gov.bnl.gums.admin.GUMSAPIImpl" /><% 
	String account = null;
	try {
		account = gums.mapUser("/DC=com/DC=example/OU=Services/CN=example.site.com", "/DC=com/DC=example/OU=People/CN=Example User 12345", null);
		if ("test".equals(account))
			out.print("OK");
		else
			out.print("FAIL");
	} catch(Exception e) {
		out.print("FAIL");
	}
%>
