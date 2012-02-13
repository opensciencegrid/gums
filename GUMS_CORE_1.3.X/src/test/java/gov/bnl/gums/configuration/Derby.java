/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.bnl.gums.configuration;

import java.sql.*;
import org.apache.log4j.*;

public class Derby {

	static Logger logger = Logger.getLogger("gov.bnl.gov.configuration.Derby");

    static String[] users = {
        "/DC=org/DC=doegrids/OU=People/CN=John R. Hover 47116",
        "/DC=org/DC=doegrids/OU=People/CN=David R. Stampf 638310"
    };
    static Connection conn;

    public static String[] getUserList() {
        return users;
    }

    public static void shutdown() throws Exception {
        conn.close();
    }
    public static void init() {
        // Question 1 - can we set up a new "gums-test" database on the fly
        // every time we run?

        String driver = "org.apache.derby.jdbc.EmbeddedDriver";
        String dbName = "gums-test";
        String connectionURL = "jdbc:derby:" + dbName + ";create=true";

	logger.info("connect to: " + connectionURL);
        //System.out.println(connectionURL);

        try {
            Class.forName(driver);
            conn = DriverManager.getConnection(connectionURL);


            Statement stmt = conn.createStatement();

            dropTableIfExists(conn,"\"USER\"");
            dropTableIfExists(conn,"MAPPING");
            dropTableIfExists(conn,"CONFIG");
            dropTableIfExists(conn,"USERS");

            // now, put in the real tables

            stmt.execute("CREATE TABLE USERS ( " +
                    "ID BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY, " +
                    "GROUP_NAME VARCHAR(255) NOT NULL, " +
                     "DN varchar(255) NOT NULL, " +
                    "FQAN varchar(255) default NULL, " +
                    "EMAIL varchar(255) default NULL)");

            stmt.execute("CREATE INDEX COMPLETE1 ON USERS (GROUP_NAME, DN, FQAN)");

            stmt.execute("CREATE TABLE MAPPING ( " +
                        "ID INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY, " +
                        "MAP VARCHAR(255) NOT NULL, " +
                        "DN varchar(255) default NULL, " +
                        "ACCOUNT varchar(255) default NULL " +
                        ")");

            stmt.execute("CREATE INDEX complete2 ON MAPPING (MAP, DN)");

            stmt.execute("CREATE TABLE CONFIG ( " +
                        "ID INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY, " +
                        "\"XML\" LONG VARCHAR NOT NULL, " +
                        "TIMESTAMP TIMESTAMP NOT NULL, " +
                        "\"CURRENT\" CHAR NOT NULL, " + // was boolean
                        "NAME varchar(255), " +
                        "AUTOGEN CHAR NOT NULL DEFAULT 'F' " +    // was boolean
                        ")");

            stmt.execute("CREATE INDEX complete3 ON CONFIG (\"CURRENT\", TIMESTAMP)");

            for(String user : users) {
            stmt.executeUpdate("INSERT INTO USERS (DN,GROUP_NAME) VALUES " +
                    "('" + user + "', 'admins')");

            }


        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void dropTableIfExists(Connection c, String tableName) {
        try {
            Statement stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT count(*) from " + tableName);
            rs.next();

            // it returned ok - drop it

            stmt.execute("DROP TABLE " + tableName);


        } catch (java.sql.SQLSyntaxErrorException e) {
            // presumable the table does not exist - all is well
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
