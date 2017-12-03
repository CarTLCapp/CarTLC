package com.cartlc.tracker.act;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.cartlc.tracker.R;
import com.cartlc.tracker.app.TBApplication;
import com.cartlc.tracker.data.DataEntry;
import com.cartlc.tracker.etc.CheckError;
import com.cartlc.tracker.etc.PrefHelper;
import com.cartlc.tracker.util.DialogHelper;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;

public class ListEntryActivity extends AppCompatActivity {

    @BindView(R.id.list_entries) RecyclerView mEntriesList;
    @BindView(R.id.toolbar)      Toolbar      mToolbar;

    ListEntryAdapter mEntryListAdapter;
    DataEntry        mSelected;
    DialogHelper     mDialogHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_entries);
        ((TBApplication) getApplicationContext()).setUncaughtExceptionHandler(this);
        ButterKnife.bind(this);
        mEntryListAdapter = new ListEntryAdapter(this, new ListEntryAdapter.OnItemSelectedListener() {
            @Override
            public void onSelected(DataEntry entry) {
                mSelected = entry;
                invalidateOptionsMenu();
            }
        });
        mDialogHelper = new DialogHelper(this);
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
        setResult(RESULT_CANCELED);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDialogHelper.clearDialog();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.list_entries, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            finish();
        } else if (itemId == R.id.edit) {
            if (mSelected != null) {
                PrefHelper.getInstance().setFromEntry(mSelected);
                setResult(RESULT_OK);
                finish();
            } else {
                mDialogHelper.showError(getString(R.string.error_please_select_an_entry));
            }
        }
        return super.onOptionsItemSelected(item);
    }

    void clear() {
        mEntryListAdapter.clear();
        mSelected = null;
        invalidateOptionsMenu();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        clear();
        super.onNewIntent(intent);
    }
}
