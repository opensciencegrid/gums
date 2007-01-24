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
    private boolean errorOnMissedMapping;

    public List getHostToGroupMappings() {
        return Collections.unmodifiableList(hostToGroupMappings);
    }
    
    public Map getGroupToAccountMappings() {
        return Collections.unmodifiableMap(groupToAccountMappings);
    }

    public Map getPersistenceFactories()  {
        return Collections.unmodifiableMap(persistenceFactories);
    }

    public Map getAccountMappers()  {
        return Collections.unmodifiableMap(accountMappers);
    }
 
    public Map getUserGroups() {
        return Collections.unmodifiableMap(userGroups);
    }    

    public Map getVirtualOrganizations() {
        return virtualOrganizations;
    }      
    
    public HostToGroupMapping getHostToGroupMapping(String name) {
        Iterator it = hostToGroupMappings.iterator();
        while(it.hasNext()) {
        	HostToGroupMapping hostToGroupMapping = (HostToGroupMapping)it.next();
        	if(hostToGroupMapping.getName().equals(name))
        		return hostToGroupMapping;
        }
        return null;
    }
    
    public GroupToAccountMapping getGroupToAccountMapping(String groupToAccountMapping) {
    	return (GroupToAccountMapping)groupToAccountMappings.get(groupToAccountMapping);
    }

    public PersistenceFactory getPersistenceFactory(String persistenceFactory)  {
        return (PersistenceFactory)persistenceFactories.get(persistenceFactory);
    }
    
    public AccountMapper getAccountMapper(String accountMapper)  {
        return (AccountMapper)accountMappers.get(accountMapper);
    }
 
    public UserGroup getUserGroup(String userGroup) {
        return (UserGroup)userGroups.get(userGroup);
    }    
    
    public VirtualOrganization getVirtualOrganization(String virtualOrganization) {
        return (VirtualOrganization)virtualOrganizations.get(virtualOrganization);
    }      
    
    public void addHostToGroupMapping(HostToGroupMapping h2GMapping) {
        log.trace("Adding HostToGroupMapper to the configuration: " + h2GMapping.getName());
        if (hostToGroupMappings.contains(h2GMapping.getName()))
    		log.error("Host to group mapping " + h2GMapping.getName() + " already exists");
        hostToGroupMappings.add(h2GMapping);
        if (h2GMapping.getConfiguration()==null)
        	h2GMapping.setConfiguration(this);
    }
    
    public void addHostToGroupMapping(int index, HostToGroupMapping h2GMapping) {
        log.trace("Adding HostToGroupMapper to the configuration: " + h2GMapping.getName());
        if (hostToGroupMappings.contains(h2GMapping.getName()))
    		log.error("Host to group mapping " + h2GMapping.getName() + " already exists");
        hostToGroupMappings.add(index, h2GMapping);
        if (h2GMapping.getConfiguration()==null)
        	h2GMapping.setConfiguration(this);
    }
    
    public void addGroupToAccountMapping(GroupToAccountMapping g2AMapping) {
        log.trace("Adding GroupToAccountMapper to the configuration: " + g2AMapping.getName());
        if (groupToAccountMappings.get(g2AMapping.getName())!=null)
        		log.error("Group to account mapping " + g2AMapping.getName() + " already exists");
       	groupToAccountMappings.put(g2AMapping.getName(), g2AMapping);
        if (g2AMapping.getConfiguration()==null)
        	g2AMapping.setConfiguration(this);
    }    
    
    public void addPersistenceFactory(PersistenceFactory peristenceFactory) {
        log.trace("Adding PersistenceManager to the configuration: " + peristenceFactory.getName());
        if (persistenceFactories.get(peristenceFactory.getName())!=null)
        	log.error("PersistenceFactory " + peristenceFactory.getName() + " already exists");
        persistenceFactories.put(peristenceFactory.getName(), peristenceFactory);
        if (peristenceFactory.getConfiguration()==null)
        	peristenceFactory.setConfiguration(this);
    }    
    
    public void addAccountMapper(AccountMapper accountMapper) {
        log.trace("Adding AccountManager to the configuration: " + accountMapper.getName());
        if (accountMappers.get(accountMapper.getName())!=null)
        	log.error("Account mapper " + accountMapper.getName() + " already exists");
        accountMappers.put(accountMapper.getName(), accountMapper);
        if (accountMapper.getConfiguration()==null)
        	accountMapper.setConfiguration(this);
    }    
    
    public void addUserGroup(UserGroup userGroup) {
        log.trace("Adding UserGroupManager to the configuration: " + userGroup.getName());
        if (userGroups.get(userGroup.getName())!=null)
        	log.error("User group " + userGroup.getName() + " already exists");
        userGroups.put(userGroup.getName(), userGroup);
        if (userGroup.getConfiguration()==null)
        	userGroup.setConfiguration(this);
    }   

    public void addVirtualOrganization(VirtualOrganization virtualOrganization) {
        log.trace("Adding VO to the configuration: " + virtualOrganization.getName());
        if (virtualOrganizations.get(virtualOrganization.getName())!=null)
        	log.error("Virtual organization " + virtualOrganization.getName() + " already exists");
        virtualOrganizations.put(virtualOrganization.getName(), virtualOrganization);
        if (virtualOrganization.getConfiguration()==null)
        	virtualOrganization.setConfiguration(this);
    }
    
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

    public GroupToAccountMapping removeGroupToAccountMapping(String name) {
    	return (GroupToAccountMapping)groupToAccountMappings.remove(name);
    }
 
    public UserGroup removeUserGroup(String name) {
    	return (UserGroup)userGroups.remove(name);
    }

    public AccountMapper removeAccountMapper(String name) {
    	return (AccountMapper)accountMappers.remove(name);
    }

    public PersistenceFactory removePersistenceFactory(String name) {
    	return (PersistenceFactory)persistenceFactories.remove(name);
    }
    
    public VirtualOrganization removeVirtualOrganization(String name) {
    	return (VirtualOrganization)virtualOrganizations.remove(name);
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
    
    public Object clone() {
    	Configuration newConf = new Configuration();
    	
    	Iterator it = persistenceFactories.values().iterator();
    	while (it.hasNext() )
    		newConf.addPersistenceFactory( (PersistenceFactory)((PersistenceFactory)it.next()).clone(newConf) );
    	
    	it = virtualOrganizations.values().iterator();
    	while (it.hasNext() )
    		newConf.addVirtualOrganization( (VirtualOrganization)((VirtualOrganization)it.next()).clone(newConf) );
    	
    	it = accountMappers.values().iterator();
    	while (it.hasNext() )
    		newConf.addAccountMapper( (AccountMapper)((AccountMapper)it.next()).clone(newConf) );    
    	
    	it = userGroups.values().iterator();
    	while (it.hasNext() )
    		newConf.addUserGroup( (UserGroup)((UserGroup)it.next()).clone(newConf) );  
    	
    	it = groupToAccountMappings.values().iterator();
    	while (it.hasNext() )
    		newConf.addGroupToAccountMapping( (GroupToAccountMapping)((GroupToAccountMapping)it.next()).clone(newConf) );  
    	
    	it = hostToGroupMappings.iterator();
    	while (it.hasNext() )
    		newConf.addHostToGroupMapping( (HostToGroupMapping)((HostToGroupMapping)it.next()).clone(newConf) );  	
    	
    	return newConf;
    }
}
