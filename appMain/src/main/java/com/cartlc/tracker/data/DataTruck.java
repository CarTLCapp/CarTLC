package com.cartlc.tracker.data;

/**
 * Created by dug on 8/31/17.
 */

public class DataTruck {

    public long   id;
    public long   serverId;
    public int    truckNumber;
    public String licensePlateNumber;

    public boolean equals(int truckNumber, String licensePlate) {
        if (this.truckNumber != truckNumber) {
            return false;
        }
        if (this.licensePlateNumber == null) {
            return (licensePlate == null);
        } else {
            return licensePlateNumber.equals(licensePlate);
        }
    }

    public boolean equals(DataTruck other) {
        if (truckNumber != other.truckNumber) {
            return false;
        }
        if (licensePlateNumber == null) {
            return other.licensePlateNumber == null;
        } else {
            return licensePlateNumber.equals(other.licensePlateNumber);
        }
    }

    public String toString() {
        StringBuilder sbuf = new StringBuilder();
        sbuf.append(id);
        sbuf.append(":#");
        sbuf.append(truckNumber);
        sbuf.append(",");
        sbuf.append(licensePlateNumber);
        return sbuf.toString();
    }
}
