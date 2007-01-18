/*
 * GroupAccountMapper.java
 *
 * Created on May 25, 2004, 2:10 PM
 */

package gov.bnl.gums.account;



import java.util.List;

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
    
    public GroupAccountMapper() {
    }
    
    public GroupAccountMapper(String name) {
    	super(name);
    }

    public void setAccountName(String accountName) {
    	log.debug("GroupName changed from  " + this.accountName + " to " + accountName);
		this.accountName = accountName;
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

    public String toXML() {
    	return super.toXML() +
			"\t\t\taccountName='"+accountName+"'/>\n\n";
    }      
    
    public String getSummary(String bgColor) {
    	return "<td bgcolor=\""+bgColor+"\">" + getName() + "</td><td bgcolor=\""+bgColor+"\"></td>";
    }
}
