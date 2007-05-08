/*
 * VOMSUserGroupManager.java
 *
 * Created on May 25, 2004, 10:20 AM
 */

package gov.bnl.gums.userGroup;

import gov.bnl.gums.FQAN;
import gov.bnl.gums.GUMS;
import gov.bnl.gums.GridUser;
import gov.bnl.gums.configuration.Configuration;
import gov.bnl.gums.db.UserGroupDB;
import gov.bnl.gums.persistence.LDAPPersistenceFactory;

import java.net.URL;
import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.edg.security.voms.service.admin.*;

/** A group of users residing on a VOMS vo database. This class is able to 
 * import a list of users from a VOMS server. It will store to a local
 * medium through the UserGroupDB interface. It also manages the caching from
 * the local database.
 * <p>
 * The authentication is done through the proxy, or a certificate/key/password
 * combination. The parameters are to be set externally as system properties.
 * The proxy can be set through "gridProxyFile" property. Other properties
 * are "sslCertfile", "sslKey", "sslKeyPasswd" and "sslCAFiles". More documentation
 * can be found in the documentation of the edg trustmanager  
 *
 * @todo Should refactor with LDAPGroup, and provide a PersistanceCachedGroup
 * since they both share local site buffering functionality
 * @author Gabriele Carcassi, Jay Packard
 */
public class VOMSUserGroup extends UserGroup {
	static private final boolean defaultAcceptProxyWithoutFQAN = true;
	static private final String defaultMatchFQAN = "ignore";
	static private String[] matchFQANTypes = {"exact","vorole","role","group","vogroup","vo","ignore"}; // group is just for backwards compatibility
	
    static public String getTypeStatic() {
		return "voms";
	}
    
    static public List getMatchFQANTypes() {
		ArrayList retList = new ArrayList();
		for(int i=0; i<matchFQANTypes.length; i++)
			retList.add(matchFQANTypes[i]);
		return retList;
	}

    private Log log = LogFactory.getLog(VOMSUserGroup.class);
    private Log resourceAdminLog = LogFactory.getLog(GUMS.resourceAdminLog);
    private String vo = "";
    private String voGroup = "";
    private String role = "";
    private String fqan = null;
    private String matchFQAN = defaultMatchFQAN;
    private String remainderUrl = "";

	private boolean acceptProxyWithoutFQAN = defaultAcceptProxyWithoutFQAN;
    
    public VOMSUserGroup() {
    	super();
    }    
 
	public VOMSUserGroup(Configuration configuration) {
		super(configuration);
	}
    
	public VOMSUserGroup(Configuration configuration, String name) {
		super(configuration, name);
	}
    
    public UserGroup clone(Configuration configuration) {
    	VOMSUserGroup userGroup = new VOMSUserGroup(configuration, getName());
    	userGroup.setDescription(getDescription());
    	userGroup.setAccess(getAccess());
    	userGroup.setVirtualOrganization(getVirtualOrganization());
    	userGroup.setRole(getRole());
    	userGroup.setVoGroup(getVoGroup());
    	userGroup.setMatchFQAN(getMatchFQAN());
    	userGroup.setRemainderUrl(getRemainderUrl());
    	userGroup.setAcceptProxyWithoutFQAN(acceptProxyWithoutFQAN);
    	return userGroup;
    }
    
    /**
     * The scheme according to which the FQAN will be matched.
     * <p>
     * Possible values are:
     * <ul>
     *   <li>exact (default) - role, group, and vo have to match. </li>
     *   <li>vorole - role and vo have to match.</li>
     *   <li>role - rolehas to match.</li>
     *   <li>group, vogroup - group and vo have to match.</li>
     *   <li>vo - vo has to match.</li>
     *   <li>ignore - no matching.</li>
     * </ul>
     * @return matching type as String.
     */
    public String getMatchFQAN() {
   		return matchFQAN;
    }
    
    public java.util.List getMemberList() {
        return getVoDB().retrieveMembers();
    }
    
    public String getRemainderUrl() {
    	return remainderUrl;
    }
    
    public String getType() {
		return "voms";
	}
    
    public String getUrl() {
    	return getVoObject().getBaseUrl() + remainderUrl;
    }
    
    /**
     * Get name of virtual organization
     * @return
     */
    public String getVirtualOrganization() {
    	return vo;
    }
    
    /**
     * Returns the VO group.
     * @return The group in the VOMS (i.e. /atlas/usatlas)
     */
    public String getVoGroup() {
        return this.voGroup;
    }
    
    public VOMSAdmin getVOMSAdmin() {
        try {
            log.info("VOMS Service Locater: url='" + getUrl() + "'");
            System.setProperty("axis.socketSecureManager", "org.edg.security.trustmanager.axis.AXISSocketManager");
            VOMSAdminServiceLocator locator = new VOMSAdminServiceLocator();
            URL vomsUrl = new URL( getUrl() );
            log.info("Trying to connect to VOMS admin at " + vomsUrl);
            return locator.getVOMSAdmin(vomsUrl);
        } catch (Throwable e) {
            log.error("Couldn't get VOMS Admin: ", e);
            throw new RuntimeException("Couldn't get VOMS Admin: " + e.getMessage(), e);
        }
    }    
    
