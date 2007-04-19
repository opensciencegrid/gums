/*
 * LDAPGroupIDAssigner.java
 *
 * Created on October 3, 2005, 9:59 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package gov.bnl.gums.db;


import java.util.Iterator;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import gov.bnl.gums.GUMS;
import gov.bnl.gums.persistence.LDAPPersistenceFactory;

/**
 *
 * @author Gabriele Carcassi, Jay Packard
 */
public class LDAPGroupIDAssigner {
    private Log log = LogFactory.getLog(LDAPGroupIDAssigner.class);
    private Log adminLog = LogFactory.getLog(GUMS.resourceAdminLog);
    private LDAPPersistenceFactory factory;
    private List domains;
    
    /**
     * Creates a new instance of LDAPGroupIDAssigner for a given LDAP factory and
     * a given list of domains.
     * 
     * @param factory The factory that will provide the LDAP connectivity
     * @param domains A list of Strings conatining the domains relative to the
     *                default DN of the LDAP connection
     */
    public LDAPGroupIDAssigner(LDAPPersistenceFactory factory, List domains) {
        this.factory = factory;
        this.domains = domains;
        log.trace("LDAPGroupIDAssigner created - factory " + factory + " domains '" + domains + "'");
    }
    
    /**
     * Changes the primary group and assigns the secondary groups to the given account.
     * 
     * @param account A UNIX username (i.e. 'carcassi')
     * @param primary A UNIX group name (i.e. 'usatlas')
     * @param secondary A list of Strings representing secondary UNIX group names
     */
    public void assignGroups(String account, String primary, List secondary) {
        if (domains == null) {
            log.trace("No domain for assigning groups:  - account '" + account + "' - primary group '" + primary + "' - secondary '" + secondary + "'");
            return;
        }
        Iterator iter = domains.iterator();
        while (iter.hasNext()) {
            String domain = (String) iter.next();
            assignGroups(domain, account, primary, secondary);
        }
    }
    
    /**
     * Assigns the groups to the account for a particular domain.
     * 
     * @param domain The domain in which to assign the groups
     * @param account A UNIX account (i.e. 'carcassi')
     * @param primary A UNIX group name (i.e. 'usatlas')
     * @param secondary A list of Strings representing secondary UNIX group names
     */
    public void assignGroups(String domain, String account, String primary, List secondary) {
        try {
            factory.changeGroupID(domain, account, primary);
            log.trace("Assigned '" + primary + "' to '" + account + "' for domain '" + domain + "'");
            if (secondary == null) return;
            Iterator iter = secondary.iterator();
            while (iter.hasNext()) {
                String group = (String) iter.next();
                factory.addToSecondaryGroup(domain, account, group);
                log.trace("Assigned secondary group '" + group + "' to '" + account + "' for domain '" + domain + "'");
            }
        } catch (Exception e) {
            log.info("Couldn't assign GIDs. Domain '" + domain + "' - account '" + account + "' - primary group '" + primary + "' - secondary '" + secondary + "'", e);
            adminLog.error("Couldn't assign GIDs: " + e.getMessage() + ". Domain '" + domain + "' - account '" + account + "' - primary group '" + primary + "' - secondary '" + secondary + "'");
            throw new RuntimeException("Couldn't assign GIDs: " + e.getMessage() + ". Domain '" + domain + "' - account '" + account + "' - primary group '" + primary + "' - secondary '" + secondary + "'", e);
        }
    }
    
    /**
     * Reassigns the groups to the account, refreshing something that should be
     * already be present in LDAP. The LDAP factory controls whether this
     * actually is performed by setting the synchGroups property.
     * 
     * @param account A UNIX account (i.e. 'carcassi')
     * @param primary A UNIX group name (i.e. 'usatlas')
     * @param secondary A list of Strings representing secondary UNIX group names
     */
    public void reassignGroups(String account, String primary, List secondary) {
        if (factory.isSynchGroups()) {
            assignGroups(account, primary, secondary);
        } else {
            log.trace("Skip reassign groups for account '" + account + "' - primary group '" + primary + "' - secondary '" + secondary + "'");
        }
    }
    
}
