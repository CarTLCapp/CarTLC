/*
 * Copyright 2019, FleetTLC. All rights reserved
 */

package com.cartlc.tracker.fresh.ui.confirm.data

import java.io.File

data class ConfirmDataPicture(
        val pictureLabel: String,
        val pictureItems: List<File>,
        val pictureNotes: List<NoteLabelValue>
)