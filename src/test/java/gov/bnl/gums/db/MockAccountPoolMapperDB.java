/*
 * MockAccountPoolMapperDB.java
 *
 * Created on June 14, 2004, 3:28 PM
 */

package gov.bnl.gums.db;

import java.util.*;

/**
 *
 * @author  carcassi
 */
public class MockAccountPoolMapperDB implements AccountPoolMapperDB {
    
    public List freeAccounts = new LinkedList();
    public Map userToAccount = new Hashtable();
    public Map userToLastDate = new Hashtable();
    
    public MockAccountPoolMapperDB() {
        this(10);
    }
    
    public MockAccountPoolMapperDB(int nAccounts) {
        for (int i = 0; i < nAccounts; i++) {
            freeAccounts.add("pool"+i);
        }
    }
    
    public String assignAccount(String userDN) {
        if (freeAccounts.size() == 0) return null;
        if (userToAccount.get(userDN) != null)
            throw new IllegalArgumentException("The user is already mapped to an account");
        
        String account = (String) freeAccounts.remove(0);
        userToAccount.put(userDN, account);
        userToLastDate.put(userDN, new Date());
        
        return account;
    }
    
    public boolean removeAccount(String account) {
    	Iterator it = freeAccounts.iterator();
    	boolean wasRemoved = false;
    	while (it.hasNext()) {
    		String itAccount = (String)it.next();
    		if (itAccount.equals(account)) {
    			freeAccounts.remove(itAccount);
    			wasRemoved = true;
    			break;
    		}
    	}
    	return wasRemoved;
    }
    
    public String retrieveAccount(String userDN) {
        userToLastDate.put(userDN, new Date());
        return (String) userToAccount.get(userDN);
    }
    
    public java.util.Map retrieveAccountMap() {
        return Collections.unmodifiableMap(userToAccount);
    }
    
    public List retrieveUsersNotUsedSince(java.util.Date date) {
        Iterator users = userToLastDate.keySet().iterator();
        List oldUsers = new ArrayList();
        while (users.hasNext()) {
            String user = (String) users.next();
            Date lastUsed = (Date) userToLastDate.get(user);
            if (lastUsed.before(date))
                oldUsers.add(user);
        }
        return Collections.unmodifiableList(oldUsers);
    }
    
    public List retrieveUsersForAccount(String accountName) {
        Iterator usersIt = userToLastDate.keySet().iterator();
        List users = new ArrayList();
        while (usersIt.hasNext()) {
            String user = (String) usersIt.next();
            if(userToAccount.get(user).equals(accountName))
            	users.add(user);
        }
        return Collections.unmodifiableList(users);   	
    }
    
    public void unassignUser(String user) {
        freeAccounts.add(userToAccount.remove(user));
        userToLastDate.remove(user);
    }
    
    public void unassignAllUsers() {
        freeAccounts.add(userToAccount.values());
        userToAccount.clear();
        userToLastDate.clear();
    }
    
    public void addAccount(String account) {
        if ((freeAccounts.contains(account))
        || (userToAccount.values().contains(account))) {
            throw new IllegalArgumentException("The account is already in the pool");
        }
        freeAccounts.add(account);
    }
    
    public int getNumberUnassignedMappings() {
    	return freeAccounts.size();
    }
    
}
