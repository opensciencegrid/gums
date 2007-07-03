/*
 * AccountPoolMapper.java
 *
 * Created on June 16, 2004, 3:10 PM
 */

package gov.bnl.gums.account;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import gov.bnl.gums.GUMS;
import gov.bnl.gums.configuration.Configuration;
import gov.bnl.gums.db.AccountPoolMapperDB;

/** 
 * Provides the mapping by assigning user accounts from a pool provided by
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
    
	private Log gumsResourceAdminLog = LogFactory.getLog(GUMS.resourceAdminLog);
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
    
    public String getAccountPoolRoot() {
    	int index = accountPool.indexOf(".");
    	if (index != -1)
    		return accountPool.substring(0, index);
    	else
    		return accountPool;
    }

    public AccountPoolMapperDB getDB() {
    	if (db==null)
    		db = getConfiguration().getPersistenceFactory(persistenceFactory).retrieveAccountPoolMapperDB(accountPool);
    	return db;
    }    

    /**
     * @return String representation of how many accounts are assigned in database for each root account
     */
    public String getAssignments() {
    	String retStr = new String();
    	Map accountReverseMap = getDB().retrieveReverseAccountMap();
    	TreeMap accountRoots = new TreeMap();
    	Iterator it = accountReverseMap.keySet().iterator();
    	while (it.hasNext()) {
    		String account = (String)it.next();
    		String accountRoot = getRoot(account);
    		Integer[] stats = (Integer[])accountRoots.get(accountRoot);
    		if (stats==null) {
    			stats = new Integer[2];
    			stats[0] = new Integer(1); // total
   				stats[1] = new Integer(!accountReverseMap.get(account).equals("")?1:0); // assigned
    		} else {
    			stats[0] = new Integer(stats[0].intValue() + 1);
    			if (!accountReverseMap.get(account).equals(""))
    				stats[1] = new Integer(stats[1].intValue() + 1);
    		}
    		accountRoots.put(accountRoot, stats);
    	}
    	it = accountRoots.keySet().iterator();
    	while (it.hasNext()) {
    		String accountRoot = (String)it.next();
    		retStr += accountRoot + "(" + 
    			((Integer[])accountRoots.get(accountRoot))[1] + "/" + 
    			((Integer[])accountRoots.get(accountRoot))[0] + ")";
    		if (it.hasNext())
    			retStr += ", ";
    	}
    	return retStr;
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
        if (createIfDoesNotExist) {
        	String newAccount = getDB().assignAccount(userDN);
        	if (newAccount==null)
        		gumsResourceAdminLog.error("Could not assign user '"+userDN+"' to account within pool account mapper '"+getName()+"'.  The most likely cause is that there are no more available pool accounts, in which case you should add more.");
        	return newAccount;
        }
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
    	return "<td bgcolor=\""+bgColor+"\"><a href=\"accountMappers.jsp?command=edit&name=" + getName() + "\">" + getName() + "</a></td><td bgcolor=\""+bgColor+"\">" + getType() + "</td><td bgcolor=\""+bgColor+"\">" + getAssignments() + "</td>";
    }
    
    public String toXML() {
    	return "\t\t<accountPoolMapper\n"+
			"\t\t\tname='"+getName()+"'\n"+
			"\t\t\tdescription='"+getDescription()+"'\n"+
			"\t\t\tpersistenceFactory='"+persistenceFactory+"'\n" +
    		"\t\t\taccountPool='"+accountPool+"'/>\n\n";
    }
    
    private static String getRoot(String account) {
    	String upper = account.toUpperCase();
    	String lower = account.toLowerCase();
    	int i = 0, len = lower.length();
    	while (i<len && lower.charAt(i)!=upper.charAt(i))
    		i++;
    	return new String( account.substring(0,i) );
    }
}
