/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package models;

import java.util.*;

import com.avaje.ebean.*;

import play.db.ebean.*;
import play.Logger;

import play.twirl.api.Html;

public class EntryPagedList {

    public enum Logic {
        AND("AND"),
        OR("OR");

        final String display;

        Logic(String text) {
            this.display = text;
        }

        public String getDisplay() {
            return display;
        }

        public static ArrayList<String> items() {
            ArrayList<String> items = new ArrayList<>();
            for (Logic item : values()) {
                items.add(item.display);
            }
            return items;
        }

        public static Logic from(String display) {
            if (display == null) {
                return Logic.OR;
            }
            for (Logic item : values()) {
                if (item.display.equals(display)) {
                    return item;
                }
            }
            return Logic.OR;
        }
    }

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

        String alias;
        String code;

        PagedSortBy(String alias, String code) {
            this.code = code;
            this.alias = alias;
        }

        public String toString() {
            return code;
        }

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

    class TermMatch implements Comparable<TermMatch> {
        String mTerm;
        int mPos;

        public TermMatch(String term, int pos) {
            mTerm = term;
            mPos = pos;
        }

        @Override
        public int compareTo(TermMatch item) {
            return mPos - item.mPos;
        }

        int start() {
            return mPos;
        }

        int end() {
            return mPos + mTerm.length();
        }

        public String toString() {
            StringBuilder sbuf = new StringBuilder();
            sbuf.append("term=");
            sbuf.append(mTerm);
            sbuf.append(", start=");
            sbuf.append(start());
            sbuf.append(", end=");
            sbuf.append(end());
            return sbuf.toString();
        }
    }

    class SearchTerms {
        List<String> mTerms;
        Logic mLogic = Logic.OR;

        SearchTerms() {
            mTerms = new ArrayList<>();
        }

        boolean hasSearch() {
            return mTerms.size() > 0;
        }

        boolean hasMultipleTerms() {
            return mTerms.size() > 1;
        }

        public void setSearch(String search) {
            mTerms.clear();
            if (search != null) {
                String[] terms = search.trim().toLowerCase().split(" +");
                for (String term : terms) {
                    if (term.trim().length() > 0) {
                        mTerms.add(term.trim().toLowerCase());
                    }
                }
            }
        }

        public void setLogic(Logic logic) {
            if (logic == null) {
                mLogic = Logic.OR;
            } else {
                mLogic = logic;
            }
        }

        public Logic getLogic() {
            return mLogic;
        }

        boolean hasMatch(String element) {
            String elementNoCase = element.toLowerCase();
            boolean hadFailure = false;
            boolean hadMatch = false;
            for (String term : mTerms) {
                int pos = elementNoCase.indexOf(term);
                if (pos >= 0) {
                    if (mLogic == Logic.OR) {
                        return true;
                    }
                    hadMatch = true;
                } else {
                    hadFailure = true;
                }
            }
            if (hadMatch && !hadFailure && mLogic == Logic.AND) {
                return true;
            }
            return false;
        }

        ArrayList<TermMatch> match(String element) {
            ArrayList<TermMatch> matches = new ArrayList<>();
            String elementNoCase = element.toLowerCase();
            TermMatch match;
            int startPos = 0;
            boolean hadFailure = false;
            for (String term : mTerms) {
                int pos = elementNoCase.indexOf(term, startPos);
                if (pos >= 0) {
                    match = new TermMatch(term, pos);
                    matches.add(match);
                    startPos = match.end();
                } else {
                    hadFailure = true;
                }
            }
            if (hadFailure && mLogic == Logic.AND) {
                matches.clear();
            } else {
                Collections.sort(matches);
            }
            return matches;

        }

        public Html highlight(String element) {
            if (!hasSearch() || element == null) {
                return Html.apply(element);
            }
            List<TermMatch> matches = match(element);
            StringBuilder sbuf = new StringBuilder();
            int curPos = 0;
            for (TermMatch match : matches) {
                sbuf.append(element.substring(curPos, match.start()));
                sbuf.append("<mark>");
                sbuf.append(element.substring(match.start(), match.end()));
                sbuf.append("</mark>");
                curPos = match.end();
            }
            sbuf.append(element.substring(curPos));
            return Html.apply(sbuf.toString());
        }

        String getPrimary() {
            return mTerms.get(0);
        }

