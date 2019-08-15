package com.cartlc.tracker.fresh.ui.confirm

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.cartlc.tracker.R
import com.cartlc.tracker.fresh.model.core.data.DataEntry
import com.cartlc.tracker.fresh.ui.app.dependencyinjection.BoundFrag
import com.cartlc.tracker.fresh.model.flow.CurrentProjectFlow
import com.cartlc.tracker.fresh.model.flow.Flow
import com.cartlc.tracker.fresh.model.flow.FlowUseCase
import com.cartlc.tracker.fresh.model.flow.Stage
import com.cartlc.tracker.fresh.ui.picture.PictureListViewMvcImpl

class ConfirmFinalController (
        boundFrag: BoundFrag,
        private val viewMvc: ConfirmFinalViewMvc
) : ConfirmFinalUseCase,
        LifecycleObserver,
        ConfirmFinalViewMvc.Listener,
        FlowUseCase.Listener {

    private val ctx = boundFrag.act
    private val repo = boundFrag.repo
    private val componentRoot = boundFrag.componentRoot
    private val prefHelper = componentRoot.prefHelper
    private var curEntry: DataEntry? = null
    // TODO: Would like a more universal way of handling this (see other references)
    private val thumbnailHeight = boundFrag.act.resources.getDimension(R.dimen.image_thumbnail_max_height).toInt()

    init {
        boundFrag.bindObserver(this)
    }

    // region lifecycle functions

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate() {
        viewMvc.registerListener(this)
        repo.flowUseCase.registerListener(this)
        onStageChanged(repo.curFlowValue)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        viewMvc.unregisterListener(this)
        repo.flowUseCase.unregisterListener(this)
    }

    // endregion lifecycle functions

    // region FlowUseCase.Listener

    override fun onStageChangedAboutTo(flow: Flow) {
    }

    override fun onStageChanged(flow: Flow) {
        when (flow.stage) {
            Stage.CONFIRM -> {
                curEntry = prefHelper.saveEntry()
                curEntry?.let { entry -> fill(entry) }
            }
            else -> {
            }
        }
    }

    // endregion FlowUseCase.Listener

    private fun fill(entry: DataEntry) {
        viewMvc.projectName = entry.projectDashName
        val address = entry.addressBlock
        if (address.isNullOrBlank()) {
            viewMvc.projectAddress = null
        } else {
            viewMvc.projectAddress = address
        }
        viewMvc.notes = entry.notesWithValues
        val truck = entry.truck
        if (truck == null) {
            viewMvc.truckNumber = null
        } else {
            viewMvc.truckNumber = truck.toString()
        }
        viewMvc.equipmentNames = entry.equipmentNames ?: emptyList()
        viewMvc.pictures = entry.pictures.toMutableList()
        viewMvc.status = entry.getStatus(ctx)
        viewMvc.pictureLabel = ctx.getString(R.string.title_pictures_, entry.pictures.size)
        viewMvc.pictureListHeight = thumbnailHeight * (viewMvc.pictures.size / PictureListViewMvcImpl.NUM_COLUMNS + 1)
    }

    override fun onConfirmOkay() {
        curEntry?.let {
            repo.add(it)
            prefHelper.clearLastEntry()
            curEntry = null
            repo.curFlowValue = CurrentProjectFlow()
        }
    }

}