package com.cartlc.tracker.data;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

/**
 * Created by dug on 8/31/17.
 */

public class DataTruck implements Comparable<DataTruck> {

    public long   id;
    public long   serverId;
    public int    truckNumber;
    public String licensePlateNumber;
    public long   projectNameId;
    public String companyName;

    // Used by List.remove(). See DCPing.queryTrucks().
    public boolean equals(Object other) {
        if (other instanceof DataTruck) {
            DataTruck o = (DataTruck) other;
            return o.id == id;
        }
        return false;
    }

    public boolean equals(DataTruck other) {
        if (truckNumber != other.truckNumber) {
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
        return true;
    }

    public String toString() {
        return toString(truckNumber, licensePlateNumber);
    }

    public String toLongString() {
        StringBuilder sbuf = new StringBuilder();
        if (truckNumber > 0) {
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
        }
        if (companyName != null) {
            if (sbuf.length() > 0) {
                sbuf.append(", ");
            }
            sbuf.append(companyName);
        }
        return sbuf.toString();
    }

    public static String toString(long truckNumber, String licensePlateNumber) {
        StringBuilder sbuf = new StringBuilder();
        if (truckNumber > 0) {
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
        return truckNumber - o.truckNumber;
    }
}
