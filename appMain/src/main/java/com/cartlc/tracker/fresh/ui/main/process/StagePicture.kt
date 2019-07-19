package com.cartlc.tracker.fresh.ui.main.process

import com.cartlc.tracker.fresh.model.core.data.DataPicture
import com.cartlc.tracker.fresh.ui.main.MainController
import com.cartlc.tracker.fresh.model.flow.Flow
import com.cartlc.tracker.fresh.model.flow.PictureFlow
import com.cartlc.tracker.fresh.model.flow.Stage
import com.cartlc.tracker.fresh.model.msg.StringMessage

class StagePicture(
        shared: MainController.Shared,
        private val taskPicture: TaskPicture
) : ProcessBase(shared) {

    fun process(flow: Flow) {
        with(shared) {
            when (flow.stage) {
                Stage.PICTURE_1,
                Stage.PICTURE_2,
                Stage.PICTURE_3 -> {
                    val pictureCount = prefHelper.numPicturesTaken
                    var showToast = false
                    if (buttonsUseCase.wasNext) {
                        showToast = true
                        buttonsUseCase.wasNext = false
                    }
                    val pictures = db.tablePictureCollection.removeNonExistant(
                            db.tablePictureCollection.queryPictures(prefHelper.currentPictureCollectionId
                            )).toMutableList()
                    if (showToast) {
                        screenNavigator.showPictureToast(pictureCount)
                    }
                    titleUseCase.setPhotoTitleCount(pictureCount)
                    buttonsUseCase.nextVisible = false
                    buttonsUseCase.centerVisible = true
                    buttonsUseCase.centerText = messageHandler.getString(StringMessage.btn_another)
                    picturesVisible = true
                    val pictureFlow = flow as PictureFlow
                    if (pictureCount < pictureFlow.expected) {
                        taskPicture.dispatchPictureRequest()
                    } else {
                        buttonsUseCase.nextVisible = true
                        setList(pictures)
                    }
                }
            }
        }
    }

    private fun setList(list: List<DataPicture>) {
        shared.pictureUseCase.pictureItems = list
    }
}