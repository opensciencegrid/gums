/*
 * Configuration.java
 *
 * Created on May 24, 2004, 2:33 PM
 */

package gov.bnl.gums.configuration;

import gov.bnl.gums.account.AccountMapper;
import gov.bnl.gums.groupToAccount.GroupToAccountMapping;
import gov.bnl.gums.hostToGroup.HostToGroupMapping;
import gov.bnl.gums.userGroup.ManualUserGroup;
import gov.bnl.gums.userGroup.UserGroup;
import gov.bnl.gums.userGroup.VomsServer;
import gov.bnl.gums.GUMS;
import gov.bnl.gums.GridUser;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Method;
import java.util.*;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import gov.bnl.gums.persistence.PersistenceFactory;

/** 
 * Holds the configuration of GUMS, including which policies will be used
 * for which hosts, which database layer is going to be used and so on.
 * <p>
 * The configuration object will be constructed programmatically by reading
 * an xml file.
 *
 * @author Gabriele Carcassi, Jay Packard
 */
public class Configuration {
    private Logger log = Logger.getLogger(Configuration.class);
    private Logger adminLog = Logger.getLogger(GUMS.gumsAdminLogName);
    private List hostToGroupMappings = new ArrayList();
    private Map groupToAccountMappings = new TreeMap();
    private Map persistenceFactories = new TreeMap();
    private Map accountMappers = new TreeMap();
    private Map vomsServers = new TreeMap();
    private Map userGroups = new TreeMap();
    private List bannedUserGroupList = new ArrayList();
    private String bannedUserGroups = "";
    private boolean allowGridmapFiles = true;
    private boolean transformingFromOld11Version = false;
	private Date created = new Date();

	static private String version = "1.3";
	static int instances = 0;
    
	public Configuration() {
		instances++;
		log.debug("Created config - " + instances + " current instance(s)");
		//try { throw new Exception();}catch(Exception e){log.error("", e);}
	};
	
	public Configuration(boolean transformingFromOld11Version) {
		this.transformingFromOld11Version = transformingFromOld11Version;
		instances++;
		log.debug("Created config - " + instances + " current instance(s)");
		//try { throw new Exception();}catch(Exception e){log.error("", e);}
	}
	
	public void finalize() {
		instances--;
		log.debug("Destroyed config - " + instances + " current instance(s)");
	}
	
    /**
     * @param accountMapper
     */
    public synchronized void addAccountMapper(AccountMapper accountMapper) {
        log.trace("Adding account mapper to the configuration: " + accountMapper.getName());
        if (accountMappers.get(accountMapper.getName()) != null) {
        	if (transformingFromOld11Version)
        		log.trace("Replacing account mapper: " + accountMapper.getName());
        	else {
	        	String message = "Account mapper " + accountMapper.getName() + " already exists";
	        	log.debug(message);
	        	adminLog.error(message);
	        	throw new RuntimeException(message);
        	}
        }     	
        accountMappers.put(accountMapper.getName(), accountMapper);
        if (accountMapper.getConfiguration()==null)
        	accountMapper.setConfiguration(this);
    }
    
    /**
     * @param g2AMapping
     */
    public synchronized void addGroupToAccountMapping(GroupToAccountMapping g2AMapping) {
        log.trace("Adding group to account mapping to the configuration: " + g2AMapping.getName());
        if (getGroupToAccountMapping(g2AMapping.getName()) != null) {
        	String message = "Group to account mapping " + g2AMapping.getName() + " already exists";
        	log.debug(message);
        	adminLog.error(message);
        	throw new RuntimeException(message);
        }
       	groupToAccountMappings.put(g2AMapping.getName(), g2AMapping);
        if (g2AMapping.getConfiguration()==null)
        	g2AMapping.setConfiguration(this);
    }

    /**
     * @param h2GMapping
     */
    public synchronized void addHostToGroupMapping(HostToGroupMapping h2GMapping) {
        log.trace("Adding Host to group mapping to the configuration: " + h2GMapping.getName());
        if (getHostToGroupMapping(h2GMapping.getName()) != null) {
        	String message = "Host to group mapping " + h2GMapping.getName() + " already exists";
        	log.debug(message);
        	adminLog.error(message);
        	throw new RuntimeException(message);
        }
        hostToGroupMappings.add(h2GMapping);
        if (h2GMapping.getConfiguration()==null)
        	h2GMapping.setConfiguration(this);
    }

    /**
     * @param index
     * @param h2GMapping
     */
    public synchronized void addHostToGroupMapping(int index, HostToGroupMapping h2GMapping) {
        log.trace("Adding Host to group mapping to the configuration: " + h2GMapping.getName());
        if (getHostToGroupMapping(h2GMapping.getName()) != null) {
        	String message = "Host to group mapping " + h2GMapping.getName() + " already exists";
        	log.debug(message);
        	adminLog.error(message);
        	throw new RuntimeException(message);
        }
        hostToGroupMappings.add(index, h2GMapping);
        if (h2GMapping.getConfiguration()==null)
        	h2GMapping.setConfiguration(this);
    }
 
