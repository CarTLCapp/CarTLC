/**
 * Copyright 2019, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.ui.common

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.transition.Fade
import com.cartlc.tracker.R

class FragmentHelper(
        private val activity: FragmentActivity
) {

    fun bind(fragment: Fragment) {
        val previousFragment = activity.supportFragmentManager.findFragmentById(R.id.frame_fragment)
        if (previousFragment != fragment) {
            previousFragment?.exitTransition = Fade()
            fragment.enterTransition = Fade()
            activity.supportFragmentManager.beginTransaction()
                    .replace(R.id.frame_fragment, fragment)
                    .commit()
        }
    }

    fun clear() {
        activity.supportFragmentManager.findFragmentById(R.id.frame_fragment)?.let {
            it.exitTransition = Fade()
            activity.supportFragmentManager.beginTransaction()
                    .remove(it)
                    .commit()
        }
    }

}