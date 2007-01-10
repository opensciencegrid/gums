/*
 * Configuration.java
 *
 * Created on May 24, 2004, 2:33 PM
 */

package gov.bnl.gums.configuration;

import gov.bnl.gums.account.AccountMapper;
import gov.bnl.gums.groupToAccount.GroupToAccountMapping;
import gov.bnl.gums.hostToGroup.HostToGroupMapping;
import gov.bnl.gums.userGroup.UserGroup;
import gov.bnl.gums.userGroup.VirtualOrganization;

import java.util.*;

import org.apache.commons.logging.*;

import gov.bnl.gums.persistence.PersistenceFactory;

/** Holds the configuration of GUMS, including which policies will be used
 * for which hosts, which database layer is going to be used and so on.
 * <p>
 * The configuration object will be constructed programmatically by reading
 * an xml file.
 *
 * @author  Gabriele Carcassi
 */
public class Configuration {
    private Log log = LogFactory.getLog(Configuration.class);
    
    private ArrayList hostToGroupMappings = new ArrayList();
    private TreeMap groupToAccountMappings = new TreeMap();
    private TreeMap persistenceFactories = new TreeMap();
    private TreeMap accountMappers = new TreeMap();
    private TreeMap virtualOrganizations = new TreeMap();
    private TreeMap userGroups = new TreeMap();
    private KeyStore keyStore = new KeyStore();
    private boolean errorOnMissedMapping;

    /**
     * Returns all the group mappings defined in the configuration,
     * indexed by their name property.
     * @return a List of Group2AccountMapper objects.
     */
    public Map getGroupToAccountMappings() {
        return groupToAccountMappings;
    }
    
    /**
     * Returns the list of all the hostGroups defined in the configuration.
     * @return a List of Host2GroupMapper objects.
     */
    public ArrayList getHostToGroupMappings() {
        return hostToGroupMappings;
    }

    /**
     * Returns a hostToGroupMapping.
     * @param hostToGroupMapping name.
     * @return a HostToGroupMapping object.
     */
    public HostToGroupMapping getHostToGroupMapping(String name) {
        Iterator it = hostToGroupMappings.iterator();
        while(it.hasNext()) {
        	HostToGroupMapping hostToGroupMapping = (HostToGroupMapping)it.next();
        	if(hostToGroupMapping.getName().equals(name))
        		return hostToGroupMapping;
        }
        return null;
    }

    /**
     * Removes hostToGroupMapping.
     * @param hostToGroupMapping name.
     * @return HostToGroupMapping object removed.
     */
    public HostToGroupMapping removeHostToGroupMapping(String name) {
        Iterator it = hostToGroupMappings.iterator();
        while(it.hasNext()) {
        	HostToGroupMapping hostToGroupMapping = (HostToGroupMapping)it.next();
        	if(hostToGroupMapping.getName().equals(name)) {
        		hostToGroupMappings.remove( hostToGroupMapping );
        		return hostToGroupMapping;
        	}
        }
        return null;
    }
    
    /**
     * Returns all the persistence managers defined in the configuration,
     * indexed by their name property.
     * @return a List of PersistentManager objects.
     */
    public Map getPersistenceFactories()  {
        return persistenceFactories;
    }
    
    /**
     * Returns all the account mappers defined in the configuration,
     * indexed by their name property.
     * @return a List of AccountManager objects.
     */
    public Map getAccountMappers()  {
        return accountMappers;
    }
 
    /**
     * Returns a list of all the user groups defined in the configuration file.
     * <p>
     * @return a list of UserGroupManager objects.
     */
    public Map getUserGroups() {
        return userGroups;
    }    

    
    /**
     * Returns a list of all the virtual organizations defined in the configuration file.
     * <p>
     * @return a list of VO objects.
     */
    public Map getVirtualOrganizations() {
        return virtualOrganizations;
    }      
    
    /**
     * Returns a list of all the self readers defined in the configuration file.
     * <p>
     * @return a list of ReaderGroupManager objects.
     */
    public ArrayList getReadSelfUserGroups() {
    	ArrayList readers = new ArrayList();
    	Iterator it = userGroups.values().iterator();
    	while( it.hasNext() ) {
    		UserGroup userGroup = (UserGroup)it.next();
    		if( userGroup.hasReadSelfAccess() ) {
    			readers.add(userGroup);
    		}
    	}
        return readers;
    }   

    /**
     * Returns a list of all the readers with all read access defined in the configuration file.
     * <p>
     * @return a list of ReaderGroupManager objects.
     */
    public ArrayList getReadAllUserGroups() {
    	ArrayList readers = new ArrayList();
    	Iterator it = userGroups.values().iterator();
    	while( it.hasNext() ) {
    		UserGroup userGroup = (UserGroup)it.next();
    		if( userGroup.hasReadAllAccess() ) {
    			readers.add(userGroup);
    		}
    	}
        return readers;
    }   
    
    /**
     * Returns a list of all the writer groups defined in the configuration file.
     * <p>
     * @return a list of WriterGroupManager objects.
     */
    public ArrayList getWriteUserGroups() {
    	ArrayList writers = new ArrayList();
    	Iterator it = userGroups.values().iterator();
    	while( it.hasNext() ) {
    		UserGroup userGroup = (UserGroup)it.next();
    		if( userGroup.hasWriteAccess() ) {
    			writers.add(userGroup);
    		}
    	}
        return writers;
    }   
    
    /**
     * Getter for property keyStore.
     * @return Value of property keyStore.
     */
    public KeyStore getKeyStore() {
        return keyStore;
    }
    
