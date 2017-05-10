package com.cartlc.trackbattery.act;

import android.os.Bundle;
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
import com.cartlc.trackbattery.data.TableCity;
import com.cartlc.trackbattery.data.TableProjects;
import com.cartlc.trackbattery.data.TableState;
import com.cartlc.trackbattery.view.NothingSelectedSpinnerAdapter;

import java.util.ArrayList;
import java.util.List;

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
    NothingSelectedSpinnerAdapter mNothingSelectedAdapter;
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
        mNothingSelectedAdapter = new NothingSelectedSpinnerAdapter(this, mSpinnerAdapter);
        mSpinner.setAdapter(mNothingSelectedAdapter);
        mSpinner.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedItem(position);
            }
        });
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
                setSpinner(R.string.title_project, PrefHelper.KEY_PROJECT, TableProjects.getInstance().query());
                break;
            case STATE:
//                setSpinner(R.string.title_state, PrefHelper.KEY_STATE, TableState.getInstance().query(PrefHelper.getInstance().getState()));
                break;
            case CITY:
                setSpinner(R.string.title_city, PrefHelper.KEY_CITY, TableCity.getInstance().query());
                break;
        }
    }

    void setSpinner(int textId, String key, List<String> list) {
        String text = getString(textId);
        mTitle.setText(text);
        mSpinner.setPrompt(text);
        mNothingSelectedAdapter.setNothingSelectedText(text);
        mSpinnerAdapter.clear();
        mSpinnerAdapter.addAll(list);
        mSpinner.performClick();

        String curValue = PrefHelper.getInstance().getString(key, null);
        if (curValue == null)
        {
            mSpinner.setSelection(0);
        } else {
            int position = mSpinnerAdapter.getPosition(curValue);
            mSpinner.setSelection(position);
        }
    }

    void selectedItem(int position)
    {
        mSpinnerAdapter.getItem(position);
    }
}
