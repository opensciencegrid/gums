/*
 * HibernateUser.java
 *
 * Created on June 15, 2005, 1:06 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package gov.bnl.gums.db;

import java.util.Date;

/**
 *
 * @author Jay Packard
 */
public class HibernateConfig {
    private Long id;
    private String xml;
    private Date timestamp;
    private Boolean current;
    private String name;
    
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getXml() {
		return xml;
	}
	public void setXml(String xml) {
		this.xml = xml;
	}
	public Date getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
	public Boolean getCurrent() {
		return current;
	}
	public void setCurrent(Boolean current) {
		this.current = current;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
}
    