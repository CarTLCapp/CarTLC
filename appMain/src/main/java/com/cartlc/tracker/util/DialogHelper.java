package com.cartlc.tracker.util;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.cartlc.tracker.R;
import com.cartlc.tracker.act.MainActivity;

/**
 * Created by dug on 12/2/17.
 */

public class DialogHelper {

    public interface DialogListener {
        void onOkay();
        void onCancel();
    }

    AlertDialog mDialog;
    Activity    mAct;

    public DialogHelper(Activity act) {
        mAct = act;
    }

    public void clearDialog() {
        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }
    }

    public void showError(String message, final DialogListener listener) {
        clearDialog();
        AlertDialog.Builder builder = new AlertDialog.Builder(mAct);
        builder.setTitle(R.string.title_error);
        builder.setMessage(message);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                clearDialog();
                if (listener != null) {
                    listener.onOkay();
                }
            }
        });
        mDialog = builder.create();
        mDialog.show();
    }

    public void showServerError(String message, final DialogListener listener) {
        clearDialog();
        AlertDialog.Builder builder = new AlertDialog.Builder(mAct);
        builder.setTitle(R.string.title_error);
        builder.setMessage(message);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                clearDialog();
            }
        });
        builder.setNegativeButton(R.string.btn_stop, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                clearDialog();
                listener.onCancel();
            }
        });
        mDialog = builder.create();
        mDialog.show();
    }

    class ConfirmDialogData {
        View     mView;
        CheckBox [] mQuestions = new CheckBox[7];

        ConfirmDialogData() {
            mView = mAct.getLayoutInflater().inflate(R.layout.confirm_dialog, null);
            setup(0, R.id.question_1);
            setup(1, R.id.question_2);
            setup(2, R.id.question_3);
            setup(3, R.id.question_4);
            setup(4, R.id.question_5);
            setup(5, R.id.question_6);
            setup(6, R.id.question_7);
        }

        View getView() { return mView; }

        void setup(int index, int resId) {
            mQuestions[index] = (CheckBox) mView.findViewById(resId);
            mQuestions[index].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    mDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(confirmAllDone());
                }
            });
        }

        boolean confirmAllDone() {
            for (int i = 0; i < mQuestions.length; i++) {
                if (!mQuestions[i].isChecked()) {
                    return false;
                }
            }
            return true;
        }
    }

    public void showConfirmDialog(final DialogListener listener) {
        clearDialog();
        final ConfirmDialogData custom = new ConfirmDialogData();
        AlertDialog.Builder builder = new AlertDialog.Builder(mAct);
        builder.setTitle(R.string.confirm_title);
        builder.setView(custom.getView());
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                clearDialog();
                listener.onOkay();
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                clearDialog();
                listener.onCancel();
            }
        });
        mDialog = builder.create();
        mDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                mDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
            }
        });
        mDialog.show();
    }

}
