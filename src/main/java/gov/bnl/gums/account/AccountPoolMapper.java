/*
 * AccountPoolMapper.java
 *
 * Created on June 16, 2004, 3:10 PM
 */

package gov.bnl.gums.account;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import gov.bnl.gums.db.HibernateMapping;
import gov.bnl.gums.db.AccountPoolMapperDB;
import gov.bnl.gums.persistence.PersistenceFactory;

/** Provides the mapping by assigning user accounts from a pool provided by
 * the AccountPoolMapperDB.
 * <p>
 * The accounts are mapped when the mapUser function is called for that
 * particular user. Accounts are never deleted from the pool
 *
 * @todo should implement caching?
 * @author  Gabriele Carcassi
 */
public class AccountPoolMapper extends AccountMapper {
    
    private AccountPoolMapperDB db;
    private PersistenceFactory persistenceFactory;
    private String accountPool = "";
    
    public String getAccountPool() {
    	return accountPool;
    }
    
    public void setAccountPool(String accountPool) {
    	this.accountPool = accountPool;
    }

    public String mapUser(String userDN) {
        String account = db.retrieveAccount(userDN);
        if (account != null) return account;
        return db.assignAccount(userDN);
    }    
    
    public boolean containsMap(String userDN, String accountName) {
    	Map mappings = db.retrieveAccountMap();
    	return mappings.get(userDN).equals(accountName);
    }
    
    public String getPersistenceFactory() {
       return (persistenceFactory!=null ? persistenceFactory.getName() : "");
    }
    
    public void setPersistenceFactory(PersistenceFactory setPersistenceFactory) {
        this.persistenceFactory = setPersistenceFactory;
        db = persistenceFactory.retrieveAccountPoolMapperDB( accountPool );
    }
    
    public String toXML() {
    	return super.toXML() +
			"\t\t\tpersistenceFactory='"+persistenceFactory.getName()+"'\n" +
    		"\t\t\taccountPool='"+accountPool+"'/>\n\n";
    }
}
