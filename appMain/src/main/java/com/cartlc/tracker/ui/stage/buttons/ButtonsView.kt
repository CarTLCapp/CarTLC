package com.cartlc.tracker.ui.stage.buttons

import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.AttrRes
import com.cartlc.tracker.ui.app.FactoryController
import com.cartlc.tracker.ui.app.FactoryViewMvc
import com.cartlc.tracker.ui.app.TBApplication
import com.cartlc.tracker.ui.app.dependencyinjection.ComponentRoot
import com.cartlc.tracker.ui.base.BaseActivity
import com.cartlc.tracker.ui.base.BaseFragment
import com.cartlc.tracker.ui.bits.SoftKeyboardDetect
import com.cartlc.tracker.ui.stage.StageHook

class ButtonsView(
        context: Context,
        attrs: AttributeSet?,
        @AttrRes defStyleAttr: Int
) : FrameLayout(context, attrs, defStyleAttr) {

    constructor(context: Context) : this(context, null, 0)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    private val app = context.applicationContext as TBApplication
    private val act = context as BaseActivity
    private val boundAct = act.boundAct
    private val componentRoot: ComponentRoot = app.componentRoot
    private val factoryViewMvc = componentRoot.factoryViewMvc
    private val factoryController = componentRoot.factoryController

    val controller: ButtonsController

    var softKeyboardDetect: SoftKeyboardDetect? = null
        set(value) {
            field = value
            field?.let { controller.install(it) } ?: run { controller.uninstall() }
        }

    init {
        val viewMvc = factoryViewMvc.allocButtonsViewMvc(null)
        controller = factoryController.allocButtonsController(boundAct, viewMvc)
        addView(viewMvc.rootView)
    }

}