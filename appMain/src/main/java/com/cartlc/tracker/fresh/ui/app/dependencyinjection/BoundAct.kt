package com.cartlc.tracker.fresh.ui.app.dependencyinjection

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.cartlc.tracker.fresh.ui.common.DialogNavigator
import com.cartlc.tracker.ui.app.TBApplication
import com.cartlc.tracker.ui.util.helper.DialogHelper

open class BoundAct(
        val act: FragmentActivity
) : BoundBase(
        (act.applicationContext as TBApplication).repo,
        (act.applicationContext as TBApplication).componentRoot) {

    override val lifecycleOwner: LifecycleOwner
        get() = act

    override val dialogHelper: DialogHelper
        get() = DialogHelper(act)

    override val dialogNavigator: DialogNavigator
        get() = DialogNavigator(act, componentRoot.messageHandler)

    open fun bindObserver(observer: LifecycleObserver): LifecycleObserver {
        (act as LifecycleOwner).lifecycle.addObserver(observer)
        return observer
    }

}