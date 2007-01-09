/*
 * KeyStore.java
 *
 * Created on May 24, 2004, 2:34 PM
 */

package gov.bnl.gums.configuration;

class KeyStore {
	private String location, password;
    
	/**
     * Getter for location variable.
     * @return location string.
     */
	public String getLocation() {
		return this.location;
	}

	/**
     * Setter for location variable.
     * @return location string.
     */
	public void setLocation(String location) {
		this.location = location;
	}
	
	/**
     * Getter for location variable.
     * @return location string.
     */
	public String getPassword() {
		return this.password;
	}

	/**
     * Setter for location variable.
     * @return password string.
     */
	public void setPassword(String password) {
		this.password = password;
	}
}
