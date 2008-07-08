/*
 * GUMSAPIImpl.java
 *
 * Created on November 1, 2004, 12:18 PM
 */

package gov.bnl.gums.admin;

import gov.bnl.gums.*;
import gov.bnl.gums.account.AccountMapper;
import gov.bnl.gums.account.AccountPoolMapper;
import gov.bnl.gums.account.ManualAccountMapper;
import gov.bnl.gums.configuration.Configuration;
import gov.bnl.gums.configuration.FileConfigurationStore;
import gov.bnl.gums.configuration.Version;
import gov.bnl.gums.userGroup.ManualUserGroup;
import gov.bnl.gums.userGroup.UserGroup;
import gov.bnl.gums.hostToGroup.HostToGroupMapping;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.digester.Digester;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import gov.bnl.gums.db.AccountPoolMapperDB;
import gov.bnl.gums.db.ManualAccountMapperDB;
import gov.bnl.gums.db.ManualUserGroupDB;

import gov.bnl.gums.persistence.PersistenceFactory;

/**
 * GUMSAPI implementation
 *
 * @author Gabriele Carcassi, Jay Packard
 */
public class GUMSAPIImpl implements GUMSAPI {
	static private GUMS gums;
	private Log log = LogFactory.getLog(GUMSAPI.class);
	private Log gumsResourceAdminLog = LogFactory.getLog(GUMS.resourceAdminLog);
	private Log siteLog = LogFactory.getLog(GUMS.siteAdminLog);
	private boolean isInWeb = false;
	private String version = null;

	{
		try {
			Class.forName("javax.servlet.Filter");
			isInWeb = true;
		} catch (ClassNotFoundException e) {
			isInWeb = false;
		}
	}

	public void addAccountRange2(String accountPoolMapperName, String range) {
		if (hasWriteAccess(currentUser())) {
			String firstAccount = range.substring(0, range.indexOf('-'));
			String lastAccountN = range.substring(range.indexOf('-') + 1);
			String firstAccountN = firstAccount.substring(firstAccount.length() - lastAccountN.length());
			String accountBase = firstAccount.substring(0, firstAccount.length() - lastAccountN.length());
			int nFirstAccount = Integer.parseInt(firstAccountN);
			int nLastAccount = Integer.parseInt(lastAccountN);

			StringBuffer last = new StringBuffer(firstAccount);
			String nLastAccountString = Integer.toString(nLastAccount);
			last.replace(firstAccount.length() - nLastAccountString.length(), firstAccount.length(), nLastAccountString);

			StringBuffer buf = new StringBuffer(firstAccount);
			int len = firstAccount.length();
			Map reverseMap = getAccountPoolMapperDB(accountPoolMapperName).retrieveReverseAccountMap();
			for (int account = nFirstAccount; account <= nLastAccount; account++) {
				String nAccount = Integer.toString(account);
				buf.replace(len - nAccount.length(), len, nAccount);
				if (reverseMap.get(buf.toString())!=null)
					throw new RuntimeException("One or more accounts already exist. None were added.");
			}
			buf = new StringBuffer(firstAccount);
			for (int account = nFirstAccount; account <= nLastAccount; account++) {
				String nAccount = Integer.toString(account);
				buf.replace(len - nAccount.length(), len, nAccount);
				getAccountPoolMapperDB(accountPoolMapperName).addAccount(buf.toString());
			}
		}
		else {
			gumsResourceAdminLog.info(logUserAccess() + "Failed to add account range because user doesn't have administrative access");
			siteLog.info(logUserAccess() + "Failed to add account range because user doesn't have administrative access");
			throw new AuthorizationDeniedException();
		}
	}

	public void backupConfiguration() {
		if (hasWriteAccess(currentUser()))
			gums().setConfiguration(gums().getConfiguration(), true);
		else {
			gumsResourceAdminLog.info(logUserAccess() + "Failed to backup configuration because user doesn't have administrative access");
			siteLog.info(logUserAccess() + "Failed to backup configuration because user doesn't have administrative access");
			throw new AuthorizationDeniedException();
		}
	}

	public void deleteBackupConfiguration(String dateStr) {
		if (hasWriteAccess(currentUser()))
			gums().deleteBackupConfiguration(dateStr);
		else {
			gumsResourceAdminLog.info(logUserAccess() + "Failed to delete configuration because user doesn't have administrative access");
			siteLog.info(logUserAccess() + "Failed to delete configuration because user doesn't have administrative access");
			throw new AuthorizationDeniedException();
		}   	
	}

