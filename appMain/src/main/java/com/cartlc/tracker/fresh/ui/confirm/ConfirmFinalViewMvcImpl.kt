package com.cartlc.tracker.fresh.ui.confirm

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cartlc.tracker.R
import com.cartlc.tracker.fresh.ui.app.factory.FactoryViewMvc
import com.cartlc.tracker.fresh.ui.bits.AutoLinearLayoutManager
import com.cartlc.tracker.fresh.ui.common.viewmvc.ViewMvcImpl
import com.cartlc.tracker.fresh.ui.confirm.data.ConfirmDataType

class ConfirmFinalViewMvcImpl(
        inflater: LayoutInflater,
        container: ViewGroup?,
        factoryViewMvc: FactoryViewMvc
) : ViewMvcImpl(), ConfirmFinalViewMvc {

    override val rootView: View = inflater.inflate(R.layout.frame_confirm, container, false) as ViewGroup

    private val confirmList = findViewById<RecyclerView>(R.id.confirm_list)
    private val confirmAdapter = ConfirmFinalAdapter(factoryViewMvc)

    init {
        confirmList.layoutManager = AutoLinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        confirmList.adapter = confirmAdapter
    }

    // region ConfirmFinalViewMvc

    override var items: List<ConfirmDataType>
        get() = confirmAdapter.items
        set(value) {
            confirmAdapter.items = value
        }

    // endregion ConfirmFinalViewMvc
}