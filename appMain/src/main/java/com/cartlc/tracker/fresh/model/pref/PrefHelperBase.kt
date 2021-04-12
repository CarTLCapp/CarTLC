/**
 * Copyright 2017-2021, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.model.pref

import android.content.Context
import android.content.SharedPreferences

/**
 * Created by dug on 4/14/17.
 */

open class PrefHelperBase constructor(protected val ctx: Context) {

    protected val prefs: SharedPreferences by lazy {
        ctx.getSharedPreferences(prefFile, 0)
    }

    private val prefFile: String
        get() = ctx.packageName + "_preferences"

    fun getString(key: String, defaultValue: String?): String? {
        return prefs.getString(key, defaultValue)
    }

    fun setString(key: String, value: String?) {
        val editor = prefs.edit()
        editor.putString(key, value)
        editor.apply()
    }

    fun getLong(key: String, defaultValue: Long): Long {
        return prefs.getLong(key, defaultValue)
    }

    fun setLong(key: String, value: Long) {
        val editor = prefs.edit()
        editor.putLong(key, value)
        editor.apply()
    }

    fun getInt(key: String, defaultValue: Int): Int {
        return prefs.getInt(key, defaultValue)
    }

    fun setInt(key: String, value: Int) {
        val editor = prefs.edit()
        editor.putInt(key, value)
        editor.apply()
    }

    open fun clearAll() {
        val editor = prefs.edit()
        editor.clear()
        editor.commit()
    }

}