	public String generateFqanMapfile(String hostname) {
		try {
			if (hasReadAllAccess(currentUser(), hostname)) {
				String map = gums().getResourceManager().generateFqanMapfile(hostname);
				gumsResourceAdminLog.info(logUserAccess() + "Generated fqan mapfile for host '" + hostname + "': " + map);
				return map;
			} else {
				throw new AuthorizationDeniedException();
			}
		} catch (AuthorizationDeniedException e) {
			gumsResourceAdminLog.info(logUserAccess() + "Failed to generate fqan mapfile for host '" + hostname + "' - " + e.getMessage());
			siteLog.info(logUserAccess() + "Unauthorized access to generate fqan mapfile for host '" + hostname + "'");
			throw e;
		} catch (RuntimeException e) {
			gumsResourceAdminLog.error(logUserAccess() + "Failed to generate fqanmapfile for host '" + hostname + "' - " + e.getMessage());
			throw e;
		}   
	}        

	public String generateGrid3UserVoMap(String hostname) {
		return generateOsgUserVoMap(hostname);
	}

	public String generateGridMapfile(String hostname) {
		try {
			if (hasReadAllAccess(currentUser(), hostname)) {
				String map = gums().getResourceManager().generateGridMapfile(hostname, false);
				gumsResourceAdminLog.info(logUserAccess() + "Generated grid mapfile for host '" + hostname + "': " + map);
				return map;
			} else {
				throw new AuthorizationDeniedException();
			}
		} catch (AuthorizationDeniedException e) {
			gumsResourceAdminLog.info(logUserAccess() + "Failed to generate grid mapfile for host '" + hostname + "' - " + e.getMessage());
			siteLog.info(logUserAccess() + "Unauthorized access to generate grid mapfile for host '" + hostname + "'");
			throw e;
		} catch (RuntimeException e) {
			gumsResourceAdminLog.error(logUserAccess() + "Failed to generate grid mapfile for host '" + hostname + "' - " + e.getMessage());
			throw e;
		}   
	}

	public String generateOsgUserVoMap(String hostname) {
		try {
			if (hasReadAllAccess(currentUser(), hostname)) {
				String map = gums().getResourceManager().generateOsgUserVoMap(hostname);
				gumsResourceAdminLog.info(logUserAccess() + "Generated grid3 vo-user map for host '" + hostname + "': " + map);
				return map;
			} else {
				throw new AuthorizationDeniedException();
			}
		} catch (AuthorizationDeniedException e) {
			gumsResourceAdminLog.info(logUserAccess() + "Failed to generate grid3 vo-user map for host '" + hostname + "' - " + e.getMessage());
			siteLog.info(logUserAccess() + "Unauthorized access to generate grid3 vo-user map for host '" + hostname + "'");
			throw e;
		} catch (RuntimeException e) {
			gumsResourceAdminLog.error(logUserAccess() + "Failed to generate grid3 vo-user map for host '" + hostname + "' - " + e.getMessage());
			throw e;
		}
	}

	public String generateVoGridMapfile(String hostname) {
		try {
			if (hasReadAllAccess(currentUser(), hostname)) {
				String map = gums().getResourceManager().generateGridMapfile(hostname, true);
				gumsResourceAdminLog.info(logUserAccess() + "Generated mapfile for host '" + hostname + "': " + map);
				return map;
			} else {
				throw new AuthorizationDeniedException();
			}
		} catch (AuthorizationDeniedException e) {
			gumsResourceAdminLog.info(logUserAccess() + "Failed to generate mapfile for host '" + hostname + "' - " + e.getMessage());
			siteLog.info(logUserAccess() + "Unauthorized access to generate mapfile for host '" + hostname + "'");
			throw e;
		} catch (RuntimeException e) {
			gumsResourceAdminLog.error(logUserAccess() + "Failed to generate mapfile for host '" + hostname + "' - " + e.getMessage());
			throw e;
		}   	
	}

	public Collection getBackupConfigDates() {
		Collection backupConfigDates = null;
		if (hasReadAllAccess(currentUser(), null))
			backupConfigDates = gums().getBackupConfigDates();
		else {
			gumsResourceAdminLog.info(logUserAccess() + "Failed to get backup config dates because user doesn't have administrative access");
			siteLog.info(logUserAccess() + "Failed to get backup config dates because user doesn't have administrative access");
			throw new AuthorizationDeniedException();
		}    	
		return backupConfigDates;
	}

	public Configuration getConfiguration() {
		if (hasReadAllAccess(currentUser(), null) && isInWeb)
			return gums().getConfiguration();
		else {
			gumsResourceAdminLog.info(logUserAccess() + "Failed to get configuration because user doesn't have administrative access");
			siteLog.info(logUserAccess() + "Failed to get configuration because user doesn't have administrative access");
			throw new AuthorizationDeniedException();
		}
	}

	public String getPoolAccountAssignments(String accountPoolMapperName) {
		if (hasReadAllAccess(currentUser(), null)) {
			if (gums().getConfiguration().getAccountMapper(accountPoolMapperName) instanceof AccountPoolMapper)
				return ((AccountPoolMapper)gums().getConfiguration().getAccountMapper(accountPoolMapperName)).getAssignments();
			else
				return "";
		}
		else {
			gumsResourceAdminLog.info(logUserAccess() + "Failed to get pool account assignments because user doesn't have administrative access");
			siteLog.info(logUserAccess() + "Failed to get pool account assignments because user doesn't have administrative access");
			throw new AuthorizationDeniedException();
		}    	
	}

