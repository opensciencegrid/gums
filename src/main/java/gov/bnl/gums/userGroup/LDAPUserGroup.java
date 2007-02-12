/*
 * LDAPGroup.java
 *
 * Created on May 25, 2004, 11:57 AM
 */

package gov.bnl.gums.userGroup;

import gov.bnl.gums.GUMS;
import gov.bnl.gums.GridUser;
import gov.bnl.gums.configuration.Configuration;
import gov.bnl.gums.configuration.ConfigurationStore;
import gov.bnl.gums.db.UserGroupDB;

import java.util.*;

import javax.naming.*;
import javax.naming.directory.*;

import org.apache.commons.logging.*;

/** A group of users defined by an LDAP VO.
 * <p>
 * The query should be :
 * <ul>
 *   <li>A People ou (i.e. ou=People,o=atlas,dc=eu-datagrid,dc=org). All the 
 *   object found in that category will have to have a 'description' attribute
 *   which will contain the certificate DN</li>
 *   <li>A group ou (i.e. ou=usatlas,o=atlas,dc=eu-datagrid,dc=org). The ou
 *   object will need to have a 'member' property with the list of people in 
 *   the VO that are in the group. Each member will be an object in
 *   ou=People</li>
 *   <li>A root o (i.e. o=atlas,dc=eu-datagrid,dc=org). A ou=People object will
 *   be expected, and it will behave like the first option</li>
 * </ul>
 *
 * @author  carcassi
 */
public class LDAPUserGroup extends UserGroup {
    private Log log = LogFactory.getLog(LDAPUserGroup.class);
    private Log resourceAdminLog = LogFactory.getLog(GUMS.resourceAdminLog);
    private UserGroupDB db;
    private String persistenceFactory = "";    
    private Configuration conf;
    private String server = "";
    private String query = "";
    private String keyStore = "";
	private String keyPassword = "";
	protected ConfigurationStore confStore;
    
    public LDAPUserGroup() {
    	super();
    }
 
	public LDAPUserGroup(Configuration configuration) {
		super(configuration);
	}
    
	public LDAPUserGroup(Configuration configuration, String name) {
		super(configuration, name);
	}
    
    public UserGroup clone(Configuration configuration) {
    	LDAPUserGroup userGroup = new LDAPUserGroup(configuration, getName());
    	userGroup.setPersistenceFactory(persistenceFactory);
    	userGroup.setAccess(getAccess());
    	userGroup.setKeyPassword(getKeyPassword());
    	userGroup.setKeyStore(getKeyStore());
    	userGroup.setQuery(getQuery());
    	userGroup.setServer(getServer());
    	return userGroup;
    }
    
    public boolean equals(Object obj) {
        if (obj instanceof LDAPUserGroup) {
            LDAPUserGroup group = (LDAPUserGroup) obj;
            if ((server == null ? group.server == null : server.equals(group.server)) &&
               (query == null ? group.query == null : query.equals(group.query)) && 
               (getName() == null ? group.getName() == null : getName().equals(group.getName())) && 
               (persistenceFactory == null ? group.persistenceFactory == null : persistenceFactory.equals(group.persistenceFactory))) {
                return true;
            }
        }
        return false;
    }
    
    public String getKeyPassword() {
    	return keyPassword;
    }
    
    public String getKeyStore() {
    	return keyStore;
    }
    
    public java.util.List getMemberList() {
        return getDB().retrieveMembers();
    }
    
    public String getPersistenceFactory() {
        return persistenceFactory;
    }
    
    /**
     * The LDAP query used to retrieveGetter for property query.
     * @return The LDAP query used. i.e. "ou=usatlas,o=atlas,dc=eu-datagrid,dc=org"
     */
    public String getQuery() {
        return this.query;
    }
    
    /**
     * Returns the name of the LDAP server used to retrieve the list of users.
     * @return The name of the server server. i.e. "grid-vo.nikhef.nl"
     */
    public String getServer() {
        return this.server;
    }
    
    public int hashCode() {
        return query.hashCode();
    }
    
    public boolean isInGroup(GridUser user) {
        return getDB().isMemberInGroup(user);
    }
    
    public void setKeyPassword(String keyPassword) {
    	this.keyPassword = keyPassword;
    }
    
    public void setKeyStore(String keyStore) {
    	this.keyStore = keyStore;
    }
    
    public void setPersistenceFactory(String persistenceFactory) {
        this.persistenceFactory = persistenceFactory;
    }
    
    /**
     * Changes the LDAP query used to retrieveGetter for property query.
     * @param query The LDAP query used. i.e. "ou=usatlas,o=atlas,dc=eu-datagrid,dc=org"
     */
    public void setQuery(String query) {
        this.query = query;
    }
    
    /**
     * Changes the LDAP server used to retrieve the list of users.
     * @param server The name of the server server. i.e. "grid-vo.nikhef.nl"
     */
    public void setServer(String server) {
        this.server = server;
    }
    
    public String toString() {
        return "LDAPGroup: ldap://"+server+"/"+query;
    }
    
    public String toString(String bgColor) {
    	return "<td bgcolor=\""+bgColor+"\">" + getName() + "</td><td bgcolor=\""+bgColor+"\"></td><td bgcolor=\""+bgColor+"\"></td><td bgcolor=\""+bgColor+"\"></td><td bgcolor=\""+bgColor+"\"></td><td bgcolor=\""+bgColor+"\">" + server + "</td>";
    }
    
