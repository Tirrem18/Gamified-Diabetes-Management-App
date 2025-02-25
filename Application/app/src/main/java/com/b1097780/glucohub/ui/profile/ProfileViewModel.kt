package com.b1097780.glucohub.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ProfileViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is Profile Fragment"
    }
    val text: LiveData<String> = _text

    // Coin multiplier with default value of 1
    private val _coinMultiplier = MutableLiveData<Int>().apply { value = 1 }
    val coinMultiplier: LiveData<Int> = _coinMultiplier

    // Function to update the coin multiplier (if needed)
    fun setCoinMultiplier(multiplier: Int) {
        _coinMultiplier.value = multiplier
    }
}
