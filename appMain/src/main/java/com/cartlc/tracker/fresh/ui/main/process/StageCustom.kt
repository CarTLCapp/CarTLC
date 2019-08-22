/**
 * Copyright 2019, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.ui.main.process

import com.cartlc.tracker.fresh.model.core.data.DataFlowElement
import com.cartlc.tracker.fresh.model.core.data.DataFlowElement.Type
import com.cartlc.tracker.fresh.model.core.data.DataNote
import com.cartlc.tracker.fresh.model.flow.CustomFlow
import com.cartlc.tracker.fresh.ui.main.MainController
import com.cartlc.tracker.fresh.model.msg.StringMessage

class StageCustom(
        shared: MainController.Shared,
        private val taskPicture: TaskPicture
) : ProcessBase(shared) {

    fun process() {
        with(shared) {
            repo.currentFlowElement?.let { element -> process(element) }
        }
    }

    private fun process(element: DataFlowElement) {
        with(shared) {
            var showToast = false
            if (buttonsUseCase.wasNext) {
                showToast = true
                buttonsUseCase.wasNext = false
            }
            val currentNumPictures: Int
            if (element.numImages > 0) {
                picturesVisible = true
                pictureUseCase.pictureItems = db.tablePicture.removeFileDoesNotExist(
                        db.tablePicture.query(prefHelper.currentPictureCollectionId, repo.curFlowValueStage)
                )
                pictureUseCase.pictureNotes = getNotes(element.id)

                currentNumPictures = pictureUseCase.pictureItems.size

                titleUseCase.mainTitleText = getPhotoTitle(element.prompt, currentNumPictures, element.numImages.toInt())

                if (currentNumPictures < element.numImages) {
                    if (element.numImages > 1 || taskPicture.takingPictureAborted) {
                        buttonsUseCase.centerVisible = true
                        buttonsUseCase.centerText = messageHandler.getString(StringMessage.btn_another)
                    }
                    if (currentNumPictures == 0 && !taskPicture.takingPictureAborted) {
                        taskPicture.dispatchPictureRequest()
                    }
                    buttonsUseCase.nextVisible = false
                } else {
                    buttonsUseCase.nextVisible = true
                }
                if (taskPicture.takingPictureAborted) {
                    showToast = true
                }
            } else {
                currentNumPictures = 0
            }
            when (element.type) {
                Type.NONE -> {
                    if (db.tableFlowElementNote.hasNotes(element.id)) {
                        titleUseCase.mainTitleText = messageHandler.getString(StringMessage.title_notes)
                        mainListUseCase.visible = true
                    } else {
                    }
                }
                Type.TOAST ->
                    if (showToast) {
                        getToastMessage(element, currentNumPictures)?.let {
                            screenNavigator.showToast(it)
                        }
                    } else {
                    }
                Type.DIALOG ->
                    element.prompt?.let {
                        dialogNavigator.showDialog(it) {
                            repo.curFlowValue.next()
                        }
                    }
                Type.CONFIRM,
                Type.CONFIRM_NEW -> {
                    mainListUseCase.visible = true
                    titleUseCase.mainTitleText = messageHandler.getString(StringMessage.title_confirm_checklist)
                    titleUseCase.mainTitleVisible = true
                }
                else -> {
                }
            }
            progress = computeProgress(element)
        }
    }

    // TODO: Perhaps if this is an existing value (see DataEntry.notesWithValues), then overlay those values
    private fun getNotes(elementId: Long): List<DataNote> {
        return with(shared) {
            val notes = mutableListOf<DataNote>()
            val elements = repo.db.tableFlowElementNote.query(elementId)
            for (element in elements) {
                repo.db.tableNote.query(element.noteId)?.let {
                    notes.add(it)
                }
            }
            notes
        }
    }

    fun save(isNext: Boolean): Boolean {
        with(shared) {
            repo.currentFlowElement?.let { element ->
                val hasNotes = db.tableFlowElementNote.hasNotes(element.id)
                val notes = if (hasNotes) {
                    db.tableFlowElementNote.queryNotes(element.id)
                } else {
                    emptyList()
                }
                when (element.type) {
                    Type.NONE -> {
                        if (hasNotes) {
                            if (isNext && !mainListUseCase.areNotesComplete) {
                                dialogNavigator.showNoteError(notes)
                                return false
                            }
                        }
                    }
                    Type.CONFIRM,
                    Type.CONFIRM_NEW -> {
                        if (!mainListUseCase.isConfirmReady) {
                            screenNavigator.showToast(messageHandler.getString(StringMessage.error_need_all_checked))
                            return false
                        }
                    }
                    else -> {
                    }
                }
                if (hasNotes) {
                    prefHelper.currentEditEntry?.let {
                        db.tableCollectionNoteEntry.save(it.noteCollectionId, notes)
                    }
                }
                taskPicture.takingPictureAborted = false

                if (isNext) {
                    db.tableFlowElement.next(element.id)?.let {
                        repo.curFlowValue = CustomFlow(it)
                    } ?: return true
                } else {
                    db.tableFlowElement.prev(element.id)?.let {
                        repo.curFlowValue = CustomFlow(it)
                    } ?: return true
                }
            } ?: return true
        }
        // Note: returning false here is very important. I am NOT using the normal
        // prev & next stages because custom is special and has it's own idea of that.
        return false
    }

    fun pictureStateChanged() {
        with(shared) {
            buttonsUseCase.nextVisible = ready
            repo.currentFlowElement?.let { element ->
                val currentNumPictures = pictureUseCase.pictureItems.size
                if (element.numImages > 1 && currentNumPictures < element.numImages) {
                    buttonsUseCase.centerVisible = true
                    buttonsUseCase.centerText = messageHandler.getString(StringMessage.btn_another)
                }
            }
        }
    }

    private val ready: Boolean
        get() {
            with(shared) {
                repo.currentFlowElement?.let { element ->
                    val stage = repo.curFlowValueStage
                    when (element.type) {
                        Type.NONE -> {
                            val hasNotes = db.tableFlowElementNote.hasNotes(element.id)
                            if (hasNotes) {
                                val notesComplete = mainListUseCase.areNotesComplete
                                if (!notesComplete) {
                                    return false
                                }
                            }
                        }
                        else -> {
                        }
                    }
                    val currentNumPictures = db.tablePicture.countPendingPictures(stage)
                    if (currentNumPictures < element.numImages) {
                        return false
                    }
                }
                return true
            }
        }

    private fun getPhotoTitle(prompt: String?, count: Int, max: Int): String? {
        return with(shared) {
            if (count == max) {
                prompt
            } else if (max > 1 && count < max) {
                messageHandler.getString(StringMessage.title_photos(count, max))
            } else {
                null
            }
        }
    }

    private fun getToastMessage(element: DataFlowElement, count: Int): String? {
        with(shared) {
            val prompt = element.prompt ?: return null
            val max = element.numImages.toInt()
            if (max > 0) {
                if (max == 1 && count == 0 || count == max - 1) {
                    return messageHandler.getString(StringMessage.prompt_custom_photo_1(prompt))
                } else if (max > 1 && count == 0) {
                    return messageHandler.getString(StringMessage.prompt_custom_photo_N(max, prompt))
                } else if (max > 1 && count < max) {
                    return messageHandler.getString(StringMessage.prompt_custom_photo_more(max - count, prompt))
                }
            }
            val hasNotes = db.tableFlowElementNote.hasNotes(element.id)
            if (hasNotes) {
                val notesComplete = mainListUseCase.areNotesComplete
                if (!notesComplete) {
                    return messageHandler.getString(StringMessage.prompt_notes(prompt))
                }
            }
            if (max > 0 && max == count) {
                return null
            }
            return prompt
        }
    }

    private fun computeProgress(element: DataFlowElement): String? {
        return shared.db.tableFlowElement.progress(element.id)?.let {
            return "${it.first + 1}/${it.second}"
        }
    }

    fun center() {
        taskPicture.takingPictureAborted = false
        taskPicture.dispatchPictureRequest()
    }
}