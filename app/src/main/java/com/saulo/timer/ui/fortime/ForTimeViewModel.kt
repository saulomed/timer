package com.saulo.timer.ui.fortime

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ForTimeViewModel : ViewModel() {

    private val _timeCap = MutableStateFlow<Int?>(null)
    val timeCap: StateFlow<Int?> = _timeCap

    fun setTimeCap(newTimeCap: Int?) {
        _timeCap.value = newTimeCap
    }
}
