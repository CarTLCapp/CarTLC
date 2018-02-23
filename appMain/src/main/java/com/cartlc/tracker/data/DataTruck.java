package com.cartlc.tracker.data;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

/**
 * Created by dug on 8/31/17.
 */

public class DataTruck implements Comparable<DataTruck> {

    public long    id;
    public long    serverId;
    public String  truckNumber;
    public String  licensePlateNumber;
    public long    projectNameId;
    public String  companyName;
    public boolean hasEntry;

    // Used by List.remove(). See DCPing.queryTrucks().
    public boolean equals(Object other) {
        if (other instanceof DataTruck) {
            DataTruck o = (DataTruck) other;
            return o.id == id;
        }
        return false;
    }

    public boolean equals(DataTruck other) {
        if (truckNumber == null) {
            if (other.truckNumber != null) {
                return false;
            }
        } else if (!truckNumber.equals(other.truckNumber)) {
            return false;
        }
        if (projectNameId != other.projectNameId) {
            return false;
        }
        if (companyName == null) {
            if (other.companyName != null) {
                return false;
            }
        } else if (!companyName.equals(other.companyName)) {
            return false;
        }
        if (licensePlateNumber == null) {
            if (other.licensePlateNumber != null) {
                return false;
            }
        } else if (!licensePlateNumber.equals(other.licensePlateNumber)) {
            return false;
        }
        if (hasEntry != other.hasEntry) {
            return false;
        }
        return true;
    }

    public String toString() {
        return toString(truckNumber, licensePlateNumber);
    }

    public String toLongString() {
        StringBuilder sbuf = new StringBuilder();
        sbuf.append(id);
        if (serverId != 0) {
            sbuf.append(" [");
            sbuf.append(serverId);
            sbuf.append("]");
        }
        if (truckNumber != null) {
            sbuf.append(", ");
            sbuf.append(truckNumber);
        }
        if (!TextUtils.isEmpty(licensePlateNumber)) {
            if (sbuf.length() > 0) {
                sbuf.append(" : ");
            }
            sbuf.append(licensePlateNumber);
        }
        if (projectNameId > 0) {
            if (sbuf.length() > 0) {
                sbuf.append(", ");
            }
            sbuf.append(TableProjects.getInstance().queryProjectName(projectNameId));
            sbuf.append("(");
            sbuf.append(projectNameId);
            sbuf.append(")");
        }
        if (companyName != null) {
            if (sbuf.length() > 0) {
                sbuf.append(", ");
            }
            sbuf.append(companyName);
        }
        if (hasEntry) {
            sbuf.append(", HASENTRY");
        }
        return sbuf.toString();
    }

    public static String toString(String truckNumber, String licensePlateNumber) {
        StringBuilder sbuf = new StringBuilder();
        if (truckNumber != null) {
            sbuf.append(truckNumber);
        }
        if (!TextUtils.isEmpty(licensePlateNumber)) {
            if (sbuf.length() > 0) {
                sbuf.append(" : ");
            }
            sbuf.append(licensePlateNumber);
        }
        return sbuf.toString();
    }

    @Override
    public int compareTo(@NonNull DataTruck o) {
        if (truckNumber == null) {
            if (o.truckNumber == null) {
                return 0;
            }
            return -1;
        } else if (o.truckNumber == null) {
            return 1;
        }
        return truckNumber.compareTo(o.truckNumber);
    }
}
