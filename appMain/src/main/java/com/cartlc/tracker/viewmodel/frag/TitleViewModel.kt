/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.viewmodel.frag

import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.cartlc.tracker.model.CarRepository
import com.cartlc.tracker.model.flow.Flow
import com.cartlc.tracker.model.flow.FlowUseCase
import com.cartlc.tracker.model.msg.MessageHandler
import com.cartlc.tracker.model.msg.StringMessage
import com.cartlc.tracker.ui.app.dependencyinjection.BoundFrag
import com.cartlc.tracker.viewmodel.BaseViewModel

class TitleViewModel(
        boundFrag: BoundFrag
) : BaseViewModel(), LifecycleObserver, FlowUseCase.Listener {

    private val messageHandler = boundFrag.componentRoot.messageHandler
    private val repo = boundFrag.repo

    var title = ObservableField<String>()
    var subTitle = ObservableField<String>()
    var showSeparator = ObservableBoolean(false)

    var titleValue: String?
        get() = title.get()
        set(value) = title.set(value)
    var subTitleValue: String?
        get() = subTitle.get()
        set(value) = subTitle.set(value)
    var showSeparatorValue: Boolean
        get() = showSeparator.get()
        set(value) = showSeparator.set(value)

    init {
        boundFrag.bindObserver(this)
        repo.flowUseCase.registerListener(this)
    }

    // region lifecycle
    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate() {
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        repo.flowUseCase.unregisterListener(this)
    }

    // endregion lifecycle

    // region FlowUseCase.Listener

    override fun onStageChangedAboutTo(flow: Flow) {
        showSeparatorValue = false
        subTitleValue = null
    }

    override fun onStageChanged(flow: Flow) {
    }

    /// endregion FlowUseCase.Listener
    fun setPhotoTitleCount(count: Int) {
        if (count == 1) {
            titleValue = messageHandler.getString(StringMessage.title_photo)
        } else {
            titleValue = messageHandler.getString(StringMessage.title_photos(count))
        }
    }

}