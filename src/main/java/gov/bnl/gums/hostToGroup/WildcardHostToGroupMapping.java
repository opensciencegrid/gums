/*
 * WildcardHostGroup.java
 *
 * Created on May 27, 2004, 2:51 PM
 */

package gov.bnl.gums.hostToGroup;

import gov.bnl.gums.GUMS;
import gov.bnl.gums.configuration.Configuration;

import java.util.*;

import org.apache.log4j.Logger;

/** Matches a set of hosts to a list of group mapping by comparing the hostname
 * with a wildcard. Examples of valid wildcards are 'star*.rhic.bnl.gov',
 * '*.usatlas.bnl.gov', 'grid.*.edu', '*test*.bnl.gov'.
 * <p>
 * The '*' character can represent any string part of a single token of the hostname.
 * That is, it can't repesent a string that contains '.'.
 *
 * @author Gabriele Carcassi, Jay Packard
 */
public class WildcardHostToGroupMapping extends HostToGroupMapping {
    static Logger gumsAdminLog = Logger.getLogger(GUMS.gumsAdminLogName);
    
    private List regexs;
  
    /**
     * Creates a new wildcard mapping - empty constructor needed by XML Digester.
     */
    public WildcardHostToGroupMapping() {
    	gumsAdminLog.debug("The use of gov.bnl.gums.WildcardHostGroup is deprecated. Please use gov.bnl.gums.CertificateHostGroup: it provides equivalent functionalities.");
    }
 
    /** 
     * 
     * Creates a new wildcard mapping with a configuration
     * 
     * @param configuration
     */
    public WildcardHostToGroupMapping(Configuration configuration) {
    	super(configuration);
    	gumsAdminLog.debug("The use of gov.bnl.gums.WildcardHostGroup is deprecated. Please use gov.bnl.gums.CertificateHostGroup: it provides equivalent functionalities.");
    }
    
    /**
     * Creates a new wildcard mapping with a configuration and wildcard. 
     * 
     * @param configuration
     * @param wildcard
     */
    public WildcardHostToGroupMapping(Configuration configuration, String wildcard) {
    	super(configuration, wildcard);
    	gumsAdminLog.debug("The use of gov.bnl.gums.WildcardHostGroup is deprecated. Please use gov.bnl.gums.CertificateHostGroup: it provides equivalent functionalities.");
    }
    
    public HostToGroupMapping clone(Configuration configuration) {
    	WildcardHostToGroupMapping hostToGroupMapping = new WildcardHostToGroupMapping(configuration, new String(getName()));
    	hostToGroupMapping.setDescription(new String(getDescription()));
    	hostToGroupMapping.setWildcard(new String(getWildcard()));
    	Iterator it = getGroupToAccountMappings().iterator();
    	while (it.hasNext()) {
    		String groupToAccountMapping = (String)it.next();
    		hostToGroupMapping.addGroupToAccountMapping(new String(groupToAccountMapping));
    	}
    	return hostToGroupMapping;
    }
    
    /**
     * Getter for wildcard property, which is really just the name.
     * 
     * @return Wildcard as string
     */
    public String getWildcard() {
        return getName();
    }
    
    public boolean isInGroup(String hostname) {
        Iterator iter = regexs.iterator();
        while (iter.hasNext()) {
            if (hostname.matches((String) iter.next()))
                return true;
        }
        return false;
    }
    
    public void setName(String name) {
    	throw new RuntimeException("Call setWildcard rather than setName");
    }
    
    /** 
     * Changes the wildcard that will be used to match the hostname.
     * 
     * @param wildcard The new wildcard (i.e. '*.mycompany.com').
     */
    public void setWildcard(String wildcard) {
    	super.setName(wildcard);
        StringTokenizer tokens = new StringTokenizer(wildcard, ",");
        regexs = new ArrayList();
        while (tokens.hasMoreTokens()) {
            String regex = tokens.nextToken();
            regex = regex.replaceAll("\\.", "\\.");
            regex = regex.replaceAll("\\*", "[^\\.]*");
            regexs.add(regex);
            regexs.add("(/[^=]*=[^=]*)*/CN=" + regex + "(/[^=]*=[^=]*)*");
        }
    }        
    
    /**
     * Get XML representation of this object for writing to gums.config
     * 
     * @return xml as string
     */
    public String toXML() {
		return null;
    }
    
}
