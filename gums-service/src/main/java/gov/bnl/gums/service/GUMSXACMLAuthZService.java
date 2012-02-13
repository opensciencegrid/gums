/*
 * XACMLGUMSAuthZService.java
 *
 * Created on May 30, 2008
 */

package gov.bnl.gums.service;

import org.opensciencegrid.authz.xacml.service.*;
import org.opensciencegrid.authz.xacml.stubs.*;

/** The AuthZ service that implements the xacml authorization interface.
 *
 * @author Jay Packard
 */
public class GUMSXACMLAuthZService extends XACMLAuthorizationPortTypeSOAPBindingSkeleton {
    static final long serialVersionUID = 1;
    
    /** Creates an authorization service using GUMS as the implementation. */
    public GUMSXACMLAuthZService() {
        super(new BasicMappingXACMLAuthZService(new GUMSXACMLMappingServiceImpl()));
    }
    
}
