package com.b1097780.glucohub.ui.streaks

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.b1097780.glucohub.PreferencesHelper

class StreaksViewModel : ViewModel() {

    private val _currentStreak = MutableLiveData<String>()
    val currentStreak: LiveData<String> = _currentStreak

    private val _highestStreak = MutableLiveData<String>()
    val highestStreak: LiveData<String> = _highestStreak

    private val _multiplierText = MutableLiveData<String>()
    val multiplierText: LiveData<String> = _multiplierText

    fun loadData(context: Context) {
        val current = PreferencesHelper.getUserStreak(context)
        val highest = PreferencesHelper.getHighestStreak(context)
        val multiplier = PreferencesHelper.getCoinMultiplier(context)

        _currentStreak.value = "Your Current Streak: $current"
        _highestStreak.value = "Your Highest Streak: $highest"
        _multiplierText.value = "Your Current Multiplier Progress: x$multiplier"
    }
}