    /**
     * @param peristenceFactory
     */
    public synchronized void addPersistenceFactory(PersistenceFactory peristenceFactory) {
        log.trace("Adding persistence factory to the configuration: " + peristenceFactory.getName());
        if (persistenceFactories.get(peristenceFactory.getName()) != null) {
        	String message = "Persistence factory " + peristenceFactory.getName() + " already exists";
        	log.debug(message);
        	adminLog.error(message);
        	throw new RuntimeException(message);
        }        
        persistenceFactories.put(peristenceFactory.getName(), peristenceFactory);
        if (peristenceFactory.getConfiguration()==null)
        	peristenceFactory.setConfiguration(this);
    }    

    /**
     * @param userGroup
     */
    public synchronized void addUserGroup(UserGroup userGroup) {
        log.trace("Adding user group to the configuration: " + userGroup.getName());
        if (userGroups.get(userGroup.getName()) != null) {
        	String message = "User group " + userGroup.getName() + " already exists";
        	log.debug(message);
        	adminLog.error(message);
        	throw new RuntimeException(message);
        }             
        userGroups.put(userGroup.getName(), userGroup);
        if (userGroup.getConfiguration()==null)
        	userGroup.setConfiguration(this);
    }      
    
    /**
     * @param vomsServer
     */
    public synchronized void addVomsServer(VomsServer vomsServer) {
        log.trace("Adding VOMS server to the configuration: " + vomsServer.getName());
        if (vomsServers.get(vomsServer.getName()) != null) {
        	if (transformingFromOld11Version)
        		log.trace("Replacing voms server: " + vomsServer.getName());
        	else {
	        	String message = "VOMS Server " + vomsServer.getName() + " already exists";
	        	log.debug(message);
	        	adminLog.error(message);
	        	throw new RuntimeException(message);
        	}
        }
        vomsServers.put(vomsServer.getName(), vomsServer);
        if (vomsServer.getConfiguration()==null)
        	vomsServer.setConfiguration(this);
    }
    
    public Object clone() {
    	Configuration newConf = new Configuration();
    	
    	newConf.setAllowGridmapFiles(getAllowGridmapFiles());
    	newConf.setBannedUserGroups(new String(getBannedUserGroups()));
    	
    	Iterator it = persistenceFactories.values().iterator();
    	while (it.hasNext() )
    		newConf.addPersistenceFactory( ((PersistenceFactory)it.next()).clone(newConf));
    	
    	it = vomsServers.values().iterator();
    	while (it.hasNext() )
    		newConf.addVomsServer( ((VomsServer)it.next()).clone(newConf));
    	
    	it = accountMappers.values().iterator();
    	while (it.hasNext() )
    		newConf.addAccountMapper( ((AccountMapper)it.next()).clone(newConf));    
    	
    	it = userGroups.values().iterator();
    	while (it.hasNext() )
    		newConf.addUserGroup( ((UserGroup)it.next()).clone(newConf));  
   
    	it = groupToAccountMappings.values().iterator();
    	while (it.hasNext() )
    		newConf.addGroupToAccountMapping( ((GroupToAccountMapping)it.next()).clone(newConf));  
    	
    	it = hostToGroupMappings.iterator();
    	while (it.hasNext() )
    		newConf.addHostToGroupMapping( ((HostToGroupMapping)it.next()).clone(newConf)); 
    	
    	return newConf;
    }

    /**
     * @param accountMapper
     * @return
     */
    public AccountMapper getAccountMapper(String accountMapper)  {
        return (AccountMapper)accountMappers.get(accountMapper);
    }
    
    /**
     * @return
     */
    public Map getAccountMappers()  {
        return Collections.unmodifiableMap(accountMappers);
    }
 
    public boolean getAllowGridmapFiles() {
		return allowGridmapFiles;
	}    
    
    public List getBannedUserGroupList() {
    	return Collections.unmodifiableList(bannedUserGroupList);
    }
    
    public String getBannedUserGroups() {
    	return bannedUserGroups;
    }

	public Date getCreated() {
		return created;
	}
    
    /**
     * @param groupToAccountMapping
     * @return
     */
    public GroupToAccountMapping getGroupToAccountMapping(String groupToAccountMapping) {
    	return (GroupToAccountMapping)groupToAccountMappings.get(groupToAccountMapping);
    }      
    
    /**
     * @return
     */
    public Map getGroupToAccountMappings() {
        return Collections.unmodifiableMap(groupToAccountMappings);
    }
    
