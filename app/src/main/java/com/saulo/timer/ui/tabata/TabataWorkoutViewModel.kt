package com.saulo.timer.ui.tabata

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

sealed class TabataState {
    data class Prepare(val remainingTime: Long) : TabataState()
    object Finished : TabataState()
    data class Work(val remainingTime: Long) : TabataState()
    data class Rest(val remainingTime: Long) : TabataState()
}

class TabataWorkoutViewModel(
    private val timerProvider: TimerProvider,
    private val soundAndVibrationManager: SoundAndVibrationManager,
    private val preferenceManager: PreferenceManager,
    private val workoutLogDao: WorkoutLogDao,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Main
) : ViewModel() {

    private val _state = MutableStateFlow<TabataState>(TabataState.Prepare(0))
    val state: StateFlow<TabataState> = _state

    private val _currentRound = MutableStateFlow(1)
    val currentRound: StateFlow<Int> = _currentRound

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning

    private var timer: Timer? = null
    private var totalRounds = 0
    private var workTime: Long = 0
    private var restTime: Long = 0

    private var intervalStartTime: Long = 0L
    private var totalActualElapsedTime: Long = 0L

    fun getPreparationTimeMillis(): Long = preferenceManager.getPreparationTime() * 1000L

    fun setup(rounds: Int, work: Long, rest: Long) {
        totalRounds = rounds
        workTime = work
        restTime = rest
        _currentRound.value = 1
        totalActualElapsedTime = 0L

        val preparationTime = getPreparationTimeMillis()
        if (preparationTime > 0) {
            _state.value = TabataState.Prepare(preparationTime)
        } else {
            _state.value = TabataState.Work(workTime)
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
            is TabataState.Prepare -> currentState.remainingTime
            is TabataState.Work -> currentState.remainingTime
            is TabataState.Rest -> currentState.remainingTime
            is TabataState.Finished -> 0
        }

        if (time > 0) {
            intervalStartTime = System.currentTimeMillis() // This will be tricky to test
            timer = timerProvider.create(time, 1000,
                onTick = { millisUntilFinished ->
                    _state.value = when (val s = _state.value) {
                        is TabataState.Prepare -> s.copy(remainingTime = millisUntilFinished)
                        is TabataState.Work -> s.copy(remainingTime = millisUntilFinished)
                        is TabataState.Rest -> s.copy(remainingTime = millisUntilFinished)
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

        val intervalDuration = when (_state.value) {
            is TabataState.Work -> workTime
            is TabataState.Rest -> restTime
            else -> 0L // Do not add time for Prepare or Finished state
        }
        totalActualElapsedTime += intervalDuration

        when (_state.value) {
            is TabataState.Prepare -> {
                _state.value = TabataState.Work(workTime)
                soundAndVibrationManager.playWorkStartSound()
                soundAndVibrationManager.vibrate()
                startNextInterval()
            }
            is TabataState.Work -> {
                soundAndVibrationManager.playWorkEndSound()
                if (_currentRound.value < totalRounds) {
                    _state.value = TabataState.Rest(restTime)
                    soundAndVibrationManager.playRestSound()
                    soundAndVibrationManager.vibrate()
                    startNextInterval()
                } else {
                    finishWorkout()
                }
            }
            is TabataState.Rest -> {
                _currentRound.value++
                _state.value = TabataState.Work(workTime)
                soundAndVibrationManager.playWorkStartSound()
                soundAndVibrationManager.vibrate()
                startNextInterval()
            }
            else -> {}
        }
    }

    private fun finishWorkout() {
        _state.value = TabataState.Finished
        _isRunning.value = false
        logWorkout(totalActualElapsedTime, "Completed")
        soundAndVibrationManager.playFinishSound()
        soundAndVibrationManager.vibrate()
    }

    fun pauseWorkout() {
        timer?.cancel()
        _isRunning.value = false
        if (intervalStartTime > 0 && _state.value !is TabataState.Prepare) {
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
                    workoutType = "TABATA",
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
