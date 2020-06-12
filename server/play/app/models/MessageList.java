/**
 * Copyright 2019, FleetTLC. All rights reserved
 */
package models;

import java.util.*;

import com.avaje.ebean.*;

import play.db.ebean.*;
import play.Logger;

import play.twirl.api.Html;
import views.formdata.InputSearch;

public class MessageList {

    public static class Option {
        public String text;
        public String selected;

        public Option(int pageSize, boolean selected) {
            this.text = Integer.toString(pageSize);
            this.selected = selected ? "selected" : "";
        }
    }

    static final int[] PAGE_SIZES = {100, 200, 400, 800, 1600, 3200, 6400, 12800};
    int mPageSize = PAGE_SIZES[0];
    PagedList<Message> mList;

    public MessageList() {
    }

    public void compute(int page, String sortBy, String order) {
        mList = Message.list(page, mPageSize, sortBy, order);
    }

    public int getTotalRowCount() {
        return mList.getTotalRowCount();
    }

    public List<Message> getList() {
        return mList.getList();
    }

    public boolean hasPrev() {
        return mList.hasPrev();
    }

    public boolean hasNext() {
        return mList.hasNext();
    }

    public int getPageIndex() {
        return mList.getPageIndex();
    }

    public String getDisplayXtoYofZ(String a1, String a2) {
        return mList.getDisplayXtoYofZ(a1, a2);
    }

    public List<Option> getPageSizes() {
        ArrayList<Option> list = new ArrayList<>();
        for (int pageSize : PAGE_SIZES) {
            list.add(new Option(pageSize, pageSize == mPageSize));
        }
        return list;
    }

    public void setPageSize(int pageSize) {
        mPageSize = pageSize;
    }
}
