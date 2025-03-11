package com.b1097780.glucohub.ui.glucose

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.b1097780.glucohub.PreferencesHelper

class GlucoseViewModel : ViewModel() {

    private val _averageGlucose = MutableLiveData<String>()
    val averageGlucose: LiveData<String> = _averageGlucose

    private val _selectedRange = MutableLiveData<String>()
    val selectedRange: LiveData<String> = _selectedRange

    private val _totalEntries = MutableLiveData<String>()
    val totalEntries: LiveData<String> = _totalEntries

    private val _lowestGlucose = MutableLiveData<String>()
    val lowestGlucose: LiveData<String> = _lowestGlucose

    private val _highestGlucose = MutableLiveData<String>()
    val highestGlucose: LiveData<String> = _highestGlucose

    private val _timeInRange = MutableLiveData<String>()
    val timeInRange: LiveData<String> = _timeInRange

    fun setAverageGlucose(value: String) {
        _averageGlucose.value = value
    }

    fun setSelectedRange(value: String) {
        _selectedRange.value = value
    }

    fun setTotalEntries(value: String) {
        _totalEntries.value = value
    }

    fun setLowestGlucose(value: String) {
        _lowestGlucose.value = value
    }

    fun setHighestGlucose(value: String) {
        _highestGlucose.value = value
    }

    fun setTimeInRange(value: String) {
        _timeInRange.value = value
    }

    // ✅ FIX: Add `context: Context` as a parameter
    fun fetchGlucoseDataForDate(context: Context, date: String) {
        val glucoseData = PreferencesHelper.getGlucoseEntriesForDate(context, date)

        if (glucoseData.isNotEmpty()) {
            val glucoseValues = glucoseData.map { it.second }
            val average = glucoseValues.average()
            val minGlucose = glucoseValues.minOrNull() ?: 0f
            val maxGlucose = glucoseValues.maxOrNull() ?: 0f
            val totalEntries = glucoseValues.size

            val minTime = glucoseData.minByOrNull { it.second }?.first ?: "--"
            val maxTime = glucoseData.maxByOrNull { it.second }?.first ?: "--"

            val inRangeCount = glucoseValues.count { it in 4.0..10.0 }
            val timeInRange = "%.1f%%".format(inRangeCount * 100.0 / totalEntries)

            _totalEntries.postValue("Total Entries: $totalEntries")
            _averageGlucose.postValue("Average Glucose: %.1f".format(average))
            _highestGlucose.postValue("Highest Glucose: %.1f (Time: $maxTime)".format(maxGlucose))
            _lowestGlucose.postValue("Lowest Glucose: %.1f (Time: $minTime)".format(minGlucose))
            _timeInRange.postValue("Time in Range: $timeInRange")

        } else {
            _totalEntries.postValue("Total Entries: 0")
            _averageGlucose.postValue("No Data")
            _highestGlucose.postValue("Highest Glucose: -- (Time: --)")
            _lowestGlucose.postValue("Lowest Glucose: -- (Time: --)")
            _timeInRange.postValue("Time in Range: --")
        }
    }
}