        String getAll() {
            StringBuilder sbuf = new StringBuilder();
            for (String term : mTerms) {
                if (sbuf.length() > 0) {
                    sbuf.append(" ");
                }
                sbuf.append(term);
            }
            return sbuf.toString();
        }

        void refine() {
            ArrayList<Entry> outgoing = new ArrayList<>();
            List<String> subterms = mTerms.subList(1, mTerms.size());
            for (Entry entry : mResult.mList) {
                if (entry.match(subterms)) {
                    outgoing.add(entry);
                }
            }
            mResult.mList = outgoing;
        }
    }

    class Parameters {
        int mPage;
        int mPageSize = PAGE_SIZE;
        PagedSortBy mSortBy = PagedSortBy.from("time");
        String mOrder = "desc";
    }

    class Result {
        List<Entry> mList = new ArrayList<Entry>();
        long mNumTotalRows;
    }

    static final int PAGE_SIZE = 100;

    Parameters mParams = new Parameters();
    Result mResult = new Result();
    SearchTerms mSearch = new SearchTerms();
    List<Long> mLimitByProject = new ArrayList<Long>();
    List<String> mLimitByCompanyName = new ArrayList<String>();
    long mByTruckId;
    int mRowNumber;

    public EntryPagedList() {
    }

    public void setPage(int page) {
        if (page < 0) {
            page = 0;
        } else {
            int next = page * mParams.mPageSize;
            if (next >= mResult.mNumTotalRows) {
                page = mParams.mPage;
            }
        }
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

    public void computeFilters(Client client) {
        setProjects(client);
        setCompanies(client);
    }

    void setProjects(Client client) {
        mLimitByProject.clear();
        List<Project> projects = client.getProjects();
        if (projects != null) {
            for (Project project : projects) {
                mLimitByProject.add(project.id);
            }
        }
    }

    void setCompanies(Client client) {
        mLimitByCompanyName.clear();
        mLimitByCompanyName.addAll(client.getCompanyNames());
    }

    public void setByTruckId(long truck_id) {
        mByTruckId = truck_id;
    }

    public boolean isByTruck() {
        return mByTruckId != 0;
    }

    public String getByTruckLine() {
        Truck truck = Truck.find.byId(mByTruckId);
        if (truck != null) {
            return truck.getLine();
        }
        return "";
    }

    String buildQuery(boolean useLimit) {
        StringBuilder query = new StringBuilder();

        query.append("SELECT DISTINCT e.id, e.tech_id, e.entry_time, e.project_id, e.company_id");
        query.append(", e.equipment_collection_id");
        query.append(", e.picture_collection_id");
        query.append(", e.note_collection_id");
        query.append(", e.truck_id, e.status, e.time_zone");
        for (PagedSortBy sortBy : PagedSortBy.values()) {
            query.append(", ");
            query.append(sortBy.code);
        }
        query.append(" FROM entry AS e");
        query.append(" INNER JOIN company AS c ON e.company_id = c.id");
        query.append(" INNER JOIN project AS p ON e.project_id = p.id");
        query.append(" INNER JOIN technician AS te ON e.tech_id = te.id");
        query.append(" INNER JOIN truck AS tr ON e.truck_id = tr.id");
        query.append(" INNER JOIN entry_equipment_collection AS eqc ON e.equipment_collection_id = eqc.collection_id");
        query.append(" INNER JOIN equipment AS eq ON eqc.equipment_id = eq.id");
        query.append(" LEFT JOIN secondary_technician AS ste ON e.id = ste.entry_id");
        query.append(" LEFT JOIN technician AS te2 ON ste.secondary_tech_id = te2.id");

        if (mByTruckId > 0) {
            query.append(" WHERE ");
            query.append("e.truck_id=");
            query.append(mByTruckId);
        } else if (mSearch.hasSearch()) {
            query.append(" WHERE (");
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
            query.append(appendSearch("te2.first_name"));
            query.append(" OR ");
            query.append(appendSearch("te2.last_name"));
            query.append(" OR ");
            query.append(appendSearch("eq.name"));
            query.append(")");
            if (mLimitByProject.size() > 0 || mLimitByCompanyName.size() > 0) {
                query.append(" AND ");
                query.append("(");
                addFilters(query);
                query.append(")");
            }
        } else {
            if (mLimitByProject.size() > 0 || mLimitByCompanyName.size() > 0) {
                query.append(" WHERE ");
                addFilters(query);
            }
        }
        if (useLimit) {
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

    void addFilters(StringBuilder query) {
        StringBuilder projects = new StringBuilder();
        boolean first = true;
        for (long project_id : mLimitByProject) {
            if (first) {
                first = false;
            } else {
                projects.append(" OR ");
            }
            projects.append("p.id = ");
            projects.append(project_id);
        }
        StringBuilder companies = new StringBuilder();
        first = true;
        for (String companyName : mLimitByCompanyName) {
            if (first) {
                first = false;
            } else {
                companies.append(" OR ");
            }
            companies.append("c.name = '");
            companies.append(companyName);
            companies.append("'");
        }
        if (projects.length() > 0 && companies.length() > 0) {
            query.append("(");
            query.append(projects.toString());
            query.append(") AND (");
            query.append(companies.toString());
            query.append(")");
        } else if (projects.length() > 0) {
            query.append(projects.toString());
        } else if (companies.length() > 0) {
            query.append(companies.toString());
        }
    }

    String appendSearch(String column) {
        return column + " LIKE '%" + mSearch.getPrimary() + "%'";
    }

    public InputSearch getInputSearch() {
        return new InputSearch(mSearch.getAll(), mSearch.getLogic().getDisplay());
    }

    public void clearCache() {
        mResult.mNumTotalRows = 0;
    }

    public void compute() {
        List<SqlRow> entries;
        String query;
        query = buildQuery(true);
        entries = Ebean.createSqlQuery(query).findList();
        mResult.mList.clear();
        if (entries == null || entries.size() == 0) {
            return;
        }
        for (SqlRow row : entries) {
            mResult.mList.add(parseEntry(row));
        }
    }

    public boolean hasRows() {
        return mResult.mList.size() > 0;
    }

    public long computeTotalNumRows() {
        if (mResult.mNumTotalRows == 0) {
            if (!mSearch.hasSearch() && mLimitByProject.size() == 0) {
                mResult.mNumTotalRows = Entry.find.where().findPagedList(0, 10).getTotalRowCount();
            } else {
                String query = buildQuery(false);
                List<SqlRow> entries = Ebean.createSqlQuery(query).findList();
                mResult.mNumTotalRows = entries.size();
            }
        }
        return mResult.mNumTotalRows;
    }

    Entry parseEntry(SqlRow row) {
        Entry entry = new Entry();
        entry.id = row.getLong("id");
        entry.tech_id = row.getInteger("tech_id");
        entry.entry_time = row.getDate("entry_time");
        entry.time_zone = row.getString("time_zone");
        entry.project_id = row.getLong("project_id");
        entry.company_id = row.getLong("company_id");
        entry.equipment_collection_id = row.getLong("equipment_collection_id");
        entry.picture_collection_id = row.getLong("picture_collection_id");
        entry.note_collection_id = row.getLong("note_collection_id");
        entry.truck_id = row.getLong("truck_id");
        if (row.get("status") != null) { // WHY DO I NEED THIS?
            entry.status = Entry.Status.from(getInteger(row, "status"));
        }
        return entry;
    }

    boolean hasEquipmentMatch(List<Entry> entries) {
        for (Entry entry : entries) {
            String line = entry.getEquipmentLine();
            if (mSearch.hasMatch(line)) {
                return true;
            }
        }
        return false;
    }

    Integer getInteger(SqlRow row, String column) {
        if (row.get(column) == null) {
            return null;
        }
        return row.getInteger(column);
    }

    public synchronized List<Entry> getList() {
        compute();
        return mResult.mList;
    }

    public long getTotalRowCount() {
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
        int next = (mParams.mPage + 1) * mParams.mPageSize;
        return (next < mResult.mNumTotalRows);
    }

    public int getPageIndex() {
        return mParams.mPage;
    }

    public String getDisplayingXtoYofZ() {
        StringBuilder sbuf = new StringBuilder();
        sbuf.append("Displaying ");
        long start = mParams.mPage * mParams.mPageSize;
        long last = start + mParams.mPageSize - 1;
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
        return mSearch.highlight(element);
    }

    public void clearSearch() {
        setSearch(null, null);
    }

    public void setSearch(String search) {
        setSearch(search, null);
    }

    public void setSearch(String search, String logic) {
        mSearch.setSearch(search);
        mSearch.setLogic(Logic.from(logic));
        mByTruckId = 0;
        mParams.mPage = 0;
        mParams.mPageSize = PAGE_SIZE;
    }
}
