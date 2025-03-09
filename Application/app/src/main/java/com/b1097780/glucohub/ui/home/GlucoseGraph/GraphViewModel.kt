package com.b1097780.glucohub.ui.home.GlucoseGraph

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.b1097780.glucohub.PreferencesHelper
import com.github.mikephil.charting.data.Entry
import java.text.SimpleDateFormat
import java.util.*

class GraphViewModel : ViewModel() {

    private val _glucoseEntries = MutableLiveData<List<Entry>>(emptyList())
    val glucoseEntries: LiveData<List<Entry>> = _glucoseEntries

    // ✅ Add new glucose entry and save it
    fun addGlucoseEntry(entry: Entry, context: Context) {
        val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val today = dateFormat.format(Date())

        val updatedEntries = _glucoseEntries.value.orEmpty().toMutableList()
        updatedEntries.add(entry)
        _glucoseEntries.value = updatedEntries

        // ✅ Retrieve existing activity data for the same day
        val existingActivityEntries = PreferencesHelper.getActivityEntriesForDate(context, today)

        // ✅ Save updated glucose entries alongside activity data
        PreferencesHelper.saveGlucoseEntries(context, today, updatedEntries.map { Pair(it.x.toString(), it.y) })

    }

    // ✅ Load glucose entries from JSON storage
    fun loadGlucoseEntries(context: Context) {
        val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val today = dateFormat.format(Date())

        val loadedEntries = PreferencesHelper.getGlucoseEntriesForDate(context, today)
            .map { Entry(it.first.toFloat(), it.second) }

        _glucoseEntries.value = loadedEntries
    }
}
