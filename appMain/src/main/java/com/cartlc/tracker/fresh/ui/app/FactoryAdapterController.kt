/**
 * Copyright 2019, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.ui.app

import com.cartlc.tracker.fresh.ui.mainlist.adapter.*
import com.cartlc.tracker.fresh.model.CarRepository
import com.cartlc.tracker.fresh.model.msg.MessageHandler

class FactoryAdapterController(
        val repo: CarRepository,
        val messageHandler: MessageHandler
) {
    fun allocSimpleListController(listener: SimpleListController.Listener): SimpleListController {
        return SimpleListController(listener)
    }

    fun allocProjectGroupController(listener: ProjectGroupListController.Listener): ProjectGroupListController {
        return ProjectGroupListController(repo, messageHandler, listener)
    }

    fun allocEquipmentSelectController(listener: EquipmentSelectController.Listener): EquipmentSelectController {
        return EquipmentSelectController(repo, listener)
    }

    fun allocRadioListController(listener: RadioListController.Listener): RadioListController {
        return RadioListController(listener)
    }

    fun allocNoteListEntryController(listener: NoteListEntryController.Listener): NoteListEntryController {
        return NoteListEntryController(repo, listener)
    }

}