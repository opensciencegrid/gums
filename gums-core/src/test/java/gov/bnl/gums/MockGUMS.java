/*
 * MockGUMS.java
 *
 * Created on June 3, 2004, 11:10 AM
 */

package gov.bnl.gums;

import gov.bnl.gums.configuration.*;

/**
 *
 * @author  carcassi
 */
public class MockGUMS extends GUMS {
    
    /** Creates a new instance of MockGUMS */
    public MockGUMS() {
        confStore = new MockConfigurationStore();
    }

    public ResourceManager getResourceManager() {
        return new ResourceManager(this);
    }
    
}
