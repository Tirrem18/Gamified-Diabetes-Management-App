package com.b1097780.glucohub.ui.graph

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.github.mikephil.charting.data.Entry

class GraphViewModel : ViewModel() {

    private val _glucoseEntries = MutableLiveData<List<Entry>>(emptyList())
    val glucoseEntries: LiveData<List<Entry>> = _glucoseEntries

    fun setGlucoseEntries(entries: List<Entry>) {
        _glucoseEntries.value = entries
    }

    fun addGlucoseEntry(entry: Entry) {
        val updatedEntries = _glucoseEntries.value.orEmpty().toMutableList()
        updatedEntries.add(entry)
        _glucoseEntries.value = updatedEntries
    }
}
