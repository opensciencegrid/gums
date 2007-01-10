/*
 * AccountMapper.java
 *
 * Created on March 30, 2004, 5:56 PM
 */

package gov.bnl.gums.account;

import java.util.List;
import java.util.Properties;

/** Defines the logic with which a user will be mapped to a local account.
 * As of now, the logic is a simple certificate subject mapped to a user
 * account. In the future this interface will be extended to map the credential
 * of a full proxy (DN, vo, role, group) to a user and group account.
 *
 * @author  Gabriele Carcassi
 */
public abstract class AccountMapper {
	private String name = "";
	
    /**
     * Maps a grid identity to a local account name.
     * @param userDN the certificate DN (i.e. '/DC=org/DC=doegrids/OU=People/CN=Gabriele Carcassi').
     * @return a user account (i.e. 'atlas').
     */
    public abstract String mapUser(String userDN);
    
    AccountMapper() {
    }
    
    AccountMapper(String name) {
    	this.name = name;
    }
    
    public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
    public String toXML() {
    	return "\t\t<accountMapper\n"+
    		"\t\t\tclassName='"+getClass().getName()+"'\n"+
    		"\t\t\tname='"+name+"'\n";
    }
}
