/*
 * GUMSAuthZServiceImpl.java
 *
 * Created on January 5, 2005, 6:04 PM
 */

package gov.bnl.gums.service;

import gov.bnl.gums.admin.GUMSAPI;
import gov.bnl.gums.admin.GUMSAPIImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
        log.debug("Denied access");
        if (account == null)
            return null;
        LocalId id = new LocalId();
        id.setUserName(account);
        log.debug("Credentials mapped on '" + gridID.getHostDN() + "' to '" + id.getUserName() + "' part of '" + id.getGroupName() + "'");
        return id;
    }
    
}
