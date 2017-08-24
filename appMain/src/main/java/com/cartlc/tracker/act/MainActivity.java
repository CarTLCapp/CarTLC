package com.cartlc.tracker.act;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.telephony.PhoneNumberUtils;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.amazonaws.util.StringUtils;
import com.cartlc.tracker.R;
import com.cartlc.tracker.app.TBApplication;
import com.cartlc.tracker.data.DataAddress;
import com.cartlc.tracker.data.DataEntry;
import com.cartlc.tracker.data.DataProject;
import com.cartlc.tracker.data.DataProjectAddressCombo;
import com.cartlc.tracker.data.DataStates;
import com.cartlc.tracker.data.DataZipCode;
import com.cartlc.tracker.data.PrefHelper;
import com.cartlc.tracker.data.TableAddress;
import com.cartlc.tracker.data.TableEntry;
import com.cartlc.tracker.data.TableEquipment;
import com.cartlc.tracker.data.TableCollectionEquipmentProject;
import com.cartlc.tracker.data.TablePictureCollection;
import com.cartlc.tracker.data.TableProjectAddressCombo;
import com.cartlc.tracker.data.TableProjects;
import com.cartlc.tracker.data.TableZipCode;
import com.cartlc.tracker.event.EventPingDone;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;

    static final int AUTO_RETURN_DELAY_MS = 100;
    static final int MSG_AUTO_RETURN      = 0;
    static final int MSG_REFRESH_PROJECTS = 1;
    static final int MSG_SET_HINT         = 2;

    static final String KEY_HINT = "hint";

    class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_AUTO_RETURN:
                    doAutoNext();
                    break;
                case MSG_REFRESH_PROJECTS:
                    mProjectAdapter.onDataChanged();
                    break;
                case MSG_SET_HINT:
                    mEntryHint.setText(msg.getData().getString(KEY_HINT));
                    mEntryHint.setVisibility(View.VISIBLE);
                    break;
            }
        }
    }

    class SoftKeyboardDetect implements ViewTreeObserver.OnGlobalLayoutListener {

        int mInitialHeight;

        void SoftKeyboardDetect() {
        }

        public void clear() {
            mButtons.setVisibility(View.VISIBLE);
        }

        @Override
        public void onGlobalLayout() {
            int heightDiff = Math.abs(mRoot.getRootView().getHeight() - mRoot.getHeight());
            if (mInitialHeight == 0) {
                mInitialHeight = heightDiff;
            } else {
                // If the diff has increased assume it's the soft keyboard.
                if (heightDiff > mInitialHeight) {
                    hideButtons();
                } else {
                    restoreButtons();
                }
            }
        }

        void hideButtons() {
            if (mButtons.getVisibility() == View.VISIBLE) {
                mButtons.setVisibility(View.GONE);
            }
        }

        void restoreButtons() {
            if (mButtons.getVisibility() != View.VISIBLE) {
                mButtons.setVisibility(View.VISIBLE);
            }
        }
    }

    class ZipCodeWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            String value = s.toString().trim();
            if (isZipCode(value)) {
                mApp.requestZipCode(value);
            }
        }
    }

    enum Stage {
        LOGIN,
        PROJECT,
        COMPANY,
        ZIPCODE,
        STATE,
        CITY,
        STREET,
        CURRENT_PROJECT,
        TRUCK_NUMBER,
        EQUIPMENT,
        NOTES,
        PICTURE,
        CONFIRM,
        ADD_ELEMENT;

        public static Stage from(int ord) {
            for (Stage s : values()) {
                if (s.ordinal() == ord) {
                    return s;
                }
            }
            return LOGIN;
        }
    }

    final static String KEY_STAGE = "stage";

    @BindView(R.id.first_name)         EditText             mFirstName;
    @BindView(R.id.last_name)          EditText             mLastName;
    @BindView(R.id.entry_simple)       EditText             mEntrySimple;
    @BindView(R.id.entry_hint)         TextView             mEntryHint;
    @BindView(R.id.frame_login)        ViewGroup            mLoginFrame;
    @BindView(R.id.frame_new_entry)    ViewGroup            mEntryFrame;
    @BindView(R.id.main_list)          RecyclerView         mMainList;
    @BindView(R.id.main_list_frame)    FrameLayout          mMainListFrame;
    @BindView(R.id.next)               Button               mNext;
    @BindView(R.id.prev)               Button               mPrev;
    @BindView(R.id.new_entry)          Button               mCenter;
    @BindView(R.id.setup_title)        TextView             mTitle;
    @BindView(R.id.fab_add)            FloatingActionButton mAdd;
    @BindView(R.id.frame_confirmation) FrameLayout          mConfirmationFrameView;
    @BindView(R.id.frame_pictures)     ViewGroup            mPictureFrame;
    @BindView(R.id.list_pictures)      RecyclerView         mPictureList;
    @BindView(R.id.empty)              TextView             mEmptyView;
    @BindView(R.id.root)               ViewGroup            mRoot;
    @BindView(R.id.buttons)            ViewGroup            mButtons;

    Stage              mCurStage           = Stage.LOGIN;
    String             mCurKey             = PrefHelper.KEY_STATE;
    MyHandler          mHandler            = new MyHandler();
    SoftKeyboardDetect mSoftKeyboardDetect = new SoftKeyboardDetect();
    TBApplication              mApp;
    boolean                    mCurStageEditing;
    SimpleListAdapter          mSimpleAdapter;
    ProjectListAdapter         mProjectAdapter;
    EquipmentSelectListAdapter mEquipmentAdapter;
    PictureListAdapter         mPictureAdapter;
    NoteListEntryAdapter       mNoteAdapter;
    InputMethodManager         mInputMM;
    ConfirmationFrame          mConfirmationFrame;
    DataEntry                  mCurEntry;
    OnEditorActionListener     mAutoNext;
    DividerItemDecoration      mDivider;
    String                     mCompanyEditing;
    ZipCodeWatcher             mZipCodeWatcher;
    boolean                    mWasNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mApp = (TBApplication) getApplicationContext();
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
//        mByAddress = getString(R.string.entry_by_address);
        mRoot.getViewTreeObserver().addOnGlobalLayoutListener(mSoftKeyboardDetect);
        mInputMM = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doBtnNext();
            }
        });
        mPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doBtnPrev();
            }
        });
        mCenter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doBtnCenter();
            }
        });
        mAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doBtnNext();
            }
        });
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mMainList.setLayoutManager(linearLayoutManager);
        mDivider = new DividerItemDecoration(mMainList.getContext(), linearLayoutManager.getOrientation());
        mMainList.addItemDecoration(mDivider);
        mSimpleAdapter = new SimpleListAdapter(this, new SimpleListAdapter.OnItemSelectedListener() {
            @Override
            public void onSelectedItem(int position, String text) {
                onSelected(text);
            }
        });
        mProjectAdapter = new ProjectListAdapter(this);
        mEquipmentAdapter = new EquipmentSelectListAdapter(this);
        mPictureAdapter = new PictureListAdapter(this);
        linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        linearLayoutManager.setAutoMeasureEnabled(true);
        mPictureList.setLayoutManager(linearLayoutManager);
        mPictureList.setAdapter(mPictureAdapter);
        mNoteAdapter = new NoteListEntryAdapter(this);
        mConfirmationFrame = new ConfirmationFrame(mConfirmationFrameView);
        mAutoNext = new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                mHandler.sendEmptyMessageDelayed(MSG_AUTO_RETURN, AUTO_RETURN_DELAY_MS);
                return false;
            }
        };
        mZipCodeWatcher = new ZipCodeWatcher();
        mLastName.setOnEditorActionListener(mAutoNext);
        mEntrySimple.setOnEditorActionListener(mAutoNext);
        PrefHelper.getInstance().setupFromCurrentProjectId();
        computeCurStage();
        if (savedInstanceState != null) {
            mCurStage = Stage.from(savedInstanceState.getInt(KEY_STAGE));
        }
        fillStage();
        EventBus.getDefault().register(this);
        setTitle(getVersionedTitle());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.profile:
                setStage(Stage.LOGIN);
                break;
            case R.id.upload:
                mApp.ping();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        mApp.ping();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    public void onEvent(EventPingDone event) {
        if (mCurStage == Stage.CURRENT_PROJECT) {
            mHandler.sendEmptyMessage(MSG_REFRESH_PROJECTS);
        }
    }

    public void onEvent(DataZipCode event) {
        if (mCurStage == Stage.ZIPCODE) {
            Message msg = new Message();
            msg.what = MSG_SET_HINT;
            Bundle bundle = new Bundle();
            bundle.putString(KEY_HINT, event.getHint());
            msg.setData(bundle);
            mHandler.sendMessage(msg);
        }
    }

    void computeCurStage() {
        boolean inEntry = false;
        if (TextUtils.isEmpty(PrefHelper.getInstance().getLastName())) {
            mCurStage = Stage.LOGIN;
        } else if (TextUtils.isEmpty(PrefHelper.getInstance().getProjectName())) {
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
            inEntry = true;
        }
//            if (TextUtils.isEmpty(PrefHelper.getInstance().getZipCode()) &&
//                    TextUtils.isEmpty(PrefHelper.getInstance().getState()) &&
//                    TextUtils.isEmpty(PrefHelper.getInstance().getCity()) &&
//                    TextUtils.isEmpty(PrefHelper.getInstance().getStreet())) {
//                mCurStage = Stage.ZIPCODE;
//            } else {
//                if (TextUtils.isEmpty(PrefHelper.getInstance().getZipCode())) {
//                    if (TextUtils.isEmpty(PrefHelper.getInstance().getState())) {
//                        mCurStage = Stage.STATE;
//                    } else if (TextUtils.isEmpty(PrefHelper.getInstance().getCity())) {
//                        mCurStage = Stage.CITY;
//                    } else if (TextUtils.isEmpty(PrefHelper.getInstance().getStreet())) {
//                        mCurStage = Stage.STREET;
//                    } else {
//                        inEntry = true;
//                    }
//                } else {
//                    inEntry = true;
//                }
//            }
        if (inEntry) {
            boolean hasTruckNumber = !TextUtils.isEmpty(getTruckValue());
            boolean hasNotes = mNoteAdapter.hasNotesEntered();
            boolean hasEquip = mEquipmentAdapter.hasChecked();
            boolean hasPictures = TablePictureCollection.getInstance().countPendingPictures() > 0;

            if (!hasTruckNumber && !hasNotes && !hasEquip && !hasPictures) {
                mCurStage = Stage.CURRENT_PROJECT;
            } else if (!hasTruckNumber) {
                mCurStage = Stage.TRUCK_NUMBER;
            } else if (!hasEquip) {
                mCurStage = Stage.EQUIPMENT;
            } else if (!hasNotes) {
                mCurStage = Stage.NOTES;
            } else {
                mCurStage = Stage.PICTURE;
            }
        }
    }

    String getEditText(EditText text) {
        return text.getText().toString().trim();
    }

    boolean save(boolean isNext) {
        if (mCurStageEditing) {
            if (mCurStage == Stage.CITY) {
                PrefHelper.getInstance().setCity(getEditText(mEntrySimple));
            } else if (mCurStage == Stage.STREET) {
                PrefHelper.getInstance().setStreet(getEditText(mEntrySimple));
            } else if (mCurStage == Stage.EQUIPMENT) {
                String name = getEditText(mEntrySimple);
                if (!TextUtils.isEmpty(name)) {
                    DataProjectAddressCombo group = PrefHelper.getInstance().getCurrentProjectGroup();
                    if (group != null) {
                        TableCollectionEquipmentProject.getInstance().addLocal(name, group.projectNameId);
                    }
                }
            } else if (mCurStage == Stage.ZIPCODE) {
                String zipCode = getEditText(mEntrySimple);
                if (isZipCode(zipCode)) {
                    PrefHelper.getInstance().setZipCode(zipCode);
                }
            } else if (mCurStage == Stage.COMPANY) {
                String newCompanyName = getEditText(mEntrySimple).trim();
                if (isNext) {
                    if (TextUtils.isEmpty(newCompanyName)) {
                        showError(getString(R.string.error_need_new_company));
                        return false;
                    }
                    PrefHelper.getInstance().setCompany(newCompanyName);
                    if (mCompanyEditing != null) {
                        List<DataAddress> companies = TableAddress.getInstance().queryByCompanyName(mCompanyEditing);
                        for (DataAddress address : companies) {
                            address.company = newCompanyName;
                            TableAddress.getInstance().update(address);
                        }
                    }
                }
            }
        } else if (mCurStage == Stage.LOGIN) {
            String firstName = getEditText(mFirstName);
            String lastName = getEditText(mLastName);
            PrefHelper.getInstance().setFirstName(firstName);
            PrefHelper.getInstance().setLastName(lastName);
            if (TextUtils.isEmpty(firstName) || TextUtils.isEmpty(lastName)) {
                showError(getString(R.string.error_enter_your_name));
                return false;
            }
            PrefHelper.getInstance().setRegistrationChanged(true);
            mApp.ping();
        } else if (mCurStage == Stage.TRUCK_NUMBER) {
            String value = mEntrySimple.getText().toString();
            if (TextUtils.isEmpty(value)) {
                if (isNext) {
                    showError(getString(R.string.error_need_a_truck_number));
                }
                return false;
            } else {
                if (TextUtils.isDigitsOnly(value)) {
                    PrefHelper.getInstance().setTruckNumber(Long.parseLong(value));
                } else {
                    PrefHelper.getInstance().setLicensePlate(value);
                }
            }
        } else if (mCurStage == Stage.EQUIPMENT) {
            if (isNext) {
                if (TableEquipment.getInstance().countChecked() == 0) {
                    showError(getString(R.string.error_need_equipment));
                    return false;
                }
            }
        } else if (mCurStage == Stage.COMPANY) {
            if (isNext) {
                if (TextUtils.isEmpty(PrefHelper.getInstance().getCompany())) {
                    showError(getString(R.string.error_need_company));
                    return false;
                }
            }
        }
        mCurStageEditing = false;
        mSoftKeyboardDetect.clear();
        return true;
    }

    void skip() {
        if (mWasNext) {
            doBtnNext();
        } else {
            doBtnPrev();
        }
    }

    void doAutoNext() {
        if (mCurStage == Stage.LOGIN) {
            doBtnCenter();
        } else {
            doBtnNext();
        }
    }

    void doBtnNext() {
        if (save(true)) {
            doNext_();
        }
    }

    void doNext_() {
        mWasNext = true;
        mCurStage = Stage.from(mCurStage.ordinal() + 1);
        fillStage();
    }

    void doBtnPrev() {
        save(false);
        doPrev_();
    }

    void doPrev_() {
        mWasNext = false;
        if (mCurStage == Stage.PROJECT) {
            PrefHelper.getInstance().recoverProject();
            mCurStage = Stage.CURRENT_PROJECT;
        } else if (mCurStage == Stage.STATE) {
            mCurStage = Stage.COMPANY;
        } else {
            mCurStage = Stage.from(mCurStage.ordinal() - 1);
        }
        fillStage();
    }

    void doBtnCenter() {
        mCurStageEditing = true;
        fillStage();
    }

    void setStage(Stage stage) {
        save(false);
        mCurStage = stage;
        fillStage();
    }

    void fillStage() {
        mLoginFrame.setVisibility(View.GONE);
        mEntryFrame.setVisibility(View.GONE);
        mMainListFrame.setVisibility(View.GONE);
        mAdd.setVisibility(View.GONE);
        mNext.setVisibility(View.INVISIBLE);
        mNext.setText(R.string.btn_next);
        mPrev.setVisibility(View.INVISIBLE);
        mCenter.setVisibility(View.INVISIBLE);
        mCenter.setText(R.string.btn_add);
        mPrev.setText(R.string.btn_prev);
        mEntrySimple.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        mEntrySimple.removeTextChangedListener(mZipCodeWatcher);
        mEntryHint.setVisibility(View.GONE);
        mConfirmationFrame.setVisibility(View.GONE);
        mPictureFrame.setVisibility(View.GONE);
        mPictureList.setVisibility(View.GONE);
        mEmptyView.setVisibility(View.GONE);
        mMainList.setVisibility(View.VISIBLE);
        mCompanyEditing = null;

        switch (mCurStage) {
            case LOGIN:
                if (mCurStageEditing) {
                    mCurStageEditing = false;
                    save(true);
                    mCurStage = Stage.CURRENT_PROJECT;
                    fillStage();
                } else {
                    mLoginFrame.setVisibility(View.VISIBLE);
                    mCenter.setVisibility(View.VISIBLE);
                    mCenter.setText(R.string.title_login);
                    mTitle.setText(R.string.title_login);
                    mFirstName.setText(PrefHelper.getInstance().getFirstName());
                    mLastName.setText(PrefHelper.getInstance().getLastName());
                }
                break;
            case PROJECT:
                mPrev.setVisibility(View.VISIBLE);
                mMainListFrame.setVisibility(View.VISIBLE);
                if (PrefHelper.getInstance().getProjectName() != null) {
                    mNext.setVisibility(View.VISIBLE);
                }
                setList(R.string.title_project, PrefHelper.KEY_PROJECT, TableProjects.getInstance().query(true));
                break;
            case COMPANY:
                mPrev.setVisibility(View.VISIBLE);
                mNext.setVisibility(View.VISIBLE);
                if (mCurStageEditing) {
                    mTitle.setText(R.string.title_company);
                    mEntryFrame.setVisibility(View.VISIBLE);
                    mEntrySimple.setHint(R.string.title_company);
                    if (isLocalCompany()) {
                        mCompanyEditing = PrefHelper.getInstance().getCompany();
                        mEntrySimple.setText(mCompanyEditing);
                    } else {
                        mEntrySimple.setText("");
                    }
                } else {
                    mMainListFrame.setVisibility(View.VISIBLE);
                    mCenter.setVisibility(View.VISIBLE);
                    List<String> companies = TableAddress.getInstance().queryCompanies();
                    setList(R.string.title_company, PrefHelper.KEY_COMPANY, companies);
                    checkEdit();
                }
                break;
            case ZIPCODE: {
                mPrev.setVisibility(View.VISIBLE);
                mNext.setVisibility(View.VISIBLE);
                PrefHelper.getInstance().setCity(null);
                PrefHelper.getInstance().setState(null);
                PrefHelper.getInstance().setZipCode(null);
                showHint();
                final String company = PrefHelper.getInstance().getCompany();
                List<String> zipcodes = TableAddress.getInstance().queryZipCodes(company);
                final boolean hasZipCodes = zipcodes.size() > 0;
                if (!hasZipCodes) {
                    mCurStageEditing = true;
                }
                if (mCurStageEditing) {
                    mTitle.setText(R.string.title_zipcode);
                    mEntryFrame.setVisibility(View.VISIBLE);
                    mEntrySimple.setHint(R.string.title_zipcode);
                    mEntrySimple.setText("");
                    mEntrySimple.addTextChangedListener(mZipCodeWatcher);
                    mEntrySimple.setRawInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                } else {
                    mMainListFrame.setVisibility(View.VISIBLE);
                    setList(R.string.title_zipcode, PrefHelper.KEY_ZIPCODE, zipcodes);
                    mCenter.setVisibility(View.VISIBLE);
                    mNext.setVisibility(View.VISIBLE);
                }
                break;
            }
            case STATE: {
                showHint();
                mMainListFrame.setVisibility(View.VISIBLE);
                mPrev.setVisibility(View.VISIBLE);
                final String company = PrefHelper.getInstance().getCompany();
                final String zipcode = PrefHelper.getInstance().getZipCode();
                List<String> states = TableAddress.getInstance().queryStates(company, zipcode);
                if (states.size() == 0) {
                    String state = TableZipCode.getInstance().queryState(zipcode);
                    if (state != null) {
                        states = new ArrayList<>();
                        states.add(state);
                    } else {
                        mCurStageEditing = true;
                    }
                }
                if (mCurStageEditing) {
                    states = DataStates.getUnusedStates(states);
                    PrefHelper.getInstance().setState(null);
                    setList(R.string.title_state, PrefHelper.KEY_STATE, states);
                } else {
                    if (!TextUtils.isEmpty(zipcode) && (states.size() == 1)) {
                        PrefHelper.getInstance().setState(states.get(0));
                        skip();
                    } else {
                        if (setList(R.string.title_state, PrefHelper.KEY_STATE, states)) {
                            mNext.setVisibility(View.VISIBLE);
                        }
                        mCenter.setVisibility(View.VISIBLE);
                    }
                }
                break;
            }
            case CITY: {
                showHint();
                mPrev.setVisibility(View.VISIBLE);
                final String company = PrefHelper.getInstance().getCompany();
                final String zipcode = PrefHelper.getInstance().getZipCode();
                final String state = PrefHelper.getInstance().getState();
                List<String> cities = TableAddress.getInstance().queryCities(company, zipcode, state);
                if (cities.size() == 0) {
                    String city = TableZipCode.getInstance().queryCity(zipcode);
                    if (city != null) {
                        cities = new ArrayList<>();
                        cities.add(city);
                    } else {
                        mCurStageEditing = true;
                    }
                }
                if (mCurStageEditing) {
                    mEntryFrame.setVisibility(View.VISIBLE);
                    mTitle.setText(R.string.title_city);
                    mEntrySimple.setHint(R.string.title_city);
                    mEntrySimple.setText("");
                } else {
                    if (!TextUtils.isEmpty(zipcode) && !TextUtils.isEmpty(state) && cities.size() == 1) {
                        PrefHelper.getInstance().setCity(cities.get(0));
                        skip();
                    } else {
                        mMainListFrame.setVisibility(View.VISIBLE);
                        mCenter.setVisibility(View.VISIBLE);
                        if (setList(R.string.title_city, PrefHelper.KEY_CITY, cities)) {
                            mNext.setVisibility(View.VISIBLE);
                        }
                    }
                }
                break;
            }
            case STREET: {
                mPrev.setVisibility(View.VISIBLE);
                showHint();
                List<String> streets = TableAddress.getInstance().queryStreets(
                        PrefHelper.getInstance().getCompany(),
                        PrefHelper.getInstance().getCity(),
                        PrefHelper.getInstance().getState(),
                        PrefHelper.getInstance().getZipCode());
                if (streets.size() == 0) {
                    mCurStageEditing = true;
                }
                if (mCurStageEditing) {
                    mTitle.setText(R.string.title_street);
                    mEntryFrame.setVisibility(View.VISIBLE);
                    mEntrySimple.setHint(R.string.title_street);
                    mEntrySimple.setText("");
                    mEntrySimple.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_POSTAL_ADDRESS | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
                } else {
                    mCenter.setVisibility(View.VISIBLE);
                    mMainListFrame.setVisibility(View.VISIBLE);
                    if (setList(R.string.title_street, PrefHelper.KEY_STREET, streets)) {
                        mNext.setVisibility(View.VISIBLE);
                    }
                }
                break;
            }
            case CURRENT_PROJECT:
                if (mCurStageEditing) {
                    PrefHelper.getInstance().clearCurProject();
                    mCurStageEditing = false;
                    mCurStage = Stage.PROJECT;
                    fillStage();
                } else {
                    PrefHelper.getInstance().saveProjectAndAddressCombo();
                    mMainListFrame.setVisibility(View.VISIBLE);
                    mCenter.setVisibility(View.VISIBLE);
                    if (TableProjectAddressCombo.getInstance().count() > 0) {
                        mAdd.setVisibility(View.VISIBLE);
                    }
                    mCurKey = null;
                    mCenter.setText(R.string.btn_new_project);
                    mTitle.setText(R.string.title_current_project);
                    mMainList.setAdapter(mProjectAdapter);
                    mProjectAdapter.onDataChanged();
                }
                break;
            case TRUCK_NUMBER:
                mNext.setVisibility(View.VISIBLE);
                mPrev.setVisibility(View.VISIBLE);
                mEntryFrame.setVisibility(View.VISIBLE);
                mTitle.setText(R.string.title_truck_number);
                mEntrySimple.setHint(R.string.title_truck_number);
                mEntrySimple.setText(getTruckValue());
                mEntrySimple.setInputType(InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
                break;
            case EQUIPMENT:
                mNext.setVisibility(View.VISIBLE);
                mPrev.setVisibility(View.VISIBLE);
                if (mCurStageEditing) {
                    mEntryFrame.setVisibility(View.VISIBLE);
                    mTitle.setText(R.string.title_equipment);
                    mEntrySimple.setHint(R.string.title_equipment);
                    mEntrySimple.setText("");
                } else {
                    if (isNewEquipmentOkay()) {
                        mCenter.setVisibility(View.VISIBLE);
                    }
                    mTitle.setText(R.string.title_equipment_installed);
                    mMainList.setAdapter(mEquipmentAdapter);
                    mEquipmentAdapter.onDataChanged();
                    mMainListFrame.setVisibility(View.VISIBLE);
                    mCenter.setVisibility(View.VISIBLE);
                }
                break;
            case NOTES:
                mNext.setVisibility(View.VISIBLE);
                mPrev.setVisibility(View.VISIBLE);
                mNoteAdapter.onDataChanged();
                mTitle.setText(R.string.title_notes);
                mMainListFrame.setVisibility(View.VISIBLE);
                if (mNoteAdapter.getItemCount() == 0) {
                    mMainList.setVisibility(View.GONE);
                    mEmptyView.setVisibility(View.VISIBLE);
                } else {
                    mMainList.setAdapter(mNoteAdapter);
                    mMainList.setVisibility(View.VISIBLE);
                    mEmptyView.setVisibility(View.GONE);
                }
                break;
            case PICTURE:
                int numberTaken = TablePictureCollection.getInstance().countPendingPictures();
                StringBuilder sbuf = new StringBuilder();
                sbuf.append(getString(R.string.title_picture));
                sbuf.append(" ");
                sbuf.append(numberTaken);
                mTitle.setText(sbuf.toString());
                mPrev.setVisibility(View.VISIBLE);
                if (mCurStageEditing || numberTaken == 0) {
                    mCurStageEditing = false;
                    if (!dispatchPictureRequest()) {
                        showError(getString(R.string.error_cannot_take_picture));
                    }
                } else {
                    mNext.setVisibility(View.VISIBLE);
                    mNext.setText(R.string.btn_done);
                    mCenter.setVisibility(View.VISIBLE);
                    mCenter.setText(R.string.btn_another);
                    mPictureFrame.setVisibility(View.VISIBLE);
                    mPictureList.setVisibility(View.VISIBLE);
                    mPictureAdapter.setList(
                            TablePictureCollection.getInstance().removeNonExistant(
                                    TablePictureCollection.getInstance().queryPendingPictures()));
                }
                break;
            case CONFIRM:
                mPrev.setVisibility(View.VISIBLE);
                mNext.setVisibility(View.VISIBLE);
                mNext.setText(R.string.btn_confirm);
                mConfirmationFrame.setVisibility(View.VISIBLE);
                mCurEntry = PrefHelper.getInstance().createEntry();
                mConfirmationFrame.fill(mCurEntry);
                mTitle.setText(R.string.title_confirmation);
                break;
            case ADD_ELEMENT:
                TableEntry.getInstance().add(mCurEntry);
                PrefHelper.getInstance().clearLastEntry();
                mApp.ping();
                mCurStage = Stage.CURRENT_PROJECT;
                mCurEntry = null;
                fillStage();
                break;
        }
    }

    boolean isNewEquipmentOkay() {
        String name = PrefHelper.getInstance().getProjectName();
        if (name.equals(TBApplication.OTHER)) {
            return true;
        }
        return false;
    }

    String getTruckValue() {
        String value = PrefHelper.getInstance().getLicensePlate();
        if (value != null) {
            return value;
        }
        long id = PrefHelper.getInstance().getTruckNumber();
        if (id == 0) {
            return "";
        }
        return Long.toString(id);
    }

    // Return false if NoneSelected situation has occured.
    boolean setList(int textId, String key, List<String> list) {
        boolean hasSelection = true;
        mCurKey = key;
        String text = getString(textId);
        mTitle.setText(text);

        if (list.size() == 0) {
            doBtnCenter();
        } else {
            mSimpleAdapter.setList(list);
            mMainList.setAdapter(mSimpleAdapter);
            String curValue = PrefHelper.getInstance().getString(key, null);
            if (curValue == null) {
                mSimpleAdapter.setNoneSelected();
                hasSelection = false;
            } else {
                int position = mSimpleAdapter.setSelected(curValue);
                if (position >= 0) {
                    mMainList.scrollToPosition(position);
                }
            }
        }
        return hasSelection;
    }

    void onSelected(String text) {
        if (mCurKey != null) {
            PrefHelper.getInstance().setString(mCurKey, text);
            if (mCurStage == Stage.PROJECT || mCurStage == Stage.CITY || mCurStage == Stage.STATE || mCurStage == Stage.STREET/* || mCurStage == Stage.ZIPCODE*/) {
                mNext.setVisibility(View.VISIBLE);
            } else if (mCurStage == Stage.COMPANY) {
                checkEdit();
            }
        }
    }

    boolean dispatchPictureRequest() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File pictureFile = PrefHelper.getInstance().genFullPictureFile();
            TablePictureCollection.getInstance().add(pictureFile);
            Uri pictureUri = TBApplication.getUri(this, pictureFile);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, pictureUri);
            // Grant permissions
            List<ResolveInfo> resInfoList = getPackageManager().queryIntentActivities(takePictureIntent, PackageManager.MATCH_DEFAULT_ONLY);
            for (ResolveInfo resolveInfo : resInfoList) {
                String packageName = resolveInfo.activityInfo.packageName;
                grantUriPermission(packageName, pictureUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
            // Start Camera activity
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            return true;
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            fillStage();
        }
    }

    void showError(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.title_error);
        builder.setMessage(message);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if (mCurStage == Stage.PICTURE) {
                    setStage(Stage.CONFIRM);
                }
            }
        });
        builder.create().show();
    }

    void checkEdit() {
        if (mCurStage == Stage.COMPANY) {
            if (isLocalCompany()) {
                mCenter.setText(R.string.btn_edit);
            } else {
                mCenter.setText(R.string.btn_add);
            }
        }
    }

    boolean isLocalCompany() {
        return TableAddress.getInstance().isLocalCompanyOnly(PrefHelper.getInstance().getCompany());
    }

    String getVersionedTitle() {
        StringBuilder sbuf = new StringBuilder();
        sbuf.append(getString(R.string.app_name));
        sbuf.append(" - ");
        try {
            String version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            sbuf.append("v");
            sbuf.append(version);

            if (PrefHelper.getInstance().isDevelopment()) {
                sbuf.append("d");
            }
        } catch (Exception e) {
            Timber.e(e);
        }
        return sbuf.toString();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        mCurStage = Stage.from(savedInstanceState.getInt(KEY_STAGE));
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(KEY_STAGE, mCurStage.ordinal());
        super.onSaveInstanceState(outState);
    }

    boolean isZipCode(String zipCode) {
        return zipCode != null && zipCode.length() == 5 && zipCode.matches("^[0-9]*$");
    }

    void showHint() {
        String hint = null;
        switch (mCurStage) {
            case ZIPCODE:
            case STATE:
            case CITY:
            case STREET:
                hint = PrefHelper.getInstance().getAddress();
                break;
        }
        if (hint != null) {
            mEntryHint.setText(hint);
            mEntryHint.setVisibility(View.VISIBLE);
        }
    }
}
