/*
 * ManualUserGroup.java
 *
 * Created on May 25, 2004, 4:48 PM
 */

package gov.bnl.gums.userGroup;

import gov.bnl.gums.configuration.Configuration;
import gov.bnl.gums.db.ManualUserGroupDB;
import gov.bnl.gums.GridUser;

/** A user group that is defined by a list of users stored in some way, allowing
 * to add and remove users. The persistance layer is implemented through an
 * interface, allowing different storage frameworks (i.e. database, LDAP, file)
 * <p>
 * This class will provide also configurable data caching.
 *
 * @author  Gabriele Carcassi
 */
public class ManualUserGroup extends UserGroup {
    private ManualUserGroupDB db;
    private String persistenceFactory = "";
    
	public ManualUserGroup() {
    	super();
    }
    
    public ManualUserGroup(Configuration configuration) {
    	super(configuration);
    }
    
    public ManualUserGroup(Configuration configuration, String name) {
		super(configuration, name);
	}
    
	public void addMember(GridUser user) {
        getDB().addMember(user);
    }
    
    public UserGroup clone(Configuration configuration) {
    	ManualUserGroup userGroup = new ManualUserGroup(configuration, getName());
    	userGroup.setAccess(getAccess());
    	userGroup.setPersistenceFactory(persistenceFactory);
    	return userGroup;
    }
    
    public java.util.List getMemberList() {
        return getDB().retrieveMembers();
    }
    
    public String getPersistenceFactory() {
        return persistenceFactory;
    }
    
    public String getType() {
		return "manual";
	}
    
    static public String getTypeStatic() {
		return "manual";
	}
    
    public boolean isInGroup(GridUser user) {
        return getDB().isMemberInGroup(user);
    }
    
    public boolean removeMember(GridUser user) {
        return getDB().removeMember(user);
    }
    
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
    	return "<td bgcolor=\""+bgColor+"\">" + getName() + "</td><td bgcolor=\""+bgColor+"\"></td><td bgcolor=\""+bgColor+"\"></td><td bgcolor=\""+bgColor+"\"></td><td bgcolor=\""+bgColor+"\"></td><td bgcolor=\""+bgColor+"\">" + persistenceFactory + "</td>";
    }
 
    public String toXML() {
    	return "\t\t<manualUserGroup\n"+
		"\t\t\taccess='"+accessTypes[accessIndex]+"'\n" +
		"\t\t\tname='"+getName()+"'\n"+
		"\t\t\tpersistenceFactory='"+persistenceFactory+"'/>\n\n";
    }
    
    public void updateMembers() {
    }    
    
    private ManualUserGroupDB getDB() {
    	if (db==null)
    		db = getConfiguration().getPersistenceFactory(persistenceFactory).retrieveManualUserGroupDB( getName() );
    	return db;
    }
}
