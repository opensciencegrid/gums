/*
 * GroupAccountMapper.java
 *
 * Created on May 25, 2004, 2:10 PM
 */

package gov.bnl.gums.account;

import gov.bnl.gums.configuration.Configuration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/** An account mapping policy that maps all user to the same account.
 * <p>
 * To configure the policy one needs only to set the groupName property
 * as the desired group account.
 *
 * @author  Gabriele Carcassi
 */
public class GroupAccountMapper extends AccountMapper {
    private Log log = LogFactory.getLog(GroupAccountMapper.class);
    private String accountName = "";
    
	static public String getType() {
		return "group";
	}
    
    public GroupAccountMapper() {
    	super();
    }

    public GroupAccountMapper(Configuration configuration) {
    	super(configuration);
    }
    
    public GroupAccountMapper(Configuration configuration, String name) {
    	super(configuration, name);
    }

    public AccountMapper clone(Configuration configuration) {
    	GroupAccountMapper accountMapper = new GroupAccountMapper(configuration, getName());
    	accountMapper.setAccountName(accountName);
    	return accountMapper;
    }
	
	public String getAccountName() {
		return accountName;
	}    
    
    public String mapUser(String userDN) {
        
        if (log.isDebugEnabled()) {
            log.debug("User " + userDN + " mapped to account " + accountName);
        }
        
        return accountName;
    }

    public void setAccountName(String accountName) {
    	log.debug("GroupName changed from  " + this.accountName + " to " + accountName);
		this.accountName = accountName;
	}
    
    public String toString(String bgColor) {
    	return "<td bgcolor=\""+bgColor+"\">" + getName() + "</td><td bgcolor=\""+bgColor+"\"></td>";
    }      
    
    public String toXML() {
    	return "\t\t<groupAccountMapper\n"+
			"\t\t\tname='"+getName()+"'\n"+
			"\t\t\taccountName='"+accountName+"'/>\n\n";
    }
}
