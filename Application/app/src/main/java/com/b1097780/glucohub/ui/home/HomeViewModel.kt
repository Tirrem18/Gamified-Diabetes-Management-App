package com.b1097780.glucohub.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.github.mikephil.charting.data.Entry

class HomeViewModel : ViewModel() {

    // LiveData for Glucose Section
    private val _glucoseTitle = MutableLiveData<String>().apply {
        value = "Glucose Levels"
    }
    val glucoseText: LiveData<String> = _glucoseTitle

    // LiveData for Planner Section
    private val _plannerTitle = MutableLiveData<String>().apply {
        value = "Activity Log"
    }
    val plannerText: LiveData<String> = _plannerTitle

    // Sample glucose data (Time in hours, Glucose level)
    private val _glucoseEntries = MutableLiveData<List<Entry>>().apply {}
    val glucoseEntries: LiveData<List<Entry>> = _glucoseEntries

    // Function to update glucose data (for Firebase later)
    fun addGlucoseEntry(time: Float, value: Float) {
        val currentList = _glucoseEntries.value?.toMutableList() ?: mutableListOf()
        val cappedValue = if (value > 20f) 20f else value
        currentList.add(Entry(time, cappedValue))
        _glucoseEntries.value = currentList
    }

    fun setGlucoseEntries(entries: List<Entry>) {
        _glucoseEntries.value = entries
    }


}
