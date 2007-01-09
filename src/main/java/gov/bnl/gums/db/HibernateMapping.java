/*
 * HibernateMap.java
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
public class HibernateMapping {
    
    /**
     * Creates a new instance of Mapping 
     */
    public HibernateMapping() {
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
     * Holds value of property account.
     */
    private String account;

    /**
     * Getter for property account.
     * @return Value of property account.
     */
    public String getAccount() {

        return this.account;
    }

    /**
     * Setter for property account.
     * @param account New value of property account.
     */
    public void setAccount(String account) {

        this.account = account;
    }

    /**
     * Holds value of property map.
     */
    private String map;

    /**
     * Getter for property group.
     * @return Value of property group.
     */
    public String getMap()   {

        return this.map;
    }

    /**
     * Setter for property group.
     * @param userGroup New value of property group.
     */
    public void setMap(String map)   {

        this.map = map;
    }

    
}
