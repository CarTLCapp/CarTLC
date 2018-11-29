/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package com.cartlc.tracker.viewmodel.frag

import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import com.cartlc.tracker.model.CarRepository
import com.cartlc.tracker.model.misc.StringMessage
import com.cartlc.tracker.viewmodel.BaseViewModel

class TitleViewModel(private val repo: CarRepository) : BaseViewModel() {

    var title = ObservableField<String>()
    var subTitle = ObservableField<String>()
    var showSeparator = ObservableBoolean(false)

    var titleValue: String?
        get() = title.get()
        set(value) = title.set(value)
    var subTitleValue: String?
        get() = subTitle.get()
        set(value) = subTitle.set(value)
    var showSeparatorValue: Boolean
        get() = showSeparator.get()
        set(value) = showSeparator.set(value)

    var getString: (msg: StringMessage) -> String = { "" }

    fun setPhotoTitleCount(count: Int) {
        if (count == 1) {
            titleValue = getString(StringMessage.title_photo)
        } else {
            titleValue = getString(StringMessage.title_photos(count))
        }
    }

}