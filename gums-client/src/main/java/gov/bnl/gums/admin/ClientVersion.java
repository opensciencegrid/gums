/*
 * Version.java
 *
 * Created on May 11, 2005, 2:46 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package gov.bnl.gums.admin;

import java.net.URL;

import org.apache.commons.cli.Options;
import org.apache.commons.digester.Digester;

import gov.bnl.gums.configuration.Version;

/**
 *
 * @author carcassi
 */
public class ClientVersion extends RemoteCommand {

    static {
        command = new ClientVersion();
    }

    /**
     * Creates a new Version object.
     */
    public ClientVersion() {
        syntax = "";
        description = "Returns the version of GUMS client being used.";
    }

    protected org.apache.commons.cli.Options buildOptions() {
        Options options = new Options();
        return options;
    }

    protected void execute(org.apache.commons.cli.CommandLine cmd) throws Exception {
    	String pomPath = "META-INF/maven/gov.bnl.racf.gums/gums-core/pom.xml";
    	URL resourceURL = getClass().getClassLoader().getSystemResource(pomPath);
    	if (resourceURL == null) {
			System.out.println("Unable to retrieve resource: "+ pomPath);
			return;
		}
    	String pomFile = resourceURL.toString();
		Digester digester = new Digester();
		digester.addObjectCreate("project/version", Version.class);
		digester.addCallMethod("project/version","setVersion",0);
		Version versionCls = null;
		try {
			versionCls = (Version)digester.parse(pomFile);
		} catch (Exception e) {
			System.out.println("Cannot get GUMS version from "+pomFile);
			return;
		}
		if (versionCls == null) {
			System.out.println("Cannot get GUMS version from "+pomFile);
			return;
		}
		String version = versionCls.getVersion();
		System.out.println(version);
    }
    
}
