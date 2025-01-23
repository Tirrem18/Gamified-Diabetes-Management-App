package com.b1097780.glucohub.ui.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class DataViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is Data Fragment"
    }
    val text: LiveData<String> = _text
}