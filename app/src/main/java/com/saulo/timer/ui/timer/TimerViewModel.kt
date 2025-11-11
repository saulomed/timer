package com.saulo.timer.ui.timer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saulo.timer.data.WorkoutLogDao
import com.saulo.timer.model.WorkoutLog
import com.saulo.timer.util.PreferenceManager
import com.saulo.timer.util.SoundAndVibrationManager
import com.saulo.timer.util.Timer
import com.saulo.timer.util.TimerProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Date

sealed class TimerState {
    data class Running(val remainingTime: Long) : TimerState()
    data class Paused(val remainingTime: Long) : TimerState()
    object Finished : TimerState()
}

class TimerViewModel(
    private val timerProvider: TimerProvider,
    private val soundAndVibrationManager: SoundAndVibrationManager,
    private val preferenceManager: PreferenceManager,
    private val workoutLogDao: WorkoutLogDao,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Main
) : ViewModel() {

    private val _state = MutableStateFlow<TimerState?>(null)
    val state: StateFlow<TimerState?> = _state

    val isRunning: Boolean
        get() = _state.value is TimerState.Running

    private var timer: Timer? = null
    private var initialTime: Long = 0L
    private var workoutType: String = ""

    var isPreparing: Boolean = false
        private set

    var preparationTime: Long = 0L
        private set

    fun setup(type: String, timeInMillis: Long) {
        this.workoutType = type
        this.initialTime = timeInMillis
        preparationTime = preferenceManager.getPreparationTime() * 1000L
        if (preparationTime > 0) {
            isPreparing = true
            _state.value = TimerState.Paused(preparationTime)
        } else {
            _state.value = TimerState.Paused(timeInMillis)
        }
    }

    fun startTimer() {
        val currentState = _state.value
        val time = when (currentState) {
            is TimerState.Paused -> currentState.remainingTime
            else -> return
        }

        if (time > 0) {
            _state.value = TimerState.Running(time)
            timer = timerProvider.create(time, 1000,
                onTick = { millisUntilFinished ->
                    _state.value = TimerState.Running(millisUntilFinished)
                    if (millisUntilFinished <= 3500) {
                        soundAndVibrationManager.playCountdownBeepSound()
                    }
                },
                onFinish = { handleFinish() }
            ).start()
        }
    }

    private fun handleFinish() {
        if (isPreparing) {
            isPreparing = false
            soundAndVibrationManager.playWorkStartSound()
            soundAndVibrationManager.vibrate()
            // Inicia o timer principal automaticamente
            _state.value = TimerState.Paused(initialTime)
            startTimer()
        } else {
            _state.value = TimerState.Finished
            logWorkout(initialTime, "Completed")
            soundAndVibrationManager.playFinishSound()
            soundAndVibrationManager.vibrate()
        }
    }

    fun pauseTimer() {
        val currentTime = (_state.value as? TimerState.Running)?.remainingTime ?: return
        timer?.cancel()
        _state.value = TimerState.Paused(currentTime)
    }

    fun stopTimer() {
        timer?.cancel()
        logWorkout(getElapsedTime(), "Interrupted")
    }

    fun getElapsedTime(): Long {
        if (isPreparing) {
            return 0L
        }
        val remainingTime = when (val currentState = _state.value) {
            is TimerState.Paused -> currentState.remainingTime
            is TimerState.Running -> currentState.remainingTime
            else -> 0L
        }
        return initialTime - remainingTime
    }

    private fun logWorkout(duration: Long, status: String) {
        if (duration <= 0) return
        viewModelScope.launch(defaultDispatcher) {
            workoutLogDao.insert(
                WorkoutLog(
                    workoutType = workoutType,
                    durationInMillis = duration,
                    completedAt = Date(),
                    status = status
                )
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        timer?.cancel()
    }
}
