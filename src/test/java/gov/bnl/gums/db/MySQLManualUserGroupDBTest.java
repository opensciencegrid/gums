/*
 * ManualUserGroupDBTest.java
 * JUnit based test
 *
 * Created on May 25, 2004, 4:41 PM
 */

package gov.bnl.gums.db;


import gov.bnl.gums.configuration.Configuration;
import gov.bnl.gums.persistence.MySQLPersistenceFactory;

import java.sql.*;
import java.util.*;
import junit.framework.*;

/**
 *
 * @author carcassi
 */
public class MySQLManualUserGroupDBTest extends ManualUserGroupDBTest {
    
    public MySQLManualUserGroupDBTest(java.lang.String testName) {
        super(testName);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(MySQLManualUserGroupDBTest.class);
        return suite;
    }
     
    public void setUp() throws SQLException {
        MySQLPersistenceFactory mysql = new MySQLPersistenceFactory(new Configuration(), "mysqlPers1");
        mysql.setConnectionFromDbProperties();
        db = mysql.retrieveManualUserGroupDB("testManual");
        Connection conn = mysql.getConnection();
        conn.createStatement().execute("DELETE FROM User where userGroup = 'testManual'");
        List mockUsers = new ArrayList();
    }
    
}
