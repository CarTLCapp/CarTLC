package com.cartlc.trackbattery.act;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.cartlc.trackbattery.R;
import com.cartlc.trackbattery.app.TBApplication;
import com.cartlc.trackbattery.data.DataStates;
import com.cartlc.trackbattery.data.PrefHelper;
import com.cartlc.trackbattery.data.TableAddress;
import com.cartlc.trackbattery.data.TableProjectGroups;
import com.cartlc.trackbattery.data.TableProjects;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

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
                    } else {
                        mInputMM.hideSoftInputFromWindow(host.getWindowToken(), 0);
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
        STREET,
        CURRENT_PROJECT,
        TRUCK_NUMBER,
        EQUIPMENT;

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
    @BindView(R.id.entry) EditText mEntry;
    @BindView(R.id.frame_login) ViewGroup mFrameLogin;
    @BindView(R.id.frame_new_entry) ViewGroup mFrameNewEntry;
    @BindView(R.id.list) RecyclerView mRecyclerView;
    @BindView(R.id.list_container) FrameLayout mListContainer;
    @BindView(R.id.next) Button mNext;
    @BindView(R.id.prev) Button mPrev;
    @BindView(R.id.new_entry) Button mNew;
    @BindView(R.id.setup_title) TextView mTitle;
    @BindView(R.id.fab_add) FloatingActionButton mAdd;

    Stage mCurStage = Stage.LOGIN;
    String mCurKey = PrefHelper.KEY_STATE;
    boolean mCurStageEditing = false;
    SimpleListAdapter mSimpleAdapter;
    ProjectListAdapter mProjectAdapter;
    EquipmentListAdapter mEquipmentAdapter;
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
        mInputMM = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
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
        mNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doNewEntry();
            }
        });
        mAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doNext();
            }
        });
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(mRecyclerView.getContext(), mLayoutManager.getOrientation()));
        mRecyclerView.setLayoutManager(mLayoutManager);
        mSimpleAdapter = new SimpleListAdapter(this, new SimpleListAdapter.OnItemSelectedListener() {
            @Override
            public void onSelectedItem(int position, String text) {
                if (mCurKey != null) {
                    PrefHelper.getInstance().setString(mCurKey, text);
                }
            }
        });
        mProjectAdapter = new ProjectListAdapter(this);
        mEquipmentAdapter = new EquipmentListAdapter(this);
        mFirstName.addTextChangedListener(new DetectReturn(mFirstName));
        mLastName.addTextChangedListener(new DetectReturn(mLastName));
        mEntry.addTextChangedListener(new DetectReturn(mEntry));
    }

    @Override
    public void onResume() {
        super.onResume();
        PrefHelper.getInstance().setupInit();
        computeCurStage();
        setStage();
    }

    void computeCurStage() {
        if (TextUtils.isEmpty(PrefHelper.getInstance().getLastName())) {
            mCurStage = Stage.LOGIN;
        } else if (TextUtils.isEmpty(PrefHelper.getInstance().getProject())) {
            mCurStage = Stage.PROJECT;
        } else if (TextUtils.isEmpty(PrefHelper.getInstance().getCompany())) {
            mCurStage = Stage.COMPANY;
        } else if (TextUtils.isEmpty(PrefHelper.getInstance().getState())) {
            mCurStage = Stage.STATE;
        } else if (TextUtils.isEmpty(PrefHelper.getInstance().getCity())) {
            mCurStage = Stage.CITY;
        } else if (TextUtils.isEmpty(PrefHelper.getInstance().getStreet())) {
            mCurStage = Stage.STREET;
        } else {
            mCurStage = Stage.CURRENT_PROJECT;
        }
    }

    boolean save(boolean errOk) {
        if (mCurStage == Stage.LOGIN) {
            PrefHelper.getInstance().setFirstName(mFirstName.getText().toString());
            PrefHelper.getInstance().setLastName(mLastName.getText().toString());
        } else if (mCurStage == Stage.TRUCK_NUMBER) {
            String value = mEntry.getText().toString();
            if (TextUtils.isDigitsOnly(value)) {
                PrefHelper.getInstance().setTruckNumber(Long.parseLong(value));
            } else {
                if (errOk) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle(R.string.title_error);
                    builder.setMessage(getString(R.string.not_a_number, value));
                    builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    builder.create().show();
                }
                return false;
            }
        } else if (mCurStageEditing) {
            if (mCurStage == Stage.CITY) {
                PrefHelper.getInstance().setCity(mEntry.getText().toString());
            } else if (mCurStage == Stage.STREET) {
                PrefHelper.getInstance().setStreet(mEntry.getText().toString());
            }
        }
        mCurStageEditing = false;
        return true;
    }

    void doNext() {
        if (save(true)) {
            mCurStage = Stage.from(mCurStage.ordinal() + 1);
            setStage();
        }
    }

    void doPrev() {
        save(false);
        mCurStage = Stage.from(mCurStage.ordinal() - 1);
        setStage();
    }

    void doNewEntry() {
        mCurStageEditing = true;
        setStage();
    }

    void setStage() {
        mFrameLogin.setVisibility(View.GONE);
        mFrameNewEntry.setVisibility(View.GONE);
        mListContainer.setVisibility(View.GONE);
        mAdd.setVisibility(View.GONE);
        mNext.setVisibility(View.INVISIBLE);
        mPrev.setVisibility(View.INVISIBLE);
        mNew.setVisibility(View.INVISIBLE);
        mEntry.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);

        switch (mCurStage) {
            case LOGIN:
                mFrameLogin.setVisibility(View.VISIBLE);
                mNext.setVisibility(View.VISIBLE);
                mTitle.setText(R.string.title_login);
                mFirstName.setText(PrefHelper.getInstance().getFirstName());
                mLastName.setText(PrefHelper.getInstance().getLastName());
                break;
            case PROJECT:
                mListContainer.setVisibility(View.VISIBLE);
                mPrev.setVisibility(View.VISIBLE);
                mNext.setVisibility(View.VISIBLE);
                setList(R.string.title_project, PrefHelper.KEY_PROJECT, TableProjects.getInstance().query());
                break;
            case COMPANY:
                mListContainer.setVisibility(View.VISIBLE);
                mPrev.setVisibility(View.VISIBLE);
                mNext.setVisibility(View.VISIBLE);
                List<String> companies = TableAddress.getInstance().queryCompanies();
                PrefHelper.getInstance().addCompany(companies);
                setList(R.string.title_company, PrefHelper.KEY_COMPANY, companies);
                break;
            case STATE:
                mListContainer.setVisibility(View.VISIBLE);
                mNext.setVisibility(View.VISIBLE);
                mPrev.setVisibility(View.VISIBLE);
                if (mCurStageEditing) {
                    List<String> states = DataStates.getUnusedStates();
                    PrefHelper.getInstance().setState(null);
                    setList(R.string.title_state, PrefHelper.KEY_STATE, states);
                } else {
                    String company = PrefHelper.getInstance().getCompany();
                    List<String> states = TableAddress.getInstance().queryStates(company);
                    PrefHelper.getInstance().addState(states);
                    setList(R.string.title_state, PrefHelper.KEY_STATE, states);
                    mNew.setVisibility(View.VISIBLE);
                }
                break;
            case CITY:
                mNext.setVisibility(View.VISIBLE);
                mPrev.setVisibility(View.VISIBLE);
                if (mCurStageEditing) {
                    mFrameNewEntry.setVisibility(View.VISIBLE);
                    mEntry.setHint(R.string.title_city);
                    mEntry.setText("");
                } else {
                    mListContainer.setVisibility(View.VISIBLE);
                    mNew.setVisibility(View.VISIBLE);
                    String state = PrefHelper.getInstance().getState();
                    List<String> cities = TableAddress.getInstance().queryCities(state);
                    PrefHelper.getInstance().addCity(cities);
                    setList(R.string.title_city, PrefHelper.KEY_CITY, cities);
                }
                break;
            case STREET:
                mNext.setVisibility(View.VISIBLE);
                mPrev.setVisibility(View.VISIBLE);
                if (mCurStageEditing) {
                    mFrameNewEntry.setVisibility(View.VISIBLE);
                    mEntry.setHint(R.string.title_location);
                    mEntry.setText("");
                } else {
                    mNew.setVisibility(View.VISIBLE);
                    mListContainer.setVisibility(View.VISIBLE);
                    List<String> locations = TableAddress.getInstance().queryStreets(
                            PrefHelper.getInstance().getCompany(),
                            PrefHelper.getInstance().getCity(),
                            PrefHelper.getInstance().getState());
                    setList(R.string.title_location, PrefHelper.KEY_STREET, locations);
                }
                break;
            case CURRENT_PROJECT:
                PrefHelper.getInstance().saveNewProjectIfNeeded();
                if (mCurStageEditing) {
                    PrefHelper.getInstance().clearCurProject();
                    mCurStageEditing = false;
                }
                if (!PrefHelper.getInstance().hasCurProject() || TableProjectGroups.getInstance().count() == 0) {
                    computeCurStage();
                    setStage();
                } else {
                    mListContainer.setVisibility(View.VISIBLE);
                    mNew.setVisibility(View.VISIBLE);
                    mAdd.setVisibility(View.VISIBLE);
                    mCurKey = null;
                    mTitle.setText(R.string.title_current_project);
                    mRecyclerView.setAdapter(mProjectAdapter);
                    mProjectAdapter.onDataChanged();
                }
                break;
            case TRUCK_NUMBER:
                mNext.setVisibility(View.VISIBLE);
                mPrev.setVisibility(View.VISIBLE);
                mFrameNewEntry.setVisibility(View.VISIBLE);
                mTitle.setText(R.string.title_truck_number);
                mEntry.setHint(R.string.title_truck_number);
                mEntry.setText(getTruckNumber());
                mEntry.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_CLASS_NUMBER);
                break;
            case EQUIPMENT:
                if (mCurStageEditing) {

                } else {
                    mNext.setVisibility(View.VISIBLE);
                    mPrev.setVisibility(View.VISIBLE);
                    mNew.setVisibility(View.VISIBLE);
                    mTitle.setText(R.string.title_equipment_installed);
                    mRecyclerView.setAdapter(mEquipmentAdapter);
                    mEquipmentAdapter.onDataChanged();
                }
                break;
        }
    }

    String getTruckNumber() {
        long id = PrefHelper.getInstance().getTruckNumber();
        if (id == 0) {
            return "";
        }
        return Long.toString(id);
    }

    void setList(int textId, String key, List<String> list) {
        mCurKey = key;
        String text = getString(textId);
        mTitle.setText(text);

        if (list.size() == 0) {
            doNewEntry();
        } else {
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
    }
}
