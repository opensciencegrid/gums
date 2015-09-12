package org.opensciencegrid.gums.service;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.json.Json;
import javax.json.stream.JsonGenerator;

import gov.bnl.gums.GridUser;
import gov.bnl.gums.AccountInfo;
import gov.bnl.gums.account.MappedAccountInfo;
import gov.bnl.gums.account.AccountMapper;
import gov.bnl.gums.account.AccountPoolMapper;
import gov.bnl.gums.account.ManualAccountMapper;
import gov.bnl.gums.userGroup.UserGroup;
import gov.bnl.gums.userGroup.ManualUserGroup;
import gov.bnl.gums.admin.GUMSAPIImpl;
import gov.bnl.gums.configuration.Configuration;


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
        if (path_info == null)
        {
            errorResponse(response, response.SC_NOT_FOUND, "REST API not specified; known JSON APIs are /gums/json/mapGridIdentity, /gums/json/manualMapper, /gums/json/userGroup, /gums/json/poolMapper, and /gums/json/version");
        }
        else if (path_info.equals("/mapGridIdentity"))
        {
            doMap(request, response);
        }
        else if (path_info.equals("/poolMapper"))
        {
            doMapper(request, response);
        }
        else if (path_info.equals("/manualMapper"))
        {
            doManualMapper(request, response);
        }
        else if (path_info.equals("/userGroup"))
        {
            doUserGroup(request, response);
        }
        else if (path_info.equals("/version"))
        {
            doVersion(request, response);
        }
        else
        {
            errorResponse(response, response.SC_NOT_FOUND, "REST API not found; only known JSON APIs are currently /gums/json/mapGridIdentity, /gums/json/manualMapper, /gums/json/userGroup, /gums/json/poolMapper, and /gums/json/version");
        }
    }


    private void doVersion(HttpServletRequest request,
                      HttpServletResponse response)
        throws ServletException, IOException
    {
        Writer out = response.getWriter();
        JsonGenerator gen = Json.createGenerator(out);
        String version = gums.getVersion();
        if (version == null) {version = "?";}
        gen.writeStartObject()
           .write("result", version.equals("?") ? "FAILED" : "OK")
           .write("version", version)
           .writeEnd();
        gen.close();
    }


    private void doUserGroup(HttpServletRequest request,
                          HttpServletResponse response)
        throws ServletException, IOException
    {
        Configuration configuration = null;
        try
        {
            configuration = gums.getConfiguration();
        }
        catch (Exception e)
        {
            errorResponse(response, response.SC_BAD_REQUEST, "Failed to load GUMS configuration: " + e.getMessage());
        }

        String group = request.getParameter("group");
        if (group == null)
        {
            errorResponse(response, response.SC_BAD_REQUEST, "Missing required input parameter 'group' (name of user group)");
            return;
        }
        List<GridUser> users = null;
        try
        {
            Map<String, UserGroup> userGroups = configuration.getUserGroups();
            UserGroup userGroupGeneric = userGroups.get(group);
            if (userGroupGeneric == null)
            {
                errorResponse(response, response.SC_BAD_REQUEST, "Requested user group (" + group + ") is not known.");
                return;
            }
            users = userGroupGeneric.getMemberList();
        }
        catch (Exception e)
        {
            errorResponse(response, response.SC_INTERNAL_SERVER_ERROR, "Error retrieving user group: " + e.getMessage());
        }
        Writer out = response.getWriter();
        JsonGenerator gen = Json.createGenerator(out);
        gen.writeStartObject()
            .write("result", (users != null) ? "OK" : "FAILED");
        if (users != null)
        {
            gen.writeStartArray("users");
            for (GridUser user : users)
            {
                if (user.getCertificateDN() == null) {continue;}
                gen.writeStartObject();
                gen.write("dn", user.getCertificateDN());
                if (user.getEmail() != null)
                {
                    gen.write("email", user.getEmail());
                }
                if (user.getVoFQAN() != null)
                {
                    gen.write("fqan", user.getVoFQAN().getFqan());
                }
                gen.writeEnd();
            }
            gen.writeEnd();
        }
        gen.writeEnd();
        gen.close();
    }

    private void doManualMapper(HttpServletRequest request,
                          HttpServletResponse response)
        throws ServletException, IOException
    {
        Configuration configuration = null;
        try
        {
            configuration = gums.getConfiguration();
        }
        catch (Exception e)
        {
            errorResponse(response, response.SC_BAD_REQUEST, "Failed to load GUMS configuration: " + e.getMessage());
        }

        String mapper = request.getParameter("mapper");
        if (mapper == null)
        {
            errorResponse(response, response.SC_BAD_REQUEST, "Missing required input parameter 'mapper' (name of manual account mapper)");
            return;
        }
        Map<String, String> entries = null;
        try
        {
            Map<String, AccountMapper> accountMappers = configuration.getAccountMappers();
            AccountMapper accountMapperGeneric = accountMappers.get(mapper);
            if (accountMapperGeneric == null)
            {
                errorResponse(response, response.SC_BAD_REQUEST, "Requested manual account mapper (" + mapper + ") is not known.");
                return;
            }
            if (!(accountMapperGeneric instanceof ManualAccountMapper))
            {
                errorResponse(response, response.SC_BAD_REQUEST, "Requested account mapper (" + mapper + ") is not a manual account mapper.");
                return;
            }
            ManualAccountMapper manualAccountMapper = (ManualAccountMapper)accountMapperGeneric;
            entries = manualAccountMapper.getAccountMap();
        }
        catch (Exception e)
        {
            errorResponse(response, response.SC_INTERNAL_SERVER_ERROR, "Error retrieving manual mapper: " + e.getMessage());
        }
        Writer out = response.getWriter();
        JsonGenerator gen = Json.createGenerator(out);
        gen.writeStartObject()
            .write("result", (entries != null) ? "OK" : "FAILED");
        if (entries != null)
        {
            gen.writeStartArray("accounts");
            for (Map.Entry<String, String> entry : entries.entrySet())
            {
                if ((entry.getKey() == null) || (entry.getValue() == null)) {continue;}
                gen.writeStartObject();
                gen.write("dn", entry.getKey());
                gen.write("username", entry.getValue());
                gen.writeEnd();
            }
            gen.writeEnd();
        }
        gen.writeEnd();
        gen.close();
    }


    private void doMapper(HttpServletRequest request,
                          HttpServletResponse response)
        throws ServletException, IOException
    {
        String pool = request.getParameter("pool");
        if (pool == null)
        {
            errorResponse(response, response.SC_BAD_REQUEST, "Missing required input parameter 'pool' (name of pool account mapper)");
            return;
        }
        List<? extends MappedAccountInfo> accountInfo = null;
        try
        {
            accountInfo = gums.getAccountInfo(pool);
        }
        catch (Exception e)
        {
            errorResponse(response, response.SC_INTERNAL_SERVER_ERROR, "Error retrieving pool map: " + e.getMessage());
            return;
        }
        Writer out = response.getWriter();
        JsonGenerator gen = Json.createGenerator(out);
        boolean gotInfo = (accountInfo != null);
        gen.writeStartObject()
            .write("result", gotInfo ? "OK" : "FAILED");
        if (gotInfo)
        {
            gen.writeStartArray("accounts");
            for (MappedAccountInfo info : accountInfo)
            {
                gen.writeStartObject();
                if (info.getDn() != null)
                {
                    gen.write("dn", info.getDn());
                }
                if (info.getAccount() != null)
                {
                    gen.write("username", info.getAccount());
                }
                if (info.getLastuse() != null)
                {
                    gen.write("last-use", info.getLastuse().toString());
                }
                gen.write("recycle", info.getRecycle());
                gen.writeEnd();
            }
            gen.writeEnd();
        }
        gen.writeEnd();
        gen.close();
    }


    private void doMap(HttpServletRequest request,
                       HttpServletResponse response)
        throws ServletException, IOException
    {

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
        boolean gotAccount = (account != null) && (account.getUser() != null) && !account.getUser().equals("");
        gen.writeStartObject()
            .write("result", gotAccount ? "OK" : "FAILED");
        if (gotAccount)
        {
            gen.write("username", account.getUser());
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
        }
        gen.writeEnd();
        gen.close();
    }

    private GUMSAPIImpl gums;
}