	public String getVersion() {
		if (version==null) {
			String pomFile = CertCache.getMetaDir()+"/maven/gums/gums-service/pom.xml";
			Digester digester = new Digester();
			digester.addObjectCreate("project/version", Version.class);
			digester.addCallMethod("project/version","setVersion",0);
			log.trace("Loading GUMS version from pom file '" + pomFile + "'");
			Version versionCls = null;
			try {
				versionCls = (Version)digester.parse("file://"+pomFile);
			} catch (Exception e) {
				gumsResourceAdminLog.error("Cannot get version from "+pomFile);
				log.error("Cannot get version from "+pomFile, e);
			}
			if (versionCls == null)
				return "?";
			else
				log.trace("GUMS version " + versionCls.getVersion() );
			version = versionCls.getVersion();
		}
		return version;
	}

	public void manualGroupAdd2(String manualUserGroupName, String userDN) {
		try {
			if (hasWriteAccess(currentUser())) {
				getManualUserGroupDB(manualUserGroupName).addMember(new GridUser(userDN, null));
				gumsResourceAdminLog.info(logUserAccess() + "Added to group '" + manualUserGroupName + "'  user '" + userDN + "'");
				siteLog.info(logUserAccess() + "Added to group '" + manualUserGroupName + "'  user '" + userDN + "'");
			} else {
				throw new AuthorizationDeniedException();
			}
		} catch (AuthorizationDeniedException e) {
			gumsResourceAdminLog.info(logUserAccess() + "Failed to add to group '" + manualUserGroupName + "' user '" + userDN + "' - " + e.getMessage());
			siteLog.info(logUserAccess() + "Unauthorized access to add to group '" + manualUserGroupName + "' user '" + userDN + "'");
			throw e;
		} catch (RuntimeException e) {
			gumsResourceAdminLog.error(logUserAccess() + "Failed to add to group '" + manualUserGroupName + "' user '" + userDN + "' - " + e.getMessage());
			siteLog.info(logUserAccess() + "Failed to add to group '" + manualUserGroupName + "' user '" + userDN + "' - " + e.getMessage());
			throw e;
		}
	}

	public void manualGroupRemove2(String manualUserGroupName, String userDN) {
		try {
			if (hasWriteAccess(currentUser())) {
				getManualUserGroupDB(manualUserGroupName).removeMember(new GridUser(userDN, null));
				gumsResourceAdminLog.info(logUserAccess() + "Removed from group '" + manualUserGroupName + "'  user '" + userDN + "'");
				siteLog.info(logUserAccess() + "Removed from group '" + manualUserGroupName + "'  user '" + userDN + "'");
			} else {
				throw new AuthorizationDeniedException();
			}
		} catch (AuthorizationDeniedException e) {
			gumsResourceAdminLog.info(logUserAccess() + "Failed to remove from group '" + manualUserGroupName + "' user '" + userDN + "' - " + e.getMessage());
			siteLog.info(logUserAccess() + "Unauthorized access to remove from group '" + manualUserGroupName + "' user '" + userDN + "'");
			throw e;
		} catch (RuntimeException e) {
			gumsResourceAdminLog.error(logUserAccess() + "Failed to remove from group '" + manualUserGroupName + "' user '" + userDN + "' - " + e.getMessage());
			siteLog.info(logUserAccess() + "Failed to remove from group '" + manualUserGroupName + "' user '" + userDN + "' - " + e.getMessage());
			throw e;
		}
	}

	public void manualMappingAdd2(String manualAccountMapperName, String userDN, String account) {
		try {
			if (hasWriteAccess(currentUser())) {
				getManualAccountMapperDB(manualAccountMapperName).createMapping(userDN, account);
				gumsResourceAdminLog.info(logUserAccess() + "Added mapping to account mapper '" + manualAccountMapperName + "' for user '" + userDN + "' to account '" + account + "'");
				siteLog.info(logUserAccess() + "Added mapping to account mapper '" + manualAccountMapperName + "' for user '" + userDN + "' to account '" + account + "'");
			} else {
				throw new AuthorizationDeniedException();
			}
		} catch (AuthorizationDeniedException e) {
			gumsResourceAdminLog.info(logUserAccess() + "Failed to add mapping to account mapper '" + manualAccountMapperName + "' for user '" + userDN + "' to account '" + account + "' - " + e.getMessage());
			siteLog.info(logUserAccess() + "Unauthorized access to add mapping to account mapper '" + manualAccountMapperName + "' for user '" + userDN + "' to account '" + account + "'");
			throw e;
		} catch (RuntimeException e) {
			gumsResourceAdminLog.error(logUserAccess() + "Failed to add mapping to account mapper '" + manualAccountMapperName + "' for user '" + userDN + "' to account '" + account + "' - " + e.getMessage());
			siteLog.info(logUserAccess() + "Failed to add mapping to persistence account mapper '" + manualAccountMapperName + "' for user '" + userDN + "' to account '" + account + "' - " + e.getMessage());
			throw e;
		}
	}

