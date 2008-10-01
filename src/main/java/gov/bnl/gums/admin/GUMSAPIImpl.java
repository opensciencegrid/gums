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
import gov.bnl.gums.userGroup.ManualUserGroup;
import gov.bnl.gums.userGroup.UserGroup;
import gov.bnl.gums.hostToGroup.HostToGroupMapping;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;

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
	private Logger log = Logger.getLogger(GUMSAPI.class);
	private Logger gumsAdminLog = Logger.getLogger(GUMS.gumsAdminLogName);
	private Logger siteAdminLog = Logger.getLogger(GUMS.siteAdminLogName);
	private boolean isInWeb = false;

	{
		try {
			Class.forName("javax.servlet.Filter");
			isInWeb = true;
		} catch (ClassNotFoundException e) {
			isInWeb = false;
		}
	}

	public void addAccountRange2(String accountPoolMapperName, String range) {
		try {
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
					if (reverseMap.get(buf.toString())!=null) {
						String message = "One or more accounts already exist for '"+accountPoolMapperName+" "+range+"'. None were added.";
						siteAdminLog.error(message);
						throw new RuntimeException(message);
					}
				}
				buf = new StringBuffer(firstAccount);
				for (int account = nFirstAccount; account <= nLastAccount; account++) {
					String nAccount = Integer.toString(account);
					buf.replace(len - nAccount.length(), len, nAccount);
					getAccountPoolMapperDB(accountPoolMapperName).addAccount(buf.toString());
				}
			}
			else {
				String message = logUserAccess() + "Unauthorized access to addAccountRange2 for '"+accountPoolMapperName+" "+range+"'";
				gumsAdminLog.warn(message);
				siteAdminLog.warn(message);
				throw new AuthorizationDeniedException();
			}
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	public void backupConfiguration() {
		try {
			if (hasWriteAccess(currentUser()))
				gums().setConfiguration(gums().getConfiguration(), true);
			else {
				String message = logUserAccess() + "Unauthorized access to backupConfiguration";
				gumsAdminLog.warn(message);
				siteAdminLog.warn(message);
				throw new AuthorizationDeniedException();
			}
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	public void deleteBackupConfiguration(String dateStr) {
		try {
			if (hasWriteAccess(currentUser()))
				gums().deleteBackupConfiguration(dateStr);
			else {
				String message = logUserAccess() + "Unauthorized access to deleteBackupConfiguration for '"+dateStr+"'";
				gumsAdminLog.warn(message);
				siteAdminLog.warn(message);
				throw new AuthorizationDeniedException();
			}  
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}		
	}

	public String generateEmailMapfile(String hostname) {
		try {
			if (hasReadAllAccess(currentUser(), hostname)) {
				String map = gums().getCoreLogic().generateGridMapfile(hostname, false, true, true);
				gumsAdminLog.info(logUserAccess() + "Generated email mapfile");
				return map;
			} else { 
				String message = logUserAccess() + "Unauthorized access to generateEmailMapfile for host '"+hostname+"'";
				gumsAdminLog.warn(message);
				siteAdminLog.warn(message);
				throw new AuthorizationDeniedException();
			}
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
	}
	
	public String generateFqanMapfile(String hostname) {
		try {
			if (hasReadAllAccess(currentUser(), hostname)) {
				String map = gums().getCoreLogic().generateFqanMapfile(hostname);
				gumsAdminLog.info(logUserAccess() + "Generated fqan mapfile for host '" + hostname + "': " + map);
				return map;
			} else {
				String message = logUserAccess() + "Unauthorized access to generateFqanMapfile for host '"+hostname+"'";
				gumsAdminLog.warn(message);
				siteAdminLog.warn(message);
				throw new AuthorizationDeniedException();
			}
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
	}        

	public String generateGrid3UserVoMap(String hostname) {
		return generateOsgUserVoMap(hostname);
	}

	public String generateGridMapfile(String hostname) {
		try {
			if (!gums().getConfiguration().getAllowGridmapFiles())
				throw new RuntimeException("Grid Mapfile generation has been disabled (probably to conserve pool accounts)");
		
			if (hasReadAllAccess(currentUser(), hostname)) {
				String map = gums().getCoreLogic().generateGridMapfile(hostname, true, false, false);
				gumsAdminLog.info(logUserAccess() + "Generated grid mapfile for host '" + hostname + "': " + map);
				return map;
			} else {
				String message = logUserAccess() + "Unauthorized access to generateGridMapfile for host '"+hostname+"'";
				gumsAdminLog.warn(message);
				siteAdminLog.warn(message);
				throw new AuthorizationDeniedException();
			}
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	public String generateOsgUserVoMap(String hostname) {
		try {
			if (hasReadAllAccess(currentUser(), hostname)) {
				String map = gums().getCoreLogic().generateOsgUserVoMap(hostname);
				gumsAdminLog.info(logUserAccess() + "Generated grid3 vo-user map for host '" + hostname + "': " + map);
				return map;
			} else {
				String message = logUserAccess() + "Unauthorized access to generateOsgUserVoMap for host '"+hostname+"'";
				gumsAdminLog.warn(message);
				siteAdminLog.warn(message);
				throw new AuthorizationDeniedException();
			}
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	public String generateVoGridMapfile(String hostname) {
		try {
			if (!gums().getConfiguration().getAllowGridmapFiles())
				throw new RuntimeException("Grid Mapfile generation has been disabled (probably to conserve pool accounts)");
			
			if (hasReadAllAccess(currentUser(), hostname)) {
				String map = gums().getCoreLogic().generateGridMapfile(hostname, true, true, false);
				gumsAdminLog.info(logUserAccess() + "Generated mapfile for host '" + hostname + "': " + map);
				return map;
			} else {
				String message = logUserAccess() + "Unauthorized access to generateVoGridMapfile for host '"+hostname+"'";
				gumsAdminLog.warn(message);
				siteAdminLog.warn(message);
				throw new AuthorizationDeniedException();
			}
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
	}
		
	public Collection getBackupConfigDates() {
		try {
			Collection backupConfigDates = null;
			if (hasReadAllAccess(currentUser(), null)) {
				backupConfigDates = gums().getBackupConfigDates();
			}
			else {
				String message = logUserAccess() + "Unauthorized access to getBackupConfigDates";
				gumsAdminLog.warn(message);
				siteAdminLog.warn(message);
				throw new AuthorizationDeniedException();
			}    	
			return backupConfigDates;
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	public Configuration getConfiguration() {
		try {
			if (hasReadAllAccess(currentUser(), null) && isInWeb) {
				return gums().getConfiguration();
			}
			else {
				String message = logUserAccess() + "Unauthorized access to getConfiguration";
				gumsAdminLog.warn(message);
				siteAdminLog.warn(message);
				throw new AuthorizationDeniedException();		
			}
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	public String getPoolAccountAssignments(String accountPoolMapperName) {
		try {
			if (hasReadAllAccess(currentUser(), null)) {
				if (gums().getConfiguration().getAccountMapper(accountPoolMapperName) instanceof AccountPoolMapper)
					return ((AccountPoolMapper)gums().getConfiguration().getAccountMapper(accountPoolMapperName)).getAssignments();
				else
					return "";
			}
			else {
				String message = logUserAccess() + "Unauthorized access to getPoolAccountAssignments for '"+accountPoolMapperName+"'";
				gumsAdminLog.warn(message);
				siteAdminLog.warn(message);
				throw new AuthorizationDeniedException();	
			}
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}		
	}

	public String getVersion() {
		return GUMS.getVersion();
	}

	public void manualGroupAdd2(String manualUserGroupName, String userDN) {
		try {
			if (hasWriteAccess(currentUser())) {
				getManualUserGroupDB(manualUserGroupName).addMember(new GridUser(userDN, null));
				gumsAdminLog.info(logUserAccess() + "Added to user group '" + manualUserGroupName + "' user '" + userDN + "'");
				siteAdminLog.info(logUserAccess() + "Added to user group '" + manualUserGroupName + "' user '" + userDN + "'");
			} else {
				String message = logUserAccess() + "Unauthorized access to manualGroupAdd2 for user group '" + manualUserGroupName + "' user '" + userDN + "'";
				gumsAdminLog.warn(message);
				siteAdminLog.warn(message);
				throw new AuthorizationDeniedException();	
			}
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}		
	}
	
	public void manualGroupAdd3(String manualUserGroupName, String userDN, String fqan, String email) {
		try {
			if (hasWriteAccess(currentUser())) {
				getManualUserGroupDB(manualUserGroupName).addMember(new GridUser(userDN, fqan, email, false));
				gumsAdminLog.info(logUserAccess() + "Added to user group '" + manualUserGroupName + "' user '" + userDN + "' email '"+email+"'");
				siteAdminLog.info(logUserAccess() + "Added to user group '" + manualUserGroupName + "' user '" + userDN + "' email '"+email+"'");
			} else {
				String message = logUserAccess() + "Unauthorized access to manualGroupAdd3 for user group '" + manualUserGroupName + "' user '" + userDN + "'"+email+"'";
				gumsAdminLog.warn(message);
				siteAdminLog.warn(message);
				throw new AuthorizationDeniedException();	
			}
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}		
	}

	public void manualGroupRemove2(String manualUserGroupName, String userDN) {
		try {
			if (hasWriteAccess(currentUser())) {
				getManualUserGroupDB(manualUserGroupName).removeMember(new GridUser(userDN, null));
				gumsAdminLog.info(logUserAccess() + "Removed from user group '" + manualUserGroupName + "'  user '" + userDN + "'");
				siteAdminLog.info(logUserAccess() + "Removed from user group '" + manualUserGroupName + "'  user '" + userDN + "'");
			} else {
				String message = logUserAccess() + "Unauthorized access to manualGroupRemove2 for user group '" + manualUserGroupName + "' user '" + userDN + "'";
				gumsAdminLog.warn(message);
				siteAdminLog.warn(message);				
				throw new AuthorizationDeniedException();
			}
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}			
	}

	public void manualGroupRemove3(String manualUserGroupName, String userDN, String fqan) {
		try {
			if (hasWriteAccess(currentUser())) {
				getManualUserGroupDB(manualUserGroupName).removeMember(new GridUser(userDN, fqan, false));
				gumsAdminLog.info(logUserAccess() + "Removed from user group '" + manualUserGroupName + "'  user '" + userDN + "' fqan '"+fqan+"'");
				siteAdminLog.info(logUserAccess() + "Removed from user group '" + manualUserGroupName + "'  user '" + userDN + "' fqan '"+fqan+"'");
			} else {
				String message = logUserAccess() + "Unauthorized access to manualGroupRemove2 for user group '" + manualUserGroupName + "' user '" + userDN + "' fqan '"+fqan+"'";
				gumsAdminLog.warn(message);
				siteAdminLog.warn(message);				
				throw new AuthorizationDeniedException();
			}
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}			
	}
	
	public void manualMappingAdd2(String manualAccountMapperName, String userDN, String account) {
		try {
			if (hasWriteAccess(currentUser())) {
				getManualAccountMapperDB(manualAccountMapperName).createMapping(userDN, account);
				gumsAdminLog.info(logUserAccess() + "Added mapping to account mapper '" + manualAccountMapperName + "' for user '" + userDN + "' to account '" + account + "'");
				siteAdminLog.info(logUserAccess() + "Added mapping to account mapper '" + manualAccountMapperName + "' for user '" + userDN + "' to account '" + account + "'");
			} else {
				String message = logUserAccess() + "Unauthorized access to manualMappingAdd2 for account mapper '" + manualAccountMapperName + "' user '" + userDN + "' account '"+account+"'";
				gumsAdminLog.warn(message);
				siteAdminLog.warn(message);				
				throw new AuthorizationDeniedException();
			}
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}		
	}

	public void manualMappingRemove2(String manualAccountMapperName, String userDN) {
		try {
			if (hasWriteAccess(currentUser())) {
				getManualAccountMapperDB(manualAccountMapperName).removeMapping(userDN);
				gumsAdminLog.info(logUserAccess() + "Removed mapping from account mapper '" + manualAccountMapperName + "' for user '" + userDN + "'");
				siteAdminLog.info(logUserAccess() + "Removed mapping from account mapper '" + manualAccountMapperName + "' for user '" + userDN + "'");
			} else {
				String message = logUserAccess() + "Unauthorized access to manualMappingRemove2 for user group '" + manualAccountMapperName + "' user '" + userDN + "'";
				gumsAdminLog.warn(message);
				siteAdminLog.warn(message);	
				throw new AuthorizationDeniedException();
			}
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}		
	}

	public String mapAccount(String accountName) {
		try {
			if (hasReadAllAccess(currentUser(), null)) {
				String DNs = gums().getCoreLogic().mapAccount(accountName);
				gumsAdminLog.info(logUserAccess() + "Mapped the account '" + accountName + "' to '" + DNs + "'");
				return DNs;
			} else {
				String message = logUserAccess() + "Unauthorized access to mapAccount for account '" + accountName + "'";
				gumsAdminLog.warn(message);
				siteAdminLog.warn(message);	
				throw new AuthorizationDeniedException();
			}
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	public String mapUser(String hostname, String userDN, String fqan) {
		try {
			if ( (hasReadSelfAccess(currentUser()) && currentUser().compareDn(userDN)==0) || hasReadAllAccess(currentUser(), hostname)) {
				String account = gums().getCoreLogic().map(hostname, new GridUser(userDN, fqan));
				gumsAdminLog.info(logUserAccess() + "Mapped on host '" + hostname + "' the user '" + userDN + "' / '" + fqan + "' to '" + account + "'");
				return account;
			} else {
				String message = logUserAccess() + "Unauthorized access to mapUser for '" + hostname + "' the user '" + userDN + "' / '" + fqan + "'";
				gumsAdminLog.warn(message);
				siteAdminLog.warn(message);	
				throw new AuthorizationDeniedException();
			}
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	public void removeAccountRange(String accountPoolMapperName, String range) {
		try {
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
				String message = logUserAccess() + "Unauthorized access to removeAccountRange for '"+accountPoolMapperName+" "+range+"'";
				gumsAdminLog.warn(message);
				siteAdminLog.warn(message);	
				throw new AuthorizationDeniedException();
			}
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	public void restoreConfiguration(String dateStr) throws Exception {
		try {
			if (hasWriteAccess(currentUser()))
				gums().restoreConfiguration(dateStr);
			else {
				String message = logUserAccess() + "Unauthorized access to restoreConfiguration for '"+dateStr+"'";
				gumsAdminLog.warn(message);
				siteAdminLog.warn(message);	
				throw new AuthorizationDeniedException();
			}  	
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}		
	}

	public void setConfiguration(Configuration configuration) throws Exception {
		try {
			if (hasWriteAccess(currentUser()))
				gums().setConfiguration(configuration, false);
			else {
				String message = logUserAccess() + "Unauthorized access to setConfiguration";
				gumsAdminLog.warn(message);
				gumsAdminLog.debug("Configuration contents: " + configuration.toXml());
				siteAdminLog.warn(message);	
				siteAdminLog.debug("Configuration contents: " + configuration.toXml());
				throw new AuthorizationDeniedException();
			}
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}		
	}

	public void unassignAccountRange(String accountPoolMapperName, String range) {
		try {
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
	
				gumsAdminLog.info(logUserAccess() + "Unassigned accounts from account mapper '" + accountPoolMapperName + "'");
				siteAdminLog.info(logUserAccess() + "Unassigned accounts from account mapper '" + accountPoolMapperName + "'");
			}
			else {
				String message = logUserAccess() + "Unauthorized access to unassignAccountRange for '"+accountPoolMapperName+" "+range+"'";
				gumsAdminLog.warn(message);
				siteAdminLog.warn(message);	
				throw new AuthorizationDeniedException();
			}
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}		
	}

	public void updateGroups() {
		try {
			if (hasWriteAccess(currentUser())) {
				gums().getCoreLogic().updateGroups();
				gumsAdminLog.info(logUserAccess() + "Groups updated");
				siteAdminLog.info(logUserAccess() + "Groups updated");
			} else {
				String message = logUserAccess() + "Unauthorized access to updateGroups";
				gumsAdminLog.warn(message);
				siteAdminLog.warn(message);	
				throw new AuthorizationDeniedException();
			}
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	public String getCurrentDn() {
		return currentUser().getCertificateDN();
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
				gumsAdminLog.info(logUserAccess() + "Added to persistence '" + persistanceFactory + "' group '" + group + "'  user '" + userDN + "'");
				siteAdminLog.info(logUserAccess() + "Added to persistence '" + persistanceFactory + "' group '" + group + "'  user '" + userDN + "'");
			} else {
				String message = logUserAccess() + "Unauthorized access to manualGroupAdd for persistance '" + persistanceFactory + "' user group '" + group + "' user '" + userDN + "'";
				gumsAdminLog.warn(message);
				siteAdminLog.warn(message);
				throw new AuthorizationDeniedException();
			}
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
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
				gumsAdminLog.info(logUserAccess() + "Removed from persistence '" + persistanceFactory + "' group '" + group + "' user '" + userDN + "'");
				siteAdminLog.info(logUserAccess() + "Removed from persistence '" + persistanceFactory + "' group '" + group + "' user '" + userDN + "'");
			} else {
				String message = logUserAccess() + "Unauthorized access to manualGroupRemove for persistance '" + persistanceFactory + "' user group '" + group + "' user '" + userDN + "'";
				gumsAdminLog.warn(message);
				siteAdminLog.warn(message);
				throw new AuthorizationDeniedException();
			}
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
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
				gumsAdminLog.info(logUserAccess() + "Added mapping to persistence '" + persistanceFactory + "' group '" + group + "' for user '" + userDN + "' to account '" + account + "'");
				siteAdminLog.info(logUserAccess() + "Added mapping to persistence '" + persistanceFactory + "' group '" + group + "' for user '" + userDN + "' to account '" + account + "'");
			} else {
				String message = "Unauthorized access to manualMappingAdd for persistence '" + persistanceFactory + "' group '" + group + "' for user '" + userDN + "' to account '" + account + "'";
				gumsAdminLog.warn(message);
				siteAdminLog.warn(message);
				throw new AuthorizationDeniedException();
			}
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
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
				gumsAdminLog.info(logUserAccess() + "Removed mapping from persistence '" + persistanceFactory + "' group '" + group + "' for user '" + userDN + "'");
				siteAdminLog.info(logUserAccess() + "Removed mapping from persistence '" + persistanceFactory + "' group '" + group + "' for user '" + userDN + "'");
			} else {
				String message = "Unauthorized access to manualMappingRemove for persistence '" + persistanceFactory + "' group '" + group + "' for user '" + userDN + "'";
				gumsAdminLog.warn(message);
				siteAdminLog.warn(message);
				throw new AuthorizationDeniedException();
			}
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	public void poolAddAccount(String persistanceFactory, String group, String accountName) {
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
				db.addAccount(accountName);
				gumsAdminLog.info(logUserAccess() + "Added account to pool: persistence '" + persistanceFactory + "' group '" + group + "' account '" + accountName + "'");
				siteAdminLog.info(logUserAccess() + "Added account to pool: persistence '" + persistanceFactory + "' group '" + group + "' account '" + accountName + "'");
			} else {
				String message = "Unauthorized access to poolAddAccount for persistence '" + persistanceFactory + "' group '" + group + "' account '" + accountName + "'";
				gumsAdminLog.warn(message);
				siteAdminLog.warn(message);
				throw new AuthorizationDeniedException();
			}
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
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

	private String getAccountPoolMapper(String pool) throws Exception {
		Collection accountMappers = gums().getConfiguration().getAccountMappers().values();
		Iterator it = accountMappers.iterator();
		while (it.hasNext()) {
			AccountMapper accountMapper = (AccountMapper)it.next();
			if ( accountMapper instanceof AccountPoolMapper && ((AccountPoolMapper)accountMapper).getAccountPoolRoot().equals(pool))
				return accountMapper.getName();
		}
		return null;
	}

	private AccountPoolMapperDB getAccountPoolMapperDB(String accountPoolMapperName) throws Exception {
		return ((AccountPoolMapper) gums().getConfiguration().getAccountMapper(accountPoolMapperName)).getDB();
	}

	private ManualAccountMapperDB getManualAccountMapperDB(String manualAccountMapperName) throws Exception {
		return ((ManualAccountMapper) gums().getConfiguration().getAccountMapper(manualAccountMapperName)).getDB();
	}

	private ManualUserGroupDB getManualUserGroupDB(String manualUserGroupName) throws Exception {
		return ((ManualUserGroup) gums().getConfiguration().getUserGroup(manualUserGroupName)).getDB();
	}    

	private GUMS gums() {
		if (gums == null) {
			FileConfigurationStore confStore = new FileConfigurationStore(CertCache.getConfigDir(), CertCache.getResourceDir(), getVersionNoPatch());
			gums = new GUMS(confStore);
		}
		return gums;
	}

	private boolean hasReadAllAccess(GridUser user, String hostname) throws Exception {
		if (user == null) 
			return false;
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

	private boolean hasReadSelfAccess(GridUser currentUser) throws Exception {
		if (currentUser == null) 
			return false;
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

	private boolean hasWriteAccess(GridUser user) throws Exception {
		if (user == null) 
			return false;
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
		try {
			Collection hostToGroupMappers = gums().getConfiguration().getHostToGroupMappings();
			Iterator it = hostToGroupMappers.iterator();
			while (it.hasNext()) {
				HostToGroupMapping hostToGroupMapper = (HostToGroupMapping) it.next();
				if (hostToGroupMapper.isInGroup(hostname)) {
					return hostToGroupMapper;
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
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
