/*
 * MockAccountMapper.java
 *
 * Created on May 24, 2004, 2:42 PM
 */

package gov.bnl.gums.account;

import gov.bnl.gums.GridUser;
import gov.bnl.gums.configuration.Configuration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author  carcassi
 */
public class MockAccountMapper extends AccountMapper {
    
    public MockAccountMapper() {
    	super();
    }
	
	public MockAccountMapper(Configuration configuration, String name) {
    	super(configuration, name);
    }
    
    public String mapUser(String userDN) {
        if (userDN.equals("/DC=org/DC=doegrids/OU=People/CN=Gabriele Carcassi"))
            return "carcassi";
        return null;
    }
    
    public boolean containsMap(String userDN, String accountName) {
    	return userDN.equals("/DC=org/DC=doegrids/OU=People/CN=Gabriele Carcassi") && accountName.equals("carcassi");
    }
    
    public List inverseMap(String accountName) {
        if (accountName.equals("carcassi")) {
        	ArrayList users = new ArrayList();
        	GridUser user = new GridUser();
        	user.setCertificateDN("/DC=org/DC=doegrids/OU=People/CN=Gabriele Carcassi");
        	users.add(user);
            return users;
        }
        return null;
    }
    
    public String toString(String bgColor) {
    	return "";
    }
    
    public AccountMapper clone(Configuration configuration) {
    	return null;
    }
}
