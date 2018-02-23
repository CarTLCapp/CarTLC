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
import android.support.v4.content.ContextCompat;
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
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.cartlc.tracker.BuildConfig;
import com.cartlc.tracker.R;
import com.cartlc.tracker.app.TBApplication;
import com.cartlc.tracker.data.DataAddress;
import com.cartlc.tracker.data.DataEntry;
import com.cartlc.tracker.data.DataNote;
import com.cartlc.tracker.data.DataProjectAddressCombo;
import com.cartlc.tracker.data.DataStates;
import com.cartlc.tracker.data.DataZipCode;
import com.cartlc.tracker.etc.CheckError;
import com.cartlc.tracker.etc.PrefHelper;
import com.cartlc.tracker.data.TableAddress;
import com.cartlc.tracker.data.TableEntry;
import com.cartlc.tracker.data.TableEquipment;
import com.cartlc.tracker.data.TableCollectionEquipmentProject;
import com.cartlc.tracker.data.TablePictureCollection;
import com.cartlc.tracker.data.TableProjectAddressCombo;
import com.cartlc.tracker.data.TableProjects;
import com.cartlc.tracker.data.TableTruck;
import com.cartlc.tracker.data.TableZipCode;
import com.cartlc.tracker.etc.TruckStatus;
import com.cartlc.tracker.event.EventError;
import com.cartlc.tracker.event.EventRefreshProjects;
import com.cartlc.tracker.util.DialogHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;

public class MainActivity extends AppCompatActivity {

    static final boolean ALLOW_EMPTY_TRUCK = BuildConfig.DEBUG; // true=Debugging only

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_EDIT_ENTRY    = 2;

    static final int AUTO_RETURN_DELAY_MS = 100;
    static final int MSG_AUTO_RETURN      = 0;
    static final int MSG_REFRESH_PROJECTS = 1;
    static final int MSG_SET_HINT         = 2;
    static final int MSG_SHOW_ERROR       = 3;

    static final String KEY_HINT = "hint";
    static final String KEY_MSG  = "msg";

