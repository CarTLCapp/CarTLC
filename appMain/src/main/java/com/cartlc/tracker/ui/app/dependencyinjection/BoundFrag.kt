package com.cartlc.tracker.ui.app.dependencyinjection

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.cartlc.tracker.ui.app.TBApplication
import com.cartlc.tracker.ui.util.helper.DialogHelper

class BoundFrag(
        private val frag: Fragment
) : BoundBase(
        (frag.activity!!.applicationContext as TBApplication).repo,
        (frag.activity!!.applicationContext as TBApplication).componentRoot) {

    val activity: FragmentActivity = frag.activity!!

    override val lifecycleOwner: LifecycleOwner
        get() = frag

    override val dialogHelper: DialogHelper
        get() = DialogHelper(activity)

    fun bindObserver(observer: LifecycleObserver): LifecycleObserver {
        (frag as LifecycleOwner).lifecycle.addObserver(observer)
        return observer
    }



}