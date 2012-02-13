/*
 * MockManualAccountMapperDB.java
 *
 * Created on May 25, 2004, 5:12 PM
 */

package gov.bnl.gums.db;

import java.util.*;

/**
 *
 * @author  carcassi
 */
public class MockManualAccountMapperDB implements ManualAccountMapperDB {
    
    private Map map = new Hashtable();
    
    public void createMapping(String userDN, String account) {
        map.put(userDN, account);
    }
    
    public boolean removeMapping(String userDN) {
        return (map.remove(userDN) != null);
    }
    
    public String retrieveMapping(String userDN) {
        return (String) map.get(userDN);
    }
    
    public List retrieveUsersForAccount(String accountName) {
        Iterator usersIt = map.keySet().iterator();
        List users = new ArrayList();
        while (usersIt.hasNext()) {
            String user = (String) usersIt.next();
            if(map.get(user).equals(accountName))
            	users.add(user);
        }
        return Collections.unmodifiableList(users);   	
    }
    
    public Map retrieveAccountMap() {
    	return map;
    }    
    
    public Map retrieveReverseAccountMap() {
    	return null;
    }    
}
