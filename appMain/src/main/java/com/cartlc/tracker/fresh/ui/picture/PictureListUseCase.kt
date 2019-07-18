/**
 * Copyright 2019, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.ui.picture

import com.cartlc.tracker.fresh.model.core.data.DataPicture
import com.cartlc.tracker.fresh.ui.common.observable.BaseObservable

interface PictureListUseCase : BaseObservable<PictureListUseCase.Listener> {

    interface Listener {
        fun onPictureRemoveDone(remaining: Int)
        fun onPictureNoteAdded(picture: DataPicture)
    }

    var isThumbnail: Boolean
    var pictureItems: List<DataPicture>
    val commonRotation: Int
    val hadSomeRotations: Boolean

    fun onPictureRefreshNeeded()

}