/*
 * AccountInfo.java
 *
 * Created on May 28, 2014, 6:i45 PM
 */

package gov.bnl.gums;

/** 
 * Represent a local user identity in GUMS, which is a username and a primary group name.
 *
 * @author Brian Bockelman
 */
public class AccountInfo {

    private final String user;
    private final String group;
    
    /**
     * Creates an account info object with no user or group info.
     */
    public AccountInfo() {
        this(null, null);
    }
    
    /**
     * Creates an account info object with only a username.
     */
    public AccountInfo(String user) {
    	this(user, null);
    }
    
    /**
     * Creates an account info object with a username and a groupname
     * @param user The local username (i.e. "jsmith")
     * @param group The local primary user group (i.e. "wheel")
     *
     */
    public AccountInfo(String user, String group) {
        this.user = user;
        this.group = group;
    }

    /**
     * A GridUser will be equal only to another GridUser with the same DN and FQAN.
     * 
     * @param obj another object
     * @return true if the object was a GridUser with equivalent credentials
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof AccountInfo)) {
            return false;
        }
        AccountInfo other = (AccountInfo)obj;
        return ((other.getUser() == user) && (other.getGroup() == group));
    }

    @Override
    public int hashCode() {
        int hash = 1;
        hash = hash * 17 + (user == null ? 0 : user.hashCode());
        hash = hash * 31 + (group == null ? 0 : group.hashCode());
        return hash;
    }

    /**
     * Retrieve the user account name (may be null).
     * 
     * @return The local user account (i.e. "jsmith")
     */
    public String getUser() {
        return this.user;
    }
    
    /**
     * Retrieve the primary group account name (may be null).
     * 
     * @return The local primary group account (i.e. "wheel")
     */
    public String getGroup() {
        return this.group;
    }

    /**
     * Returns a legible String representation for the account info.
     * 
     * @return String reprentation of the account info (i.e. "AccountInfo[user=jsmith, group=wheel]")
     */
    public String toString() {
        if (group == null) {
            return "Account[user=" + user + "]";
        }
        return "Account[user=" + user + ", group=" + group + "]";
    }

}
