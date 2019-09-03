/**
 * Copyright 2019, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.ui.common

import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.transition.Fade
import timber.log.Timber

class FragmentHelper(
        private val activity: FragmentActivity
) {

    fun bind(@IdRes frame: Int, fragment: Fragment) {
        val previousFragment = activity.supportFragmentManager.findFragmentById(frame)
        if (previousFragment != fragment) {
            previousFragment?.exitTransition = Fade()
            fragment.enterTransition = Fade()
            activity.supportFragmentManager.beginTransaction()
                    .replace(frame, fragment)
                    .commit()
        }
    }

    fun clear(@IdRes frame: Int) {
        activity.supportFragmentManager.findFragmentById(frame)?.let {
            it.exitTransition = Fade()
            activity.supportFragmentManager.beginTransaction()
                    .remove(it)
                    .commit()
        }
    }

}