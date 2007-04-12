/*
 * ResourceManager.java
 *
 * Created on May 24, 2004, 3:17 PM
 */

package gov.bnl.gums;

import gov.bnl.gums.account.AccountMapper;
import gov.bnl.gums.configuration.Configuration;
import gov.bnl.gums.groupToAccount.GroupToAccountMapping;
import gov.bnl.gums.hostToGroup.HostToGroupMapping;
import gov.bnl.gums.userGroup.UserGroup;

import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/** The main interface of the core logic for GUMS, allows to update the
 * groups, generate the gridMapfiles and to map a single user.
 *
 * @author Gabriele Carcassi, Jay Packard
 */
public class ResourceManager {
    private Log log = LogFactory.getLog(ResourceManager.class);
    private Log resourceAdminLog = LogFactory.getLog(GUMS.resourceAdminLog);
    GUMS gums;
    
    /** Creates a Resource Manager for a particula instance of the GUMS server.
     */
    public ResourceManager(GUMS gums) {
        this.gums = gums;
    }
    
    public String generateGrid3UserVoMap(String hostname) {
        Configuration conf = gums.getConfiguration();
        HostToGroupMapping hostGroup = host2GroupMapping(conf, hostname);
        if (hostGroup == null)
            throw new RuntimeException("The host '" + hostname + "' is not defined in any group.");
        StringBuffer grid3MapBuffer = new StringBuffer("");
        Iterator iter = hostGroup.getGroupToAccountMappings().iterator();
        
        grid3MapBuffer.append("#User-VO map\n");
        grid3MapBuffer.append("# #comment line, format of each regular line line: account VO\n");
        grid3MapBuffer.append("# Next 2 lines with VO names, same order, all lowercase, with case (lines starting with #voi, #VOc)\n");
        
        String voi = "#voi";
        String voc = "#VOc";
        while (iter.hasNext()) {
            GroupToAccountMapping gMap = (GroupToAccountMapping) conf.getGroupToAccountMapping( (String)iter.next() );
            Collection userGroups = gMap.getUserGroups();
            Iterator userGroupIt = userGroups.iterator();
            while (userGroupIt.hasNext()) {
            	UserGroup userGroup = (UserGroup) conf.getUserGroup( (String)userGroupIt.next() );
	            if (gMap.getAccountingVo() != null && gMap.getAccountingDesc() != null && userGroup.getMemberList().size() != 0) {
	                voi = voi + " " + gMap.getAccountingVo();
	                voc = voc + " " + gMap.getAccountingDesc();
	            }
            }
        }
        
        grid3MapBuffer.append(voi);
        grid3MapBuffer.append("\n");
        grid3MapBuffer.append(voc);
        grid3MapBuffer.append("\n");
        
        iter = hostGroup.getGroupToAccountMappings().iterator();
        
        List accountsInMap = new ArrayList();
        
        while (iter.hasNext()) {
            GroupToAccountMapping gMap = (GroupToAccountMapping) conf.getGroupToAccountMapping( (String)iter.next() );
            Collection userGroups = gMap.getUserGroups();
            Iterator userGroupIt = userGroups.iterator();
            while (userGroupIt.hasNext()) {
            	UserGroup userGroup = (UserGroup) conf.getUserGroup( (String)userGroupIt.next() );
	            List members = userGroup.getMemberList();
	            members = new ArrayList(members);
	            Collections.sort(members, retrieveUserComparatorByDN());
	            grid3MapBuffer.append("#---- accounts for vo: " + userGroup.getName() + " ----#\n");          
	            Iterator memberIter = members.iterator();
	            while (memberIter.hasNext()) {
	                GridUser user = (GridUser) memberIter.next();
	                Collection accountMappers = gMap.getAccountMappers();
                    Iterator accountMapperIt = accountMappers.iterator();
                    while (accountMapperIt.hasNext()) {
                    	AccountMapper accountMapper = (AccountMapper) conf.getAccountMapper( (String)accountMapperIt.next() );
		                String username = accountMapper.mapUser(user.getCertificateDN());
		                if ((username != null) && !accountsInMap.contains(username) && (gMap.getAccountingVo() != null)) {
		                    grid3MapBuffer.append(username);
		                    grid3MapBuffer.append(' ');
		                    grid3MapBuffer.append(gMap.getAccountingVo());
		                    grid3MapBuffer.append("\n");
		                    accountsInMap.add(username);
		                }
                    }
	            }
            }
        }
        String finalGrid3Map = grid3MapBuffer.toString();
        return finalGrid3Map;
    }
    
