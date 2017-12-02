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

    public boolean equals(int truckNumber, String licensePlate) {
        if (this.truckNumber == truckNumber) {
            return true;
        }
        if (this.licensePlateNumber == null) {
            return (licensePlate == null);
        } else {
            return licensePlateNumber.equals(licensePlate);
        }
    }

    public boolean equals(DataTruck other) {
        if (truckNumber != 0 && truckNumber == other.truckNumber) {
            return true;
        }
        if (licensePlateNumber == null) {
            return other.licensePlateNumber == null;
        } else {
            return licensePlateNumber.equals(other.licensePlateNumber);
        }
    }

    public String toString() {
        return toString(truckNumber, licensePlateNumber);
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
