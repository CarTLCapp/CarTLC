/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.fresh.model.core.data

/**
 * Created by dug on 5/15/17.
 */

class DataPictureCollection(
        var id: Long // collection id shared by all pictures.
) {
    var pictures: MutableList<DataPicture> = mutableListOf()

    fun add(picture: DataPicture) {
        pictures.add(picture)
    }
}
