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

}