    /**
     * Adds a group mapping to the configuration.
     * @param mapper a GroupMapper object.
     */
    public void addGroupToAccountMapping(GroupToAccountMapping mapping) {
        log.trace("Adding GroupToAccountMapper to the configuration: " + mapping);
        if (groupToAccountMappings.get(mapping.getName())!=null)
        		log.error("Group to account mapping " + mapping.getName() + " already exists");
        groupToAccountMappings.put(mapping.getName(), mapping);
    }    
    
    /**
     * Addes a host group to the configuation.
     * @param mapper a Host2GroupMapper object
     */
    public void addHostToGroupMapping(HostToGroupMapping mapping) {
        log.trace("Adding HostToGroupMapper to the configuration: " + mapping);
        hostToGroupMappings.add(mapping);
    }

    /**
     * Adds a persistence factory to the configuration.
     * @param persistenceFactory a persistenceFactory object.
     */
    public void addPersistenceFactory(PersistenceFactory factory) {
        log.trace("Adding PersistenceManager to the configuration: " + factory);
        if (persistenceFactories.get(factory.getName())!=null)
        	log.error("PersistenceFactory " + factory.getName() + " already exists");
        persistenceFactories.put(factory.getName(), factory);
    }    
    
    /**
     * Adds an account mapper to the configuration.
     * @param accountMapper an AccountMapper object.
     */
    public void addAccountMapper(AccountMapper accountMapper) {
        log.trace("Adding AccountManager to the configuration: " + accountMapper);
        if (accountMappers.get(accountMapper.getName())!=null)
        	log.error("Account mapper " + accountMapper.getName() + " already exists");
        accountMappers.put(accountMapper.getName(), accountMapper);
    }    
    
    /**
     * Adds an account manager to the configuration.
     * @param accountManager an AccountManager object.
     */
    public void addUserGroup(UserGroup userGroup) {
        log.trace("Adding UserGroupManager to the configuration: " + userGroup);
        if (userGroups.get(userGroup.getName())!=null)
        	log.error("User group " + userGroup.getName() + " already exists");
        userGroups.put(userGroup.getName(), userGroup);
    }   

    /**
     * Adds a virtual organization to the configuration.
     * @param vO a VO object.
     */
    public void addVirtualOrganization(VirtualOrganization virtualOrganization) {
        log.trace("Adding VO to the configuration: " + virtualOrganization);
        if (virtualOrganizations.get(virtualOrganization.getName())!=null)
        	log.error("Virtual organization " + virtualOrganization.getName() + " already exists");
        virtualOrganizations.put(virtualOrganization.getName(), virtualOrganization);
    }     
    
    /**
     * Sets group mappings in configuration.
     * @param mapper a GroupMapper object.
     */
    public void setGroupToAccountMappings(Map groupToAccountMappings) {
        log.trace("Setting GroupToAccountMappers in configuration: " + groupToAccountMappings);
        this.groupToAccountMappings.clear();
        this.groupToAccountMappings.putAll(groupToAccountMappings);
    }    
    
    /**
     * Sets host groups in configuation.
     * @param mapper a Host2GroupMapper object
     */
    public void setHostToGroupMappings(ArrayList hostToGroupMappings) {
        log.trace("Setting HostToGroupMappigns in configuration: " + hostToGroupMappings);
        this.hostToGroupMappings.clear();
        this.hostToGroupMappings.addAll(hostToGroupMappings);
    }    

    /**
     * Sets persistence factory in configuration.
     * @param persistenceFactory a persistenceFactory object.
     */
    public void setPersistenceFactories(Map persistenceFactories) {
        log.trace("Setting PersistenceFactories in configuration: " + persistenceFactories);
        this.persistenceFactories.clear();
        this.persistenceFactories.putAll(persistenceFactories);
    }    
    
    /**
     * Sets account mappers in configuration.
     * @param accountMapper an AccountMapper object.
     */
    public void setAccountMappers(Map accountMappers) {
        log.trace("Setting AccountMappers in configuration: " + accountMappers);
        this.accountMappers.clear();
        this.accountMappers.putAll(accountMappers);
    }    
    
    /**
     * Sets account managers in configuration.
     * @param accountManager an AccountManager object.
     */
    public void setUserGroups(Map userGroups) {
        log.trace("Setting UserGroups in configuration: " + userGroups);
        this.userGroups.clear();
        this.userGroups.putAll(userGroups);
    }    
    

    /**
     * Sets virtual organizations in configuration.
     * @param vO a VO object.
     */
    public void setVirtualOrganizations(Map virtualOrganizations) {
        log.trace("Setting UserGroups in configuration: " + virtualOrganizations);
        this.virtualOrganizations.clear();
        this.virtualOrganizations.putAll(virtualOrganizations);
    }    
    
    /**
     * Setter for property keyStore.
     * @param keyStore New value of property keyStore.
     */
    public void setKeyStore(KeyStore keyStore) {
        log.trace("Changing the keyStore system properties");
        System.setProperty("javax.net.ssl.keyStore",keyStore.getLocation());
        System.setProperty("javax.net.ssl.keyStorePassword",keyStore.getPassword());
        this.keyStore = keyStore;
    }      
    
    /**
     * Getter for property errorOnMissedMapping.
     * @return Value of property errorOnMissedMapping.
     */
    public boolean isErrorOnMissedMapping() {
        return this.errorOnMissedMapping;
    }

    /**
     * Setter for property errorOnMissedMapping.
     * @param errorOnMissedMapping New value of property errorOnMissedMapping.
     */
    public void setErrorOnMissedMapping(boolean errorOnMissedMapping) {
        this.errorOnMissedMapping = errorOnMissedMapping;
    }
}