    public String toXML() {
    	return super.toXML() +
    	"\t\t\tserver='"+server+"'\n" +
		"\t\t\tquery='"+query+"'\n" +
		"\t\t\tpersistenceFactory='"+persistenceFactory+"'\n" +
		"\t\t\tkeyStore='"+keyStore+"'\n" +
		"\t\t\tkeyPassword='"+keyPassword+"'/>\n\n";
    }
    
    public void updateMembers() {
    	getDB().loadUpdatedList(retrieveMembers());
    }
    
    private UserGroupDB getDB() {
    	if (db==null)
            db = getConfiguration().getPersistenceFactory(persistenceFactory).retrieveUserGroupDB( getName() );
    	return db;
    }
    
    private List retrieveGroupMembers(DirContext rootCtx, Attribute members) throws javax.naming.NamingException {
        Map people = retrievePeopleMap(rootCtx);
        NamingEnumeration names = members.getAll();
        List list = new ArrayList();
        while (names.hasMore()) {
            // Converting the people to the DN, by looking up the person description attribute
            String ldapName = (String) names.next();
            ldapName = ldapName.trim();
            String certDN = (String) people.get(ldapName);
            if (certDN == null) {
                resourceAdminLog.warn("Member of a LDAP VO group not mapped to any certificate: '" + ldapName + "'");
            } else {
                list.add(new GridUser(certDN, null));
            }
        }
        if (list.isEmpty()) {
            resourceAdminLog.warn("The following group returned no members: " + this);
        }
        return list;
    }

    /**
     * Returns the list of member retrieved from the LDAP server. The members are not saved in the database.
     * Must be synchronized since the System properties are being set
     * @return A list of VOEntry objects representing the members.
     */
    private synchronized List retrieveMembers() {
        java.util.Properties jndiProperties = new java.util.Properties();
        jndiProperties.put("java.naming.provider.url","ldap://"+server);
        jndiProperties.put("java.naming.factory.initial","com.sun.jndi.ldap.LdapCtxFactory");
        System.setProperty("javax.net.ssl.keyStore",getKeyStore());
        System.setProperty("javax.net.ssl.keyStorePassword",getKeyPassword());
        log.info("Retrieving members from '" + jndiProperties.getProperty("java.naming.provider.url") +
                 "'  '" + query + "'");
        try {
            javax.naming.directory.DirContext jndiCtx = new javax.naming.directory.InitialDirContext(jndiProperties);
            if (query.startsWith("ou=People,")) {
                String voRoot = query.substring(query.indexOf(',')+1);
                DirContext rootCtx = (DirContext) jndiCtx.lookup(voRoot);
                return retrieveVOMembers(rootCtx);
            } else if (query.startsWith("ou=")) {
                Attributes atts = jndiCtx.getAttributes(query);
                Attribute members = atts.get("member");
                if (members == null) {
                    throw new RuntimeException("Couldn't retrieve the list of members from the LDAP group: missing attribute member");
                }
                String voRoot = query.substring(query.indexOf(',')+1);
                DirContext rootCtx = (DirContext) jndiCtx.lookup(voRoot);
                return retrieveGroupMembers(rootCtx, members);
            } else if (query.startsWith("o=")) {
                DirContext rootCtx = (DirContext) jndiCtx.lookup(query);
                return retrieveVOMembers(rootCtx);
            } else {
                throw new IllegalArgumentException("The query is not understood by the LDAP group. It is expected to start with \"ou=...\" or \"o=...\"");
            }

        } catch (javax.naming.NamingException e) {
            String message = "Couldn't retrieve users from LDAP server: " + e;
/*            if (e.getRootCause() != null) {
                message += " caused by [" + e.getRootCause().getMessage() + "]";
            }*/
            log.error("Couldn't retrieve LDAP users: ", e);
            throw new RuntimeException(message, e);
        }
    }
    
    private List retrieveVOMembers(DirContext rootCtx) throws NamingException {
        Map people = retrievePeopleMap(rootCtx);
        List list = new ArrayList(people.values());
        if (list.isEmpty()) {
            resourceAdminLog.warn("The following group returned no members: " + this);
        }
        Iterator iter = list.iterator();
        List users = new ArrayList();
        while (iter.hasNext()) {
            String dn = (String) iter.next();
            users.add(new GridUser(dn, null));
        }
        if (list.isEmpty()) {
            resourceAdminLog.warn("The following group returned no members: " + this);
        }
        return users;
    }    
    
    Map retrievePeopleMap(DirContext ldap) throws javax.naming.NamingException {
        NamingEnumeration people = ldap.search("ou=People", "(description=subject=*)", null);
        Map map = new Hashtable();
        while (people.hasMore()) {
            SearchResult person = (SearchResult) people.next();
            Attributes personAtts = person.getAttributes();
            String ldapDN = person.getName();
            if (person.isRelative()) {
                ldapDN = ldapDN + ",ou=People," + ldap.getNameInNamespace();
            }
            
            String certDN = (String) personAtts.get("description").get();
            if (certDN.startsWith("subject=")) {
                certDN = certDN.substring(8);
            }
            certDN = certDN.trim();
            map.put(ldapDN, certDN);
        }
        return map;
    }
}
