package com.fleettlc.trackbattery.act;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.fleettlc.trackbattery.R;
import com.fleettlc.trackbattery.app.TBApplication;
import com.fleettlc.trackbattery.data.TableProjects;
import com.fleettlc.trackbattery.view.NothingSelectedSpinnerAdapter;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    TBApplication mApp;

    enum Stage {
        LOGIN,
        PROJECT,
        STATE,
        CITY,
        COMPANY,
        LOCATION,
        CURRENT_PROJECT,
        TRUCK_NUMBER,
        EQUIPMENT_INSTALLED,
        NOTES,
        END;

        public static Stage from(int ord) {
            for (Stage s : values()) {
                if (s.ordinal() == ord) {
                    return s;
                }
            }
            return LOGIN;
        }
    }

    @BindView(R.id.first_name) EditText mFirstName;
    @BindView(R.id.last_name) EditText mLastName;
    @BindView(R.id.frame_login) ViewGroup mFrameLogin;
    @BindView(R.id.frame_spinner) ViewGroup mFrameSpinner;
    @BindView(R.id.entry_spinner) Spinner mSpinner;
    @BindView(R.id.next) Button mNext;
    @BindView(R.id.setup_title) TextView mTitle;

    ArrayAdapter<String> mSpinnerAdapter;

    Stage mCurStage = Stage.LOGIN;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mApp = (TBApplication) getApplicationContext();
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mNext.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                doNext();
            }
        });
        mSpinnerAdapter = new ArrayAdapter(this, R.layout.spinner_item, new ArrayList());
        mSpinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        NothingSelectedSpinnerAdapter nothingSelected = new NothingSelectedSpinnerAdapter(this, mSpinnerAdapter);
        mSpinner.setAdapter(nothingSelected);
    }

    void doNext() {
        mCurStage = Stage.from(mCurStage.ordinal() + 1);
        setStage();
    }

    void setStage() {
        switch (mCurStage) {
            case LOGIN:
                mFrameLogin.setVisibility(View.VISIBLE);
                mFrameSpinner.setVisibility(View.GONE);
                mTitle.setText(R.string.title_login);
                break;
            case PROJECT:
                mFrameLogin.setVisibility(View.INVISIBLE);
                mFrameSpinner.setVisibility(View.VISIBLE);
                mTitle.setText(R.string.title_project);
                mSpinner.setPrompt(getString(R.string.title_project));
                mSpinnerAdapter.clear();
                mSpinnerAdapter.addAll(TableProjects.getInstance().query());
                mSpinner.performClick();
                break;
        }
    }
}
