package com.cartlc.tracker.act;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.cartlc.tracker.R;
import com.cartlc.tracker.app.TBApplication;
import com.cartlc.tracker.data.DataEntry;
import com.cartlc.tracker.data.DataProjectAddressCombo;
import com.cartlc.tracker.data.TableProjectAddressCombo;
import com.cartlc.tracker.etc.PrefHelper;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ListEntryActivity extends AppCompatActivity implements ListEntryAdapter.OnItemSelectedListener {

    @BindView(R.id.list_entries)    RecyclerView mEntriesList;
    @BindView(R.id.edit_address)    Button       mEditAddress;
    @BindView(R.id.toolbar)         Toolbar      mToolbar;
    @BindView(R.id.project_name)    TextView     mProjectName;
    @BindView(R.id.project_address) TextView     mProjectAddress;
    @BindView(R.id.delete)          Button       mDelete;

    ListEntryAdapter mEntryListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_entries);
        ((TBApplication) getApplicationContext()).setUncaughtExceptionHandler(this);
        ButterKnife.bind(this);
        mEntryListAdapter = new ListEntryAdapter(this, this);
        mEditAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PrefHelper.getInstance().setFromCurrentProjectId();
                setResult(MainActivity.RESULT_EDIT_PROJECT);
                finish();
            }
        });
        mDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DataProjectAddressCombo projectGroup = PrefHelper.getInstance().getCurrentProjectGroup();
                TableProjectAddressCombo.getInstance().remove(projectGroup.id);
                setResult(MainActivity.RESULT_DELETE_PROJECT);
                finish();
            }
        });
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
        setProjectDisplay();
        setTitle(getTitleString());
        setResult(RESULT_CANCELED);
        if (mEntryListAdapter.getItemCount() == 0) {
            mDelete.setVisibility(View.VISIBLE);
        } else {
            mDelete.setVisibility(View.GONE);
        }
    }

    String getTitleString() {
        StringBuilder sbuf = new StringBuilder();
        sbuf.append(PrefHelper.getInstance().getProjectName());
        sbuf.append(" - ");
        int count = mEntryListAdapter.getItemCount();
        if (count == 1) {
            sbuf.append(getString(R.string.title_element));
        } else {
            sbuf.append(getString(R.string.title_elements, count));
        }
        return sbuf.toString();
    }

    protected void setProjectDisplay() {
        DataProjectAddressCombo combo = PrefHelper.getInstance().getCurrentProjectGroup();
        if (combo == null) {
            mProjectName.setText("");
            mProjectAddress.setText("");
        } else {
            mProjectName.setText(combo.getProjectName());
            mProjectAddress.setText(combo.getAddressLine());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onEdit(DataEntry entry) {
        PrefHelper.getInstance().setFromEntry(entry);
        setResult(MainActivity.RESULT_EDIT_ENTRY);
        finish();
    }
}
