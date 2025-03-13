package com.b1097780.glucohub.ui.dailylogs

import android.app.AlertDialog
import android.content.Context
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.widget.*
import com.b1097780.glucohub.PreferencesHelper
import com.b1097780.glucohub.R

class DailyLogsReviewDialog(
    private val context: Context,
    private val selectedDate: String,
    private var startFrom: String? = null // ✅ Updated to var for refreshing
) {
    private var dialog: AlertDialog? = null
    private val entriesPerPage = 10
    private var listEntries: ListView? = null // ✅ Store ListView globally

    fun show() {
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 30, 40, 30)
        }

        val title = TextView(context).apply {
            text = "Review Glucose Entries"
            textSize = 24f
            setTypeface(null, Typeface.BOLD)
            gravity = Gravity.CENTER
            setTextColor(getThemeColor(android.R.attr.textColorPrimary))
        }

        listEntries = ListView(context) // ✅ Store globally for refresh
        val buttonLayout = createPaginationButtons()

        val buttonClose = Button(context).apply {
            text = "Close"
            setBackgroundColor(getThemeColor(android.R.attr.colorPrimary))
            setTextColor(getThemeColor(android.R.attr.textColorPrimary))
            setPadding(20, 10, 20, 10)
            setOnClickListener { dialog?.dismiss() }
        }

        layout.addView(title)
        layout.addView(listEntries)
        layout.addView(buttonLayout)
        layout.addView(buttonClose)

        dialog = AlertDialog.Builder(context, R.style.CustomAlertDialogTheme)
            .setView(layout)
            .create()

        dialog?.show()
        updateEntries() // ✅ Load initial entries
    }

    private fun createPaginationButtons(): LinearLayout {
        val buttonLayout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(0, 10, 0, 10)
        }

        val prevButton = Button(context).apply {
            text = "<"
            textSize = 14f
            setPadding(12, 6, 12, 6)
            setBackgroundColor(getThemeColor(android.R.attr.colorPrimary))
            setTextColor(getThemeColor(android.R.attr.textColorPrimary))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 160, 0) } // ✅ Adds spacing

            setOnClickListener {
                val previousStart = getPreviousStartTime()
                if (previousStart != null) {
                    refreshDialog(previousStart) // ✅ Refresh instead of creating a new one
                }
            }
        }

        val nextButton = Button(context).apply {
            text = ">"
            textSize = 14f
            setPadding(12, 6, 12, 6)
            setBackgroundColor(getThemeColor(android.R.attr.colorPrimary))
            setTextColor(getThemeColor(android.R.attr.textColorPrimary))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(160, 0, 0, 0) } // ✅ Adds spacing

            setOnClickListener {
                val nextStart = getNextStartTime()
                if (nextStart != null) {
                    refreshDialog(nextStart) // ✅ Refresh instead of creating a new one
                }
            }
        }

        buttonLayout.addView(prevButton)
        buttonLayout.addView(nextButton)

        return buttonLayout
    }

    private fun updateEntries() {
        val allEntries = getGlucoseEntries()

        val displayedEntries = mutableListOf<String>().apply {
            addAll(allEntries)
            while (size < entriesPerPage) add(" ") // ✅ Ensure exactly 10 rows
        }

        val adapter = ArrayAdapter(context, android.R.layout.simple_list_item_1, displayedEntries)
        listEntries?.adapter = adapter

        listEntries?.setOnItemClickListener { _, _, position, _ ->
            val selectedEntry = displayedEntries[position]
            if (selectedEntry.isNotBlank()) { // ✅ Only open details if it's not a blank placeholder
                listEntries?.setOnItemClickListener { _, _, position, _ ->
                    val selectedEntry = displayedEntries[position]
                    if (selectedEntry.isNotBlank()) {
                        GlucoseEntryDetailHelper.showGlucoseEntryDetails(context, selectedDate, selectedEntry)
                    }
                }


            }
        }
    }

    private fun refreshDialog(newStartFrom: String) {
        startFrom = newStartFrom // ✅ Update pagination start
        updateEntries() // ✅ Refresh entries in existing dialog
    }

    private fun getGlucoseEntries(): List<String> {
        val allEntries = PreferencesHelper.getGlucoseEntriesForDate(context, selectedDate)
            .sortedBy { it.first.toFloat() }

        val startIndex = startFrom?.let {
            allEntries.indexOfFirst { entry -> entry.first == it }.takeIf { it >= 0 } ?: 0
        } ?: 0

        val endIndex = (startIndex + entriesPerPage).coerceAtMost(allEntries.size)
        return allEntries.subList(startIndex, endIndex).map {
            "${formatGlucoseTime(it.first)} - ${it.second} mmol/L"
        }
    }

    private fun getNextStartTime(): String? {
        val allEntries = PreferencesHelper.getGlucoseEntriesForDate(context, selectedDate)
            .sortedBy { it.first.toFloat() }

        val startIndex = startFrom?.let {
            allEntries.indexOfFirst { entry -> entry.first == it }.takeIf { it >= 0 } ?: 0
        } ?: 0

        return if (startIndex + entriesPerPage < allEntries.size) {
            allEntries[startIndex + entriesPerPage].first
        } else null
    }

    private fun getPreviousStartTime(): String? {
        val allEntries = PreferencesHelper.getGlucoseEntriesForDate(context, selectedDate)
            .sortedBy { it.first.toFloat() }

        val startIndex = startFrom?.let {
            allEntries.indexOfFirst { entry -> entry.first == it }.takeIf { it >= 0 } ?: 0
        } ?: 0

        return if (startIndex - entriesPerPage >= 0) {
            allEntries[(startIndex - entriesPerPage).coerceAtLeast(0)].first
        } else null
    }

    private fun showEntryDetails(entry: String) {
        AlertDialog.Builder(context)
            .setTitle("Glucose Entry Details")
            .setMessage(entry)
            .setPositiveButton("OK", null)
            .create()
            .show()
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

    private fun getThemeColor(attr: Int): Int {
        val typedValue = android.util.TypedValue()
        context.theme.resolveAttribute(attr, typedValue, true)
        return typedValue.data
    }
}
