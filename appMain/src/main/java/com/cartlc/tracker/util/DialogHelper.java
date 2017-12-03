package com.cartlc.tracker.util;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

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

    public void showError(String message) {
        showError(message, null);
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

}
