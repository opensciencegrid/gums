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

import org.apache.log4j.Logger;

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
 * @author Gabriele Carcassi, Jay Packard
 */
public class LDAPUserGroup extends UserGroup {
	/**
	 * @return user friendly string representation of the property type called statically 
	 */
	static public String getTypeStatic() {
		return "ldap";
	}

	protected Logger log = Logger.getLogger(LDAPUserGroup.class);
	protected Logger gumsAdminLog = Logger.getLogger(GUMS.gumsAdminLogName);
	protected ConfigurationStore confStore;    
	protected UserGroupDB db;
	protected Configuration conf;
	protected String persistenceFactory = "";    
	protected String server = "";
	protected String certDNField = "description";
	protected String memberUidField = "memberUid";
	protected String uidField = "uid";
	protected String peopleTree = "";
	protected String peopleObject = "ou=People";
	protected String peopleContext = null;
	protected String groupTree = "";

	/**
	 * Create a new ldap user group. This empty constructor is needed by the XML Digestor.
	 */
	public LDAPUserGroup() {
		super();
	}

	/**
	 * Create a new ldap user group with a configuration.
	 */
	public LDAPUserGroup(Configuration configuration) {
		super(configuration);
	}

	/**
	 * Create a new ldap user group with a configuration and a name.
	 */
	public LDAPUserGroup(Configuration configuration, String name) {
		super(configuration, name);
	}

	public UserGroup clone(Configuration configuration) {
		LDAPUserGroup userGroup = new LDAPUserGroup(configuration, new String(getName()));
		userGroup.setDescription(new String(getDescription()));
		userGroup.setPersistenceFactory(new String(persistenceFactory));
		userGroup.setAccess(new String(getAccess()));
		userGroup.setPeopleTree(new String(getPeopleTree()));
		userGroup.setGroupTree(new String(getGroupTree()));
		userGroup.setMemberUidField(new String(getMemberUidField()));
		userGroup.setUidField(new String(getUidField()));
		userGroup.setServer(new String(getServer()));
		userGroup.setCertDNField(new String(certDNField));
		return userGroup;
	}

