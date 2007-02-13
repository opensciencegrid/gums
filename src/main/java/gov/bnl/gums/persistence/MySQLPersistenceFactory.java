/*
 * GumsDbImplementation.java
 *
 * Created on May 26, 2004, 12:34 PM
 */

package gov.bnl.gums.persistence;

import gov.bnl.gums.GridUser;
import gov.bnl.gums.configuration.Configuration;
import gov.bnl.gums.db.AccountPoolMapperDB;
import gov.bnl.gums.db.ManualAccountMapperDB;
import gov.bnl.gums.db.ManualUserGroupDB;
import gov.bnl.gums.db.UserGroupDB;

import java.sql.*;
import java.util.Date;
import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/** This class hold the whole MySQL database implementation by providing factory
 * methods for the different database interfaces. It makes use of some query
 * features defined in mysql 4.0.18, therefore it assumes that as a minimum
 * version.
 * <p>
 * @todo query depends on version of MySQL: should check it.
 *
 * @author  Gabriele Carcassi
 */
public class MySQLPersistenceFactory extends PersistenceFactory {
    public static final Byte USER_ACCOUNT = new Byte((byte)0);
    //    public static final Byte GROUP_ACCOUNT = new Byte((byte)1);
    //    public static final Byte POOL_ACCOUNT = new Byte((byte)2);
    private Log log = LogFactory.getLog(MySQLPersistenceFactory.class);
    private List connections = Collections.synchronizedList(new LinkedList());
    private Map statementCache = new Hashtable();
    
	static public String getType() {
		return "mySql";
	}
    
    private class GumsDBAccountMapperDB implements ManualAccountMapperDB {
        String userGroup;
        
        GumsDBAccountMapperDB(String userGroup) {
            this.userGroup = userGroup;
        }
        
        public void createMapping(String userDN, String account) {
            mapAccountToUser(userGroup, userDN, account);
        }
        
        public boolean removeMapping(String userDN) {
            return removeMappings(userDN, userGroup);
        }
        
        public String retrieveMapping(String userDN) {
            Connection conn = getConnection();
            try {
                PreparedStatement retrieveMappingStmt = prepareStatement(conn, "SELECT Account " +
                    "FROM UserAccountMapping " +
                    "WHERE userGroup = ? AND userDN = ? AND " +
                    "startDate <= NOW() AND (endDate > NOW() OR " +
                    "isNull(endDate))");
                
                retrieveMappingStmt.setString(1, userGroup);
                retrieveMappingStmt.setString(2, userDN);
                
                ResultSet set = retrieveMappingStmt.executeQuery();
                if (!set.next()) return null;
                
                return set.getString(1);
            } catch (SQLException e) {
                log.info("Exception while executing query", e);
                throw new RuntimeException("Couldn't retrieve account mapping from the account group " + userGroup, e);
            } finally {
                releaseConnection(conn);
            }
            
        }    
        
        // Not supported
        public java.util.List retrieveMappings() {
        	return null;
        }
        
    }
    private class GumsDbUserGroupDB implements UserGroupDB, ManualUserGroupDB {
        private String groupName;
        private List newMembers;
        private List removedMembers;
        
        GumsDbUserGroupDB(String groupName) {
            this.groupName = groupName;
        }
        
        public void addMember(GridUser user) {
            if (user.getVoFQAN() == null) {
                addUser(user.getCertificateDN(), groupName);
            } else {
                addUser(user.getCertificateDN(), user.getVoFQAN().getFqan(), groupName);
            }
        }
        
        public boolean isMemberInGroup(GridUser user) {
            if (user.getVoFQAN() == null) {
                return (findUserID(user.getCertificateDN(), groupName) != null);
            } else {
                return (findUserID(user.getCertificateDN(), groupName, user.getVoFQAN().getFqan()) != null);
            }
        }
        
