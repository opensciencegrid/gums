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

/**
 *
 * @author Gabriele Carcassi, Jay Packard
 */
public class HibernateUser {
    private Long id;
    private String dn;
    private String fqan;
    private String group;
    private String email;
    
    /** Creates a new instance of User */
    public HibernateUser() {
    }

    /**
     * Getter for property dn.
     * @return Value of property dn.
     */
    public String getDn() {

        return this.dn;
    }

    /**
     * Getter for property fqan.
     * @return Value of property fqan.
     */
    public String getFqan() {

        return this.fqan;
    }

    /**
     * Getter for property group.
     * @return Value of property group.
     */
    public String getGroup() {

        return this.group;
    }

    /**
     * Getter for property id.
     * @return Value of property id.
     */
    public Long getId() {

        return this.id;
    }

    /**
     * Setter for property dn.
     * @param dn New value of property dn.
     */
    public void setDn(String dn) {

        this.dn = dn;
    }

    /**
     * Setter for property fqan.
     * @param fqan New value of property fqan.
     */
    public void setFqan(String fqan) {

        this.fqan = fqan;
    }

    /**
     * Setter for property group.
     * @param group New value of property group.
     */
    public void setGroup(String group) {

        this.group = group;
    }

    /**
     * Setter for property id.
     * @param id New value of property id.
     */
    public void setId(Long id) {

        this.id = id;
    }

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
    
}
