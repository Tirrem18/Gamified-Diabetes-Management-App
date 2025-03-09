package com.b1097780.glucohub.ui.home.ActivityLog

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.b1097780.glucohub.PreferencesHelper
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

    // ✅ Load the most recent glucose entry from JSON storage
    fun loadRecentBloodEntry(context: Context) {
        val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val today = dateFormat.format(Date())

        val glucoseEntries = PreferencesHelper.getGlucoseEntriesForDate(context, today)

        // ✅ Ensure there is valid data
        if (glucoseEntries.isEmpty()) {
            _recentGlucoseTime.value = "--:--"
            _recentGlucoseValue.value = "-- mmol/L"
            return
        }

        // ✅ Get the most recent entry (last item in the list)
        val (entryTime, glucoseLevel) = glucoseEntries.last()

        _recentGlucoseTime.value = formatTime(entryTime.toFloat())
        _recentGlucoseValue.value = "$glucoseLevel mmol/L"
    }

    // ✅ Add a new activity entry and save it
    fun addActivityEntry(entry: ActivityLogEntry, context: Context) {
        val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val today = dateFormat.format(Date())

        val updatedEntries = _activityLogEntries.value.orEmpty().toMutableList()
        updatedEntries.add(entry)
        _activityLogEntries.value = updatedEntries

        // ✅ Save updated activities alongside glucose data
        PreferencesHelper.saveActivityEntries(context, today, updatedEntries)

    }

    // ✅ Load today's activities from JSON storage
    fun loadActivityEntries(context: Context) {
        val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val today = dateFormat.format(Date())

        val loadedEntries = PreferencesHelper.getActivityEntriesForDate(context, today)
        _activityLogEntries.value = loadedEntries // ✅ Notify UI to update
    }

    // ✅ Format time from float to HH:MM
    private fun formatTime(timeFloat: Float): String {
        val hours = timeFloat.toInt()
        val minutes = ((timeFloat - hours) * 60).toInt()
        return String.format("%02d:%02d", hours, minutes)
    }
}