        public void loadUpdatedList(List members) {
            // TODO This is a very rough implementation
            // A better implementation could be to write all the new members in a
            // temp table, and with RIGHT and LEFT join figure out what new
            // and old members are available.
            List currentMembers = retrieveMembers();
            newMembers = new ArrayList(members);
            newMembers.removeAll(currentMembers);
            removedMembers = new ArrayList(currentMembers);
            removedMembers.removeAll(members);
            Iterator iter = newMembers.iterator();
            while (iter.hasNext()) {
                GridUser user = (GridUser) iter.next();
                addMember(user);
            }
            iter = removedMembers.iterator();
            while (iter.hasNext()) {
                GridUser userDN = (GridUser) iter.next();
                removeMember(userDN);
            }
        }
        
        public boolean removeMember(GridUser user) {
            Connection conn = getConnection();
            try {
                PreparedStatement removeMemberStmt = prepareStatement(conn, "UPDATE User SET removalDate=NOW() " +
                    "WHERE userDN = ? AND userFQAN = ? AND userGroup = ? AND isNull(removalDate)");
                
                PreparedStatement removeMemberFqanNullStmt = prepareStatement(conn, "UPDATE User SET removalDate=NOW() " +
                    "WHERE userDN = ? AND isNull(userFQAN) AND userGroup = ? AND isNull(removalDate)");
                
                if (user.getVoFQAN() == null) {
                    removeMemberFqanNullStmt.setString(1, user.getCertificateDN());
                    removeMemberFqanNullStmt.setString(2, groupName);
                    return (removeMemberFqanNullStmt.executeUpdate() > 0);
                } else {
                    removeMemberStmt.setString(1, user.getCertificateDN());
                    removeMemberStmt.setString(2, user.getVoFQAN().getFqan());
                    removeMemberStmt.setString(3, groupName);
                    return (removeMemberStmt.executeUpdate() > 0);
                }
                
            } catch (SQLException e) {
                log.info("Exception while executing query", e);
                throw new RuntimeException("Couldn't add a member to group " + groupName, e);
            } finally {
                releaseConnection(conn);
            }
        }
        
        public List retrieveMembers() {
            Connection conn = getConnection();
            try {
                PreparedStatement retrieveMembersStmt = prepareStatement(conn, "SELECT userDN, userFQAN FROM User " +
                    "WHERE registrationDate <= NOW() AND (removalDate > NOW() OR " +
                    "isNull(removalDate)) AND userGroup = ?");
                
                retrieveMembersStmt.setString(1, groupName);
                
                ResultSet set = retrieveMembersStmt.executeQuery();
                List members = new ArrayList();
                while (set.next()) {
                    members.add(new GridUser(set.getString("userDN"), set.getString("userFQAN")));
                }
                
                return members;
            } catch (SQLException e) {
                log.info("Exception while executing query", e);
                throw new RuntimeException("Couldn't retrieve the member list for group " + groupName, e);
            } finally {
                releaseConnection(conn);
            }
        }
        
        public List retrieveNewMembers() {
            return newMembers;
        }
        
        public List retrieveRemovedMembers() {
            return removedMembers;
        }
        
    }
    
    private class MySQLAccountPoolMapperDB implements AccountPoolMapperDB {
        
        String userGroup;
        
        MySQLAccountPoolMapperDB(String userGroup) {
            this.userGroup = userGroup;
        }
        
        public void addAccount(String account) {
            if (doesAccountExists(account)) {
                throw new IllegalArgumentException("The account is already in the pool");
            }
            Connection conn = getConnection();
            try {
                PreparedStatement addAccountStmt = prepareStatement(conn, "INSERT INTO UserAccountMapping SET userGroup=?, userDN=NULL, account=?, startDate=NULL");
                
                addAccountStmt.setString(1, userGroup);
                addAccountStmt.setString(2, account);
                
                addAccountStmt.executeUpdate();
            } catch (SQLException e) {
                log.info("Exception while executing query", e);
                throw new RuntimeException("Couldn't add account to pool", e);
            } finally {
                releaseConnection(conn);
            }
        }
        
