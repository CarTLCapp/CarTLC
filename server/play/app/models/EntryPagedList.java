package models;

import java.util.*;

import com.avaje.ebean.*;

import play.db.ebean.*;
import play.Logger;

import play.twirl.api.Html;

public class EntryPagedList {

    public enum PagedSortBy {
        TECH("tech_name", "te.last_name"),
        TIME("time", "e.entry_time"),
        PROJECT("project_name", "p.name"),
        TRUCK_NUMBER("truck_number", "tr.truck_number"),
        COMPANY_NAME("company_name", "c.name"),
        STREET("street", "c.street"),
        CITY("city", "c.city"),
        STATE("state", "c.state"),
        ZIP("zipcode", "c.zipcode");

        String code;
        String alias;

        PagedSortBy(String alias, String code) {
            this.code = code;
            this.alias = alias;
        }

        public String toString() { return code; }

        public static PagedSortBy from(String match) {
            for (PagedSortBy item : values()) {
                if (item.code.equals(match) || item.alias.equals(match)) {
                    return item;
                }
            }
            Logger.error("Invalid sort by : " + match);
            return null;
        }
    }

    class Parameters {
        int mPage;
        int mPageSize = PAGE_SIZE;
        PagedSortBy mSortBy = PagedSortBy.from("time");
        String mOrder = "desc";
        String mSearch;

        boolean hasSearch() {
            return mSearch != null && !mSearch.isEmpty();
        }
    }

    class Result {
        List<Entry> mList = new ArrayList<Entry>();
        int mNumTotalRows;
    }

    static final int PAGE_SIZE = 100;

    Parameters mParams = new Parameters();
    Result mResult = new Result();
    int mRowNumber;

    public EntryPagedList() {}

    public void setPage(int page) {
        mParams.mPage = page;
    }

    public void setSortBy(String sortBy) {
        if (sortBy == null) {
            mParams.mSortBy = PagedSortBy.TIME;
        } else {
            mParams.mSortBy = PagedSortBy.from(sortBy);
        }
    }

    public String getSortBy() {
        return mParams.mSortBy.toString();
    }

    public void setOrder(String order) {
        if (order == null) {
            mParams.mOrder = "desc";
        } else {
            mParams.mOrder = order;
        }
    }

