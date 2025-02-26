package com.b1097780.glucohub.ui.home.ActivityLog

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

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


        val lastEntry = glucoseEntries.split(";").lastOrNull()?.split(",")
        val recentGlucose = lastEntry?.getOrNull(1)?.toFloatOrNull()?.toString() ?: "--"
        val recentTime = lastEntry?.getOrNull(0)?.toFloatOrNull()?.let { formatTime(it) } ?: "--:--"

        _recentGlucoseTime.value = recentTime
        _recentGlucoseValue.value = "$recentGlucose mmol/L"

        // Fake activity data (Replace this with actual activity logs from storage)
        val activities = listOf(
            ActivityLogEntry("Gym", "15:00 - 16:00", "Cardio + Arms"),
            ActivityLogEntry("Meal", "16:14", "50 carbs, 6 Units"),
            ActivityLogEntry("Long-acting", "17:30", "30 units")
        )

        _activityLogEntries.value = activities
    }

    private fun formatTime(timeFloat: Float): String {
        val hours = timeFloat.toInt()
        val minutes = ((timeFloat - hours) * 60).toInt()
        return String.format("%02d:%02d", hours, minutes)
    }
}
