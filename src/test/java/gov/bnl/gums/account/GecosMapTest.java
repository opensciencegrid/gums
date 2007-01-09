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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
        map.addEntry("carcassi", "Gabriele Carcassi");
        String account = map.findAccount("Gabriele", "Carcassi");
        assertEquals("carcassi", account);
    }

    public void testReverseEntry() {
        map.addEntry("carcassi", "Gabriele Carcassi");
        String account = map.findAccount("Carcassi", "Gabriele");
        assertEquals("carcassi", account);
    }

    public void testMultipleReverseEntries() {
        map.addEntry("carcassi", "Gabriele Carcassi");
        map.addEntry("carcassi2", "Gabriele Carcassi");
        String account = map.findAccount("Carcassi", "Gabriele");
        assertNull(account);
    }

    public void testMultipleEntries() {
        map.addEntry("carcassi", "Gabriele Carcassi");
        map.addEntry("carcassi2", "Gabriele Carcassi");
        String account = map.findAccount("Gabriele", "Carcassi");
        assertNull(account);
    }

    public void testMisspelledName() {
        map.addEntry("carcassi", "Gabrielle Carcassi");
        String account = map.findAccount("Gabriele", "Carcassi");
        assertEquals("carcassi", account);
    }

    public void testMisspelledNameWithMultipleSurnames() {
        map.addEntry("carcassi", "Gabriele Carcassi");
        map.addEntry("gcarcass", "Gabrielle Carcassi");
        String account = map.findAccount("Gabriele", "Carcassi");
        assertEquals("carcassi", account);
    }
    
}
