/*
 * ResourceManager.java
 *
 * Created on May 24, 2004, 3:17 PM
 */

package gov.bnl.gums;

import gov.bnl.gums.account.*;
import gov.bnl.gums.configuration.Configuration;
import gov.bnl.gums.groupToAccount.GroupToAccountMapping;
import gov.bnl.gums.hostToGroup.HostToGroupMapping;
import gov.bnl.gums.userGroup.UserGroup;
import gov.bnl.gums.userGroup.VOMSUserGroup;

import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/** 
 * The main interface of the core logic for GUMS, allows to update the
 * groups, generate the gridMapfiles and to map a single user.
 *
 * @author Gabriele Carcassi, Jay Packard
 */
public class ResourceManager {
    private Log log = LogFactory.getLog(ResourceManager.class);
    private Log resourceAdminLog = LogFactory.getLog(GUMS.resourceAdminLog);
    GUMS gums;
    
    /**
     * Creates a Resource Manager for a particular instance of the GUMS server.
     * 
     * @param gums
     */
    public ResourceManager(GUMS gums) {
    	this.gums = gums;
    }
    
    
    /**
     * @param hostname
     * @return
     */
    public String generateOsgUserVoMap(String hostname) {
        Configuration conf = gums.getConfiguration();
        HostToGroupMapping hostGroup = hostToGroupMapping(conf, hostname);
        if (hostGroup == null)
            throw new RuntimeException("The host '" + hostname + "' is not defined in any group.");
        StringBuffer osgMapBuffer = new StringBuffer("");
        Iterator iter = hostGroup.getGroupToAccountMappings().iterator();
        
        osgMapBuffer.append("#User-VO map\n");
        osgMapBuffer.append("# #comment line, format of each regular line line: account VO\n");
        osgMapBuffer.append("# Next 2 lines with VO names, same order, all lowercase, with case (lines starting with #voi, #VOc)\n");
        
        String voi = "#voi";
        String voc = "#VOc";
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
        
        iter = hostGroup.getGroupToAccountMappings().iterator();
        
        List accountsInMap = new ArrayList();
        int unknownCount = 1;
        
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
		            /*if (members.size()==0) {
	                    Iterator accountMapperIt = accountMappers.iterator();
	                    while (accountMapperIt.hasNext()) {
	                    	AccountMapper accountMapper = (AccountMapper) conf.getAccountMapper( (String)accountMapperIt.next() );
	                    	String account = null;
	                    	if (accountMapper instanceof GroupAccountMapper)
	                    		account = ((GroupAccountMapper)accountMapper).getAccountName();
	                    	else if (accountMapper instanceof AccountPoolMapper)
			                    account = ((AccountPoolMapper)accountMapper).getAccountPool();
	                    	if (account == null)
	                    		account = "unknown" + Integer.toString(unknownCount++);
	                    	if (!accountsInMap.contains(account)) {
		                    	osgMapBuffer.append(account);
			                    osgMapBuffer.append(' ');
			                    osgMapBuffer.append(gMap.getAccountingVoSubgroup());
			                    osgMapBuffer.append("\n");
			                    accountsInMap.add(account);
	                    	}
	                    }
		            }*/
		            Collections.sort(members, retrieveUserComparatorByDN());
		            Iterator memberIter = members.iterator();
		            while (memberIter.hasNext()) {
		                GridUser user = (GridUser) memberIter.next();
	                    Iterator accountMapperIt = accountMappers.iterator();
	                    while (accountMapperIt.hasNext()) {
	                    	AccountMapper accountMapper = (AccountMapper) conf.getAccountMapper( (String)accountMapperIt.next() );
			                String account = accountMapper.mapUser(user, false);
			                if ((account != null) && !accountsInMap.contains(account)) {
			                	osgMapBuffer.append(account);
			                	osgMapBuffer.append(' ');
			                	osgMapBuffer.append(gMap.getAccountingVoSubgroup());
			                	osgMapBuffer.append("\n");
			                    accountsInMap.add(account);
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
    public String generateFqanMapfile(String hostname) {
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
    public String generateGridMapfile(String hostname, boolean createNewMappings, boolean includeFQAN, boolean includeEmail) {
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
    public String map(String hostname, GridUser user) {
        String value;
        value = mapImpl(hostname, user);
        return value;
    }
    
    /**
     * Map a local account to a list of grid identity
     * 
     * @param accountName
     * @return
     */
    public String mapAccount(String accountName) {
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
        
        StringBuffer output = new StringBuffer();
        Iterator it = dns.iterator();
        while(it.hasNext()) {
        	output.append( (String)it.next() );
        	output.append("\n");
        }
        
        return output.toString();
    }
    
    /**
     * Scans the configuration and calls updateMembers() on all the groups,
     * updating the local database.
     */
    public void updateGroups() {
        updateGroupsImpl();
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
    
    private String generateGridMapfile(HostToGroupMapping hToGMapping, boolean createNewMappings, boolean includeFQAN, boolean includeEmail) {
    	Configuration conf = hToGMapping.getConfiguration();
        Iterator iter = hToGMapping.getGroupToAccountMappings().iterator();
        TreeSet usersInMap = new TreeSet();
        
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
	                    String fqan = user.getVoFQAN()!=null?user.getVoFQAN().toString():"";
	                    String email = user.getEmail()!=null?user.getEmail().toString():"";
		                if ( !usersInMap.contains(user.getCertificateDN() + fqan) ) {
		                	Collection accountMappers = gMap.getAccountMappers();
		                    Iterator accountMappersIt = accountMappers.iterator();
		                    while (accountMappersIt.hasNext()) {
		                    	AccountMapper accountMapper = (AccountMapper) conf.getAccountMapper( (String)accountMappersIt.next() );
			                	String account = accountMapper.mapUser(user, true);
			                	if (account != null) {
			                        gridMapfileBuffer.append('"');
			                        gridMapfileBuffer.append( user.getCertificateDN() );
			                        gridMapfileBuffer.append('"' );
			                        gridMapfileBuffer.append(" \"");
			                        gridMapfileBuffer.append( fqan );
			                        gridMapfileBuffer.append('"');         	
			                        gridMapfileBuffer.append(' ');
			                        gridMapfileBuffer.append( account );
			                        if (includeEmail) {
				                        gridMapfileBuffer.append(' ');
				                        gridMapfileBuffer.append( email );
			                        }
			                        gridMapfileBuffer.append("\n");
		                        	usersInMap.add( user.getCertificateDN() + fqan );
		                        	break;
			                    } else {
			                        resourceAdminLog.warn("User " + user + " from group " + gMap.getUserGroups() + " can't be mapped.");
			                    }
		                    }
		            	}
		            }
	            }
	            else {
		            while (memberIter.hasNext()) {
		                GridUser user = (GridUser) memberIter.next();
		                String email = user.getEmail()!=null?user.getEmail().toString():"";
		                if (!usersInMap.contains(user.getCertificateDN()) && userGroup.isInGroup(new GridUser(user.getCertificateDN(), null))) {
		                	Collection accountMappers = gMap.getAccountMappers();
		                    Iterator accountMappersIt = accountMappers.iterator();
		                    while (accountMappersIt.hasNext()) {
		                    	AccountMapper accountMapper = (AccountMapper) conf.getAccountMapper( (String)accountMappersIt.next() );
			                	String account = accountMapper.mapUser(user, true);
			                    if (account != null) {
			                        gridMapfileBuffer.append('"');
			                        gridMapfileBuffer.append( user.getCertificateDN() );
			                        gridMapfileBuffer.append('"' );
			                        gridMapfileBuffer.append(' ');
			                        gridMapfileBuffer.append( account );
			                        if (includeEmail && email!=null) {
				                        gridMapfileBuffer.append(' ');
				                        gridMapfileBuffer.append( email );
			                        }
			                        gridMapfileBuffer.append("\n");
		                        	usersInMap.add(user.getCertificateDN());
		                        	break;
			                    } else {
			                        resourceAdminLog.warn("User " + user + " from group " + gMap.getUserGroups() + " can't be mapped.");
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
 
    private String generateFqanMapfileImpl(String hostname) {
        Configuration conf = gums.getConfiguration();
        HostToGroupMapping host2GroupMapper = hostToGroupMapping(conf, hostname);
        if (host2GroupMapper == null) return null;
        String mapfile = generateFqanMapfile(host2GroupMapper);
        return mapfile;
    }
    
    private String generateGridMapfileImpl(String hostname, boolean createNewMappings, boolean includeFqan, boolean includeEmail) {
        Configuration conf = gums.getConfiguration();
        HostToGroupMapping host2GroupMapper = hostToGroupMapping(conf, hostname);
        if (host2GroupMapper == null) return null;
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

    private String mapImpl(String hostname, GridUser user) {
        Configuration conf = gums.getConfiguration();
        HostToGroupMapping hostToGroupMapping = hostToGroupMapping(conf, hostname);
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
                        String localUser = accountMapper.mapUser(user, true);
                        if (localUser != null) {
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
                return user1.compareDn(user2);
            }
        };
    }
    
    private void updateGroupsImpl() {
        boolean success = true;
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
