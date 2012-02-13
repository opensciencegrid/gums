/*
 * GecosAccountMapper.java
 *
 * Created on May 25, 2004, 2:25 PM
 */

package gov.bnl.gums.account;

import gov.bnl.gums.CertToolkit;
import gov.bnl.gums.GridUser;
import gov.bnl.gums.configuration.Configuration;

import java.util.*;

import org.apache.log4j.Logger;

/** 
 * Maps a user to a local account based on the CN of the certificate and the
 * gecos field taken from a NIS/YP/LDAP/... database. The mapping can't be perfect, but contains
 * a series of heuristics that solve up to 90% of the cases, depending on how
 * the database itself is kept.
 * <p>
 * This is an abstact class, and will need the implementation of the method
 * that retrieves the list of GECOS fields.
 * <p>
 * It's suggested not to use this policy by itself, but to have it part of a 
 * CompositeAccountMapper in which a ManualAccountMapper comes first. This allows
 * to override those user mapping that are not satisfying.
 *
 * @author Gabriele Carcassi, Jay Packard
 */
public abstract class GecosAccountMapper extends AccountMapper {
    static private Logger log = Logger.getLogger(GecosAccountMapper.class);
    static private Map gecosMaps = new Hashtable();

    static public String getTypeStatic() {
		return "gecos";
	}
    
    public GecosAccountMapper() {
    	super();
    }
    
    public GecosAccountMapper(Configuration configuration) {
    	super(configuration);
    }
    
    public GecosAccountMapper(Configuration configuration, String name) {
    	super(configuration, name);
    }
    
    public String getType() {
		return "gecos";
	}
    
    public String mapUser(GridUser user, boolean createIfDoesNotExist) {
        String[] nameSurname = CertToolkit.parseNameAndSurname(user.getCertificateDN());
        GecosMap map = gecosMap();
        log.trace("GECOS findAccount. Name: " + nameSurname[0] + " - Surname: " + nameSurname[1] + " - GECOSMap: " + gecosMap());
        return map.findAccount(nameSurname[0], nameSurname[1]);
    }
    
    private GecosMap gecosMap() {
        synchronized (gecosMaps) {
            GecosMap map = (GecosMap) gecosMaps.get(getMapName());
            if (map != null) {
                if (map.isValid()) {
                    log.trace("Reusing GECOS map for '" + getMapName() +"'");
                    return map;
                } else {
                    log.trace("Invalidating expired GECOS map for '" + getMapName() +"'");
                    gecosMaps.remove(getMapName());
                }
            }
            log.debug("Creating new GECOS map for '" + getMapName() + "'");
            map = createMap();
            gecosMaps.put(getMapName(), map);
            return map;
        }
    }
    
    /**
     * Implements the creation of the map. The implementation is supposed to
     * connect to the source, and fill an object of the GecosMap type.
     * 
     * @return GECOS map object
     */
    protected abstract GecosMap createMap();
    
    /** 
     * Returns an ID for the map used/created by this specific mapper. The
     * name will be used to cache the generated map also across different
     * instances of the mapper.
     * 
     * @return map name as String
     */
    protected abstract String getMapName();
}
