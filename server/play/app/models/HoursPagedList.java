/**
 * Copyright 2021, FleetTLC. All rights reserved
 */
package models;

import java.util.*;
import java.text.SimpleDateFormat;

import com.avaje.ebean.*;

import play.db.ebean.*;
import play.Logger;

import play.twirl.api.Html;
import views.formdata.InputSearch;

import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.TimeUnit;

public class HoursPagedList {

    public enum ColumnSelector {
        ALL("All", "all", null),
        TECH("Technician", "tech", "te.last_name"),
        DATE("Date", "date", "h.entry_time"),
        ROOT_PROJECT_ID("Root Project", "root_project", "r.name"),
        SUB_PROJECT_ID("Sub Project", "sub_project", "p.name");

        String text;
        String alias;
        String column;

        ColumnSelector(String text, String alias, String column) {
            this.text = text;
            this.alias = alias;
            this.column = column;
        }

        public String toString() {
            return alias;
        }

        public static ColumnSelector from(String match) {
            for (ColumnSelector item : values()) {
                if (item.alias.equals(match) || item.text.equals(match)) {
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

    class SearchInfo {
        String mSearchTerm;
        ColumnSelector mColumnSelector;

        SearchInfo() {
        }

        public SearchInfo(SearchInfo other) {
            mSearchTerm = other.mSearchTerm;
            mColumnSelector = other.mColumnSelector;
        }

        boolean hasSearch() {
            return mSearchTerm != null && mSearchTerm.length() > 0;
        }

        public void setSearchTerm(String search) {
            mSearchTerm = search;
        }

        public void setSearchField(ColumnSelector selector) {
            mColumnSelector = selector;
        }

        boolean hasPartialMatch(String element) {
            return StringUtils.containsIgnoreCase(element, mSearchTerm);
        }

        boolean hasMatch(String element) {
            return element.equalsIgnoreCase(mSearchTerm);
        }

        boolean hasSearchBy(ColumnSelector selector) {
            return (mColumnSelector == ColumnSelector.ALL || mColumnSelector == selector);
        }

        String getTerm() {
            return mSearchTerm;
        }

        String getTermChkNull() {
            if (mSearchTerm != null) {
                return mSearchTerm;
            }
            return "";
        }

        String getField() {
            if (mColumnSelector == null) {
                return ColumnSelector.ALL.alias;
            }
            return mColumnSelector.alias;
        }

        public Html highlight(String element, ColumnSelector selector) {
            if (!hasSearch() || element == null) {
                return Html.apply(element);
            }
            if (selector == mColumnSelector || mColumnSelector == ColumnSelector.ALL) {
                if (selector == ColumnSelector.TECH) {
                    if (hasPartialMatch(element)) {
                        return highlightPartial(element);
                    }
                }
                if (hasMatch(element)) {
                    StringBuilder sbuf = new StringBuilder();
                    sbuf.append("<mark>");
                    sbuf.append(element);
                    sbuf.append("</mark>");
                    return Html.apply(sbuf.toString());
                }
            }
            return Html.apply(element);
        }

        private Html highlightPartial(String element) {
            TermMatch match = matchPartial(element);
            if (match != null) {
                StringBuilder sbuf = new StringBuilder();
                sbuf.append(element.substring(0, match.start()));
                sbuf.append("<mark>");
                sbuf.append(element.substring(match.start(), match.end()));
                sbuf.append("</mark>");
                int curPos = match.end();
                sbuf.append(element.substring(curPos));
                return Html.apply(sbuf.toString());
            }
            return Html.apply(element);
        }

        TermMatch matchPartial(String element) {
            ArrayList<TermMatch> matches = new ArrayList<>();
            String elementNoCase = element.toLowerCase();
            String term = mSearchTerm.toLowerCase();
            int pos = elementNoCase.indexOf(term, 0);
            if (pos >= 0) {
                return new TermMatch(term, pos);
            }
            return null;
        }

    }

    class Parameters {
        int mPage;
        int mPageSize = mDefaultPageSize;
        ColumnSelector mSortBy = ColumnSelector.from("time");
        String mOrder = "desc";
    }

    class Result {
        List<Hours> mList = new ArrayList<Hours>();
        long mNumTotalRows;
    }

    static final boolean VERBOSE = false;
    static final int[] PAGE_SIZES = {100, 200, 300, 400, 500};
    static final String CLASS_PREV = "prev";
    static final String CLASS_PREV_DISABLED = "prev disabled";
    static final String CLASS_NEXT = "next";
    static final String CLASS_NEXT_DISABLED = "next disabled";

    private static final String PARSE_DATE_FORMAT = "MM/dd/yyyy";
    private SimpleDateFormat mParseDateFormat = new SimpleDateFormat(PARSE_DATE_FORMAT);
    private static final String PARSE_DATE_FORMAT_ZZZ = "MM/dd/yyyy zzz";
    private SimpleDateFormat mParseDateFormatZZZ = new SimpleDateFormat(PARSE_DATE_FORMAT_ZZZ);

    Parameters mParams = new Parameters();
    Result mResult = new Result();
    SearchInfo mSearch = new SearchInfo();
    List<Long> mLimitByProject = new ArrayList<Long>();
    int mRowNumber;
    int mDefaultPageSize = PAGE_SIZES[0];
    Date mSearchStartDate = null;
    Date mSearchEndDate = null;

    public long mForClientId = 0;

    public HoursPagedList() {
    }

    public HoursPagedList(HoursPagedList other) {
        mParams.mSortBy = other.mParams.mSortBy;
        mParams.mOrder = other.mParams.mOrder;
        mSearch = new SearchInfo(other.mSearch);
        mLimitByProject = new ArrayList<>(other.mLimitByProject);
        mResult.mNumTotalRows = other.mResult.mNumTotalRows;
        mRowNumber = 0;
    }

    public void setPage(int page) {
        if (page < 0) {
            page = 0;
        }
        mParams.mPage = page;
    }

    public void setPageSize(int pageSize) {
        mDefaultPageSize = pageSize;
        mParams.mPageSize = pageSize;
    }

    public int getPageSize() {
        return mParams.mPageSize;
    }

    public static class Option {
        public String text;
        public String value;
        public String selected;

        public Option(int pageSize, boolean selected) {
            this.text = Integer.toString(pageSize);
            this.value = this.text;
            this.selected = selected ? "selected" : "";
        }

        public Option(String text, String value, boolean selected) {
            this.text = text;
            this.value = value;
            this.selected = selected ? "selected" : "";
        }
    }

    public List<Option> getPageSizes() {
        ArrayList<Option> list = new ArrayList<>();
        for (int pageSize : PAGE_SIZES) {
            list.add(new Option(pageSize, pageSize == mParams.mPageSize));
        }
        return list;
    }

    public List<Option> getSearchFieldOptions() {
        ArrayList<Option> list = new ArrayList<>();
        for (ColumnSelector field : ColumnSelector.values()) {
            if (field.text != null && field.text.length() > 0) {
                list.add(new Option(field.text, field.alias, field.alias == getSearchField()));
            }
        }
        return list;
    }

    public void setSortBy(String sortBy) {
        if (sortBy == null) {
            mParams.mSortBy = ColumnSelector.DATE;
        } else {
            mParams.mSortBy = ColumnSelector.from(sortBy);
        }
    }

    public String getSortBy() {
        return mParams.mSortBy.alias;
    }

    public String getSortByColumn() {
        return mParams.mSortBy.column;
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

        if (client.is_admin) {
            mForClientId = 0;
        } else {
            mForClientId = client.id != null ? client.id : 0;
        }
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

    private String buildQuery(boolean useLimit) {
        StringBuilder query = new StringBuilder();

        query.append("SELECT DISTINCT h.id, h.tech_id, h.entry_time, h.project_id");
        query.append(", h.start_time, h.end_time, h.lunch_time, h.break_time, h.drive_time");
        query.append(", h.notes, h.time_zone, h.viewed");

        switch (mParams.mSortBy) {
            case TECH:
            case SUB_PROJECT_ID:
            case ROOT_PROJECT_ID:
                query.append(" , " + getSortByColumn());
                break;
        }
        query.append(" FROM hours AS h");

        switch (mParams.mSortBy) {
            case TECH:
                query.append(" INNER JOIN technician AS te ON e.tech_id = te.id");
                break;
            case ROOT_PROJECT_ID:
                query.append(" INNER JOIN project AS p ON e.project_id = p.id");
                query.append(" INNER JOIN root_project AS r ON p.root_project_id = r.id");
                break;
            case SUB_PROJECT_ID:
                query.append(" INNER JOIN project AS p ON e.project_id = p.id");
                break;
        }
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
        query.append(" ORDER BY ");
        query.append(getSortByColumn());
        query.append(" ");
        query.append(getOrder());
        if (useLimit) {
            if (mParams.mPageSize == 0) {
                mParams.mPageSize = mDefaultPageSize;
            }
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
        if (mSearch.hasSearch()) {
            if (mSearch.hasSearchBy(ColumnSelector.ROOT_PROJECT_ID) || mSearch.hasSearchBy(ColumnSelector.SUB_PROJECT_ID)) {
                appendSearch(query, "h.project_id", getSearchByProject());
            }
            if (mSearch.hasSearchBy(ColumnSelector.TECH)) {
                List<Long> techs = getSearchByTechnician();
                appendSearch(query, "h.tech_id", techs);
                if (techs.size() > 0) {
                    List<Long> entries_ids = SecondaryTechnician.findMatches(techs);
                    appendSearch(query, "h.id", entries_ids);
                }
            }
            if (mSearch.hasSearchBy(ColumnSelector.DATE) && getSearchByDate()) {
                boolean paren;
                if (query.length() > 0) {
                    query.append(" OR (");
                    paren = true;
                } else {
                    paren = false;
                }
                query.append("h.entry_time BETWEEN '");
                query.append(mParseDateFormatZZZ.format(mSearchStartDate));
                query.append("' AND '");
                query.append(mParseDateFormatZZZ.format(mSearchEndDate));
                query.append("'");
                if (paren) {
                    query.append(")");
                }
            }
        }
        return query.toString();
    }

    private void appendSearch(StringBuilder query, String prefix, List<Long> items) {
        if (items.size() == 0) {
            if (mSearch.mColumnSelector == ColumnSelector.ALL) {
                return;
            }
        }
        if (query.length() > 0) {
            query.append(" OR ");
        }
        query.append(prefix);
        query.append(" IN ");
        query.append("(");
        boolean first = true;
        if (items.size() == 0) {
            query.append("0");
        } else {
            for (long id : items) {
                if (first) {
                    first = false;
                } else {
                    query.append(", ");
                }
                query.append(id);
            }
        }
        query.append(")");
    }

    private List<Long> getSearchByProject() {
        HashSet<Long> set = new HashSet<Long>();
        if (mSearch.hasSearchBy(ColumnSelector.SUB_PROJECT_ID)) {
            set.addAll(Project.findMatches(mSearch.getTerm()));
        }
        if (mSearch.hasSearchBy(ColumnSelector.ROOT_PROJECT_ID)) {
            for (Long rootProjectId : getSearchByRootProject()) {
                set.addAll(Project.findWithRootProjectId(rootProjectId));
            }
        }
        List<Long> list = new ArrayList<Long>();
        list.addAll(set);
        return list;
    }

    private List<Long> getSearchByRootProject() {
        List<Long> list = new ArrayList<Long>();
        list.addAll(RootProject.findMatches(mSearch.getTerm()));
        return list;
    }

    private List<Long> getSearchByTechnician() {
        List<Long> list = new ArrayList<Long>();
        String term = mSearch.getTerm();
        String[] terms = term.split(" ");
        if (terms.length > 1) {
            list.addAll(Technician.findMatches(terms[0], terms[1]));
        } else {
            list.addAll(Technician.findMatches(term));
        }
        return list;
    }

    // Return start of scanned time (the beginning of the day)
    private boolean getSearchByDate() {
        // Translate entered date value into a concrete time local to the tech's time.
        try {
            Date date = parseDate(mSearch.getTerm().trim());
            if (date == null) {
                return false;
            }
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            mSearchStartDate = new Date(calendar.getTimeInMillis());
            calendar.add(Calendar.HOUR_OF_DAY, 24);
            mSearchEndDate = new Date(calendar.getTimeInMillis());
            return true;
        } catch (Exception ex) {
            Logger.error("Invalid date entered: " + ex.getMessage());
        }
        return false;
    }

    private Date parseDate(String term) {
        if (term.contains(" ")) {
            try {
                Date date = mParseDateFormatZZZ.parse(term);
                return date;
            } catch (Exception ex) {
                Logger.info(ex.getMessage());
            }
        }
        try {
            Date date = mParseDateFormat.parse(term);
            return date;
        } catch (Exception ex) {
            Logger.info(ex.getMessage());
        }
        return null;
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
            projects.append("h.project_id = ");
            projects.append(project_id);
        }
        StringBuilder query = new StringBuilder();
        if (projects.length() > 0) {
            query.append(projects.toString());
        }
        return query.toString();
    }

    public InputSearch getInputSearch() {
        return new InputSearch(mSearch.getTermChkNull(), mSearch.getField());
    }

    public void clearCache() {
        mResult.mNumTotalRows = 0;
    }

    public void compute() {
        List<SqlRow> entries;
        String query;
        query = buildQuery(true);
        if (VERBOSE) {
            Logger.debug("Query: " + query);
        }
        SqlQuery sqlQuery = Ebean.createSqlQuery(query);
        entries = sqlQuery.findList();
        mResult.mList.clear();
        if (entries == null || entries.size() == 0) {
            Logger.error("No entries");
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
            String query = buildQuery(false);
            List<SqlRow> entries = Ebean.createSqlQuery(query).findList();
            mResult.mNumTotalRows = entries.size();
        }
        return mResult.mNumTotalRows;
    }

    private Hours parseEntry(SqlRow row) {
        Hours entry = new Hours();
        entry.id = row.getLong("id");
        entry.tech_id = row.getInteger("tech_id");
        entry.entry_time = row.getDate("entry_time");
        entry.time_zone = row.getString("time_zone");
        entry.project_id = row.getLong("project_id");
        entry.start_time = row.getInteger("start_time");
        entry.end_time = row.getInteger("end_time");
        entry.lunch_time = row.getInteger("lunch_time");
        entry.break_time = row.getInteger("break_time");
        entry.drive_time = row.getInteger("drive_time");
        entry.notes = row.getString("notes");
        entry.viewed = row.getBoolean("viewed");
        return entry;
    }

    public synchronized List<Hours> getList() {
        return mResult.mList;
    }

    public List<Hours> getOrderedList() {
        if (isOrderDesc()) {
            List<Hours> reversed = new ArrayList<>(mResult.mList);
            Collections.reverse(reversed);
            return reversed;
        }
        return mResult.mList;
    }

    ///////////////////
    // PAGE SELECTION
    ///////////////////

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

    public String getPrevClass() {
        if (hasPrev()) {
            return CLASS_PREV;
        } else {
            return CLASS_PREV_DISABLED;
        }
    }

    public boolean hasNext() {
        int next = (mParams.mPage + 1) * mParams.mPageSize;
        return (next < mResult.mNumTotalRows);
    }

    public String getNextClass() {
        if (hasNext()) {
            return CLASS_NEXT;
        } else {
            return CLASS_NEXT_DISABLED;
        }
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
        sbuf.append(start + 1);
        sbuf.append(" - ");
        sbuf.append(last + 1);
        sbuf.append(" of ");
        sbuf.append(mResult.mNumTotalRows);
        return sbuf.toString();
    }

    ///////////////////
    // SEARCH
    ///////////////////

    public Html highlightSearch(String element, ColumnSelector selector) {
        return mSearch.highlight(element, selector);
    }

    public void clearSearch() {
        setSearch(null, null);
    }

    public void setSearch(String searchTerm, String columnSelector) {
        Logger.debug("setSearch(" + searchTerm + ", " + columnSelector.toString() + ")");
        if (searchTerm != null && searchTerm.length() > 0 && !searchTerm.equals("null")) {
            mSearch.setSearchTerm(searchTerm);
        } else {
            mSearch.setSearchTerm(null);
        }
        if (columnSelector != null && columnSelector.length() > 0 && !columnSelector.equals("null")) {
            mSearch.setSearchField(ColumnSelector.from(columnSelector));
        } else {
            mSearch.setSearchField(ColumnSelector.ALL);
        }
    }

    public String getSearchTerm() {
        return mSearch.getTermChkNull();
    }

    public String getSearchField() {
        return mSearch.getField();
    }

}
