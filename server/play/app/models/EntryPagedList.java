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

    public enum PagedSortBy {
        TECH("tech", "e.tech_id"),
        TIME("time", "e.entry_time"),
        PROJECT_ID("project", "e.project_id"),
        TRUCK_NUMBER("truck", "e.truck_id"),
        COMPANY_NAME("company", "e.company_id"),
        STREET("street", "e.company_id"),
        CITY("city", "e.company_id"),
        STATE("state", "e.company_id"),
        ZIP("zipcode", "e.company_id");

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

        public TermMatch(TermMatch other) {
            mTerm = other.mTerm;
            mPos = other.mPos;
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

        SearchTerms() {
            mTerms = new ArrayList<>();
        }

        public SearchTerms(SearchTerms other) {
            mTerms = new ArrayList<>(other.mTerms);
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

        boolean hasMatch(String element) {
            String elementNoCase = element.toLowerCase();
            boolean hadFailure = false;
            boolean hadMatch = false;
            for (String term : mTerms) {
                int pos = elementNoCase.indexOf(term);
                if (pos >= 0) {
                    return true;
                }
            }
            return false;
        }

        ArrayList<TermMatch> match(String element) {
            ArrayList<TermMatch> matches = new ArrayList<>();
            String elementNoCase = element.toLowerCase();
            TermMatch match;
            int startPos = 0;
            for (String term : mTerms) {
                int pos = elementNoCase.indexOf(term, startPos);
                if (pos >= 0) {
                    match = new TermMatch(term, pos);
                    matches.add(match);
                    startPos = match.end();
                }
            }
            Collections.sort(matches);
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

        String detectSingleQuote(String term) {
            return term;
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

        // Logical AND for multiple terms.
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
    List<Long> mSearchFilterByProject = new ArrayList<Long>();
    long mByTruckId;
    int mRowNumber;
    boolean mAllEntries;

    public EntryPagedList() {
    }

    // Used by export(), so no paging.
    public EntryPagedList(EntryPagedList other) {
        mParams.mSortBy = other.mParams.mSortBy;
        mParams.mOrder = other.mParams.mOrder;
        mSearch = new SearchTerms(other.mSearch);
        mLimitByProject = new ArrayList<>(other.mLimitByProject);
        mLimitByCompanyName = new ArrayList<>(other.mLimitByCompanyName);
        mByTruckId = other.mByTruckId;
        mRowNumber = 0;
        mAllEntries = true;
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

    public boolean isOrderDesc() {
        return ((mParams.mOrder == null) || mParams.mOrder.equals("desc"));
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
        query.append(" FROM entry AS e");
        query.append(" INNER JOIN entry_equipment_collection AS eqc ON e.equipment_collection_id = eqc.collection_id");
        query.append(" INNER JOIN equipment AS eq ON eqc.equipment_id = eq.id");

        if (mByTruckId > 0) {
            query.append(" WHERE ");
            query.append("e.truck_id=");
            query.append(mByTruckId);
        } else {
            StringBuilder whereQuery = new StringBuilder();
            String search = getWhereSearch();
            if (search.length() > 0) {
                whereQuery.append("(");
                whereQuery.append(search);
                whereQuery.append(")");
            }
            String limit = getLimitFilters();
            if (limit.length() > 0) {
                if (whereQuery.length() > 0) {
                    whereQuery.append(" AND ");
                }
                whereQuery.append("(");
                whereQuery.append(limit);
                whereQuery.append(")");
            }
            if (whereQuery.length() > 0) {
                query.append(" WHERE ");
                query.append(whereQuery.toString());
            }
        }
        query.append(" ORDER BY ");
        query.append(getSortBy());
        query.append(" ");
        query.append(getOrder());
        if (useLimit) {
            int start = mParams.mPage * mParams.mPageSize;
            query.append(" LIMIT ");
            query.append(start);
            query.append(", ");
            query.append(mParams.mPageSize);
        }
        return query.toString();
    }


    private String getWhereSearch() {
        StringBuilder query = new StringBuilder();
        appendSearch(query, "e.project_id", getSearchByProject());
        appendSearch(query, "e.company_id", getSearchByCompany());
        appendSearch(query, "e.truck_id", getSearchByTruck());
        appendSearch(query, "eq.id", getSearchByEquipment());
        List<Long> techs = getSearchByTechnician();
        appendSearch(query, "e.tech_id", techs);
        if (techs.size() > 0) {
            List<Long> entries_ids = SecondaryTechnician.findMatches(techs);
            appendSearch(query, "e.id", entries_ids);
        }
        return query.toString();
    }

    private void appendSearch(StringBuilder query, String prefix, List<Long> items) {
        if (items.size() == 0) {
            return;
        }
        if (query.length() > 0) {
            query.append(" OR ");
        }
        query.append(prefix);
        query.append(" IN ");
        query.append("(");
        boolean first = true;
        for (long id : items) {
            if (first) {
                first = false;
            } else {
                query.append(", ");
            }
            query.append(id);
        }
        query.append(")");
    }

    private List<Long> getSearchByProject() {
        HashSet<Long> set = new HashSet<Long>();
        for (String term : mSearch.mTerms) {
            set.addAll(Project.findMatches(term));
        }
        List<Long> list = new ArrayList<Long>();
        list.addAll(set);
        return list;
    }

    private List<Long> getSearchByCompany() {
        HashSet<Long> set = new HashSet<Long>();

        for (String term : mSearch.mTerms) {
            set.addAll(Company.findMatches(term));
        }
        List<Long> list = new ArrayList<Long>();
        list.addAll(set);
        return list;
    }

    private List<Long> getSearchByTechnician() {
        HashSet<Long> set = new HashSet<Long>();
        for (String term : mSearch.mTerms) {
            set.addAll(Technician.findMatches(term));
        }
        List<Long> list = new ArrayList<Long>();
        list.addAll(set);
        return list;
    }

    private List<Long> getSearchByTruck() {
        HashSet<Long> set = new HashSet<Long>();
        for (String term : mSearch.mTerms) {
            set.addAll(Truck.findMatches(term));
        }
        List<Long> list = new ArrayList<Long>();
        list.addAll(set);
        return list;
    }

    private List<Long> getSearchByEquipment() {
        HashSet<Long> set = new HashSet<Long>();
        for (String term : mSearch.mTerms) {
            set.addAll(Equipment.findMatches(term));
        }
        List<Long> list = new ArrayList<Long>();
        list.addAll(set);
        return list;
    }

    private String getLimitFilters() {
        StringBuilder projects = new StringBuilder();
        boolean first = true;
        for (long project_id : mLimitByProject) {
            if (first) {
                first = false;
            } else {
                projects.append(" OR ");
            }
            projects.append("e.project_id = ");
            projects.append(project_id);
        }
        StringBuilder companies = new StringBuilder();
        first = true;
        for (String companyName : mLimitByCompanyName) {
            Company company = Company.findByName(companyName);
            if (company != null) {
                if (first) {
                    first = false;
                } else {
                    companies.append(" OR ");
                }
                companies.append("e.company_id = '");
                companies.append(company.id);
            }
        }
        StringBuilder query = new StringBuilder();
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
        return query.toString();
    }

    public InputSearch getInputSearch() {
        return new InputSearch(mSearch.getAll());
    }

    public void clearCache() {
        mResult.mNumTotalRows = 0;
    }

    public void compute() {
        List<SqlRow> entries;
        String query;
        query = buildQuery(!mAllEntries);
        Logger.debug("Query: " + query);
        entries = Ebean.createSqlQuery(query).findList();
        mResult.mList.clear();
        if (entries == null || entries.size() == 0) {
            return;
        }
        for (SqlRow row : entries) {
            mResult.mList.add(parseEntry(row));
        }
        if (mSearch.hasMultipleTerms()) {
            mSearch.refine();
            mResult.mNumTotalRows = mResult.mList.size();
            mParams.mPageSize = mResult.mList.size();
            mParams.mPage = 0;
        } else if (mAllEntries) {
            mResult.mNumTotalRows = mResult.mList.size();
            mParams.mPageSize = mResult.mList.size();
            mParams.mPage = 0;
        }
    }

    public boolean hasRows() {
        return mResult.mList.size() > 0;
    }

    public long computeTotalNumRows() {
        if (mResult.mNumTotalRows == 0) {
            if (mSearch.hasMultipleTerms()) {
                mResult.mNumTotalRows = mResult.mList.size();
            } else if (!mSearch.hasSearch() && mLimitByProject.size() == 0) {
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
        return mResult.mList;
    }

    public List<Entry> getOrderedList() {
        if (isOrderDesc()) {
            List<Entry> reversed = new ArrayList<>(mResult.mList);
            Collections.reverse(reversed);
            return reversed;
        }
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
        setSearch(null);
    }

    public void setSearch(String search) {
        mSearch.setSearch(search);
        mByTruckId = 0;
        mParams.mPage = 0;
        mParams.mPageSize = PAGE_SIZE;
    }
}