    /**
     * Changes the role.
     * @return The role name in the VOMS server (i.e. myrole), or "" for no role
     */
    public String getRole() {
        return this.role;
    }
    
    /**
     * True if non-VOMS will be accepted. If true, all non-VOMS proxies with a matchin
     * DN will be matched. VOMS proxies won't be affected by the use of this property.
     * @return True if group will accept non-VOMS proxies
     */
    public boolean isAcceptProxyWithoutFQAN() {
        return this.acceptProxyWithoutFQAN;
    }
    
    /**
     * Convenience function for "ignore".equals(getmatchFQAN())
     * @return False if FQAN is used during the match
     */
   public boolean isIgnoreFQAN() {
        return "ignore".equals(matchFQAN);
    }
    
    public boolean isInGroup(GridUser user) {
        // If the user comes in without FQAN and we don't accept proxies without fqan,
        // kick him out right away
        if (user.getVoFQAN()==null && !isAcceptProxyWithoutFQAN())
            return false;
        
        // If the user comes in without FQAN and we accept proxies without it,
        // we simply check whether the DN is in the database
        if (user.getVoFQAN()==null && isAcceptProxyWithoutFQAN()) {
            if (getVoDB().isMemberInGroup(new GridUser(user.getCertificateDN(), fqan)))
                return true;
            return false;
        }
        
        // At this point, the user has FQAN
        // To avoid a query on the db, we check if the fqan matches first

        // If we have vorole match, entire fqan has to be the same
        if ("exact".equals(getMatchFQAN())) {
            if (!user.getVoFQAN().toString().equals(fqan))
                return false;
        }
        
        // If we have a vo-role match, vo and role has to be the same
        if ("vorole".equals(getMatchFQAN())) {
        	FQAN theFQAN = new FQAN(fqan);
            if (!user.getVoFQAN().getVo().equals(theFQAN.getVo()) && !user.getVoFQAN().getRole().equals(theFQAN.getRole()))
                return false;
        }
        
        // If we have a role match, role has to be the same
        if ("role".equals(getMatchFQAN())) {
        	FQAN theFQAN = new FQAN(fqan);
            if (!user.getVoFQAN().getRole().equals(theFQAN.getRole()))
                return false;
        }
        
        // If we match the group, we make sure the VO starts with the group
        if ("group".equals(getMatchFQAN()) || "vogroup".equals(getMatchFQAN())) {
            if (!user.getVoFQAN().toString().startsWith(voGroup))
                return false;
        }

        // If we match the vo, we check the vo is the same
        if ("vo".equals(getMatchFQAN())) {
            FQAN theFQAN = new FQAN(fqan);
            if (!user.getVoFQAN().getVo().equals(theFQAN.getVo()))
                return false;
        }
        
        // FQAN matches, let's look up if the DN is in the db
        // If not, he's kicked out
        return getVoDB().isMemberInGroup(new GridUser(user.getCertificateDN(), fqan));
    }

    /**
     * Changes the way non-VOMS proxies are handled.
     * @param acceptProxyWithoutFQAN True if group will accept non-VOMS proxies
     */
    public void setAcceptProxyWithoutFQAN(boolean acceptProxyWithoutFQAN) {
        this.acceptProxyWithoutFQAN = acceptProxyWithoutFQAN;
    }

    /**
     * Changes the scheme according to which the FQAN will be matched. See
     * getMatchFQAN for more details.
     * @param matchFQAN One of the following:  "exact, "vorole, "role", "group", "vo", "ignore".
     */
    public void setMatchFQAN(String matchFQAN) {
    	boolean found = false;
    	for (int i=0; i<matchFQANTypes.length; i++)
    		if (matchFQANTypes[i].equals(matchFQAN)) found = true;
    	if (!found)
    		throw new RuntimeException("Invalid match FQAN string: "+matchFQAN);
        this.matchFQAN = matchFQAN;
    }
    
    public void setRemainderUrl(String remainderUrl) {
    	this.remainderUrl = remainderUrl;
    }

    /**
     * Set name of virtual organization
     * @param vo
     */
    public void setVirtualOrganization(String vo) {
    	this.vo = vo;
    }
    
    /**
     * Changes the VO group.
     * @param voGroup The group in the VOMS (i.e. /atlas/usatlas)
     */
    public void setVoGroup(String voGroup) {
        this.voGroup = voGroup;
        prepareFQAN();
    }
    
    /**
     * Changes the role.
     * @param voGroup The group in the VOMS (i.e. /atlas/usatlas)
     */
    public void setRole(String role) {
        this.role = role;
        prepareFQAN();
    }

    public String toString() {
        return "VOMSGroup: remainderUrl='" + remainderUrl + "' - voGroup='" + getVoGroup() + "' - voRole='" + getRole() + "'";
    }
    