	public void manualMappingRemove2(String manualAccountMapperName, String userDN) {
		try {
			if (hasWriteAccess(currentUser())) {
				getManualAccountMapperDB(manualAccountMapperName).removeMapping(userDN);
				gumsResourceAdminLog.info(logUserAccess() + "Removed mapping from account mapper '" + manualAccountMapperName + "' for user '" + userDN + "'");
				siteLog.info(logUserAccess() + "Removed mapping from account mapper '" + manualAccountMapperName + "' for user '" + userDN + "'");
			} else {
				throw new AuthorizationDeniedException();
			}
		} catch (AuthorizationDeniedException e) {
			gumsResourceAdminLog.info(logUserAccess() + "Failed to remove mapping from account mapper '" + manualAccountMapperName + "' for user '" + userDN + "' - " + e.getMessage());
			siteLog.info(logUserAccess() + "Unauthorized access to remove mapping from account mapper '" + manualAccountMapperName + "' for user '" + userDN + "'");
			throw e;
		} catch (RuntimeException e) {
			gumsResourceAdminLog.error(logUserAccess() + "Failed to remove mapping from account mapper '" + manualAccountMapperName + "' for user '" + userDN + "' - " + e.getMessage());
			siteLog.info(logUserAccess() + "Failed to remove mapping from persistence account mapper '" + manualAccountMapperName + "' for user '" + userDN + "' - " + e.getMessage());
			throw e;
		}
	}

	public String mapAccount(String accountName) {
		try {
			if (hasReadAllAccess(currentUser(), null)) {
				String DNs = gums().getResourceManager().mapAccount(accountName);
				gumsResourceAdminLog.info(logUserAccess() + "Mapped the account '" + accountName + "' to '" + DNs + "'");
				return DNs;
			} else {
				throw new AuthorizationDeniedException();
			}
		} catch (AuthorizationDeniedException e) {
			gumsResourceAdminLog.info(logUserAccess() + "Failed to map the account '" + accountName + "' - " + e.getMessage());
			siteLog.info(logUserAccess() + "Unauthorized access to map the account '" + accountName + "'");
			throw e;
		} catch (RuntimeException e) {
			gumsResourceAdminLog.error(logUserAccess() + "Failed to map the account '" + accountName + "' - "  + e.getMessage());
			throw e;
		}   	
	}

	public String mapUser(String hostname, String userDN, String fqan) {
		try {
			if ( (hasReadSelfAccess(currentUser()) && currentUser().compareDn(userDN)==0) || hasReadAllAccess(currentUser(), hostname)) {
				String account = gums().getResourceManager().map(hostname, new GridUser(userDN, fqan));
				gumsResourceAdminLog.info(logUserAccess() + "Mapped on host '" + hostname + "' the user '" + userDN + "' / '" + fqan + "' to '" + account + "'");
				return account;
			} else {
				throw new AuthorizationDeniedException();
			}
		} catch (AuthorizationDeniedException e) {
			gumsResourceAdminLog.info(logUserAccess() + "Failed to map on host '" + hostname + "' the user '" + userDN + "' / '" + fqan + "' - " + e.getMessage());
			siteLog.info(logUserAccess() + "Unauthorized access to map on host '" + hostname + "' the user '" + userDN + "' / '" + fqan + "'");
			throw e;
		} catch (RuntimeException e) {
			gumsResourceAdminLog.error(logUserAccess() + "Failed to map on host '" + hostname + "' the user '" + userDN + "' / '" + fqan + "' - " + e.getMessage());
			throw e;
		}
	}

	public void removeAccountRange(String accountPoolMapperName, String range) {
		if (hasWriteAccess(currentUser())) {
			String firstAccount = range.substring(0, range.indexOf('-'));
			String lastAccountN = range.substring(range.indexOf('-') + 1);
			String firstAccountN = firstAccount.substring(firstAccount.length() - lastAccountN.length());
			String accountBase = firstAccount.substring(0, firstAccount.length() - lastAccountN.length());
			int nFirstAccount = Integer.parseInt(firstAccountN);
			int nLastAccount = Integer.parseInt(lastAccountN);

			StringBuffer last = new StringBuffer(firstAccount);
			String nLastAccountString = Integer.toString(nLastAccount);
			last.replace(firstAccount.length() - nLastAccountString.length(), firstAccount.length(), nLastAccountString);

			StringBuffer buf = new StringBuffer(firstAccount);
			int len = firstAccount.length();
			for (int account = nFirstAccount; account <= nLastAccount; account++) {
				String nAccount = Integer.toString(account);
				buf.replace(len - nAccount.length(), len, nAccount);
				getAccountPoolMapperDB(accountPoolMapperName).removeAccount(buf.toString());
			}
		}
		else {
			gumsResourceAdminLog.info(logUserAccess() + "Failed to remove account range because user doesn't have administrative access");
			siteLog.info(logUserAccess() + "Failed to remove account range because user doesn't have administrative access");
			throw new AuthorizationDeniedException();
		}
	}

