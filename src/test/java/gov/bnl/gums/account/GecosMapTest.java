/*
 * GecosMapTest.java
 * JUnit based test
 *
 * Created on May 10, 2005, 11:45 AM
 */

package gov.bnl.gums.account;

import gov.bnl.gums.account.GecosMap;
import junit.framework.*;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingEnumeration;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchResult;
import org.apache.commons.collections.MultiHashMap;
import org.apache.commons.collections.MultiMap;
import org.apache.log4j.Logger;

/**
 *
 * @author carcassi
 */
public class GecosMapTest extends TestCase {
    GecosMap map;
    
    public GecosMapTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        map = new GecosMap();
    }

    protected void tearDown() throws Exception {
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(GecosMapTest.class);
        
        return suite;
    }

    public void testSimpleEntry() {
        map.addEntry("jsmith", "John Smith");
        String account = map.findAccount("John", "Smith");
        assertEquals("jsmith", account);
    }

    public void testReverseEntry() {
        map.addEntry("jsmith", "John Smith");
        String account = map.findAccount("Smith", "John");
        assertEquals("jsmith", account);
    }

    public void testMultipleReverseEntries() {
        map.addEntry("jsmith", "John Smith");
        map.addEntry("jsmith2", "John Smith");
        String account = map.findAccount("Smith", "John");
        assertNull(account);
    }

    public void testMultipleEntries() {
        map.addEntry("jsmith", "John Smith");
        map.addEntry("jsmith2", "John Smith");
        String account = map.findAccount("John", "Smith");
        assertNull(account);
    }

    public void testMisspelledName() {
        map.addEntry("jsmith", "John Smith");
        String account = map.findAccount("Jon", "Smith");
        assertEquals("jsmith", account);
    }

    public void testMisspelledNameWithMultipleSurnames() {
        map.addEntry("jsmith", "John Smith");
        map.addEntry("jsmith2", "Jon Smith");
        String account = map.findAccount("Jon", "Smith");
        assertEquals("jsmith2", account);
    }
    
}
