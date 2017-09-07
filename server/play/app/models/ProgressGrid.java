package models;

import java.util.*;
import java.lang.Math;
import java.lang.Integer;

import com.avaje.ebean.*;

import play.db.ebean.*;
import play.Logger;

public class ProgressGrid extends WorkOrderList {

    final static int MAX_COLS = 25;

    int mNumCols;

    public ProgressGrid() {
        super();
        setOrder(Order.ASC);
        setSortBy(SortBy.TRUCK_NUMBER);
        setPageSize(MAX_COLS * MAX_COLS * 2);
    }

    @Override
    public void compute() {
        super.compute();
        mNumCols = (int) Math.ceil(Math.sqrt((double) mComputed.size()));
        if (mNumCols > MAX_COLS) {
            mNumCols = MAX_COLS;
        }
    }

    int index(int row, int col) {
        return row * mNumCols + col;
    }

    WorkOrder getWorkOrder(int row, int col) {
        int i = index(row, col);
        if (i < mComputed.size()) {
            return mComputed.get(i);
        }
        return null;
    }

    public String getCellString(int row, int col) {
        WorkOrder order = getWorkOrder(row, col);
        if (order == null) {
            return "";
        }
        int truck_number = order.getTruckNumber();
        if (truck_number == 0) {
            return "";
        }
        return Integer.toString(truck_number);
    }

    public String getCellColor(int row, int col) {
        WorkOrder order = getWorkOrder(row, col);
        if (order == null) {
            return "";
        }
        Entry entry = Entry.getFulfilledBy(order);
        if (entry == null) {
            return "";
        }
        return entry.getCellColor();
    }

    public int getNumRows() {
        return mComputed.size() / mNumCols + 1;
    }

    public int getNumCols() {
        return mNumCols;
    }
}
