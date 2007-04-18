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
 * @author  Gabriele Carcassi, Jay Packard
 */
public class AccountPoolMapper extends AccountMapper {
    static public String getTypeStatic() {
		return "pool";
	}
    
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
    	accountMapper.setDescription(getDescription());
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
 
    public String getType() {
		return "pool";
	}
        
    public String mapUser(String userDN, boolean createIfDoesNotExist) {
        String account = getDB().retrieveAccount(userDN);
        if (account != null) return account;
        if (createIfDoesNotExist)
        	return getDB().assignAccount(userDN);
        else
        	return null;
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
    	return "\t\t<accountPoolMapper\n"+
			"\t\t\tname='"+getName()+"'\n"+
			"\t\t\tdescription='"+getDescription()+"'\n"+
			"\t\t\tpersistenceFactory='"+persistenceFactory+"'\n" +
    		"\t\t\taccountPool='"+accountPool+"'/>\n\n";
    }
    
    private AccountPoolMapperDB getDB() {
    	if (db==null)
    		db = getConfiguration().getPersistenceFactory(persistenceFactory).retrieveAccountPoolMapperDB( accountPool );
    	return db;
    }
}