	public void restoreConfiguration(String dateStr) throws Exception {
		if (hasWriteAccess(currentUser()))
			gums().restoreConfiguration(dateStr);
		else {
			gumsResourceAdminLog.info(logUserAccess() + "Failed to set configuration because user doesn't have administrative access");
			siteLog.info(logUserAccess() + "Failed to set configuration because user doesn't have administrative access");
			throw new AuthorizationDeniedException();
		}  	
	}

	public void setConfiguration(Configuration configuration) throws Exception {
		if (hasWriteAccess(currentUser()))
			gums().setConfiguration(configuration, false);
		else {
			gumsResourceAdminLog.info(logUserAccess() + "Failed to set configuration because user doesn't have administrative access");
			siteLog.info(logUserAccess() + "Failed to set configuration because user doesn't have administrative access");
			throw new AuthorizationDeniedException();
		}
	}

	public void unassignAccountRange(String accountPoolMapperName, String range) {
		if (hasWriteAccess(currentUser())) {
			String firstAccount = range.substring(0, range.indexOf('-'));
			String lastAccountN = range.substring(range.indexOf('-') + 1);
			String firstAccountN = firstAccount.substring(firstAccount.length() - lastAccountN.length());
			String accountBase = firstAccount.substring(0, firstAccount.length() - lastAccountN.length());
			int nFirstAccount = Integer.parseInt(firstAccountN);
			int nLastAccount = Integer.parseInt(lastAccountN);

			StringBuffer last = new StringBuffer(firstAccount);
			String nLastAccountString = Integer.toString(nLastAccount);
			last.replace(firstAccount.length() - nLastAccountString.length(), firstAccount.length(), nLastAccountString);

			StringBuffer buf = new StringBuffer(firstAccount);
			int len = firstAccount.length();
			for (int account = nFirstAccount; account <= nLastAccount; account++) {
				String nAccount = Integer.toString(account);
				buf.replace(len - nAccount.length(), len, nAccount);
				getAccountPoolMapperDB(accountPoolMapperName).unassignAccount(buf.toString());
			}

			gumsResourceAdminLog.info(logUserAccess() + "Unassigned accounts from account mapper '" + accountPoolMapperName + "'");
			siteLog.info(logUserAccess() + "Unassigned accounts from account mapper '" + accountPoolMapperName + "'");
		}
		else {
			gumsResourceAdminLog.info(logUserAccess() + "Failed to unassign accounts because user doesn't have administrative access");
			siteLog.info(logUserAccess() + "Failed to unassign accounts because user doesn't have administrative access");
			throw new AuthorizationDeniedException();
		}
	}

	public void updateGroups() {
		try {
			if (hasWriteAccess(currentUser())) {
				gums().getResourceManager().updateGroups();
				gumsResourceAdminLog.info(logUserAccess() + "Groups updated");
				siteLog.info(logUserAccess() + "Groups updated");
			} else {
				throw new AuthorizationDeniedException();
			}
		} catch (AuthorizationDeniedException e) {
			gumsResourceAdminLog.info(logUserAccess() + "Failed to update all groups - " + e.getMessage());
			siteLog.info(logUserAccess() + "Unauthorized access to update all groups");
			throw e;
		} catch (RuntimeException e) {
			gumsResourceAdminLog.error(logUserAccess() + "Failed to update all groups - " + e.getMessage());
			siteLog.info(logUserAccess() + "Failed to update all groups - " + e.getMessage());
			throw e;
		}
	}

	public String getCurrentDn() {
		try {
			return currentUser().getCertificateDN();
		}
		catch(Exception e) {
			return "UNKNOWN";
		}
	}

	// Depricated