    /** Generates a grid mapfile for a given host and prints it to out.
     */
    public String generateGridMapfile(String hostname, boolean includeFQAN) {
        String mapfile;
        mapfile = generateGridMapfileImpl(hostname, includeFQAN);
        return mapfile;
    }
    
    /** Maps a user to a local account for a given host.
     */
    public String map(String hostname, GridUser user) {
        String value;
        value = mapImpl(hostname, user);
        return value;
    }
    
    public String mapAccount(String accountName) {
    	String allDNs = null;
        Configuration conf = gums.getConfiguration();
        Iterator g2AMappingsIt = conf.getGroupToAccountMappings().values().iterator();
        while (g2AMappingsIt.hasNext()) {
            GroupToAccountMapping g2AMapping = (GroupToAccountMapping) g2AMappingsIt.next();
            Collection userGroups = g2AMapping.getUserGroups();
            Iterator userGroupsIt = userGroups.iterator();
            while (userGroupsIt.hasNext()) {
            	UserGroup userGroup = (UserGroup) conf.getUserGroup( (String)userGroupsIt.next() );
            	List users = userGroup.getMemberList();
            	Iterator usersIt = users.iterator();
            	while (usersIt.hasNext()) {
            		GridUser user = (GridUser)usersIt.next();
                	Collection accountMappers = g2AMapping.getAccountMappers();
                    Iterator accountMappersIt = accountMappers.iterator();
                    while (accountMappersIt.hasNext()) {
                    	AccountMapper accountMapper = (AccountMapper) conf.getAccountMapper( (String)accountMappersIt.next() );
                        if (accountName.equals(accountMapper.mapUser(user.getCertificateDN()))) {
                        	if (allDNs==null)
                        		allDNs = new String();
                        	else
                        		allDNs += "\n";
                        	if (allDNs.indexOf(user.getCertificateDN())==-1)
                        		allDNs += user.getCertificateDN();
                        }
                    }            		
            	}
            }
        }
        return allDNs;
    }
    
    /** Scans the configuration and calls updateMembers() on all the groups,
     * updating the local database.
     */
    public void updateGroups() {
        updateGroupsImpl();
    }
    
    /**
     * @todo usersInMap should be a sorted list to make things faster, but
     * no ready made implementation was found.
     */
    private String generateGridMapfile(HostToGroupMapping h2GMapping, boolean includeFQAN) {
    	Configuration conf = h2GMapping.getConfiguration();
        Iterator iter = h2GMapping.getGroupToAccountMappings().iterator();
        List usersInMap = new ArrayList();
        
        StringBuffer gridMapfileBuffer = new StringBuffer("");
        while (iter.hasNext()) {
            GroupToAccountMapping gMap = (GroupToAccountMapping) conf.getGroupToAccountMapping( (String)iter.next() );
            Collection userGroups = gMap.getUserGroups();
            Iterator userGroupIt = userGroups.iterator();
            while (userGroupIt.hasNext()) {
            	UserGroup userGroup = (UserGroup) conf.getUserGroup( (String)userGroupIt.next() );
	            List members = userGroup.getMemberList();
	            members = new ArrayList(members);
	            Collections.sort(members, retrieveUserComparatorByDN());
	            gridMapfileBuffer.append("#---- members of vo: " + userGroup.getName() + " ----#\n");
	            
	            Iterator memberIter = members.iterator();
	            while (memberIter.hasNext()) {
	                GridUser user = (GridUser) memberIter.next();
	                if ((!usersInMap.contains(user.getCertificateDN())) && (userGroup.isInGroup(new GridUser(user.getCertificateDN(), null)))) {
	                	Collection accountMappers = gMap.getAccountMappers();
	                    Iterator accountMappersIt = accountMappers.iterator();
	                    while (accountMappersIt.hasNext()) {
	                    	AccountMapper accountMapper = (AccountMapper) conf.getAccountMapper( (String)accountMappersIt.next() );
		                	String username = accountMapper.mapUser(user.getCertificateDN());
		                    if (username != null) {
		                        gridMapfileBuffer.append('"');
		                        gridMapfileBuffer.append(user.getCertificateDN() );
		                        gridMapfileBuffer.append('"' );
		                        if (includeFQAN) {
			                        gridMapfileBuffer.append(" \"");
			                        gridMapfileBuffer.append(user.getVoFQAN() );
			                        gridMapfileBuffer.append('"' );		                        	
		                        }
		                        gridMapfileBuffer.append(' ');
		                        gridMapfileBuffer.append(username );
		                        gridMapfileBuffer.append("\n");
		                        usersInMap.add(user.getCertificateDN());
		                    } else {
		                        resourceAdminLog.warn("User " + user + " from group " + gMap.getUserGroups() + " can't be mapped.");
		                    }
	                    }
	                }
	            }
            }
        }
        String finalGridmapfile = gridMapfileBuffer.toString();
        return finalGridmapfile;
    }
    
