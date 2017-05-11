package com.cartlc.trackbattery.act;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.cartlc.trackbattery.R;
import com.cartlc.trackbattery.app.TBApplication;
import com.cartlc.trackbattery.data.PrefHelper;
import com.cartlc.trackbattery.data.TableAddress;
import com.cartlc.trackbattery.data.TableProjects;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements SimpleListAdapter.OnSelectedItemListener {

    class DetectReturn implements TextWatcher {

        EditText host;
        boolean changing;

        DetectReturn(EditText host) {
            this.host = host;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (!changing) {
                changing = true;
                String text = s.toString();
                if (text.contains("\n")) {
                    s.replace(0, s.length(), text.replaceAll("\\n", ""));
                    if (host == mFirstName) {
                        mLastName.requestFocus();
                    } else if (host == mLastName) {
                        mInputMM.hideSoftInputFromWindow(mLastName.getWindowToken(), 0);
                        doNext();
                    }
                }
                changing = false;
            }
        }
    }

    static final String ACTION_PROJECT = "project";

    TBApplication mApp;

    enum Stage {
        LOGIN,
        PROJECT,
        COMPANY,
        STATE,
        CITY,
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
    @BindView(R.id.list) RecyclerView mRecyclerView;
    @BindView(R.id.next) Button mNext;
    @BindView(R.id.prev) Button mPrev;
    @BindView(R.id.setup_title) TextView mTitle;

    Stage mCurStage = Stage.LOGIN;
    String mCurKey = PrefHelper.KEY_STATE;
    SimpleListAdapter mSimpleAdapter;
    LinearLayoutManager mLayoutManager;
    InputMethodManager mInputMM;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mApp = (TBApplication) getApplicationContext();
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mInputMM = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
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
        mRecyclerView.setLayoutManager(mLayoutManager = new LinearLayoutManager(this));
        mSimpleAdapter = new SimpleListAdapter(this, this);
        mFirstName.addTextChangedListener(new DetectReturn(mFirstName));
        mLastName.addTextChangedListener(new DetectReturn(mLastName));
        if (TextUtils.isEmpty(PrefHelper.getInstance().getLastName())) {
            mCurStage = Stage.LOGIN;
        } else /*if (TextUtils.isEmpty(PrefHelper.getInstance().getProject())) */ {
            mCurStage = Stage.PROJECT;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        PrefHelper.getInstance().setupInit();
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
                mRecyclerView.setVisibility(View.GONE);
                mPrev.setVisibility(View.GONE);
                mTitle.setText(R.string.title_login);
                mFirstName.setText(PrefHelper.getInstance().getFirstName());
                mLastName.setText(PrefHelper.getInstance().getLastName());
                break;
            case PROJECT:
                mFrameLogin.setVisibility(View.GONE);
                mRecyclerView.setVisibility(View.VISIBLE);
                mPrev.setVisibility(View.VISIBLE);
                setList(R.string.title_project, PrefHelper.KEY_PROJECT, TableProjects.getInstance().query());
                break;
            case COMPANY:
                List<String> companies = TableAddress.getInstance().queryCompanies();
                setList(R.string.title_company, PrefHelper.KEY_COMPANY, companies);
                break;
            case STATE:
                List<String> states = TableAddress.getInstance().queryStates();
                setList(R.string.title_state, PrefHelper.KEY_STATE, states);
                break;
            case CITY:
                List<String> cities = TableAddress.getInstance().queryCities(PrefHelper.getInstance().getState());
                setList(R.string.title_city, PrefHelper.KEY_CITY, cities);
                break;
            case LOCATION:
                List<String> locations = TableAddress.getInstance().queryStreets(PrefHelper.getInstance().getState(),
                        PrefHelper.getInstance().getCity(),
                        PrefHelper.getInstance().getCompany());
                setList(R.string.title_location, PrefHelper.KEY_STREET, locations);
                break;
            case DONE:
                PrefHelper.getInstance().setupSaveNew();
                startEntryActivity();
                break;
        }
    }

    void setList(int textId, String key, List<String> list) {
        mCurKey = key;
        String text = getString(textId);
        mTitle.setText(text);
        mSimpleAdapter.setList(list);
        mRecyclerView.setAdapter(mSimpleAdapter);

        String curValue = PrefHelper.getInstance().getString(key, null);
        if (curValue == null) {
            mSimpleAdapter.setNoneSelected();
        } else {
            int position = mSimpleAdapter.setSelected(curValue);
            if (position >= 0) {
                mRecyclerView.scrollToPosition(position);
            }
        }
    }

    @Override
    public void onSelectedItem(int position, String text) {
        PrefHelper.getInstance().setString(mCurKey, text);
    }

    void startEntryActivity() {
        Intent intent = new Intent(this, EntryActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

}
