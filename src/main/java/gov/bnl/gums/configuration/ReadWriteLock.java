/*
 * ReadWriteLock.java
 *
 * Created on January 25, 2005, 10:39 AM
 */

package gov.bnl.gums.configuration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/** Implements a lock to prevent access to GUMS data when updating. This is
 * essentially the same as ReadWriteLock in JDK 1.5 (which I discovered after
 * implementing this), which can't be used as we need JDK 1.4 compatibility.
 * In the future it can be eliminated.
 * <p>
 * The write lock is exclusive, while the read locks allows for concurrent reads.
 * Once the write lock is requested, no more reads are accepted. The write lock
 * is guaranteed only when all read locks are released.
 *
 * @author Gabriele Carcassi
 */
public class ReadWriteLock {
    private Log log = LogFactory.getLog(ReadWriteLock.class);
    
    private int[] lock = new int[2];
    private String name;
    
    public ReadWriteLock(String name) {
        this.name = name;
    }
    
    /** Checks whether anybody has a read lock.
     */
    public synchronized boolean isReadLocked() {
        return lock[1] != 0;
    }
    
    /** Checks whether anybody is reading data.
     */
    public synchronized boolean isWriteLocked() {
        return lock[0] != 0;
    }
    
    public synchronized void obtainWriteLock() {
        log.trace("Trying to write on " + name + ": " + lock[0] + ", " + lock[1]);
        
        // Book a write
        while (lock[1] != 0) {
            try {
                log.trace("Waiting write on " + name + ": " + lock[0] + ", " + lock[1]);
                wait();
            } catch (InterruptedException e) {
                log.error("Wait was interrupted: " + e.getMessage(), e);
            }
        }
        lock[1] = 1;
        log.trace("Locked write on " + name + ": " + lock[0] + ", " + lock[1]);
        notifyAll();
        
        // Wait all the readers are done
        while (lock[0] != 0) {
            try {
                wait();
            } catch (InterruptedException e) {
                log.error("Wait was interrupted: " + e.getMessage(), e);
            }
        }
    }
    
    public synchronized void releaseWriteLock() {
        log.trace("Unlocking write on " + name + ": " + lock[0] + ", " + lock[1]);
        lock[1]=0;
        log.trace("Unocked write on " + name + ": " + lock[0] + ", " + lock[1]);
        notifyAll();
    }
    
    public synchronized void obtainReadLock() {
        log.trace("Trying to read on " + name + ": " + lock[0] + ", " + lock[1]);
        // Wait that writes are finished
        while (lock[1] != 0) {
            try {
                log.trace("Waiting read on " + name + ": " + lock[0] + ", " + lock[1]);
                wait();
            } catch (InterruptedException e) {
                log.error("Wait was interrupted: " + e.getMessage(), e);
            }
        }
        
        // Increase read counter
        lock[0]++;
        log.trace("Locked read on " + name + ": " + lock[0] + ", " + lock[1]);
        notifyAll();
    }
    
    public synchronized void releaseReadLock() {
        log.trace("Unlocking read on " + name + ": " + lock[0] + ", " + lock[1]);
        lock[0]--;
        log.trace("Unocked read on " + name + ": " + lock[0] + ", " + lock[1]);
        notifyAll();
    }
    
}
