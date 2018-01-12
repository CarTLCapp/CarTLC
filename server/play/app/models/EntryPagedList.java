package models;

import java.util.*;

import com.avaje.ebean.*;

import play.db.ebean.*;
import play.Logger;

import play.twirl.api.Html;

public class EntryPagedList {

    public enum SortBy {
        TECH("tech_id"),
        TIME("entry_time"),
        PROJECT("project_id"),
        TRUCK_NUMBER("truck_number"),
        COMPANY("company_id");

        String code;

        SortBy(String code) {
            this.code = code;
        }

        public String toString() { return code; }

        public static SortBy from(String code) {
            for (SortBy item : values()) {
                if (item.code.equals(code)) {
                    return item;
                }
            }
            Logger.error("Invalid sort by : " + code);
            return null;
        }
    }

    static final int PAGE_SIZE = 100;

    PagedList<Entry> mEntries;
    int mPage;
    int mRowNumber;
    int mPageSize = PAGE_SIZE;
    SortBy mSortBy = SortBy.from("entry_time");
    String mOrder = "desc";
    String mSearch;

    public EntryPagedList() {}

    public void setPage(int page) {
        mPage = page;
    }

    public void setSortBy(String sortBy) {
        mSortBy = SortBy.from(sortBy);
    }

    public String getSortBy() {
        return mSortBy.toString();
    }

    public void setOrder(String order) {
        mOrder = order;
    }

    public String getOrder() {
        return mOrder;
    }

    public void clearCache() {
    }

    public void computeFilters(Client client) {
        setProjects(client);
        setCompanies(client);
    }

    void setProjects(Client client) {
    }

    void setCompanies(Client client) {
    }

    public void compute() {
        mEntries = Entry.list(mPage, mPageSize, getSortBy(), getOrder());
    }

    public List<Entry> getList() {
        compute();
        return mEntries.getList();
    }

    public int getTotalRowCount() {
        return mEntries.getTotalRowCount();
    }

    public void resetRowNumber() {
        mRowNumber = mPage * mPageSize;
    }

    public void incRowNumber() {
        mRowNumber++;
    }

    public String getRowNumber() {
        return Integer.toString(mRowNumber);
    }

    public boolean hasPrev() {
        return mEntries.hasPrev();
    }

    public boolean hasNext() {
        return mEntries.hasNext();
    }

    public int getPageIndex() {
        return mEntries.getPageIndex();
    }

    public String getDisplayXtoYofZ(String to, String of) {
        return mEntries.getDisplayXtoYofZ(to, of);
    }

    public Html highlightSearch(String element) {
        if (element != null && mSearch != null && !mSearch.isEmpty()) {
            if (element.contains(mSearch)) {
                int pos = element.indexOf(mSearch);
                if (pos >= 0) {
                    StringBuilder sbuf = new StringBuilder();
                    sbuf.append(element.substring(0, pos));
                    sbuf.append("<mark>");
                    sbuf.append(element.substring(pos, mSearch.length() + pos));
                    sbuf.append("</mark>");
                    sbuf.append(element.substring(mSearch.length() + pos));
                    return Html.apply(sbuf.toString());
                }
            }
        }
        return Html.apply(element);
    }

    public void setSearch(String search) {
        if (search != null) {
            mSearch = search.trim();
        } else {
            mSearch = null;
        }
    }

}
