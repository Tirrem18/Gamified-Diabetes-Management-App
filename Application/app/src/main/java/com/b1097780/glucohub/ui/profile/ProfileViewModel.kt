package com.b1097780.glucohub.ui.profile

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.b1097780.glucohub.PreferencesHelper

class ProfileViewModel(context: Context) : ViewModel() {

    private val _profileTitle = MutableLiveData(PreferencesHelper.getUsername(context))
    val profileTitle: LiveData<String> = _profileTitle

    private val _userMotto = MutableLiveData(PreferencesHelper.getUserMotto(context))
    val userMotto: LiveData<String> = _userMotto

    private val _glucoseEntries = MutableLiveData("Glucose Entries: ${PreferencesHelper.getTotalGlucoseEntries(context)}")
    val glucoseEntries: LiveData<String> = _glucoseEntries

    private val _activityEntries = MutableLiveData("Activity Entries: ${PreferencesHelper.getTotalActivityEntries(context)}")
    val activityEntries: LiveData<String> = _activityEntries

    private val _highestStreak = MutableLiveData("Highest Streak: ${PreferencesHelper.getHighestStreak(context)}")
    val highestStreak: LiveData<String> = _highestStreak

    private val _joiningDate = MutableLiveData("Joining Date: ${PreferencesHelper.getJoiningDate(context)}")
    val joiningDate: LiveData<String> = _joiningDate

    // ✅ Dynamically generate achievements & descriptions
    private val _activityAchievement = MutableLiveData<Pair<String, String>>(
        getActivityAchievement(PreferencesHelper.getTotalActivityEntries(context))
    )
    val activityAchievement: LiveData<Pair<String, String>> = _activityAchievement

    private val _glucoseAchievement = MutableLiveData<Pair<String, String>>(
        getGlucoseAchievement(PreferencesHelper.getTotalGlucoseEntries(context))
    )
    val glucoseAchievement: LiveData<Pair<String, String>> = _glucoseAchievement

    private val _streakAchievement = MutableLiveData<Pair<String, String>>(
        getStreakAchievement(PreferencesHelper.getUserStreak(context))
    )
    val streakAchievement: LiveData<Pair<String, String>> = _streakAchievement

    // ✅ Observe changes and update dynamically
    init {
        _activityEntries.observeForever {
            _activityAchievement.value = getActivityAchievement(PreferencesHelper.getTotalActivityEntries(context))
        }
        _glucoseEntries.observeForever {
            _glucoseAchievement.value = getGlucoseAchievement(PreferencesHelper.getTotalGlucoseEntries(context))
        }
        _highestStreak.observeForever {
            _streakAchievement.value = getStreakAchievement(PreferencesHelper.getUserStreak(context))
        }
    }

    // ✅ Achievement calculation functions (Title + Description)
    private fun getActivityAchievement(entries: Int): Pair<String, String> {
        return when {
            entries >= 1000 -> "Top Logger" to "Logged 1000+ activities!"
            entries >= 500 -> "Momentum" to "Logged 500 activities!"
            entries >= 250 -> "Energy Boost" to "Logged 250 activities!"
            entries >= 100 -> "On the Move" to "Logged 100 activities!"
            entries >= 50 -> "Active Mind" to "Logged 50 activities!"
            entries >= 20 -> "Habit Pro" to "Logged 20 activities!"
            entries >= 10 -> "Step Up" to "Logged 10 activities!"
            else -> "Fresh Start" to "Start tracking activities!"
        }
    }

    private fun getGlucoseAchievement(entries: Int): Pair<String, String> {
        return when {
            entries >= 1000 -> "Insulin King" to "Logged 1000+ glucose readings!"
            entries >= 500 -> "Sugar Sensei" to "Logged 500 glucose readings!"
            entries >= 250 -> "Blood Master" to "Logged 250 glucose readings!"
            entries >= 100 -> "Glucose Pro" to "Logged 100 glucose readings!"
            entries >= 50 -> "Health Watch" to "Logged 50 glucose readings!"
            entries >= 20 -> "Log Expert" to "Logged 20 glucose readings!"
            entries >= 10 -> "Data Builder" to "Logged 10 glucose readings!"
            else -> "New Tracker" to "Start tracking glucose!"
        }
    }

    private fun getStreakAchievement(days: Int): Pair<String, String> {
        return when {
            days >= 365 -> "1 Year Beast" to "Kept a streak for a full year!"
            days >= 250 -> "250-Day Legend" to "Kept a streak for 250 days!"
            days >= 100 -> "100 Days Strong" to "Kept a streak for 100 days!"
            days >= 50 -> "50-Day Champ" to "Kept a streak for 50 days!"
            days >= 30 -> "One Month" to "Kept a streak for 30 days!"
            days >= 10 -> "10-Day Master" to "Kept a streak for 10 days!"
            days >= 5 -> "5-Day Hero" to "Kept a streak for 5 days!"
            else -> "Getting Ready" to "Start your first streak!"
        }
    }
}
