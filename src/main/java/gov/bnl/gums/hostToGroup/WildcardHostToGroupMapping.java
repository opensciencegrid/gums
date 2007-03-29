/*
 * WildcardHostGroup.java
 *
 * Created on May 27, 2004, 2:51 PM
 */

package gov.bnl.gums.hostToGroup;

import gov.bnl.gums.GUMS;
import gov.bnl.gums.configuration.Configuration;

import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/** Matches a set of hosts to a list of group mapping by comparing the hostname
 * with a wildcard. Examples of valid wildcards are 'star*.rhic.bnl.gov',
 * '*.usatlas.bnl.gov', 'grid.*.edu', '*test*.bnl.gov'.
 * <p>
 * The '*' character can represent any string part of a single token of the hostname.
 * That is, it can't repesent a string that contains '.'.
 *
 * @author  Gabriele Carcassi
 */
public class WildcardHostToGroupMapping extends HostToGroupMapping {
    static Log adminLog = LogFactory.getLog(GUMS.resourceAdminLog);
    
    private List regexs;
  
    /** Creates a new wildcard mapping, which needs to be properly configured. */
    public WildcardHostToGroupMapping() {
        adminLog.warn("The use of gov.bnl.gums.WildcardHostGroup is deprecated. Please use gov.bnl.gums.CertificateHostGroup: it provides equivalent functionalities.");
    }
 
    /** Creates a new wildcard mapping, which needs to be properly configured. */
    public WildcardHostToGroupMapping(Configuration configuration) {
    	super(configuration);
        adminLog.warn("The use of gov.bnl.gums.WildcardHostGroup is deprecated. Please use gov.bnl.gums.CertificateHostGroup: it provides equivalent functionalities.");
    }
    
    /** Creates a new wildcard mapping, which needs to be properly configured. */
    public WildcardHostToGroupMapping(Configuration configuration, String wildcard) {
    	super(configuration, wildcard);
        adminLog.warn("The use of gov.bnl.gums.WildcardHostGroup is deprecated. Please use gov.bnl.gums.CertificateHostGroup: it provides equivalent functionalities.");
    }
    
    public HostToGroupMapping clone(Configuration configuration) {
    	WildcardHostToGroupMapping hostToGroupMapping = new WildcardHostToGroupMapping(configuration, getName());
    	hostToGroupMapping.setDescription(getDescription());
    	hostToGroupMapping.setWildcard(getWildcard());
    	Iterator it = getGroupToAccountMappings().iterator();
    	while (it.hasNext()) {
    		String groupToAccountMapping = (String)it.next();
    		hostToGroupMapping.addGroupToAccountMapping(groupToAccountMapping);
    	}
    	return hostToGroupMapping;
    }
    
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
    
    /** Changes the wildcard that will be used to match the hostname.
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
    
    public String toXML() {
		return null;
    }
    
}
