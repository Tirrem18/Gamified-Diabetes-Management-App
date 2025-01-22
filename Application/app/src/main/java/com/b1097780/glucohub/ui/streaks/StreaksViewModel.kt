package com.b1097780.glucohub.ui.streaks

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class StreaksViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is Streaks Fragment"
    }
    val text: LiveData<String> = _text
}