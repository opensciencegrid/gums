/*
 * ManualAccountMapper.java
 *
 * Created on May 25, 2004, 5:06 PM
 */

package gov.bnl.gums.account;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import gov.bnl.gums.configuration.Configuration;
import gov.bnl.gums.db.HibernateMapping;
import gov.bnl.gums.db.ManualAccountMapperDB;
import gov.bnl.gums.persistence.PersistenceFactory;

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
    
    public String mapUser(String userDN) {
        return getDB().retrieveMapping(userDN);
    }
    
    public void createMapping(String userDN, String account) {
    	getDB().createMapping(userDN, account);
    }
    
    public boolean removeMapping(String userDN) {
        return getDB().removeMapping(userDN);
    }
    
    public java.util.List getMappings() {
    	return getDB().retrieveMappings();
    }
    
    public String getPersistenceFactory() {
        return persistenceFactory;
    }
    
    private ManualAccountMapperDB getDB() {
    	if (db==null)
    		db = getConfiguration().getPersistenceFactory(persistenceFactory).retrieveManualAccountMapperDB( getName() );
    	return db;
    }
    
    public void setPersistenceFactory(String persistanceFactory) {
        this.persistenceFactory = persistanceFactory;
    }  
    
    public String toString(String bgColor) {
    	return "<td bgcolor=\""+bgColor+"\">" + getName() + "</td><td bgcolor=\""+bgColor+"\">" + persistenceFactory + "</td>";
    }

    public String toXML() {
    	return super.toXML() +
			"\t\t\tpersistenceFactory='"+persistenceFactory+"'/>\n\n";
    }      
    
    public AccountMapper clone(Configuration configuration) {
    	ManualAccountMapper accountMapper = new ManualAccountMapper(configuration, getName());
    	accountMapper.setPersistenceFactory(persistenceFactory);
    	return accountMapper;
    }
}
