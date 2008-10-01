/*
 * FQAN.java
 *
 * Created on September 10, 2004, 12:14 PM
 */

package gov.bnl.gums;

import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

/** 
 * Represent a VOMS extended proxy credential as defined in
 * http://edg-wp2.web.cern.ch/edg-wp2/security/voms/edg-voms-credential.pdf.
 * <p>
 * A Fully Qualified Attribute Name consists of a VO, a group, a role and
 * a capability. Only the VO is mandatory. The syntax is
 * /VO[/group[/subgroup(s)]][/Role=role][/Capability=cap].
 * <p>
 * The class will always check that every modification matches the regex
 * /[\w-\.]+(/[\w-\.]+)*(/Role=[\w-\.]+)?(/Capability=[\w-\.]+)?
 * which fully defines all the characters allowed. This differs slightly from
 * the one contained in the specification, as it contained some small errors.
 * It matches the description of the syntax of the document.
 *
 * @author  Gabriele Carcassi, Jay Packard
 */
public class FQAN {
	static private Pattern fqanPattern = Pattern.compile("/[\\w-\\.]+(/[\\w-\\.]+)*(/Role=[\\w-\\.]+)?(/Capability=[\\w-\\.]+)?");
    private String fqan;
    private String vo;
    private String group;
    private String role;
    private String capability;
    private boolean checkFormedness = true;
    
    /**
     * Creates a FQAN based on String representation. If the parsing fails, it will
     * throw an exception.
     * @param fqan A VOMS FQAN (i.e. "/atlas/production/Role=Leader")
     */
    public FQAN(String fqan) {
       this(fqan, true);
    }
    
    /**
     * Creates a FQAN based on String representation. If the parsing fails, it will
     * throw an exception.
     * @param fqan A VOMS FQAN (i.e. "/atlas/production/Role=Leader")
     */
    public FQAN(String fqan, boolean checkFormedness) {
    	this.checkFormedness = checkFormedness;
        setFqan(fqan);
        if (parseFqan(fqan))
        	generateFqan();
    }
    
    /** Creates a FQAN based on the different pieces of information.
     * 
     * @param vo The VO name, which cannot be null (i.e. "atlas")
     * @param group A group path, including subgroups (i.e. "/production")
     * @param role The role (i.e. "Leader")
     * @param capability A capability (i.e. "capability"). Note capabilities are
     * being deprecated in VOMS.
     */
    public FQAN(String vo, String group, String role, String capability) {
        if (vo == null)
            throw new IllegalArgumentException("The vo for a FQAN can't be null");
        setVo(vo);
        setGroup(group);
        setRole(role);
        setCapability(capability);
        generateFqan();
    }
    
    /** FQANs are equals to each other if their vo, group, role and capability
     * are the same.
     * 
     * @param obj Another object.
     * @return True if the object was a FQAN with equal information. False otherwise.
     */
    public boolean equals(Object obj) {
        if (obj == null) return false;
        FQAN fqan2 = (FQAN) obj;
        return (fqan2.fqan == null) ? fqan == null : fqan2.fqan.equalsIgnoreCase(fqan);
    }

    /**
     *  The capability used within the FQAN.
     *  
     * @return The capability or null if none was specified.
     */
    public String getCapability() {
        return this.capability;
    }

    /** A full String representation of the FQAN.
     * 
     * @return The Fully Qualified Attribute name String representation (i.e. "/atlas/production/Role=Leader")
     */
    public String getFqan() {
        return this.fqan;
    }
    
    /** The path which includes the groups and subgroups within the FQAN.
     * 
     * @return The group path (i.e. "/production")
     */
    public String getGroup() {
        return this.group;
    }
    
    /** The role used within the FQAN.
     * 
     * @return The role or null if no role was specified
     */
    public String getRole() {
        return this.role;
    }
    
    /** Returns the VO corresponding to the FQAN.
     * 
     * @return The VO name (i.e. "atlas")
     */
    public String getVo() {
        return this.vo;
    }
    
    /** A hashcode generated from the string representation.
     * 
     * @return A hashcode.
     */
    public int hashCode() {
        if (fqan == null) return 0;
        return fqan.hashCode();
    }
    
    /** Returns the full string representation of the FQAN.
     * 
     * @return The full FQAN (i.e. "/atlas/production/Role=Leader")
     */
    public String toString() {
        return fqan;
    }
    
    private void generateFqan() {
        if (vo == null) {
            fqan = null;
            return;
        }
        StringBuffer bf = new StringBuffer();
        bf.append('/');
        bf.append(vo);
        if (group != null)
            bf.append(group);
        if (role != null) {
            bf.append("/Role=");
            bf.append(role);
        }
        if (capability != null) {
            bf.append("/Capability=");
            bf.append(capability);
        }
        fqan = bf.toString();
    }
    
    private boolean parseFqan(String fqan) {
        // Matches to the specification.
    	Matcher m = fqanPattern.matcher(fqan);
        if (!m.matches()) {
        	if (checkFormedness)
        		throw new IllegalArgumentException("FQAN '" + fqan + "' is malformed (syntax: /VO[/group[/subgroup(s)]][/Role=role][/Capability=cap])");
        	else
        		return false;
        }
        
        StringTokenizer stk = new StringTokenizer(fqan, "/");
        vo = stk.nextToken();
        if (!stk.hasMoreTokens()) {
            group = null;
            role = null;
            capability = null;
            return true;
        }
        String tempGroup = "";
        String token = stk.nextToken();
        while ((!token.startsWith("Role=") && !token.startsWith("Capability="))) {
            tempGroup = tempGroup + "/" + token;
            group = tempGroup;
            if (!stk.hasMoreTokens()) {
                role = null;
                capability = null;
                return true;
            }
            token =  stk.nextToken();
        }
        if (token.startsWith("Role=")) {
            setRole(token.substring(5));
            if (!stk.hasMoreTokens()) {
                capability = null;
                return true;
            }
            token = stk.nextToken();
        }
        if (token.startsWith("Capability=")) {
            setCapability(token.substring(11));
        }
        
        return true;
    }
    
    private void setCapability(String capability) {
        if ((capability != null) && (!capability.matches("[\\w-\\.]+"))) {
            throw new IllegalArgumentException("The capability '" + capability + "' is malformed");
        }
        this.capability = capability;
        if ("NULL".equalsIgnoreCase(capability)) {
            this.capability = null;
        }
    }
    
    private void setFqan(String fqan) {
        this.fqan = fqan;
    }
    
    private void setGroup(String group) {
        if ((group != null) && (!group.matches("(/[\\w-\\.]+)+"))) {
            throw new IllegalArgumentException("The group '" + group + "' is malformed");
        }
        this.group = group;
    }
    
    private void setRole(String role) {
        if ((role != null) && (!role.matches("[\\w-\\.]+"))) {
            throw new IllegalArgumentException("The role '" + role + "' is malformed");
        }
        this.role = role;
        if ("NULL".equalsIgnoreCase(role)) {
            this.role = null;
        }
    }
    
    private void setVo(String vo) {
        if ((vo != null) && (!vo.matches("[\\w-\\.]+"))) {
            throw new IllegalArgumentException("The vo '" + vo + "' is malformed");
        }
        this.vo = vo;
    }
    
}
