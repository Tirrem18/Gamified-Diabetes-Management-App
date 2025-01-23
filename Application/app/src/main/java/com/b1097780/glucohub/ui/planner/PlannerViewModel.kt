package com.b1097780.glucohub.ui.planner

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class PlannerViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is planner Fragment"
    }
    val text: LiveData<String> = _text
}