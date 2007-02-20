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

import javax.xml.parsers.ParserConfigurationException;

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
    
    public void addAccountMapper(AccountMapper accountMapper) {
        log.trace("Adding AccountManager to the configuration: " + accountMapper.getName());
        if (accountMappers.get(accountMapper.getName())!=null)
        	log.warn("Account mapper " + accountMapper.getName() + " replaced with one with same name");
        accountMappers.put(accountMapper.getName(), accountMapper);
        if (accountMapper.getConfiguration()==null)
        	accountMapper.setConfiguration(this);
    }
    
    public void addGroupToAccountMapping(GroupToAccountMapping g2AMapping) {
        log.trace("Adding GroupToAccountMapper to the configuration: " + g2AMapping.getName());
        if (getGroupToAccountMapping(g2AMapping.getName())!=null) {
        	log.warn("Group to account mapping " + g2AMapping.getName() + " merged with one with same name");
        	// For the sake of old versions of gums.config that may have duplicate g2AMappings with
        	// different account mappers, copy over account mappers into new groupToAccountMapping
        	Iterator it = getGroupToAccountMapping(g2AMapping.getName()).getAccountMappers().iterator();
        	while (it.hasNext())
        		g2AMapping.addAccountMapper( new String((String)it.next())); 
        }
       	groupToAccountMappings.put(g2AMapping.getName(), g2AMapping);
        if (g2AMapping.getConfiguration()==null)
        	g2AMapping.setConfiguration(this);
    }

    public void addHostToGroupMapping(HostToGroupMapping h2GMapping) {
        log.trace("Adding HostToGroupMapper to the configuration: " + h2GMapping.getName());
        if ( getHostToGroupMapping(h2GMapping.getName())!=null )
        	log.warn("Host to group mapping " + h2GMapping.getName() + " replaced with one with same name");
        hostToGroupMappings.add(h2GMapping);
        if (h2GMapping.getConfiguration()==null)
        	h2GMapping.setConfiguration(this);
    }

    public void addHostToGroupMapping(int index, HostToGroupMapping h2GMapping) {
        log.trace("Adding HostToGroupMapper to the configuration: " + h2GMapping.getName());
        if ( getHostToGroupMapping(h2GMapping.getName())!=null )
    		log.warn("Host to group mapping " + h2GMapping.getName() + " replaced with one with same name");
        hostToGroupMappings.add(index, h2GMapping);
        if (h2GMapping.getConfiguration()==null)
        	h2GMapping.setConfiguration(this);
    }
 
    public void addPersistenceFactory(PersistenceFactory peristenceFactory) {
        log.trace("Adding PersistenceManager to the configuration: " + peristenceFactory.getName());
        if (persistenceFactories.get(peristenceFactory.getName())!=null)
        	log.warn("PersistenceFactory " + peristenceFactory.getName() + " replaced with one with same name");
        persistenceFactories.put(peristenceFactory.getName(), peristenceFactory);
        if (peristenceFactory.getConfiguration()==null)
        	peristenceFactory.setConfiguration(this);
    }    

    public void addUserGroup(UserGroup userGroup) {
        log.trace("Adding UserGroupManager to the configuration: " + userGroup.getName());
        if (userGroups.get(userGroup.getName())!=null)
        	log.warn("User group " + userGroup.getName() + " replaced with one with same name");
        userGroups.put(userGroup.getName(), userGroup);
        if (userGroup.getConfiguration()==null)
        	userGroup.setConfiguration(this);
    }      
    
    public void addVirtualOrganization(VirtualOrganization virtualOrganization) {
        log.trace("Adding VO to the configuration: " + virtualOrganization.getName());
        if (virtualOrganizations.get(virtualOrganization.getName())!=null)
        	log.warn("Virtual organization " + virtualOrganization.getName() + " replaced with one with same name");
        virtualOrganizations.put(virtualOrganization.getName(), virtualOrganization);
        if (virtualOrganization.getConfiguration()==null)
        	virtualOrganization.setConfiguration(this);
    }
    
    public Object clone() {
    	Configuration newConf = new Configuration();
    	
    	Iterator it = persistenceFactories.values().iterator();
    	while (it.hasNext() )
    		newConf.addPersistenceFactory( ((PersistenceFactory)it.next()).clone(newConf) );
    	
    	it = virtualOrganizations.values().iterator();
    	while (it.hasNext() )
    		newConf.addVirtualOrganization( ((VirtualOrganization)it.next()).clone(newConf) );
    	
    	it = accountMappers.values().iterator();
    	while (it.hasNext() )
    		newConf.addAccountMapper( ((AccountMapper)it.next()).clone(newConf) );    
    	
    	it = userGroups.values().iterator();
    	while (it.hasNext() )
    		newConf.addUserGroup( ((UserGroup)it.next()).clone(newConf) );  
   
    	it = groupToAccountMappings.values().iterator();
    	while (it.hasNext() )
    		newConf.addGroupToAccountMapping( ((GroupToAccountMapping)it.next()).clone(newConf) );  
    	
    	it = hostToGroupMappings.iterator();
    	while (it.hasNext() )
    		newConf.addHostToGroupMapping( ((HostToGroupMapping)it.next()).clone(newConf) );  	
    	
    	return newConf;
    }

    public AccountMapper getAccountMapper(String accountMapper)  {
        return (AccountMapper)accountMappers.get(accountMapper);
    }
    
    public Map getAccountMappers()  {
        return Collections.unmodifiableMap(accountMappers);
    }
 
    public GroupToAccountMapping getGroupToAccountMapping(String groupToAccountMapping) {
    	return (GroupToAccountMapping)groupToAccountMappings.get(groupToAccountMapping);
    }    
    
    public Map getGroupToAccountMappings() {
        return Collections.unmodifiableMap(groupToAccountMappings);
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
    
    public List getHostToGroupMappings() {
        return Collections.unmodifiableList(hostToGroupMappings);
    }
    
    public Map getPersistenceFactories()  {
        return Collections.unmodifiableMap(persistenceFactories);
    }    
    
    public PersistenceFactory getPersistenceFactory(String persistenceFactory)  {
        return (PersistenceFactory)persistenceFactories.get(persistenceFactory);
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

    public UserGroup getUserGroup(String userGroup) {
        return (UserGroup)userGroups.get(userGroup);
    }
    
    public Map getUserGroups() {
        return Collections.unmodifiableMap(userGroups);
    }

    public VirtualOrganization getVirtualOrganization(String virtualOrganization) {
        return (VirtualOrganization)virtualOrganizations.get(virtualOrganization);
    }
 
    public Map getVirtualOrganizations() {
        return virtualOrganizations;
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
    
    public AccountMapper removeAccountMapper(String name) {
    	return (AccountMapper)accountMappers.remove(name);
    }
    
    public GroupToAccountMapping removeGroupToAccountMapping(String name) {
    	return (GroupToAccountMapping)groupToAccountMappings.remove(name);
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
    
    public PersistenceFactory removePersistenceFactory(String name) {
    	return (PersistenceFactory)persistenceFactories.remove(name);
    }   
    
    public UserGroup removeUserGroup(String name) {
    	return (UserGroup)userGroups.remove(name);
    }

    public VirtualOrganization removeVirtualOrganization(String name) {
    	return (VirtualOrganization)virtualOrganizations.remove(name);
    }
    
    /**
     * Setter for property errorOnMissedMapping.
     * @param errorOnMissedMapping New value of property errorOnMissedMapping.
     */
    public void setErrorOnMissedMapping(boolean errorOnMissedMapping) {
        this.errorOnMissedMapping = errorOnMissedMapping;
    }
}
