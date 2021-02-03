package com.cartlc.tracker.fresh.ui.daar

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cartlc.tracker.R
import com.cartlc.tracker.fresh.ui.buttons.ButtonsView
import com.cartlc.tracker.fresh.ui.buttons.ButtonsViewMvc
import com.cartlc.tracker.fresh.ui.common.viewmvc.ObservableViewMvcImpl
import com.cartlc.tracker.fresh.ui.daar.DaarViewMvc.ProjectSelect
import com.cartlc.tracker.fresh.ui.title.TitleView
import com.cartlc.tracker.fresh.ui.title.TitleViewMvc
import java.util.*

class DaarViewMvcImpl(
        private val inflater: LayoutInflater,
        container: ViewGroup?
) : ObservableViewMvcImpl<DaarViewMvc.Listener>(),
        DaarViewMvc,
        DatePickerDialog.OnDateSetListener,
        TimePickerDialog.OnTimeSetListener,
        DaarProjectAdapter.Listener {

    override val rootView: View = inflater.inflate(R.layout.content_daar, container, false) as ViewGroup

    private val instructionView = findViewById<TextView>(R.id.instruction)
    private val entryEditText = findViewById<EditText>(R.id.entry)
    private val timeDateDisplay = findViewById<Button>(R.id.timeDateDisplay)
    private val titleView = findViewById<TitleView>(R.id.frame_title)
    private val buttonsView = findViewById<ButtonsView>(R.id.frame_buttons)
    private val projectList = findViewById<RecyclerView>(R.id.projectList)
    private val projectSelect = findViewById<RadioGroup>(R.id.projectSelect)
    private val projectRecent = findViewById<RadioButton>(R.id.projectRecent)
    private val projectAll = findViewById<RadioButton>(R.id.projectAll)
    private val projectOther = findViewById<RadioButton>(R.id.projectOther)

    init {
        entryEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val value = s.toString()
                for (listener in listeners) {
                    listener.editTextAfterTextChanged(value)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })
        entryEditText.setOnEditorActionListener { _, _, _ ->
            listeners.forEach { it.onEditTextReturn() }
            false
        }
        timeDateDisplay.setOnClickListener { listeners.forEach { it.onDateButtonClicked() } }
        projectList.layoutManager = LinearLayoutManager(context)
        projectRecent.setOnCheckedChangeListener { _, isChecked -> onProjectSelected(ProjectSelect.PROJECT_RECENT, isChecked) }
        projectAll.setOnCheckedChangeListener { _, isChecked -> onProjectSelected(ProjectSelect.PROJECT_ALL, isChecked) }
        projectOther.setOnCheckedChangeListener { _, isChecked -> onProjectSelected(ProjectSelect.PROJECT_OTHER, isChecked) }
    }

    private fun onProjectSelected(select: ProjectSelect, isChecked: Boolean) {
        if (isChecked) {
            listeners.forEach { it.onProjectTypeSelected(select) }
        }
    }

    // region DaarViewMvc

    override val titleViewMvc: TitleViewMvc
        get() = titleView.viewMvc

    override val buttonsViewMvc: ButtonsViewMvc
        get() = buttonsView.viewMvc

    override var buttonsViewVisible: Boolean
        get() = buttonsView.visibility == View.VISIBLE
        set(value) {
            buttonsView.visibility = if (value) View.VISIBLE else View.GONE
        }

    override fun setInstruction(text: Int) {
        instructionView.setText(text)
    }

    override var entryEditTextValue: String?
        get() = entryEditText.text.toString().trim { it <= ' ' }
        set(value) {
            entryEditText.setText(value)
        }

    override var timeDateVisible: Boolean
        get() = timeDateDisplay.visibility == View.VISIBLE
        set(value) {
            timeDateDisplay.visibility = if (value) View.VISIBLE else View.GONE
        }

    override var entryVisible: Boolean
        get() = entryEditText.visibility == View.VISIBLE
        set(value) {
            entryEditText.visibility = if (value) View.VISIBLE else View.GONE
        }

    override var timeDateTextValue: String?
        get() = timeDateDisplay.text.toString()
        set(value) {
            timeDateDisplay.text = value
        }

    override fun invokeDatePicker() {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val currentMonth = Calendar.getInstance().get(Calendar.MONTH)
        val currentDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(rootView.context, this, currentYear, currentMonth, currentDay).show()
    }

    override fun invokeTimePicker() {
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val currentMinute = Calendar.getInstance().get(Calendar.MINUTE)

        TimePickerDialog(rootView.context, this, currentHour, currentMinute, false).show()
    }

    override var projectListVisible: Boolean
        get() = projectList.visibility == View.VISIBLE
        set(value) {
            projectList.visibility = if (value) View.VISIBLE else View.GONE
        }

    override var projectSelectVisible: Boolean
        get() = projectSelect.visibility == View.VISIBLE
        set(value) {
            projectSelect.visibility = if (value) View.VISIBLE else View.GONE
        }

    override var projectsSelectRecentEnabled: Boolean
        get() = projectRecent.isEnabled
        set(value) {
            projectRecent.isEnabled = value
        }

    override var projectsSelectAllEnabled: Boolean
        get() = projectAll.isEnabled
        set(value) {
            projectAll.isEnabled = value
        }

    override var projectSelectWhich: ProjectSelect
        get() {
            return when {
                projectRecent.isChecked -> ProjectSelect.PROJECT_RECENT
                projectAll.isChecked -> ProjectSelect.PROJECT_ALL
                else -> ProjectSelect.PROJECT_OTHER
            }
        }
        set(value) {
            when (value) {
                ProjectSelect.PROJECT_RECENT -> {
                    projectRecent.isChecked = true
                    projectAll.isChecked = false
                    projectOther.isChecked = false
                }
                ProjectSelect.PROJECT_ALL -> {
                    projectRecent.isChecked = false
                    projectAll.isChecked = true
                    projectOther.isChecked = false
                }
                ProjectSelect.PROJECT_OTHER -> {
                    projectRecent.isChecked = false
                    projectAll.isChecked = false
                    projectOther.isChecked = true
                }
            }
        }

    override fun prepareProjectList(items: List<String>) {
        projectList.adapter = DaarProjectAdapter(inflater, items, this)
    }

    override fun refreshProjectList() {
        projectList.adapter?.notifyDataSetChanged()
    }

    // endregion DaarViewMvc

    // region DaarProjectAdapter.Listener

    override fun onProjectItemSelected(position: Int, item: String) {
        listeners.forEach { it.onProjectItemSelected(position, item) }
    }

    override fun isSelected(position: Int, item: String): Boolean {
        return listeners.firstOrNull()?.isProjectSelected(position, item) ?: false
    }

    // endregion DaarProjectAdapter.Listener

    // region DatePickerDialog.OnDateSetListener

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        val c = Calendar.getInstance()
        c.set(year, month, dayOfMonth)
        val date = c.timeInMillis
        listeners.forEach { it.onDateEntered(date) }
    }

    // endregion DatePickerDialog.OnDateSetListener

    // region TimePickerDialog.onTimeSetListener

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        val gc = GregorianCalendar()
        gc.add(Calendar.DATE, 1)
        gc.set(Calendar.HOUR_OF_DAY, hourOfDay)
        gc.set(Calendar.MINUTE, minute)
        listeners.forEach { it.onTimeEntered(gc.timeInMillis) }
    }

    // endregion TimePickerDialog.onTimeSetListener
}
