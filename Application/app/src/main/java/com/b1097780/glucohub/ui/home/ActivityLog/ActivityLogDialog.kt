package com.b1097780.glucohub.ui.home.ActivityLog

import android.app.AlertDialog
import android.app.TimePickerDialog
import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.text.InputFilter
import android.text.InputType
import android.util.TypedValue
import android.view.Gravity
import android.view.View

import android.widget.*
import com.b1097780.glucohub.R
import java.text.SimpleDateFormat
import java.util.*

class ActivityLogDialog(private val context: Context, private val callback: (String, String?, String?, String) -> Unit) {

    private var mainDialog: AlertDialog? = null

    // ✅ Displays the main dialog to select an activity type
    fun show() {
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(50, 40, 50, 40)
            gravity = Gravity.CENTER
        }

        fun createStyledButton(text: String, onClick: () -> Unit): Button {
            return Button(context).apply {
                this.text = text
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                setTypeface(null, Typeface.BOLD)
                setPadding(20, 10, 20, 10)
                background = createButtonBackground() // ✅ Apply border & background
                setTextColor(getThemeColor(context, android.R.attr.textColorPrimary))
                setOnClickListener { onClick() }
            }
        }

        val mealButton = createStyledButton("Meal") {
            mainDialog?.dismiss()
            showMealInputDialog()
        }

        val insulinButton = createStyledButton("Insulin") {
            mainDialog?.dismiss()
            showInsulinInputDialog()
        }

        val activityButton = createStyledButton("Activity") {
            mainDialog?.dismiss() // ✅ Close the main menu
            showActivityInputDialog() // ✅ Open the activity input dialog
        }



        layout.apply {
            addView(mealButton, createButtonLayoutParams())
            addView(insulinButton, createButtonLayoutParams())
            addView(activityButton, createButtonLayoutParams())
        }

        mainDialog = AlertDialog.Builder(context, R.style.CustomAlertDialogTheme)
            .setTitle("Log Activity")
            .setView(layout)
            .setNegativeButton("Cancel", null)
            .create()

