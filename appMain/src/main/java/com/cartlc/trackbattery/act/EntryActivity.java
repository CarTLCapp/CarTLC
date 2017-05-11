package com.cartlc.trackbattery.act;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.cartlc.trackbattery.R;
import com.cartlc.trackbattery.data.PrefHelper;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EntryActivity extends AppCompatActivity {

    @BindView(R.id.fab_add) FloatingActionButton mAdd;
    @BindView(R.id.project_list) RecyclerView mProjectList;
    @BindView(R.id.message_line) TextView mMessageLine;
    @BindView(R.id.new_project) Button mNewProject;

    ProjectListAdapter mProjectAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_entry);

        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        mNewProject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setupNewProject();
            }
        });
        mProjectList.setLayoutManager(new LinearLayoutManager(this));
        mProjectAdapter = new ProjectListAdapter(this);

    }

    @Override
    public void onResume() {
        super.onResume();
        fill();
    }

    void fill() {
        mProjectAdapter.onDataChanged();
    }

    void setupNewProject() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setAction(MainActivity.ACTION_PROJECT);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    void setupName() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

}