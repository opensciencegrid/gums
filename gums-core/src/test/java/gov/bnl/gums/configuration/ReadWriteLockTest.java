/*
 * ReadWriteLockTest.java
 * JUnit based test
 *
 * Created on January 25, 2005, 10:47 AM
 */

package gov.bnl.gums.configuration;

import java.util.Random;
import junit.framework.*;
import org.apache.log4j.Logger;

/**
 *
 * @author carcassi
 */
public class ReadWriteLockTest extends TestCase {
    
    public ReadWriteLockTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws java.lang.Exception {
    }
    
    protected void tearDown() throws java.lang.Exception {
    }
    
    public static junit.framework.Test suite() {
        junit.framework.TestSuite suite = new junit.framework.TestSuite(ReadWriteLockTest.class);
        
        return suite;
    }
    
    private ReadWriteLock lock;
    private boolean testing;
    
    public void testLocks() throws InterruptedException {
        lock = new ReadWriteLock("test");
        testing = true;
        Runnable testCode = new Runnable() {
            public void run() {
                Random rand = new Random();
                while (testing) {
                    try {
                        Thread.sleep(rand.nextInt(20));
                    } catch (InterruptedException e) {
                        // Never happens
                    }
                    if (rand.nextInt(10) == 0) {
                        lock.obtainWriteLock();
                        try {
                            Thread.sleep(rand.nextInt(20));
                        } catch (InterruptedException e) {
                            // Never happens
                        }
                        lock.releaseWriteLock();
                    } else {
                        lock.obtainReadLock();
                        try {
                            Thread.sleep(rand.nextInt(20));
                        } catch (InterruptedException e) {
                            // Never happens
                        }
                        lock.releaseReadLock();
                    }
                }
            }
        };
        for (int i = 0; i < 5; i++) {
            new Thread(testCode).start();
        }
        Thread.sleep(5000);
        testing = false;
        Thread.sleep(1000);
        assertFalse(lock.isWriteLocked());
        assertFalse(lock.isReadLocked());
        
    }
    
    
}
