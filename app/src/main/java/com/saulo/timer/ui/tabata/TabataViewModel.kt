package com.saulo.timer.ui.tabata

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class TabataViewModel : ViewModel() {

    private val _rounds = MutableStateFlow(8)
    val rounds: StateFlow<Int> = _rounds

    private val _workTime = MutableStateFlow(20)
    val workTime: StateFlow<Int> = _workTime

    private val _restTime = MutableStateFlow(10)
    val restTime: StateFlow<Int> = _restTime

    fun setRounds(newRounds: Int) {
        if (newRounds > 0) {
            _rounds.value = newRounds
        }
    }

    fun setWorkTime(newWorkTime: Int) {
        if (newWorkTime > 0) {
            _workTime.value = newWorkTime
        }
    }

    fun setRestTime(newRestTime: Int) {
        if (newRestTime >= 0) {
            _restTime.value = newRestTime
        }
    }
}