    /**
     * @param name
     * @return
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
     * @return
     */
    public List getHostToGroupMappings() {
        return Collections.unmodifiableList(hostToGroupMappings);
    }    
    
    /**
     * @return
     */
    public Map getPersistenceFactories()  {
        return Collections.unmodifiableMap(persistenceFactories);
    }    
    
    /**
     * @param persistenceFactory
     * @return
     */
    public PersistenceFactory getPersistenceFactory(String persistenceFactory)  {
        return (PersistenceFactory)persistenceFactories.get(persistenceFactory);
    }    
    
    /**
     * Returns a list of all the readers with all read access defined in the configuration file.
     *
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
     * 
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
     * @param userGroup
     * @return
     */
    public UserGroup getUserGroup(String userGroup) {
        return (UserGroup)userGroups.get(userGroup);
    }

    /**
     * @return
     */
    public Map getUserGroups() {
        return Collections.unmodifiableMap(userGroups);
    }
 
    /**
     * @param vomsServer
     * @return
     */
    public VomsServer getVomsServer(String vomsServer) {
        return (VomsServer)vomsServers.get(vomsServer);
    }

    /**
     * @return
     */
    public Map getVomsServers() {
        return vomsServers;
    }

    /**
     * Returns a list of all the writer groups defined in the configuration file.
     *
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
     * @param name
     * @return
     */
    public synchronized AccountMapper removeAccountMapper(String name) {
    	return (AccountMapper)accountMappers.remove(name);
    }
    
    /**
     * @param name
     * @return
     */
    public synchronized GroupToAccountMapping removeGroupToAccountMapping(String name) {
    	return (GroupToAccountMapping)groupToAccountMappings.remove(name);
    }   

