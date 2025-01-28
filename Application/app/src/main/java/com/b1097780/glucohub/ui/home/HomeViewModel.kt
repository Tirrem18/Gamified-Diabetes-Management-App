package com.b1097780.glucohub.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map

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

    // LiveData for Coin and Streak Buttons
    private val _coinValue = MutableLiveData<Int>().apply {
        value = 1000 // Example starting value
    }
    val coinValue: LiveData<String> = _coinValue.map { formatNumber(it) }

    private val _streakValue = MutableLiveData<Int>().apply {
        value = 500 // Example starting value
    }
    val streakValue: LiveData<String> = _streakValue.map { formatNumber(it) }

    // Update values dynamically
    fun updateCoinValue(newValue: Int) {
        _coinValue.value = newValue
    }

    fun updateStreakValue(newValue: Int) {
        _streakValue.value = newValue
    }

    // Helper function to format numbers
    private fun formatNumber(value: Int): String {
        return when {
            value > 99999 -> "100K+"
            value > 999 -> "${value / 1000}K+"
            else -> value.toString()
        }
    }
}
