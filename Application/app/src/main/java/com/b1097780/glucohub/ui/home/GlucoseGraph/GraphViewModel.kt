package com.b1097780.glucohub.ui.home.GlucoseGraph

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.b1097780.glucohub.MainActivity
import com.github.mikephil.charting.data.Entry

class GraphViewModel : ViewModel() {

    private val _glucoseEntries = MutableLiveData<List<Entry>>(emptyList())
    val glucoseEntries: LiveData<List<Entry>> = _glucoseEntries

    fun setGlucoseEntries(entries: List<Entry>) {
        _glucoseEntries.value = entries
    }

    fun addGlucoseEntry(entry: Entry, mainActivity: MainActivity) {
        val updatedEntries = _glucoseEntries.value.orEmpty().toMutableList()
        updatedEntries.add(entry)
        _glucoseEntries.value = updatedEntries

        // ✅ Save updated data to SharedPreferences
        mainActivity.saveGlucoseEntries(updatedEntries)
    }

    fun loadSavedEntries(mainActivity: MainActivity) {
        val savedEntries = mainActivity.loadGlucoseEntries()
        _glucoseEntries.value = savedEntries
    }
}

