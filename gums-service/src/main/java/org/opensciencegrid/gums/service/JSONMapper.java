package org.opensciencegrid.gums.service;

import java.io.IOException;
import java.io.Writer;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.json.Json;
import javax.json.stream.JsonGenerator;

import gov.bnl.gums.AccountInfo;
import gov.bnl.gums.admin.GUMSAPIImpl;

public class JSONMapper extends HttpServlet {

    @Override
    public void init() throws ServletException
    {
        gums = new GUMSAPIImpl();
    }


    @Override
    public void destroy()
    {
    }


    private void errorResponse(HttpServletResponse response, int sc, String msg)
        throws IOException
    {
        Writer out = response.getWriter();
        JsonGenerator gen = Json.createGenerator(out);
        gen.writeStartObject()
            .write("result", "ERROR")
            .write("message", msg)
        .writeEnd();
        gen.close();
        response.setStatus(sc);
    }


    @Override
    public void doGet(HttpServletRequest request,
                      HttpServletResponse response)
        throws ServletException, IOException
    {
        String path_info = request.getPathInfo();
        if ((path_info == null) || !path_info.equals("/mapGridIdentity"))
        {
            errorResponse(response, response.SC_NOT_FOUND, "REST API not found; only known JSON API is currently /gums/json/mapGridIdentity");
            return;
        }

        String request_dn = request.getParameter("subject-x509-id");
        if (request_dn == null)
        {
            errorResponse(response, response.SC_BAD_REQUEST, "Missing required input parameter 'subject-x509-id' (DN of user)");
            return;
        }
        String voms_fqan = request.getParameter("voms-fqan");
        String hostname = request.getParameter("dns-host-name");
        if ((hostname != null) && gums.getConfiguration().getSimpleHostMatchingEnabled())
        {
            hostname = "/CN=" + hostname;
        }
        String host_dn = request.getParameter("resource-x509-id");
        if ((hostname == null) && (host_dn == null))
        {
            errorResponse(response, response.SC_BAD_REQUEST, "Either dns-host-name (hostname of service) or resource-x509-id (service's DN) must be specified.");
            return;
        }
        String voms_signing_subject = request.getParameter("voms-signing-subject");
        boolean verified = voms_signing_subject != null;

        AccountInfo account = null;
        try
        {
            account = gums.mapUser(host_dn == null ? hostname : host_dn, request_dn, voms_fqan, verified);
        }
        catch (Exception e)
        {
            errorResponse(response, response.SC_INTERNAL_SERVER_ERROR, "Error mapping grid identity: " + e.getMessage());
            return;
        }

        Writer out = response.getWriter();
        JsonGenerator gen = Json.createGenerator(out);
        gen.writeStartObject()
            .write("result", account == null ? "FAILED" : "OK");
        if (account != null)
        {
            gen.write("username", account.getUser());
        }
        if (account.getGroup() != null && !account.getGroup().equals(""))
        {
            try
            {
                gen.write("gid", Integer.parseInt(account.getGroup()));
            }
            catch (NumberFormatException _)
            {
                gen.write("groupname", account.getGroup());
            }
        }
        gen.writeEnd();
        gen.close();
    }

    private GUMSAPIImpl gums;
}
