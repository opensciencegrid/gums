/*
 * ManualUserGroup.java
 *
 * Created on May 25, 2004, 4:48 PM
 */

package gov.bnl.gums.userGroup;

import gov.bnl.gums.configuration.Configuration;
import gov.bnl.gums.db.ManualUserGroupDB;
import gov.bnl.gums.GridUser;

/** 
 * A user group that is defined by a list of users stored in some way, allowing
 * to add and remove users. The persistance layer is implemented through an
 * interface, allowing different storage frameworks (i.e. database, LDAP, file)
 * <p>
 * This class will provide also configurable data caching.
 *
 * @author Gabriele Carcassi, Jay Packard
 */
public class ManualUserGroup extends UserGroup {
    static public String getTypeStatic() {
		return "manual";
	}
    
    private ManualUserGroupDB db;
	private String persistenceFactory = "";
    
    /**
     * Create a new manual user group. This empty constructor is needed by the XML Digestor.
     */
    public ManualUserGroup() {
    	super();
    }
    
    /**
     * Create a new manual user group with a configuration.
     */
    public ManualUserGroup(Configuration configuration) {
    	super(configuration);
    }
    
    /**
     * Create a new manual user group with a configuration and a name.
     */
	public ManualUserGroup(Configuration configuration, String name) {
		super(configuration, name);
	}
    
    public void addMember(GridUser user) {
        getDB().addMember(user);
    }
    
    public UserGroup clone(Configuration configuration) {
    	ManualUserGroup userGroup = new ManualUserGroup(configuration, getName());
    	userGroup.setDescription(getDescription());
    	userGroup.setAccess(getAccess());
    	userGroup.setPersistenceFactory(persistenceFactory);
    	return userGroup;
    }
    
    public ManualUserGroupDB getDB() {
    	if (db==null)
    		db = getConfiguration().getPersistenceFactory(persistenceFactory).retrieveManualUserGroupDB( getName() );
    	return db;
    }
    
    public java.util.List getMemberList() {
        return getDB().retrieveMembers();
    }
    
    /**
     * Setter for property persistenceFactory.
     * 
     * @return persistence factory as string.
     */
    public String getPersistenceFactory() {
        return persistenceFactory;
    }
    
    public String getType() {
		return "manual";
	}
    
    public boolean isInGroup(GridUser user) {
        return getDB().isMemberInGroup(user);
    }
    
    public boolean removeMember(GridUser user) {
        return getDB().removeMember(user);
    }
    
    /**
     * Setter for property persistenceFactory
     * 
     * @param persistenceFactory
     */
    public void setPersistenceFactory(String persistenceFactory) {
        this.persistenceFactory = persistenceFactory;
    }

    public String toString() {
        if (persistenceFactory == null) {
            return "ManualUserGroup: persistenceFactory=null - group='" + getName() + "'";
        } else {
            return "ManualUserGroup: persistenceFactory='" + persistenceFactory + "' - group='" + getName() + "'";
        }
    }
 
    public String toString(String bgColor) {
    	return "<td bgcolor=\""+bgColor+"\"><a href=\"userGroups.jsp?action=edit&name=" + getName() + "\">" + getName() + "</a></td><td bgcolor=\""+bgColor+"\">" + getType() + "</td><td bgcolor=\""+bgColor+"\"></td><td bgcolor=\""+bgColor+"\"></td><td bgcolor=\""+bgColor+"\"></td><td bgcolor=\""+bgColor+"\"></td>";
    }
    
    public String toXML() {
    	return "\t\t<manualUserGroup\n"+
		"\t\t\taccess='"+accessTypes[accessIndex]+"'\n" +
		"\t\t\tname='"+getName()+"'\n"+
		"\t\t\tdescription='"+getDescription()+"'\n"+
		"\t\t\tpersistenceFactory='"+persistenceFactory+"'/>\n\n";
    }    
    
    public void updateMembers() {
    }
}
