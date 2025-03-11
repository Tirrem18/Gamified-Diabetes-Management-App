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

    private val _selectedDate = MutableLiveData<String>().apply {
        value = formatDate(calendar)
    }
    val selectedDate: LiveData<String> = _selectedDate

    private val _hasData = MutableLiveData<Boolean?>().apply {
        value = null
    }
    val hasData: LiveData<Boolean?> = _hasData

    fun updateSelectedDate(year: Int, month: Int, day: Int, hasData: Boolean?) {
        calendar.set(year, month, day)
        val formattedDate = formatDate(calendar)

        Log.d("ViewModel", "Updating selected date: $formattedDate, Has Data: $hasData")

        _selectedDate.postValue(formattedDate)
        _hasData.postValue(hasData)
    }

    private fun formatDate(calendar: Calendar): String {
        return SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault()).format(calendar.time)
    }
}
