package com.cartlc.tracker.fresh.model.core.table

import com.cartlc.tracker.fresh.model.core.data.DataPicture
import com.cartlc.tracker.fresh.model.flow.Stage
import java.io.File

interface TablePicture {
    fun add(pictures: List<DataPicture>)
    fun add(picture: File, collection_id: Long, stage: Stage): DataPicture
    fun clearPendingPictures()
    fun clearUploadedUnscaledPhotos()
    fun countPendingPictures(stage: Stage): Int
    fun countPictures(collection_id: Long, stage: Stage?): Int
    fun createCollectionFromPending(nextPictureCollectionID: Long): List<DataPicture>
    fun query(collection_id: Long, stage: Stage?): List<DataPicture>
    fun query(picture_id: Long): DataPicture?
    fun removeFileDoesNotExist()
    fun removeFileDoesNotExist(list: List<DataPicture>): List<DataPicture>
    fun setUploaded(item: DataPicture)
    fun update(item: DataPicture)
}