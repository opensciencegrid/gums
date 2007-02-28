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
 * @author  Gabriele Carcassi
 */
public class VOMSUserGroup extends UserGroup {
	private static final boolean defaultAcceptProxyWithoutFQAN = true;
	private static final String defaultMatchFQAN = "ignore";
    private Log log = LogFactory.getLog(VOMSUserGroup.class);
    private Log resourceAdminLog = LogFactory.getLog(GUMS.resourceAdminLog);
    private String vo = "";
    private String voGroup = "";
    private String voRole = "";
    private String fqan = "";
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
    	userGroup.setAccess(getAccess());
    	userGroup.setVirtualOrganization(getVirtualOrganization());
    	userGroup.setVoRole(getVoRole());
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
     *   <li>exact (default) - the FQAN in the proxy has to be the same as
     *   what the VOMSGroup is set to. </li>
     *   <li>group - the FQAN in the proxy has to be the same group, or any
     *   subgroup; role is ignored.</li>
     *   <li>vo - the FQAN in the proxy has to be of the same vo.</li>
     *   <li>ignore - the FQAN in the proxy is completely ignored.</li>
     * </ul>
     * @return One of the following:  "ignore", "vo", "group" or "exact".
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
    
    static public String getTypeStatic() {
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
     * Changes the VO role.
     * @return The role name in the VOMS server (i.e. myrole), or "" for no role
     */
    public String getVoRole() {
        return this.voRole;
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
        // If the user comes in without FQAN, either we accept proxies without
        // it or we kick him out right away
        if ((user.getVoFQAN() == null) && !isAcceptProxyWithoutFQAN()) {
            
            // FIXME To achieve complete backward compatibility with GUMS 1.0,
            // if the voGroup is null and the user comes in with no Proxy
            // we still allow him in.
            // We should remove this in versions after 1.1
            if ((voGroup.equals("")) && (voRole.equals(""))) {
                return getVoDB().isMemberInGroup(user);
            }
            return false;
        }
        
        // If the user comes in wihout FQAN, but we accept proxies without it,
        // we simply check whether the DN is in the database
        if ((user.getVoFQAN().equals("")) && isAcceptProxyWithoutFQAN()) {
            if (getVoDB().isMemberInGroup(new GridUser(user.getCertificateDN(), fqan)))
                return true;
            return false;
        }
        
        // At this point, the user has FQAN
        // To avoid a query on the db, we check if the fqan matches first

        // If we have an exact match, fqan has to be the same
        if ("exact".equals(getMatchFQAN())) {
            if (!user.getVoFQAN().toString().equals(fqan))
                return false;
        }

        // If we match the vo, we check the vo is the same
        if ("vo".equals(getMatchFQAN())) {
            FQAN theFQAN = new FQAN(fqan);
            if (!user.getVoFQAN().getVo().equals(theFQAN.getVo()))
                return false;
        }
        
        // If we match the group, we make sure the VO starts with the group
        if ("group".equals(getMatchFQAN())) {
            if (!user.getVoFQAN().toString().startsWith(voGroup))
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
     * @param matchFQAN One of the following:  "ignore", "vo", "group" or "exact".
     */
    public void setMatchFQAN(String matchFQAN) {
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
     * Changes the role to be retrieved within the VO group.
     * @param voRole The role name in the VOMS server (i.e. myrole), or "" for no role
     */
    public void setVirtualOrganizationRole(String voRole) {
        this.voRole = voRole;
        prepareFQAN();
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
    public void setVoRole(String voRole) {
        this.voRole = voRole;
    }

    public String toString() {
        return "VOMSGroup: remainderUrl='" + remainderUrl + "' - voGroup='" + getVoGroup() + "' - voRole='" + getVoRole() + "'";
    }
    
    public String toString(String bgColor) {
    	return "<td bgcolor=\""+bgColor+"\">" + getName() + "</td><td bgcolor=\""+bgColor+"\">" + matchFQAN + "</td><td bgcolor=\""+bgColor+"\">" + acceptProxyWithoutFQAN + "</td><td bgcolor=\""+bgColor+"\">" + voGroup + "</td><td bgcolor=\""+bgColor+"\">" + voRole + "</td><td bgcolor=\""+bgColor+"\">" + getVoObject().getBaseUrl() + remainderUrl + "</td>";
    }

    public String toXML() {
    	String retStr = "\t\t<vomsUserGroup\n"+
		"\t\t\taccess='"+accessTypes[accessIndex]+"'\n" +
		"\t\t\tname='"+getName()+"'\n"+
        "\t\t\tvirtualOrganization='"+vo+"'\n";
    	if (!remainderUrl.equals(""))
    		retStr += "\t\t\tremainderUrl='"+remainderUrl+"'\n";
   		retStr += "\t\t\tmatchFQAN='"+matchFQAN+"'\n";
   		retStr += "\t\t\tacceptProxyWithoutFQAN='"+acceptProxyWithoutFQAN+"'\n"; 
    	if (!voGroup.equals(""))
        	retStr += "\t\t\tvoGroup='"+voGroup+"'\n";
    	if (!voRole.equals(""))
    		retStr += "\t\t\tvoRole='"+voRole+"'\n";
    	if (retStr.charAt(retStr.length()-1)=='\n')
    		retStr = retStr.substring(0, retStr.length()-1);
    	retStr += "/>\n\n";
    	return retStr;
    }

    public void updateMembers() {
   		getVoDB().loadUpdatedList(retrieveMembers());
    }
    
    private UserGroupDB getVoDB() {
    	return getVoObject().getDB();
    }
    
    private VirtualOrganization getVoObject() {
    	if (getConfiguration()==null)
    		throw new RuntimeException("Configuration has not yet been set for this class");    	
    	return getConfiguration().getVirtualOrganization(vo);
    }

    private void prepareFQAN() {
        if (voGroup.equals("")) {
            fqan = null;
        } else {
            if (voRole.equals("")) {
                fqan = voGroup;
            } else {
                fqan = voGroup + "/Role=" + voRole;
            }
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
            VOMSAdmin voms = getVOMSAdmin();
        	org.edg.security.voms.service.User[] users = null;
            if (!voRole.equals("") && !voGroup.equals("")) {
                users = voms.listUsersWithRole("/atlas/usatlas", "Role=software");
            } else if (!voGroup.equals("")){
                users = voms.listMembers(getVoGroup());
            }
            if (users.length > 0) {
                log.trace("Retrieved " + users.length + " users. First is: '" + users[0].getDN());
            } else {
                log.trace("Retrieved no users.");
            }
            System.setProperties(p);
            List entries = new ArrayList();
            for (int n=0; n< users.length; n++) {
                entries.add(new GridUser(users[n].getDN(), fqan));
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
