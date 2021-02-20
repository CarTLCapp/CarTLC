/**
 * Copyright 2019, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.ui.main.process

import com.cartlc.tracker.fresh.ui.main.MainController
import com.cartlc.tracker.fresh.model.msg.StringMessage

class StageTruckNumber(
        shared: MainController.Shared,
        private val taskPicture: TaskPicture
) : ProcessBase(shared) {

    fun process() {
        with(shared) {
            prefHelper.saveProjectAndAddressCombo(modifyCurrent = false, needsValidServerId = true)
            picturesVisible = true

            var showToast = false
            if (buttonsController.wasNext) {
                showToast = true
                buttonsController.wasNext = false
            }
            pictureUseCase.pictureItems = db.tablePicture.removeFileDoesNotExist(
                    db.tablePicture.query(prefHelper.currentPictureCollectionId, repo.curFlowValueStage)
            )
            // TODO: Perhaps if this is an existing value (see DataEntry.notesWithValues), then overlay those values
            db.tableNote.noteTruckNumber?.let { pictureUseCase.pictureNotes = listOf(it) }

            val numPictures = pictureUseCase.pictureItems.size
            if (numPictures == 0) {
                if (showToast) {
                    screenNavigator.showToast(messageHandler.getString(StringMessage.truck_number_request))
                }
                buttonsController.nextVisible = false

                if (taskPicture.takingPictureAborted) {
                    buttonsController.centerVisible = true
                    buttonsController.centerText = messageHandler.getString(StringMessage.btn_another)
                } else {
                    taskPicture.dispatchPictureRequest()
                }
            } else {
                if (!hasTruckNumberValue) {
                    if (showToast) {
                        screenNavigator.showToast(messageHandler.getString(StringMessage.truck_number_enter))
                    }
                    buttonsController.nextVisible = false
                }
            }
            titleUseCase.mainTitleText = title
            titleUseCase.mainTitleVisible = true
        }
    }

    private val hasTruckNumberValue: Boolean
        get() {
            with(shared) {
                return db.tableNote.noteTruckNumber?.value?.isNotBlank() ?: false
            }
        }

    private val title: String?
        get() {
            with (shared) {
                return db.tableNote.noteTruckNumber?.name
            }
        }

    fun save(): Boolean {
        return true
    }

    fun pictureStateChanged() {
        with(shared) {
            buttonsController.nextVisible = pictureUseCase.pictureItems.size == 1 && hasTruckNumberValue

            val currentNumPictures = pictureUseCase.pictureItems.size
            if (currentNumPictures == 0) {
                buttonsController.centerVisible = true
                buttonsController.centerText = messageHandler.getString(StringMessage.btn_another)
            }
        }
    }

    fun center() {
        taskPicture.takingPictureAborted = false
        taskPicture.dispatchPictureRequest()
    }

}