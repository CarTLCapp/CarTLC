/*
 * Copyright 2019, FleetTLC. All rights reserved
 */

package com.cartlc.tracker.fresh.ui.main.process

import com.cartlc.tracker.fresh.model.msg.StringMessage
import com.cartlc.tracker.fresh.ui.main.MainController

class StageStatus(
        shared: MainController.Shared
) : ProcessBase(shared) {

    fun process() {
        with(shared) {
            buttonsUseCase.nextText = messageHandler.getString(StringMessage.btn_done)
            mainListUseCase.visible = true
            titleUseCase.mainTitleText = messageHandler.getString(StringMessage.title_status)
            titleUseCase.subTitleText = statusHint
            pictureUseCase.clearCache()
        }
    }

    private val statusHint: String
        get() {
            with(shared) {
                val sbuf = StringBuilder()
                val countPictures = prefHelper.numPicturesTaken
                val maxEquip = prefHelper.numEquipPossible
                val checkedEquipment = db.tableEquipment.queryChecked().size
                val noteCount = countNotes
                sbuf.append(messageHandler.getString(StringMessage.status_installed_equipments(checkedEquipment, maxEquip)))
                sbuf.append("\n")
                sbuf.append(messageHandler.getString(StringMessage.status_installed_pictures(countPictures)))
                if (noteCount > 0) {
                    sbuf.append("\n")
                    sbuf.append(messageHandler.getString(StringMessage.status_notes_used(noteCount)))
                }
                return sbuf.toString()
            }
        }

    private val countNotes: Int
        get() {
            with(shared) {
                return prefHelper.currentProjectGroup?.let { combo ->
                    var count = 0
                    for (note in db.noteHelper.getPendingNotes(combo.projectNameId)) {
                        if (!note.value.isNullOrBlank()) {
                            count++
                        }
                    }
                    count
                } ?: 0
            }
        }

}