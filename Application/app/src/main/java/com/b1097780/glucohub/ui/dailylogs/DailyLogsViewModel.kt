package com.b1097780.glucohub.ui.dailylogs

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class DailyLogsViewModel : ViewModel() {

    private val calendar = Calendar.getInstance()

    // LiveData to store the formatted date for display (e.g., "Monday, 12 March 2025")
    private val _formattedDate = MutableLiveData<String>().apply {
        value = formatDate(calendar)
    }
    val formattedDate: LiveData<String> = _formattedDate

    // LiveData to store the non-formatted date for query/search (e.g., "20250311")
    private val _selectedDate = MutableLiveData<String>().apply {
        value = String.format("%04d%02d%02d", calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH))
    }
    val selectedDate: LiveData<String> = _selectedDate

    // LiveData to store whether data exists for the selected date
    private val _hasData = MutableLiveData<Boolean?>().apply {
        value = null
    }
    val hasData: LiveData<Boolean?> = _hasData

    fun updateSelectedDate(year: Int, month: Int, day: Int, hasData: Boolean?) {
        // Set the calendar with the year, month, and day
        calendar.set(year, month, day)

        // Store the non-formatted date as yyyyMMdd (e.g., "20250311")
        val nonFormattedDateString = String.format("%04d%02d%02d", year, month + 1, day)

        // Format the date for display as "Monday, 12 March 2025"
        val formattedDateString = formatDate(calendar)

        Log.d("ViewModel", "Updating selected date: $nonFormattedDateString, Has Data: $hasData")

        // Post the non-formatted date string (for query/search) and the formatted date string (for display)
        _selectedDate.postValue(nonFormattedDateString)  // For query/search
        _formattedDate.postValue(formattedDateString)    // For display
        _hasData.postValue(hasData)
    }

    private fun formatDate(calendar: Calendar): String {
        return SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault()).format(calendar.time)
    }
}
