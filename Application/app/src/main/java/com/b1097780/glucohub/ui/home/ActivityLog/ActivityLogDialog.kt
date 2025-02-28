package com.b1097780.glucohub.ui.home.ActivityLog

import android.app.AlertDialog
import android.app.TimePickerDialog
import android.content.Context
import android.graphics.Typeface
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.b1097780.glucohub.R
import java.text.SimpleDateFormat
import java.util.*

class ActivityLogDialog(private val context: Context, private val callback: (String, String, String?, String) -> Unit) {

    fun show() {
        val activityTypes = mutableListOf("Meal", "Insulin", "Activity", "Long-Acting Insulin", "Custom")

        val adapter = android.widget.ArrayAdapter(
            context,
            android.R.layout.simple_list_item_1,
            activityTypes
        ).apply {
            setDropDownViewResource(android.R.layout.simple_list_item_1)
        }

        val dialog = AlertDialog.Builder(context, R.style.CustomAlertDialogTheme)
            .setTitle("Log Activity")
            .setAdapter(adapter) { _, which ->
                when (activityTypes[which]) {
                    "Activity" -> showActivityInputDialog() // Calls the existing Activity dialog
                }
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .create()

        dialog.show()

    }



    private fun showActivityInputDialog() {
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 40, 50, 40)
        }

        // 🔹 Name Label + Input (Max 12 Characters)
        val nameLayout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        val nameLabel = EditText(context).apply {
            setText("Name:")
            isEnabled = false
            setBackgroundColor(context.getColor(android.R.color.transparent))
        }

        val activityEditText = EditText(context).apply {
            hint = "                    "
            maxLines = 1
            filters = arrayOf(android.text.InputFilter.LengthFilter(12)) // Limit to 12 characters
        }

        nameLayout.addView(nameLabel)
        nameLayout.addView(activityEditText)

        // 🔹 Time Label + Input (Checkbox below)
        val timeLayout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        val timeLabel = EditText(context).apply {
            setText("Time:")
            isEnabled = false
            setBackgroundColor(context.getColor(android.R.color.transparent))
        }

        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val currentTime = timeFormat.format(Date())

        val startTimeEditText = EditText(context).apply {
            setText(currentTime)
            hint = "Time"
            isFocusable = false
            setOnClickListener { showTimePicker(this, null) }
        }

        val separatorTextView = EditText(context).apply {
            setText(" - ")
            isEnabled = false
            visibility = View.GONE // Initially hidden
        }

        val endTimeEditText = EditText(context).apply {
            setText(currentTime)
            hint = "End Time"
            isFocusable = false
            isEnabled = false // Disabled until checkbox is checked
            visibility = View.GONE // Initially hidden
            setOnClickListener { showTimePicker(this, startTimeEditText) }
        }

        val timeInputLayout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            addView(startTimeEditText)
            addView(separatorTextView)
            addView(endTimeEditText)
        }

        val enableEndTimeCheckbox = CheckBox(context).apply {
            text = "Set Time Frame (e.g., 13:00 - 15:00)"
        }

        enableEndTimeCheckbox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                startTimeEditText.hint = "Start Time"
                separatorTextView.visibility = View.VISIBLE
                endTimeEditText.visibility = View.VISIBLE
                endTimeEditText.isEnabled = true
            } else {
                startTimeEditText.hint = "Time"
                separatorTextView.visibility = View.GONE
                endTimeEditText.visibility = View.GONE
                endTimeEditText.isEnabled = false
            }
        }

        timeLayout.addView(timeLabel)
        timeLayout.addView(timeInputLayout)

        // 🔹 Description Label + Input (Max 18 Characters)
        val descriptionLayout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        val descriptionLabel = EditText(context).apply {
            setText("Description:")
            isEnabled = false
            setBackgroundColor(context.getColor(android.R.color.transparent))
        }

        val descriptionEditText = EditText(context).apply {
            hint = "Details..."
            maxLines = 1
            filters = arrayOf(android.text.InputFilter.LengthFilter(18)) // Limit to 18 characters
        }

        descriptionLayout.addView(descriptionLabel)
        descriptionLayout.addView(descriptionEditText)

        // 🔹 Add Everything to Layout
        layout.addView(nameLayout)
        layout.addView(timeLayout)
        layout.addView(enableEndTimeCheckbox) // Checkbox should be under time
        layout.addView(descriptionLayout)

        // 🔹 Create Dialog
        val dialog = AlertDialog.Builder(context, R.style.CustomAlertDialogTheme)
            .setTitle("Log Activity")
            .setView(layout)
            .setPositiveButton("OK", null)
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .create()

        // Handle "OK" Button Click
        dialog.setOnShowListener {
            val okButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            okButton.setOnClickListener {
                val activity = activityEditText.text.toString().trim()
                val startTime = startTimeEditText.text.toString()
                val endTime = if (enableEndTimeCheckbox.isChecked) endTimeEditText.text.toString() else null
                val description = descriptionEditText.text.toString().trim()

                if (activity.isEmpty()) {
                    activityEditText.error = "Activity name cannot be empty."
                    return@setOnClickListener
                }

                if (enableEndTimeCheckbox.isChecked && startTime >= endTime!!) {
                    endTimeEditText.error = "End time must be later than start time."
                    return@setOnClickListener
                }

                // 🔥 Pass Data Back Using Callback
                callback(activity, startTime, endTime, description)

                dialog.dismiss()
            }
        }
        dialog.show()
    }








    private fun showTimePicker(editText: EditText, startTimeEditText: EditText?) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(
            context,
            { _, selectedHour, selectedMinute ->
                val formattedTime = String.format("%02d:%02d", selectedHour, selectedMinute)

                // If selecting End Time, ensure it's after Start Time
                if (startTimeEditText != null) {
                    val startTime = startTimeEditText.text.toString()
                    if (startTime.isNotEmpty() && formattedTime <= startTime) {
                        editText.error = "End time must be later than start time."
                        return@TimePickerDialog
                    }
                }

                editText.setText(formattedTime)
            },
            hour, minute, true
        )

        timePickerDialog.show()
    }

}
