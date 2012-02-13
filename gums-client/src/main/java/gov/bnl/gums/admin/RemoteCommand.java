/*
 * AbstractWebCommand.java
 *
 * Created on November 4, 2004, 10:07 AM
 */
package gov.bnl.gums.admin;


import gov.bnl.gums.command.AbstractCommand;
import gov.bnl.gums.command.Configuration;
import java.util.Iterator;
import org.apache.axis.client.Stub;
import org.apache.log4j.Logger;

import java.net.URL;

/**
 * @author carcassi
 */
public abstract class RemoteCommand extends AbstractCommand {
    private Logger log = Logger.getLogger(RemoteCommand.class);
	
    private GUMSAPI clientStub;

    protected GUMSAPI getGums(String gumsUrlStr) {
        log.debug("Retrieving GUMS stub");
        if (clientStub != null) return clientStub;        
        try {
            GUMSAPIService service = new GUMSAPIServiceLocator();
            if (Configuration.getInstance().isDirect()) {
                log.info("Accessing direct implementation.");
                return new GUMSAPIImpl();
            } else {
            	URL gumsUrl = gumsUrlStr!=null ? new URL(gumsUrlStr) : Configuration.getInstance().getGUMSLocation();;

                log.info("Accessing GUMS implementation at " + gumsUrl + ".");
                clientStub = service.getadmin(gumsUrl);
                Stub axisStub = (Stub) clientStub;
                axisStub.setMaintainSession(true);
                Iterator iter = axisStub._getPropertyNames();
                while (iter.hasNext()) {
                    String name = (String) iter.next();
                    log.debug("Client stub property '" + name + "' value '" + axisStub._getProperty(name));
                }
                return clientStub;
            }
        } catch (Exception e) {
            System.out.println("Couldn't initialize GUMS client:" + e.getMessage());
            log.fatal("Couldn't initialize GUMS client", e);
            System.exit(-1);

            return null;
        }
    }
    
    protected GUMSAPI getGums() {
    	return getGums(null);
    }
}
