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
 * @author carcassi
 */
public class HibernateUser {
    
    /** Creates a new instance of User */
    public HibernateUser() {
    }

    /**
     * Holds value of property id.
     */
    private Long id;

    /**
     * Getter for property id.
     * @return Value of property id.
     */
    public Long getId() {

        return this.id;
    }

    /**
     * Setter for property id.
     * @param id New value of property id.
     */
    public void setId(Long id) {

        this.id = id;
    }

    /**
     * Holds value of property dn.
     */
    private String dn;

    /**
     * Getter for property dn.
     * @return Value of property dn.
     */
    public String getDn() {

        return this.dn;
    }

    /**
     * Setter for property dn.
     * @param dn New value of property dn.
     */
    public void setDn(String dn) {

        this.dn = dn;
    }

    /**
     * Holds value of property fqan.
     */
    private String fqan;

    /**
     * Getter for property fqan.
     * @return Value of property fqan.
     */
    public String getFqan() {

        return this.fqan;
    }

    /**
     * Setter for property fqan.
     * @param fqan New value of property fqan.
     */
    public void setFqan(String fqan) {

        this.fqan = fqan;
    }

    /**
     * Holds value of property group.
     */
    private String group;

    /**
     * Getter for property group.
     * @return Value of property group.
     */
    public String getGroup() {

        return this.group;
    }

    /**
     * Setter for property group.
     * @param group New value of property group.
     */
    public void setGroup(String group) {

        this.group = group;
    }
    
}
