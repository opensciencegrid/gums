/*
 * GridUser.java
 *
 * Created on August 31, 2004, 4:00 PM
 */

package gov.bnl.gums;

import org.apache.log4j.Logger;

/** 
 * Represent a GRID Identity in GUMS, which is a certificate with its DN and FQAN.
 *
 * @author  Gabriele Carcassi, Jay Packard
 */
public class GridUser {
    private Logger log = Logger.getLogger(GridUser.class);
    private String certificateDN;
    private FQAN voFQAN;
    private String email;
    
    /**
     * Creates a GRID credentail with no DN and FQAN.
     */
    public GridUser() {
    }
    
    /**
     * Creates a GRID credential with DN.
     */
    public GridUser(String userDN) {
    	this(userDN, null, null, false);
    }
    
    /**
     * Creates a GRID credential with DN and FQAN.
     */
    public GridUser(String userDN, String fqan) {
        this(userDN, fqan, null, true);
    }
    
    /**
     * Creates a GRID credential with DN and FQAN.
     */
    public GridUser(String userDN, String fqan, boolean enforceFqanWellFormedness) {
        this(userDN, fqan, null, enforceFqanWellFormedness);
    }
 
    /**
     * Creates a new object representing a Grid credential.
     * 
     * @param userDN the DN of the user certificate (i.e. "/DC=org/DC=doegrids/OU=People/CN=John Smith")
     * @param fqan The Fully Qualified Attribute name (i.e. "/atlas/production/Role=Leader")
     * @param email
     */
    public GridUser(String userDN, String fqan, String email) {
    	this(userDN, fqan, email, true);
    }
    
    /**
     * Creates a new object representing a Grid credential.
     * 
     * @param userDN the DN of the user certificate (i.e. "/DC=org/DC=doegrids/OU=People/CN=John Smith")
     * @param fqan The Fully Qualified Attribute name (i.e. "/atlas/production/Role=Leader")
     * @param email
     */
    public GridUser(String userDN, String fqan, String email, boolean enforceFqanWellFormedness) {
        setCertificateDN(userDN);
        if (fqan!=null && fqan.length()>0)
        	setVoFQAN(new FQAN(fqan, enforceFqanWellFormedness));
       	setEmail(email);
    }

  /**
     * @param user
     * @return true if user DN element matches
     */
    public int compareDn(GridUser user) {
		if (this.certificateDN == null || user.certificateDN == null)
			return (this.certificateDN==user.certificateDN ? 0 : (user.certificateDN==null ? -1: 1));
    	return this.compareDn( user.getCertificateDN() );
    }
    
    /**
     * @param userDn
     * @return true if user DN element matches
     */
    public int compareDn(String userDn) {
		if (this.certificateDN == null || userDn == null)
			return (this.certificateDN==userDn ? 0 : (userDn==null ? -1: 1));
    	return this.certificateDN.compareTo( userDn );//compareToIgnoreCase( userDn );
    }
    
    /**
     * A GridUser will be equal only to another GridUser with the same DN and FQAN.
     * 
     * @param obj another object
     * @return true if the object was a GridUser with equivalent credentials
     */
    public boolean equals(Object obj) {
        GridUser user = (GridUser) obj;
        if ((user.getCertificateDN() == null) ? certificateDN != null : (user.compareDn(certificateDN)!=0)) {
            if (log.isTraceEnabled()) {
                log.trace(this + " !equals " + obj + " for different DN");
            }
            return false;
        }
        if ((user.voFQAN == null) ? voFQAN != null : (!user.voFQAN.equals(voFQAN))) {
            if (log.isTraceEnabled()) {
                log.trace(this + " !equals " + obj + " for different FQAN");
            }
            return false;
        }
        if (log.isTraceEnabled()) {
            log.trace(this + " equals " + obj);
        }
        return true;
    }
    
    /**
     * Retrieve the certificate DN of the user.
     * 
     * @return The certificate DN (i.e. "/DC=org/DC=doegrids/OU=People/CN=John Smith")
     */
    public String getCertificateDN() {
        return this.certificateDN;
    }
    
    /**
     * Retrieve the email of the user.
     * 
     * @return The email
     */
    public String getEmail() {
        return this.email;
    }

    /**
     * Retrieve the VOMS Fully Qualified Attribute name.
     * 
     * @return The VOMS FQAN selected with voms-proxy-init (i.e. "/atlas/production/Role=Leader")
     */
    public FQAN getVoFQAN() {
        return this.voFQAN;
    }
    
    /**
     * Changed to reflect the change in equals, as in Object contract.
     * 
     * @return A hash created from the DN and FQAN.
     */
    public int hashCode() {
        if (certificateDN != null) {
            return certificateDN.hashCode();
        }
        if (voFQAN != null) {
            return voFQAN.getFqan().hashCode();
        }
        return 0;
    }
    
    /**
     * Changes the certificate DN for the Grid credential.
     * 
     * @param certificateDN A GRID certificate DN (i.e. "/DC=org/DC=doegrids/OU=People/CN=Gabriele Carcassi")
     */
    public void setCertificateDN(String certificateDN) {
        this.certificateDN = certificateDN;//removeSpaces(certificateDN);
    }
    
    /**
     * Changes the email.
     * 
     * @param email
     */
    public void setEmail(String email) {
        this.email = email;
    }
    
    /**
     * Sets the VOMS Fully Qualified Attribute name for the credential.
     * 
     * @param voFQAN The VOMS FQAN selected with voms-proxy-init (i.e. "/atlas/production/Role=Leader")
     */
    public void setVoFQAN(FQAN voFQAN) {
        this.voFQAN = voFQAN;
    }
    
    /**
     * Returns a legible String representation for the credentail.
     * 
     * @return String reprentation of the credential (i.e. "GridID[/DC=org/DC=doegrids/OU=People/CN=Gabriele Carcassi]")
     */
    public String toString() {
        if (voFQAN == null) {
            return "GridID[" + certificateDN + "]";
        }
        return "GridID[" + certificateDN + ", " + voFQAN + "]";
    }
    
    /**
     * Trim and remove two or more consecutive strings
     * 
     * @param str
     * @return new string
     */
    private String removeSpaces(String str) {
    	if (str!=null) {
    		str = str.trim();
	    	String tempStr;
	    	while ( !(tempStr=str.replaceAll("\\s\\s", " ")).equals(str) )
	    		str = tempStr;	   	    	
    	}
   		return str;
    }
    
}
