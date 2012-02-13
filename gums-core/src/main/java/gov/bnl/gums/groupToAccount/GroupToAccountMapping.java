/*
 * Group2AccountMapping.java
 *
 * Created on May 24, 2004, 2:36 PM
 */

package gov.bnl.gums.groupToAccount;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import gov.bnl.gums.configuration.Configuration;

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
 * @author Gabriele Carcassi, Jay Packard
 */
public class GroupToAccountMapping {
    private ArrayList userGroups = new ArrayList();
    private ArrayList accountMappers = new ArrayList();
    private String name = "";
	private String description = "";
    private String accountingVo = "";
    private String accountingVoSubgroup = "";
    private SoftReference configurationRef = null;

	/**
	 * Creates a GroupToAccountMapping object. This empty constructor is needed by the XML Digestor.
	 */
	public GroupToAccountMapping() {
	}    
    
	/**
	 * Creates a GroupToAccountMapping object with a configuration.
	 * 
	 * @param configuration
	 */
	public GroupToAccountMapping(Configuration configuration) {
		this.configurationRef = new SoftReference(configuration);
	}
	
	/**
	 * Creates a GroupToAccountMapping with a configuration and a name.
	 * 
	 * @param configuration
	 * @param name
	 */
	public GroupToAccountMapping(Configuration configuration, String name) {
		this.configurationRef = new SoftReference(configuration);
		this.name = name;
	}

	/**
     * Setter for property mapper.
     * 
     * @param mapper New value of property mapper.
     */
    public void addAccountMapper(String accountMapper) {
        accountMappers.add(accountMapper);
    }
	
	/**
     * Setter for property group.
     * 
     * @param group New value of property group.
     */
    public void addUserGroup(String userGroup) {
    	userGroups.add(userGroup);
    }
	
	/**
	 * Create a clone of itself for specified configuration.
	 * 
	 * @param configuration
	 * @return
	 */
    public GroupToAccountMapping clone(Configuration configuration) {
    	GroupToAccountMapping groupToAccountMapping = new GroupToAccountMapping(configuration, new String(name));
    	groupToAccountMapping.setDescription(new String(getDescription()));
    	groupToAccountMapping.setAccountingVoSubgroup(new String(accountingVoSubgroup));
    	groupToAccountMapping.setAccountingVo(new String(accountingVo));
    	Iterator it = getUserGroups().iterator();
    	while (it.hasNext())
    		groupToAccountMapping.addUserGroup( new String((String)it.next()) );
    	it = getAccountMappers().iterator();
    	while (it.hasNext())
    		groupToAccountMapping.addAccountMapper( new String((String)it.next()) );
    	return groupToAccountMapping;
    }
    
	/**
     * @return returns true if accountMapper is matched.
     */
    public boolean containsAccountMapper(String accountMapperQuery) {
    	Iterator accountMapperIt = accountMappers.iterator();
    	while(accountMapperIt.hasNext()) {
    		String accountMapper = (String)accountMapperIt.next();
    		if(accountMapper.equals(accountMapperQuery))
    			return true;
    	}
    	return false;
    }
    
	/**
     * @return returns true if userGroup is matched.
     */
    public boolean containsUserGroup(String userGroupQuery) {
    	Iterator userGroupIt = userGroups.iterator();
    	while(userGroupIt.hasNext()) {
    		String userGroup = (String)userGroupIt.next();
    		if(userGroup.equals(userGroupQuery))
    			return true;
    	}
    	return false;
    }
    
    /**
     * Getter for property accountingVo.
     * 
     * @return Value of property accountingVo.
     */
    public String getAccountingVo() {

        return this.accountingVo;
    }    
    
    /**
     * Getter for property accountingVoSubgroup.
     * 
     * @return Value of property accountingVoSubgroup.
     */
    public String getAccountingVoSubgroup()  {

        return this.accountingVoSubgroup;
    }    
    
    /**
     * Getter for property mapper.
     * 
     * @return Value of property mapper.
     */
    public ArrayList getAccountMappers() {
        return accountMappers;
    }    
    
    /**
     * Getter for property configuration.
     * 
     * @return Configuration object.
     */
    public Configuration getConfiguration() {
    	if (configurationRef == null)
    		return null;
		return (Configuration)configurationRef.get();
	}
    
    /**
     * Getter for property description.
     * 
     * @return Description as string.
     */
    public String getDescription() {
		return description;
	}
	
    /**
     * Getter for property name.
     * 
     * @return Value of property name.
     */
    public String getName() {
        return (name!=null ? name : "");
    }
    
    /**
     * Getter for property group.
     * 
     * @return Value of property group.
     */
    public ArrayList getUserGroups() {
        return this.userGroups;
    }
    
    /**
     * Setter for property accountingVo.
     * @param accountingVo New value of property y.
     */
    public void setAccountingVo(String accountingVo) {

        this.accountingVo = accountingVo;
    }

    /**
     * Setter for property accountingVo.
     * 
     * @param accountingVo New value of property accountingVo.
     */
    public void setAccountingVoSubgroup(java.lang.String accountingVoSubgroup)  {

        this.accountingVoSubgroup = accountingVoSubgroup;
    }

    /**
     * Setter for property configuration.
     * 
     * @param configuration.
     */
    public void setConfiguration(Configuration configuration) {
		this.configurationRef = new SoftReference(configuration);
	}
    
    /**
     * Setter for property description.
     * 
     * @param description.
     */
    public void setDescription(String description) {
    	this.description = description;
    }
    
    /**
     * Setter for property name.
     * 
     * @param name.
     */
    public void setName(String name) {
		this.name = name;
	}
    
    public String toString(String bgColor) {
    	return "<td bgcolor=\""+bgColor+"\"><a href=\"groupToAccountMappings.jsp?command=edit&name=" + getName() + "\">" + getName() + "</a></td><td bgcolor=\""+bgColor+"\">" + getAccountingVoSubgroup() + "&nbsp;</td><td bgcolor=\""+bgColor+"\">" + getAccountingVo() + "&nbsp;</td>";
    }
    
    /**
     * Get XML representation of this object for writing to gums.config
     * 
     * @return xml as string
     */
    public String toXML() {
    	String retStr = "\t\t<groupToAccountMapping\n"+
		"\t\t\tname='"+name+"'\n"+
		"\t\t\tdescription='"+getDescription()+"'\n"+
		"\t\t\taccountingVoSubgroup='"+accountingVoSubgroup+"'\n"+
		"\t\t\taccountingVo='"+accountingVo+"'\n"+
		"\t\t\tuserGroups='";

	    List userGroups = getUserGroups();
		Iterator it = (Iterator)userGroups.iterator();
		while(it.hasNext()) {
			String userGroup = (String)it.next();
			retStr += userGroup + (it.hasNext()?", ":"");
		}
		
		retStr += "'\n";
		
		retStr += "\t\t\taccountMappers='";
	    
		List accountMappers = getAccountMappers();
		it = (Iterator)accountMappers.iterator();
		while(it.hasNext()) {
			String accountMapper = (String)it.next();
			retStr += accountMapper + (it.hasNext()?", ":"");
		}
		
		retStr += "'/>\n\n";
		
		return retStr;
    }
    
}
