package com.cartlc.tracker.ui.app.dependencyinjection

import androidx.lifecycle.LifecycleOwner
import com.cartlc.tracker.model.CarRepository
import com.cartlc.tracker.ui.util.helper.DialogHelper

abstract class BoundBase(
        val repo: CarRepository,
        val componentRoot: ComponentRoot
) {

    abstract val lifecycleOwner: LifecycleOwner
    abstract val dialogHelper: DialogHelper
}