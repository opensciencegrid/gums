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

import java.sql.Timestamp;
import gov.bnl.gums.account.MappedAccountInfo;

/**
 *
 * @author Gabriele Carcassi, Jay Packard
 */
public class HibernateMapping implements MappedAccountInfo {
    private Long id;
    private String dn;
    private String account;
    private String map;
    private Timestamp lastuse;
    private boolean recycle;

    /**
     * Creates a new instance of Mapping 
     */
    public HibernateMapping() {
    }

    /**
     * Getter for property account.
     * @return Value of property account.
     */
    public String getAccount() {

        return this.account;
    }

    /**
     * Getter for property dn.
     * @return Value of property dn.
     */
    public String getDn() {

        return this.dn;
    }

    /**
     * Getter for property id.
     * @return Value of property id.
     */
    public Long getId() {

        return this.id;
    }

    /**
     * Getter for property group.
     * @return Value of property group.
     */
    public String getMap()   {

        return this.map;
    }

    /**
     * Setter for property account.
     * @param account New value of property account.
     */
    public void setAccount(String account) {

        this.account = account;
    }

    /**
     * Setter for property dn.
     * @param dn New value of property dn.
     */
    public void setDn(String dn) {

        this.dn = dn;
    }

    /**
     * Setter for property id.
     * @param id New value of property id.
     */
    public void setId(Long id) {

        this.id = id;
    }

    /**
     * Setter for property group.
     * @param userGroup New value of property group.
     */
    public void setMap(String map)   {

        this.map = map;
    }

    public void setLastuse(Timestamp lastuse) {
        this.lastuse = lastuse;
    }

    public Timestamp getLastuse() {
        return this.lastuse;
    }

    public void setRecycle(Boolean recycle) {
        if (recycle == null) {recycle = false;}
        else {this.recycle = recycle;}
    }

    public boolean getRecycle() {
        return this.recycle;
    }
}
