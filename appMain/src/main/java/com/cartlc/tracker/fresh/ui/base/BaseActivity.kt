package com.cartlc.tracker.fresh.ui.base

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.cartlc.tracker.R
import com.cartlc.tracker.fresh.model.misc.EntryHint
import com.cartlc.tracker.fresh.model.msg.ErrorMessage
import com.cartlc.tracker.fresh.ui.app.TBApplication
import com.cartlc.tracker.fresh.ui.app.dependencyinjection.BoundAct
import com.cartlc.tracker.fresh.ui.app.dependencyinjection.ComponentRoot
import com.cartlc.tracker.ui.util.helper.DialogHelper
import kotlinx.android.synthetic.main.content_main.*

abstract class BaseActivity : AppCompatActivity() {

    private lateinit var dialogHelper: DialogHelper // TODO: This is to be obsoleted in favor of DialogNavigator

    lateinit var boundAct: BoundAct
    lateinit var componentRoot: ComponentRoot

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        componentRoot = (applicationContext as TBApplication).componentRoot
        boundAct = BoundAct(this)
        dialogHelper = boundAct.dialogHelper
    }

    override fun onDestroy() {
        super.onDestroy()
        dialogHelper.clearDialog()
    }

}