package com.b1097780.glucohub.ui.home.ActivityLog

import android.app.AlertDialog
import android.app.TimePickerDialog
import android.content.Context
import android.graphics.Typeface
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.b1097780.glucohub.R
import java.text.SimpleDateFormat
import java.util.*

class ActivityLogDialog(private val context: Context, private val callback: (String, String?, String?, String) -> Unit) {

    fun show() {
        val activityTypes = mutableListOf("Meal", "Insulin", "Activity", "Long-Acting Insulin", "Custom")

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
                when (activityTypes[which]) {
                    "Activity" -> showActivityInputDialog(context)
                }
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            styleDialogButtons(dialog)
        }

        dialog.show()
    }

    private fun showActivityInputDialog(context: Context) {
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 30, 40, 30) // ✅ Reduced padding for better alignment
        }

        val primaryColor = getThemeColor(context, com.google.android.material.R.attr.colorPrimary)
        val textColorPrimary = getThemeColor(context, android.R.attr.textColorPrimary)

        // Name Section
        val nameLayout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        val nameLabel = TextView(context).apply {
            text = "Name:"
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(textColorPrimary)
            setPadding(0, 0, 20, 0) // ✅ Adds a small right margin instead of weight
        }

        val activityEditText = EditText(context).apply {
            hint = "Enter Activity"
            maxLines = 1
            filters = arrayOf(android.text.InputFilter.LengthFilter(12))
            setHintTextColor(primaryColor)
            setTextColor(textColorPrimary)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f) // ✅ Only input takes space
        }

        nameLayout.addView(nameLabel)
        nameLayout.addView(activityEditText)

        // Time Section
        val timeLayout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 10, 0, 0) // ✅ Added spacing only above time section
        }

        val timeLabel = TextView(context).apply {
            text = "Time:"
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(textColorPrimary)
            setPadding(0, 0, 20, 0) // ✅ Adds a small right margin instead of weight
        }

        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val currentTime = timeFormat.format(Date())

        val startTimeEditText = EditText(context).apply {
            setText(currentTime)
            hint = "Time"
            isFocusable = false
            setHintTextColor(primaryColor)
            setTextColor(textColorPrimary)
            setOnClickListener { showTimePicker(context, this, null) }
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f) // ✅ Takes available space
        }

        val separatorTextView = TextView(context).apply {
            text = " - "
            visibility = View.GONE
            setTextColor(textColorPrimary)
            setPadding(10, 0, 10, 0) // ✅ Adds minimal spacing between times
        }

        val endTimeEditText = EditText(context).apply {
            setText(currentTime)
            hint = "End Time"
            isFocusable = false
            isEnabled = false
            visibility = View.GONE
            setHintTextColor(primaryColor)
            setTextColor(textColorPrimary)
            setOnClickListener { showTimePicker(context, this, startTimeEditText) }
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val timeInputLayout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            addView(startTimeEditText)
            addView(separatorTextView)
            addView(endTimeEditText)
        }

        val enableEndTimeCheckbox = CheckBox(context).apply {
            text = "Set Time Frame (e.g., 13:00 - 15:00)"
            setTextColor(textColorPrimary)
            setPadding(0, 10, 0, 0) // ✅ Adds space above checkbox only
        }

        enableEndTimeCheckbox.setOnCheckedChangeListener { _, isChecked ->
            separatorTextView.visibility = if (isChecked) View.VISIBLE else View.GONE
            endTimeEditText.visibility = if (isChecked) View.VISIBLE else View.GONE
            endTimeEditText.isEnabled = isChecked
        }

        timeLayout.addView(timeLabel)
        timeLayout.addView(timeInputLayout)

        // Description Section
        val descriptionLayout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 10, 0, 0) // ✅ Adds space above description
        }

        val descriptionLabel = TextView(context).apply {
            text = "Description:"
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(textColorPrimary)
            setPadding(0, 0, 20, 0) // ✅ Adds a small right margin
        }

        val descriptionEditText = EditText(context).apply {
            hint = "Details..."
            maxLines = 1
            filters = arrayOf(android.text.InputFilter.LengthFilter(18))
            setHintTextColor(primaryColor)
            setTextColor(textColorPrimary)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f) // ✅ Takes available space
        }

        descriptionLayout.addView(descriptionLabel)
        descriptionLayout.addView(descriptionEditText)

        // Add Everything to Layout
        layout.addView(nameLayout)
        layout.addView(timeLayout)
        layout.addView(enableEndTimeCheckbox)
        layout.addView(descriptionLayout)

        val dialog = AlertDialog.Builder(context, R.style.CustomAlertDialogTheme)
            .setTitle("Log Activity")
            .setView(layout)
            .setPositiveButton("OK", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            styleDialogButtons(dialog)
        }

        dialog.show()
    }



    private fun showTimePicker(context: Context, editText: EditText, startTimeEditText: EditText?) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(
            context,
            { _, selectedHour, selectedMinute ->
                val formattedTime = String.format("%02d:%02d", selectedHour, selectedMinute)

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

    private fun getThemeColor(context: Context, attr: Int): Int {
        val typedValue = TypedValue()
        val theme = context.theme
        theme.resolveAttribute(attr, typedValue, true)
        return typedValue.data
    }

    private fun styleDialogButtons(dialog: AlertDialog) {
        val cancelButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
        val okButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)

        val textColorPrimary = getThemeColor(dialog.context, android.R.attr.textColorPrimary)
        val primaryColor = getThemeColor(dialog.context, com.google.android.material.R.attr.colorPrimary)

        cancelButton.setTextColor(textColorPrimary)
        cancelButton.setBackgroundColor(primaryColor)

        okButton.setTextColor(textColorPrimary)
        okButton.setBackgroundColor(primaryColor)
    }
}