    public String getOrder() {
        return mParams.mOrder;
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

    String buildQuery(boolean countOnly) {
        StringBuilder query = new StringBuilder();
        query.append("SELECT e.id, e.tech_id, e.entry_time, e.project_id, e.company_id");
        query.append(", e.equipment_collection_id");
        query.append(", e.picture_collection_id");
        query.append(", e.note_collection_id");
        query.append(", e.truck_id, e.status");
        query.append(", c.name, c.street, c.city, c.state, c.zipcode");
        query.append(", p.name");
        query.append(", te.id, te.first_name, te.last_name");
        query.append(", tr.id, tr.truck_number, tr.license_plate");
        query.append(", eqc.collection_id, eqc.equipment_id");
        query.append(", eq.name");
        query.append(" FROM entry AS e");
        query.append(" INNER JOIN company AS c ON e.company_id = c.id");
        query.append(" INNER JOIN project AS p ON e.project_id = p.id");
        query.append(" INNER JOIN technician AS te ON e.tech_id = te.id");
        query.append(" INNER JOIN truck AS tr ON e.truck_id = tr.id");
        query.append(" INNER JOIN entry_equipment_collection AS eqc ON e.equipment_collection_id = eqc.collection_id");
        query.append(" INNER JOIN equipment AS eq ON eqc.equipment_id = eq.id");
        if (mParams.hasSearch()) {
            final String search = mParams.mSearch;
            query.append(" WHERE ");
            query.append(appendSearch("c.name"));
            query.append(" OR ");
            query.append(appendSearch("c.city"));
            query.append(" OR ");
            query.append(appendSearch("c.street"));
            query.append(" OR ");
            query.append(appendSearch("c.state"));
            query.append(" OR ");
            query.append(appendSearch("c.zipcode"));
            query.append(" OR ");
            query.append(appendSearch("p.name"));
            query.append(" OR ");
            query.append(appendSearch("te.first_name"));
            query.append(" OR ");
            query.append(appendSearch("te.last_name"));
            query.append(" OR ");
            query.append(appendSearch("tr.truck_number"));
            query.append(" OR ");
            query.append(appendSearch("tr.license_plate"));
            query.append(" OR ");
            query.append(appendSearch("eq.name"));
        }
        if (!countOnly) {
            query.append(" ORDER BY ");
            query.append(getSortBy());
            query.append(" ");
            query.append(getOrder());

            int start = mParams.mPage * mParams.mPageSize;
            query.append(" LIMIT ");
            query.append(start);
            query.append(", ");
            query.append(mParams.mPageSize);
        }
        return query.toString();
    }

    String appendSearch(String column) {
        return column + " LIKE BINARY '%" + mParams.mSearch + "%'";
    }

    public InputLines getSearchInputLines() {
        return new InputLines(mParams.mSearch);
    }

    public void compute() {
        List<SqlRow> entries;
        if (mParams.hasSearch()) {
            String query = buildQuery(true);
            entries = Ebean.createSqlQuery(query).findList();
            mResult.mNumTotalRows = entries.size();
        } else {
            mResult.mNumTotalRows = Entry.find.where().findPagedList(0, 10).getTotalRowCount();
        }
        String query = buildQuery(false);
        entries = Ebean.createSqlQuery(query).findList();

        mResult.mList.clear();
        if (entries == null || entries.size() == 0) {
            return;
        }
        for (SqlRow row : entries) {
            Entry entry = new Entry();
            entry.id = row.getLong("id");
            entry.tech_id = row.getInteger("tech_id");
            entry.entry_time = row.getDate("entry_time");
            entry.project_id = row.getLong("project_id");
            entry.company_id = row.getLong("company_id");
            entry.equipment_collection_id = row.getLong("equipment_collection_id");
            entry.picture_collection_id = row.getLong("picture_collection_id");
            entry.note_collection_id = row.getLong("note_collection_id");
            entry.truck_id = row.getLong("truck_id");
            if (row.get("status") != null) { // WHY DO I NEED THIS?
                entry.status = Entry.Status.from(getInteger(row, "status"));
            }
            mResult.mList.add(entry);
        }
    }

    Integer getInteger(SqlRow row, String column) {
        if (row.get(column) == null) {
            return null;
        }
        return row.getInteger(column);
    }

    public List<Entry> getList() {
        compute();
        return mResult.mList;
    }

    public int getTotalRowCount() {
        return mResult.mNumTotalRows;
    }

    public void resetRowNumber() {
        mRowNumber = mParams.mPage * mParams.mPageSize;
    }

    public void incRowNumber() {
        mRowNumber++;
    }

    public String getRowNumber() {
        return Integer.toString(mRowNumber);
    }

    public boolean hasPrev() {
        return mParams.mPage > 0;
    }

    public boolean hasNext() {
        int next = (mParams.mPage+1) * mParams.mPageSize;
        return (next < mResult.mNumTotalRows);
    }

    public int getPageIndex() {
        return mParams.mPage;
    }

    public String getDisplayXtoYofZ(String to, String of) {
        StringBuilder sbuf = new StringBuilder();
        sbuf.append("Displaying ");
        int start = mParams.mPage * mParams.mPageSize;
        int last = start + mParams.mPageSize - 1;
        if (last >= mResult.mNumTotalRows) {
            last = mResult.mNumTotalRows - 1;
        }
        sbuf.append(start);
        sbuf.append(" - ");
        sbuf.append(last);
        sbuf.append(" of ");
        sbuf.append(mResult.mNumTotalRows);
        return sbuf.toString();
    }

    public Html highlightSearch(String element) {
        if (!mParams.hasSearch() || element == null) {
            return Html.apply(element);
        }
        final String search = mParams.mSearch;
        if (element.contains(search)) {
            int pos = element.indexOf(search);
            if (pos >= 0) {
                StringBuilder sbuf = new StringBuilder();
                sbuf.append(element.substring(0, pos));
                sbuf.append("<mark>");
                sbuf.append(element.substring(pos, search.length() + pos));
                sbuf.append("</mark>");
                sbuf.append(element.substring(search.length() + pos));
                return Html.apply(sbuf.toString());
            }
        }
        return Html.apply(element);
    }

    public void setSearch(String search) {
        if (search != null) {
            mParams.mSearch = search.trim();
        } else {
            mParams.mSearch = null;
        }
        mParams.mPage = 0;
    }

}
