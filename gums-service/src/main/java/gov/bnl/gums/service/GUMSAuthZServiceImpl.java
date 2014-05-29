/*
 * GUMSAuthZServiceImpl.java
 *
 * Created on January 5, 2005, 6:04 PM
 */

package gov.bnl.gums.service;

import org.apache.log4j.Logger;

import gov.bnl.gums.AccountInfo;
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
    private Logger log = Logger.getLogger(GUMSAuthZServiceImpl.class);
    private static GUMSAPI gums = new GUMSAPIImpl();
    
    public LocalId mapCredentials(GridId gridID) {
        log.debug("Mapping credentials on '" + gridID.getHostDN() + "' for '" + gridID.getUserDN() + "' coming as '" + gridID.getUserFQAN() + "' authenticated by '" + gridID.getUserFQANIssuer() + "'");
        if (gridID.getHostDN() == null) throw new RuntimeException("The request had a null host");
        AccountInfo account = gums.mapUser(gridID.getHostDN(), gridID.getUserDN(), gridID.getUserFQAN());
        if (account == null || account.getUser() == null) {
            log.debug("Denied access on '" + gridID.getHostDN() + "' for '" + gridID.getUserDN() + "' with fqan '" + gridID.getUserFQAN() + "'");
            return null;
        }
        else
        	log.debug("Credentials mapped on '" + gridID.getHostDN() + "' for '" + gridID.getUserDN() + "' with fqan '" + gridID.getUserFQAN() + "' to '" + account + "'");
        LocalId id = new LocalId();
        id.setUserName(account.getUser());
        return id;
    }
}    
