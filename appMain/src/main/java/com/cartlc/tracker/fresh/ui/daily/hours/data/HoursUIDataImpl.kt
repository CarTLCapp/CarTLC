package com.cartlc.tracker.fresh.ui.daily.hours.data

import com.cartlc.tracker.R
import com.cartlc.tracker.fresh.model.core.data.DataHours
import com.cartlc.tracker.fresh.model.core.table.DatabaseTable
import com.cartlc.tracker.fresh.model.msg.MessageHandler
import com.cartlc.tracker.fresh.model.msg.StringMessage
import com.cartlc.tracker.fresh.ui.daily.hours.data.HoursUIData.Stage
import com.cartlc.tracker.fresh.ui.daily.hours.data.HoursUIData.Stage.*
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

class HoursUIDataImpl(
    private val dm: DatabaseTable,
    private val messageHandler: MessageHandler,
) : HoursUIData {

    companion object {
        private const val DAY_FORMAT = "yyyy-MM-dd"
        private const val WORKED_FORMAT = "hh:mm aa"
    }

    private val stages = listOf(
        StageDay(R.string.hours_instruction_date, { it.date }) { item, value -> item.date = value },
        StageProject(
            R.string.hours_instruction_project,
            { StageProjectValue.get(it) }) { item, value -> set(item, value) },
        StageHoursWorked(
            R.string.hours_instruction_worked,
            { Pair(it.startTime, it.endTime) }) { item, value ->
            item.startTime = value.first; item.endTime = value.second
        },
        StageBreak(
            R.string.hours_instruction_lunch,
            15,
            9,
            { it.lunchTime }) { item, value -> item.lunchTime = value },
        StageBreak(
            R.string.hours_instruction_break,
            15,
            5,
            { it.breakTime }) { item, value -> item.breakTime = value },
        StageBreak(
            R.string.hours_instruction_drive,
            30,
            21,
            { it.driveTime }) { item, value -> item.driveTime = value },
        StageString(R.string.hours_instruction_notes, { it.notes }) { item, value ->
            item.notes = value
        }
    )

    private fun set(item: DataHours, value: StageProjectValue) {
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
            is StageDay -> stage.enteredValue > 0
            is StageBreak -> stage.enteredValue >= 0
            is StageString -> !stage.enteredValue.isNullOrBlank()
            is StageProject -> stage.enteredValue.isReady
            is StageHoursWorked -> stage.enteredValue.first >= 0 && stage.enteredValue.second > stage.enteredValue.first
        }
    }

    override val curStage: Stage?
        get() = if (curIndex < stages.size) stages[curIndex] else null

    override fun clear() {
        stages.forEach {
            when (it) {
                is StageDay -> it.enteredValue = 0L
                is StageString -> it.enteredValue = null
                is StageProject -> it.enteredValue = StageProjectValue()
                is StageBreak -> it.enteredValue = -1
                is StageHoursWorked -> it.enteredValue = Pair(0, 0)
            }
        }
    }

    override fun first() {
        curIndex = 0
    }

    override val isFirst: Boolean
        get() = curIndex == 0

    override val isLast: Boolean
        get() = curIndex >= stages.size - 1

    override fun prev() {
        if (curIndex > 0) {
            curIndex--
        }
    }

    override fun next() {
        curIndex++
    }

    override fun storeValueToStage(value: String?) {
        val stage = curStage ?: return
        when (stage) {
            is StageString -> {
                stage.enteredValue = value
            }
            else -> {
                Timber.e("current value is not a string")
            }
        }
    }

    override fun storeValueToStage(value: Long): String? {
        val stage = curStage ?: return ""
        return when (stage) {
            is StageBreak -> {
                stage.enteredValue = value.toInt()
                getStringValueOf(stage)
            }
            is StageDay -> {
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

    override fun storeValueToStage(start: Int, end: Int): String? {
        val stage = curStage ?: return ""
        return when (stage) {
            is StageHoursWorked -> {
                stage.enteredValue = Pair(start, end)
                getStringValueOf(stage)
            }
            else -> {
                Timber.e("current value is not a long")
                ""
            }
        }
    }

    override fun getStringValueOf(stage: Stage, which: Int): String? {
        return when (stage) {
            is StageDay -> {
                toDayString(stage.enteredValue)
            }
            is StageString -> {
                stage.enteredValue
            }
            is StageProject -> {
                stage.enteredValue.let { value ->
                    if (value.id > 0) {
                        dm.tableProjects.queryById(value.id)?.dashName
                    } else {
                        ""
                    }
                } ?: ""
            }
            is StageBreak -> toBreakString(stage.enteredValue)
            is StageHoursWorked -> {
                messageHandler.getString(
                    StringMessage.hours_time_start_hint_value(
                        if (which == 0)
                            toWorkedString(stage.enteredValue.first)
                        else
                            toWorkedString(stage.enteredValue.second)
                    )
                )
            }
        }
    }

    override var data: DataHours
        get() {
            val item = DataHours(dm)
            stages.forEach { stage ->
                when (stage) {
                    is StageDay -> stage.setItemValueFromStored(item)
                    is StageString -> stage.setItemValueFromStored(item)
                    is StageProject -> stage.setItemValueFromStored(item)
                    is StageBreak -> stage.setItemValueFromStored(item)
                    is StageHoursWorked -> stage.setItemValueFromStored(item)
                }
            }
            item.id = curDataId
            return item
        }
        set(item) {
            curDataId = item.id
            stages.forEach { stage ->
                when (stage) {
                    is StageDay -> stage.storeValueFromItem(item)
                    is StageString -> stage.storeValueFromItem(item)
                    is StageProject -> stage.storeValueFromItem(item)
                    is StageBreak -> stage.storeValueFromItem(item)
                    is StageHoursWorked -> stage.storeValueFromItem(item)
                }
            }
        }

    // endregion HoursData

    private fun toDayString(value: Long): String {
        return SimpleDateFormat(DAY_FORMAT, Locale.getDefault()).format(Date(value))
    }

    private fun toWorkedString(value: Int): String {
        return SimpleDateFormat(WORKED_FORMAT, Locale.getDefault()).format(Date(value.toLong()))
    }

    private fun toBreakString(value: Int): String {
        return if (value > 60) {
            String.format("%d HOURS %d MINS", value / 60, value % 60)
        } else {
            String.format("%d MINS", value)
        }
    }
}
