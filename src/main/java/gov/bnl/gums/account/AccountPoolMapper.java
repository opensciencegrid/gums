/*
 * AccountPoolMapper.java
 *
 * Created on June 16, 2004, 3:10 PM
 */

package gov.bnl.gums.account;

import gov.bnl.gums.configuration.Configuration;
import gov.bnl.gums.db.AccountPoolMapperDB;

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
 
    public AccountPoolMapper(Configuration configuration) {
    	super(configuration);
    }
    
    public AccountPoolMapper(Configuration configuration, String name) {
    	super(configuration, name);
    }
    
    public AccountMapper clone(Configuration configuration) {
    	AccountPoolMapper accountMapper = new AccountPoolMapper(configuration, getName());
    	accountMapper.setAccountPool(accountPool);
    	accountMapper.setPersistenceFactory(persistenceFactory);
    	return accountMapper;
    }
    
    public String getAccountPool() {
    	return accountPool;
    }

    public int getNumberAssigned() {
    	return getDB().retrieveAccountMap().values().size();
    }    
    
    public int getNumberUnassigned() {
    	return getDB().getNumberUnassignedMappings();
    }

    public String getPersistenceFactory() {
       return persistenceFactory;
    }
    
    public String mapUser(String userDN) {
        String account = getDB().retrieveAccount(userDN);
        if (account != null) return account;
        return getDB().assignAccount(userDN);
    }
    
    public void setAccountPool(String accountPool) {
    	this.accountPool = accountPool;
    }
    
    public void setPersistenceFactory(String persistenceFactory) {
        this.persistenceFactory = persistenceFactory;
    }
    
    public String toString(String bgColor) {
    	return "<td bgcolor=\""+bgColor+"\">" + getName() + "</td><td bgcolor=\""+bgColor+"\">" + persistenceFactory + "</td>";
    }

    public String toXML() {
    	return super.toXML() +
			"\t\t\tpersistenceFactory='"+persistenceFactory+"'\n" +
    		"\t\t\taccountPool='"+accountPool+"'/>\n\n";
    }
    
    private AccountPoolMapperDB getDB() {
    	if (db==null)
    		db = getConfiguration().getPersistenceFactory(persistenceFactory).retrieveAccountPoolMapperDB( accountPool );
    	return db;
    }
}