        public String assignAccount(String userDN) {
            String account = retrieveFreeAccount();
            if (account == null) return null;
            Connection conn = getConnection();
            try {
                PreparedStatement assignAccountStmt = prepareStatement(conn, "UPDATE UserAccountMapping " +
                    "SET startDate=?, endDate=?, userDN = ? " +
                    "WHERE userGroup = ? " +
                    "AND account = ?");

                assignAccountStmt.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
                assignAccountStmt.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
                assignAccountStmt.setString(3, userDN);
                assignAccountStmt.setString(4, userGroup);
                assignAccountStmt.setString(5, account);

                assignAccountStmt.executeUpdate();
                return account;
            } catch (SQLException e) {
                log.info("Exception while executing query", e);
                throw new RuntimeException("Couldn't remove the account mapping", e);
            } finally {
                releaseConnection(conn);
            }
        }
        
        public int getNumberUnassignedMappings() {
            Connection conn = getConnection();
            try {
                PreparedStatement retrieveAccountMapStmt = prepareStatement(conn, "SELECT account, userDN " +
                    "FROM UserAccountMapping " +
                    "WHERE userDN = NULL");
                
                ResultSet set = retrieveAccountMapStmt.executeQuery();
                return set.getFetchSize();
            } catch (SQLException e) {
                log.info("Exception while executing query", e);
                throw new RuntimeException("Couldn't retrieve number of unassigned mappings " + userGroup, e);
            } finally {
                releaseConnection(conn);
            }
        }
        
        public String retrieveAccount(String userDN) {
            Connection conn = getConnection();
            try {
                PreparedStatement retrieveAccountStmt = prepareStatement(conn, "SELECT account " +
                    "FROM UserAccountMapping " +
                    "WHERE userGroup = ? AND userDN = ?");
                
                retrieveAccountStmt.setString(1, userGroup);
                retrieveAccountStmt.setString(2, userDN);
                
                ResultSet set = retrieveAccountStmt.executeQuery();
                if (!set.next()) return null;
                
                String account = set.getString(1);
                
                PreparedStatement touchAccountStmt = prepareStatement(conn, "UPDATE UserAccountMapping " +
                    "SET endDate = ? " +
                    "WHERE userGroup = ? " +
                    "AND account = ?");
                
                touchAccountStmt.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
                touchAccountStmt.setString(2, userGroup);
                touchAccountStmt.setString(3, account);
                
                touchAccountStmt.executeUpdate();
                
                return account;
            } catch (SQLException e) {
                log.info("Exception while executing query", e);
                throw new RuntimeException("Couldn't determine if account is in pool " + userGroup, e);
            } finally {
                releaseConnection(conn);
            }
        }
        
        public Map retrieveAccountMap() {
            Connection conn = getConnection();
            try {
                PreparedStatement retrieveAccountMapStmt = prepareStatement(conn, "SELECT account, userDN " +
                    "FROM UserAccountMapping " +
                    "WHERE userGroup = ?");
                
                retrieveAccountMapStmt.setString(1, userGroup);
                
                ResultSet set = retrieveAccountMapStmt.executeQuery();
                Map map = new Hashtable();
                while (set.next()) {
                    if (set.getString(2) != null) {
                        map.put(set.getString(2), set.getString(1));
                    }
                }
                
                return map;
            } catch (SQLException e) {
                log.info("Exception while executing query", e);
                throw new RuntimeException("Couldn't determine if account is in pool " + userGroup, e);
            } finally {
                releaseConnection(conn);
            }
        }
        
