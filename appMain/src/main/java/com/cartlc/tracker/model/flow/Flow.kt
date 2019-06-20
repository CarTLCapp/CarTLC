package com.cartlc.tracker.model.flow

import androidx.annotation.VisibleForTesting
import com.cartlc.tracker.model.event.Action
import com.cartlc.tracker.model.event.Button
import timber.log.Timber

sealed class ActionBundle
data class ActionArg(val action: Action) : ActionBundle()
data class StageArg(val stage: Stage) : ActionBundle()

open class Flow(
        val stage: Stage,
        private val prev: ActionBundle? = null,
        private val center: ActionBundle? = null,
        private val next: ActionBundle? = null
) {

    constructor(
            stage: Stage,
            prev: Stage?,
            center: Stage?,
            next: Stage?
    ) : this(stage, prev?.let { StageArg(prev) }, center?.let { StageArg(center) }, next?.let { StageArg(next) })

    constructor(
            stage: Stage,
            prev: Action?,
            center: Action?,
            next: Action?
    ) : this(stage, prev?.let { ActionArg(prev) }, center?.let { ActionArg(center) }, next?.let { ActionArg(next) })

    open val isPictureStage = false

    companion object {

        fun checkNull(flow: Flow?): Flow {
            if (flow == null) {
                Timber.e("UNKNOWN stage")
                return LoginFlow()
            }
            return flow
        }

        fun from(ord: Int): Flow = checkNull(from(Stage.from(ord)))

        fun from(stage: Stage?): Flow? =
                when (stage) {
                    Stage.LOGIN -> LoginFlow()
                    Stage.ROOT_PROJECT -> RootProjectFlow()
                    Stage.SUB_PROJECT -> SubProjectFlow()
                    Stage.COMPANY -> CompanyFlow()
                    Stage.ADD_COMPANY -> AddCompanyFlow()
                    Stage.STATE -> StateFlow()
                    Stage.ADD_STATE -> AddStateFlow()
                    Stage.CITY -> CityFlow()
                    Stage.ADD_CITY -> AddCityFlow()
                    Stage.STREET -> StreetFlow()
                    Stage.ADD_STREET -> AddStreetFlow()
                    Stage.CONFIRM_ADDRESS -> ConfirmAddressFlow()
                    Stage.CURRENT_PROJECT -> CurrentProjectFlow()
                    Stage.TRUCK -> TruckFlow()
                    Stage.EQUIPMENT -> EquipmentFlow()
                    Stage.ADD_EQUIPMENT -> AddEquipmentFlow()
                    Stage.NOTES -> NotesFlow()
                    Stage.STATUS -> StatusFlow()
                    Stage.PICTURE_1 -> Picture1Flow()
                    Stage.PICTURE_2 -> Picture2Flow()
                    Stage.PICTURE_3 -> Picture3Flow()
                    Stage.CONFIRM -> ConfirmFlow()
                    else -> null
                }

        var processStageEvent: (next: Flow) -> Unit = {}
        var processActionEvent: (act: Action) -> Unit = {}
    }

    private fun process(action: ActionBundle?) {
        when (action) {
            is StageArg -> processStageEvent(checkNull(from(action.stage)))
            is ActionArg -> processActionEvent(action.action)
        }
    }

    fun next() {
        process(next)
    }

    fun center() {
        process(center)
    }

    fun prev() {
        process(prev)
    }

    fun process(button: Button) {
        when (button) {
            Button.BTN_NEXT -> next()
            Button.BTN_CENTER -> center()
            Button.BTN_PREV -> prev()
            else -> {}
        }
    }

    val hasNext: Boolean
        get() = next != null

    val hasPrev: Boolean
        get() = prev != null

    val hasCenter: Boolean
        get() = center != null

    @VisibleForTesting
    val nextStage: Stage?
        get() = (next as? StageArg)?.stage

    @VisibleForTesting
    val prevStage: Stage?
        get() = (prev as? StageArg)?.stage

    @VisibleForTesting
    val centerStage: Stage?
        get() = (center as? StageArg)?.stage

    @VisibleForTesting
    val nextAction: Action?
        get() = (next as? ActionArg)?.action

    @VisibleForTesting
    val prevAction: Action?
        get() = (prev as? ActionArg)?.action

    @VisibleForTesting
    val centerAction: Action?
        get() = (center as? ActionArg)?.action
}


open class PictureFlow(
        stage: Stage,
        prev: Stage,
        next: Stage,
        val expected: Int
) : Flow(stage, StageArg(prev), ActionArg(Action.ADD_PICTURE), StageArg(next)) {
    override val isPictureStage = true
}

class LoginFlow : Flow(Stage.LOGIN)
class RootProjectFlow : Flow(Stage.ROOT_PROJECT, Stage.CURRENT_PROJECT, null, Stage.COMPANY)
class CompanyFlow : Flow(Stage.COMPANY, Stage.SUB_PROJECT, null, Stage.STATE)
class AddCompanyFlow : Flow(Stage.ADD_COMPANY, Stage.SUB_PROJECT, null, Stage.STATE)
class StateFlow : Flow(Stage.STATE, Stage.COMPANY, Stage.ADD_STATE, Stage.CITY)
class AddStateFlow : Flow(Stage.ADD_STATE, Stage.COMPANY, null, Stage.CITY)
class CityFlow : Flow(Stage.CITY, Stage.STATE, Stage.ADD_CITY, Stage.STREET)
class AddCityFlow : Flow(Stage.ADD_CITY, Stage.STATE, null, Stage.STREET)
class StreetFlow : Flow(Stage.STREET, Stage.CITY, Stage.ADD_STREET, Stage.CONFIRM_ADDRESS)
class AddStreetFlow : Flow(Stage.ADD_STREET, Stage.CITY, null, Stage.CONFIRM_ADDRESS)
class ConfirmAddressFlow : Flow(Stage.CONFIRM_ADDRESS, Stage.STREET, null, Stage.CURRENT_PROJECT)

class CurrentProjectFlow : Flow(Stage.CURRENT_PROJECT, Action.VIEW_PROJECT, Action.NEW_PROJECT, null)

class SubProjectFlow : Flow(Stage.SUB_PROJECT, Stage.CURRENT_PROJECT, null, Stage.TRUCK)
class TruckFlow : Flow(Stage.TRUCK, Stage.SUB_PROJECT, null, Stage.PICTURE_1)
class Picture1Flow : PictureFlow(Stage.PICTURE_1, Stage.TRUCK, Stage.EQUIPMENT, 1)
class EquipmentFlow : Flow(Stage.EQUIPMENT, Stage.PICTURE_1, Stage.ADD_EQUIPMENT, Stage.NOTES)
class AddEquipmentFlow : Flow(Stage.ADD_EQUIPMENT, Stage.PICTURE_1, null, Stage.NOTES)
class NotesFlow : Flow(Stage.NOTES, Stage.EQUIPMENT, null, Stage.PICTURE_2)
class Picture2Flow : PictureFlow(Stage.PICTURE_2, Stage.NOTES, Stage.STATUS, 2)
class StatusFlow : Flow(Stage.STATUS, Stage.PICTURE_2, null, Stage.PICTURE_3)
class Picture3Flow : PictureFlow(Stage.PICTURE_3, Stage.STATUS, Stage.CONFIRM, 3)
class ConfirmFlow : Flow(Stage.CONFIRM, Stage.PICTURE_3, null, Stage.CURRENT_PROJECT)
