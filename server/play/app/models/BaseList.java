package models;

import java.util.*;

import com.avaje.ebean.*;

import play.db.ebean.*;
import play.Logger;

/**
 * Created because Ebean doesn't work in one annoying aspect:
 * When I try to join the Entry and Company tables so that I can do
 * proper ordering and so on, well I can get them joined. But I can't
 * insert new entries with the company_id field being filled in.
 */
public abstract class BaseList<T> {

    protected static final int PAGE_SIZE = 100;

    public enum SortBy {
        CLIENT("client_id"),
        TECH("tech_id"),
        TIME("entry_time"),
        PROJECT("project_id"),
        TRUCK_NUMBER("truck_number"),
        COMPANY("company"),
        STATE("state"),
        CITY("city"),
        ZIPCODE("zipcode"),
        STREET("street");

        String code;

        SortBy(String code) {
            this.code = code;
        }

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

    public enum Order {
        ASC("asc"),
        DESC("desc");

        String code;

        Order(String code) {
            this.code = code;
        }

        public static Order from(String code) {
            for (Order item : values()) {
                if (item.code.equals(code)) {
                    return item;
                }
            }
            Logger.error("Invalid order by : " + code);
            return null;
        }
    }

    protected class Parameters {
        SortBy     sortBy;
        Order      order;
        List<Long> projectIds;

        Parameters() {
            sortBy = SortBy.TIME;
            order = Order.DESC;
        }

        Parameters(Parameters other) {
            sortBy = other.sortBy;
            order = other.order;
            if (other.projectIds != null) {
                projectIds = new ArrayList<Long>();
                for (int i = 0; i < other.projectIds.size(); i++) {
                    projectIds.add(other.projectIds.get(i));
                }
            }
        }

        public boolean equals(Parameters other) {
            if (sortBy == other.sortBy && order == other.order) {
                if (projectIds == null && other.projectIds == null) {
                    return true;
                }
                if (projectIds == null || other.projectIds == null) {
                    return false;
                }
                if (projectIds.size() != other.projectIds.size()) {
                    return false;
                }
                for (int i = 0; i < projectIds.size(); i++) {
                    if (projectIds.get(i) != other.projectIds.get(i)) {
                        return false;
                    }
                }
                return true;
            }
            return false;
        }
    }

    protected List<T>    mComputed;
    protected List<T>    mResult;
    protected Parameters mLastParameters;
    protected Parameters mNextParameters = new Parameters();
    protected int        mPageSize       = PAGE_SIZE;
    protected int mPage;

    public BaseList() {
    }

    public void setSortBy(String sortBy) {
        mNextParameters.sortBy = SortBy.from(sortBy);
    }

    public void setOrder(String order) {
        mNextParameters.order = Order.from(order);
    }

    public void setPage(int page) {
        mPage = page;
    }

    public void clearCache() {
        mLastParameters = null;
    }

    public void setProjectIdFilter(List<Long> projects) {
        mNextParameters.projectIds = projects;
    }

    public void setProjects(Client client) {
        if (client == null || client.is_admin) {
            setProjectIdFilter(null);
        } else {
            List<Project> projects = client.getProjects();
            if (projects != null && projects.size() > 0) {
                ArrayList<Long> list = new ArrayList<Long>();
                for (Project project : projects) {
                    list.add(project.id);
                }
                setProjectIdFilter(list);
            } else {
                setProjectIdFilter(null);
            }
        }
    }

    protected String getOrderBy() {
        return mNextParameters.sortBy.code + " " + mNextParameters.order.code;
    }

    protected abstract List<T> getOrderedList();

    protected abstract List<T> getRawList();

    protected abstract void sort(List<T> list);

    protected abstract long getProjectId(T obj);

    public void compute() {
        if (mLastParameters != null && mNextParameters.equals(mLastParameters)) {
            return;
        }
        mComputed = null;
        boolean needsSort = false;
        if (mNextParameters.sortBy != null) {
            switch (mNextParameters.sortBy) {
                case TECH:
                case TIME:
                    mComputed = getOrderedList();
                    break;
                case COMPANY:
                case STATE:
                case CITY:
                case STREET:
                case ZIPCODE:
                case TRUCK_NUMBER:
                case PROJECT:
                case CLIENT:
                    mComputed = getRawList();
                    needsSort = true;
                    break;
                default:
                    Logger.error("Invalid sort by code: " + mNextParameters.sortBy.toString());
                    return;
            }
        } else {
            Logger.error("Invalid NULL sort by");
            return;
        }
        if (mNextParameters.projectIds != null && mNextParameters.projectIds.size() > 0) {
            if (mNextParameters.projectIds != null && mNextParameters.projectIds.size() > 0) {
                List<T> list = new ArrayList<T>();
                for (T entry : mComputed) {
                    if (mNextParameters.projectIds.contains(getProjectId(entry))) {
                        list.add(entry);
                    }
                }
                mComputed = list;
            }
        }
        if (needsSort) {
            sort(mComputed);
        }
        mLastParameters = new Parameters(mNextParameters);
    }

    public List<T> getList() {
        compute();
        if (mComputed == null) {
            mResult = new ArrayList<T>();
        } else {
            if (mComputed.size() > mPageSize) {
                int fromIndex = mPage * mPageSize;
                int toIndex = fromIndex + mPageSize;
                if (toIndex > mComputed.size()) {
                    toIndex = mComputed.size();
                }
                mResult = mComputed.subList(fromIndex, toIndex);
            } else {
                mResult = mComputed;
            }
        }
        return mResult;
    }

    public int getPageIndex() {
        return mPage;
    }

    public boolean hasPrev() {
        return mPage > 0 && mComputed != null && mComputed.size() > mPageSize;
    }

    public boolean hasNext() {
        return mComputed != null && ((mPage + 1) * mPageSize < mComputed.size());
    }

    public int getTotalRowCount() {
        compute();
        if (mComputed == null) {
            return 0;
        }
        return mComputed.size();
    }

    public String getDisplayXtoYofZ(String to, String of) {
        StringBuilder sbuf = new StringBuilder();
        int iFrom = mPage * mPageSize + 1;
        int iTo = iFrom + mPageSize - 1;
        if (iTo > mComputed.size()) {
            iTo = mComputed.size();
        }
        sbuf.append(iFrom);
        sbuf.append(to);
        sbuf.append(iTo);
        sbuf.append(of);
        sbuf.append(mComputed.size());
        return sbuf.toString();
    }

}