        public List retrieveUsersNotUsedSince(Date date) {
            Connection conn = getConnection();
            try {
                PreparedStatement retrieveUsersNotUsedSinceStmt = prepareStatement(conn, "SELECT userDN " +
                    "FROM UserAccountMapping " +
                    "WHERE userGroup = ? AND endDate < ?");
                
                retrieveUsersNotUsedSinceStmt.setString(1, userGroup);
                retrieveUsersNotUsedSinceStmt.setTimestamp(2, new Timestamp(date.getTime()));
                
                ResultSet set = retrieveUsersNotUsedSinceStmt.executeQuery();
                List list = new ArrayList();
                while (set.next()) {
                    if (set.getString(1) != null) {
                        list.add(set.getString(1));
                    }
                }
                
                return list;
            } catch (SQLException e) {
                log.info("Exception while executing query", e);
                throw new RuntimeException("Couldn't determine if account is in pool " + userGroup, e);
            } finally {
                releaseConnection(conn);
            }
        }
        
        public void unassignUser(String user) {
            Connection conn = getConnection();
            try {
                PreparedStatement unassignUserStmt = prepareStatement(conn, "UPDATE UserAccountMapping " +
                    "SET userDN = NULL " +
                    "WHERE userGroup = ? " +
                    "AND userDN = ?");
                
                unassignUserStmt.setString(1, userGroup);
                unassignUserStmt.setString(2, user);
                
                unassignUserStmt.executeUpdate();
            } catch (SQLException e) {
                log.info("Exception while executing query", e);
                throw new RuntimeException("Couldn't remove the account mapping", e);
            } finally {
                releaseConnection(conn);
            }
        }
        
        private boolean doesAccountExists(String account) {
            Connection conn = getConnection();
            try {
                PreparedStatement doesAccountExistsStmt = prepareStatement(conn, "SELECT account " +
                    "FROM UserAccountMapping " +
                    "WHERE userGroup = ? AND account=?");
                
                doesAccountExistsStmt.setString(1, userGroup);
                doesAccountExistsStmt.setString(2, account);
                
                ResultSet set = doesAccountExistsStmt.executeQuery();
                if (!set.next()) return false;
                
                return true;
            } catch (SQLException e) {
                log.info("Exception while executing query", e);
                throw new RuntimeException("Couldn't determine if account is in pool " + userGroup, e);
            } finally {
                releaseConnection(conn);
            }
        }
        
        private String retrieveFreeAccount() {
            Connection conn = getConnection();
            try {
                PreparedStatement retrieveFreeAccountStmt = prepareStatement(conn, "SELECT account " +
                    "FROM UserAccountMapping " +
                    "WHERE userGroup = ? AND isNull(userDN) ORDER BY account LIMIT 1");
                
                retrieveFreeAccountStmt.setString(1, userGroup);
                
                ResultSet set = retrieveFreeAccountStmt.executeQuery();
                if (!set.next()) return null;
                
                return set.getString(1);
            } catch (SQLException e) {
                log.info("Exception while executing query", e);
                throw new RuntimeException("Couldn't determine if account is in pool " + userGroup, e);
            } finally {
                releaseConnection(conn);
            }
        }
        
    }

    public MySQLPersistenceFactory() {
    	super();
    }
    
    public MySQLPersistenceFactory(Configuration configuration) {
    	super(configuration);
    }
    
    public MySQLPersistenceFactory(Configuration configuration, String name) {
    	super(configuration, name);
    }
    
    public PersistenceFactory clone(Configuration configuration) {
    	MySQLPersistenceFactory persistenceFactory = new MySQLPersistenceFactory(configuration, getName());
    	persistenceFactory.setProperties((Properties)getProperties().clone());
    	return persistenceFactory;
    }
    
    public Connection getConnection() {
         return retrieveConnection();
    }
    
    public boolean removeMappings(String user, String userGroup) {
        Connection conn = getConnection();
        try {
            PreparedStatement removeMappingsStmt = prepareStatement(conn, "UPDATE UserAccountMapping " +
                "SET endDate=NOW() " +
                "WHERE userGroup = ? " +
                "AND userDN = ?");
            
            removeMappingsStmt.setString(1, userGroup);
            removeMappingsStmt.setString(2, user);
            
            return (removeMappingsStmt.executeUpdate() > 0);
        } catch (SQLException e) {
            log.info("Exception while executing query", e);
            throw new RuntimeException("Couldn't remove the account mapping", e);
        } finally {
            releaseConnection(conn);
        }
    }
    
