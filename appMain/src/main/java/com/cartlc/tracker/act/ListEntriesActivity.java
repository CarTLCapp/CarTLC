package com.cartlc.tracker.act;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.cartlc.tracker.R;
import com.cartlc.tracker.etc.PrefHelper;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ListEntriesActivity extends AppCompatActivity {

    @BindView(R.id.list_entries) RecyclerView mEntriesList;
    @BindView(R.id.toolbar)      Toolbar      mToolbar;

    EntryListAdapter mEntryListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_entries);
        ButterKnife.bind(this);
        mEntryListAdapter = new EntryListAdapter(this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setAutoMeasureEnabled(true);
        mEntriesList.setLayoutManager(linearLayoutManager);
        mEntriesList.setAdapter(mEntryListAdapter);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mEntryListAdapter.onDataChanged();
        setTitle(PrefHelper.getInstance().getProjectName());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
