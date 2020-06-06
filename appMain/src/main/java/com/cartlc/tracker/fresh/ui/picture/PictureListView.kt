/**
 * Copyright 2019, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.ui.picture

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.annotation.AttrRes
import com.cartlc.tracker.fresh.ui.app.dependencyinjection.BoundAct
import com.cartlc.tracker.fresh.ui.app.TBApplication
import com.cartlc.tracker.fresh.ui.base.BaseActivity
import com.cartlc.tracker.fresh.ui.base.BaseFragment

class PictureListView(
        context: Context, attrs: AttributeSet?,
        @AttrRes defStyleAttr: Int
) : FrameLayout(context, attrs, defStyleAttr) {

    constructor(context: Context) : this(context, null, 0)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    private val app = context.applicationContext as TBApplication
    private val componentRoot = app.componentRoot
    private val factoryViewMvc = componentRoot.factoryViewMvc
    private val factoryController = componentRoot.factoryController
    private val boundAct: BoundAct

    init {
        when (context) {
            is BaseActivity -> boundAct = context.boundAct
            is BaseFragment -> boundAct = context.boundFrag
            else -> boundAct = (context as BaseActivity).boundAct // Intentional crash
        }
    }

    val control: PictureListController

    init {
        val viewMvc = factoryViewMvc.allocPictureListViewMvc(this)
        control = factoryController.allocPictureListController(boundAct, viewMvc)
        addView(viewMvc.rootView)
    }

}