    public AccountPoolMapperDB retrieveAccountPoolMapperDB(String name) {
        // TODO It can only store one set of mapping for now
        return new MySQLAccountPoolMapperDB(name);
    }
    
    public ManualAccountMapperDB retrieveManualAccountMapperDB(String name) {
        return new GumsDBAccountMapperDB(name);
    }
    
    public ManualUserGroupDB retrieveManualUserGroupDB(String groupName) {
        return new GumsDbUserGroupDB(groupName);
    }
    
    public UserGroupDB retrieveUserGroupDB(String groupName) {
        return new GumsDbUserGroupDB(groupName);
    }    
    
    public void setConnection(Connection conn) {
        releaseConnection(conn);
    }
    public void setConnectionFromDbProperties() {
        try {
            setProperties(readDbProperties());
        } catch (MissingResourceException e) {
            throw new RuntimeException("Couldn't find database configuration file (etc/db.properties)", e);
        }
    }
    
    public String toXML() {
    	return null;
    }
    
    private void addUser(String userDN, String groupName) {
        if (findUserID(userDN, groupName) != null) {
            throw new RuntimeException("Couldn't add member '" + userDN + "' to group '" + groupName +"': already in group");
        }
        Connection conn = getConnection();
        try {

            PreparedStatement addMemberStmt = prepareStatement(conn, "INSERT INTO User SET userDN=?, userGroup=?, registrationDate=NOW()");
            
            addMemberStmt.setString(1, userDN);
            addMemberStmt.setString(2, groupName);
            
            addMemberStmt.executeUpdate();
            
        } catch (SQLException e) {
            log.info("Exception while executing query", e);
            throw new RuntimeException("Couldn't add a member to group " + groupName, e);
        } finally {
            releaseConnection(conn);
        }
    }

    private void addUser(String userDN, String fqan, String groupName) {
        if (findUserID(userDN, groupName, fqan) != null) {
            throw new RuntimeException("Couldn't add member '" + userDN + "' to group '" + groupName +"': already in group");
        }
        Connection conn = getConnection();
        try {

            PreparedStatement addMemberFQANStmt = prepareStatement(conn, "INSERT INTO User SET userDN=?, userFQAN=?, userGroup=?, registrationDate=NOW()");
            
            addMemberFQANStmt.setString(1, userDN);
            addMemberFQANStmt.setString(2, fqan);
            addMemberFQANStmt.setString(3, groupName);
            
            addMemberFQANStmt.executeUpdate();
        } catch (SQLException e) {
            log.info("Exception while executing query", e);
            throw new RuntimeException("Couldn't add a member to group " + groupName, e);
        } finally {
            releaseConnection(conn);
        }
    }
    
    private Connection createConnectionFromProperties() {
        try {
            Properties prop = getProperties();
            try {
                Class.forName(prop.getProperty("jdbcDriver"));
            } catch (NullPointerException e) {
                throw new RuntimeException("The database driver wasn't specified: jdbcDriver property is missing from the configuration.", e);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("The database driver wasn't found", e);
            }
            return DriverManager.getConnection(prop.getProperty("jdbcUrl"), prop);
        } catch (SQLException e) {
            String url = null;
            if (getProperties() != null) {
                url = getProperties().getProperty("jdbcUrl");
            }
            log.info("Exception while connecting to the database", e);
            throw new RuntimeException("Couldn't connect to the database '" + url + "': check gums.config or the status of the database.", e);
        }
    }

