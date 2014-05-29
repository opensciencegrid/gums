/*
 * GroupAccountMapper.java
 *
 * Created on May 25, 2004, 2:10 PM
 */

package gov.bnl.gums.account;

import gov.bnl.gums.GridUser;
import gov.bnl.gums.AccountInfo;
import gov.bnl.gums.configuration.Configuration;

import org.apache.log4j.Logger;

/** 
 * An account mapping policy that maps all user to the same account.
 * <p>
 * To configure the policy one needs only to set the groupName property
 * as the desired group account.
 *
 * @author Gabriele Carcassi, Jay Packard
 */
public final class GroupAccountMapper extends AccountMapper {
    static public String getTypeStatic() {
		return "group";
	}
    
    private Logger log = Logger.getLogger(GroupAccountMapper.class);
    private String accountName = "";
    private String groupName = "";
    
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
    	GroupAccountMapper accountMapper = new GroupAccountMapper(configuration, new String(getName()));
    	accountMapper.setDescription(new String(getDescription()));
    	accountMapper.setAccountName(new String(accountName));
        accountMapper.setGroupName(new String(groupName));
    	return accountMapper;
    }

    public String getAccountName() {
        return accountName;
    }    

    public String getGroupName() {
        return groupName;
    }

    public String getType() {
        return "group";
    }

    @Override
    public AccountInfo mapUser(GridUser user, boolean createIfDoesNotExist) {
        
        if (log.isDebugEnabled()) {
            log.debug("User " + user.getCertificateDN() + " mapped to account " + accountName);
        }
        if (groupName != null && !groupName.equals("")) return new AccountInfo(accountName, groupName);
        return new AccountInfo(accountName);
    }

    public void setAccountName(String accountName) {
        if (this.accountName.length()>0)
            log.debug("Account name changed from  " + this.accountName + " to " + accountName);
        this.accountName = accountName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
    
    public String toString(String bgColor) {
    	return "<td bgcolor=\""+bgColor+"\"><a href=\"accountMappers.jsp?command=edit&name=" + getName() + "\">" + getName() + "</a></td><td bgcolor=\""+bgColor+"\">" + getType() + "</td><td bgcolor=\""+bgColor+"\">" + accountName + "</td>";
    }      
    
    public String toXML() {
    	String retStr = "\t\t<groupAccountMapper\n"+
			"\t\t\tname='"+getName()+"'\n"+
			"\t\t\tdescription='"+getDescription()+"'\n"+
			"\t\t\taccountName='"+accountName+"'\n";
        if (groupName != null && !groupName.equals(""))
            retStr += "\t\t\tgroupName='"+groupName+"'\n";

        if (retStr.charAt(retStr.length()-1)=='\n') {
            retStr = retStr.substring(0, retStr.length()-1);
        }
        retStr += "/>\n\n";
        return retStr;
    }
}
