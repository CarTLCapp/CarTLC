package com.cartlc.trackbattery.act;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.cartlc.trackbattery.R;
import com.cartlc.trackbattery.app.TBApplication;
import com.cartlc.trackbattery.data.PrefHelper;
import com.cartlc.trackbattery.data.TableAddress;
import com.cartlc.trackbattery.data.TableProjects;
import com.cartlc.trackbattery.view.NothingSelectedSpinnerAdapter;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SetupActivity extends AppCompatActivity {

    static final String ACTION_PROJECT = "project";

    TBApplication mApp;

    enum Stage {
        LOGIN,
        PROJECT,
        STATE,
        CITY,
        COMPANY,
        LOCATION,
        DONE;

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
    @BindView(R.id.prev) Button mPrev;
    @BindView(R.id.setup_title) TextView mTitle;

    ArrayAdapter<String> mSpinnerAdapter;
    NothingSelectedSpinnerAdapter mNothingSelectedAdapter;
    Stage mCurStage = Stage.LOGIN;
    String mCurKey = PrefHelper.KEY_STATE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mApp = (TBApplication) getApplicationContext();
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doNext();
            }
        });
        mPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doPrev();
            }
        });
        mSpinnerAdapter = new ArrayAdapter(this, R.layout.spinner_item, new ArrayList());
        mSpinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        mNothingSelectedAdapter = new NothingSelectedSpinnerAdapter(this, mSpinnerAdapter);
        mSpinner.setAdapter(mNothingSelectedAdapter);
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                spinnerSelectItem(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        processIntent(getIntent());
    }

    void processIntent(Intent intent) {
        if (ACTION_PROJECT.equals(intent.getAction())) {
            mCurStage = Stage.PROJECT;
        } else {
            mCurStage = Stage.LOGIN;
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        processIntent(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        setStage();
    }

    void doNext() {
        if (mCurStage == Stage.LOGIN) {
            PrefHelper.getInstance().setFirstName(mFirstName.getText().toString());
            PrefHelper.getInstance().setLastName(mLastName.getText().toString());
        }
        mCurStage = Stage.from(mCurStage.ordinal() + 1);
        setStage();
    }

    void doPrev() {
        mCurStage = Stage.from(mCurStage.ordinal() - 1);
        setStage();
    }

    void setStage() {
        switch (mCurStage) {
            case LOGIN:
                mFrameLogin.setVisibility(View.VISIBLE);
                mFrameSpinner.setVisibility(View.GONE);
                mPrev.setVisibility(View.GONE);
                mTitle.setText(R.string.title_login);
                mFirstName.setText(PrefHelper.getInstance().getFirstName());
                mLastName.setText(PrefHelper.getInstance().getLastName());
                break;
            case PROJECT:
                mFrameLogin.setVisibility(View.INVISIBLE);
                mFrameSpinner.setVisibility(View.VISIBLE);
                mPrev.setVisibility(View.VISIBLE);
                setSpinner(R.string.title_project, PrefHelper.KEY_PROJECT, TableProjects.getInstance().query());
                break;
            case STATE:
                List<String> states = TableAddress.getInstance().queryStates();
                setSpinner(R.string.title_state, PrefHelper.KEY_STATE, states);
                break;
            case CITY:
                List<String> cities = TableAddress.getInstance().queryCities(PrefHelper.getInstance().getState());
                setSpinner(R.string.title_city, PrefHelper.KEY_CITY, cities);
                break;
            case COMPANY:
                List<String> companies = TableAddress.getInstance().queryCompanies(PrefHelper.getInstance().getState(), PrefHelper.getInstance().getCity());
                setSpinner(R.string.title_company, PrefHelper.KEY_COMPANY, companies);
                break;
            case LOCATION:
                List<String> locations = TableAddress.getInstance().queryLocations(PrefHelper.getInstance().getState(),
                        PrefHelper.getInstance().getCity(),
                        PrefHelper.getInstance().getCompany());
                setSpinner(R.string.title_location, PrefHelper.KEY_LOCATION, locations);
                break;
            case DONE:
                startEntryActivity();
                break;
        }
    }

    void setSpinner(int textId, String key, List<String> list) {
        mCurKey = key;
        String text = getString(textId);
        mTitle.setText(text);
        mSpinner.setPrompt(text);
        mNothingSelectedAdapter.setNothingSelectedText(text);
        mSpinnerAdapter.clear();
        mSpinnerAdapter.addAll(list);
//        mSpinner.performClick();

        String curValue = PrefHelper.getInstance().getString(key, null);
        if (curValue == null) {
            mSpinner.setSelection(0);
        } else {
            int position = mSpinnerAdapter.getPosition(curValue);
            mSpinner.setSelection(position);
        }
    }

    void spinnerSelectItem(int position) {
        if (position > 0) {
            String selection = mSpinnerAdapter.getItem(position - 1);
            PrefHelper.getInstance().setString(mCurKey, selection);
        }
    }

    void startEntryActivity() {
        Intent intent = new Intent(this, EntryActivity.class);
        startActivity(intent);
    }
}
