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

import gov.bnl.gums.configuration.Configuration;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

/** Represent a set of services identified by a wildcard on their CN or DN.
 *
 * @author Gabriele Carcassi, Jay Packard
 */
public class CertificateHostToGroupMapping extends HostToGroupMapping {
    private String cn = null;
    private String dn = null;
    private List regexs;
    
    /**
     * Create a new CertificateHostToGroupMapping object.
     */
    public CertificateHostToGroupMapping() {
    	super();
    }
    
    /**
     * Create a new CertificateHostToGroupMapping object.
     * 
     * @param configuration
     */
    public CertificateHostToGroupMapping(Configuration configuration) {
    	super(configuration);
    }

    public HostToGroupMapping clone(Configuration configuration) {
    	CertificateHostToGroupMapping hostToGroupMapping = new CertificateHostToGroupMapping(configuration);
    	hostToGroupMapping.setDescription(new String(getDescription()));
    	if (getDn()!=null)
    		hostToGroupMapping.setDn(new String(getDn()));
    	if (getCn()!=null)
    		hostToGroupMapping.setCn(new String(getCn()));
    	Iterator it = getGroupToAccountMappings().iterator();
    	while (it.hasNext()) {
    		String groupToAccountMapping = (String)it.next();
    		hostToGroupMapping.addGroupToAccountMapping(new String(groupToAccountMapping));
    	}
    	return hostToGroupMapping;
    }
    
    /** 
     * Retrieves the wildcard that will be used to match the CN.
     * 
     * @return The wildcard (i.e. '*.mycompany.com').
     */
    public String getCn() {
        return this.cn;
    }
    
    /** 
     * Retrieves the wildcard that will be used to match the DN.
     * 
     * @return The wildcard (i.e. '/DC=org/DC=doegrids/OU=Services/CN=*.mycompany.com').
     */
    public String getDn() {
        return this.dn;
    }
    
    public boolean isInGroup(String hostname) {
        Iterator iter = regexs.iterator();
        while (iter.hasNext()) {
            if (hostname.matches((String) iter.next()))
                return true;
        }
        return false;
    }
    
    /** Changes the wildcard that will be used to match the CN(s).
     * @param cn The new wildcard (i.e. '*.mycompany.com, *othercompany.com').
     */
    public void setCn(String cn) {
        super.setName(cn);
        this.cn = cn;
        updateRegEx();
    }
    
    /** Changes the wildcard that will be used to match the DN(s).
     * @param wildcard The new wildcard (i.e. '/DC=org/DC=doegrids/OU=Services/CN=*.mycompany.com, /DC=org/DC=doegrids/OU=Services/CN=*.othercompany.com').
     */
    public void setDn(String dn) {
        super.setName(dn);
        this.dn = dn;
        updateRegEx();
    }
    
    public void setName(String name) {
    	throw new RuntimeException("Call setCn or SetDn rather than setName");
    }
    
    public String toXML() {
    	String retStr = "\t\t<hostToGroupMapping\n"+
		"\t\t\tgroupToAccountMappings='";
		Iterator it = getGroupToAccountMappings().iterator();
		while(it.hasNext()) {
			String groupToAccountMapping = (String)it.next();
			retStr += groupToAccountMapping + (it.hasNext()?", ":"");
		}
		retStr += "'\n"+
    	"\t\t\tdescription='"+getDescription()+"'\n";
    	if (dn != null)
    		retStr += "\t\t\tdn='" + dn + "'";
    	if (cn != null)
    		retStr += "\t\t\tcn='" + (cn!=null?cn:"") + "'";
    	retStr += "/>\n\n";
    	return retStr;
    }      
    
    private void updateRegEx() {
        regexs = new ArrayList();
        if (cn != null) {
            StringTokenizer tokens = new StringTokenizer(cn, ",");
            while (tokens.hasMoreTokens()) {
                String regex = tokens.nextToken();
		regex = regex.trim();
                regex = regex.replaceAll("\\.", "\\.");
                regex = regex.replaceAll("\\*", "[^\\./=]*");
                regexs.add("(/[^=]*=[^=]*)*/CN=" + regex + "(/[^=]*=[^=]*)*");
            }
        }
        if (dn != null) {
            StringTokenizer tokens = new StringTokenizer(dn, ",");
            while (tokens.hasMoreTokens()) {
                String regex = tokens.nextToken();
		regex = regex.trim();
                regex = regex.replaceAll("\\.", "\\.");
                regex = regex.replaceAll("\\*", "[^\\./=]*");
                regexs.add(regex);
            }
        }
    }
}
