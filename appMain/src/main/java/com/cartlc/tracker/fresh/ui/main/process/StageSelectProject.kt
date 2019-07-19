package com.cartlc.tracker.fresh.ui.main.process

import com.cartlc.tracker.fresh.ui.main.MainController
import com.cartlc.tracker.fresh.model.flow.Flow
import com.cartlc.tracker.fresh.model.flow.RootProjectFlow
import com.cartlc.tracker.fresh.model.flow.Stage
import com.cartlc.tracker.fresh.model.msg.StringMessage
import com.cartlc.tracker.fresh.model.pref.PrefHelper

class StageSelectProject(
        shared: MainController.Shared
) : ProcessBase(shared) {

    fun process(flow: Flow) {
        with(shared) {
            when(flow.stage) {
                Stage.ROOT_PROJECT -> {
                    mainListUseCase.visible = true
                    titleUseCase.subTitleText = null
                    titleUseCase.mainTitleVisible = true
                    titleUseCase.subTitleVisible = true
                    buttonsUseCase.nextVisible = hasProjectRootName
                    setList(StringMessage.title_root_project, PrefHelper.KEY_ROOT_PROJECT, db.tableProjects.queryRootProjectNames())
                    getLocation()
                }
                Stage.SUB_PROJECT -> {
                    titleUseCase.mainTitleVisible = true
                    prefHelper.projectRootName?.let { rootName ->
                        mainListUseCase.visible = true
                        titleUseCase.subTitleText = curProjectHint
                        buttonsUseCase.nextVisible = hasProjectSubName
                        setList(StringMessage.title_sub_project, PrefHelper.KEY_SUB_PROJECT, db.tableProjects.querySubProjectNames(rootName))
                    } ?: run {
                        curFlowValue = RootProjectFlow()
                    }
                }
            }
        }
    }
}