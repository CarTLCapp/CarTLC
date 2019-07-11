package com.cartlc.tracker.fresh.ui.entrysimple

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.annotation.AttrRes
import com.cartlc.tracker.fresh.ui.base.BaseActivity
import com.cartlc.tracker.fresh.ui.app.TBApplication

class EntrySimpleView(
        context: Context, attrs: AttributeSet?,
        @AttrRes defStyleAttr: Int
) : FrameLayout(context, attrs, defStyleAttr) {

    constructor(context: Context) : this(context, null, 0)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    private val app = context.applicationContext as TBApplication
    private val act = context as BaseActivity
    private val boundAct = act.boundAct
    private val componentRoot = app.componentRoot
    private val factoryViewMvc = componentRoot.factoryViewMvc
    private val factoryController = componentRoot.factoryController

    val control: EntrySimpleUseCase

    init {
        val entryView = factoryViewMvc.allocEntrySimpleViewMvc(this)
        control = factoryController.allocEntrySimpleController(boundAct, entryView)
        addView(entryView.rootView)
    }

}