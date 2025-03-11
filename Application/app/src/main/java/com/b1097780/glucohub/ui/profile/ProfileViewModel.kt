package com.b1097780.glucohub.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ProfileViewModel : ViewModel() {

    private val _profileTitle = MutableLiveData("Jacob's Profile")
    val profileTitle: LiveData<String> = _profileTitle

    private val _sampleText = MutableLiveData("Sample Text")
    val sampleText: LiveData<String> = _sampleText

    private val _glucoseEntries = MutableLiveData("Glucose Entries")
    val glucoseEntries: LiveData<String> = _glucoseEntries

    private val _activityEntries = MutableLiveData("Activity Entries")
    val activityEntries: LiveData<String> = _activityEntries
}