    /**
     * @param name
     * @return
     */
    public synchronized HostToGroupMapping removeHostToGroupMapping(String name) {
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
     * @param name
     * @return
     */
    public synchronized PersistenceFactory removePersistenceFactory(String name) {
    	return (PersistenceFactory)persistenceFactories.remove(name);
    }   
    
    /**
     * @param name
     * @return
     */
    public synchronized UserGroup removeUserGroup(String name) {
    	return (UserGroup)userGroups.remove(name);
    }

    /**
     * @param name
     * @return
     */
    public synchronized VomsServer removeVomsServer(String name) {
    	return (VomsServer)vomsServers.remove(name);
    }
    
    public synchronized void setAllowGridmapFiles(boolean allowGridmapFiles) {
		this.allowGridmapFiles = allowGridmapFiles;
	}

	public synchronized void setBannedUserGroups(String bannedUserGroups) {
		this.bannedUserGroups = bannedUserGroups;
		if (bannedUserGroups!=null && bannedUserGroups.length()>0)
			this.bannedUserGroupList = Collections.synchronizedList(Arrays.asList(bannedUserGroups.split(",")));
		else
			this.bannedUserGroupList = Collections.synchronizedList(new ArrayList());
	}
	
	/**
     * @return
     */
   
    public String toXml() throws IOException {
    	StringWriter writer = new StringWriter();
    	write(writer);
    	return writer.toString();
    }
	
	public synchronized void write(Writer out) throws IOException {
		out.write("<?xml version='1.0' encoding='UTF-8'?>\n\n");

		out.write("<gums version='"+version+"' "
				+"allowGridmapFiles='"+(getAllowGridmapFiles()?"true":"false")+"' "
				+"bannedUserGroups='"+getBannedUserGroups()+"'"
				+">\n\n");

		// Write persistence factories
		if( getPersistenceFactories().size()>0 ) {
			out.write("\t<persistenceFactories>\n\n");
			Iterator it = getPersistenceFactories().values().iterator();
			while( it.hasNext() ) {
				PersistenceFactory persistenceFactory = (PersistenceFactory)it.next();
				out.write( persistenceFactory.toXML() );
			}
			out.write("\t</persistenceFactories>\n\n");
		}

		// Write Voms Servers
		if( getVomsServers().size()>0 ) {
			out.write("\t<vomsServers>\n\n");
			Iterator it = getVomsServers().values().iterator();
			while( it.hasNext() ) {
				VomsServer vo = (VomsServer)it.next();
				out.write( vo.toXML() );
			}
			out.write("\t</vomsServers>\n\n");
		}           

		// Write User Groups
		if( getUserGroups().size()>0 ) {
			out.write("\t<userGroups>\n\n");
			Iterator it = getUserGroups().values().iterator();
			while( it.hasNext() ) {
				UserGroup userGroup = (UserGroup)it.next();
				out.write( userGroup.toXML() );
			}
			out.write("\t</userGroups>\n\n");
		}                

		// Write Account Mappers
		if( getAccountMappers().size()>0 ) {
			out.write("\t<accountMappers>\n\n");
			Iterator it = getAccountMappers().values().iterator();
			while( it.hasNext() ) {
				AccountMapper accountMapper = (AccountMapper)it.next();
				out.write( accountMapper.toXML() );
			}
			out.write("\t</accountMappers>\n\n");
		}             

		// Write Group To Account Mappings
		if( getGroupToAccountMappings().size()>0 ) {
			out.write("\t<groupToAccountMappings>\n\n");
			Iterator it = getGroupToAccountMappings().values().iterator();
			while( it.hasNext() ) {
				GroupToAccountMapping groupToAccountMapping = (GroupToAccountMapping)it.next();
				out.write( groupToAccountMapping.toXML() );
			}
			out.write("\t</groupToAccountMappings>\n\n");
		}                

		// Write Host To Group Mappings
		if( getHostToGroupMappings().size()>0 ) {
			out.write("\t<hostToGroupMappings>\n\n");
			Iterator it = getHostToGroupMappings().iterator();
			while( it.hasNext() ) {
				HostToGroupMapping hostToGroupMapping = (HostToGroupMapping)it.next();
				out.write( hostToGroupMapping.toXML() );
			}
			out.write("\t</hostToGroupMappings>\n\n");
		}                

		out.write("</gums>");   	
    }
	 
    public void mergeConfiguration(Configuration configuraton, String persistenceFactory, String hostToGroupMapping) {
    	Iterator it;
    	
    	HostToGroupMapping h2GMapping = getHostToGroupMapping(hostToGroupMapping);
    	if (h2GMapping==null)
    		throw new RuntimeException("Host to Group Mapping '" + hostToGroupMapping + "' does not exist");
    	List groupToAccountMappingNames = h2GMapping.getGroupToAccountMappings();

    	PersistenceFactory persFact = getPersistenceFactory(persistenceFactory);
    	if (persFact==null)
    		throw new RuntimeException("Persistence Factory '" + persistenceFactory + "' does not exist");
    	
    	// Merge VOMS servers
		it = configuraton.getVomsServers().values().iterator();
		while( it.hasNext() ) {
			VomsServer newVomsServer = (VomsServer)it.next();
			try {
				Method m = newVomsServer.getClass().getDeclaredMethod("setPersistenceFactory", new Class[]{ Class.forName("java.lang.String") });
				m.invoke(newVomsServer, new Object[]{persistenceFactory});
			} catch (Exception e) {}
			vomsServers.put(newVomsServer.getName(), newVomsServer);
		}
    	
		// Merge VOMS servers
    	it = configuraton.getUserGroups().values().iterator();
		while( it.hasNext() ) {
			UserGroup newUserGroup = (UserGroup)it.next();
			userGroups.put(newUserGroup.getName(), newUserGroup);
			try {
				Method m = newUserGroup.getClass().getDeclaredMethod("setPersistenceFactory", new Class[]{ Class.forName("java.lang.String") });
				m.invoke(newUserGroup, new Object[]{persistenceFactory});
			} catch (Exception e) {}
		}
		
		// Merge Account Mappers
		it = configuraton.getAccountMappers().values().iterator();
		while( it.hasNext() ) {
			AccountMapper newAccountMapper = (AccountMapper)it.next();
			try {
				Method m = newAccountMapper.getClass().getDeclaredMethod("setPersistenceFactory", new Class[]{ Class.forName("java.lang.String") });
				m.invoke(newAccountMapper, new Object[]{persistenceFactory});
			} catch (Exception e) {}
			accountMappers.put(newAccountMapper.getName(), newAccountMapper);
		}
		
		// Merge Group to Account Mappings
		it = configuraton.getGroupToAccountMappings().values().iterator();
		while( it.hasNext() ) {
			GroupToAccountMapping newGroupToAccountMapping = (GroupToAccountMapping)it.next();
			try {
				Method m = newGroupToAccountMapping.getClass().getDeclaredMethod("setPersistenceFactory", new Class[]{ Class.forName("java.lang.String") });
				m.invoke(newGroupToAccountMapping, new Object[]{persistenceFactory});
			} catch (Exception e) {}
			groupToAccountMappings.put(newGroupToAccountMapping.getName(), newGroupToAccountMapping);
				
			// Add to hostToGroupMapping if it doesn't exist already
			boolean nameFound = false;
			Iterator g2aIt = groupToAccountMappingNames.iterator();
			while (g2aIt.hasNext())
			{
				String name = (String)g2aIt.next();
				if (name.equals(newGroupToAccountMapping.getName()))
				{
					nameFound = true;
					break;
				}
			}
			if (!nameFound)
				h2GMapping.addGroupToAccountMapping(newGroupToAccountMapping.getName());
		}
		
    }

}
