/*
 * AuthorizationDeniedException.java
 *
 * Created on January 14, 2005, 9:12 AM
 */

package gov.bnl.gums.admin;

/**
 *
 * @author carcassi
 */
public class AuthorizationDeniedException extends RuntimeException {
    
    /** Creates a new instance of AuthorizationDeniedException */
    public AuthorizationDeniedException() {
        super("Authorization denied");
    }
    
}
