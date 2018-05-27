/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.data;

import android.text.TextUtils;

import com.cartlc.tracker.util.BitmapHelper;

import java.io.File;

import timber.log.Timber;

/**
 * Created by dug on 5/16/17.
 */

public class DataPicture {

    static final int MAX_NOTE_LENGTH = 1000;

    public long    id;
    public String  unscaledFilename;
    public String  scaledFilename;
    public String  note;
    public boolean uploaded;

    File unscaledFile;
    File scaledFile;

    public DataPicture() {
    }

    public DataPicture(long id, String pictureFilename, String uploadingFilename, String note, boolean uploaded) {
        this.id = id;
        this.unscaledFilename = pictureFilename;
        this.scaledFilename = uploadingFilename;
        this.note = note;
        this.uploaded = uploaded;
    }

    public File getUnscaledFile() {
        if (unscaledFile == null) {
            unscaledFile = new File(unscaledFilename);
        }
        return unscaledFile;
    }

    public String getTailname() {
        int pos = unscaledFilename.lastIndexOf("/");
        if (pos >= 0) {
            return unscaledFilename.substring(pos + 1);
        }
        return unscaledFilename;
    }

    public boolean existsUnscaled() {
        return getUnscaledFile().exists();
    }

    public boolean existsScaled() {
        if (getScaledFile() != null) {
            return getScaledFile().exists();
        }
        return false;
    }

    public void remove() {
        getUnscaledFile().delete();
        if (getScaledFile() != null) {
            getScaledFile().delete();
        }
    }

    // Warning: will create the scaled file if it does not yet exist.
    public File getScaledFile() {
        if (scaledFilename == null) {
            scaledFilename = BitmapHelper.createScaled(getUnscaledFile());
            if (scaledFilename != null) {
                TablePictureCollection.getInstance().update(this, null);
            } else {
                Timber.e("Failed to create uploading version of " + getUnscaledFile().getAbsolutePath());
            }
        }
        if (scaledFile == null && scaledFilename != null) {
            scaledFile = new File(scaledFilename);
        }
        return scaledFile;
    }

    public void setNote(String note) {
        // Safety:
        if (note.length() > MAX_NOTE_LENGTH) {
            note = note.substring(0, MAX_NOTE_LENGTH);
        }
        this.note = note;
        TablePictureCollection.getInstance().update(this, null);
    }

    public int rotateCW() {
        BitmapHelper.rotate(getUnscaledFile(), 90);
        return 90;
    }

    public int rotateCCW() {
        BitmapHelper.rotate(getUnscaledFile(), -90);
        return -90;
    }

    public String toString() {
        StringBuilder sbuf = new StringBuilder();
        sbuf.append(id);
        if (unscaledFilename != null) {
            sbuf.append(", ");
            sbuf.append(unscaledFilename);
        }
        if (scaledFilename != null) {
            sbuf.append(", ");
            sbuf.append(scaledFilename);
        }
        if (!TextUtils.isEmpty(note)) {
            sbuf.append(", note=");
            sbuf.append(note);
        }
        if (uploaded) {
            sbuf.append(", UPLOADED");
        }
        return sbuf.toString();
    }
}
