package com.cartlc.tracker.fresh.ui.main.process

import com.cartlc.tracker.fresh.ui.main.MainController
import com.cartlc.tracker.model.msg.StringMessage

class StageConfirm(
        shared: MainController.Shared
) : ProcessBase(shared) {

    fun process() {
        with(shared) {
            buttonsUseCase.nextText = messageHandler.getString(StringMessage.btn_confirm)
            titleUseCase.mainTitleText = messageHandler.getString(StringMessage.title_confirmation)
            storeCommonRotation()
        }
    }

    private fun storeCommonRotation() {
        with(shared) {
            val commonRotation = pictureUseCase.commonRotation
            if (commonRotation != 0) {
                prefHelper.incAutoRotatePicture(commonRotation)
            } else {
                if (pictureUseCase.hadSomeRotations) {
                    prefHelper.clearAutoRotatePicture()
                }
            }
        }
    }

}