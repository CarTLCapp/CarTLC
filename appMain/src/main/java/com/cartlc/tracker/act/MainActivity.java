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
import android.text.InputType;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.cartlc.tracker.R;
import com.cartlc.tracker.app.TBApplication;
import com.cartlc.tracker.data.DataEntry;
import com.cartlc.tracker.data.DataProjectAddressCombo;
import com.cartlc.tracker.data.DataStates;
import com.cartlc.tracker.data.PrefHelper;
import com.cartlc.tracker.data.TableAddress;
import com.cartlc.tracker.data.TableEntries;
import com.cartlc.tracker.data.TableEquipment;
import com.cartlc.tracker.data.TableEquipmentProjectCollection;
import com.cartlc.tracker.data.TablePendingPictures;
import com.cartlc.tracker.data.TableProjectAddressCombo;
import com.cartlc.tracker.data.TableProjects;
import com.cartlc.tracker.server.ServerHelper;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;

    static final int AUTO_RETURN_DELAY_MS = 100;
    static final int MSG_AUTO_RETURN      = 0;

    class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_AUTO_RETURN:
                    doNext();
                    break;
            }
        }
    }

    enum Stage {
        LOGIN,
        PROJECT,
        COMPANY,
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

    @BindView(R.id.first_name)         EditText             mFirstName;
    @BindView(R.id.last_name)          EditText             mLastName;
    @BindView(R.id.entry_simple)       EditText             mEntrySimple;
    @BindView(R.id.frame_login)        ViewGroup            mLoginFrame;
    @BindView(R.id.frame_new_entry)    ViewGroup            mEntryFrame;
    @BindView(R.id.list)               RecyclerView         mMainList;
    @BindView(R.id.list_container)     FrameLayout          mMainListFrame;
    @BindView(R.id.next)               Button               mNext;
    @BindView(R.id.prev)               Button               mPrev;
    @BindView(R.id.new_entry)          Button               mNew;
    @BindView(R.id.setup_title)        TextView             mTitle;
    @BindView(R.id.fab_add)            FloatingActionButton mAdd;
    @BindView(R.id.frame_confirmation) FrameLayout          mConfirmationFrameView;
    @BindView(R.id.frame_pictures)     ViewGroup            mPictureFrame;
    @BindView(R.id.list_pictures)      RecyclerView         mPictureList;
    @BindView(R.id.empty)              TextView             mEmptyView;

    Stage     mCurStage = Stage.LOGIN;
    String    mCurKey   = PrefHelper.KEY_STATE;
    MyHandler mHandler  = new MyHandler();
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
        LinearLayoutManager linearLayoutManager;
        mMainList.setLayoutManager(linearLayoutManager = new LinearLayoutManager(this));
        mMainList.addItemDecoration(new DividerItemDecoration(mMainList.getContext(), linearLayoutManager.getOrientation()));
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
        mLastName.setOnEditorActionListener(mAutoNext);
        mEntrySimple.setOnEditorActionListener(mAutoNext);
        PrefHelper.getInstance().setupFromCurrentProjectId();
        computeCurStage();
        fillStage();
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
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (ServerHelper.getInstance().hasConnection()) {
            mApp.flushEvents();
        }
    }

    void computeCurStage() {
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
            boolean hasTruckNumber = !TextUtils.isEmpty(getTruckNumber());
            boolean hasNotes = mNoteAdapter.hasNotesEntered();
            boolean hasEquip = mEquipmentAdapter.hasChecked();;
            boolean hasPictures = TablePendingPictures.getInstance().queryPictures().size() > 0;

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
        if (mCurStage == Stage.LOGIN) {
            String firstName = getEditText(mFirstName);
            String lastName = getEditText(mLastName);
            PrefHelper.getInstance().setFirstName(firstName);
            PrefHelper.getInstance().setLastName(lastName);
            if (TextUtils.isEmpty(firstName) || TextUtils.isEmpty(lastName)) {
                showError(getString(R.string.error_enter_your_name));
                return false;
            }
            mApp.flushEvents();
        } else if (mCurStage == Stage.TRUCK_NUMBER) {
            String value = mEntrySimple.getText().toString();
            if (!TextUtils.isEmpty(value) && TextUtils.isDigitsOnly(value)) {
                PrefHelper.getInstance().setTruckNumber(Long.parseLong(value));
            } else {
                if (isNext) {
                    if (TextUtils.isEmpty(value)) {
                        showError(getString(R.string.error_need_a_number));
                    } else {
                        showError(getString(R.string.error_not_a_number, value));
                    }
                }
                return false;
            }
        } else if (mCurStage == Stage.EQUIPMENT) {
            if (isNext) {
                if (TableEquipment.getInstance().countChecked() == 0) {
                    showError(getString(R.string.error_need_equipment));
                    return false;
                }
            }
        } else if (mCurStage == Stage.NOTES) {
            if (isNext) {
//                if (!mNoteAdapter.hasEnoughValues()) {
//                    StringBuilder sbuf = new StringBuilder();
//                    sbuf.append(getString(R.string.error_need_note_fields));
//                    sbuf.append("\n");
//                    sbuf.append(mNoteAdapter.getEmptyFields());
//                    showError(sbuf.toString());
//                    return false;
//                }
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
                    TableEquipmentProjectCollection.getInstance().addLocal(name, group.projectNameId);
                }
            }
        }
        mCurStageEditing = false;
        return true;
    }

    void doNext() {
        if (save(true)) {
            mCurStage = Stage.from(mCurStage.ordinal() + 1);
            fillStage();
        }
    }

    void doPrev() {
        save(false);
        if (mCurStage == Stage.PROJECT) {
            PrefHelper.getInstance().recoverProject();
            mCurStage = Stage.CURRENT_PROJECT;
        } else {
            mCurStage = Stage.from(mCurStage.ordinal() - 1);
        }
        fillStage();
    }

    void doNewEntry() {
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
        mNew.setVisibility(View.INVISIBLE);
        mNew.setText(R.string.btn_add);
        mPrev.setText(R.string.btn_prev);
        mEntrySimple.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        mConfirmationFrame.setVisibility(View.GONE);
        mPictureFrame.setVisibility(View.GONE);
        mPictureList.setVisibility(View.GONE);
        mEmptyView.setVisibility(View.GONE);
        mMainList.setVisibility(View.VISIBLE);

        switch (mCurStage) {
            case LOGIN:
                mLoginFrame.setVisibility(View.VISIBLE);
                mNext.setVisibility(View.VISIBLE);
                mTitle.setText(R.string.title_login);
                mFirstName.setText(PrefHelper.getInstance().getFirstName());
                mLastName.setText(PrefHelper.getInstance().getLastName());
                break;
            case PROJECT:
                if (mCurStageEditing) {
                    showError("No projects!");
                } else {
                    if (TableProjectAddressCombo.getInstance().count() > 0) {
                        mPrev.setVisibility(View.VISIBLE);
                    }
                    mMainListFrame.setVisibility(View.VISIBLE);

                    if (PrefHelper.getInstance().getProjectName() != null) {
                        mNext.setVisibility(View.VISIBLE);
                    }
                    setList(R.string.title_project, PrefHelper.KEY_PROJECT, TableProjects.getInstance().query());
                }
                break;
            case COMPANY:
                mMainListFrame.setVisibility(View.VISIBLE);
                mPrev.setVisibility(View.VISIBLE);
                mNext.setVisibility(View.VISIBLE);
                List<String> companies = TableAddress.getInstance().queryCompanies();
                PrefHelper.getInstance().addCompany(companies);
                setList(R.string.title_company, PrefHelper.KEY_COMPANY, companies);
                break;
            case STATE:
                mMainListFrame.setVisibility(View.VISIBLE);
                mNext.setVisibility(View.VISIBLE);
                mPrev.setVisibility(View.VISIBLE);
                if (mCurStageEditing) {
                    List<String> states = DataStates.getUnusedStates();
                    PrefHelper.getInstance().setState(null);
                    setList(R.string.title_state, PrefHelper.KEY_STATE, states);
                } else {
                    String company = PrefHelper.getInstance().getCompany();
                    List<String> states = TableAddress.getInstance().queryStates(company);
                    if (states.size() > 0) {
                        PrefHelper.getInstance().addState(states);
                        if (!setList(R.string.title_state, PrefHelper.KEY_STATE, states)) {
                            mNext.setVisibility(View.INVISIBLE);
                        }
                        mNew.setVisibility(View.VISIBLE);
                    } else {
                        mCurStageEditing = true;
                        fillStage();
                    }
                }
                break;
            case CITY:
                mNext.setVisibility(View.VISIBLE);
                mPrev.setVisibility(View.VISIBLE);
                if (mCurStageEditing) {
                    mEntryFrame.setVisibility(View.VISIBLE);
                    mTitle.setText(R.string.title_city);
                    mEntrySimple.setHint(R.string.title_city);
                    mEntrySimple.setText("");
                } else {
                    mMainListFrame.setVisibility(View.VISIBLE);
                    mNew.setVisibility(View.VISIBLE);
                    String state = PrefHelper.getInstance().getState();
                    List<String> cities = TableAddress.getInstance().queryCities(state);
                    if (cities.size() > 0) {
                        PrefHelper.getInstance().addCity(cities);
                        if (!setList(R.string.title_city, PrefHelper.KEY_CITY, cities)) {
                            mNext.setVisibility(View.INVISIBLE);
                        }
                    } else {
                        mCurStageEditing = true;
                        fillStage();
                    }
                }
                break;
            case STREET:
                mNext.setVisibility(View.VISIBLE);
                mPrev.setVisibility(View.VISIBLE);
                if (mCurStageEditing) {
                    mTitle.setText(R.string.title_street);
                    mEntryFrame.setVisibility(View.VISIBLE);
                    mEntrySimple.setHint(R.string.title_street);
                    mEntrySimple.setText("");
                    mEntrySimple.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_POSTAL_ADDRESS);
                } else {
                    mNew.setVisibility(View.VISIBLE);
                    mMainListFrame.setVisibility(View.VISIBLE);
                    List<String> locations = TableAddress.getInstance().queryStreets(
                            PrefHelper.getInstance().getCompany(),
                            PrefHelper.getInstance().getCity(),
                            PrefHelper.getInstance().getState());
                    if (locations.size() > 0) {
                        if (!setList(R.string.title_street, PrefHelper.KEY_STREET, locations)) {
                            mNext.setVisibility(View.INVISIBLE);
                        }
                    } else {
                        mCurStageEditing = true;
                        fillStage();
                    }
                }
                break;
            case CURRENT_PROJECT:
                PrefHelper.getInstance().saveNewProjectIfNeeded();
                if (mCurStageEditing) {
                    PrefHelper.getInstance().clearCurProject();
                    mCurStageEditing = false;
                }
                if (!PrefHelper.getInstance().hasCurProject() || TableProjectAddressCombo.getInstance().count() == 0) {
                    computeCurStage();
                    fillStage();
                } else {
                    mMainListFrame.setVisibility(View.VISIBLE);
                    mNew.setVisibility(View.VISIBLE);
                    mAdd.setVisibility(View.VISIBLE);
                    mCurKey = null;
                    mNew.setText(R.string.btn_new_project);
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
                mEntrySimple.setText(getTruckNumber());
                mEntrySimple.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_CLASS_NUMBER);
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
                        mNew.setVisibility(View.VISIBLE);
                    }
                    mTitle.setText(R.string.title_equipment_installed);
                    mMainList.setAdapter(mEquipmentAdapter);
                    mEquipmentAdapter.onDataChanged();
                    mMainListFrame.setVisibility(View.VISIBLE);
                }
                break;
            case PICTURE:
                int numberTaken = TablePendingPictures.getInstance().count();
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
                    mNew.setVisibility(View.VISIBLE);
                    mNew.setText(R.string.btn_another);
                    mPictureFrame.setVisibility(View.VISIBLE);
                    mPictureList.setVisibility(View.VISIBLE);
                    mPictureAdapter.setList(TablePendingPictures.getInstance().queryPictures());
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
                TableEntries.getInstance().add(mCurEntry);
                PrefHelper.getInstance().clearLastEntry();
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

    String getTruckNumber() {
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
            doNewEntry();
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
            if (mCurStage == Stage.PROJECT || mCurStage == Stage.CITY || mCurStage == Stage.STATE || mCurStage == Stage.STREET) {
                mNext.setVisibility(View.VISIBLE);
            }
        }
    }


    boolean dispatchPictureRequest() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            Uri pictureUri = TablePendingPictures.getInstance().genNewPictureUri(this);
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
}
