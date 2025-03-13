package com.b1097780.glucohub.ui.dailylogs

import android.content.Context
import android.app.AlertDialog
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.graphics.Typeface
import com.b1097780.glucohub.PreferencesHelper
import com.b1097780.glucohub.R
import com.b1097780.glucohub.ui.home.ActivityLog.ActivityLogEntry

object GlucoseEntryDetailHelper {

    fun showGlucoseEntryDetails(context: Context, date: String, entry: String) {
        val entryTime = extractTimeFromEntry(entry) ?: return

        val relatedEvents = getEventsAroundTime(context, entryTime, date)
        val glucoseChange = getGlucoseChange(context, entryTime, date)

        val message = buildDetailMessage(entry, relatedEvents, glucoseChange)

        val dialog = AlertDialog.Builder(context, R.style.CustomAlertDialogTheme)
            .setTitle("Glucose Entry Details")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .create()

        dialog.show()
        styleDialogButtons(dialog)
    }

    private fun extractTimeFromEntry(entry: String): String? {
        return entry.split(" - ").firstOrNull()?.trim()
    }

    private fun getEventsAroundTime(context: Context, time: String, date: String): List<String> {
        val allActivities = PreferencesHelper.getActivityEntriesForDate(context, date)
        val allGlucose = PreferencesHelper.getGlucoseEntriesForDate(context, date)

        val targetTime = timeToFloat(time)

        // Filter glucose entries (-2 hours, +1 hour), excluding the current glucose reading
        val relevantGlucose = allGlucose.filter {
            val glucoseTime = it.first.toFloatOrNull() ?: -1f
            glucoseTime in (targetTime - 2.0).coerceAtLeast(0.0)..(targetTime + 1.0) && glucoseTime != targetTime
        }

        // Find previous and next glucose readings
        val previousGlucose = relevantGlucose.lastOrNull { it.first.toFloatOrNull() ?: -1f < targetTime }
        val nextGlucose = relevantGlucose.firstOrNull { it.first.toFloatOrNull() ?: -1f > targetTime }

        val glucoseEntries = mutableListOf<String>().apply {
            previousGlucose?.let { add("Previous Glucose: ${formatGlucoseTime(it.first)} - ${it.second} mmol/L") }
            nextGlucose?.let { add("Next Glucose: ${formatGlucoseTime(it.first)} - ${it.second} mmol/L") }
        }

        // Filter activity entries (-2.5 hours, +1 hour) → Include activities where **end time falls in range**
        val relevantActivities = allActivities.filter {
            val startTime = timeToFloat(it.startTime)
            val endTime = it.endTime?.let { timeToFloat(it) } ?: startTime // If no end time, treat as one-time event
            startTime in (targetTime - 2.5).coerceAtLeast(0.0)..(targetTime + 1.0) ||
                    endTime in (targetTime - 2.5).coerceAtLeast(0.0)..(targetTime + 1.0)
        }

        // Sort: Meals first, then Insulin, then Other Activities
        val sortedActivities = relevantActivities.sortedWith(
            compareByDescending<ActivityLogEntry> {
                it.name.contains("Meal", ignoreCase = true) ||
                        it.name.contains("Snack", ignoreCase = true) ||
                        it.name.contains("Lunch", ignoreCase = true) ||
                        it.name.contains("Dinner", ignoreCase = true) ||
                        it.name.contains("Breakfast", ignoreCase = true) ||
                        it.name.contains("Insulin", ignoreCase = true)
            }.thenByDescending { timeToFloat(it.startTime) }
        ).map { formatEventDetail(it) }

        return if (sortedActivities.isEmpty() && glucoseEntries.isEmpty()) {
            listOf("🚫 No events logged near this time.")
        } else {
            sortedActivities + listOf("📉 **Glucose Readings:**") + glucoseEntries
        }
    }

