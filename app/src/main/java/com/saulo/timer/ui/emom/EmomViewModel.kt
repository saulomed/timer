package com.saulo.timer.ui.emom

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class EmomViewModel : ViewModel() {

    private val _minutes = MutableStateFlow(1)
    val minutes: StateFlow<Int> = _minutes

    fun setMinutes(newMinutes: Int) {
        if (newMinutes > 0) { // EMOM must be at least 1 minute
            _minutes.value = newMinutes
        }
    }
}
