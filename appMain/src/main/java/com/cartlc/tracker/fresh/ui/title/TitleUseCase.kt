package com.cartlc.tracker.fresh.ui.title

interface TitleUseCase {

    var mainTitleText: String?
    var mainTitleVisible: Boolean
    var subTitleText: String?
    var subTitleVisible: Boolean
    var separatorVisible: Boolean

    fun setPhotoTitleCount(count: Int)
}