    private Integer findUserID(String DN, String groupName) {
        Connection conn = getConnection();
        try {

            PreparedStatement retrieveUserIDStmt = prepareStatement(conn, "SELECT * FROM User " +
                "WHERE registrationDate <= NOW() AND (removalDate > NOW() OR " +
                "isNull(removalDate)) AND userGroup=? AND userDN=? AND isNull(userFQAN);");
            
            retrieveUserIDStmt.setString(1, groupName);
            retrieveUserIDStmt.setString(2, DN);
            
            ResultSet set = retrieveUserIDStmt.executeQuery();
            if (!set.next())
                return null;
            return new Integer(set.getInt("userID"));
        } catch (SQLException e) {
            log.info("Exception while executing query", e);
            throw new RuntimeException("Couldn't determine whether a user was in group " + groupName, e);
        } finally {
            releaseConnection(conn);
        }
    }
    
    private Integer findUserID(String DN, String groupName, String fqan) {
        Connection conn = getConnection();
        try {

            PreparedStatement retrieveUserIDFqanStmt = prepareStatement(conn, "SELECT * FROM User " +
                "WHERE registrationDate <= NOW() AND (removalDate > NOW() OR " +
                "isNull(removalDate)) AND userGroup=? AND userDN=? AND userFQAN =?;");
            
            retrieveUserIDFqanStmt.setString(1, groupName);
            retrieveUserIDFqanStmt.setString(2, DN);
            retrieveUserIDFqanStmt.setString(3, fqan);
            
            ResultSet set = retrieveUserIDFqanStmt.executeQuery();
            if (!set.next())
                return null;
            return new Integer(set.getInt("userID"));
        } catch (SQLException e) {
            log.info("Exception while executing query", e);
            throw new RuntimeException("Couldn't determine whether a user was in group " + groupName, e);
        } finally {
            releaseConnection(conn);
        }
    }
    
    private PreparedStatement prepareStatement(Connection conn, String sql) throws SQLException {
        Map statementsPerConnection = (Map) statementCache.get(conn);
        if (statementsPerConnection == null) {
            statementsPerConnection = new Hashtable();
            statementCache.put(conn, statementsPerConnection);
        }
        PreparedStatement stmt = (PreparedStatement) statementsPerConnection.get(sql);
        if (stmt == null) {
            stmt = conn.prepareStatement(sql);
            statementsPerConnection.put(sql, stmt);
            log.trace("Created newstatement for: '" + sql + "'");
        }
        return stmt;
    }
    
    private Properties readDbProperties() {
        PropertyResourceBundle prop = (PropertyResourceBundle) ResourceBundle.getBundle("db");
        Properties prop2 = new Properties();
        Enumeration keys = prop.getKeys();
        while (keys.hasMoreElements()) {
            String key = (String) keys.nextElement();
            prop2.setProperty(key, prop.getString(key));
        }
        return prop2;
    }

    private void releaseConnection(Connection conn) {
        connections.add(0, conn);
        log.trace("Connection added. Connections in the pool: " + connections.size());
    }
    
    private Connection retrieveConnection() {
        Connection conn;
        if (connections.size() == 0) {
            conn = createConnectionFromProperties();
            log.trace("Connection created. Connections in the pool: " + connections.size());
        } else {
            conn = (Connection) connections.remove(0);
            log.trace("Connection reused. Connections in the pool: " + connections.size());
        }
        return conn;
    }

    protected void mapAccountToUser(String userGroup, String user, String account) {
        Connection conn = getConnection();
        try {
            PreparedStatement mapAccountToUserStmt = prepareStatement(conn, "INSERT INTO UserAccountMapping SET userGroup=?, userDN=?, account=?, startDate=NOW()");
            
            mapAccountToUserStmt.setString(1, userGroup);
            mapAccountToUserStmt.setString(2, user);
            mapAccountToUserStmt.setString(3, account);
            
            mapAccountToUserStmt.executeUpdate();
        } catch (SQLException e) {
            log.info("Exception while executing query", e);
            throw new RuntimeException("Couldn't map user to account", e);
        } finally {
            releaseConnection(conn);
        }
    }

}
