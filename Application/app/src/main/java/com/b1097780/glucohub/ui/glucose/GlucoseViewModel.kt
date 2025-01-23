package com.b1097780.glucohub.ui.glucose

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class GlucoseViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is glucose Fragment"
    }
    val text: LiveData<String> = _text
}