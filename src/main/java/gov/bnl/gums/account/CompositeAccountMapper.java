/*
 * CompositeAccountMapper.java
 *
 * Created on May 4, 2004, 4:27 PM
 */

package gov.bnl.gums.account;



import java.util.*;

/** Maps a user to an account by using the first of a list of policies that
 * returns a result. This is used to define an aggregate policy of any
 * kind of policies.
 * <p>
 * An example of such policy is: the user is first mapped by looking for
 * an entry in the database to a manual map; if that is not found, the NIS
 * map is used to determine the account; if that is not found, a generic
 * group account is used.
 * <p>
 * From a developer point of view, it iterates over a list of AccountMappers
 * until one of them doesn't return null. All the exception pass straight 
 * through.
 * <p>
 * @todo it might be better to define an exception
 * AccountMappingException that goes through, while the other are catched,
 * logged and the iteration continues. 
 * @ deprecated 
 *
 * @author  Gabriele Carcassi
 */
public class CompositeAccountMapper extends AccountMapper {
    
    private List mappers = new ArrayList();
    
    /**
     * Assigns the list of mappers. The list must contain
     * only objects of class AccountMapper.
     * <p>
     * The list provided will be copied: further modification of the mappers
     * parameters won't affect the list inside the object.
     * @param mappers a List of AccountMapper objects
     */
    public void setMappers(List mappers) {
        this.mappers = new ArrayList(mappers);
    }
    
    /**
     * Returns the list of mappers used by this composite mapper.
     * <p>
     * The list is an unmodifiable copy of the internal list. To change
     * the list of mappers one will have to create a new list and use the
     * setMappers() method. To simply add another mapper to the list, call
     * addMapper().
     * @return a List of AccountMapper objects
     */
    public List getMappers() {
        return Collections.unmodifiableList(mappers);
    }
    
    public String mapUser(String userDN) {
        Iterator iter = mappers.iterator();
        while (iter.hasNext()) {
            AccountMapper mapper = (AccountMapper) iter.next();
            String acc = mapper.mapUser(userDN);
            if (acc != null) return acc;
        }
        return null;
    }
    
    public boolean containsMap(String userDN, String accountName) {
    	return false;
    }
    
    /**
     * Adds a mapper at the end of the list of mapper used by this composite.
     * @param mapper The new mapper to be used only if all the previous mappers failed
     */
    public void addMapper(AccountMapper mapper) {
        mappers.add(mapper);
    }
    
}
