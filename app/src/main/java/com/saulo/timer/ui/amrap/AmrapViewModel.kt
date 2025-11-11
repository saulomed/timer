package com.saulo.timer.ui.amrap

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AmrapViewModel : ViewModel() {

    private val _time = MutableStateFlow(0)
    val time: StateFlow<Int> = _time

    fun setTime(newTime: Int) {
        _time.value = newTime
    }
}
