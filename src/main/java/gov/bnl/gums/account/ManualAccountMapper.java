/*
 * ManualAccountMapper.java
 *
 * Created on May 25, 2004, 5:06 PM
 */

package gov.bnl.gums.account;

import gov.bnl.gums.configuration.Configuration;
import gov.bnl.gums.db.ManualAccountMapperDB;

/** An account mapping policy that looks at a stored table to determine the
 * account. The database implementation is an abstract interface, which
 * allows the mapping to be persistent on different mediums (i.e. database,
 * LDAP, file, ...)
 * <p>
 * This class will provide also configurable data caching.
 *
 * @author  Gabriele Carcassi
 */
public class ManualAccountMapper extends AccountMapper {
    static public String getTypeStatic() {
		return "manual";
	}
    
    private ManualAccountMapperDB db;
	private String persistenceFactory = "";
    
    public ManualAccountMapper() {
    	super();
    }
 
    public ManualAccountMapper(Configuration configuration) {
    	super(configuration);
    }
    
    public ManualAccountMapper(Configuration configuration, String name) {
    	super(configuration, name);
    }
    
    public AccountMapper clone(Configuration configuration) {
    	ManualAccountMapper accountMapper = new ManualAccountMapper(configuration, getName());
    	accountMapper.setDescription(getDescription());
    	accountMapper.setPersistenceFactory(persistenceFactory);
    	return accountMapper;
    }
    
    public void createMapping(String userDN, String account) {
    	getDB().createMapping(userDN, account);
    }
    
    public java.util.List getMappings() {
    	return getDB().retrieveMappings();
    }
    
    public String getPersistenceFactory() {
        return persistenceFactory;
    }
    
    public String getType() {
		return "manual";
	}
    
    public String mapUser(String userDN) {
        return getDB().retrieveMapping(userDN);
    }
    
    public boolean removeMapping(String userDN) {
        return getDB().removeMapping(userDN);
    }
    
    public void setPersistenceFactory(String persistanceFactory) {
        this.persistenceFactory = persistanceFactory;
    }  
    
    public String toString(String bgColor) {
    	return "<td bgcolor=\""+bgColor+"\">" + getName() + "</td><td bgcolor=\""+bgColor+"\">" + persistenceFactory + "</td>";
    }

    public String toXML() {
    	return "\t\t<manualAccountMapper\n"+
			"\t\t\tname='"+getName()+"'\n"+
			"\t\t\tdescription='"+getDescription()+"'\n"+
			"\t\t\tpersistenceFactory='"+persistenceFactory+"'/>\n\n";
    }      
    
    private ManualAccountMapperDB getDB() {
    	if (db==null)
    		db = getConfiguration().getPersistenceFactory(persistenceFactory).retrieveManualAccountMapperDB( getName() );
    	return db;
    }
}
