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

        val updatedEntries = _glucoseEntries.value.orEmpty().toMutableList()
        updatedEntries.add(entry)
        _glucoseEntries.value = updatedEntries

        val entryString = convertEntriesToString(updatedEntries)
        PreferencesHelper.setRecentGlucoseEntries(context, entryString)

    }



    // ✅ Load glucose entries from SharedPreferences
    fun loadGlucoseEntries(context: Context) {
        val entryString = PreferencesHelper.getRecentGlucoseEntries(context)
        val loadedEntries = parseEntriesFromString(entryString)

        _glucoseEntries.value = loadedEntries
    }


    // ✅ Convert List<Entry> to a String for storage
    private fun convertEntriesToString(entries: List<Entry>): String {
        val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val today = dateFormat.format(Date())

        return entries.joinToString(";") { entry ->
            "$today,${entry.x},${entry.y}" // ✅ Save date along with time and level
        }
    }

    // ✅ Parse stored String back to List<Entry>
    private fun parseEntriesFromString(entryString: String): List<Entry> {
        val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val today = dateFormat.format(Date())

        return entryString.split(";").mapNotNull {
            val parts = it.split(",")
            if (parts.size == 3 && parts[0] == today) {
                Entry(parts[1].toFloat(), parts[2].toFloat()) // ✅ Only return today's entries
            } else {
                null
            }
        }
    }
}