    private fun getGlucoseChange(context: Context, time: String, date: String): String {
        val allGlucose = PreferencesHelper.getGlucoseEntriesForDate(context, date)
            .sortedBy { it.first.toFloatOrNull() ?: 0f }

        val targetTime = timeToFloat(time)

        val previousEntry = allGlucose.lastOrNull {
            val glucoseTime = it.first.toFloatOrNull() ?: -1f
            glucoseTime < targetTime
        } ?: return "**📊 No previous glucose entry.**"

        val currentGlucose = allGlucose.find { it.first.toFloatOrNull() == targetTime }?.second ?: previousEntry.second
        val change = currentGlucose - previousEntry.second

        val trend = when {
            change > 0.1 -> "rose by ${String.format("%.1f", kotlin.math.abs(change))} mmol/L "
            change < -0.1 -> "dropped by ${String.format("%.1f", kotlin.math.abs(change))} mmol/L "
            else -> "stayed the same"
        }

        return "**📊 Glucose $trend since ${formatGlucoseTime(previousEntry.first)}.**"
    }

    private fun buildDetailMessage(glucoseEntry: String, events: List<String>, glucoseChange: String): SpannableString {
        val builder = StringBuilder()
        builder.append("📌 **Glucose Reading:**\n $glucoseEntry\n\n")
        builder.append("$glucoseChange\n\n")
        builder.append("📝 **Recent Related Events:**\n")

        // ✅ Ensure we only consider real events, ignoring placeholders
        val actualEvents = events.filter { it.isNotBlank() && it != "📉 **Glucose Readings:**" }

        if (actualEvents.isNotEmpty()) {
            actualEvents.forEach {
                if (it == "📉 **Glucose Readings:**") {
                    builder.append("\n$it\n") // ✅ Section for glucose readings
                } else {
                    builder.append("- $it\n") // ✅ Normal event
                }
            }
        } else {
            builder.append("🚫 No events logged near this time.\n") // ✅ Always displays when no real events exist
        }

        val spannableString = SpannableString(builder.toString())

        // ✅ Bold Section Titles
        val boldSections = listOf("📌 **Glucose Reading:**", "📝 **Recent Related Events:**", "📉 **Glucose Readings:**")
        boldSections.forEach { section ->
            val startIndex = builder.indexOf(section)
            if (startIndex != -1) {
                spannableString.setSpan(StyleSpan(Typeface.BOLD), startIndex, startIndex + section.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }

        return spannableString
    }



    private fun styleDialogButtons(dialog: AlertDialog) {
        val okButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
        okButton?.apply {
            setTextColor(getThemeColor(dialog.context, android.R.attr.textColorPrimary))
            setBackgroundColor(getThemeColor(dialog.context, android.R.attr.colorPrimary))
            setPadding(20, 10, 20, 10)
        }
    }

    private fun getThemeColor(context: Context, attr: Int): Int {
        val typedValue = android.util.TypedValue()
        context.theme.resolveAttribute(attr, typedValue, true)
        return typedValue.data
    }

    private fun timeToFloat(time: String): Float {
        return try {
            if (time.contains(":")) {
                val parts = time.split(":").map { it.toInt() }
                parts[0] + (parts[1] / 60f)
            } else {
                time.toFloat()
            }
        } catch (e: Exception) {
            0f
        }
    }

    private fun formatGlucoseTime(time: String): String {
        return try {
            val decimalTime = time.toFloatOrNull() ?: return "--:--"
            val hours = decimalTime.toInt()
            val minutes = ((decimalTime - hours) * 60).toInt()
            String.format("%02d:%02d", hours, minutes)
        } catch (e: Exception) {
            "--:--"
        }
    }

    private fun formatEventDetail(event: ActivityLogEntry): String {
        val eventTime = event.startTime // ✅ Keep activity times as they are

        return when {
            event.endTime == null -> "📍 ${event.name} at $eventTime: ${event.description}" // One-time event
            else -> "🏃 ${event.name} from $eventTime to ${event.endTime}: ${event.description}" // Use raw times
        }
    }

}