	public void manualGroupAdd(String persistanceFactory, String group, String userDN) {
		try {
			if (hasWriteAccess(currentUser())) {
				Collection userGroups = gums().getConfiguration().getUserGroups().values();
				Iterator it = userGroups.iterator();
				boolean found = false;
				while (it.hasNext()) {
					UserGroup userGroup = (UserGroup)it.next();
					if (userGroup instanceof ManualUserGroup) {
						if (((ManualUserGroup)userGroup).getName().equals(group))
							found = true;
					}
				}
				if (!found)
					throw new RuntimeException("No manual user group named " + group);

				PersistenceFactory factory = (PersistenceFactory) gums().getConfiguration().getPersistenceFactories().get(persistanceFactory);
				ManualUserGroupDB db = factory.retrieveManualUserGroupDB(group);
				db.addMember(new GridUser(userDN, null));
				gumsResourceAdminLog.info(logUserAccess() + "Added to persistence '" + persistanceFactory + "' group '" + group + "'  user '" + userDN + "'");
				siteLog.info(logUserAccess() + "Added to persistence '" + persistanceFactory + "' group '" + group + "'  user '" + userDN + "'");
			} else {
				throw new AuthorizationDeniedException();
			}
		} catch (AuthorizationDeniedException e) {
			gumsResourceAdminLog.info(logUserAccess() + "Failed to add to persistence '" + persistanceFactory + "' group '" + group + "' user '" + userDN + "' - " + e.getMessage());
			siteLog.info(logUserAccess() + "Unauthorized access to add to persistence '" + persistanceFactory + "' group '" + group + "' user '" + userDN + "'");
		} catch (RuntimeException e) {
			gumsResourceAdminLog.error(logUserAccess() + "Failed to add to persistence '" + persistanceFactory + "' group '" + group + "' user '" + userDN + "' - " + e.getMessage());
			siteLog.info(logUserAccess() + "Failed to add to persistence '" + persistanceFactory + "' group '" + group + "' user '" + userDN + "' - " + e.getMessage());
			throw e;
		}
	}

	public void manualGroupRemove(String persistanceFactory, String group, String userDN) {
		try {
			if (hasWriteAccess(currentUser())) {
				Collection userGroups = gums().getConfiguration().getUserGroups().values();
				Iterator it = userGroups.iterator();
				boolean found = false;
				while (it.hasNext()) {
					UserGroup userGroup = (UserGroup)it.next();
					if (userGroup instanceof ManualUserGroup) {
						if (((ManualUserGroup)userGroup).getName().equals(group))
							found = true;
					}
				}
				if (!found)
					throw new RuntimeException("No manual user group named " + group);

				PersistenceFactory factory = (PersistenceFactory) gums().getConfiguration().getPersistenceFactories().get(persistanceFactory);
				ManualUserGroupDB db = factory.retrieveManualUserGroupDB(group);
				db.removeMember(new GridUser(userDN, null));
				gumsResourceAdminLog.info(logUserAccess() + "Removed from persistence '" + persistanceFactory + "' group '" + group + "'  user '" + userDN + "'");
				siteLog.info(logUserAccess() + "Removed from persistence '" + persistanceFactory + "' group '" + group + "'  user '" + userDN + "'");
			} else {
				throw new AuthorizationDeniedException();
			}
		} catch (AuthorizationDeniedException e) {
			gumsResourceAdminLog.info(logUserAccess() + "Failed to remove from persistence '" + persistanceFactory + "' group '" + group + "' user '" + userDN + "' - " + e.getMessage());
			siteLog.info(logUserAccess() + "Unauthorized access to remove from persistence '" + persistanceFactory + "' group '" + group + "' user '" + userDN + "'");
			throw e;
		} catch (RuntimeException e) {
			gumsResourceAdminLog.error(logUserAccess() + "Failed to remove from persistence '" + persistanceFactory + "' group '" + group + "' user '" + userDN + "' - " + e.getMessage());
			siteLog.info(logUserAccess() + "Failed to remove from persistence '" + persistanceFactory + "' group '" + group + "' user '" + userDN + "' - " + e.getMessage());
			throw e;
		}
	}

	public void manualMappingAdd(String persistanceFactory, String group, String userDN, String account) {
		try {
			if (hasWriteAccess(currentUser())) {
				Collection accountMappers = gums().getConfiguration().getAccountMappers().values();
				Iterator it = accountMappers.iterator();
				boolean found = false;
				while (it.hasNext()) {
					AccountMapper accountMapper = (AccountMapper)it.next();
					if (accountMapper instanceof ManualAccountMapper) {
						if (((ManualAccountMapper)accountMapper).getName().equals(group))
							found = true;
					}
				}
				if (!found)
					throw new RuntimeException("No manual account mapper named " + group);

				PersistenceFactory factory = (PersistenceFactory) gums().getConfiguration().getPersistenceFactories().get(persistanceFactory);
				ManualAccountMapperDB db = factory.retrieveManualAccountMapperDB(group);
				db.createMapping(userDN, account);
				gumsResourceAdminLog.info(logUserAccess() + "Added mapping to persistence '" + persistanceFactory + "' group '" + group + "' for user '" + userDN + "' to account '" + account + "'");
				siteLog.info(logUserAccess() + "Added mapping to persistence '" + persistanceFactory + "' group '" + group + "' for user '" + userDN + "' to account '" + account + "'");
			} else {
				throw new AuthorizationDeniedException();
			}
		} catch (AuthorizationDeniedException e) {
			gumsResourceAdminLog.info(logUserAccess() + "Failed to add mapping to persistence '" + persistanceFactory + "' group '" + group + "' for user '" + userDN + "' to account '" + account + "' - " + e.getMessage());
			siteLog.info(logUserAccess() + "Unauthorized access to add mapping to persistence '" + persistanceFactory + "' group '" + group + "' for user '" + userDN + "' to account '" + account + "'");
			throw e;
		} catch (RuntimeException e) {
			gumsResourceAdminLog.error(logUserAccess() + "Failed to add mapping to persistence '" + persistanceFactory + "' group '" + group + "' for user '" + userDN + "' to account '" + account + "' - " + e.getMessage());
			siteLog.info(logUserAccess() + "Failed to add mapping to persistence '" + persistanceFactory + "' group '" + group + "' for user '" + userDN + "' to account '" + account + "' - " + e.getMessage());
			throw e;
		}
	}

