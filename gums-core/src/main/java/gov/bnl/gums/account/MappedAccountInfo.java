
package gov.bnl.gums.account;

import java.sql.Timestamp;

/**
 * Model of a mapped account
 *
 * @author Brian Bockelman
 */
public interface MappedAccountInfo {

    public String getAccount();
    public String getDn();
    public Timestamp getLastuse();
    public boolean getRecycle();
}

