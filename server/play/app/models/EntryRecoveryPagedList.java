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
import modules.Status;
import modules.AmazonHelper;

public class EntryRecoveryPagedList {

    public enum ColumnSelector {
        ALL("All", "all", null),
        TECH("Technician", "tech", "te.last_name"),
        DATE("Date", "date", "e.entry_time"),
        TIME("", "time", "e.entry_time"),
        ROOT_PROJECT_ID("Root Project", "root_project", "r.name"),
        SUB_PROJECT_ID("Sub Project", "sub_project", "p.name"),
        COMPANY_NAME("Company Name", "company", "c.name"),
        STREET("Street", "street", "c.street"),
        CITY("City", "city", "c.city"),
        STATE("State", "state", "c.state"),
        ZIP("Zip Code", "zipcode", "c.zipcode"),
        TRUCK_NUMBER("Truck", "truck", "tr.truck_number"),
        EQUIPMENT("Equipment", "equip", "eq.name");

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
            error("Invalid sort by : " + match);
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
                if (selector == ColumnSelector.TECH || selector == ColumnSelector.EQUIPMENT || selector == ColumnSelector.STREET) {
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
        List<EntryRecovery> mList = new ArrayList<EntryRecovery>();
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
    private static final String ENTRY_DATE_FORMAT = "YYYY-MM-dd HH:mm:SS zzz";
    private SimpleDateFormat mBuildDateFormat = new SimpleDateFormat(ENTRY_DATE_FORMAT);

    Parameters mParams = new Parameters();
    Result mResult = new Result();
    SearchInfo mSearch = new SearchInfo();
    List<Long> mLimitByProject = new ArrayList<Long>();
    List<String> mLimitByCompanyName = new ArrayList<String>();
    long mByTruckId;
    int mRowNumber;
    int mDefaultPageSize = PAGE_SIZES[0];
    Date mSearchStartDate = null;
    Date mSearchEndDate = null;
    String mEntryRecoveryFile = "";

    public boolean canViewTrucks = true;
    public boolean canViewPictures = true;
    public long mForClientId = 0;

    public EntryRecoveryPagedList(AmazonHelper amazonHelper) {
        mEntryRecoveryFile = amazonHelper.getRecoveryFile().toString();
    }

    public EntryRecoveryPagedList(EntryRecoveryPagedList other) {
        mParams.mSortBy = other.mParams.mSortBy;
        mParams.mOrder = other.mParams.mOrder;
        mSearch = new SearchInfo(other.mSearch);
        mLimitByProject = new ArrayList<>(other.mLimitByProject);
        mLimitByCompanyName = new ArrayList<>(other.mLimitByCompanyName);
        mByTruckId = other.mByTruckId;
        mResult.mNumTotalRows = other.mResult.mNumTotalRows;
        mRowNumber = 0;
    }

    public String getRecoveryFile() {
        return mEntryRecoveryFile;
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
            mParams.mSortBy = ColumnSelector.TIME;
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
        setCompanies(client);

        if (client.is_admin) {
            canViewTrucks = true;
            canViewPictures = true;
            mForClientId = 0;
        } else {
            canViewTrucks = ClientAssociation.hasShowTrucks(client);
            canViewPictures = ClientAssociation.hasShowPictures(client);
            mForClientId = client.id != null ? client.id : 0;
        }
    }

    public String getEquipmentLine(EntryRecovery entry) {
        return entry.getEquipmentLine(mForClientId);
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

    private String buildQuery(boolean useLimit) {
        StringBuilder query = new StringBuilder();

        query.append("SELECT DISTINCT e.id, e.tech_id, e.entry_time, e.project_id, e.company_id");
        query.append(", e.equipment_collection_id");
        query.append(", e.picture_collection_id");
        query.append(", e.note_collection_id");
        query.append(", e.truck_id, e.status, e.time_zone, e.error");

        switch (mParams.mSortBy) {
            case TECH:
            case SUB_PROJECT_ID:
            case ROOT_PROJECT_ID:
            case TRUCK_NUMBER:
            case COMPANY_NAME:
            case STREET:
            case CITY:
            case STATE:
            case ZIP:
                query.append(" , " + getSortByColumn());
                break;
        }
        query.append(" FROM entry_recovery AS e");

        if (mSearch.hasSearch() && mSearch.hasSearchBy(ColumnSelector.EQUIPMENT)) {
            query.append(" INNER JOIN entry_equipment_collection AS eqc ON e.equipment_collection_id = eqc.collection_id");
            query.append(" INNER JOIN equipment AS eq ON eqc.equipment_id = eq.id");
        }
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
            case TRUCK_NUMBER:
                query.append(" INNER JOIN truck AS tr ON e.truck_id = tr.id");
                break;
            case COMPANY_NAME:
            case STREET:
            case CITY:
            case STATE:
            case ZIP:
                query.append(" INNER JOIN company AS c ON e.company_id = c.id");
                break;
        }
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
                appendSearch(query, "e.project_id", getSearchByProject());
            }
            if (hasSearchByCompany()) {
                appendSearch(query, "e.company_id", getSearchByCompany());
            }
            if (mSearch.hasSearchBy(ColumnSelector.TRUCK_NUMBER)) {
                appendSearch(query, "e.truck_id", getSearchByTruck());
            }
            if (mSearch.hasSearchBy(ColumnSelector.EQUIPMENT)) {
                appendSearch(query, "eq.id", getSearchByEquipment());
            }
            if (mSearch.hasSearchBy(ColumnSelector.TECH)) {
                List<Long> techs = getSearchByTechnician();
                appendSearch(query, "e.tech_id", techs);
                if (techs.size() > 0) {
                    List<Long> entries_ids = SecondaryTechnician.findMatches(techs);
                    appendSearch(query, "e.id", entries_ids);
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
                query.append("e.entry_time BETWEEN '");
                query.append(mBuildDateFormat.format(mSearchStartDate));
                query.append("' AND '");
                query.append(mBuildDateFormat.format(mSearchEndDate));
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

    private List<Long> getSearchByCompany() {
        HashSet<Long> set = new HashSet<Long>();
        if (mSearch.hasSearchBy(ColumnSelector.COMPANY_NAME)) {
            set.addAll(Company.convert(Company.findByName(mSearch.getTerm())));
        }
        if (mSearch.hasSearchBy(ColumnSelector.STREET)) {
            set.addAll(Company.convert(Company.findByStreet(mSearch.getTerm())));
        }
        if (mSearch.hasSearchBy(ColumnSelector.CITY)) {
            set.addAll(Company.convert(Company.findByCity(mSearch.getTerm())));
        }
        if (mSearch.hasSearchBy(ColumnSelector.STATE)) {
            set.addAll(Company.convert(Company.findByState(mSearch.getTerm())));
        }
        if (mSearch.hasSearchBy(ColumnSelector.ZIP)) {
            set.addAll(Company.convert(Company.findByZip(mSearch.getTerm())));
        }
        List<Long> list = new ArrayList<Long>();
        list.addAll(set);
        return list;
    }

    private boolean hasSearchByCompany() {
        return mSearch.hasSearchBy(ColumnSelector.COMPANY_NAME) ||
                mSearch.hasSearchBy(ColumnSelector.STREET) ||
                mSearch.hasSearchBy(ColumnSelector.CITY) ||
                mSearch.hasSearchBy(ColumnSelector.STATE) ||
                mSearch.hasSearchBy(ColumnSelector.ZIP);
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

    private List<Long> getSearchByTruck() {
        List<Long> list = new ArrayList<Long>();
        list.addAll(Truck.findMatches(mSearch.getTerm()));
        return list;
    }

    private List<Long> getSearchByEquipment() {
        List<Long> list = new ArrayList<Long>();
        list.addAll(Equipment.findMatches(mSearch.getTerm()));
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
            error("Invalid date entered: " + ex.getMessage());
        }
        return false;
    }

    private Date parseDate(String term) {
        if (term.contains(" ")) {
            try {
                Date date = mParseDateFormatZZZ.parse(term);
                return date;
            } catch (Exception ex) {
                info(ex.getMessage());
            }
        }
        try {
            Date date = mParseDateFormat.parse(term);
            return date;
        } catch (Exception ex) {
            info(ex.getMessage());
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
            projects.append("e.project_id = ");
            projects.append(project_id);
        }
        StringBuilder companies = new StringBuilder();
        first = true;
        for (String companyName : mLimitByCompanyName) {
            List<Company> matches = Company.findByName(companyName);
            for (Company company : matches) {
                if (first) {
                    first = false;
                } else {
                    companies.append(" OR ");
                }
                companies.append("e.company_id = ");
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
            debug("Query: " + query);
        }
        SqlQuery sqlQuery = Ebean.createSqlQuery(query);
        // Note: it is possible to add parameters to the query call. Referenced within the query as ":start" for example.
//      sqlQuery.setParameter("start", mSearchStartDate);
//      sqlQuery.setParameter("end:, mSearchEndDate);
        entries = sqlQuery.findList();
        mResult.mList.clear();
        if (entries == null || entries.size() == 0) {
            error("No entries");
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

    private EntryRecovery parseEntry(SqlRow row) {
        EntryRecovery entry = new EntryRecovery();
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
        entry.error = row.getString("error");
        if (row.get("status") != null) { // WHY DO I NEED THIS?
            entry.status = Status.from(getInteger(row, "status"));
        }
        return entry;
    }

    private boolean hasEquipmentMatch(List<EntryRecovery> entries) {
        for (EntryRecovery entry : entries) {
            String line = entry.getEquipmentLine(mForClientId);
            if (mSearch.hasMatch(line)) {
                return true;
            }
        }
        return false;
    }

    private Integer getInteger(SqlRow row, String column) {
        if (row.get(column) == null) {
            return null;
        }
        return row.getInteger(column);
    }

    public synchronized List<EntryRecovery> getList() {
        return mResult.mList;
    }

    public List<EntryRecovery> getOrderedList() {
        if (isOrderDesc()) {
            List<EntryRecovery> reversed = new ArrayList<>(mResult.mList);
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

    public String getRowNumber(EntryRecovery entry) {
        StringBuilder sbuf = new StringBuilder();
        sbuf.append(mRowNumber);
        sbuf.append(" (");
        sbuf.append(entry.id);
        sbuf.append(")");
        return sbuf.toString();
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
        debug("setSearch(" + searchTerm + ", " + columnSelector.toString() + ")");
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
        mByTruckId = 0;
    }

    public String getSearchTerm() {
        return mSearch.getTermChkNull();
    }

    public String getSearchField() {
        return mSearch.getField();
    }

    // region Logger

    private static void error(String msg) {
        Logger.error(msg);
    }

    private static void warn(String msg) {
        Logger.warn(msg);
    }

    private static void info(String msg) {
        Logger.info(msg);
    }

    private static void debug(String msg) {
        Logger.debug(msg);
    }

    // endregion Logger

}
