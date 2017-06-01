package com.cartlc.tracker.act;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.cartlc.tracker.R;
import com.cartlc.tracker.app.TBApplication;
import com.cartlc.tracker.data.DataNote;
import com.cartlc.tracker.data.DataProjectAddressCombo;
import com.cartlc.tracker.data.PrefHelper;
import com.cartlc.tracker.data.TableNote;
import com.cartlc.tracker.data.TableCollectionNoteProject;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by dug on 5/12/17.
 */

public class NoteListEntryAdapter extends RecyclerView.Adapter<NoteListEntryAdapter.CustomViewHolder> {

    protected class CustomViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.label) TextView label;
        @BindView(R.id.entry) EditText entry;

        public CustomViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    final protected Context        mContext;
    protected       List<DataNote> mItems;

    public NoteListEntryAdapter(Context context) {
        mContext = context;
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.entry_item_entry_note, parent, false);
        return new CustomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final CustomViewHolder holder, final int position) {
        final DataNote item = mItems.get(position);
        holder.label.setText(item.name);
        holder.entry.setText(item.value);
        holder.entry.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                item.value = s.toString();
                TableNote.getInstance().updateValue(item);
            }
        });
        if (item.type == DataNote.Type.ALPHANUMERIC) {
            holder.entry.setInputType(InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
            holder.entry.setMaxLines(1);
        } else if (item.type == DataNote.Type.NUMERIC_WITH_SPACES) {
            holder.entry.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_CLASS_NUMBER);
            holder.entry.setMaxLines(1);
        } else if (item.type == DataNote.Type.NUMERIC) {
            holder.entry.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_CLASS_NUMBER);
            holder.entry.setMaxLines(1);
        } else if (item.type == DataNote.Type.MULTILINE) {
            holder.entry.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
            holder.entry.setMaxLines(3);
            holder.entry.setLines(3);
        } else {
            holder.entry.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
            holder.entry.setMaxLines(1);
        }
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public void onDataChanged() {
        DataProjectAddressCombo curGroup = PrefHelper.getInstance().getCurrentProjectGroup();
        if (curGroup != null) {
            mItems = TableCollectionNoteProject.getInstance().getNotes(curGroup.projectNameId);
            notifyDataSetChanged();
        }
    }

    public boolean hasNotesEntered() {
        DataProjectAddressCombo curGroup = PrefHelper.getInstance().getCurrentProjectGroup();
        if (curGroup != null) {
            for (DataNote note : TableCollectionNoteProject.getInstance().getNotes(curGroup.projectNameId)) {
                if (!TextUtils.isEmpty(note.value)) {
                    return true;
                }
            }
        }
        return false;
    }
}
