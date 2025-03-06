package com.b1097780.glucohub.ui.home.ActivityLog

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.b1097780.glucohub.MainActivity
import com.b1097780.glucohub.PreferencesHelper
import com.github.mikephil.charting.data.Entry
import java.text.SimpleDateFormat
import java.util.*

data class ActivityLogEntry(val name: String, val startTime: String, val endTime: String?, val description: String?)

class ActivityLogViewModel : ViewModel() {

    // ✅ Existing Glucose Tracking Variables
    private val _recentGlucoseTime = MutableLiveData<String>()
    val recentGlucoseTime: LiveData<String> = _recentGlucoseTime

    private val _recentGlucoseValue = MutableLiveData<String>()
    val recentGlucoseValue: LiveData<String> = _recentGlucoseValue

    // ✅ Activity Log Variables
    private val _activityLogEntries = MutableLiveData<List<ActivityLogEntry>>(emptyList())
    val activityLogEntries: LiveData<List<ActivityLogEntry>> = _activityLogEntries

    // ✅ Load the most recent glucose entry from SharedPreferences
    fun loadRecentBloodEntry(context: Context) {
        val glucoseEntries = PreferencesHelper.getRecentGlucoseEntries(context)

        // ✅ Ensure there is valid data
        if (glucoseEntries.isEmpty()) {
            _recentGlucoseTime.value = "--:--"
            _recentGlucoseValue.value = "-- mmol/L"
            return
        }

        // ✅ Avoid index errors when splitting
        val lastEntryParts = glucoseEntries.split(";").lastOrNull()?.split(",") ?: return

        if (lastEntryParts.size < 3) {
            _recentGlucoseTime.value = "--:--"
            _recentGlucoseValue.value = "-- mmol/L"
            return
        }

        val entryDate = lastEntryParts[0]
        val entryTime = lastEntryParts[1].toFloatOrNull()
        val glucoseLevel = lastEntryParts[2].toFloatOrNull()?.toString() ?: "--"

        val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val today = dateFormat.format(Date())

        if (entryDate == today && entryTime != null) {
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

        // ✅ Save updated activities
        PreferencesHelper.setActivityLogEntries(context, updatedEntries)

    }



    // ✅ Load today's activities from SharedPreferences
    fun loadActivityEntries(context: Context) {
        val loadedEntries = PreferencesHelper.getActivityLogEntries(context)
        _activityLogEntries.value = loadedEntries // ✅ Notify UI to update
    }

    // ✅ Format time from float to HH:MM
    private fun formatTime(timeFloat: Float): String {
        val hours = timeFloat.toInt()
        val minutes = ((timeFloat - hours) * 60).toInt()
        return String.format("%02d:%02d", hours, minutes)
    }
}
