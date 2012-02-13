/*
 * MockAccountPoolMapperDB.java
 *
 * Created on June 14, 2004, 3:28 PM
 */

package gov.bnl.gums.db;

import gov.bnl.gums.GridUser;

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
    
    public void addAccount(String account) {
        if ((freeAccounts.contains(account))
        || (userToAccount.values().contains(account))) {
            throw new IllegalArgumentException("The account is already in the pool");
        }
        freeAccounts.add(account);
    }
    
    public String assignAccount(GridUser user) {
        if (freeAccounts.size() == 0) return null;
        if (userToAccount.get(user.getCertificateDN()) != null)
            throw new IllegalArgumentException("The user is already mapped to an account");
        
        String account = (String) freeAccounts.remove(0);
        userToAccount.put(user.getCertificateDN(), account);
        userToLastDate.put(user.getCertificateDN(), new Date());
        
        return account;
    }
    
    public String getMap() {
    	return "";
    }
    
    public boolean needsCacheRefresh() {
    	return true;
    }
    
    public boolean removeAccount(String account) {
    	Iterator it = freeAccounts.iterator();
    	boolean wasRemoved = false;
    	while (it.hasNext()) {
    		String itAccount = (String)it.next();
    		if (itAccount.equals(account)) {
    			freeAccounts.remove(itAccount);
    			Iterator it2 = userToAccount.keySet().iterator();
    			while (it2.hasNext()) {
    				String userDN = (String)it2.next();
    				if (userToAccount.get(userDN).equals(itAccount) ) {
    					userToAccount.remove(userDN);
    					userToLastDate.remove(userDN);
    				}
    			}
    			wasRemoved = true;
    			break;
    		}
    	}
    	return wasRemoved;
    }
    
    public String retrieveAccount(GridUser user) {
    	String userDN = user.getCertificateDN();
        userToLastDate.put(userDN, new Date());
        return (String) userToAccount.get(userDN);
    }
    
    public java.util.Map retrieveAccountMap() {
        return Collections.unmodifiableMap(userToAccount);
    }
    
    public java.util.Map retrieveReverseAccountMap() {
    	Map reverseMap = new Hashtable();
    	Iterator it = userToAccount.keySet().iterator();
    	while (it.hasNext()) {
    		String user = (String)it.next();
    		reverseMap.put(userToAccount.get(user), user);
    	}
    	it = freeAccounts.iterator();
    	while (it.hasNext()) {
    		String account = (String)it.next();
    		reverseMap.put(account, "");
    	}
        return reverseMap;
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
 
    public void setCacheRefreshed() {
    }
    
    public void unassignAccount(String account) {
    	String user = (String)retrieveReverseAccountMap().get(account);
    	if (!user.equals(""))
    		unassignUser( user );
    }
    
    public void unassignUser(String user) {
        freeAccounts.add(userToAccount.remove(user));
        userToLastDate.remove(user);
    }
    
}
