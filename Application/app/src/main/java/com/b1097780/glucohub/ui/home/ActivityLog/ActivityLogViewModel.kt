package com.b1097780.glucohub.ui.home.ActivityLog

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.text.SimpleDateFormat
import java.util.*

data class ActivityLogEntry(val activity: String, val time: String, val details: String)

class ActivityLogViewModel : ViewModel() {

    private val _recentGlucoseTime = MutableLiveData<String>()
    val recentGlucoseTime: LiveData<String> = _recentGlucoseTime

    private val _recentGlucoseValue = MutableLiveData<String>()
    val recentGlucoseValue: LiveData<String> = _recentGlucoseValue

    private val _activityLogEntries = MutableLiveData<List<ActivityLogEntry>>()
    val activityLogEntries: LiveData<List<ActivityLogEntry>> = _activityLogEntries

    fun loadRecentBloodEntry(context: Context) {
        val sharedPrefs = context.getSharedPreferences("GlucoHubPrefs", Context.MODE_PRIVATE)
        val glucoseEntries = sharedPrefs.getString("glucoseEntries", "") ?: ""

        // ✅ Check if there is a valid glucose entry for today
        val lastEntry = glucoseEntries.split(";").lastOrNull()?.split(",")
        if (lastEntry == null || lastEntry.size < 3) {
            _recentGlucoseTime.value = null // ✅ Now returns `null` instead of "--:--"
            _recentGlucoseValue.value = null // ✅ Now returns `null` instead of "-- mmol/L"
            return
        }

        val entryDate = lastEntry[0] // Date in format yyyyMMdd
        val entryTime = lastEntry[1].toFloatOrNull() ?: 0f // Time in hours (float)
        val glucoseLevel = lastEntry[2].toFloatOrNull()?.toString() ?: "--"

        val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val today = dateFormat.format(Date()) // Get today's date in yyyyMMdd format

        if (entryDate == today) {
            _recentGlucoseTime.value = formatTime(entryTime) // ✅ Convert float time to HH:MM
            _recentGlucoseValue.value = "$glucoseLevel mmol/L"
        } else {
            _recentGlucoseTime.value = null
            _recentGlucoseValue.value = null
        }
    }


    private fun formatTime(timeFloat: Float): String {
        val hours = timeFloat.toInt()
        val minutes = ((timeFloat - hours) * 60).toInt()
        return String.format("%02d:%02d", hours, minutes) // ✅ Properly formats to HH:MM
    }
}
