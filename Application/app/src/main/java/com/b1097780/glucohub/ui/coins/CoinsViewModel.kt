package com.b1097780.glucohub.ui.coins

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CoinsViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is Coins Fragment"
    }
    val text: LiveData<String> = _text
}