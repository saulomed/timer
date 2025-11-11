package com.saulo.timer.ui.timer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saulo.timer.data.WorkoutLogDao
import com.saulo.timer.model.WorkoutLog
import com.saulo.timer.util.SoundAndVibrationManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Date

class StopwatchViewModel(
    private val soundAndVibrationManager: SoundAndVibrationManager,
    private val workoutLogDao: WorkoutLogDao,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Main
) : ViewModel() {

    private val _time = MutableStateFlow(0L)
    val time: StateFlow<Long> = _time

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning

    private var timerJob: Job? = null
    private var finalDuration: Long = 0L

    fun getFinalDuration(): Long = finalDuration

    fun startTimer() {
        if (!_isRunning.value) {
            _isRunning.value = true
            timerJob = viewModelScope.launch(defaultDispatcher) {
                var currentTime = _time.value
                while (isActive) { // Use isActive for a cancellable loop
                    delay(1000)
                    currentTime += 1000
                    _time.value = currentTime
                }
            }
        }
    }

    suspend fun pauseTimer() {
        timerJob?.cancelAndJoin()
        _isRunning.value = false
    }

    suspend fun stopTimer() {
        finalDuration = _time.value
        _time.value = 0
        timerJob?.cancelAndJoin()
        _isRunning.value = false

        if (finalDuration >= 1000) { // Only log if it ran for at least a second
            logWorkout(finalDuration, "Completed")
            soundAndVibrationManager.playFinishSound()
            soundAndVibrationManager.vibrate()
        }
    }

    private fun logWorkout(duration: Long, status: String) {
        viewModelScope.launch(defaultDispatcher) {
            workoutLogDao.insert(
                WorkoutLog(
                    workoutType = "FOR TIME (Stopwatch)",
                    durationInMillis = duration,
                    completedAt = Date(),
                    status = status
                )
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
