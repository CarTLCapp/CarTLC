package com.cartlc.tracker.viewmodel

import android.app.Activity
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import com.cartlc.tracker.databinding.FragTitleBinding
import com.cartlc.tracker.model.CarRepository
import com.cartlc.tracker.model.misc.StringMessage
import com.cartlc.tracker.model.pref.PrefHelper
import com.cartlc.tracker.ui.app.TBApplication
import javax.inject.Inject

class TitleViewModel(private val act: Activity) : BaseViewModel() {

    @Inject
    lateinit var repo: CarRepository

    private val app: TBApplication
        get() = act.applicationContext as TBApplication

    init {
        app.carRepoComponent.inject(this)
    }

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