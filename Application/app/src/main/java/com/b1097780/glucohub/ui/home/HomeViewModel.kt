package com.b1097780.glucohub.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.github.mikephil.charting.data.Entry

class HomeViewModel : ViewModel() {

    // LiveData for Glucose Section
    private val _glucoseTitle = MutableLiveData<String>().apply {
        value = "Glucose Data Here"
    }
    val glucoseText: LiveData<String> = _glucoseTitle

    // LiveData for Planner Section
    private val _plannerTitle = MutableLiveData<String>().apply {
        value = "Planner Data Here"
    }
    val plannerText: LiveData<String> = _plannerTitle

    // Sample glucose data (Time in hours, Glucose level)
    private val _glucoseEntries = MutableLiveData<List<Entry>>().apply {
        value = listOf(
            Entry(1.0f, 5.5f), Entry(1.34f, 7.4f), Entry(1.57f, 4.5f), Entry(1.84f, 7.9f), Entry(2.15f, 4.2f),
            Entry(2.52f, 8.8f), Entry(2.79f, 13.4f), Entry(3.02f, 14.5f), Entry(3.33f, 11.5f), Entry(3.56f, 14.9f),
            Entry(3.85f, 10.6f), Entry(4.16f, 9.3f), Entry(4.43f, 5.5f), Entry(4.8f, 7.0f), Entry(5.02f, 6.7f),
            Entry(5.39f, 7.0f), Entry(5.68f, 9.3f), Entry(6.02f, 14.0f), Entry(6.27f, 10.3f), Entry(6.48f, 11.7f),
            Entry(6.79f, 7.8f), Entry(7.06f, 5.5f), Entry(7.42f, 5.6f), Entry(7.7f, 5.6f), Entry(8.0f, 6.9f),
            Entry(8.24f, 8.0f), Entry(8.81f, 9.5f), Entry(9.2f, 13.2f), Entry(9.39f, 13.4f), Entry(9.62f, 12.4f),
            Entry(9.82f, 11.1f), Entry(10.11f, 9.3f), Entry(10.42f, 13.8f), Entry(10.81f, 12.6f), Entry(11.04f, 14.0f),
            Entry(11.4f, 12.6f), Entry(11.75f, 12.3f), Entry(12.08f, 10.9f), Entry(12.32f, 10.9f), Entry(12.7f, 13.8f),
            //Entry(12.99f, 12.0f), Entry(13.37f, 8.9f), Entry(13.76f, 13.5f), Entry(13.97f, 13.1f), Entry(14.33f, 11.8f),
            //Entry(14.56f, 11.8f), Entry(14.93f, 9.8f), Entry(15.32f, 13.3f), Entry(15.68f, 14.4f), Entry(15.9f, 10.7f),
            //Entry(16.14f, 10.2f), Entry(16.45f, 12.2f), Entry(16.77f, 8.1f), Entry(17.09f, 5.6f), Entry(17.49f, 6.8f),
            //Entry(17.82f, 7.5f), Entry(18.07f, 9.7f), Entry(18.43f, 12.0f), Entry(18.66f, 9.4f), Entry(18.92f, 11.9f),
            //Entry(19.15f, 10.2f), Entry(19.39f, 6.2f), Entry(19.77f, 6.7f), Entry(20.08f, 3.6f), Entry(20.41f, 7.0f),
            //Entry(20.74f, 10.0f), Entry(21.01f, 9.4f), Entry(21.34f, 8.5f), Entry(21.63f, 5.0f), Entry(21.89f, 3.3f),
            //Entry(22.23f, 8.2f), Entry(22.62f, 12.8f), Entry(22.89f, 14.8f), Entry(23.29f, 10.0f), Entry(23.51f, 11.1f),
           // Entry(23.79f, 6.5f)





        )
    }

    val glucoseEntries: LiveData<List<Entry>> = _glucoseEntries

    // Function to update glucose data (for Firebase later)
    fun addGlucoseEntry(time: Float, value: Float) {
        val currentList = _glucoseEntries.value?.toMutableList() ?: mutableListOf()
        currentList.add(Entry(time, value))
        _glucoseEntries.value = currentList
    }
}
