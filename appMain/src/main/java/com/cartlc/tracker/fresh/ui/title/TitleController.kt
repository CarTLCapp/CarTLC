package com.cartlc.tracker.fresh.ui.title

import androidx.lifecycle.Lifecycle
import com.cartlc.tracker.fresh.ui.app.dependencyinjection.BoundAct
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.cartlc.tracker.model.flow.Flow
import com.cartlc.tracker.model.flow.FlowUseCase
import com.cartlc.tracker.model.flow.Stage
import com.cartlc.tracker.model.msg.StringMessage

class TitleController(
        boundAct: BoundAct,
        private val viewMvc: TitleViewMvc
) : LifecycleObserver, TitleUseCase, FlowUseCase.Listener {

    private val repo = boundAct.repo
    private val messageHandler = boundAct.componentRoot.messageHandler

    init {
        boundAct.bindObserver(this)
    }

    // region lifecycle

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate() {
        repo.flowUseCase.registerListener(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        repo.flowUseCase.unregisterListener(this)
    }

    // endregion lifecycle

    override var mainTitleText: String?
        get() = viewMvc.mainTitleText
        set(value) {
            viewMvc.mainTitleText = value
            mainTitleVisible = value != null
        }
    override var mainTitleVisible: Boolean
        get() = viewMvc.mainTitleVisible
        set(value) { viewMvc.mainTitleVisible = value }
    override var subTitleText: String?
        get() = viewMvc.subTitleText
        set(value) {
            viewMvc.subTitleText = value
            subTitleVisible = value != null
        }
    override var subTitleVisible: Boolean
        get() = viewMvc.subTitleVisible
        set(value) { viewMvc.subTitleVisible = value }
    override var separatorVisible: Boolean
        get() = viewMvc.separatorVisible
        set(value) { viewMvc.separatorVisible = value }

    // region FlowUseCase.Listener

    override fun onStageChangedAboutTo(flow: Flow) {
        separatorVisible = false
        mainTitleVisible = false
        subTitleVisible = false
        mainTitleText = null
        subTitleText = null
    }

    override fun onStageChanged(flow: Flow) {
        when (flow.stage) {
            Stage.LOGIN -> {
                mainTitleText = messageHandler.getString(StringMessage.title_login)
                mainTitleVisible = true
            }
        }
    }

    // endregion FlowUseCase.Listener

    override fun setPhotoTitleCount(count: Int) {
        mainTitleText = if (count == 1) {
            messageHandler.getString(StringMessage.title_photo)
        } else {
            messageHandler.getString(StringMessage.title_photos(count))
        }
    }

}