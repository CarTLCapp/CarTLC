/**
 * Copyright 2019, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.ui.picture

import com.cartlc.tracker.fresh.model.core.data.DataNote
import com.cartlc.tracker.fresh.model.core.data.DataPicture
import com.cartlc.tracker.fresh.ui.common.observable.BaseObservable

interface PictureListUseCase : BaseObservable<PictureListUseCase.Listener> {

    interface Listener {
        fun onPictureRemoveDone(numPictures: Int)
        fun onPictureNoteChanged(note: DataNote)
    }

    var isThumbnail: Boolean
    var pictureItems: List<DataPicture>
    var pictureNotes: List<DataNote>
    val commonRotation: Int
    val hadSomeRotations: Boolean
    val notesReady: Boolean

    fun onPictureRefreshNeeded()

}