	public boolean equals(Object obj) {
		if (obj instanceof LDAPUserGroup) {
			LDAPUserGroup group = (LDAPUserGroup) obj;
			if ((server == null ? group.server == null : server.equals(group.server)) &&
					(peopleTree == null ? group.peopleTree == null : peopleTree.equals(group.peopleTree)) && 
					(groupTree == null ? group.groupTree == null : groupTree.equals(group.groupTree)) &&
					(certDNField == null ? group.certDNField == null : certDNField.equals(group.certDNField)) &&
					(memberUidField == null ? group.memberUidField == null : memberUidField.equals(group.memberUidField)) &&
					(uidField == null ? group.uidField == null : uidField.equals(group.uidField)) &&
					(getName() == null ? group.getName() == null : getName().equals(group.getName())) && 
					(persistenceFactory == null ? group.persistenceFactory == null : persistenceFactory.equals(group.persistenceFactory))) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Getter for property certDN
	 * 
	 * @return cert DN as string
	 */
	 public String getCertDNField() {
		 return certDNField;
	 }

	 /**
	  * Getter for property groupTree
	  *
	  * @return groupTree as string
	  */
	 public String getGroupTree() {
		 return groupTree;
	 }

	 public java.util.List getMemberList() {
		 return getDB().retrieveMembers();
	 }

	 public String getMemberUidField() {
		 return memberUidField;
	 }

	 /**
	  * Getter for property peopleTree
	  *
	  * @return peopleTree as string
	  */
	 public String getPeopleTree() {
		 return peopleTree;
	 }

	 /**
	  * Getter for property persistenceFactory
	  * 
	  * @return persistenceFactory as string
	  */
	 public String getPersistenceFactory() {
		 return persistenceFactory;
	 }

	 /**
	  * The LDAP query used to retrieveGetter for property query.
	  * 
	  * @return The LDAP query used. i.e. "ou=usatlas,o=atlas,dc=eu-datagrid,dc=org"
	  */
	 public String getQuery() {
		 return peopleTree;
	 }

	 /**
	  * Returns the name of the LDAP server used to retrieve the list of users.
	  * 
	  * @return The name of the server server. i.e. "grid-vo.nikhef.nl"
	  */
	 public String getServer() {
		 return this.server;
	 }

	 public String getType() {
		 return "ldap";
	 }

	 public String getUidField() {
		 return uidField;
	 }

	 public int hashCode() {
		 return new String(peopleTree + " " + groupTree).hashCode();
	 }

	 @Override
	 public boolean isInGroup(GridUser user) {
		 return getDB().isMemberInGroup(user);
	 }

	 @Override
	 public boolean isDNInGroup(GridUser user) {
		return getDB().isDNInGroup(user);
	 }

	 /**
	  * Setter for property certDN
	  * 
	  * @param certDN as string
	  */
	 public void setCertDNField(String certDNField) {
		 this.certDNField = certDNField;
	 }

	 /**
	  * Setter for property groupTree
	  *
	  * @param groupTree as string
	  */
	 public void setGroupTree(String groupTree) {
		 this.groupTree = groupTree;
	 }

	 public void setMemberUidField(String memberUidField) {
		 this.memberUidField = memberUidField;
	 }

	 /**
	  * Setter for property peopleTree
	  *
	  * @param peopleTree as string
	  */
	 public void setPeopleTree(String peopleTree) {
		 this.peopleTree = peopleTree;
		 this.peopleObject = peopleTree.substring(0, peopleTree.indexOf(','));
		 this.peopleContext = peopleTree.substring(peopleTree.indexOf(',')+1);
	 }
	 
	 /**
	  * Setter for property persistenceFactory
	  *
	  * @param persistenceFactory as string
	  */
	 public void setPersistenceFactory(String persistenceFactory) {
		 this.persistenceFactory = persistenceFactory;
	 }

	 /**
	  * Changes the LDAP query used to retrieveGetter for property query.
	  * 
	  * @param query The LDAP query used. i.e. "ou=usatlas,o=atlas,dc=eu-datagrid,dc=org"
	  * @deprecated
	  */
	 public void setQuery(String query) {
		 if (query.startsWith(peopleObject+",")) {
			 peopleTree = query;
			 peopleObject = query.substring(0, query.indexOf(','));
			 peopleContext = query.substring(query.indexOf(',')+1);
		 } else if (query.startsWith("ou=")) {
			 peopleObject = "ou=People";
			 peopleContext = query.substring(query.indexOf(',')+1);
			 peopleTree = peopleObject + "," + peopleContext;
		 } else if (query.startsWith("o=")) {
			 peopleObject = "ou=People";
			 peopleContext = query;
			 peopleTree = peopleObject + "," + peopleContext;
		 } else {
			 throw new IllegalArgumentException("The query is not understood by the LDAP group. It is expected to start with \"ou=...\" or \"o=...\"");
		 }
	 }

	 /**
	  * Changes the LDAP server used to retrieve the list of users.
	  * @param server The name of the server server. i.e. "grid-vo.nikhef.nl"
	  */
	 public void setServer(String server) {
		 this.server = server;
	 }

	 public void setUidField(String uidField) {
		 this.uidField = uidField;
	 }

	 public String toString() {
		 return "LDAPGroup: ldap://"+server+"/"+peopleTree+","+groupTree;
	 }

	 public String toString(String bgColor) {
		 return "<td bgcolor=\""+bgColor+"\"><a href=\"userGroups.jsp?command=edit&name=" + getName() + "\">" + getName() + "</a></td><td bgcolor=\""+bgColor+"\">" + getType() + "</td><td bgcolor=\""+bgColor+"\">&nbsp;</td><td bgcolor=\""+bgColor+"\">&nbsp;</td><td bgcolor=\""+bgColor+"\"></td><td bgcolor=\""+bgColor+"\">&nbsp;</td>";
	 }

	 public String toXML() {
		 return "\t\t<ldapUserGroup\n"+
		 "\t\t\tname='"+getName()+"'\n"+
		 "\t\t\taccess='"+accessTypes[accessIndex]+"'\n" +
		 "\t\t\tdescription='"+getDescription()+"'\n"+
		 "\t\t\tserver='"+server+"'\n" +
		 "\t\t\tpeopleTree='"+peopleTree+"'\n" +
		 "\t\t\tgroupTree='"+groupTree+"'\n" +
		 "\t\t\tmemberUidField='"+memberUidField+"'\n" +
		 "\t\t\tuidField='" + uidField+"'\n" +
		 "\t\t\tcertDNField='"+certDNField+"'\n" +
		 "\t\t\tpersistenceFactory='"+persistenceFactory+"'/>\n\n";
	 }

	 public void updateMembers() {
		 getDB().loadUpdatedList(retrieveMembers());
	 }

	 private UserGroupDB getDB() {
		 if (db==null)
			 db = getConfiguration().getPersistenceFactory(persistenceFactory).retrieveUserGroupDB( getName() );
		 return db;
	 }

	 private List<GridUser> retrieveGroupMembers(DirContext rootCtx, Attribute members) throws javax.naming.NamingException {
		 Map<String, Set<String>> people = retrievePeopleMap(rootCtx);
		 NamingEnumeration names = members.getAll();
		 List<GridUser> groupMembers = new ArrayList<GridUser>();
		 while (names.hasMore()) {
			 // Converting the people to the DN, by looking up the person description attribute
			 String ldapName = (String) names.next();
			 ldapName = ldapName.trim();
			 Set<String> certDNs = people.get(ldapName);
			 if (certDNs == null) {
				 gumsAdminLog.debug("Member of a LDAP VO group not mapped to any certificate: '" + ldapName + "'");
			 } else {
				 for (String certDN : certDNs) {
					 groupMembers.add(new GridUser(certDN, null));
				 }
			 }
		 }
		 if (groupMembers.isEmpty()) {
			 gumsAdminLog.warn("The following group returned no members: " + this);
		 }
		 return groupMembers;
	 }

	 /**
	  * Returns the list of member retrieved from the LDAP server. The members are not saved in the database.
	  * Must be synchronized since the System properties are being set
	  * 
	  * @return A list of VOEntry objects representing the members.
	  */
	 private synchronized List retrieveMembers() {
		 java.util.Properties properties = retrieveProperties();
		 log.info("Retrieving members from '" + properties.getProperty("java.naming.provider.url") + "'  '" + peopleTree + "' '" + groupTree + "'");
		 try {
			 javax.naming.directory.DirContext jndiCtx = new javax.naming.directory.InitialDirContext(properties);
			 if (!peopleTree.equals("") && groupTree.equals("")) {
				 DirContext rootCtx = (DirContext) jndiCtx.lookup(peopleContext);
				 return retrieveVOMembers(rootCtx);
			 }
			 else if (!groupTree.equals("") && !peopleTree.equals("")) {
				 Attributes atts = jndiCtx.getAttributes(groupTree);
				 Attribute members = atts.get(memberUidField);
				 if (members == null) {
					 String message = "Couldn't retrieve the list of members from the LDAP group: missing attribute member";
					 GUMS.gumsAdminEmailLog.put("ldapUpdateProblem", message, false);
					 throw new RuntimeException(message);
				 }
				 DirContext rootCtx = (DirContext) jndiCtx.lookup(peopleContext);
				 return retrieveGroupMembers(rootCtx, members);            
			 }
		 } catch (javax.naming.NamingException e) {
			 String message = "Couldn't retrieve users from LDAP server: " + e;
			 /*            if (e.getRootCause() != null) {
                message += " caused by [" + e.getRootCause().getMessage() + "]";
            }*/
			 log.error("Couldn't retrieve LDAP users: ", e);
			 throw new RuntimeException(message, e);
		 }
		 return null;
	 }

	 private Properties retrieveProperties() {
		 Properties properties = new java.util.Properties();
		 properties.put(Context.PROVIDER_URL, "ldap://"+server);
		 properties.put(Context.INITIAL_CONTEXT_FACTORY,"com.sun.jndi.ldap.LdapCtxFactory");
		 properties.put(Context.SECURITY_PROTOCOL, "none");
		 return properties;
	 }

	 private List<GridUser> retrieveVOMembers(DirContext rootCtx) throws NamingException {
		 Map<String, Set<String>> people = retrievePeopleMap(rootCtx);
		 List<Set<String>> listOfDNs = new ArrayList<Set<String>>(people.values());
		 List<GridUser> users = new ArrayList<GridUser>(listOfDNs.size());
		 if (listOfDNs.isEmpty()) {
			 gumsAdminLog.warn("The following group returned no members: " + this);
		 }
		 for (Set<String> DNs : listOfDNs) {
		 	 for (String dn : DNs) {
				 users.add(new GridUser(dn, null));
			 }
		 }
		 return users;
	 }    

	 protected Map<String, Set<String>> retrievePeopleMap(DirContext ldap) throws javax.naming.NamingException {
		 NamingEnumeration people = ldap.search(peopleObject, "("+certDNField+"=*)", null);
		 Map<String, Set<String>> map = new Hashtable<String, Set<String>>();
		 while (people.hasMore()) {
			 SearchResult person = (SearchResult) people.next();
			 Attributes personAtts = person.getAttributes();
			 if (personAtts == null) {continue;}
			 String ldapDN = (String)personAtts.get(uidField).get();
			 Attribute attrDNs = personAtts.get(certDNField);
			 int valueCount = attrDNs.size();
			 Set<String> allDNs = new HashSet<String>(valueCount);
			 for (int idx = 0; idx < valueCount; idx++) {
				 String certDN = (String) personAtts.get(certDNField).get(idx);
				 if (certDN.startsWith("subject=")) {
					 certDN = certDN.substring(8);
				 }
				 certDN = certDN.trim();
				 allDNs.add(certDN);
			 }
			 map.put(ldapDN, allDNs);
		 }
		 return map;
	 }
}