        mainDialog?.setOnShowListener { styleDialogButtons(mainDialog!!) }
        mainDialog?.show()
    }

    // ✅ Displays the input dialog for entering activity details
    private fun showActivityInputDialog() {
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 40, 50, 40)
        }

        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val currentTime = timeFormat.format(Date())

        fun createStyledEditText(hint: String, length: Int): EditText {
            return EditText(context).apply {
                this.hint = hint
                maxLines = 1
                filters = arrayOf(InputFilter.LengthFilter(length))
                inputType = InputType.TYPE_CLASS_TEXT
                gravity = Gravity.START
                background = createEditTextBackground()
                setPadding(20, 20, 20, 20)
                setTextColor(getThemeColor(context, android.R.attr.textColorPrimary))
            }
        }

        fun createTimeEditText(): EditText {
            return EditText(context).apply {
                setText(currentTime)
                maxLines = 1
                isFocusable = false
                inputType = InputType.TYPE_NULL
                background = createEditTextBackground() // ✅ Apply background & border
                gravity = Gravity.CENTER
                setPadding(20, 20, 20, 20)
                setTextColor(getThemeColor(context, android.R.attr.textColorPrimary))
            }
        }

        val activityEditText = createStyledEditText("Enter Activity", 9)
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
        }

        startTimeEditText.setOnClickListener { showTimePicker(startTimeEditText, null, endTimeEditText) }
        endTimeEditText.setOnClickListener { showTimePicker(endTimeEditText, startTimeEditText, null) }

        val enableEndTimeCheckbox = CheckBox(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(40, 0, 0, 0)
            }
        }
        val enableEndTimeLabel = TextView(context).apply {
            text = "Set Time Frame"
            setPadding(10, 0, 0, 0)
            setTextColor(getThemeColor(context, android.R.attr.textColorPrimary))
        }

        enableEndTimeCheckbox.setOnCheckedChangeListener { _, isChecked ->
            separatorTextView.visibility = if (isChecked) View.VISIBLE else View.GONE
            endTimeEditText.visibility = if (isChecked) View.VISIBLE else View.GONE
            endTimeEditText.isEnabled = isChecked
        }

        val detailsEditText = createStyledEditText("Extra Info...", 17)

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
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss()  }
            .setNegativeButton("Cancel") { dialog, _ -> show() } // ✅ Returns to main menu ONLY on "Cancel"
            .create()

        dialog.setOnShowListener {
            styleDialogButtons(dialog)
            val okButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)

            okButton.setOnClickListener {
                val name = activityEditText.text.toString().trim()
                val startTime = startTimeEditText.text.toString()
                val endTime = if (enableEndTimeCheckbox.isChecked) endTimeEditText.text.toString() else null
                val details = detailsEditText.text.toString().trim().takeIf { it.isNotEmpty() }

                // ✅ Validation checks
                if (name.isEmpty()) {
                    showAlert("Error", "Activity name cannot be empty")
                    return@setOnClickListener
                }
                val restrictedNames = listOf("Breakfast", "Lunch", "Dinner", "Snack", "Insulin")
                if (name in restrictedNames) {
                    showAlert("Error", "Activity name cannot be $name. Please enter a different name.")
                    return@setOnClickListener
                }


                if (endTime != null && startTime >= endTime) {
                    showAlert("Error", "Start time must be before end time.")
                    return@setOnClickListener
                }

                dialog.dismiss()
                callback(name, startTime, endTime, details ?: "")

            }
        }

        dialog.show()
    }



    private fun createEditTextBackground(): GradientDrawable {
        return GradientDrawable().apply {
            setColor(getThemeColor(context, android.R.attr.colorPrimary)) // ✅ Matches ActivityLogDialog background
            setStroke(3, getThemeColor(context, android.R.attr.colorPrimaryDark)) // ✅ Adds a border
            cornerRadius = 16f // ✅ Rounded corners for consistency
        }
    }

    private fun createButtonBackground(): GradientDrawable {
        return GradientDrawable().apply {
            setColor(getThemeColor(context, android.R.attr.colorPrimary))
            setStroke(3, getThemeColor(context, android.R.attr.colorPrimaryDark))
            cornerRadius = 16f
        }
    }

    private fun createButtonLayoutParams(): LinearLayout.LayoutParams {
        return LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
            setMargins(10, 0, 10, 0)
        }
    }

    private fun showAlert(title: String, message: String) {
        val errorDialog = AlertDialog.Builder(context, R.style.CustomAlertDialogTheme)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK", null)
            .create()
        errorDialog.show()
    }

    private fun styleDialogButtons(dialog: AlertDialog) {
        val cancelButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
        val okButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)

        val textColorPrimary = getThemeColor(context, android.R.attr.textColorPrimary)
        val backgroundColor = getThemeColor(context, android.R.attr.colorPrimary)

        cancelButton.setTextColor(textColorPrimary)
        okButton.setTextColor(textColorPrimary)

        cancelButton.setBackgroundColor(backgroundColor)
        okButton.setBackgroundColor(backgroundColor)

        cancelButton.setPadding(20, 10, 20, 10)
        okButton.setPadding(20, 10, 20, 10)
    }

    private fun getThemeColor(context: Context, attr: Int): Int {
        val typedValue = TypedValue()
        context.theme.resolveAttribute(attr, typedValue, true)
        return typedValue.data
    }
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
    private fun showTimePicker(editText: EditText, startTimeEditText: EditText?, endTimeEditText: EditText?) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        TimePickerDialog(context, { _, selectedHour, selectedMinute ->
            val formattedTime = String.format("%02d:%02d", selectedHour, selectedMinute)
            editText.setText(formattedTime)
        }, hour, minute, true).show()
    }

    private fun showInsulinInputDialog(autoSelectFastActing: Boolean = false) {
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 40, 50, 40)
            gravity = Gravity.CENTER
        }

        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val currentTime = timeFormat.format(Date())

        val doseInput = EditText(context).apply {
            hint = "Enter Units"
            maxLines = 1
            filters = arrayOf(InputFilter.LengthFilter(2)) // Max 99 units
            inputType = InputType.TYPE_CLASS_NUMBER
            gravity = Gravity.CENTER
            background = createEditTextBackground()
            setPadding(20, 20, 20, 20)
            setTextColor(getThemeColor(context, android.R.attr.textColorPrimary))
        }

        val timeInput = EditText(context).apply {
            setText(currentTime)
            maxLines = 1
            isFocusable = false
            inputType = InputType.TYPE_NULL
            background = createEditTextBackground()
            gravity = Gravity.CENTER
            setPadding(20, 20, 20, 20)
            setTextColor(getThemeColor(context, android.R.attr.textColorPrimary))
        }
        timeInput.setOnClickListener { showTimePicker(timeInput, null, null) }

        val insulinTypeContainer = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(0, 20, 0, 20)
        }

        var selectedType: String? = if (autoSelectFastActing) "Fast-Acting" else null

        val isMealRelated = autoSelectFastActing // ✅ If true, prevent type change


        val fastActingButton = Button(context)
        val longActingButton = Button(context)

        fastActingButton.apply {
            text = "Fast-Acting"
            background = createButtonBackground()
            setTextColor(getThemeColor(context, android.R.attr.textColorPrimary))
            setPadding(20, 10, 20, 10)
            isEnabled = !isMealRelated // ✅ Disable button if from meal
            if (isMealRelated) {
                setSelectedStyle(this, true) // ✅ Keep selected if from meal
            } else {
                setOnClickListener {
                    selectedType = "Fast-Acting"
                    setSelectedStyle(this, true)
                    setSelectedStyle(longActingButton, false)
                }
            }
        }


        longActingButton.apply {
            text = "Long-Acting"
            background = createButtonBackground()
            setTextColor(getThemeColor(context, android.R.attr.textColorPrimary))
            setPadding(20, 10, 20, 10)
            isEnabled = !isMealRelated // ✅ Disable button if from meal
            if (!isMealRelated) {
                setOnClickListener {
                    selectedType = "Long-Acting"
                    setSelectedStyle(this, true)
                    setSelectedStyle(fastActingButton, false)
                }
            }
        }


        insulinTypeContainer.apply {
            addView(fastActingButton, createButtonLayoutParams())
            addView(longActingButton, createButtonLayoutParams())
        }

        layout.apply {
            addView(createLabeledRow("Dose (Units):", doseInput))
            addView(createLabeledRow("Time:", timeInput))
            addView(insulinTypeContainer)
        }

        val dialog = AlertDialog.Builder(context, R.style.CustomAlertDialogTheme)
            .setTitle("Log Insulin")
            .setView(layout)
            .setPositiveButton("OK", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            styleDialogButtons(dialog)

            // ✅ Auto-select "Fast-Acting" insulin when triggered from meal logging
            if (autoSelectFastActing) {
                fastActingButton.performClick()
            }

            val okButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            okButton.setOnClickListener {
                val dose = doseInput.text.toString().trim()
                val time = timeInput.text.toString()

                if (dose.isEmpty()) {
                    doseInput.error = "Enter dose"
                    return@setOnClickListener
                }

                if (selectedType == null) {
                    showAlert("Error", "Please select insulin type")
                    return@setOnClickListener
                }

                val description = "$selectedType: ${dose}u"

                dialog.dismiss()
                callback("Insulin", time, null, description)
            }
        }

        dialog.show()
    }


    private fun showMealInputDialog() {
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 40, 50, 40)
            gravity = Gravity.CENTER
        }

        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val currentTime = timeFormat.format(Date())

        val foodInput = EditText(context).apply {
            hint = "Enter Food"
            maxLines = 1
            filters = arrayOf(InputFilter.LengthFilter(12))
            inputType = InputType.TYPE_CLASS_TEXT
            gravity = Gravity.CENTER
            background = createEditTextBackground()
            setPadding(20, 20, 20, 20)
            setTextColor(getThemeColor(context, android.R.attr.textColorPrimary))
        }

        val carbInput = EditText(context).apply {
            hint = "Enter Carbs (g)"
            maxLines = 1
            filters = arrayOf(InputFilter.LengthFilter(3)) // Max 999g
            inputType = InputType.TYPE_CLASS_NUMBER
            gravity = Gravity.CENTER
            background = createEditTextBackground()
            setPadding(20, 20, 20, 20)
            setTextColor(getThemeColor(context, android.R.attr.textColorPrimary))
        }

        val timeInput = EditText(context).apply {
            setText(currentTime)
            maxLines = 1
            isFocusable = false
            inputType = InputType.TYPE_NULL
            background = createEditTextBackground()
            gravity = Gravity.CENTER
            setPadding(20, 20, 20, 20)
            setTextColor(getThemeColor(context, android.R.attr.textColorPrimary))
        }
        timeInput.setOnClickListener { showTimePicker(timeInput, null, null) }

        val separatorTextView = TextView(context).apply {
            text = " - "
            visibility = View.GONE
            setPadding(20, 0, 20, 0)
            setTextColor(getThemeColor(context, android.R.attr.textColorPrimary))
        }

        val endTimeInput = EditText(context).apply {
            setText(currentTime)
            maxLines = 1
            isFocusable = false
            inputType = InputType.TYPE_NULL
            background = createEditTextBackground()
            gravity = Gravity.CENTER
            setPadding(20, 20, 20, 20)
            setTextColor(getThemeColor(context, android.R.attr.textColorPrimary))
            visibility = View.GONE
        }
        endTimeInput.setOnClickListener { showTimePicker(endTimeInput, timeInput, null) }

        val enableTimeFrameCheckbox = CheckBox(context).apply {
            text = "Set Time Frame (e.g., 12:00 - 13:00)"
            setTextColor(getThemeColor(context, android.R.attr.textColorPrimary))
            setPadding(0, 10, 0, 10)
        }

        enableTimeFrameCheckbox.setOnCheckedChangeListener { _, isChecked ->
            separatorTextView.visibility = if (isChecked) View.VISIBLE else View.GONE
            endTimeInput.visibility = if (isChecked) View.VISIBLE else View.GONE
            endTimeInput.isEnabled = isChecked
        }

        val mealTypeContainer = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(0, 20, 0, 20)
        }

        var selectedMealType: String? = null

        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)

        val mealOptions = mutableListOf("Snack")

        if (currentHour in 0..12) mealOptions.add("Breakfast")
        if (currentHour in 10..14) mealOptions.add("Lunch")
        if (currentHour in 14..23) mealOptions.add("Dinner")

        val mealButtons = mutableListOf<Button>()

        mealOptions.forEach { mealType ->
            val button = Button(context).apply {
                text = mealType
                background = createButtonBackground()
                setTextColor(getThemeColor(context, android.R.attr.textColorPrimary))
                setPadding(20, 10, 20, 10)
                setOnClickListener {
                    selectedMealType = mealType
                    mealButtons.forEach { btn -> setSelectedStyle(btn, btn == this) }
                }
            }
            mealButtons.add(button)
            mealTypeContainer.addView(button, createButtonLayoutParams())
        }

        val logInsulinCheckbox = CheckBox(context).apply {
            text = "Log Insulin with entry?"
            setTextColor(getThemeColor(context, android.R.attr.textColorPrimary))
            setPadding(0, 10, 0, 10)
            isChecked = true // ✅ Auto-ticked by default
        }


        layout.apply {
            addView(mealTypeContainer)
            addView(createLabeledRow("Food:", foodInput))
            addView(createLabeledRow("Carbs (g):", carbInput))
            addView(createLabeledRow("Time:", timeInput, separatorTextView, endTimeInput))
            addView(enableTimeFrameCheckbox)
            addView(logInsulinCheckbox)
        }

        val dialog = AlertDialog.Builder(context, R.style.CustomAlertDialogTheme)
            .setTitle("Log Meal")
            .setView(layout)
            .setPositiveButton("OK", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            styleDialogButtons(dialog)
            val okButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)

            okButton.setOnClickListener {
                val food = foodInput.text.toString().trim()
                val carbs = carbInput.text.toString().trim()
                val time = timeInput.text.toString()
                val endTime = if (enableTimeFrameCheckbox.isChecked) endTimeInput.text.toString() else null

                if (selectedMealType == null) {
                    showAlert("Error", "Please select a meal type")
                    return@setOnClickListener
                }

                if (endTime != null && time >= endTime) {
                    showAlert("Error", "Start time must be before end time.")
                    return@setOnClickListener
                }

                if (food.isEmpty()) {
                    showAlert("Error", "Food name cannot be empty")
                    return@setOnClickListener
                }

                if (carbs.isEmpty()) {
                    showAlert("Error", "Enter carb amount")
                    return@setOnClickListener
                }

                val carbAmount = carbs.toIntOrNull()?.coerceAtMost(999) ?: 999
                val description = "$food (${carbAmount}g)"


                dialog.dismiss()
                callback(selectedMealType!!, time, endTime, description)

                if (logInsulinCheckbox.isChecked) {
                    showInsulinInputDialog(autoSelectFastActing = true)
                }
            }
        }

        dialog.show()
    }



    private fun setSelectedStyle(button: Button, isSelected: Boolean) {
        if (isSelected) {
            button.setBackgroundColor(getThemeColor(context, R.attr.colorPrimaryVariant))
        } else {
            button.setBackgroundColor(getThemeColor(context, android.R.attr.colorPrimary))
        }
    }




}
