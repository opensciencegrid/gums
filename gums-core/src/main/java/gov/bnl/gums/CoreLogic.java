/*
 * CoreLogic.java
 *
 * Created on May 24, 2004, 3:17 PM
 */

package gov.bnl.gums;

import gov.bnl.gums.account.AccountMapper;
import gov.bnl.gums.account.GroupAccountMapper;
import gov.bnl.gums.account.AccountPoolMapper;
import gov.bnl.gums.configuration.Configuration;
import gov.bnl.gums.groupToAccount.GroupToAccountMapping;
import gov.bnl.gums.hostToGroup.HostToGroupMapping;
import gov.bnl.gums.userGroup.UserGroup;
import gov.bnl.gums.userGroup.VOMSUserGroup;

import java.util.*;

import org.apache.log4j.Logger;

/** 
 * The main interface of the core logic for GUMS, allows to update the
 * groups, generate the gridMapfiles and to map a single user.
 *
 * @author Gabriele Carcassi, Jay Packard
 */
public class CoreLogic {
    private Logger log = Logger.getLogger(CoreLogic.class); // only use this log for particularly tricky aspects of this class - otherwise log within lower level classes
    private Logger gumsAdminLog = Logger.getLogger(GUMS.gumsAdminLogName);
    GUMS gums;
    
    /**
     * Creates a Resource Manager for a particular instance of the GUMS server.
     * 
     * @param gums
     */
    public CoreLogic(GUMS gums) {
    	this.gums = gums;
    }
    
    
    /**
     * @param hostname
     * @return
     */
    public String generateOsgUserVoMap(String hostname) throws Exception {
        Configuration conf = gums.getConfiguration();
        StringBuffer osgMapBuffer = new StringBuffer("");
        HostToGroupMapping host2GroupMapper = hostToGroupMapping(conf, hostname);
        if (host2GroupMapper == null) {
        	String message = "Cannot generate osg user VO map for host '" + hostname + "' - it is not defined in any host to group mapping.";
            gumsAdminLog.warn(message);
        	throw new RuntimeException(message);
        }
        
        // Create header
        osgMapBuffer.append("#User-VO map\n");
        osgMapBuffer.append("# #comment line, format of each regular line line: account VO\n");
        osgMapBuffer.append("# Next 2 lines with VO names, same order, all lowercase, with case (lines starting with #voi, #VOc)\n");
        String voi = "#voi";
        String voc = "#VOc";
        Iterator iter = host2GroupMapper.getGroupToAccountMappings().iterator();
        while (iter.hasNext()) {
            GroupToAccountMapping gMap = (GroupToAccountMapping) conf.getGroupToAccountMapping( (String)iter.next() );
            Collection userGroups = gMap.getUserGroups();
            Iterator userGroupIt = userGroups.iterator();
            while (userGroupIt.hasNext()) {
            	UserGroup userGroup = (UserGroup) conf.getUserGroup( (String)userGroupIt.next() );
	            if (userGroup.getMemberList().size()!=0 && !gMap.getAccountingVoSubgroup().equals("") && !gMap.getAccountingVo().equals("")) {
	                voi = voi + " " + gMap.getAccountingVoSubgroup();
	                voc = voc + " " + gMap.getAccountingVo();
	            }
            }
        }
        osgMapBuffer.append(voi);
        osgMapBuffer.append("\n");
        osgMapBuffer.append(voc);
        osgMapBuffer.append("\n");
        
        // Loop through group to account mappings
        List accountsInMap = new ArrayList();
        int unknownCount = 1;
        iter = host2GroupMapper.getGroupToAccountMappings().iterator();
        while (iter.hasNext()) {
            GroupToAccountMapping gMap = (GroupToAccountMapping) conf.getGroupToAccountMapping( (String)iter.next() );
            if (!gMap.getAccountingVoSubgroup().equals("") && !gMap.getAccountingVo().equals("")) {
	            Collection userGroups = gMap.getUserGroups();
            	Collection accountMappers = gMap.getAccountMappers();
	            Iterator userGroupIt = userGroups.iterator();
	            while (userGroupIt.hasNext()) {
	            	UserGroup userGroup = (UserGroup) conf.getUserGroup( (String)userGroupIt.next() );
		            List members = userGroup.getMemberList();
		            members = new ArrayList(members);
		            osgMapBuffer.append("#---- accounts for vo: " + userGroup.getName() + " ----#\n");          
		            Collections.sort(members, retrieveUserComparatorByDN());
		            Iterator memberIter = members.iterator();
		            while (memberIter.hasNext()) {
		                GridUser user = (GridUser) memberIter.next();
		                if (gums.isUserBanned(user))
		                	continue;
	                    Iterator accountMapperIt = accountMappers.iterator();
	                    while (accountMapperIt.hasNext()) {
	                    	AccountMapper accountMapper = (AccountMapper) conf.getAccountMapper( (String)accountMapperIt.next() );
			                AccountInfo account = accountMapper.mapUser(user, false);
			                if ((account != null && account.getUser() != null) && !accountsInMap.contains(account.getUser())) {
			                	osgMapBuffer.append(account.getUser());
			                	osgMapBuffer.append(' ');
			                	osgMapBuffer.append(gMap.getAccountingVoSubgroup());
			                	osgMapBuffer.append("\n");
			                    accountsInMap.add(account.getUser());
			                }
	                    }
		            }
	            }
            }
        }
        
        String finalOsgMap = osgMapBuffer.toString();
        return finalOsgMap;
    }
    

