/*
 * CompositeAccountMapper2Test.java
 * JUnit based test
 *
 * Created on May 25, 2004, 3:26 PM
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
public class MySQLAccountPoolMapperDBTest extends AccountPoolMapperDBTest {
    
    public MySQLAccountPoolMapperDBTest(java.lang.String testName) {
        super(testName);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(MySQLAccountPoolMapperDBTest.class);
        return suite;
    }
    
    public void setUp() throws SQLException {
        MySQLPersistenceFactory mysql = new MySQLPersistenceFactory(new Configuration(), "mysqlPers1");
        mysql.setConnectionFromDbProperties();
        db = mysql.retrieveAccountPoolMapperDB("userGroup");
        Connection conn = mysql.getConnection();
        conn.createStatement().execute("DELETE FROM UserAccountMapping " +
        "WHERE userGroup  = 'userGroup'");
        for (int i = 0; i < 10; i++) {
            db.addAccount("pool"+i);
        }
        lastUsedDelay = 1000;
    }
    
    public static void main(String args[]) {
        Test test = suite();
        test.run(new TestResult());
    }    
}
