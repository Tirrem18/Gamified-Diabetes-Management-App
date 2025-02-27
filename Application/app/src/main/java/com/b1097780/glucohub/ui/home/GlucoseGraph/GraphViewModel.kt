package com.b1097780.glucohub.ui.home.GlucoseGraph

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.github.mikephil.charting.data.Entry
import java.text.SimpleDateFormat
import java.util.*

class GraphViewModel : ViewModel() {

    private val _glucoseEntries = MutableLiveData<List<Entry>>(emptyList())
    val glucoseEntries: LiveData<List<Entry>> = _glucoseEntries

    fun setGlucoseEntries(entries: List<Entry>) {
        _glucoseEntries.value = entries
    }

    fun addGlucoseEntry(entry: Entry, context: Context) {
        val updatedEntries = _glucoseEntries.value.orEmpty().toMutableList()
        updatedEntries.add(entry)
        _glucoseEntries.value = updatedEntries

        // ✅ Save to SharedPreferences using context
        saveGlucoseEntries(updatedEntries, context)
    }

    fun saveGlucoseEntries(entries: List<Entry>, context: Context) {
        val sharedPrefs = context.getSharedPreferences("GlucoHubPrefs", Context.MODE_PRIVATE)
        val editor = sharedPrefs.edit()
        val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val today = dateFormat.format(Date())

        val entryString = entries.joinToString(";") { entry ->
            "$today,${entry.x},${entry.y}" // Save date along with time and level
        }

        editor.putString("glucoseEntries", entryString)
        editor.apply()
    }

    fun loadGlucoseEntries(context: Context) {
        val sharedPrefs = context.getSharedPreferences("GlucoHubPrefs", Context.MODE_PRIVATE)
        val entryString = sharedPrefs.getString("glucoseEntries", "") ?: ""
        val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val today = dateFormat.format(Date())

        val loadedEntries = entryString.split(";").mapNotNull {
            val parts = it.split(",")
            if (parts.size == 3 && parts[0] == today) {
                Entry(parts[1].toFloat(), parts[2].toFloat()) // ✅ Only return today's entries
            } else {
                null
            }
        }

        _glucoseEntries.value = loadedEntries // ✅ Notify UI to update
    }
}
