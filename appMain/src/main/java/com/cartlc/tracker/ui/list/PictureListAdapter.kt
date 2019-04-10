/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.ui.list

import java.io.File
import java.util.ArrayList
import java.util.HashMap

import com.cartlc.tracker.R
import com.cartlc.tracker.ui.app.TBApplication
import com.cartlc.tracker.model.data.DataPicture
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Picasso

import android.app.AlertDialog
import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Message
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.entry_item_picture.view.*

import timber.log.Timber

/**
 * Created by dug on 5/10/17.
 */
open class PictureListAdapter(
        protected val mContext: Context,
        protected val mListener: (Int) -> Unit?
) : RecyclerView.Adapter<PictureListAdapter.CustomViewHolder>() {
    protected val mLayoutInflater: LayoutInflater
    private var mRotation: HashMap<String, Int> = HashMap()
    protected var mItems: MutableList<DataPicture> = ArrayList()
    protected val mDecHeight: Int by lazy {
        mContext.resources.getDimension(R.dimen.image_dec_size).toInt()
    }
    protected var mHandler = MyHandler()

    protected open val itemLayout: Int
        get() = R.layout.entry_item_picture

    protected open val maxHeightResource: Int
        get() = R.dimen.image_full_max_height

    private var maxHeight: Int = 0
        get() {
            if (field == 0) {
                field = mContext.resources.getDimension(maxHeightResource).toInt()
            }
            return field
        }

    val commonRotation: Int
        get() {
            var commonRotation = 0
            for (picture in mItems) {
                val path = picture.unscaledFile.absolutePath
                if (!mRotation.containsKey(path)) {
                    return 0
                }
                val rotation = mRotation[path]
                if (commonRotation == 0 && rotation != null) {
                    commonRotation = rotation
                } else if (commonRotation != rotation) {
                    return 0
                }
            }
            return commonRotation
        }

    inner class MyHandler : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MSG_DECREASE_SIZE -> {
                    var height = maxHeight
                    height -= mDecHeight
                    if (height < mDecHeight) {
                        height = mDecHeight
                    }
                    maxHeight = height
                    notifyDataSetChanged()
                }
                MSG_REMOVE_ITEM -> if (msg.obj is DataPicture) {
                    val item = msg.obj as DataPicture
                    item.remove()
                    mItems.remove(item)
                    notifyDataSetChanged()
                }
            }
        }
    }

    inner class CustomViewHolder(val view: View) : RecyclerView.ViewHolder(view) {

        fun bind(item: DataPicture) {
            with(view) {
                Picasso.get().cancelRequest(picture!!)
                val builder = Picasso.Builder(mContext)
                builder.listener { _, uri, exception ->
                    val sbuf = StringBuilder()
                    sbuf.append("While processing: ")
                    sbuf.append(uri.toString())
                    sbuf.append("\n")
                    sbuf.append(exception.message)
                    Timber.e(sbuf.toString())

                    loading?.setText(R.string.error_while_loading_picture)
                    picture?.setImageResource(android.R.color.transparent)

                    // TODO: Need to get rid of adjustViewBounds since I am getting way too many errors!
                    mHandler.sendEmptyMessageDelayed(MSG_DECREASE_SIZE, DELAY_DECREASE_SIZE.toLong())
                }
                val pictureFile: File?
                when {
                    item.existsUnscaled -> pictureFile = item.unscaledFile
                    item.existsScaled -> pictureFile = item.scaledFile
                    else -> pictureFile = null
                }
                if (pictureFile == null || !pictureFile.exists()) {
                    val msg = Message()
                    msg.what = MSG_REMOVE_ITEM
                    msg.obj = item
                    mHandler.sendMessageDelayed(msg, DELAY_REMOVE_ITEM.toLong())
                    picture!!.setImageResource(android.R.color.transparent)
                    loading!!.setText(R.string.error_picture_removed)
                } else {
                    builder.build()
                            .load(getUri(pictureFile))
                            .placeholder(R.drawable.loading)
                            .centerInside()
                            .resize(0, maxHeight)
                            .into(picture)
                    loading!!.visibility = View.GONE
                    remove?.let { view ->
                        view.setOnClickListener { _ ->
                            item.remove()
                            mItems.remove(item)
                            notifyDataSetChanged()
                            mListener.invoke(mItems.size)
                        }
                    }
                    rotate_cw?.let { view ->
                        view.setOnClickListener { _ ->
                            incRotation(item, item.rotateCW())
                            notifyDataSetChanged()
                        }
                    }
                    rotate_ccw?.let { view ->
                        view.setOnClickListener { _ ->
                            incRotation(item, item.rotateCCW())
                            notifyDataSetChanged()
                        }
                    }
                    note_dialog?.let { view ->
                        view.setOnClickListener { _ -> showPictureNoteDialog(item) }
                    }
                    note?.let { view ->
                        if (TextUtils.isEmpty(item.note)) {
                            view.visibility = View.INVISIBLE
                        } else {
                            view.visibility = View.VISIBLE
                            view.text = item.note
                        }
                    }
                }
            }

        }
    }

    init {
        mLayoutInflater = LayoutInflater.from(mContext)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val view = mLayoutInflater.inflate(itemLayout, parent, false)
        return CustomViewHolder(view)
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        holder.bind(mItems[position])
    }

    fun getUri(file: File): Uri {
        return TBApplication.getUri(mContext, file)
    }

    override fun getItemCount(): Int {
        return mItems.size
    }

    fun setList(list: List<DataPicture>) {
        mItems = list.toMutableList()
        maxHeight = 0
        notifyDataSetChanged()
    }

    internal fun showPictureNoteDialog(item: DataPicture) {
        val builder = AlertDialog.Builder(mContext)
        val noteView = mLayoutInflater.inflate(R.layout.picture_note, null)
        builder.setView(noteView)

        val edt = noteView.findViewById<View>(R.id.note) as EditText
        edt.setText(item.note)

        builder.setTitle(R.string.picture_note_title)
        builder.setPositiveButton("Done") { dialog, _ ->
            item.note = edt.text.toString().trim { it <= ' ' }
            dialog.dismiss()
            notifyDataSetChanged()
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
        val b = builder.create()
        b.show()
    }

    internal fun incRotation(item: DataPicture, degrees: Int) {
        val file = item.unscaledFile
        val path = file.absolutePath
        if (mRotation.containsKey(path)) {
            mRotation[path] = (mRotation[path]!! + degrees) % 360
        } else {
            mRotation[path] = degrees
        }
    }

    fun hadSomeRotations(): Boolean {
        for (key in mRotation.keys) {
            if (mRotation[key] != 0) {
                return true
            }
        }
        return false
    }

    companion object {

        internal val MSG_DECREASE_SIZE = 0
        internal val MSG_REMOVE_ITEM = 1

        internal val DELAY_DECREASE_SIZE = 100
        internal val DELAY_REMOVE_ITEM = 100
    }
}
