package com.b1097780.glucohub.ui.home.ActivityLog

import android.app.AlertDialog
import android.app.TimePickerDialog
import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.text.TextWatcher
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.b1097780.glucohub.R
import java.text.SimpleDateFormat
import java.util.*

class ActivityLogDialog(private val context: Context, private val callback: (String, String?, String?, String) -> Unit) {

    // Displays the main dialog to select an activity type
    fun show() {
        val activityTypes = listOf("Meal", "Insulin", "Activity", "Long-Acting Insulin", "Preset")

        val adapter = object : ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, activityTypes) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent) as TextView
                view.textSize = 18f
                view.setTypeface(null, Typeface.BOLD)
                view.setTextColor(getThemeColor(context, android.R.attr.textColorPrimary))
                return view
            }
        }

        val dialog = AlertDialog.Builder(context, R.style.CustomAlertDialogTheme)
            .setTitle("Log Activity")
            .setAdapter(adapter) { _, which ->
                if (activityTypes[which] == "Activity") {
                    showActivityInputDialog()
                }
            }
            .setNegativeButton("Cancel", null)
            .setPositiveButton("OK", null)
            .create()

        dialog.setOnShowListener { styleDialogButtons(dialog) }
        dialog.show()
    }


    // Displays the input dialog for entering activity details
    private fun showActivityInputDialog() {
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 40, 50, 40)
        }

        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val currentTime = timeFormat.format(Date())

        // Creates an underlined EditText for text input
        fun createUnderlinedEditText(hint: String, length: Int): EditText {
            return EditText(context).apply {
                this.hint = hint
                maxLines = 1
                filters = arrayOf(InputFilter.LengthFilter(length))
                inputType = InputType.TYPE_CLASS_TEXT
                gravity = Gravity.START
                background = null
                setPadding(10, 10, 10, 20)
                paintFlags = paintFlags or Paint.UNDERLINE_TEXT_FLAG
                setTextColor(getThemeColor(context, android.R.attr.textColorPrimary))
            }
        }

        // Creates an EditText for selecting time
        fun createTimeEditText(): EditText {
            return EditText(context).apply {
                setText(currentTime)
                maxLines = 1
                isFocusable = false
                inputType = InputType.TYPE_NULL
                setOnClickListener { showTimePicker(this, null) }
                background = null
                setPadding(10, 10, 10, 20)
                paintFlags = paintFlags or Paint.UNDERLINE_TEXT_FLAG
                setTextColor(getThemeColor(context, android.R.attr.textColorPrimary))
            }
        }

        val activityEditText = createUnderlinedEditText("Enter Activity", 10)
        val startTimeEditText = createTimeEditText()
        val separatorTextView = TextView(context).apply {
            text = " - "
            visibility = View.GONE
            setPadding(20, 0, 20, 0)
            setTextColor(getThemeColor(context, android.R.attr.textColorPrimary))
        }
        val endTimeEditText = createTimeEditText().apply {
            visibility = View.GONE
            isEnabled = false
            setOnClickListener { showTimePicker(this, startTimeEditText) }
        }

        // Creates a checkbox to enable/disable end time selection
        val enableEndTimeCheckbox = CheckBox(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(40, 0, 0, 0)
            }
        }
        val enableEndTimeLabel = TextView(context).apply {
            text = "Set Time Frame (e.g., 13:00 - 15:00)"
            setPadding(10, 0, 0, 0)
            setTextColor(getThemeColor(context, android.R.attr.textColorPrimary))
        }

        enableEndTimeCheckbox.setOnCheckedChangeListener { _, isChecked ->
            separatorTextView.visibility = if (isChecked) View.VISIBLE else View.GONE
            endTimeEditText.visibility = if (isChecked) View.VISIBLE else View.GONE
            endTimeEditText.isEnabled = isChecked
        }

        val detailsEditText = createUnderlinedEditText("Extra Info...", 17)

        layout.apply {
            addView(createLabeledRow("Name:", activityEditText))
            addView(createLabeledRow("Time:", startTimeEditText, separatorTextView, endTimeEditText))
            addView(LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(0, 10, 0, 30)
                addView(enableEndTimeCheckbox)
                addView(enableEndTimeLabel)
            })
            addView(createLabeledRow("Details:", detailsEditText))
        }

        val dialog = AlertDialog.Builder(context, R.style.CustomAlertDialogTheme)
            .setTitle("Log Activity")
            .setView(layout)
            .setPositiveButton("OK", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            styleDialogButtons(dialog)
            val okButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)

            // Set click listener for OK button to validate name input
            okButton.setOnClickListener {
                val name = activityEditText.text.toString().trim()
                val startTime = startTimeEditText.text.toString()
                val endTime = if (enableEndTimeCheckbox.isChecked) endTimeEditText.text.toString() else null
                val details = detailsEditText.text.toString().trim().takeIf { it.isNotEmpty() } // Allows null if empty

                if (name.isEmpty()) {
                    showAlert("Error", "Activity name cannot be empty")
                    return@setOnClickListener
                }

                // Close dialog and return values if validation is passed
                dialog.dismiss()
                callback(name, startTime, endTime, details ?: "")
            }
        }

        dialog.show()
    }

    // Shows an alert dialog with a given title and message
    private fun showAlert(title: String, message: String) {
        AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }

    // Creates a labeled row with text and input fields
    private fun createLabeledRow(label: String, vararg views: View): LinearLayout {
        return LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 0, 0, 20)
            addView(TextView(context).apply {
                text = label
                typeface = Typeface.DEFAULT_BOLD
                setPadding(0, 0, 20, 0)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                setTextColor(getThemeColor(context, android.R.attr.textColorPrimary))
            })
            views.forEach { addView(it) }
        }
    }

    // Displays a TimePickerDialog and updates the corresponding EditText
    private fun showTimePicker(editText: EditText, startTimeEditText: EditText?) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        TimePickerDialog(context, { _, selectedHour, selectedMinute ->
            val formattedTime = String.format("%02d:%02d", selectedHour, selectedMinute)

            if (startTimeEditText != null && formattedTime <= startTimeEditText.text.toString()) {
                editText.error = "End time must be later than start time."
                return@TimePickerDialog
            }

            editText.setText(formattedTime)
        }, hour, minute, true).show()
    }

// Styles the dialog buttons to match the theme
    private fun styleDialogButtons(dialog: AlertDialog) {
        val cancelButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
        val okButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)

        val textColorPrimary = getThemeColor(context, android.R.attr.textColorPrimary)
        val primaryColor = getThemeColor(context, android.R.attr.colorPrimary)

        cancelButton.setTextColor(textColorPrimary)
        cancelButton.setBackgroundColor(primaryColor)

        okButton.setTextColor(textColorPrimary)
        okButton.setBackgroundColor(primaryColor)
    }

    // Retrieves the primary theme color for text and UI elements
    private fun getThemeColor(context: Context, attr: Int): Int {
        val typedValue = TypedValue()
        context.theme.resolveAttribute(attr, typedValue, true)
        return typedValue.data
    }
}
