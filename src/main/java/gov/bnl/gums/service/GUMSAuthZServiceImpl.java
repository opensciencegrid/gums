/*
 * GUMSAuthZServiceImpl.java
 *
 * Created on January 5, 2005, 6:04 PM
 */

package gov.bnl.gums.service;

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import org.joda.time.DateTime;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;

import gov.bnl.gums.admin.GUMSAPI;
import gov.bnl.gums.admin.GUMSAPIImpl;

import org.opensciencegrid.authz.common.GridId;
import org.opensciencegrid.authz.common.LocalId;
import org.opensciencegrid.authz.service.GRIDIdentityMappingService;

/** Implements a GRID Identity Mapping Service by using GUMS logic.
 *
 * @author Gabriele Carcassi
 */
public class GUMSAuthZServiceImpl implements GRIDIdentityMappingService {
    private Log log = LogFactory.getLog(GUMSAuthZServiceImpl.class);
    private static GUMSAPI gums = new GUMSAPIImpl();
    
    public LocalId mapCredentials(GridId gridID) {
        log.debug("Mapping credentials on '" + gridID.getHostDN() + "' for '" + gridID.getUserDN() + "' coming as '" + gridID.getUserFQAN() + "' authenticated by '" + gridID.getUserFQANIssuer() + "'");
        if (gridID.getHostDN() == null) throw new RuntimeException("The request had a null host");
        String account = gums.mapUser(gridID.getHostDN(), gridID.getUserDN(), gridID.getUserFQAN());
        if (account == null) {
            log.debug("Denied access on '" + gridID.getHostDN() + "' for '" + gridID.getUserDN() + "' with fqan '" + gridID.getUserFQAN() + "'");
            return null;
        }
        else
        	log.debug("Credentials mapped on '" + gridID.getHostDN() + "' for '" + gridID.getUserDN() + "' with fqan '" + gridID.getUserFQAN() + "' to '" + account + "'");
        LocalId id = new LocalId();
        id.setUserName(account);
        return id;
    }
}    
