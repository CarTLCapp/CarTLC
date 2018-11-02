package com.cartlc.tracker.ui.frag

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.cartlc.tracker.viewmodel.BaseViewModel
import com.cartlc.tracker.model.misc.EntryHint
import com.cartlc.tracker.model.misc.ErrorMessage
import com.cartlc.tracker.ui.act.BaseActivity

open class BaseFragment : Fragment() {

    protected lateinit var baseVM: BaseViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val act = activity as BaseActivity
        val errorObserver = Observer<ErrorMessage> { message -> act.showError(message) }
        baseVM.error.observe(act, errorObserver)
        val entryHintObserver = Observer<EntryHint> { hint -> act.showEntryHint(hint) }
        baseVM.entryHint.observe(act, entryHintObserver)
        return null
    }

}