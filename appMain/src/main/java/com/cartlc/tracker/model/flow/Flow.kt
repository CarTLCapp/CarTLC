package com.cartlc.tracker.model.flow

import timber.log.Timber

sealed class ActionBundle
data class ActionArg(val action: Action) : ActionBundle()
data class StageArg(val stage: Stage) : ActionBundle()

open class Flow(
        val stage: Stage,
        val prev: ActionBundle?,
        val center: ActionBundle?,
        val next: ActionBundle?
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

        fun from(ord: Int): Flow = from(Stage.from(ord))

        fun from(stage: Stage?): Flow =
                when (stage) {
                    Stage.LOGIN -> LoginFlow()
                    Stage.PROJECT -> ProjectFlow()
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
                    Stage.NOTES -> NotesFlow()
                    Stage.STATUS -> StatusFlow()
                    Stage.PICTURE_1 -> Picture1Flow()
                    Stage.PICTURE_2 -> Picture2Flow()
                    Stage.PICTURE_3 -> Picture3Flow()
                    Stage.CONFIRM -> ConfirmFlow()
                    else -> {
                        Timber.e("UNKNOWN stage")
                        LoginFlow()
                    }
                }
    }
}


open class PictureFlow(
        stage: Stage,
        prev: Stage?,
        center: Stage?,
        next: Stage?,
        val expected: Int
) : Flow(stage, prev, center, next) {
    override val isPictureStage = true
}

class LoginFlow : Flow(Stage.LOGIN, null, Stage.PROJECT, null)
class ProjectFlow : Flow(Stage.PROJECT, Stage.CURRENT_PROJECT, null, Stage.COMPANY)
class CompanyFlow : Flow(Stage.COMPANY, Stage.PROJECT, Stage.ADD_COMPANY, Stage.STATE)
class AddCompanyFlow : Flow(Stage.ADD_COMPANY, Stage.PROJECT, null, Stage.STATE)
class StateFlow : Flow(Stage.STATE, Stage.COMPANY, Stage.ADD_STATE, Stage.CITY)
class AddStateFlow : Flow(Stage.ADD_STATE, Stage.COMPANY, null, Stage.CITY)
class CityFlow : Flow(Stage.CITY, Stage.STATE, Stage.ADD_CITY, Stage.STREET)
class AddCityFlow : Flow(Stage.ADD_CITY, Stage.STATE, null, Stage.STREET)
class StreetFlow : Flow(Stage.STREET, Stage.CITY, Stage.ADD_STREET, Stage.CONFIRM_ADDRESS)
class AddStreetFlow : Flow(Stage.ADD_STREET, Stage.CITY, null, Stage.CONFIRM_ADDRESS)
class ConfirmAddressFlow : Flow(Stage.CONFIRM_ADDRESS, Stage.STREET, null, Stage.CURRENT_PROJECT)

class CurrentProjectFlow : Flow(Stage.CURRENT_PROJECT, Action.VIEW_PROJECT, Action.NEW_PROJECT, null)

class TruckFlow : Flow(Stage.TRUCK, Stage.CURRENT_PROJECT, null, Stage.PICTURE_1)
class Picture1Flow : PictureFlow(Stage.PICTURE_1, Stage.TRUCK, Stage.ADD_PICTURE, Stage.EQUIPMENT, 1)
class EquipmentFlow : Flow(Stage.EQUIPMENT, Stage.PICTURE_1, Stage.ADD_EQUIPMENT, Stage.NOTES)
class NotesFlow : Flow(Stage.NOTES, Stage.EQUIPMENT, null, Stage.PICTURE_2)
class Picture2Flow : PictureFlow(Stage.PICTURE_2, Stage.NOTES, Stage.ADD_PICTURE, Stage.STATUS, 2)
class StatusFlow : Flow(Stage.STATUS, Stage.PICTURE_2, null, Stage.PICTURE_3)
class Picture3Flow : PictureFlow(Stage.PICTURE_3, Stage.STATUS, Stage.ADD_PICTURE, Stage.CONFIRM, 3)
class ConfirmFlow : Flow(Stage.CONFIRM, Stage.PICTURE_3, null, Stage.CURRENT_PROJECT)
