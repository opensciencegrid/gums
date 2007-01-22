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
    private String fqan = null; // null since it is optional
    private String matchFQAN = defaultMatchFQAN;
    private String remainderUrl = "";
    private boolean acceptProxyWithoutFQAN = defaultAcceptProxyWithoutFQAN;

    public VOMSUserGroup() {
    	super();
    }    
    
	public VOMSUserGroup(Configuration configuration, String name) {
		super(configuration, name);
	}
    
    public java.util.List getMemberList() {
        return getVoDB().retrieveMembers();
    }
    
    private VirtualOrganization getVoObject() {
    	return getConfiguration().getVirtualOrganization(vo);
    }
    
    private UserGroupDB getVoDB() {
    	return getVoObject().getDB();
    }
    
    public boolean isInGroup(GridUser user) {
        // If the user comes in without FQAN, either we accept proxies without
        // it or we kick him out right away
        if ((user.getVoFQAN() == null) && !isAcceptProxyWithoutFQAN()) {
            
            // FIXME To achieve complete backward compatibility with GUMS 1.0,
            // if the voGroup is null and the user comes in with no Proxy
            // we still allow him in.
            // We should remove this in versions after 1.1
            if ((voGroup == null) && (voRole == null)) {
                return getVoDB().isMemberInGroup(user);
            }
            return false;
        }
        
        // If the user comes in wihout FQAN, but we accept proxies without it,
        // we simply check whether the DN is in the database
        if ((user.getVoFQAN() == null) && isAcceptProxyWithoutFQAN()) {
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
    
    public void updateMembers() {
    	if(vo!=null)	
    		getVoDB().loadUpdatedList(retrieveMembers());
    }
    
    private List retrieveMembers() {
        Properties p = System.getProperties();
        try {
            setProperties();
            log.debug("SSL properties: sslCAFiles='" + System.getProperty("sslCAFiles") + 
            "' sslCertfile='" + System.getProperty("sslCertfile") +
            "' sslKey='" + System.getProperty("sslKey") +
            "' sslKeyPasswd set:" + (System.getProperty("sslKeyPasswd")!= null) +"'"); 
            VOMSAdmin voms = getVOMSAdmin();
        	org.edg.security.voms.service.User[] users = null;
            if (voRole == null) {
                users = voms.listMembers(getVoGroup());
            } else {
                users = voms.listUsersWithRole(getVoGroup(), "Role=" + getVoRole());
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
    
    public String toString() {
        return "VOMSGroup: " + getUrl() + " - voGroup='" + getVoGroup() + "' - voRole='" + getVoRole() + "' - sslCAFiles='" + getVoObject().getSslCAFiles() +"' sslCertfile='" + getVoObject().getSslCertfile() +"' sslKey='" + getVoObject().getSslKey() + "' sslKeyPasswd=" + ((getVoObject().getSslKeyPasswd()!= null) ? "[set]" : "[not set]");
    }

    public String getUrl() {
    	return (getVoObject()!=null ? getVoObject().getBaseUrl() + remainderUrl : "");
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
    
    public String getRemainderUrl() {
    	return remainderUrl;
    }
    
    public void setRemainderUrl(String remainderUrl) {
    	this.remainderUrl = remainderUrl;
    }
    
    /**
     * Returns the VO group.
     * @return The group in the VOMS (i.e. /atlas/usatlas)
     */
    public String getVoGroup() {
        return this.voGroup;
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
     * Changes the VO role.
     * @return The role name in the VOMS server (i.e. myrole), or null for no role
     */
    public String getVoRole() {
        return this.voRole;
    }

    /**
     * Changes the role.
     * @param voGroup The group in the VOMS (i.e. /atlas/usatlas)
     */
    public void setVoRole(String voRole) {
        this.voRole = voRole;
    }
    
    /**
     * Changes the role to be retrieved within the VO group.
     * @param voRole The role name in the VOMS server (i.e. myrole), or null for no role
     */
    public void setVirtualOrganizationRole(String voRole) {
        this.voRole = voRole;
        prepareFQAN();
    }

    /**
     * @param vo
     */
    public void setVirtualOrganization(String vo) {
    	this.vo = vo;
    }

    /**
     * @return
     */
    public String getVirtualOrganization() {
    	return vo;
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
    
    /**
     * Tells whether the FQAN should be used to match the proxy or not. If set to false,
     * the FQAN has to be present (i.e. won't accept non VOMS proxy) and has to match
     * the one defined by the group. If set to true, the FQAN is not looked at, making
     * all proxies (non VOMS and VOMS proxy with different FQAN) match as long as the
     * DN matches. Setting true makes the behaviour similar to the one of the
     * grid-mapfile.
     * @return False if FQAN is used during the match
     */
   public boolean isIgnoreFQAN() {
        return "ignore".equals(matchFQAN);
    }
    
    /**
     * Changes the way FQAN is used to match.
     * @param ignoreFQAN False if FQAN is used during the matchNew value of property ignoreFQAN.
     * @deprecated As of GUMS 1.1, use matchFQAN and acceptProxyWithoutFQAN
     */
/*    public void setIgnoreFQAN(boolean ignoreFQAN) {
        resourceAdminLog.warn("The attribute \"ignoreFQAN\" for VOMSGroup is deprecated: use matchFQAN instead. DO NOT MIX ignoreFQAN and matchFQAN.");
        if (ignoreFQAN) {
            matchFQAN = "ignore";
            acceptProxyWithoutFQAN = true;
        } else {
            matchFQAN = "exact";
            acceptProxyWithoutFQAN = false;
        }
    }*/

    /**
     * Changes the scheme according to which the FQAN will be matched. See
     * getMatchFQAN for more details.
     * @param matchFQAN One of the following:  "ignore", "vo", "group" or "exact".
     */
    public void setMatchFQAN(String matchFQAN) {
        this.matchFQAN = matchFQAN;
    }
    
    private void prepareFQAN() {
        if (voGroup == null) {
            fqan = null;
        } else {
            if (voRole == null) {
                fqan = voGroup;
            } else {
                fqan = voGroup + "/Role=" + voRole;
            }
        }
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
     * Changes the way non-VOMS proxies are handled.
     * @param acceptProxyWithoutFQAN True if group will accept non-VOMS proxies
     */
    public void setAcceptProxyWithoutFQAN(boolean acceptProxyWithoutFQAN) {
        this.acceptProxyWithoutFQAN = acceptProxyWithoutFQAN;
    }
    
    private void setProperties() {
    	VirtualOrganization voObject = getVoObject();
        log.debug("SSL properties: sslCAFiles='" + voObject.getSslCAFiles() +"' sslCertfile='" + voObject.getSslCertfile() +"' sslKey='" + voObject.getSslKey() + "' sslKeyPasswd set:" + (voObject.getSslKeyPasswd()!= null) +"'"); 
        if (voObject.getSslCAFiles() != null) {
            System.setProperty("sslCAFiles", voObject.getSslCAFiles());
        }
        if (voObject.getSslCertfile() != null) {
            System.setProperty("sslCertfile", voObject.getSslCertfile());
        }
        if (voObject.getSslKey() != null) {
            System.setProperty("sslKey", voObject.getSslKey());
        }
        if (voObject.getSslKeyPasswd() != null) {
            System.setProperty("sslKeyPasswd", voObject.getSslKeyPasswd());
        }
    }
    
    public String toXML() {
    	String retStr = super.toXML() +
        "\t\t\tvirtualOrganization='"+vo+"'\n";
    	if (remainderUrl!=null)
    		retStr += "\t\t\tremainderUrl='"+remainderUrl+"'\n";
   		retStr += "\t\t\tmatchFQAN='"+matchFQAN+"'\n";
   		retStr += "\t\t\tacceptProxyWithoutFQAN='"+acceptProxyWithoutFQAN+"'\n"; 
    	if (voGroup!=null)
        	retStr += "\t\t\tvoGroup='"+voGroup+"'\n";
    	if (voRole!=null)
    		retStr += "\t\t\tvoRole='"+voRole+"'\n";
    	if (retStr.charAt(retStr.length()-1)=='\n')
    		retStr = retStr.substring(0, retStr.length()-1);
    	retStr += "/>\n\n";
    	return retStr;
    }
    
    public String getSummary(String bgColor) {
    	return "<td bgcolor=\""+bgColor+"\">" + getName() + "</td><td bgcolor=\""+bgColor+"\">" + matchFQAN + "</td><td bgcolor=\""+bgColor+"\">" + acceptProxyWithoutFQAN + "</td><td bgcolor=\""+bgColor+"\">" + voGroup + "</td><td bgcolor=\""+bgColor+"\">" + voRole + "</td><td bgcolor=\""+bgColor+"\">" + getVoObject().getBaseUrl() + remainderUrl + "</td>";
    }
    
    public Object clone() {
    	VOMSUserGroup userGroup = new VOMSUserGroup(getConfiguration(), getName());
    	userGroup.setAccess(getAccess());
    	userGroup.setVirtualOrganization(getVirtualOrganization());
    	userGroup.setVoRole(getVoRole());
    	userGroup.setVoGroup(getVoGroup());
    	userGroup.setMatchFQAN(getMatchFQAN());
    	userGroup.setRemainderUrl(getRemainderUrl());
    	userGroup.setAcceptProxyWithoutFQAN(acceptProxyWithoutFQAN);
    	return userGroup;
    }
}
