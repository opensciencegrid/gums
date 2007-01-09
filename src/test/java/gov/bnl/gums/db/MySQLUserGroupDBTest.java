/*
 * UserGroupDBTest.java
 * JUnit based test
 *
 * Created on May 25, 2004, 10:38 AM
 */

package gov.bnl.gums.db;


import gov.bnl.gums.persistence.MySQLPersistenceFactory;

import java.sql.*;
import java.util.*;
import junit.framework.*;

/**
 *
 * @author carcassi
 */
public class MySQLUserGroupDBTest extends UserGroupDBTest {
    
    public MySQLUserGroupDBTest(java.lang.String testName) {
        super(testName);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(MySQLUserGroupDBTest.class);
        return suite;
    }
    
    public void setUp() throws SQLException {
        MySQLPersistenceFactory mysql = new MySQLPersistenceFactory("mysqlPers1");
        mysql.setConnectionFromDbProperties();
        db = mysql.retrieveUserGroupDB("test");
        Connection conn = mysql.getConnection();
        conn.createStatement().execute("DELETE FROM User where userGroup = 'test'");
        initDB();
    }
}
