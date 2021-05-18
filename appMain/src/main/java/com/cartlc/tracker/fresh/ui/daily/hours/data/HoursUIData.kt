package com.cartlc.tracker.fresh.ui.daily.hours.data

import androidx.annotation.StringRes
import com.cartlc.tracker.fresh.model.core.data.DataHours
import com.cartlc.tracker.fresh.model.msg.MessageHandler
import com.cartlc.tracker.fresh.model.msg.StringMessage

interface HoursUIData {

    companion object {
        fun toBreakString(value: Int): String {
            return when {
                value < 0 -> ""
                value > 60 -> {
                    String.format("%d HOURS %d MINS", value / 60, value % 60)
                }
                else -> {
                    String.format("%d MINS", value)
                }
            }
        }
    }

    sealed class Stage(@StringRes val instruction: Int) {

        data class StageString(
            @StringRes private val i: Int,
            private val getValueFromItem: (item: DataHours) -> String?,
            private val setValueOfItem: (item: DataHours, value: String?) -> Unit
        ) : Stage(i) {
            var enteredValue: String? = null

            fun setItemValueFromStored(item: DataHours) {
                setValueOfItem(item, enteredValue)
            }

            fun storeValueFromItem(item: DataHours) {
                enteredValue = getValueFromItem(item)
            }

        }

        data class StageDay(
            @StringRes private val i: Int,
            private val getValueFromItem: (item: DataHours) -> Long,
            private val setValueOfItem: (item: DataHours, value: Long) -> Unit
        ) : Stage(i) {
            var enteredValue: Long = 0

            fun setItemValueFromStored(item: DataHours) {
                setValueOfItem(item, enteredValue)
            }

            fun storeValueFromItem(item: DataHours) {
                enteredValue = getValueFromItem(item)
            }

        }

        data class StageHoursWorked(
            @StringRes private val i: Int,
            private val getValueFromItem: (item: DataHours) -> Pair<Int, Int>,
            private val setValueOfItem: (item: DataHours, value: Pair<Int, Int>) -> Unit
        ) : Stage(i) {

            var enteredValue = Pair(0, 0)

            fun setItemValueFromStored(item: DataHours) {
                setValueOfItem(item, enteredValue)
            }

            fun storeValueFromItem(item: DataHours) {
                enteredValue = getValueFromItem(item)
            }
        }

        data class StageBreak(
            @StringRes private val i: Int,
            val intervalInMinutes: Int,
            val numIntervals: Int,
            private val getValueFromItem: (item: DataHours) -> Int,
            private val setValueOfItem: (item: DataHours, value: Int) -> Unit
        ) : Stage(i) {

            var enteredValue = -1

            fun setItemValueFromStored(item: DataHours) {
                setValueOfItem(item, enteredValue)
            }

            fun storeValueFromItem(item: DataHours) {
                enteredValue = getValueFromItem(item)
            }

            private val intValues = mutableListOf<Int>()

            fun stringList(messageHandler: MessageHandler): List<String> {
                val stringValues = mutableListOf<String>()
                for (ele in list) {
                    if (ele == 0) {
                        stringValues.add(messageHandler.getString(StringMessage.hours_time_none))
                    } else {
                        stringValues.add(toBreakString(ele))
                    }
                }
                return stringValues
            }

            val list: List<Int>
                get() {
                    if (intValues.isNotEmpty()) {
                        return intValues
                    }
                    intValues.add(0)
                    var minutes = 0
                    for (i in 0..numIntervals) {
                        minutes += intervalInMinutes
                        intValues.add(minutes)
                    }
                    return intValues
                }

            override fun toString(): String {
                return toBreakString(enteredValue)
            }
        }

        data class StageProjectValue(
            var id: Long = 0,
            var desc: String? = null
        ) {
            companion object {
                fun get(item: DataHours): StageProjectValue {
                    return StageProjectValue(item.projectNameId, item.projectDesc)
                }
            }

            val isReady: Boolean
                get() = id > 0
        }

        data class StageProject(
            @StringRes private val i: Int,
            private val getValueFromItem: (item: DataHours) -> StageProjectValue,
            private val setValueOfItem: (item: DataHours, value: StageProjectValue) -> Unit
        ) : Stage(i) {

            var enteredValue: StageProjectValue = StageProjectValue()

            fun setItemValueFromStored(item: DataHours) {
                setValueOfItem(item, enteredValue)
            }

            fun storeValue(projectId: Long) {
                enteredValue = StageProjectValue(projectId)
            }

            fun storeValueFromItem(item: DataHours) {
                enteredValue = getValueFromItem(item)
            }
        }
    }

    val isFirst: Boolean
    val isLast: Boolean
    val isComplete: Boolean
    fun isComplete(stage: Stage): Boolean

    val curStage: Stage?
    val hasNext: Boolean
    val hasPrev: Boolean
    val hasSave: Boolean

    var data: DataHours

    fun clear()
    fun first()
    fun prev()
    fun next()

    fun storeValueToStage(value: String?)
    fun storeValueToStage(value: Long): String?
    fun storeValueToStage(start: Int, end: Int): String?
    fun getStringValueOf(stage: Stage, which: Int = 0): String?

}
