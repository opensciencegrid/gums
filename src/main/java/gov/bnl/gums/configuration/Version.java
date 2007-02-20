package gov.bnl.gums.configuration;

/**
 * Simple class needed by digester to parse version from configuration file
 * @author jpackard
 *
 */
public class Version {
	private String version = null;
	
	public void setVersion(String version) {
		this.version = version;
	}
	
	public String getVersion() {
		return version;
	}
};