package com.b1097780.glucohub.ui.dailylogs

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.b1097780.glucohub.PreferencesHelper
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class DailyLogsViewModel : ViewModel() {

    private val calendar = Calendar.getInstance()
    private val todayDateString: String = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(calendar.time)

    private val _formattedDate = MutableLiveData<String>()
    val formattedDate: LiveData<String> = _formattedDate

    private val _selectedDate = MutableLiveData<String>()
    val selectedDate: LiveData<String> = _selectedDate

    private val _averageGlucose = MutableLiveData<String>()
    val averageGlucose: LiveData<String> = _averageGlucose

    private val _totalEntries = MutableLiveData<String>()
    val totalEntries: LiveData<String> = _totalEntries

    private val _timeInRange = MutableLiveData<String>()
    val timeInRange: LiveData<String> = _timeInRange

    private val _highestGlucose = MutableLiveData<String>()
    val highestGlucose: LiveData<String> = _highestGlucose

    private val _lowestGlucose = MutableLiveData<String>()
    val lowestGlucose: LiveData<String> = _lowestGlucose

    private val _totalActivityEntries = MutableLiveData<String>()
    val totalActivityEntries: LiveData<String> = _totalActivityEntries

    private val _hasData = MutableLiveData<Boolean?>().apply { value = null }
    val hasData: LiveData<Boolean?> = _hasData

    init {
        setDefaultDate()
    }

    fun setDefaultDate() {
        updateSelectedDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), null)
    }

    fun fetchGlucoseAndActivityData(context: Context, date: String) {
        val glucoseStats = PreferencesHelper.getGlucoseStatsForDate(context, date)
        val activityEntries = PreferencesHelper.getActivityEntriesForDate(context, date)

        Log.d("DailyLogsViewModel", "Raw glucoseStats data: $glucoseStats") // Log entire map

        val hasGlucoseData = glucoseStats["totalEntries"]?.toString()?.toIntOrNull() ?: 0 > 0
        val hasActivityData = activityEntries.isNotEmpty()

        _hasData.postValue(hasGlucoseData || hasActivityData)

        val highestGlucose = glucoseStats["highestGlucose"]?.toString() ?: "--"
        val lowestGlucose = glucoseStats["lowestGlucose"]?.toString() ?: "--"
        val highestTime = formatDecimalTime(glucoseStats["highestTime"]?.toString())
        val lowestTime = formatDecimalTime(glucoseStats["lowestTime"]?.toString())


        _highestGlucose.postValue("Highest: $highestGlucose mmol/L at $highestTime")
        _lowestGlucose.postValue("Lowest: $lowestGlucose mmol/L at $lowestTime")

        Log.d("DailyLogsViewModel", "Formatted highestTime: $highestTime")
        Log.d("DailyLogsViewModel", "Formatted lowestTime: $lowestTime")

        _totalEntries.postValue("Glucose Entries: ${glucoseStats["totalEntries"]}")
        _averageGlucose.postValue("Avg Glucose: ${glucoseStats["averageGlucose"]} mmol/L")
        _timeInRange.postValue("Time in Range: ${glucoseStats["timeInRange"]}")
        _highestGlucose.postValue("Highest: $highestGlucose mmol/L at $highestTime")
        _lowestGlucose.postValue("Lowest: $lowestGlucose mmol/L at $lowestTime")

        _totalActivityEntries.postValue("Total Activities: ${activityEntries.size}")
    }

    private fun formatDecimalTime(time: String?): String {
        if (time == null) {
            Log.d("DailyLogsViewModel", "Time input is null, returning --:--")
            return "--:--"
        }

        val decimalTime = time.toFloatOrNull()
        if (decimalTime == null) {
            Log.d("DailyLogsViewModel", "Time conversion failed, input was: $time")
            return "--:--"
        }

        val hours = decimalTime.toInt()  // Extract hours
        val minutes = ((decimalTime - hours) * 60).toInt()  // Convert fractional part to minutes

        val formattedTime = String.format("%02d:%02d", hours, minutes) // Ensure 2-digit formatting
        Log.d("DailyLogsViewModel", "Converted decimal time: $time -> $formattedTime")

        return formattedTime
    }



    fun updateSelectedDate(year: Int, month: Int, day: Int, hasData: Boolean?, context: Context? = null) {
        calendar.set(year, month, day)

        val nonFormattedDateString = String.format("%04d%02d%02d", year, month + 1, day)
        val formattedDateString = if (nonFormattedDateString == todayDateString) {
            "Today's Entries"  // ✅ Show "Today" instead of full date if it is today
        } else {
            formatDate(calendar) // Otherwise, format normally (e.g., "Monday, 12 March 2025")
        }

        Log.d("ViewModel", "Updating selected date: $nonFormattedDateString, Has Data: $hasData")

        _selectedDate.postValue(nonFormattedDateString)
        _formattedDate.postValue(formattedDateString)
        _hasData.postValue(hasData)

        // ✅ Fetch glucose & activity data if context is provided
        context?.let { fetchGlucoseAndActivityData(it, nonFormattedDateString) }
    }

    private fun formatDate(calendar: Calendar): String {
        return SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault()).format(calendar.time)
    }
}
