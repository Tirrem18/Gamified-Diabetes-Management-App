package com.b1097780.glucohub.ui.findusers

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class FindUsersViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is Find Users Fragment"
    }
    val text: LiveData<String> = _text

}
