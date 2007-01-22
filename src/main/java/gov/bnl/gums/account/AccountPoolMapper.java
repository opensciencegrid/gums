/*
 * AccountPoolMapper.java
 *
 * Created on June 16, 2004, 3:10 PM
 */

package gov.bnl.gums.account;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import gov.bnl.gums.configuration.Configuration;
import gov.bnl.gums.db.HibernateMapping;
import gov.bnl.gums.db.AccountPoolMapperDB;
import gov.bnl.gums.groupToAccount.GroupToAccountMapping;
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
    private String persistenceFactory = "";
    private String accountPool = "";
    
    public AccountPoolMapper() {
    	super();
    }
    
    public AccountPoolMapper(Configuration configuration, String name) {
    	super(configuration, name);
    }
    
    public String getAccountPool() {
    	return accountPool;
    }
    
    public void setAccountPool(String accountPool) {
    	this.accountPool = accountPool;
    }

    public String mapUser(String userDN) {
        String account = getDB().retrieveAccount(userDN);
        if (account != null) return account;
        return getDB().assignAccount(userDN);
    }    
    
    private AccountPoolMapperDB getDB() {
    	if (db==null)
    		db = getConfiguration().getPersistenceFactory(persistenceFactory).retrieveAccountPoolMapperDB( accountPool );
    	return db;
    }
    
    public String getPersistenceFactory() {
       return persistenceFactory;
    }
    
    public void setPersistenceFactory(String persistenceFactory) {
        this.persistenceFactory = persistenceFactory;
    }
    
    public String toXML() {
    	return super.toXML() +
			"\t\t\tpersistenceFactory='"+persistenceFactory+"'\n" +
    		"\t\t\taccountPool='"+accountPool+"'/>\n\n";
    }
    
    public String getSummary(String bgColor) {
    	return "<td bgcolor=\""+bgColor+"\">" + getName() + "</td><td bgcolor=\""+bgColor+"\">" + persistenceFactory + "</td>";
    }
    
    public Object clone() {
    	AccountPoolMapper accountMapper = new AccountPoolMapper(getConfiguration(), getName());
    	accountMapper.setAccountPool(accountPool);
    	accountMapper.setPersistenceFactory(persistenceFactory);
    	return accountMapper;
    }
}
