package com.cartlc.tracker.fresh.model.core.table

import com.cartlc.tracker.fresh.model.core.data.DataPicture
import com.cartlc.tracker.fresh.model.core.data.DataPictureCollection
import java.io.File

interface TablePictureCollection {
    fun add(collection: DataPictureCollection)
    fun add(picture: File, collection_id: Long?): DataPicture
    fun clearPendingPictures()
    fun clearUploadedUnscaledPhotos()
    fun countPendingPictures(): Int
    fun countPictures(collection_id: Long): Int
    fun createCollectionFromPending(nextPictureCollectionID: Long): DataPictureCollection
    fun query(collection_id: Long): DataPictureCollection
    fun queryPictures(collection_id: Long): List<DataPicture>
    fun removeNonExistant(list: List<DataPicture>): List<DataPicture>
    fun setUploaded(item: DataPicture)
    fun update(item: DataPicture, collection_id: Long?)
}