    /**
     * Generates a fqan to account mapping for a given host and prints it to out.
     * 
     * @param hostname
     * @return
     */
    public String generateFqanMapfile(String hostname) throws Exception {
        String mapfile;
        mapfile = generateFqanMapfileImpl(hostname);
        return mapfile;
    }    
    
    /**
     * Generates a grid mapfile for a given host and prints it to out.
     * 
     * @param hostname
     * @param includeFQAN
     * @return
     */
    public String generateGridMapfile(String hostname, boolean createNewMappings, boolean includeFQAN, boolean includeEmail) throws Exception {
        String mapfile;
        mapfile = generateGridMapfileImpl(hostname, createNewMappings, includeFQAN, includeEmail);
        return mapfile;
    }
    
    /**
     * Maps a user to a local account for a given host to a grid identity.
     * 
     * @param hostname
     * @param user
     * @return
     */
    public AccountInfo map(String hostname, GridUser user, boolean checkBannedList) throws Exception {
        if (checkBannedList && gums.isUserBanned(user))
        	return null;
        return mapImpl(hostname, user);
    }
    
    /**
     * Map a local account to a list of grid identity
     * 
     * @param accountName
     * @return
     */
    public String mapAccount(String accountName) throws Exception {
    	TreeSet dns = new TreeSet();
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
            		if (gums.isUserBanned(user))
                    	continue;
                	Collection accountMappers = g2AMapping.getAccountMappers();
                    Iterator accountMappersIt = accountMappers.iterator();
                    while (accountMappersIt.hasNext()) {
                    	AccountMapper accountMapper = (AccountMapper) conf.getAccountMapper( (String)accountMappersIt.next() );
			if (accountName.equals(accountMapper.mapUser(user, false)) && !dns.contains(user.getCertificateDN()))
                        	dns.add(user.getCertificateDN());
                    }            		
            	}
            }
        }
        
        if (dns.size()==0) {
            gumsAdminLog.warn("Cannot map account '" + accountName + "' to any DN.");
        }

        StringBuffer output = new StringBuffer();
        Iterator it = dns.iterator();
        while(it.hasNext()) {
        	output.append( (String)it.next() );
        	output.append("\n");
        }

		gumsAdminLog.info("Mapped the account '" + accountName + "' to '" + output.toString() + "'");
        return output.toString();
    }
    
    /**
     * Scans the configuration and calls updateMembers() on all the groups,
     * updating the local database.
     */
    public void updateGroups() throws Exception {
        updateGroupsImpl();
    }
    
    /**
     * Scans the configuration and calls updateMembers() on all the banned groups,
     * updating the local database.
     */
    public void updateBannedGroups() throws Exception {
        updateBannedGroupsImpl();
    }
    
    /**
     * @param hostname
     * @return
     */
    private String generateFqanMapfile(HostToGroupMapping hToGMapping) {   
       	Configuration conf = hToGMapping.getConfiguration();
        Iterator iter = hToGMapping.getGroupToAccountMappings().iterator();
        TreeSet usersInMap = new TreeSet();
        
        StringBuffer fqanMapfileBuffer = new StringBuffer("");
        while (iter.hasNext()) {
            GroupToAccountMapping gMap = (GroupToAccountMapping) conf.getGroupToAccountMapping( (String)iter.next() );
            Collection userGroups = gMap.getUserGroups();
            Iterator userGroupIt = userGroups.iterator();
            while (userGroupIt.hasNext()) {
            	UserGroup userGroup = (UserGroup) conf.getUserGroup( (String)userGroupIt.next() );
            	if (userGroup instanceof VOMSUserGroup) {
            		VOMSUserGroup vomsUserGroup = (VOMSUserGroup)userGroup;
            		Collection accountMappers = gMap.getAccountMappers();
                    Iterator accountMapperIt = accountMappers.iterator();
                    while (accountMapperIt.hasNext()) {
                    	AccountMapper accountMapper = (AccountMapper) conf.getAccountMapper( (String)accountMapperIt.next() );
                    	if (accountMapper instanceof GroupAccountMapper || accountMapper instanceof AccountPoolMapper) {
	                		String fqan = vomsUserGroup.getVoGroup() + (vomsUserGroup.getRole().equals("") ? "" : "/Role=" + vomsUserGroup.getRole());
                        	String account = null;
	                       	if (accountMapper instanceof GroupAccountMapper) {
	                    		GroupAccountMapper groupPoolMapper = (GroupAccountMapper)accountMapper;
	                    		account = groupPoolMapper.getAccountName();
	                    	}
	                    	else if (accountMapper instanceof AccountPoolMapper){
	                    		AccountPoolMapper accountPoolMapper = (AccountPoolMapper)accountMapper;
	                    		account = accountPoolMapper.getAssignments();
	                    		if (account==null || account.length()==0)
	                    			account = "null";
	                    	}
	                       	if (account != null) {
	                       		fqanMapfileBuffer.append('"');
	                       		fqanMapfileBuffer.append( fqan );
	                       		fqanMapfileBuffer.append('"');
	                       		fqanMapfileBuffer.append(' ');
	                       		fqanMapfileBuffer.append( account );
	                       		fqanMapfileBuffer.append("\n");
	                       		break;
	                       	}
                    	}
                    }
            	}
            }
        }
        
        return fqanMapfileBuffer.toString();
    }    
    
    private String generateGridMapfile(HostToGroupMapping hToGMapping, boolean createNewMappings, boolean includeFQAN, boolean includeEmail) throws Exception {
    	Configuration conf = hToGMapping.getConfiguration();
        Iterator iter = hToGMapping.getGroupToAccountMappings().iterator();
        HashSet usersInMap = new HashSet();
        
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
	            if (includeFQAN) {
		            while (memberIter.hasNext()) {
		                GridUser user = (GridUser) memberIter.next();
		                if (gums.isUserBanned(user))
	                    	continue;
	                    	String fqan = user.getVoFQAN()!=null?user.getVoFQAN().toString():"";
	                    	String email = user.getEmail()!=null?user.getEmail().toString():"";
		                if (!usersInMap.contains(user.getCertificateDN() + fqan) ) {
		                    Collection accountMappers = gMap.getAccountMappers();
		                    Iterator accountMappersIt = accountMappers.iterator();
		                    while (accountMappersIt.hasNext()) {
		                    	AccountMapper accountMapper = (AccountMapper) conf.getAccountMapper( (String)accountMappersIt.next() );
			                	AccountInfo account = accountMapper.mapUser(user, true);
			                	if (account != null && account.getUser() != null) {
			                        gridMapfileBuffer.append('"');
			                        gridMapfileBuffer.append( user.getCertificateDN() );
			                        gridMapfileBuffer.append('"' );
			                        gridMapfileBuffer.append(" \"");
			                        gridMapfileBuffer.append( fqan );
			                        gridMapfileBuffer.append('"');         	
			                        gridMapfileBuffer.append(' ');
			                        gridMapfileBuffer.append( account.getUser() );
			                        if (includeEmail) {
				                        gridMapfileBuffer.append(' ');
				                        gridMapfileBuffer.append( email );
			                        }
			                        gridMapfileBuffer.append("\n");
		                        	usersInMap.add( user.getCertificateDN() + fqan );
		                        	break;
			                    } else {
			                    	gumsAdminLog.debug("User " + user + " from group " + gMap.getUserGroups() + " can't be mapped.");
			                    }
		                    }
		            	}
		            }
	            }
	            else {
		            while (memberIter.hasNext()) {
		                GridUser user = (GridUser) memberIter.next();
		                if (gums.isUserBanned(user))
	                    	continue;
		                String email = user.getEmail()!=null?user.getEmail().toString():"";
		                if (!usersInMap.contains(user.getCertificateDN()) && userGroup.isInGroup(new GridUser(user.getCertificateDN(), null))) {
		                	Collection accountMappers = gMap.getAccountMappers();
		                    Iterator accountMappersIt = accountMappers.iterator();
		                    while (accountMappersIt.hasNext()) {
		                    	AccountMapper accountMapper = (AccountMapper) conf.getAccountMapper( (String)accountMappersIt.next() );
			                	AccountInfo account = accountMapper.mapUser(user, true);
			                    if (account != null && account.getUser() != null) {
			                        gridMapfileBuffer.append('"');
			                        gridMapfileBuffer.append( user.getCertificateDN() );
			                        gridMapfileBuffer.append('"' );
			                        gridMapfileBuffer.append(' ');
			                        gridMapfileBuffer.append( account.getUser() );
			                        if (includeEmail && email!=null) {
				                        gridMapfileBuffer.append(' ');
				                        gridMapfileBuffer.append( email );
			                        }
			                        gridMapfileBuffer.append("\n");
		                        	usersInMap.add(user.getCertificateDN());
		                        	break;
			                    } else {
			                    	gumsAdminLog.debug("User " + user + " from group " + gMap.getUserGroups() + " can't be mapped.");
			                    }
		                    }
		            	}
		            }	            	
	            }
            }
        }

        String finalGridmapfile = gridMapfileBuffer.toString();
        return finalGridmapfile;
    }
 
    private String generateFqanMapfileImpl(String hostname) throws Exception {
        Configuration conf = gums.getConfiguration();
        HostToGroupMapping host2GroupMapper = hostToGroupMapping(conf, hostname);
        if (host2GroupMapper == null) {
        	String message = "Cannot generate FQAN mapfile for host '" + hostname + "' - it is not defined in any host to group mapping.";
            gumsAdminLog.warn(message);
        	throw new RuntimeException(message);
        }
        String mapfile = generateFqanMapfile(host2GroupMapper);
        return mapfile;
    }
    
    private String generateGridMapfileImpl(String hostname, boolean createNewMappings, boolean includeFqan, boolean includeEmail) throws Exception {
    	Configuration conf = gums.getConfiguration();
        HostToGroupMapping host2GroupMapper = hostToGroupMapping(conf, hostname);
        if (host2GroupMapper == null) {
        	String message = "Cannot generate grid mapfile for host '" + hostname + "' - it is not defined in any host to group mapping.";
            gumsAdminLog.warn(message);
        	throw new RuntimeException(message);
        }
        String mapfile = generateGridMapfile(host2GroupMapper, createNewMappings, includeFqan, includeEmail);
        return mapfile;
    }
    
    private HostToGroupMapping hostToGroupMapping(Configuration conf, String hostname) {
    	Collection hostToGroupMappers = conf.getHostToGroupMappings();
    	Iterator it = hostToGroupMappers.iterator();
        while (it.hasNext()) {
            HostToGroupMapping hostToGroupMapper = (HostToGroupMapping) it.next();
            if (hostToGroupMapper.isInGroup(hostname)) {
                return hostToGroupMapper;
            }
        }
        return null;
    }

    private AccountInfo mapImpl(String hostname, GridUser user) throws Exception {
        Configuration conf = gums.getConfiguration();
        HostToGroupMapping host2GroupMapper = hostToGroupMapping(conf, hostname);
        if (host2GroupMapper == null) {
        	String message = "Cannot map user for host '" + hostname + "' - it is not defined in any host to group mapping.";
            gumsAdminLog.warn(message);
        	throw new RuntimeException(message);
        }

        Iterator g2AMappingsIt = host2GroupMapper.getGroupToAccountMappings().iterator();
        while (g2AMappingsIt.hasNext()) {
            GroupToAccountMapping g2AMapping = (GroupToAccountMapping) conf.getGroupToAccountMapping( (String)g2AMappingsIt.next() );
            Collection userGroups = g2AMapping.getUserGroups();
            Iterator userGroupsIt = userGroups.iterator();
            while (userGroupsIt.hasNext()) {
            	UserGroup userGroup = (UserGroup) conf.getUserGroup( (String)userGroupsIt.next() );
                if (userGroup.isInGroup(user)) {
                    if (gumsAdminLog.isDebugEnabled()) { gumsAdminLog.debug("User " + user + " is in group " + userGroup); }
                    Collection accountMappers = g2AMapping.getAccountMappers();
                    Iterator accountMappersIt = accountMappers.iterator();
                    while (accountMappersIt.hasNext()) {
                    	AccountMapper accountMapper = (AccountMapper) conf.getAccountMapper( (String)accountMappersIt.next() );
                        AccountInfo localUser = accountMapper.mapUser(user, true);
                        if (localUser != null)
                            return localUser;
                    }
                } else if (gumsAdminLog.isDebugEnabled()) {
                    gumsAdminLog.debug("User " + user + " is not in group " + userGroup);
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
                return user1.compareDn(user2);
            }
        };
    }
    
    private void updateGroupsImpl() throws Exception {
        Collection groups = gums.getConfiguration().getUserGroups().values();
        gumsAdminLog.info("Updating user group users for all " + groups.size() + " user groups");
        StringBuffer failedGroups = null;
        Iterator iter = groups.iterator();
        while (iter.hasNext()) {
            UserGroup group = (UserGroup) iter.next();
            if (!gums.getConfiguration().getBannedUserGroupList().contains(group)) {
            	// Banned user groups updated in another function
	            try {
	                group.updateMembers();
	                gumsAdminLog.info("User group update for " + group.getName() + " (" + group.getMemberList().size() + " users).");
	            } catch (Exception e) {
	                gumsAdminLog.warn("User group update for " + group.getName() + " failed: " + e.getMessage());
			gumsAdminLog.debug("Failure exception", e);
	            	if (failedGroups == null)
	            		failedGroups = new StringBuffer("Some user groups weren't updated correctly:\n");
	            	failedGroups.append( group.getName() + " - " + e.getMessage() + "\n" );
	            }
            }
        }
        if (failedGroups == null) {
        	gumsAdminLog.info("Finished updating users for all user groups");
        }
        else {
        	gumsAdminLog.warn("Some user groups weren't updated correctly");
            throw new RuntimeException(failedGroups.toString());
        }
    }
    
    private void updateBannedGroupsImpl() throws Exception {
        Collection<String> groups = gums.getConfiguration().getBannedUserGroupList();
        if (groups.size()>0) {
	        gumsAdminLog.info("Updating user group users for all " + groups.size() + " banned user groups");
	        StringBuffer failedGroups = null;
	        for (String groupName : groups) {
	            UserGroup group = gums.getConfiguration().getUserGroup(groupName);
		    if (group == null) { continue; }
	            try {
	                group.updateMembers();
	                gumsAdminLog.info("User group update for " + group.getName() + " (" + group.getMemberList().size() + " users).");
	            } catch (Exception e) {
	                gumsAdminLog.warn("User group update for " + group.getName() + " failed: " + e.getMessage());
	            	if (failedGroups == null)
	            		failedGroups = new StringBuffer("Some banned user groups weren't updated correctly:\n");
	            	failedGroups.append( group.getName() + " - " + e.getMessage() + "\n" );
	            }
	        }
	        if (failedGroups == null) {
	        	gumsAdminLog.info("Finished updating banned users for all user groups");
	        }
	        else {
	        	gumsAdminLog.warn("Some banned user groups weren't updated correctly");
	            throw new RuntimeException(failedGroups.toString());
	        }
        }
    }    
}
