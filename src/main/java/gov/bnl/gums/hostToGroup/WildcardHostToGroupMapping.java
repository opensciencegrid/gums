/*
 * WildcardHostGroup.java
 *
 * Created on May 27, 2004, 2:51 PM
 */

package gov.bnl.gums.hostToGroup;

import gov.bnl.gums.GUMS;

import java.util.*;
import java.util.regex.*;
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
    
    private String wildcard;
    private List regexs;
    
    /** Creates a new wildcard mapping, which needs to be properly configured. */
    public WildcardHostToGroupMapping() {
        adminLog.warn("The use of gov.bnl.gums.WildcardHostGroup is deprecated. Please use gov.bnl.gums.CertificateHostGroup: it provides equivalent functionalities.");
    }
    
    public String getName() {
    	return wildcard;
    }
    
    public boolean isInGroup(String hostname) {
        Iterator iter = regexs.iterator();
        while (iter.hasNext()) {
            if (hostname.matches((String) iter.next()))
                return true;
        }
        return false;
    }
    
    /** Retrieves the wildcard that will be used to match the hostname.
     * @return The wildcard (i.e. '*.mycompany.com').
     */
    public String getWildcard() {
        return this.wildcard;
    }
    
    /** Changes the wildcard that will be used to match the hostname.
     * @param wildcard The new wildcard (i.e. '*.mycompany.com').
     */
    public void setWildcard(String wildcard) {
        this.wildcard = wildcard;
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
    	return super.toXML() +
			"\t\t\twildcard='"+wildcard+"'/>\n\n";
    }        
    
}
