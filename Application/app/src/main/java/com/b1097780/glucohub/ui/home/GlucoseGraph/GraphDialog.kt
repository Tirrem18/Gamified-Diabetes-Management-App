package com.b1097780.glucohub.ui.home.GraphDialog

import android.app.AlertDialog
import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.text.InputType
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import com.b1097780.glucohub.R

import java.util.*

class GraphDialog(
    private val context: Context,
    private val callback: (Float, Float) -> Unit
) {

    fun show() {
        val editText = createStyledEditText()

        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(50, 30, 50, 30)
            addView(editText, LinearLayout.LayoutParams(400, ViewGroup.LayoutParams.WRAP_CONTENT))
        }

        val dialog = AlertDialog.Builder(context, R.style.CustomAlertDialogTheme)
            .setTitle("Enter Glucose Level")
            .setMessage("Please enter your current glucose level:")
            .setView(container)
            .setPositiveButton("OK", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            styleDialogButtons(dialog)

            val okButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            okButton.setOnClickListener {
                val inputText = editText.text.toString()
                val number = inputText.toFloatOrNull()

                if (number == null || number < 0f || number > 40f) {
                    editText.error = "Invalid value. Enter a number between 0 and 40."
                    return@setOnClickListener
                }

                val currentTime = getCurrentTimeAsFloat()

                when {
                    number > 25f -> showWarningDialog(
                        "High Glucose Alert",
                        "A glucose level above 25 is dangerously high. Are you sure? Please inject accordingly",
                        number, currentTime, dialog
                    )

                    number < 2f -> showWarningDialog(
                        "Low Glucose Warning",
                        "A glucose level below 2 is critically low. Are you sure? Please eat fast-acting carbs accordingly",
                        number, currentTime, dialog
                    )

                    else -> {
                        callback(currentTime, number)
                        dialog.dismiss()
                    }
                }
            }
        }
        dialog.show()
    }

    private fun createStyledEditText(): EditText {
        return EditText(context).apply {
            inputType = InputType.TYPE_NUMBER_FLAG_DECIMAL or InputType.TYPE_CLASS_NUMBER
            hint = "Enter Glucose"
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
            setPadding(30, 20, 30, 20)
            gravity = Gravity.CENTER
            background = createEditTextBackground() // ✅ Apply the proper background styling
            setTextColor(getThemeColor(context, android.R.attr.textColorPrimary))
        }
    }

    private fun createEditTextBackground(): GradientDrawable {
        return GradientDrawable().apply {
            setColor(getThemeColor(context, android.R.attr.colorPrimary)) // ✅ Matches ActivityLogDialog background
            setStroke(3, getThemeColor(context, android.R.attr.colorPrimaryDark)) // ✅ Adds a border
            cornerRadius = 16f // ✅ Rounded corners for consistency
        }
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

    private fun showWarningDialog(title: String, message: String, value: Float, time: Float, parentDialog: AlertDialog) {
        AlertDialog.Builder(context, R.style.CustomAlertDialogTheme)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Confirm") { _, _ ->
                callback(time, value)
                parentDialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun getCurrentTimeAsFloat(): Float {
        val calendar = Calendar.getInstance()
        val hours = calendar.get(Calendar.HOUR_OF_DAY).toFloat()
        val minutes = calendar.get(Calendar.MINUTE) / 60f
        return hours + minutes
    }

    private fun getThemeColor(context: Context, attr: Int): Int {
        val typedValue = TypedValue()
        context.theme.resolveAttribute(attr, typedValue, true)
        return typedValue.data
    }
}
