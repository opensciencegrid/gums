/*
 * Group2AccountMapping.java
 *
 * Created on May 24, 2004, 2:36 PM
 */

package gov.bnl.gums.groupToAccount;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import gov.bnl.gums.account.AccountMapper;
import gov.bnl.gums.userGroup.UserGroup;

/** This class defines which mapping policy should be used for the given group.
 * It tells that a given user group (all the members of the 'usatlas'
 * group in the 'atlas' LDAP vo) should be mapped using a given mapping policy
 * (a composite made of a NISAccountMapper and GroupAccountMapper, meaning
 * each user should be mapped to his account if exists, otherwise the generic
 * group account should be used).
 * <p>
 * This class is used by the Hostname mapping to hold a series of group/accountmapping
 * pairs.
 *
 * @author  Gabriele Carcassi
 */
public class GroupToAccountMapping {
    private ArrayList userGroups = new ArrayList();
    private ArrayList accountMappers = new ArrayList();
    private String name = "";
    private String accountingVo = "";
    private String accountingDesc = "";

	public GroupToAccountMapping() {
	}    
    
	public GroupToAccountMapping(String name) {
		this.name = name;
	}
	
    /**
     * Setter for property name.
     * @return Value of property name.
     */	
	public void setName(String name) {
		this.name = name;
	}
	
    /**
     * Getter for property name.
     * @return Value of property name.
     */
    public String getName() {
        return (name!=null ? name : "");
    }
    
    /**
     * Getter for property group.
     * @return Value of property group.
     */
    public ArrayList getUserGroups() {
        return this.userGroups;
    }    
    
    /**
     * Setter for property group.
     * @param group New value of property group.
     */
    public void addUserGroup(UserGroup userGroup) {
    	userGroups.add(userGroup);
    }    
    
    /**
     * @return returns true if userGroup is matched.
     */
    public boolean containsUserGroup(String userGroupName) {
    	Iterator userGroupIt = userGroups.iterator();
    	while(userGroupIt.hasNext()) {
    		UserGroup userGroup = (UserGroup)userGroupIt.next();
    		if(userGroup.getName().equals(userGroupName))
    			return true;
    	}
    	return false;
    }    
    
    /**
     * Getter for property mapper.
     * @return Value of property mapper.
     */
    public ArrayList getAccountMappers() {
        return accountMappers;
    }
    
    /**
     * Setter for property mapper.
     * @param mapper New value of property mapper.
     */
    public void addAccountMapper(AccountMapper accountMapper) {
        accountMappers.add(accountMapper);
    }
    
    /**
     * Getter for property accountingVo.
     * @return Value of property accountingVo.
     */
    public String getAccountingVo()  {

        return this.accountingVo;
    }
    
    /**
     * Setter for property accountingVo.
     * @param accountingVo New value of property accountingVo.
     */
    public void setAccountingVo(java.lang.String accountingVo)  {

        this.accountingVo = accountingVo;
    }

    /**
     * Getter for property accountingDesc.
     * @return Value of property accountingDesc.
     */
    public String getAccountingDesc() {

        return this.accountingDesc;
    }

    /**
     * Setter for property accountingDesc.
     * @param accountingDesc New value of property accountingDesc.
     */
    public void setAccountingDesc(String accountingDesc) {

        this.accountingDesc = accountingDesc;
    }
    
    public String toXML() {
    	String retStr = "\t\t<groupToAccountMapping\n"+
		"\t\t\tname='"+name+"'\n"+
		"\t\t\taccountingVo='"+accountingVo+"'\n"+
		"\t\t\taccountingDesc = '"+accountingDesc+"'\n"+
		"\t\t\tuserGroups='";

	    List userGroups = getUserGroups();
		Iterator it = (Iterator)userGroups.iterator();
		while(it.hasNext()) {
			UserGroup userGroup = (UserGroup)it.next();
			retStr += userGroup.getName() + (it.hasNext()?", ":"");
		}
		
		retStr += "'\n";
		
		retStr += "\t\t\taccountMappers='";
	    
		List accountMappers = getAccountMappers();
		it = (Iterator)accountMappers.iterator();
		while(it.hasNext()) {
			AccountMapper accountMapper = (AccountMapper)it.next();
			retStr += accountMapper.getName() + (it.hasNext()?", ":"");
		}
		
		retStr += "'/>\n\n";
		
		return retStr;
    }
    
}
