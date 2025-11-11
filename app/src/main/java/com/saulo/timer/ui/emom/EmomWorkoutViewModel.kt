package com.saulo.timer.ui.emom

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

sealed class EmomState {
    data class Prepare(val remainingTime: Long) : EmomState()
    data class Work(val remainingTime: Long, val currentMinute: Int) : EmomState()
    object Finished : EmomState()
}

class EmomWorkoutViewModel(
    private val timerProvider: TimerProvider,
    private val soundAndVibrationManager: SoundAndVibrationManager,
    private val preferenceManager: PreferenceManager,
    private val workoutLogDao: WorkoutLogDao,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Main
) : ViewModel() {

    private val _state = MutableStateFlow<EmomState>(EmomState.Prepare(0))
    val state: StateFlow<EmomState> = _state

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning

    private var timer: Timer? = null
    private var totalMinutes = 0

    private var intervalStartTime: Long = 0L
    private var totalActualElapsedTime: Long = 0L

    fun getPreparationTimeMillis(): Long = preferenceManager.getPreparationTime() * 1000L

    fun setup(minutes: Int) {
        totalMinutes = minutes
        totalActualElapsedTime = 0L
        val preparationTime = getPreparationTimeMillis()
        if (preparationTime > 0) {
            _state.value = EmomState.Prepare(preparationTime)
        } else {
            _state.value = EmomState.Work(60000, 1)
        }
    }

    fun startWorkout() {
        if (_isRunning.value) return
        _isRunning.value = true
        startNextInterval()
    }

    private fun startNextInterval() {
        val currentState = _state.value
        val time = when (currentState) {
            is EmomState.Prepare -> currentState.remainingTime
            is EmomState.Work -> currentState.remainingTime
            is EmomState.Finished -> 0
        }

        if (time > 0) {
            intervalStartTime = System.currentTimeMillis()
            timer = timerProvider.create(time, 1000,
                onTick = { millisUntilFinished ->
                    _state.value = when (val s = _state.value) {
                        is EmomState.Prepare -> s.copy(remainingTime = millisUntilFinished)
                        is EmomState.Work -> s.copy(remainingTime = millisUntilFinished)
                        else -> s
                    }
                    if (millisUntilFinished <= 3500) {
                        soundAndVibrationManager.playCountdownBeepSound()
                    }
                },
                onFinish = { handleIntervalFinish() }
            ).start()
        } else {
            handleIntervalFinish()
        }
    }

    private fun handleIntervalFinish() {
        timer?.cancel()

        if (_state.value !is EmomState.Prepare) {
            val intervalDuration = when (_state.value) {
                is EmomState.Work -> 60000L
                else -> 0L
            }
            totalActualElapsedTime += intervalDuration
        }

        when (val currentState = _state.value) {
            is EmomState.Prepare -> {
                _state.value = EmomState.Work(60000, 1)
                soundAndVibrationManager.playWorkStartSound()
                soundAndVibrationManager.vibrate()
                startNextInterval()
            }
            is EmomState.Work -> {
                if (currentState.currentMinute < totalMinutes) {
                    _state.value = EmomState.Work(60000, currentState.currentMinute + 1)
                    soundAndVibrationManager.playWorkStartSound()
                    soundAndVibrationManager.vibrate()
                    startNextInterval()
                } else {
                    finishWorkout()
                }
            }
            else -> {}
        }
    }

    private fun finishWorkout() {
        _state.value = EmomState.Finished
        _isRunning.value = false
        logWorkout(totalActualElapsedTime, "Completed")
        soundAndVibrationManager.playFinishSound()
        soundAndVibrationManager.vibrate()
    }

    fun pauseWorkout() {
        timer?.cancel()
        _isRunning.value = false
        if (intervalStartTime > 0 && _state.value !is EmomState.Prepare) {
            val currentIntervalDuration = System.currentTimeMillis() - intervalStartTime
            totalActualElapsedTime += currentIntervalDuration
            intervalStartTime = 0L
        }
    }

    fun skip() {
        pauseWorkout()
        handleIntervalFinish()
    }

    fun stopWorkoutAndGetElapsedTime(): Long {
        pauseWorkout()
        if (_state.value is EmomState.Prepare) {
            return 0L
        }
        logWorkout(totalActualElapsedTime, "Interrupted")
        return totalActualElapsedTime
    }

    fun getFinalElapsedTime(): Long {
        return totalActualElapsedTime
    }

    private fun logWorkout(duration: Long, status: String) {
        if (duration <= 0) return
        viewModelScope.launch(defaultDispatcher) {
            workoutLogDao.insert(
                WorkoutLog(
                    workoutType = "EMOM",
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
