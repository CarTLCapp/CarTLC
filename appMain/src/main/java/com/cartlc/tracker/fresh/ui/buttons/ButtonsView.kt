package com.cartlc.tracker.fresh.ui.buttons

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.annotation.AttrRes
import com.cartlc.tracker.fresh.ui.app.TBApplication
import com.cartlc.tracker.fresh.ui.app.dependencyinjection.ComponentRoot

class ButtonsView(
        context: Context,
        attrs: AttributeSet?,
        @AttrRes defStyleAttr: Int
) : FrameLayout(context, attrs, defStyleAttr) {

    constructor(context: Context) : this(context, null, 0)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    private val app = context.applicationContext as TBApplication
    private val componentRoot: ComponentRoot = app.componentRoot
    private val factoryViewMvc = componentRoot.factoryViewMvc

    val viewMvc: ButtonsViewMvc = factoryViewMvc.allocButtonsViewMvc(null)

    init {
        addView(viewMvc.rootView)
    }

}