	public void manualMappingRemove(String persistanceFactory, String group, String userDN) {
		try {
			if (hasWriteAccess(currentUser())) {
				Collection accountMappers = gums().getConfiguration().getAccountMappers().values();
				Iterator it = accountMappers.iterator();
				boolean found = false;
				while (it.hasNext()) {
					AccountMapper accountMapper = (AccountMapper)it.next();
					if (accountMapper instanceof ManualAccountMapper) {
						if (((ManualAccountMapper)accountMapper).getName().equals(group))
							found = true;
					}
				}
				if (!found)
					throw new RuntimeException("No manual account mapper named " + group);

				PersistenceFactory factory = (PersistenceFactory) gums().getConfiguration().getPersistenceFactories().get(persistanceFactory);
				ManualAccountMapperDB db = factory.retrieveManualAccountMapperDB(group);
				db.removeMapping(userDN);
				gumsResourceAdminLog.info(logUserAccess() + "Removed mapping from persistence '" + persistanceFactory + "' group '" + group + "' for user '" + userDN + "'");
				siteLog.info(logUserAccess() + "Removed mapping from persistence '" + persistanceFactory + "' group '" + group + "' for user '" + userDN + "'");
			} else {
				throw new AuthorizationDeniedException();
			}
		} catch (AuthorizationDeniedException e) {
			gumsResourceAdminLog.info(logUserAccess() + "Failed to remove mapping from persistence '" + persistanceFactory + "' group '" + group + "' for user '" + userDN + "' - " + e.getMessage());
			siteLog.info(logUserAccess() + "Unauthorized access to remove mapping from persistence '" + persistanceFactory + "' group '" + group + "' for user '" + userDN + "'");
			throw e;
		} catch (RuntimeException e) {
			gumsResourceAdminLog.error(logUserAccess() + "Failed to remove mapping from persistence '" + persistanceFactory + "' group '" + group + "' for user '" + userDN + "' - " + e.getMessage());
			siteLog.info(logUserAccess() + "Failed to remove mapping from persistence '" + persistanceFactory + "' group '" + group + "' for user '" + userDN + "' - " + e.getMessage());
			throw e;
		}
	}

	public void poolAddAccount(String persistanceFactory, String group, String username) {
		try {
			if (hasWriteAccess(currentUser())) {
				Collection accountMappers = gums().getConfiguration().getAccountMappers().values();
				Iterator it = accountMappers.iterator();
				boolean found = false;
				while (it.hasNext()) {
					AccountMapper accountMapper = (AccountMapper)it.next();
					if (accountMapper instanceof AccountPoolMapper) {
						if (((AccountPoolMapper)accountMapper).getAccountPool().equals(group))
							found = true;
					}
				}
				if (!found)
					throw new RuntimeException("No pool account mapper with group " + group);

				PersistenceFactory factory = (PersistenceFactory) gums().getConfiguration().getPersistenceFactories().get(persistanceFactory);
				if (factory == null) {
					throw new RuntimeException("PersistenceFactory '" + persistanceFactory + "' does not exist");
				}
				AccountPoolMapperDB db = factory.retrieveAccountPoolMapperDB(group);
				db.addAccount(username);
				gumsResourceAdminLog.info(logUserAccess() + "Added account to pool: persistence '" + persistanceFactory + "' group '" + group + "' username '" + username + "'");
				siteLog.info(logUserAccess() + "Added account to pool: persistence '" + persistanceFactory + "' group '" + group + "' username '" + username + "'");
			} else {
				throw new AuthorizationDeniedException();
			}
		} catch (AuthorizationDeniedException e) {
			gumsResourceAdminLog.info(logUserAccess() + "Failed to add account to pool: persistence '" + persistanceFactory + "' group '" + group + "' username '" + username + "' - " + e.getMessage());
			siteLog.info(logUserAccess() + "Unauthorized access to add account to pool: persistence '" + persistanceFactory + "' group '" + group + "' username '" + username + "'");
			throw e;
		} catch (RuntimeException e) {
			gumsResourceAdminLog.error(logUserAccess() + "Failed to add account to pool: persistence '" + persistanceFactory + "' group '" + group + "' username '" + username + "' - " + e.getMessage());
			siteLog.info(logUserAccess() + "Failed to add account to pool: persistence '" + persistanceFactory + "' group '" + group + "' username '" + username + "' - " + e.getMessage());
			throw e;
		}
	}

