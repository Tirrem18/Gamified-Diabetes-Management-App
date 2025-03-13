package com.b1097780.glucohub.ui.dailylogs

import android.app.AlertDialog
import android.content.Context
import android.graphics.Typeface
import android.view.Gravity
import android.widget.*
import com.b1097780.glucohub.MainActivity
import com.b1097780.glucohub.PreferencesHelper
import com.b1097780.glucohub.R
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class DailyLogsDeleteDialog(
    private val context: Context,
    private val selectedDate: String,
    private val onEntriesUpdated: () -> Unit // Callback to refresh data
) {
    private var dialog: AlertDialog? = null

    fun show() {
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 40, 50, 40)
        }

        val title = TextView(context).apply {
            text = "Delete Entries"
            textSize = 24f
            setTypeface(null, Typeface.BOLD)
            gravity = Gravity.CENTER
            setTextColor(getThemeColor(android.R.attr.textColorPrimary))
        }

        val listGlucose = ListView(context)
        val listPastActivities = ListView(context)
        val listFutureActivities = ListView(context)

        layout.addView(title)

        // ✅ Get last 3 glucose entries, ensure deletion works correctly
        val rawGlucoseEntries = PreferencesHelper.getGlucoseEntriesForDate(context, selectedDate)
            .takeLast(3)
            .toMutableList() // ✅ Preserve original time values for deletion

        val displayGlucoseEntries = rawGlucoseEntries.map { "${formatGlucoseTime(it.first)} - ${it.second} mmol/L" }
            .toMutableList() // ✅ Display formatted, but delete using raw

        if (displayGlucoseEntries.isNotEmpty()) {
            layout.addView(createSectionLabel("Last 3 Glucose Entries"))
            layout.addView(listGlucose)
        }

        val glucoseAdapter = ArrayAdapter(context, android.R.layout.simple_list_item_1, displayGlucoseEntries)
        listGlucose.adapter = glucoseAdapter

        // ✅ Get last 3 past activities
        val pastActivities = PreferencesHelper.getActivityEntriesForDate(context, selectedDate)
            .filter { isPastActivity(it.startTime) }
            .takeLast(3)
            .toMutableList()

        val displayPastActivities = pastActivities.map { "${it.startTime} - ${it.name}" }
            .toMutableList()

        if (displayPastActivities.isNotEmpty()) {
            layout.addView(createSectionLabel("Last 3 Past Activities"))
            layout.addView(listPastActivities)
        }

        val pastActivitiesAdapter = ArrayAdapter(context, android.R.layout.simple_list_item_1, displayPastActivities)
        listPastActivities.adapter = pastActivitiesAdapter

        // ✅ Get future activities
        val futureActivities = PreferencesHelper.getActivityEntriesForDate(context, selectedDate)
            .filter { isFutureActivity(it.startTime) }
            .sortedBy { parseTime(it.startTime) }
            .take(1)
            .toMutableList()

        val displayFutureActivities = futureActivities.map { "${it.startTime} - ${it.name}" }
            .toMutableList()

        if (displayFutureActivities.isNotEmpty()) {
            layout.addView(createSectionLabel("Most Recent Future Activity"))
            layout.addView(listFutureActivities)
        }

        val futureActivitiesAdapter = ArrayAdapter(context, android.R.layout.simple_list_item_1, displayFutureActivities)
        listFutureActivities.adapter = futureActivitiesAdapter

        val buttonClose = Button(context).apply {
            text = "Close"
            setBackgroundColor(getThemeColor(android.R.attr.colorPrimary))
            setTextColor(getThemeColor(android.R.attr.textColorPrimary))
            setPadding(20, 10, 20, 10)
            setOnClickListener { dialog?.dismiss() }
        }
        layout.addView(buttonClose)

        // ✅ Handle deleting glucose entries using the correct raw time value
        listGlucose.setOnItemClickListener { _, _, position, _ ->
            confirmDeleteDialog("Glucose Entry", displayGlucoseEntries[position]) {
                val deletedTime = rawGlucoseEntries[position].first // ✅ Use original time value for deletion
                PreferencesHelper.deleteGlucoseEntry(context, selectedDate, deletedTime)
                rawGlucoseEntries.removeAt(position)
                displayGlucoseEntries.removeAt(position)
                glucoseAdapter.notifyDataSetChanged()
                closeDialogs()
                deductCoin()
                onEntriesUpdated()
            }
        }

        // ✅ Handle deleting past activities
        listPastActivities.setOnItemClickListener { _, _, position, _ ->
            confirmDeleteDialog("Past Activity", displayPastActivities[position]) {
                val deletedTime = pastActivities[position].startTime
                PreferencesHelper.deleteActivityEntry(context, selectedDate, deletedTime)
                pastActivities.removeAt(position)
                displayPastActivities.removeAt(position)
                pastActivitiesAdapter.notifyDataSetChanged()
                closeDialogs()
                deductCoin()
                onEntriesUpdated()
            }
        }

        // ✅ Handle deleting future activity
        listFutureActivities.setOnItemClickListener { _, _, position, _ ->
            confirmDeleteDialog("Upcoming Activity", displayFutureActivities[position]) {
                val deletedTime = futureActivities[position].startTime
                PreferencesHelper.deleteActivityEntry(context, selectedDate, deletedTime)
                displayFutureActivities.removeAt(position)
                futureActivitiesAdapter.notifyDataSetChanged()
                closeDialogs()
                deductCoin()
                onEntriesUpdated()
            }
        }

        dialog = AlertDialog.Builder(context, R.style.CustomAlertDialogTheme)
            .setView(layout)
            .create()

        dialog?.show()
    }

    private fun confirmDeleteDialog(type: String, entry: String, onDelete: () -> Unit) {
        val dialog = AlertDialog.Builder(context, R.style.CustomAlertDialogTheme)
            .setTitle("Confirm Delete")
            .setMessage("Are you sure you want to delete this $type?\n\n$entry")
            .setPositiveButton("Delete") { _, _ ->
                onDelete()
                updateCoinUI()
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener { styleDialogButtons(dialog) }
        dialog.show()
    }

    private fun closeDialogs() {
        dialog?.dismiss()
    }

    private fun updateCoinUI() {
        (context as? MainActivity)?.updateCoinButton(PreferencesHelper.getUserCoins(context))
    }

    private fun createSectionLabel(text: String): TextView {
        return TextView(context).apply {
            this.text = text
            textSize = 20f
            setTypeface(null, Typeface.BOLD)
            gravity = Gravity.START
            setPadding(0, 10, 0, 5)
            setTextColor(getThemeColor(android.R.attr.textColorPrimary))
        }
    }

    private fun isPastActivity(startTime: String): Boolean {
        return parseTime(startTime).isBefore(LocalTime.now())
    }

    private fun isFutureActivity(startTime: String): Boolean {
        return parseTime(startTime).isAfter(LocalTime.now())
    }

    private fun parseTime(time: String): LocalTime {
        return try {
            LocalTime.parse(time, DateTimeFormatter.ofPattern("HH:mm"))
        } catch (e: Exception) {
            LocalTime.of(0, 0)
        }
    }

    private fun formatGlucoseTime(time: String): String {
        return try {
            val decimalTime = time.toFloat()
            val hours = decimalTime.toInt()
            val minutes = ((decimalTime - hours) * 60).toInt()
            String.format("%02d:%02d", hours, minutes)
        } catch (e: Exception) {
            "--:--"
        }
    }

    private fun deductCoin() {
        val currentCoins = PreferencesHelper.getUserCoins(context)
        if (currentCoins > 0) {
            PreferencesHelper.setUserCoins(context, currentCoins - 1)
            updateCoinUI()
        }
    }

    private fun getThemeColor(attr: Int): Int {
        val typedValue = android.util.TypedValue()
        context.theme.resolveAttribute(attr, typedValue, true)
        return typedValue.data
    }

    private fun styleDialogButtons(dialog: AlertDialog) {
        val cancelButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
        val deleteButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)

        val textColorPrimary = getThemeColor(android.R.attr.textColorPrimary)
        val backgroundColor = getThemeColor(android.R.attr.colorPrimary)

        cancelButton.setTextColor(textColorPrimary)
        deleteButton.setTextColor(textColorPrimary)

        cancelButton.setBackgroundColor(backgroundColor)
        deleteButton.setBackgroundColor(backgroundColor)

        cancelButton.setPadding(20, 10, 20, 10)
        deleteButton.setPadding(20, 10, 20, 10)
    }
}
