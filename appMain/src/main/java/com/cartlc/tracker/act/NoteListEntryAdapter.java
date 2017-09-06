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
import com.cartlc.tracker.data.DataEntry;
import com.cartlc.tracker.data.DataNote;
import com.cartlc.tracker.data.DataProjectAddressCombo;
import com.cartlc.tracker.data.TableEntry;
import com.cartlc.tracker.etc.PrefHelper;
import com.cartlc.tracker.data.TableNote;
import com.cartlc.tracker.data.TableCollectionNoteProject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

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

    public interface EntryListener {
        void textEntered(DataNote note);

        void textFocused(DataNote note);
    }

    final protected Context        mContext;
    final protected EntryListener  mListener;
    protected       List<DataNote> mItems;

    public NoteListEntryAdapter(Context context, EntryListener listener) {
        mContext = context;
        mListener = listener;
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
                if (holder.label.isSelected()) {
                    item.value = s.toString();
                    TableNote.getInstance().updateValue(item);
                    mListener.textEntered(item);
                }
            }
        });
        holder.entry.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    mListener.textFocused(item);
                    holder.label.setSelected(true);
                } else {
                    holder.label.setSelected(false);
                }
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
        DataEntry entry = PrefHelper.getInstance().getCurrentEditEntry();
        if (entry != null) {
            mItems = entry.getNotesAllWithValuesOverlaid();
        } else {
            DataProjectAddressCombo curGroup = PrefHelper.getInstance().getCurrentProjectGroup();
            if (curGroup != null) {
                mItems = TableCollectionNoteProject.getInstance().getNotes(curGroup.projectNameId);
            } else {
                mItems = new ArrayList<>();
            }
        }
        pushToBottom("Other");
        notifyDataSetChanged();

    }

    void pushToBottom(String name) {
        List<DataNote> others = new ArrayList<>();
        for (DataNote item : mItems) {
            if (item.name.startsWith(name)) {
                others.add(item);
                break;
            }
        }
        for (DataNote item : others) {
            mItems.remove(item);
            mItems.add(item);
        }
    }

    public boolean hasNotesEntered() {
        if (mItems != null) {
            for (DataNote note : mItems) {
                if (!TextUtils.isEmpty(note.value)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isNotesComplete() {
        if (mItems != null) {
            for (DataNote note : mItems) {
                if (!TextUtils.isEmpty(note.value)) {
                    if (note.num_digits > 0 && (note.value.length() != note.num_digits)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public List<DataNote> getNotes() {
        return mItems;
    }
}
