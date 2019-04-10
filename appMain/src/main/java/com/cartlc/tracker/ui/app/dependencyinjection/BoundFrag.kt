package com.cartlc.tracker.ui.app.dependencyinjection

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.cartlc.tracker.ui.app.TBApplication

class BoundFrag(
        private val frag: Fragment
) : BoundBase(
        (frag.activity!!.applicationContext as TBApplication).repo,
        (frag.activity!!.applicationContext as TBApplication).componentRoot) {

    val activity: FragmentActivity = frag.activity!!

    override val lifecycleOwner: LifecycleOwner
        get() = frag

    fun bindObserver(observer: LifecycleObserver): LifecycleObserver {
        (frag as LifecycleOwner).lifecycle.addObserver(observer)
        return observer
    }

}