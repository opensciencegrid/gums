/*
 * ManualAccountMapper.java
 *
 * Created on May 25, 2004, 5:06 PM
 */

package gov.bnl.gums.account;

import java.util.Map;

import gov.bnl.gums.GridUser;
import gov.bnl.gums.AccountInfo;
import gov.bnl.gums.configuration.Configuration;
import gov.bnl.gums.db.ManualAccountMapperDB;

/** 
 * An account mapping policy that looks at a stored table to determine the
 * account. The database implementation is an abstract interface, which
 * allows the mapping to be persistent on different mediums (i.e. database,
 * LDAP, file, ...)
 * <p>
 * This class will provide also configurable data caching.
 *
 * @author Gabriele Carcassi, Jay Packard
 */
public final class ManualAccountMapper extends AccountMapper {
    static public String getTypeStatic() {
		return "manual";
	}
    
    private ManualAccountMapperDB db;
    private String persistenceFactory = "";
    private String accountPool = "";
    private String groupName = "";
    
    public ManualAccountMapper() {
    	super();
    }
 
    public ManualAccountMapper(Configuration configuration) {
    	super(configuration);
    }
    
    public ManualAccountMapper(Configuration configuration, String name) {
    	super(configuration, name);
    }
    
    public void addMapping(String userDN, String account) {
    	getDB().createMapping(userDN, account);
    }
    
    public AccountMapper clone(Configuration configuration) {
    	ManualAccountMapper accountMapper = new ManualAccountMapper(configuration, new String(getName()));
    	accountMapper.setDescription(new String(getDescription()));
    	accountMapper.setPersistenceFactory(new String(persistenceFactory));
        accountMapper.setGroupName(new String(groupName));
        accountMapper.setAccountPool(new String(accountPool));
    	return accountMapper;
    }
    
    public void createMapping(String userDN, String account) {
    	getDB().createMapping(userDN, account);
    }
    
    public ManualAccountMapperDB getDB() {
    	if (db==null)
    		db = getConfiguration().getPersistenceFactory(persistenceFactory).retrieveManualAccountMapperDB( getAccountPool() );
    	return db;
    }
    
    public Map getAccountMap() {
    	return getDB().retrieveAccountMap();
    }
    
    public Map getReverseAccountMap() {
    	return getDB().retrieveReverseAccountMap();
    }
    
    public String getPersistenceFactory() {
        return persistenceFactory;
    }

    public String getAccountPool() {
	if (accountPool == null || accountPool.equals("")) return getName();
        return accountPool;
    }

    public String getGroupName() {
        return groupName;
    }

    public String getType() {
		return "manual";
	}

    @Override 
    public AccountInfo mapUser(GridUser user, boolean createIfDoesNotExist) {
        if (groupName != null && !groupName.equals("")) { return new AccountInfo(getDB().retrieveMapping(user.getCertificateDN()), groupName); }
        return new AccountInfo(getDB().retrieveMapping(user.getCertificateDN()));
    }
    
    public boolean removeMapping(String userDN) {
        return getDB().removeMapping(userDN);
    }  
    
    public void setPersistenceFactory(String persistanceFactory) {
        this.persistenceFactory = persistanceFactory;
    }

    public void setAccountPool(String accountPool) {
        this.accountPool = accountPool;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String toString(String bgColor) {
    	return "<td bgcolor=\""+bgColor+"\"><a href=\"accountMappers.jsp?command=edit&name=" + getName() + "\">" + getName() + "</a></td><td bgcolor=\""+bgColor+"\">" + getType() + "</td><td bgcolor=\""+bgColor+"\">&nbsp;</td>";
    }      
    
    public String toXML() {
    	String retStr = "\t\t<manualAccountMapper\n"+
			"\t\t\tname='"+getName()+"'\n"+
			"\t\t\tdescription='"+getDescription()+"'\n"+
			"\t\t\tpersistenceFactory='"+persistenceFactory+"'\n";
        if (groupName != null && !groupName.equals(""))
            retStr += "\t\t\tgroupName='"+groupName+"'\n";
        if (accountPool != null && !accountPool.equals(""))
            retStr += "\t\t\taccountPool='"+accountPool+"'\n";

        if (retStr.charAt(retStr.length()-1)=='\n') {
            retStr = retStr.substring(0, retStr.length()-1);
        }
        retStr += "/>\n\n";
        return retStr;
    }
}
