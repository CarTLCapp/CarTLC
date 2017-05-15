package com.cartlc.tracker.view;

/**
 * Created by dug on 5/9/17.
 */

import android.content.Context;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.cartlc.tracker.R;

/**
 * Decorator Adapter to allow a Spinner to show a 'Nothing Selected...' initially
 * displayed instead of the first choice in the Adapter.
 */
public class NothingSelectedSpinnerAdapter implements SpinnerAdapter, ListAdapter {

    protected static final int EXTRA = 1;
    protected final SpinnerAdapter adapter;
    protected final Context context;
    protected int nothingSelectedLayout = R.layout.spinner_nothing_selected;
    protected LayoutInflater layoutInflater;
    protected TextView mNothingSelectedView;
    protected String mNothingSelectedText = "Select";

    /**
     * Use this constructor to Define your 'Select One...' layout as the first
     * row in the returned choices.
     * If you do this, you probably don't want a prompt on your spinner or it'll
     * have two 'Select' rows.
     *
     * @param spinnerAdapter wrapped Adapter. Should probably return false for isEnabled(0)
     *                       the dropdown.
     * @param context
     */
    public NothingSelectedSpinnerAdapter(Context context, SpinnerAdapter spinnerAdapter) {
        this.adapter = spinnerAdapter;
        this.context = context;
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public final View getView(int position, View convertView, ViewGroup parent) {
        // This provides the View for the Selected Item in the Spinner, not
        // the dropdown (unless dropdownView is not set).
        if (position == 0) {
            return getNothingSelectedView(parent);
        }
        return adapter.getView(position - EXTRA, null, parent);
    }

    /**
     * View to show in Spinner with Nothing Selected
     * Override this to do something dynamic... e.g. "37 Options Found"
     *
     * @param parent
     * @return
     */
    protected View getNothingSelectedView(ViewGroup parent) {
        if (mNothingSelectedView == null) {
            mNothingSelectedView = (TextView) layoutInflater.inflate(nothingSelectedLayout, parent, false);
        }
        mNothingSelectedView.setText(mNothingSelectedText);
        return mNothingSelectedView;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        if (position == 0) {
            return new View(context);
        }
        // Could re-use the convertView if possible, use setTag...
        return adapter.getDropDownView(position - EXTRA, null, parent);
    }

    @Override
    public int getCount() {
        int count = adapter.getCount();
        return count == 0 ? 0 : count + EXTRA;
    }

    @Override
    public Object getItem(int position) {
        return position == 0 ? null : adapter.getItem(position - EXTRA);
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position >= EXTRA ? adapter.getItemId(position - EXTRA) : position - EXTRA;
    }

    @Override
    public boolean hasStableIds() {
        return adapter.hasStableIds();
    }

    @Override
    public boolean isEmpty() {
        return adapter.isEmpty();
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
        adapter.registerDataSetObserver(observer);
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
        adapter.unregisterDataSetObserver(observer);
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int position) {
        return position != 0; // Don't allow the 'nothing selected' item to be picked.
    }

    public void setNothingSelectedText(String text) {
        mNothingSelectedText = text;
        if (mNothingSelectedView != null) {
            mNothingSelectedView.setText(text);
        }
    }

}