    private String generateGridMapfileImpl(String hostname, boolean generateGridMapfile) {
        Configuration conf = gums.getConfiguration();
        HostToGroupMapping host2GroupMapper = host2GroupMapping(conf, hostname);
        if (host2GroupMapper == null) return null;
        String mapfile = generateGridMapfile(host2GroupMapper, generateGridMapfile);
        return mapfile;
    }
    
    private HostToGroupMapping host2GroupMapping(Configuration conf, String hostname) {
    	Collection host2GroupMappers = conf.getHostToGroupMappings();
    	Iterator it = host2GroupMappers.iterator();
        while (it.hasNext()) {
            HostToGroupMapping host2GroupMapper = (HostToGroupMapping) it.next();
            if (host2GroupMapper.isInGroup(hostname)) {
                return host2GroupMapper;
            }
        }
        return null;
    }

    private String mapImpl(String hostname, GridUser user) {
        Configuration conf = gums.getConfiguration();
        HostToGroupMapping hostToGroupMapping = host2GroupMapping(conf, hostname);
        if (hostToGroupMapping == null) return null;
        Iterator g2AMappingsIt = hostToGroupMapping.getGroupToAccountMappings().iterator();
        while (g2AMappingsIt.hasNext()) {
            GroupToAccountMapping g2AMapping = (GroupToAccountMapping) conf.getGroupToAccountMapping( (String)g2AMappingsIt.next() );
            Collection userGroups = g2AMapping.getUserGroups();
            Iterator userGroupsIt = userGroups.iterator();
            while (userGroupsIt.hasNext()) {
            	UserGroup userGroup = (UserGroup) conf.getUserGroup( (String)userGroupsIt.next() );
                if (userGroup.isInGroup(user)) {
                	Collection accountMappers = g2AMapping.getAccountMappers();
                    Iterator accountMappersIt = accountMappers.iterator();
                    while (accountMappersIt.hasNext()) {
                    	AccountMapper accountMapper = (AccountMapper) conf.getAccountMapper( (String)accountMappersIt.next() );
                        String localUser = accountMapper.mapUser(user.getCertificateDN());
                        if (user != null) {
                            return localUser;
                        } else {
                            if (conf.isErrorOnMissedMapping()) {
                                resourceAdminLog.error("User " + user + " wasn't mapped even though is present in group " + g2AMapping.getUserGroups());
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    private Comparator retrieveUserComparatorByDN() {
        return new Comparator() {
            public int compare(Object o1, Object o2) {
                GridUser user1 = (GridUser) o1;
                GridUser user2 = (GridUser) o2;
                return user1.getCertificateDN().compareTo(user2.getCertificateDN());
            }
        };
    }
    
    private void updateGroupsImpl() {
        boolean success = true;
/*        UserGroupManager admins = gums.getConfiguration().getAdminGroup();
        if (admins != null) {
            log.debug("Updating group " + admins);
            try {
                admins.updateMembers();
                resourceAdminLog.info("Admin group " + admins.toString() + " updated");
            } catch (Exception e) {
                resourceAdminLog.warn("Admin group " + admins.toString() + " wasn't updated successfully: " + " [" + e.getMessage() + "]");
                log.warn("updateMember for " + admins + " threw an exception", e);
                success = false;
            }
        }*/
        Collection groups = gums.getConfiguration().getUserGroups().values();
        log.info("Updating group information for all " + groups.size() + " groups");
        Iterator iter = groups.iterator();
        while (iter.hasNext()) {
            UserGroup group = (UserGroup) iter.next();
            log.debug("Updating group " + group);
            try {
                group.updateMembers();
                resourceAdminLog.info(group.toString() + " updated");
            } catch (Exception e) {
                resourceAdminLog.warn(group.toString() + " wasn't updated successfully: " + " [" + e.getMessage() + "]");
                log.warn("updateMember for " + group + " threw an exception", e);
                success = false;
            }
        }
        if (!success) {
            throw new RuntimeException("Some groups weren't updated correctly: consult the logs for more details. Check GUMS configuration or the status of the VO servers.");
        }
    }
}
