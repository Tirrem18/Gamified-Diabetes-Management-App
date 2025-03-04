package com.b1097780.glucohub.ui.home.ActivityLog

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.text.SimpleDateFormat
import java.util.*

data class ActivityLogEntry(val name: String, val startTime: String, val endTime: String?, val description: String?)

class ActivityLogViewModel : ViewModel() {

    // ✅ Existing Glucose Tracking Variables (KEEPING THESE)
    private val _recentGlucoseTime = MutableLiveData<String>()
    val recentGlucoseTime: LiveData<String> = _recentGlucoseTime

    private val _recentGlucoseValue = MutableLiveData<String>()
    val recentGlucoseValue: LiveData<String> = _recentGlucoseValue

    // ✅ New Activity Log Variables
    private val _activityLogEntries = MutableLiveData<List<ActivityLogEntry>>(emptyList())
    val activityLogEntries: LiveData<List<ActivityLogEntry>> = _activityLogEntries

    // ✅ Load the most recent glucose entry (KEPT FROM EXISTING CODE)
    fun loadRecentBloodEntry(context: Context) {
        val sharedPrefs = context.getSharedPreferences("GlucoHubPrefs", Context.MODE_PRIVATE)
        val glucoseEntries = sharedPrefs.getString("glucoseEntries", "") ?: ""

        val lastEntry = glucoseEntries.split(";").lastOrNull()?.split(",")
        if (lastEntry == null || lastEntry.size < 3) {
            _recentGlucoseTime.value = "--:--"
            _recentGlucoseValue.value = "-- mmol/L"
            return
        }

        val entryDate = lastEntry[0]
        val entryTime = lastEntry[1].toFloatOrNull() ?: 0f
        val glucoseLevel = lastEntry[2].toFloatOrNull()?.toString() ?: "--"

        val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val today = dateFormat.format(Date())

        if (entryDate == today) {
            _recentGlucoseTime.value = formatTime(entryTime)
            _recentGlucoseValue.value = "$glucoseLevel mmol/L"
        } else {
            _recentGlucoseTime.value = "--:--"
            _recentGlucoseValue.value = "-- mmol/L"
        }
    }

    // ✅ Add a new activity entry and save it
    fun addActivityEntry(entry: ActivityLogEntry, context: Context) {
        val updatedEntries = _activityLogEntries.value.orEmpty().toMutableList()
        updatedEntries.add(entry)
        _activityLogEntries.value = updatedEntries

        saveActivityEntries(updatedEntries, context)
    }


    fun saveActivityEntries(entries: List<ActivityLogEntry>, context: Context) {
        val sharedPrefs = context.getSharedPreferences("GlucoHubPrefs", Context.MODE_PRIVATE)
        val editor = sharedPrefs.edit()
        val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val today = dateFormat.format(Date())

        val entryString = entries.joinToString(";") { entry ->
            "$today,${entry.name},${entry.startTime},${entry.endTime ?: "null"},${entry.description ?: "null"}"
        }

        editor.putString("activityLogEntries", entryString)
        editor.apply() // ✅ Save to SharedPreferences
    }


    // ✅ Load today's activities from SharedPreferences
    fun loadActivityEntries(context: Context) {
        val sharedPrefs = context.getSharedPreferences("GlucoHubPrefs", Context.MODE_PRIVATE)
        val entryString = sharedPrefs.getString("activityLogEntries", "") ?: ""
        val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val today = dateFormat.format(Date())

        val loadedEntries = entryString.split(";").mapNotNull {
            val parts = it.split(",")
            if (parts.size >= 4 && parts[0] == today) {
                ActivityLogEntry(
                    name = parts[1],
                    startTime = parts[2],
                    endTime = parts[3].takeIf { it != "null" },
                    description = parts.getOrNull(4)?.takeIf { it != "null" }
                )
            } else {
                null
            }
        }

        _activityLogEntries.value = loadedEntries // ✅ Notify UI to update
    }

    private fun formatTime(timeFloat: Float): String {
        val hours = timeFloat.toInt()
        val minutes = ((timeFloat - hours) * 60).toInt()
        return String.format("%02d:%02d", hours, minutes) // ✅ Convert float time to HH:MM
    }
}
