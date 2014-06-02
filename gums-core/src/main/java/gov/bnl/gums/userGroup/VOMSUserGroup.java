/*
 * VOMSUserGroupManager.java
 *
 * Created on May 25, 2004, 10:20 AM
 */

package gov.bnl.gums.userGroup;

import gov.bnl.gums.FQAN;
import gov.bnl.gums.GridUser;
import gov.bnl.gums.configuration.Configuration;
import gov.bnl.gums.db.UserGroupDB;

import java.net.URL;
import java.util.*;

import org.apache.log4j.Logger; 
import org.apache.log4j.Level;

import org.glite.voms.generated.*;

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
	static private String[] matchFQANTypes = {"exact","vorole","role","vogroup","vo","ignore"};
	
	/*static {
       Logger.getLogger(org.glite.security.trustmanager.CRLFileTrustManager.class.getName()).setLevel(Level.ERROR);
       Logger.getLogger("org.glite.security.trustmanager.axis.AXISSocketFactory").setLevel(Level.OFF);
       Logger.getLogger("org.glite.security.util.DirectoryList").setLevel(Level.OFF);
       VOMSValidator.setTrustStore(new BasicVOMSTrustStore("/etc/grid-security/certificates", 12*3600*1000));
	}*/
	
    static public String getTypeStatic() {
		return "voms";
	}
    
    static public List getMatchFQANTypes() {
		ArrayList retList = new ArrayList();
		for(int i=0; i<matchFQANTypes.length; i++)
			retList.add(matchFQANTypes[i]);
		return retList;
	}

    private Logger log = Logger.getLogger(VOMSUserGroup.class);
    private String vomsServer = "";
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
    	VOMSUserGroup userGroup = new VOMSUserGroup(configuration, new String(getName()));
    	userGroup.setDescription(new String(getDescription()));
    	userGroup.setAccess(new String(getAccess()));
    	userGroup.setVomsServer(new String(getVomsServer()));
    	userGroup.setRole(new String(getRole()));
    	userGroup.setVoGroup(new String(getVoGroup()));
    	userGroup.setMatchFQAN(new String(getMatchFQAN()));
    	userGroup.setRemainderUrl(new String(getRemainderUrl()));
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
     *   <li>role - role has to match.</li>
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
		if (getVoDB()!=null)
			return getVoDB().retrieveMembers();
		else
			return new ArrayList();
    }
    
    public String getRemainderUrl() {
    	return remainderUrl;
    }
    
    public String getType() {
		return "voms";
	}
    
    public String getUrl() {
		if (getVoObject()!=null)
			return getVoObject().getBaseUrl() + remainderUrl;
		else
			return "";
    }
    
    /**
     * Get name of VomsServer
     * @return
     */
    public String getVomsServer() {
    	return vomsServer;
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
            log.trace("VOMS Service Locator: url='" + getUrl() + "/services/VOMSAdmin'");
//            System.setProperty("axis.socketSecureManager", "org.glite.security.trustmanager.axis.AXISSocketManager");
            VOMSAdminServiceLocator locator = new VOMSAdminServiceLocator();
            URL vomsUrl = new URL( getUrl() + "/services/VOMSAdmin" );
            log.info("Connecting to VOMS admin at " + vomsUrl);
            return locator.getVOMSAdmin(vomsUrl);
        } catch (Throwable e) {
            log.error("Couldn't get VOMS Admin: ", e);
            throw new RuntimeException("Couldn't get VOMS Admin: " + e.getMessage(), e);
        }
    }    
    
    public VOMSCompatibility getVOMSCompatibility() {
        try {
            log.trace("VOMS Service Locator: url='" + getUrl() + "/services/VOMSAdmin'");
            VOMSCompatibilityServiceLocator locator = new VOMSCompatibilityServiceLocator();
            URL vomsUrl = new URL( getUrl() + "/services/VOMSCompatibility" );
            log.info("Connecting to VOMS Compatibility at " + vomsUrl);
            return locator.getVOMSCompatibility(vomsUrl);
        } catch (Throwable e) {
            log.error("Couldn't get VOMS Compatiblity interface: ", e);
            throw new RuntimeException("Couldn't get VOMS Compatibility interface: " + e.getMessage(), e);
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

    @Override
    public boolean isInGroup(GridUser user) {
    	if (user.getVoFQAN() == null) {
            // If the user comes in without FQAN and we don't accept proxies without fqan,
            // kick him out right away
	        if (!isAcceptProxyWithoutFQAN())
	            return false;
	        // If the user comes in without FQAN and we accept proxies without it,
	        // we simply check whether the DN is in the database
	        else {
	        	if (getVoDB()!=null)
	        		return getVoDB().isMemberInGroup(new GridUser(user.getCertificateDN(), fqan));
	        	else
	        		return false;
	        }
    	}

        // We now know we don't have user.getVoFQAN()==null

        // If we have vorole match, entire fqan has to be the same
        if ("exact".equals(getMatchFQAN())) {
            if (!user.getVoFQAN().toString().equals(fqan))
                return false;
        }
        
        // If we have a vo-role match, vo and role has to be the same
        if ("vorole".equals(getMatchFQAN()) && user.getVoFQAN().getVo()!=null && user.getVoFQAN().getRole()!=null) {
        	FQAN theFQAN = new FQAN(fqan);
            if (!user.getVoFQAN().getVo().equals(theFQAN.getVo()) && !user.getVoFQAN().getRole().equals(theFQAN.getRole()))
                return false;
        }
        
        // If we have a role match, role has to be the same
        if ("role".equals(getMatchFQAN()) && user.getVoFQAN().getRole()!=null) {
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
        if ("vo".equals(getMatchFQAN()) && user.getVoFQAN().getVo()!=null) {
            FQAN theFQAN = new FQAN(fqan);
            if (!user.getVoFQAN().getVo().equals(theFQAN.getVo()))
                return false;
        }
        
        // FQAN matches, let's look up if the DN is in the db
        // If not, he's kicked out
		if (getVoDB()!=null)
			return getVoDB().isMemberInGroup(new GridUser(user.getCertificateDN(), fqan));
		else
			return true;
    }

    @Override
    public boolean isDNInGroup(GridUser user) {
        if (getVoDB() != null) {
            return getVoDB().isDNInGroup(user);
        } else {
            return true;
        }
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
     * @param matchFQAN One of the following:  "exact, "vorole, "role", "vogroup", "vo", "ignore". (also "group" for backwards compat.)
     */
    public void setMatchFQAN(String matchFQAN) {
    	boolean found = false;
    	if (matchFQAN.equals("group"))
    		matchFQAN = "vogroup";
    	if (matchFQAN.equals(""))
    		matchFQAN = "exact";
    	for (int i=0; i<matchFQANTypes.length; i++)
    		if (matchFQANTypes[i].equalsIgnoreCase(matchFQAN)) found = true;
    	if (!found)
    		throw new RuntimeException("Invalid match FQAN string: "+matchFQAN);
        this.matchFQAN = matchFQAN;
    }
    
    public void setRemainderUrl(String remainderUrl) {
    	this.remainderUrl = remainderUrl;
    }

    /**
     * Set name of VOMS Server
     * @param vo
     */
    public void setVomsServer(String vomsServer) {
    	this.vomsServer = vomsServer;
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
     * @param role The role in the VOMS (i.e.production)
     */
    public void setRole(String role) {
        this.role = role;
        prepareFQAN();
    }

    public String toString() {
        return "VOMSGroup: remainderUrl='" + remainderUrl + "' - voGroup='" + getVoGroup() + "' - role='" + getRole() + "'";
    }
    
    public String toString(String bgColor) {
    	return "<td bgcolor=\""+bgColor+"\"><a href=\"userGroups.jsp?command=edit&name=" + getName() + "\">" + getName() + "</a></td><td bgcolor=\""+bgColor+"\">" + getType() + "</td><td bgcolor=\""+bgColor+"\">" + matchFQAN + "</td><td bgcolor=\""+bgColor+"\">" + acceptProxyWithoutFQAN + "</td><td bgcolor=\""+bgColor+"\">" + voGroup + "&nbsp;</td><td bgcolor=\""+bgColor+"\">" + role + "&nbsp;</td>";
    }

    public String toXML() {
    	String retStr = "\t\t<vomsUserGroup\n"+
		"\t\t\tname='"+getName()+"'\n"+
		"\t\t\taccess='"+accessTypes[accessIndex]+"'\n" +
		"\t\t\tdescription='"+getDescription()+"'\n"+
        "\t\t\tvomsServer='"+vomsServer+"'\n";
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
		if (getVoDB()!=null)
	   		getVoDB().loadUpdatedList(retrieveMembers());
                else 
                    throw new RuntimeException("Could not updateMembers for " + vomsServer + " getVoDB returned null");
                // note, if for some reason getVoDB returns null, you SILENTLY
                // fail to load the new updated list It is no longer silent.
                // note - this ***SHOULD*** not happen.
                //
                // getVoDB returns null if 1) getVoObject() returns null, or if
                // getVoDB does not return null, but getVoObject().getDB(getName())
                // does.

                // getVoObject checks to make sure that there is a configuration
                // and if so, gets it and calls getVomsServer(vomsServer) on it.
                // if there is no configuration, it crashes

    }
    
    private UserGroupDB getVoDB() {
		if (getVoObject()!=null)
			return getVoObject().getDB( getName() );
		else
			return null;
    }
    
    private VomsServer getVoObject() {
    	if (getConfiguration()==null)
    		throw new RuntimeException("Configuration has not yet been set for this class");
    	return getConfiguration().getVomsServer(vomsServer);
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
    
    private List retrieveMembersOriginal() {
		if (getVoObject()==null)
			return null;
        Properties p = System.getProperties();
        try {
            setProperties();
            log.debug("SSL properties read: " + 
            "sslCertfile='" + System.getProperty("sslCertfile") +
            "' sslKey='" + System.getProperty("sslKey") +
            "' sslKeyPasswd set:" + (System.getProperty("sslKeyPasswd")!=null) +
            " sslCAFiles='" + System.getProperty("sslCAFiles") + "'" ); 
            System.setProperty("axis.socketSecureFactory", "org.glite.security.trustmanager.axis.AXISSocketFactory");
            VOMSAdmin voms = getVOMSAdmin();
        	org.glite.voms.generated.User[] users = null;
            if (role.equals("")) {
                users = voms.listMembers( !getVoGroup().equals("")?getVoGroup():null );
            } else if(!getVoGroup().equals("")) {
                users = voms.listUsersWithRole( getVoGroup(), "Role=" + getRole());
            }        	
            if (users.length > 0) {
                log.info("Retrieved " + users.length + " users.");
                log.info("First user is: '" + users[0].getDN() + "'");
                log.info("Last user is: '" + users[users.length - 1 ].getDN() + "'");
            } else {
                log.info("Retrieved no users.");
            }
            System.setProperties(p);
            List entries = new ArrayList();
            for (int n=0; n < users.length; n++) {
            	GridUser gridUser = new GridUser(users[n].getDN(), fqan, users[n].getMail());
                entries.add(gridUser);
            }
            return entries;
        } catch (Throwable e) {
        	String message = "Couldn't retrieve users: ";
            log.error(message, e);
            e.printStackTrace();
            throw new RuntimeException(message + e.getMessage());
        }
    }
    
    /**
    * Retrieves the list of members for this VOMSUserGroup
    * 
    * 
    * 
    */
    private List retrieveMembers() {
		if (getVoObject()==null)
			return null;
        Properties p = System.getProperties();
        try {
            setProperties();
            log.debug("SSL properties read: " + 
            "sslCertfile='" + System.getProperty("sslCertfile") +
            "' sslKey='" + System.getProperty("sslKey") +
            "' sslKeyPasswd set:" + (System.getProperty("sslKeyPasswd")!=null) +
            " sslCAFiles='" + System.getProperty("sslCAFiles") + "'" ); 
            System.setProperty("axis.socketSecureFactory", "org.glite.security.trustmanager.axis.AXISSocketFactory");
                     
            VOMSCompatibility vomscompat = getVOMSCompatibility();
            
        	String[] users = null;
            String container = null;
            
        	if ((getVoGroup().equals("") && role.equals("") )) {
        		container = null;
            } else if (!getVoGroup().equals("") && role.equals("") ) {
            	container = getVoGroup();
            } else {
            	container = getVoGroup() + "/Role=" + getRole();
            	
            }
        	users = vomscompat.getGridmapUsers(container);        	
    	
            if (users.length > 0) {
                log.info("Retrieved " + users.length + " users.");
                log.info("First user is: '" + users[0] + "'");
                log.info("Last user is: '" + users[users.length - 1 ] + "'");
            } else {
                log.info("Retrieved no users.");
            }
            System.setProperties(p);
            List entries = new ArrayList();
            for (int n=0; n < users.length; n++) {
            	GridUser gridUser = new GridUser(users[n], fqan);
                entries.add(gridUser);
            }
            return entries;
        } catch (Throwable e) {
        	String message = "Couldn't retrieve users: ";
            log.error(message, e);
            e.printStackTrace();
            throw new RuntimeException(message + e.getMessage());
        }
    }
        
    
    
    
    private void setProperties() {
    	VomsServer voObject = getVoObject();
    	if (voObject!=null) {
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
}
