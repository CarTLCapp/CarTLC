package com.cartlc.tracker.viewmodel

import android.app.Activity
import android.view.View
import androidx.lifecycle.MutableLiveData
import com.cartlc.tracker.databinding.FragMainListBinding
import com.cartlc.tracker.model.CarRepository
import com.cartlc.tracker.model.flow.Flow
import com.cartlc.tracker.model.flow.LoginFlow
import com.cartlc.tracker.model.flow.Stage
import com.cartlc.tracker.model.pref.PrefHelper
import com.cartlc.tracker.model.table.DatabaseTable
import com.cartlc.tracker.ui.act.MainActivity
import com.cartlc.tracker.ui.app.TBApplication
import kotlinx.android.synthetic.main.content_main.*
import javax.inject.Inject

class MainListViewModel(
        private val act: Activity,
        private val binding: FragMainListBinding
) : BaseViewModel() {

    @Inject
    lateinit var repo: CarRepository

    private val prefHelper: PrefHelper
        get() = repo.prefHelper
    private val tmpActivity: MainActivity
        get() = act as MainActivity
    private val app: TBApplication
        get() = act.applicationContext as TBApplication
    val tmpPrefHelper: PrefHelper
        get() = repo.prefHelper
    val tmpDb: DatabaseTable
        get() = repo.db

    init {
        app.carRepoComponent.inject(this)
    }

    var showing: Boolean = false
        set(value) {
            field = value
            binding.invalidateAll()
        }

    var showEmpty: Boolean = false
        set(value) {
            field = value
            binding.invalidateAll()
        }

    var curKey: String? = null

    val curFlow: MutableLiveData<Flow>
        get() = repo.curFlow

    private var curFlowValue: Flow
        get() = curFlow.value ?: LoginFlow()
        set(value) {
            curFlow.value = value
        }

    val isInNotes: Boolean
        get() = curFlowValue.stage == Stage.NOTES

    fun onKeySelected(text: String) {
        curKey?.let {
            prefHelper.setKeyValue(it, text)
            when (curFlowValue.stage) {
                Stage.PROJECT,
                Stage.CITY,
                Stage.STATE,
                Stage.STREET -> tmpActivity.btn_next.visibility = View.VISIBLE
                Stage.COMPANY -> tmpActivity.checkCenterButtonIsEdit()
                Stage.TRUCK -> tmpActivity.entry_simple.setText(text)
                else -> {
                }
            }
        }
    }

}