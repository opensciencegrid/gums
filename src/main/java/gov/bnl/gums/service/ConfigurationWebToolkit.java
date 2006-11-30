/*
 * ConfigurationWebToolkit.java
 *
 * Created on Oct 16, 2006, 2:03 PM
 */

package gov.bnl.gums.service;

import gov.bnl.gums.configuration.Configuration;
import javax.servlet.http.HttpServletRequest;
import java.rmi.Remote;

public class ConfigurationWebToolkit implements Remote {
	static public Configuration loadConfiguration(HttpServletRequest request) {
		return null;
	}
}
