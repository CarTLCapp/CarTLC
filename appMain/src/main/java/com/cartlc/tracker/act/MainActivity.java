package com.cartlc.tracker.act;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.FileProvider;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.cartlc.tracker.R;
import com.cartlc.tracker.app.TBApplication;
import com.cartlc.tracker.data.DataEntry;
import com.cartlc.tracker.data.DataPicture;
import com.cartlc.tracker.data.DataPictureCollection;
import com.cartlc.tracker.data.DataProjectGroup;
import com.cartlc.tracker.data.DataStates;
import com.cartlc.tracker.data.PrefHelper;
import com.cartlc.tracker.data.TableAddress;
import com.cartlc.tracker.data.TableEntries;
import com.cartlc.tracker.data.TableEquipment;
import com.cartlc.tracker.data.TableNotes;
import com.cartlc.tracker.data.TablePendingPictures;
import com.cartlc.tracker.data.TableProjectGroups;
import com.cartlc.tracker.data.TableProjects;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;

    class DetectReturn implements TextWatcher {
        public DetectReturn() {
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    }

    class CountChars implements TextWatcher {

        public CountChars() {
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            int           numChars = s.toString().length();
            StringBuilder sbuf     = new StringBuilder();
            sbuf.append(numChars);
            sbuf.append("/");
            sbuf.append(mEntryMaxLength);
            mNumChars.setText(sbuf.toString());
        }
    }

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
    @BindView(R.id.entry_notes)        EditText             mEntryNotes;
    @BindView(R.id.frame_login)        ViewGroup            mLoginFrame;
    @BindView(R.id.frame_new_entry)    ViewGroup            mEntryFrame;
    @BindView(R.id.frame_new_notes)    ViewGroup            mNotesFrame;
    @BindView(R.id.list)               RecyclerView         mMainList;
    @BindView(R.id.list_container)     FrameLayout          mMainListFrame;
    @BindView(R.id.next)               Button               mNext;
    @BindView(R.id.prev)               Button               mPrev;
    @BindView(R.id.new_entry)          Button               mNew;
    @BindView(R.id.setup_title)        TextView             mTitle;
    @BindView(R.id.fab_add)            FloatingActionButton mAdd;
    @BindView(R.id.number_characters)  TextView             mNumChars;
    @BindView(R.id.frame_confirmation) FrameLayout          mConfirmationFrameView;
    @BindView(R.id.frame_pictures)     ViewGroup            mPictureFrame;
    @BindView(R.id.list_pictures)      RecyclerView         mPictureList;

    Stage  mCurStage = Stage.LOGIN;
    String mCurKey   = PrefHelper.KEY_STATE;
    boolean                    mCurStageEditing;
    SimpleListAdapter          mSimpleAdapter;
    ProjectListAdapter         mProjectAdapter;
    EquipmentSelectListAdapter mEquipmentAdapter;
    PictureListAdapter         mPictureAdapter;
    LinearLayoutManager        mLayoutManager;
    InputMethodManager         mInputMM;
    CountChars                 mEntryCountChars;
    int                        mEntryMaxLength;
    ConfirmationFrame          mConfirmationFrame;
    DataEntry                  mCurEntry;

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
        mMainList.addItemDecoration(new DividerItemDecoration(mMainList.getContext(), mLayoutManager.getOrientation()));
        mMainList.setLayoutManager(mLayoutManager);
        mSimpleAdapter = new SimpleListAdapter(this, new SimpleListAdapter.OnItemSelectedListener() {
            @Override
            public void onSelectedItem(int position, String text) {
                if (mCurKey != null) {
                    PrefHelper.getInstance().setString(mCurKey, text);
                }
            }
        });
        mProjectAdapter = new ProjectListAdapter(this);
        mEquipmentAdapter = new EquipmentSelectListAdapter(this);
        mPictureAdapter = new PictureListAdapter(this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        layoutManager.setAutoMeasureEnabled(true);
        mPictureList.setLayoutManager(layoutManager);
        mPictureList.setAdapter(mPictureAdapter);

        mEntryCountChars = new CountChars();
        mEntryNotes.addTextChangedListener(mEntryCountChars);

        try {
            mEntryMaxLength = getResources().getInteger(R.integer.entry_max_length);
        } catch (Exception ex) {
            Timber.e(ex);
        }
        mConfirmationFrame = new ConfirmationFrame(mConfirmationFrameView);
    }

    @Override
    public void onResume() {
        super.onResume();
        PrefHelper.getInstance().setupInit();
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
        } else if (mCurStage.ordinal() <= Stage.CURRENT_PROJECT.ordinal()) {
            mCurStage = Stage.CURRENT_PROJECT;
        }
    }

    String getEditText(EditText text) {
        return text.getText().toString().trim();
    }

    boolean save(boolean isNext) {
        if (mCurStage == Stage.LOGIN) {
            PrefHelper.getInstance().setFirstName(getEditText(mFirstName));
            PrefHelper.getInstance().setLastName(getEditText(mLastName));
        } else if (mCurStage == Stage.TRUCK_NUMBER) {
            String value = mEntrySimple.getText().toString();
            if (TextUtils.isDigitsOnly(value)) {
                PrefHelper.getInstance().setTruckNumber(Long.parseLong(value));
            } else {
                if (isNext) {
                    showError(getString(R.string.error_not_a_number, value));
                }
                return false;
            }
        } else if (mCurStageEditing) {
            if (mCurStage == Stage.CITY) {
                PrefHelper.getInstance().setCity(getEditText(mEntrySimple));
            } else if (mCurStage == Stage.STREET) {
                PrefHelper.getInstance().setStreet(getEditText(mEntrySimple));
            } else if (mCurStage == Stage.EQUIPMENT) {
                String name = getEditText(mEntrySimple);
                if (!TextUtils.isEmpty(name)) {
                    DataProjectGroup group = PrefHelper.getInstance().getCurrentProjectGroup();
                    TableEquipment.getInstance().addLocal(name, group.projectNameId);
                }
            }
        } else if (mCurStage == Stage.NOTES) {
            String value = getEditText(mEntryNotes);
            PrefHelper.getInstance().setNotes(value);

            if (isNext) {
                long id = TableNotes.getInstance().add(value);
                PrefHelper.getInstance().setLastNotesId(id);
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
        mCurStage = Stage.from(mCurStage.ordinal() - 1);
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
        mNotesFrame.setVisibility(View.GONE);
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

        switch (mCurStage) {
            case LOGIN:
                mLoginFrame.setVisibility(View.VISIBLE);
                mNext.setVisibility(View.VISIBLE);
                mTitle.setText(R.string.title_login);
                mFirstName.setText(PrefHelper.getInstance().getFirstName());
                mLastName.setText(PrefHelper.getInstance().getLastName());
                break;
            case PROJECT:
                mMainListFrame.setVisibility(View.VISIBLE);
                mNext.setVisibility(View.VISIBLE);
                setList(R.string.title_project, PrefHelper.KEY_PROJECT, TableProjects.getInstance().query());
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
                    String       company = PrefHelper.getInstance().getCompany();
                    List<String> states  = TableAddress.getInstance().queryStates(company);
                    if (states.size() > 0) {
                        PrefHelper.getInstance().addState(states);
                        setList(R.string.title_state, PrefHelper.KEY_STATE, states);
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
                    String       state  = PrefHelper.getInstance().getState();
                    List<String> cities = TableAddress.getInstance().queryCities(state);
                    if (cities.size() > 0) {
                        PrefHelper.getInstance().addCity(cities);
                        setList(R.string.title_city, PrefHelper.KEY_CITY, cities);
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
                } else {
                    mNew.setVisibility(View.VISIBLE);
                    mMainListFrame.setVisibility(View.VISIBLE);
                    List<String> locations = TableAddress.getInstance().queryStreets(
                            PrefHelper.getInstance().getCompany(),
                            PrefHelper.getInstance().getCity(),
                            PrefHelper.getInstance().getState());
                    if (locations.size() > 0) {
                        setList(R.string.title_street, PrefHelper.KEY_STREET, locations);
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
                if (!PrefHelper.getInstance().hasCurProject() || TableProjectGroups.getInstance().count() == 0) {
                    computeCurStage();
                    fillStage();
                } else {
                    mMainListFrame.setVisibility(View.VISIBLE);
                    mNew.setVisibility(View.VISIBLE);
                    mAdd.setVisibility(View.VISIBLE);
                    mCurKey = null;
                    mNew.setText(R.string.btn_new);
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
                mNotesFrame.setVisibility(View.VISIBLE);
                mTitle.setText(R.string.title_notes);
                mEntryNotes.setText(PrefHelper.getInstance().getNotes());
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
                    mNew.setVisibility(View.VISIBLE);
                    mTitle.setText(R.string.title_equipment_installed);
                    mMainList.setAdapter(mEquipmentAdapter);
                    mEquipmentAdapter.onDataChanged();
                    mMainListFrame.setVisibility(View.VISIBLE);
                }
                break;
            case PICTURE:
                mTitle.setText(R.string.title_picture);
                mPrev.setVisibility(View.VISIBLE);
                if (mCurStageEditing || TablePendingPictures.getInstance().count() == 0) {
                    mCurStageEditing = false;
                    if (!dispatchPictureRequest()) {
                        showError(getString(R.string.error_cannot_take_picture));
                    }
                } else {
                    mNext.setVisibility(View.VISIBLE);
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
        if (name.equals("Other")) {
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

    void setList(int textId, String key, List<String> list) {
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
            } else {
                int position = mSimpleAdapter.setSelected(curValue);
                if (position >= 0) {
                    mMainList.scrollToPosition(position);
                }
            }
        }
    }

    boolean dispatchPictureRequest() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            Uri pictureUri = TablePendingPictures.getInstance().genNewPictureUri(this);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, pictureUri);
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            return true;
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            // Everything needed will happen automatically in fillStage();
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