    public static final int RESULT_EDIT_ENTRY     = 2;
    public static final int RESULT_EDIT_PROJECT   = 3;
    public static final int RESULT_DELETE_PROJECT = 4;

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
                    break;
                case MSG_SHOW_ERROR:
                    showServerError(msg.getData().getString(KEY_MSG));
                    break;
            }
        }
    }

    class SoftKeyboardDetect implements ViewTreeObserver.OnGlobalLayoutListener {

        int mInitialHeight;

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
        TRUCK,
        EQUIPMENT,
        NOTES,
        PICTURE,
        STATUS,
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

    @BindView(R.id.first_name)           EditText             mFirstName;
    @BindView(R.id.last_name)            EditText             mLastName;
    @BindView(R.id.entry_simple)         EditText             mEntrySimple;
    @BindView(R.id.entry_hint)           TextView             mEntryHint;
    @BindView(R.id.list_entry_hint)      TextView             mListEntryHint;
    @BindView(R.id.frame_login)          ViewGroup            mLoginFrame;
    @BindView(R.id.frame_entry)          ViewGroup            mEntryFrame;
    @BindView(R.id.frame_status)         ViewGroup            mStatusFrame;
    @BindView(R.id.main_list)            RecyclerView         mMainList;
    @BindView(R.id.main_list_frame)      FrameLayout          mMainListFrame;
    @BindView(R.id.next)                 Button               mNext;
    @BindView(R.id.prev)                 Button               mPrev;
    @BindView(R.id.new_entry)            Button               mCenter;
    @BindView(R.id.main_title)           LinearLayout         mMainTitle;
    @BindView(R.id.main_title_text)      TextView             mMainTitleText;
    @BindView(R.id.sub_title)            TextView             mSubTitle;
    @BindView(R.id.main_title_separator) View                 mMainTitleSeparator;
    @BindView(R.id.fab_add)              FloatingActionButton mAdd;
    @BindView(R.id.frame_confirmation)   FrameLayout          mConfirmationFrameView;
    @BindView(R.id.frame_pictures)       ViewGroup            mPictureFrame;
    @BindView(R.id.list_pictures)        RecyclerView         mPictureList;
    @BindView(R.id.empty)                TextView             mEmptyView;
    @BindView(R.id.root)                 ViewGroup            mRoot;
    @BindView(R.id.buttons)              ViewGroup            mButtons;
    @BindView(R.id.status_select)        RadioGroup           mStatusSelect;
    @BindView(R.id.status_needs_repair)  RadioButton          mStatusNeedsRepair;
    @BindView(R.id.status_complete)      RadioButton          mStatusComplete;
    @BindView(R.id.status_partial)       RadioButton          mStatusPartial;

    Stage              mCurStage           = Stage.LOGIN;
    String             mCurKey             = PrefHelper.KEY_STATE;
    MyHandler          mHandler            = new MyHandler();
    SoftKeyboardDetect mSoftKeyboardDetect = new SoftKeyboardDetect();
    TBApplication              mApp;
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
    DialogHelper               mDialogHelper;
    boolean                    mWasNext;
    boolean                    mCurStageEditing;
    boolean                    mDoingCenter;
    boolean mShowServerError = true;
    boolean mEditCurProject  = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mApp = (TBApplication) getApplicationContext();
        setContentView(R.layout.activity_main);
        mApp.setUncaughtExceptionHandler(this);
        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mRoot.getViewTreeObserver().addOnGlobalLayoutListener(mSoftKeyboardDetect);
        mInputMM = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doBtnNext();
                TBApplication.hideKeyboard(MainActivity.this, v);
            }
        });
        mPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doBtnPrev();
                TBApplication.hideKeyboard(MainActivity.this, v);
            }
        });
        mDialogHelper = new DialogHelper(this);
        mCenter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doBtnCenter();
            }
        });
        mAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doBtnPlus();
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
        mNoteAdapter = new NoteListEntryAdapter(this, new NoteListEntryAdapter.EntryListener() {
            DataNote currentFocus;

            @Override
            public void textEntered(DataNote note) {
                if (currentFocus == note) {
                    display(note);
                }
            }

            @Override
            public void textFocused(DataNote note) {
                currentFocus = note;
                display(note);
            }

            void display(DataNote note) {
                if (mCurStage == Stage.NOTES) {
                    if (note.num_digits > 0) {
                        if (note.value != null && note.value.length() > 0) {
                            StringBuilder sbuf = new StringBuilder();
                            int count = note.value.length();
                            sbuf.append(count);
                            sbuf.append("/");
                            sbuf.append(note.num_digits);
                            mListEntryHint.setText(sbuf.toString());
                            if (count > note.num_digits) {
                                mListEntryHint.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.entry_error_color));
                            } else {
                                mListEntryHint.setTextColor(ContextCompat.getColor(MainActivity.this, android.R.color.white));
                            }
                        } else {
                            mListEntryHint.setText("");
                        }
                        mListEntryHint.setVisibility(View.VISIBLE);
                    } else {
                        mListEntryHint.setVisibility(View.GONE);
                    }
                }
            }
        });
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
        PrefHelper.getInstance().setFromCurrentProjectId();
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
                PrefHelper.getInstance().reloadFromServer();
                mApp.ping();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        mApp.checkPermissions(this, null);
        mApp.ping();
        mDoingCenter = false; // Safety
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        CheckError.getInstance().cleanup();
        mDialogHelper.clearDialog();
    }

    public void onEvent(EventRefreshProjects event) {
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

    public void onEvent(EventError event) {
        if (mShowServerError) {
            Message msg = new Message();
            msg.what = MSG_SHOW_ERROR;
            Bundle bundle = new Bundle();
            bundle.putString(KEY_MSG, event.toString());
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
        if (inEntry) {
            boolean hasTruck = !TextUtils.isEmpty(PrefHelper.getInstance().getTruckValue());
            boolean hasNotes = mNoteAdapter.hasNotesEntered() && mNoteAdapter.isNotesComplete();
            boolean hasEquip = mEquipmentAdapter.hasChecked();
            boolean hasPictures = TablePictureCollection.getInstance().countPictures(PrefHelper.getInstance().getCurrentPictureCollectionId()) > 0;
            if (!hasTruck && !hasNotes && !hasEquip && !hasPictures) {
                mCurStage = Stage.CURRENT_PROJECT;
            } else if (!hasTruck) {
                mCurStage = Stage.TRUCK;
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
        if (mCurStage == Stage.TRUCK) {
            String value = mEntrySimple.getText().toString().trim();
            if (TextUtils.isEmpty(value)) {
                if (isNext) {
                    showError(getString(R.string.error_need_a_truck_number));
                }
                if (!ALLOW_EMPTY_TRUCK) {
                    return false;
                }
                // For debugging purposes only.
                PrefHelper.getInstance().setTruckNumber(null);
                PrefHelper.getInstance().setLicensePlate(null);
                PrefHelper.getInstance().setDoErrorCheck(true);
            } else {
                PrefHelper.getInstance().parseTruckValue(value);
            }
        } else if (mCurStageEditing) {
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
        } else if (mCurStage == Stage.NOTES) {
            if (isNext) {
                if (detectNoteError()) {
                    return false;
                }
            }
        } else if (mCurStage == Stage.STATUS) {
            if (isNext) {
                if (PrefHelper.getInstance().getStatus() == TruckStatus.UNKNOWN) {
                    showError(getString(R.string.error_need_status));
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

    void doBtnPlus() {
        if (PrefHelper.getInstance().getCurrentEditEntryId() != 0) {
            PrefHelper.getInstance().clearLastEntry();
        }
        doBtnNext();
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
        if (mCurStage == Stage.CURRENT_PROJECT) {
            doViewProject();
        } else {
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
    }

    void doBtnCenter() {
        if (!mDoingCenter) {
            mDoingCenter = true;
            mCurStageEditing = true;
            fillStage();
            mDoingCenter = false;
        }
    }

    void doViewProject() {
        Intent intent = new Intent(this, ListEntryActivity.class);
        startActivityForResult(intent, REQUEST_EDIT_ENTRY);
    }

    void setStage(Stage stage) {
        save(false);
        mCurStage = stage;
        fillStage();
    }

    void fillStage() {
        mLoginFrame.setVisibility(View.GONE);
        mEntryFrame.setVisibility(View.GONE);
        mStatusFrame.setVisibility(View.GONE);
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
        mConfirmationFrame.setVisibility(View.GONE);
        mPictureFrame.setVisibility(View.GONE);
        mPictureList.setVisibility(View.GONE);
        mEmptyView.setVisibility(View.GONE);
        mListEntryHint.setVisibility(View.GONE);
        mEntryHint.setVisibility(View.GONE);
        mSubTitle.setVisibility(View.GONE);
        mMainTitleSeparator.setVisibility(View.GONE);
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
                    mMainTitleText.setText(R.string.title_login);
                    mFirstName.setText(PrefHelper.getInstance().getFirstName());
                    mLastName.setText(PrefHelper.getInstance().getLastName());
                }
                mApp.ping();
                break;
            case PROJECT:
                mPrev.setVisibility(View.VISIBLE);
                showMainListFrame();
                showSubTitleHint();
                if (PrefHelper.getInstance().getProjectName() != null) {
                    mNext.setVisibility(View.VISIBLE);
                }
                setList(R.string.title_project, PrefHelper.KEY_PROJECT, TableProjects.getInstance().query(true));
                break;
            case COMPANY:
                mPrev.setVisibility(View.VISIBLE);
                mNext.setVisibility(View.VISIBLE);
                showSubTitleHint();
                if (mCurStageEditing) {
                    mMainTitleText.setText(R.string.title_company);
                    mEntryFrame.setVisibility(View.VISIBLE);
                    mEntrySimple.setHint(R.string.title_company);
                    if (isLocalCompany()) {
                        mCompanyEditing = PrefHelper.getInstance().getCompany();
                        mEntrySimple.setText(mCompanyEditing);
                    } else {
                        mEntrySimple.setText("");
                    }
                } else {
                    showMainListFrame();
                    mCenter.setVisibility(View.VISIBLE);
                    List<String> companies = TableAddress.getInstance().queryCompanies();
                    setList(R.string.title_company, PrefHelper.KEY_COMPANY, companies);
                    checkEdit();
                }
                break;
            case ZIPCODE: {
                mPrev.setVisibility(View.VISIBLE);
                mNext.setVisibility(View.VISIBLE);
                showEntryHint();
                showSubTitleHint();
                final String company = PrefHelper.getInstance().getCompany();
                List<String> zipcodes = TableAddress.getInstance().queryZipCodes(company);
                final boolean hasZipCodes = zipcodes.size() > 0;
                if (!hasZipCodes) {
                    mCurStageEditing = true;
                }
                if (mCurStageEditing) {
                    mMainTitleText.setText(R.string.title_zipcode);
                    mEntryFrame.setVisibility(View.VISIBLE);
                    mEntrySimple.setHint(R.string.title_zipcode);
                    mEntrySimple.setText(PrefHelper.getInstance().getZipCode());
                    mEntrySimple.addTextChangedListener(mZipCodeWatcher);
                    mEntrySimple.setRawInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                } else {
                    showMainListFrame();
                    setList(R.string.title_zipcode, PrefHelper.KEY_ZIPCODE, zipcodes);
                    mCenter.setVisibility(View.VISIBLE);
                    mNext.setVisibility(View.VISIBLE);
                }
                break;
            }
            case STATE: {
                showEntryHint();
                showSubTitleHint();
                showMainListFrame();
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
                showEntryHint();
                showSubTitleHint();
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
                    mMainTitleText.setText(R.string.title_city);
                    mEntrySimple.setHint(R.string.title_city);
                    mEntrySimple.setText("");
                } else {
                    if (!TextUtils.isEmpty(zipcode) && !TextUtils.isEmpty(state) && cities.size() == 1) {
                        PrefHelper.getInstance().setCity(cities.get(0));
                        skip();
                    } else {
                        showMainListFrame();
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
                showEntryHint();
                showSubTitleHint();
                List<String> streets = TableAddress.getInstance().queryStreets(
                        PrefHelper.getInstance().getCompany(),
                        PrefHelper.getInstance().getCity(),
                        PrefHelper.getInstance().getState(),
                        PrefHelper.getInstance().getZipCode());
                if (streets.size() == 0) {
                    mCurStageEditing = true;
                }
                if (mCurStageEditing) {
                    mMainTitleText.setText(R.string.title_street);
                    mEntryFrame.setVisibility(View.VISIBLE);
                    mEntrySimple.setHint(R.string.title_street);
                    mEntrySimple.setText("");
                    mEntrySimple.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_POSTAL_ADDRESS | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
                } else {
                    mCenter.setVisibility(View.VISIBLE);
                    showMainListFrame();
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
                    mApp.ping();
                    PrefHelper.getInstance().saveProjectAndAddressCombo(mEditCurProject);
                    mEditCurProject = false;
                    showMainListFrame();
                    mMainTitleSeparator.setVisibility(View.VISIBLE);
                    mCenter.setVisibility(View.VISIBLE);
                    mPrev.setVisibility(View.VISIBLE);
                    mPrev.setText(R.string.btn_edit);
                    if (TableProjectAddressCombo.getInstance().count() > 0) {
                        mAdd.setVisibility(View.VISIBLE);
                    }
                    mCurKey = null;
                    mCenter.setText(R.string.btn_new_project);
                    mMainTitleText.setText(R.string.title_current_project);
                    mMainList.setAdapter(mProjectAdapter);
                    mProjectAdapter.onDataChanged();
                    checkErrors();
                }
                break;
            case TRUCK:
                mNext.setVisibility(View.VISIBLE);
                mPrev.setVisibility(View.VISIBLE);
                mEntryFrame.setVisibility(View.VISIBLE);
                mEntrySimple.setHint(R.string.title_truck);
                mEntrySimple.setText(PrefHelper.getInstance().getTruckValue());
                mEntrySimple.setInputType(InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
                showMainListFrame(mEntryFrame);
                setList(R.string.title_truck, PrefHelper.KEY_TRUCK,
                        TableTruck.getInstance().queryStrings(PrefHelper.getInstance().getCurrentProjectGroup()));
                showEntryHint();
                showSubTitleHint();
                break;
            case EQUIPMENT:
                mNext.setVisibility(View.VISIBLE);
                mPrev.setVisibility(View.VISIBLE);
                if (mCurStageEditing) {
                    mEntryFrame.setVisibility(View.VISIBLE);
                    mMainTitleText.setText(R.string.title_equipment);
                    mEntrySimple.setHint(R.string.title_equipment);
                    mEntrySimple.setText("");
                } else {
                    if (isNewEquipmentOkay()) {
                        mCenter.setVisibility(View.VISIBLE);
                    }
                    mMainTitleText.setText(R.string.title_equipment_installed);
                    mMainList.setAdapter(mEquipmentAdapter);
                    mEquipmentAdapter.onDataChanged();
                    showMainListFrame();
                    mCenter.setVisibility(View.VISIBLE);
                }
                break;
            case NOTES:
                mNext.setVisibility(View.VISIBLE);
                mPrev.setVisibility(View.VISIBLE);
                mNoteAdapter.onDataChanged();
                mMainTitleText.setText(R.string.title_notes);
                showMainListFrame();
                if (mNoteAdapter.getItemCount() == 0) {
                    mMainList.setVisibility(View.GONE);
                    mEmptyView.setVisibility(View.VISIBLE);
                } else {
                    mMainList.setAdapter(mNoteAdapter);
                    mEmptyView.setVisibility(View.GONE);
                }
                break;
            case PICTURE:
                int pictureCount = PrefHelper.getInstance().getNumPicturesTaken();
                mMainTitleText.setText(getString(R.string.title_picture, pictureCount));
                mPrev.setVisibility(View.VISIBLE);
                if (mCurStageEditing || pictureCount == 0) {
                    mCurStageEditing = false;
                    if (!dispatchPictureRequest()) {
                        showError(getString(R.string.error_cannot_take_picture));
                    }
                } else {
                    mNext.setVisibility(View.VISIBLE);
                    mCenter.setVisibility(View.VISIBLE);
                    mCenter.setText(R.string.btn_another);
                    mPictureFrame.setVisibility(View.VISIBLE);
                    mPictureList.setVisibility(View.VISIBLE);
                    mPictureAdapter.setList(
                            TablePictureCollection.getInstance().removeNonExistant(
                                    TablePictureCollection.getInstance().queryPictures(PrefHelper.getInstance().getCurrentPictureCollectionId()
                                    )));
                }
                break;
            case STATUS:
                mPrev.setVisibility(View.VISIBLE);
                mNext.setVisibility(View.VISIBLE);
                mNext.setText(R.string.btn_done);
                mStatusFrame.setVisibility(View.VISIBLE);
                mMainTitleText.setText(R.string.title_status);
                setStatusButton();
                mCurEntry = null;
                break;
            case CONFIRM:
                mPrev.setVisibility(View.VISIBLE);
                mNext.setVisibility(View.VISIBLE);
                mNext.setText(R.string.btn_confirm);
                mConfirmationFrame.setVisibility(View.VISIBLE);
                mCurEntry = PrefHelper.getInstance().saveEntry();
                mConfirmationFrame.fill(mCurEntry);
                mMainTitleText.setText(R.string.title_confirmation);
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

    // Return false if NoneSelected situation has occured.
    boolean setList(int titleId, String key, List<String> list) {
        boolean hasSelection = true;
        mCurKey = key;
        String title = getString(titleId);
        mMainTitleText.setText(title);
        if (list.size() == 0) {
            mMainListFrame.setVisibility(View.GONE);
            doBtnCenter();
        } else {
            mSimpleAdapter.setList(list);
            mMainList.setAdapter(mSimpleAdapter);
            String curValue = PrefHelper.getInstance().getKeyValue(key);
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
            PrefHelper.getInstance().setKeyValue(mCurKey, text);
            if (mCurStage == Stage.PROJECT || mCurStage == Stage.CITY || mCurStage == Stage.STATE || mCurStage == Stage.STREET) {
                mNext.setVisibility(View.VISIBLE);
            } else if (mCurStage == Stage.COMPANY) {
                checkEdit();
            } else if (mCurStage == Stage.TRUCK) {
                mEntrySimple.setText(text);
            }
        }
    }

    boolean dispatchPictureRequest() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File pictureFile = PrefHelper.getInstance().genFullPictureFile();
            TablePictureCollection.getInstance().add(pictureFile, PrefHelper.getInstance().getCurrentPictureCollectionId());
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
        if (resultCode == RESULT_EDIT_ENTRY) {
            doEditEntry();
        } else if (resultCode == RESULT_EDIT_PROJECT) {
            doEditProject();
        } else if (resultCode == RESULT_DELETE_PROJECT) {
            PrefHelper.getInstance().clearCurProject();
            mCurStageEditing = false;
            mCurStage = Stage.CURRENT_PROJECT;
            fillStage();
        } else if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                fillStage();
            }
        }
    }

    void doEditEntry() {
        mCurStage = Stage.from(Stage.CURRENT_PROJECT.ordinal() + 1);
        fillStage();
    }

    void doEditProject() {
        mCurStage = Stage.from(Stage.PROJECT.ordinal());
        mEditCurProject = true;
        fillStage();
    }

    public void showError(String message) {
        mDialogHelper.showError(message, new DialogHelper.DialogListener() {
            @Override
            public void onOkay() {
                if (mCurStage == MainActivity.Stage.PICTURE) {
                    setStage(MainActivity.Stage.CONFIRM);
                }
            }

            @Override
            public void onCancel() {
            }
        });

    }

    public void showServerError(String message) {
        mDialogHelper.showServerError(message, new DialogHelper.DialogListener() {
            @Override
            public void onOkay() {
            }

            @Override
            public void onCancel() {
                mShowServerError = false;
            }
        });
    }

    boolean detectNoteError() {
        if (!mNoteAdapter.isNotesComplete()) {
            showNoteError(mNoteAdapter.getNotes());
            return true;
        }
        return false;
    }

    void showNoteError(List<DataNote> notes) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.title_notes);
        StringBuilder sbuf = new StringBuilder();
        for (DataNote note : notes) {
            if (note.num_digits > 0 && note.valueLength() > 0 && (note.valueLength() != note.num_digits)) {
                sbuf.append("    ");
                sbuf.append(note.name);
                sbuf.append(": ");
                sbuf.append(getString(R.string.error_incorrect_note_count, note.valueLength(), note.num_digits));
                sbuf.append("\n");
            }
        }
        String msg = getString(R.string.error_incorrect_digit_count, sbuf.toString());
        builder.setMessage(msg);
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                doNext_();
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
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
            sbuf.append(mApp.getVersion());
        } catch (Exception ex) {
            TBApplication.ReportError(ex, MainActivity.class, "getVersionedTitle()", "main");
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

    void showEntryHint() {
        String hint = null;
        switch (mCurStage) {
            case ZIPCODE:
            case STATE:
            case CITY:
            case STREET:
                hint = PrefHelper.getInstance().getAddress();
                break;
            case STATUS:
                hint = getStatusHint();
                break;
            case TRUCK:
                hint = getString(R.string.entry_hint_truck);
                break;
        }
        if (hint != null && hint.length() > 0) {
            mEntryHint.setText(hint);
            mEntryHint.setVisibility(View.VISIBLE);
        }
    }

    String getStatusHint() {
        StringBuilder sbuf = new StringBuilder();
        int countPictures = PrefHelper.getInstance().getNumPicturesTaken();
        int maxEquip = PrefHelper.getInstance().getNumEquipPossible();
        int checkedEquipment = TableEquipment.getInstance().queryChecked().size();
        sbuf.append(getString(R.string.status_installed_equipments, checkedEquipment, maxEquip));
        sbuf.append("\n");
        sbuf.append(getString(R.string.status_installed_pictures, countPictures));
        return sbuf.toString();
    }

    void showSubTitleHint() {
        String hint = null;
        switch (mCurStage) {
            case PROJECT:
            case COMPANY:
            case ZIPCODE:
            case STATE:
            case CITY:
            case STREET:
                if (mEditCurProject) {
                    hint = getEditProjectHint();
                }
                break;
            case TRUCK:
                hint = PrefHelper.getInstance().getCurrentProjectGroup().getHintLine();
                break;
        }
        if (hint != null && hint.length() > 0) {
            mSubTitle.setText(hint);
            mSubTitle.setVisibility(View.VISIBLE);
        }
    }

    String getEditProjectHint() {
        StringBuilder sbuf = new StringBuilder();
        sbuf.append(getString(R.string.entry_hint_edit_project));
        sbuf.append("\n");
        sbuf.append(PrefHelper.getInstance().getProjectName());
        sbuf.append("\n");
        sbuf.append(PrefHelper.getInstance().getAddress());
        return sbuf.toString();
    }

    void showMainListFrame() {
        showMainListFrame(null);
    }

    void showMainListFrame(View below) {
        if (below == null) {
            below = mMainTitle;
        }
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mMainListFrame.getLayoutParams();
        params.addRule(RelativeLayout.BELOW, below.getId());
        mMainListFrame.setVisibility(View.VISIBLE);
        mMainList.setVisibility(View.VISIBLE);
    }

    public void onStatusButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();
        if (checked) {
            switch (view.getId()) {
                case R.id.status_complete:
                    PrefHelper.getInstance().setStatus(TruckStatus.COMPLETE);
                    break;
                case R.id.status_partial:
                    PrefHelper.getInstance().setStatus(TruckStatus.PARTIAL);
                    break;
                case R.id.status_needs_repair:
                    PrefHelper.getInstance().setStatus(TruckStatus.NEEDS_REPAIR);
                    break;
            }
        }
    }

    void setStatusButton() {
        TruckStatus status = PrefHelper.getInstance().getStatus();
        if (status != null) {
            if (status == TruckStatus.NEEDS_REPAIR) {
                mStatusNeedsRepair.setChecked(true);
            } else if (status == TruckStatus.COMPLETE) {
                mStatusComplete.setChecked(true);
            } else if (status == TruckStatus.PARTIAL) {
                mStatusPartial.setChecked(true);
            } else {
                mStatusNeedsRepair.setChecked(false);
                mStatusComplete.setChecked(false);
                mStatusPartial.setChecked(false);
            }
        }
    }

    void checkErrors() {
        if (PrefHelper.getInstance().getDoErrorCheck()) {
            if (!CheckError.getInstance().checkEntryErrors(this, new CheckError.CheckErrorResult() {
                @Override
                public void doEdit() {
                    MainActivity.this.doEditEntry();
                }
            })) {
                PrefHelper.getInstance().setDoErrorCheck(false);
            }
        }
        CheckError.getInstance().checkProjectErrors();
    }
}