    public String toString(String bgColor) {
    	return "<td bgcolor=\""+bgColor+"\"><a href=\"userGroups.jsp?action=edit&name=" + getName() + "\">" + getName() + "</a></td><td bgcolor=\""+bgColor+"\">" + getType() + "</td><td bgcolor=\""+bgColor+"\">" + matchFQAN + "</td><td bgcolor=\""+bgColor+"\">" + acceptProxyWithoutFQAN + "</td><td bgcolor=\""+bgColor+"\">" + voGroup + "</td><td bgcolor=\""+bgColor+"\">" + role + "</td>";
    }

    public String toXML() {
    	String retStr = "\t\t<vomsUserGroup\n"+
		"\t\t\taccess='"+accessTypes[accessIndex]+"'\n" +
		"\t\t\tname='"+getName()+"'\n"+
		"\t\t\tdescription='"+getDescription()+"'\n"+
        "\t\t\tvirtualOrganization='"+vo+"'\n";
    	if (!remainderUrl.equals(""))
    		retStr += "\t\t\tremainderUrl='"+remainderUrl+"'\n";
   		retStr += "\t\t\tmatchFQAN='"+matchFQAN+"'\n";
   		retStr += "\t\t\tacceptProxyWithoutFQAN='"+acceptProxyWithoutFQAN+"'\n"; 
    	if (!voGroup.equals(""))
        	retStr += "\t\t\tvoGroup='"+voGroup+"'\n";
    	if (!role.equals(""))
    		retStr += "\t\t\trole='"+role+"'\n";
    	if (retStr.charAt(retStr.length()-1)=='\n')
    		retStr = retStr.substring(0, retStr.length()-1);
    	retStr += "/>\n\n";
    	return retStr;
    }

    public void updateMembers() {
   		getVoDB().loadUpdatedList(retrieveMembers());
    }
    
    private UserGroupDB getVoDB() {
    	return getVoObject().getDB( getName() );
    }
    
    private VirtualOrganization getVoObject() {
    	if (getConfiguration()==null)
    		throw new RuntimeException("Configuration has not yet been set for this class");    	
    	return getConfiguration().getVirtualOrganization(vo);
    }

    private void prepareFQAN() {
        if (!voGroup.equals("")) {
            if (!role.equals("") && !voGroup.equals(""))
            	fqan = voGroup + "/Role=" + role;
            else if (!voGroup.equals(""))
                fqan = voGroup;
            else
            	fqan = null;
        }
    }
    
    private List retrieveMembers() {
        Properties p = System.getProperties();
        try {
            setProperties();
            log.debug("SSL properties read: " + 
            "sslCertfile='" + System.getProperty("sslCertfile") +
            "' sslKey='" + System.getProperty("sslKey") +
            "' sslKeyPasswd set:" + (System.getProperty("sslKeyPasswd")!=null) +
            " sslCAFiles='" + System.getProperty("sslCAFiles") + "'" ); 
            System.setProperty("axis.socketSecureFactory", "org.edg.security.trustmanager.axis.AXISSocketFactory");
            VOMSAdmin voms = getVOMSAdmin();
        	org.edg.security.voms.service.User[] users = null;
            if (role.equals("")) {
                users = voms.listMembers( !getVoGroup().equals("")?getVoGroup():null );
            } else if(!getVoGroup().equals("")) {
                users = voms.listUsersWithRole( getVoGroup(), "Role=" + getRole());
            }        	
            if (users.length > 0) {
                log.trace("Retrieved " + users.length + " users. First is: '" + users[0].getDN());
            } else {
                log.trace("Retrieved no users.");
            }
            System.setProperties(p);
            List entries = new ArrayList();
            for (int n=0; n< users.length; n++) {
            	GridUser gridUser = new GridUser(users[n].getDN(), fqan);
                entries.add(gridUser);
            }
            return entries;
        } catch (Throwable e) {
            log.error("Couldn't retrieve VOMS users: ", e);
            throw new RuntimeException("Couldn't retrieve users from VOMS server: " + e.getMessage(), e);
        }
    }
    
    private void setProperties() {
    	VirtualOrganization voObject = getVoObject();
        log.debug( "SSL properties set: sslCertfile='" + voObject.getSslCertfile() + "' sslKey='" + voObject.getSslKey() + "' sslKeyPasswd set:" + (!voObject.getSslKeyPasswd().equals("")) + " sslCAFiles='" + voObject.getSslCAFiles() + "'" ); 
        if (!voObject.getSslCertfile().equals("")) {
            System.setProperty("sslCertfile", voObject.getSslCertfile());
        }
        if (!voObject.getSslKey().equals("")) {
            System.setProperty("sslKey", voObject.getSslKey());
        }
        if (!voObject.getSslKeyPasswd().equals("")) {
            System.setProperty("sslKeyPasswd", voObject.getSslKeyPasswd());
        }
        if (!voObject.getSslCAFiles().equals("")) {
            System.setProperty("sslCAFiles", voObject.getSslCAFiles());
        }
    }
}
