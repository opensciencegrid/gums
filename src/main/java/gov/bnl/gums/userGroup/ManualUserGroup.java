/*
 * ManualUserGroup.java
 *
 * Created on May 25, 2004, 4:48 PM
 */

package gov.bnl.gums.userGroup;

import gov.bnl.gums.db.ManualUserGroupDB;
import gov.bnl.gums.persistence.PersistenceFactory;
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
    private PersistenceFactory persistanceFactory;
    
    public ManualUserGroup() {
    }
    
	public ManualUserGroup(String name) {
		super(name);
	}
    
    public java.util.List getMemberList() {
        return db.retrieveMembers();
    }
    
    public boolean isInGroup(GridUser user) {
        return db.isMemberInGroup(user);
    }
    
    public void updateMembers() {
    }
    
    public void addMember(GridUser user) {
        db.addMember(user);
    }
    
    public boolean removeMember(GridUser user) {
        return db.removeMember(user);
    }
    
    public String getPersistenceFactory() {
        return (persistanceFactory!=null ? persistanceFactory.getName() : "");
    }
    
    public void setPersistenceFactory(PersistenceFactory persistenceFactory) {
        this.persistanceFactory = persistenceFactory;
        db = persistenceFactory.retrieveManualUserGroupDB( getName() );
    }

    public String toString() {
        if (persistanceFactory == null) {
            return "ManualUserGroup: persistenceFactory=null - group='" + getName() + "'";
        } else {
            return "ManualUserGroup: persistenceFactory='" + persistanceFactory.getName() + "' - group='" + getName() + "'";
        }
    }
    
    public String toXML() {
    	return super.toXML() +
		"\t\t\tpersistenceFactory='"+persistanceFactory.getName()+"'/>\n\n";
    }    
    
    public String getSummary(String bgColor) {
    	return "<td bgcolor=\""+bgColor+"\">" + getName() + "</td><td bgcolor=\""+bgColor+"\"></td><td bgcolor=\""+bgColor+"\"></td><td bgcolor=\""+bgColor+"\"></td><td bgcolor=\""+bgColor+"\"></td><td bgcolor=\""+bgColor+"\">" + persistanceFactory.getName() + "</td>";
    }
}
