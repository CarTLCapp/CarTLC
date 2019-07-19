package com.cartlc.tracker.fresh.ui.main.process

import android.os.AsyncTask
import com.cartlc.tracker.fresh.ui.main.MainController
import com.cartlc.tracker.fresh.model.msg.ErrorMessage
import com.cartlc.tracker.ui.util.helper.BitmapHelper
import java.io.File
import java.lang.ref.WeakReference

class TaskPicture (
        shared: MainController.Shared
) : ProcessBase(shared) {

    private class RotatePictureTask(task: TaskPicture) : AsyncTask<Void, Void, Boolean>() {

        private val ref = WeakReference(task)
        private val main: TaskPicture?
            get() = ref.get()

        override fun doInBackground(vararg voids: Void): Boolean? {
            main?.autoRotatePictureResult()
            return true
        }

        override fun onPostExecute(result: Boolean?) {
            main?.onRefreshIfNeeded()
        }
    }

    var takingPictureFile: File? = null

    private val isPictureStage: Boolean
        get() = shared.curFlowValue.isPictureStage

    fun dispatchPictureRequest() {
        with(shared) {
            val pictureFile = prefHelper.genFullPictureFile()
            db.tablePictureCollection.add(pictureFile, prefHelper.currentPictureCollectionId)
            takingPictureFile = pictureFile
            dispatchPictureRequest(pictureFile)
        }
    }

    private fun dispatchPictureRequest(pictureFile: File) {
        with(shared) {
            if (!screenNavigator.dispatchPictureRequest(pictureFile, MainController.REQUEST_IMAGE_CAPTURE)) {
                dispatchPictureRequestFailure()
            }
        }
    }

    private fun dispatchPictureRequestFailure() {
        with(shared) {
            takingPictureFile = null
            errorValue = ErrorMessage.CANNOT_TAKE_PICTURE
        }
    }

    fun rotatePicture() {
        RotatePictureTask(this).execute()
    }

    fun onPictureRequestComplete() {
        shared.repo.flowUseCase.notifyListeners()
    }

    private fun autoRotatePictureResult() {
        with(shared) {
            takingPictureFile?.let {
                if (it.exists()) {
                    val degrees = prefHelper.autoRotatePicture
                    if (degrees != 0) {
                        BitmapHelper.rotate(it, degrees)
                    }
                }
            }
        }
    }

    private fun onRefreshIfNeeded() {
        with(shared) {
            if (!isFinishing && isPictureStage) {
                pictureUseCase.onPictureRefreshNeeded()
            }
        }
    }
}