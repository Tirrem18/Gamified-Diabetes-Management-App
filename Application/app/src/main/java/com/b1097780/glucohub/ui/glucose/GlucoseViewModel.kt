package com.b1097780.glucohub.ui.glucose

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

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
}
