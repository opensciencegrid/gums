/*
 * GUMSAuthZService.java
 *
 * Created on January 5, 2005, 6:03 PM
 */

package gov.bnl.gums.service;

import org.opensciencegrid.authz.service.BasicMappingAuthZService;
import org.opensciencegrid.authz.stubs.AuthorizationServiceBindingSkeleton;

/** The AuthZ service that implements the standard authorization interface.
 *
 * @author Gabriele Carcassi
 */
public class GUMSAuthZService extends AuthorizationServiceBindingSkeleton {
    static final long serialVersionUID = 1;
    
    /** Creates an authorization service using GUMS as the implementation. */
    public GUMSAuthZService() {
        super(new BasicMappingAuthZService(new GUMSAuthZServiceImpl()));
    }
    
}
