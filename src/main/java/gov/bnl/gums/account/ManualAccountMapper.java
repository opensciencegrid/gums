/*
 * ManualAccountMapper.java
 *
 * Created on May 25, 2004, 5:06 PM
 */

package gov.bnl.gums.account;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
    private PersistenceFactory persistenceFactory;
    
    public String mapUser(String userDN) {
        return db.retrieveMapping(userDN);
    }
    
    public void createMapping(String userDN, String account) {
        db.createMapping(userDN, account);
    }
    
    public boolean removeMapping(String userDN) {
        return db.removeMapping(userDN);
    }
    
    public java.util.List getMappings() {
    	return db.retrieveMappings();
    }
    
    public String getPersistenceFactory() {
        return (persistenceFactory!=null ? persistenceFactory.getName() : "");
    }
    
    public void setPersistenceFactory(PersistenceFactory persistanceFactory) {
        this.persistenceFactory = persistanceFactory;
        db = persistanceFactory.retrieveManualAccountMapperDB( getName() );
    }  
    
    public String toXML() {
    	return super.toXML() +
			"\t\t\tpersistenceFactory='"+persistenceFactory.getName()+"'/>\n\n";
    }      
}
