package com.cartlc.tracker.fresh.ui.daar.data

import androidx.annotation.StringRes
import com.cartlc.tracker.fresh.model.core.data.DataDaar

interface DaarUIData {

    enum class TimeOrDate {
        TIME,
        DATE
    }

    sealed class Stage(@StringRes val instruction: Int) {

        data class StageString(
                @StringRes private val i: Int,
                private val getValueFromItem: (item: DataDaar) -> String?,
                private val setValueOfItem: (item: DataDaar, value: String?) -> Unit
        ) : Stage(i) {
            var enteredValue: String? = null

            fun setItemValueFromStored(item: DataDaar) {
                setValueOfItem(item, enteredValue)
            }

            fun storeValueFromItem(item: DataDaar) {
                enteredValue = getValueFromItem(item)
            }

        }

        data class StageDate(
                @StringRes private val i: Int,
                private val timeOrDate: TimeOrDate,
                private val getValueFromItem: (item: DataDaar) -> Int,
                private val setValueOfItem: (item: DataDaar, value: Long) -> Unit
        ) : Stage(i) {
            var enteredValue: Long = 0

            fun setItemValueFromStored(item: DataDaar) {
                setValueOfItem(item, enteredValue)
            }

            fun storeValueFromItem(item: DataDaar) {
                enteredValue = getValueFromItem(item).toLong()
            }

            val isTime: Boolean
                get() = timeOrDate == TimeOrDate.TIME
        }

        data class StageProjectValue(
                var id: Long = 0,
                var desc: String? = null
        ) {
            companion object {
                fun get(item: DataDaar): StageProjectValue {
                    return StageProjectValue(item.projectNameId, item.projectDesc)
                }
            }

            val isReady: Boolean
                get() = id > 0 || !desc.isNullOrEmpty()
        }

        data class StageProject(
                @StringRes private val i: Int,
                private val getValueFromItem: (item: DataDaar) -> StageProjectValue,
                private val setValueOfItem: (item: DataDaar, value: StageProjectValue) -> Unit
        ) : Stage(i) {

            var enteredValue: StageProjectValue = StageProjectValue()

            fun setItemValueFromStored(item: DataDaar) {
                setValueOfItem(item, enteredValue)
            }

            fun storeValue(projectId: Long) {
                enteredValue = StageProjectValue(projectId, null)
            }

            fun storeValueFromItem(item: DataDaar) {
                enteredValue = getValueFromItem(item)
            }
        }
    }

    val isFirst: Boolean
    val isLast: Boolean
    val isStageInTime: Boolean
    val isComplete: Boolean
    fun isComplete(stage: Stage): Boolean

    val curStage: Stage?
    val projectStage: Stage.StageProject
    val hasNext: Boolean
    val hasPrev: Boolean
    val hasSave: Boolean

    var data: DataDaar

    fun clear()
    fun first()
    fun prev()
    fun next()

    fun storeValueToStage(value: String?)
    fun storeValueToStage(value: Long): String?
    fun getStringValueOf(stage: Stage): String?

}
