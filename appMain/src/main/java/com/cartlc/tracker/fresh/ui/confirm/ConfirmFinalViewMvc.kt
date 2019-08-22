package com.cartlc.tracker.fresh.ui.confirm

import com.cartlc.tracker.fresh.model.core.data.DataNote
import com.cartlc.tracker.fresh.model.core.data.DataPicture
import com.cartlc.tracker.fresh.ui.common.viewmvc.ObservableViewMvc

interface ConfirmFinalViewMvc : ObservableViewMvc<ConfirmFinalViewMvc.Listener> {

    interface Listener {

    }

    var projectName: String
    var projectAddress: String?
    var truckNumber: String?
    var status: String
    var pictureLabel: String
    var notes: List<DataNote>
    var equipmentNames: List<String>
    var pictures: List<DataPicture>

}