	////////////

	private GridUser currentUser() {
		if (!isInWeb) return null;
		String DN = CertCache.getUserDN();
		if (DN != null) {
			return new GridUser(DN, null);
		} else {
			return null;
		}
	}

	private String getAccountPoolMapper(String pool) {
		Collection accountMappers = gums().getConfiguration().getAccountMappers().values();
		Iterator it = accountMappers.iterator();
		while (it.hasNext()) {
			AccountMapper accountMapper = (AccountMapper)it.next();
			if ( accountMapper instanceof AccountPoolMapper && ((AccountPoolMapper)accountMapper).getAccountPoolRoot().equals(pool))
				return accountMapper.getName();
		}
		return null;
	}

	private AccountPoolMapperDB getAccountPoolMapperDB(String accountPoolMapperName) {
		return ((AccountPoolMapper) gums().getConfiguration().getAccountMapper(accountPoolMapperName)).getDB();
	}

	private ManualAccountMapperDB getManualAccountMapperDB(String manualAccountMapperName) {
		return ((ManualAccountMapper) gums().getConfiguration().getAccountMapper(manualAccountMapperName)).getDB();
	}

	private ManualUserGroupDB getManualUserGroupDB(String manualUserGroupName) {
		return ((ManualUserGroup) gums().getConfiguration().getUserGroup(manualUserGroupName)).getDB();
	}    

	private GUMS gums() {
		if (gums == null) {
			FileConfigurationStore confStore = new FileConfigurationStore(CertCache.getConfigDir(), CertCache.getResourceDir(), getVersionNoPatch(), true);
			gums = new GUMS(confStore);
		}
		return gums;
	}

	private boolean hasReadAllAccess(GridUser user, String hostname) {
		if (user == null) return false;
		if (gums().getConfiguration().getReadAllUserGroups() != null) {
			Collection readAllUserGroups = gums().getConfiguration().getReadAllUserGroups();
			Iterator it = readAllUserGroups.iterator();
			while (it.hasNext()) {
				UserGroup userGroup = (UserGroup)it.next();
				if (userGroup.isInGroup(user))
					return true;
			}
		}
		// return true if user certificate (issuer in reality - see CertToolkit) matches host certificate
		if (hostname!=null && user.getCertificateDN().indexOf(hostname) != -1)
			return true;
		// return true if user certificate (issuer in reality - see CertToolkit) matches any of the hostToGroupMappings
		if (hostToGroupMapping(user.getCertificateDN()) != null)
			return true;  
		return false;
	}

	private boolean hasReadSelfAccess(GridUser currentUser) {
		if (currentUser == null) return false;
		if (gums().getConfiguration().getReadSelfUserGroups() == null)
			return false;
		Collection readSelfUserGroups = gums().getConfiguration().getReadSelfUserGroups();
		Iterator it = readSelfUserGroups.iterator();
		while (it.hasNext()) {
			UserGroup userGroup = (UserGroup)it.next();
			if (userGroup.isInGroup(currentUser))
				return true;
		}
		return false;
	}

	private boolean hasWriteAccess(GridUser user) {
		if (user == null) return false;
		if (gums().getConfiguration().getWriteUserGroups() == null)
			return false;
		Collection writeUserGroups = gums().getConfiguration().getWriteUserGroups();
		Iterator it = writeUserGroups.iterator();
		while (it.hasNext()) {
			UserGroup userGroup = (UserGroup)it.next();
			if (userGroup.isInGroup(user)) 
				return true;
		}
		return false;
	}

	private HostToGroupMapping hostToGroupMapping(String hostname) {
		Collection hostToGroupMappers = gums().getConfiguration().getHostToGroupMappings();
		Iterator it = hostToGroupMappers.iterator();
		while (it.hasNext()) {
			HostToGroupMapping hostToGroupMapper = (HostToGroupMapping) it.next();
			if (hostToGroupMapper.isInGroup(hostname)) {
				return hostToGroupMapper;
			}
		}
		return null;
	}

	private String logUserAccess() {
		if (currentUser() == null) {
			return "No AuthN - ";
		} else {
			return currentUser() + " - ";
		}
	}

	private String getVersionNoPatch() {
		String version = getVersion();
		int lastDot = version.lastIndexOf(".");
		if (lastDot!=-1)
			version = version.substring(0, lastDot);
		return version;
	}

}
