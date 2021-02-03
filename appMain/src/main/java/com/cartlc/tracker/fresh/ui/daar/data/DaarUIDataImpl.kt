package com.cartlc.tracker.fresh.ui.daar.data

import com.cartlc.tracker.R
import com.cartlc.tracker.R.string.daar_instruction_missed_units
import com.cartlc.tracker.fresh.model.core.data.DataDaar
import com.cartlc.tracker.fresh.model.core.table.DatabaseTable
import com.cartlc.tracker.fresh.model.msg.MessageHandler
import com.cartlc.tracker.fresh.model.msg.StringMessage
import com.cartlc.tracker.fresh.ui.common.DateUtil
import com.cartlc.tracker.fresh.ui.daar.data.DaarUIData.Stage
import com.cartlc.tracker.fresh.ui.daar.data.DaarUIData.Stage.*
import com.cartlc.tracker.fresh.ui.daar.data.DaarUIData.TimeOrDate
import timber.log.Timber

class DaarUIDataImpl(
        private val dm: DatabaseTable,
        private val messageHandler: MessageHandler
) : DaarUIData {

    private val stages = listOf(
            StageDate(R.string.daar_instruction_date, TimeOrDate.DATE, { it.date.toInt() }) { item, value -> item.date = value },
            StageProject(R.string.daar_instruction_project, { StageProjectValue.get(it) }) { item, value -> set(item, value) },
            StageString(R.string.daar_instruction_work_completed, { it.workCompleted }) { item, value -> item.workCompleted = value },
            StageString(daar_instruction_missed_units, { it.missedUnits }) { item, value -> item.missedUnits = value },
            StageString(R.string.daar_instruction_issues, { it.issues }) { item, value -> item.issues = value },
            StageString(R.string.daar_instruction_injuries, { it.injuries }) { item, value -> item.injuries = value },
            StageDate(R.string.daar_instruction_starting_time_tomorrow, TimeOrDate.TIME, { it.startTimeTomorrow.toInt() }) { item, value -> item.startTimeTomorrow = value }
    )

    private fun set(item: DataDaar, value: StageProjectValue) {
        item.projectNameId = value.id
        item.projectDesc = value.desc
    }

    private var curIndex = 0
    private var curDataId: Long = 0

    // region DaarData

    override val hasNext: Boolean
        get() = curIndex < stages.size - 1

    override val hasPrev: Boolean
        get() = curIndex > 0

    override val hasSave: Boolean
        get() = isComplete

    override val isComplete: Boolean
        get() = stages.all { isComplete(it) }

    override fun isComplete(stage: Stage): Boolean {
        return when (stage) {
            is StageDate -> stage.enteredValue > 0
            is StageString -> !stage.enteredValue.isNullOrBlank()
            is StageProject -> stage.enteredValue.isReady ?: false
        }
    }

    override val curStage: Stage?
        get() = if (curIndex < stages.size) stages[curIndex] else null

    override val projectStage: StageProject
        get() {
            return stages.filterIsInstance<StageProject>().first()
        }

    override fun clear() {
        stages.forEach {
            when (it) {
                is StageDate -> it.enteredValue = 0L
                is StageString -> it.enteredValue = null
                is StageProject -> it.enteredValue = StageProjectValue()
            }
        }
    }

    override fun first() {
        curIndex = 0
    }

    override val isFirst: Boolean
        get() = curIndex == 0

    override val isLast: Boolean
        get() = curIndex >= stages.size-1

    override fun prev() {
        if (curIndex > 0) {
            curIndex--
        }
    }

    override fun next() {
        curIndex++
    }

    override val isStageInTime: Boolean
        get() {
            val stage = curStage ?: return false
            if (stage is StageDate) {
                return stage.isTime
            }
            return false
        }

    override fun storeValueToStage(value: String?) {
        val stage = curStage ?: return
        when (stage) {
            is StageString -> {
                stage.enteredValue = value
            }
            is StageProject -> {
                stage.enteredValue.desc = value
            }
            else -> {
                Timber.e("current value is not a string")
            }
        }
    }

    override fun storeValueToStage(value: Long): String? {
        val stage = curStage ?: return ""
        return when (stage) {
            is StageDate -> {
                stage.enteredValue = value
                getStringValueOf(stage)
            }
            is StageProject -> {
                stage.enteredValue.id = value
                getStringValueOf(stage)
            }
            else -> {
                Timber.e("current value is not a long")
                ""
            }
        }
    }

    override fun getStringValueOf(stage: Stage): String? {
        return when (stage) {
            is StageDate -> {
                when {
                    stage.enteredValue == 0L -> {
                        messageHandler.getString(if (stage.isTime) StringMessage.daar_entry_time else StringMessage.daar_entry_date)
                    }
                    stage.isTime -> {
                        DateUtil.getTimeString(stage.enteredValue)
                    }
                    else -> {
                        DateUtil.getDateString(stage.enteredValue)
                    }
                }
            }
            is StageString -> {
                stage.enteredValue
            }
            is StageProject -> {
                stage.enteredValue.let { value ->
                    if (value.id > 0) {
                        dm.tableProjects.queryById(value.id)?.dashName
                    } else {
                        value.desc
                    }
                } ?: ""
            }
        }
    }

    override var data: DataDaar
        get() {
            val item = DataDaar(dm)
            stages.forEach { stage ->
                when (stage) {
                    is StageDate -> { stage.setItemValueFromStored(item) }
                    is StageString -> { stage.setItemValueFromStored(item) }
                    is StageProject -> { stage.setItemValueFromStored(item) }
                }
            }
            item.id = curDataId
            return item
        }
        set(item) {
            curDataId = item.id
            stages.forEach { stage ->
                when (stage) {
                    is StageDate -> { stage.storeValueFromItem(item) }
                    is StageString -> { stage.storeValueFromItem(item) }
                    is StageProject -> { stage.storeValueFromItem(item) }
                }
            }
        }

    // endregion DaarData


}
