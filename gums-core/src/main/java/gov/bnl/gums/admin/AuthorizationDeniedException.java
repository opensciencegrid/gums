/*
 * AuthorizationDeniedException.java
 *
 * Created on January 14, 2005, 9:12 AM
 */

package gov.bnl.gums.admin;

/**
 * Exception class for indicating a denied authorization
 *
 * @author Gabriele Carcassi, Jay Packard
 */
public class AuthorizationDeniedException extends RuntimeException {
    
    /** 
     * Creates a new instance of AuthorizationDeniedException
     */
    public AuthorizationDeniedException() {
        super("You are not authorized to perform this function.  Contact your gums administrator if access is needed.");
    }
    
}
