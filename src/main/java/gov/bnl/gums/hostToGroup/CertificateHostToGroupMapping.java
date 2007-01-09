/*
 * CertificateHostGroup.java
 *
 * Created on May 10, 2005, 3:56 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package gov.bnl.gums.hostToGroup;



import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

/** Represent a set of services identified by a wildcard on their CN or DN.
 *
 * @author Gabriele Carcassi
 * @since 1.1.0
 */
public class CertificateHostToGroupMapping extends HostToGroupMapping {
    
    private String cn;
    private String dn;
    private List regexs;
    
    public CertificateHostToGroupMapping() {
    	
    }

    public boolean isInGroup(String hostname) {
        Iterator iter = regexs.iterator();
        while (iter.hasNext()) {
            if (hostname.matches((String) iter.next()))
                return true;
        }
        return false;
    }
    
    public String getName() {
    	if(cn!=null)
    		return cn;
    	else if(dn!=null)
    		return dn;
    	else
    		return "";
    }
    
    /** Retrieves the wildcard that will be used to match the CN.
     * @return The wildcard (i.e. '*.mycompany.com').
     */
    public String getCn() {
        return this.cn;
    }
    
    /** Changes the wildcard that will be used to match the CN.
     * @param cn The new wildcard (i.e. '*.mycompany.com').
     */
    public void setCn(String cn) {
        this.cn = cn;
        updateRegEx();
    }
    
    /** Retrieves the wildcard that will be used to match the DN.
     * @return The wildcard (i.e. '/DC=org/DC=doegrids/OU=Services/CN=*.mycompany.com').
     */
    public String getDn() {
        return this.dn;
    }
    
    /** Changes the wildcard that will be used to match the DN.
     * @param wildcard The new wildcard (i.e. '/DC=org/DC=doegrids/OU=Services/CN=*.mycompany.com').
     */
    public void setDn(String dn) {
        this.dn = dn;
        updateRegEx();
    }
    
    private void updateRegEx() {
        regexs = new ArrayList();
        if (cn != null) {
            StringTokenizer tokens = new StringTokenizer(cn, ",");
            while (tokens.hasMoreTokens()) {
                String regex = tokens.nextToken();
                regex = regex.replaceAll("\\.", "\\.");
                regex = regex.replaceAll("\\*", "[^\\./=]*");
                regexs.add("(/[^=]*=[^=]*)*/CN=" + regex + "(/[^=]*=[^=]*)*");
            }
        }
        if (dn != null) {
            StringTokenizer tokens = new StringTokenizer(dn, ",");
            while (tokens.hasMoreTokens()) {
                String regex = tokens.nextToken();
                regex = regex.replaceAll("\\.", "\\.");
                regex = regex.replaceAll("\\*", "[^\\./=]*");
                regexs.add(regex);
            }
        }
    }
    
    public String toXML() {
    	String retStr = super.toXML();
    	if (cn != null)
    		retStr += "\t\t\tcn='"+cn+"'";
    	if(cn != null && dn != null)
    		retStr += "\n";
    	if (dn != null)
    		retStr += "\t\t\tdn='"+dn+"'";
    	retStr += "/>\n\n";
    	return retStr;
    }       
}
