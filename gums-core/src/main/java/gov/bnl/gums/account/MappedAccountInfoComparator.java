
package gov.bnl.gums.account;

import java.util.Comparator;

import java.sql.Timestamp;

public class MappedAccountInfoComparator implements Comparator<MappedAccountInfo> {

    @Override
    public int compare(MappedAccountInfo left, MappedAccountInfo right) {
        int result = left.getAccount().compareTo(right.getAccount());
        if (result != 0) {return result;}

        result = left.getDn().compareTo(right.getDn());
        if (result != 0) {return result;}

        result = (new Boolean(left.getRecycle())).compareTo(new Boolean(right.getRecycle()));
        if (result != 0) {return result;}

        return left.getLastuse().compareTo(right.getLastuse());
    }
}

