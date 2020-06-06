/*
 * Copyright 2019, FleetTLC. All rights reserved
 */

package com.cartlc.tracker.fresh.ui.common

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.widget.ImageView
import com.cartlc.tracker.R
import java.lang.ref.WeakReference
import kotlin.math.roundToInt

class BitmapHelper {

    private data class BitmapKey(
            val filename: String,
            val size: Int
    )

    private class Cache {
        val cache = HashMap<BitmapKey,Bitmap>()
        val usedIn = HashMap<BitmapKey, ImageView>()

        fun contains(key: BitmapKey): Boolean {
            return cache.contains(key)
        }

        fun query(key: BitmapKey): Bitmap? {
            return cache[key]
        }

        fun clear() {
            for (key in cache.keys) {
                usedIn[key]?.setImageBitmap(null)
                cache[key]?.recycle()
            }
            cache.clear()
            usedIn.clear()
        }

        fun save(key: BitmapKey, bitmap: Bitmap) {
            cache[key] = bitmap
        }

        fun used(key: BitmapKey, imageView: ImageView) {
            usedIn[key] = imageView
        }
    }

    var cacheOkay = true
    private val cache = Cache()

    private class LoadTask(
            helper : BitmapHelper,
            imageView: ImageView,
            val filename: String,
            val dstHeight: Int,
            private val isSmall: Boolean,
            private val done: () -> Unit
    ) : AsyncTask<String, String, Bitmap>() {

        val helperRef = WeakReference(helper)
        val imageRef = WeakReference(imageView)
        val key = BitmapKey(filename, dstHeight)

        override fun onPreExecute() {
            imageRef.get()?.setImageResource(if (isSmall) R.drawable.loading_small else R.drawable.loading)
        }

        override fun doInBackground(vararg values: String?): Bitmap {
            val bitmap = loadBitmap(filename, dstHeight)
            helperRef.get()?.saveInCache(key, bitmap)
            return bitmap
        }

        override fun onPostExecute(result: Bitmap) {
            imageRef.get()?.let { imageView ->
                imageView.setImageBitmap(result)
                helperRef.get()?.usedInCache(key, imageView)
            }
            done()
        }

        private fun loadBitmap(pathname: String, dstHeight: Int): Bitmap {
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeFile(pathname, options)
            val origHeight = options.outHeight
            options.inJustDecodeBounds = false
            val sampleSize = 1f / (dstHeight.toFloat() / origHeight.toFloat())
            options.inSampleSize = sampleSize.roundToInt()
            return BitmapFactory.decodeFile(pathname, options)
        }
    }

    fun loadBitmap(pathname: String, dstHeight: Int, imageView: ImageView, isSmall: Boolean = false, done: () -> Unit = {}) {
        val key = BitmapKey(pathname, dstHeight)
        if (cache.contains(key)) {
            imageView.setImageBitmap(cache.query(key))
            done()
        } else {
            LoadTask(this, imageView, pathname, dstHeight, isSmall, done).execute()
        }
    }

    fun clearCache() {
        cache.clear()
    }

    private fun saveInCache(key: BitmapKey, bitmap: Bitmap) {
        if (cacheOkay) {
            cache.save(key, bitmap)
        }
    }

    private fun usedInCache(key: BitmapKey, imageView: ImageView) {
        if (cacheOkay) {
            cache.used(key, imageView)
        }
    }

}