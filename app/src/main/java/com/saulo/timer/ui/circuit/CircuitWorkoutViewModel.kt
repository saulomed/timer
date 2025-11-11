package com.saulo.timer.ui.circuit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saulo.timer.data.WorkoutLogDao
import com.saulo.timer.model.Circuit
import com.saulo.timer.model.Exercise
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

sealed class CircuitWorkoutState {
    data class Prepare(val remainingTime: Long) : CircuitWorkoutState()
    data class Work(val exercise: Exercise, val remainingTime: Long) : CircuitWorkoutState()
    data class RestBetweenWork(val remainingTime: Long) : CircuitWorkoutState()
    data class Rest(val nextExercise: Exercise, val remainingTime: Long) : CircuitWorkoutState()
    data class RestBetweenRounds(val remainingTime: Long) : CircuitWorkoutState()
    object Finished : CircuitWorkoutState()
}

class CircuitWorkoutViewModel(
    private val timerProvider: TimerProvider,
    private val soundAndVibrationManager: SoundAndVibrationManager,
    private val preferenceManager: PreferenceManager,
    private val workoutLogDao: WorkoutLogDao,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Main
) : ViewModel() {

    private val _state = MutableStateFlow<CircuitWorkoutState?>(null)
    val state: StateFlow<CircuitWorkoutState?> = _state

    private val _currentRound = MutableStateFlow(1)
    val currentRound: StateFlow<Int> = _currentRound

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning

    private val _isSecondWorkInterval = MutableStateFlow(false)
    val isSecondWorkInterval: StateFlow<Boolean> = _isSecondWorkInterval

    private var timer: Timer? = null
    lateinit var circuit: Circuit // Made public
    var currentExerciseIndex = 0
        private set
    private var totalWorkoutTime: Long = 0

    val isCircuitInitialized: Boolean
        get() = this::circuit.isInitialized

    fun getTotalWorkoutTime(): Long = totalWorkoutTime

    fun getPreparationTimeMillis(): Long = preferenceManager.getPreparationTime() * 1000L

    fun setupCircuit(circuit: Circuit) {
        this.circuit = circuit
        _currentRound.value = 1
        currentExerciseIndex = 0
        _isSecondWorkInterval.value = false // Reset
        val preparationTime = getPreparationTimeMillis()
        _state.value = CircuitWorkoutState.Prepare(preparationTime)
        calculateTotalTime()
    }

    private fun calculateTotalTime() {
        var roundTime: Long = 0
        circuit.exercises.forEachIndexed { index, exercise ->
            roundTime += exercise.workTime1 * 1000
            if (exercise.workTime2 != null && exercise.workTime2 > 0) {
                roundTime += (exercise.restBetweenWork ?: 0) * 1000
                roundTime += exercise.workTime2 * 1000
            }
            // Only add rest time if it's not the last exercise of the round
            if (index < circuit.exercises.size - 1) {
                roundTime += exercise.restTime * 1000
            }
        }

        var total: Long = roundTime * circuit.rounds

        // Add rest time between rounds
        if (circuit.rounds > 1) {
            total += circuit.restBetweenRounds * 1000 * (circuit.rounds - 1)
        }

        totalWorkoutTime = total
    }

    fun startWorkout() {
        if (_isRunning.value) return
        _isRunning.value = true
        startNextInterval()
    }

    private fun startNextInterval() {
        val currentState = _state.value
        val time = when (currentState) {
            is CircuitWorkoutState.Prepare -> currentState.remainingTime
            is CircuitWorkoutState.Work -> currentState.remainingTime
            is CircuitWorkoutState.RestBetweenWork -> currentState.remainingTime
            is CircuitWorkoutState.Rest -> currentState.remainingTime
            is CircuitWorkoutState.RestBetweenRounds -> currentState.remainingTime
            else -> 0
        }

        if (time > 0) {
            timer = timerProvider.create(time, 1000,
                onTick = { millisUntilFinished ->
                    _state.value = when (val s = _state.value) {
                        is CircuitWorkoutState.Prepare -> s.copy(remainingTime = millisUntilFinished)
                        is CircuitWorkoutState.Work -> s.copy(remainingTime = millisUntilFinished)
                        is CircuitWorkoutState.RestBetweenWork -> s.copy(remainingTime = millisUntilFinished)
                        is CircuitWorkoutState.Rest -> s.copy(remainingTime = millisUntilFinished)
                        is CircuitWorkoutState.RestBetweenRounds -> s.copy(remainingTime = millisUntilFinished)
                        else -> s
                    }
                    if (millisUntilFinished <= 3500) { // Beep for the last 3 seconds
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
        // _isRunning.value = false // REMOVIDO: Não deve parar o timer aqui

        when (val currentState = _state.value) {
            is CircuitWorkoutState.Prepare -> {
                val firstExercise = circuit.exercises.first()
                _state.value = CircuitWorkoutState.Work(firstExercise, firstExercise.workTime1 * 1000)
                soundAndVibrationManager.playWorkStartSound()
                soundAndVibrationManager.vibrate()
                if (_isRunning.value) startNextInterval() // Inicia o próximo intervalo
            }
            is CircuitWorkoutState.Work -> {
                soundAndVibrationManager.playWorkEndSound()
                val currentExercise = currentState.exercise
                if (!_isSecondWorkInterval.value && currentExercise.workTime2 != null && currentExercise.workTime2 > 0) {
                    if (currentExercise.restBetweenWork != null && currentExercise.restBetweenWork > 0) {
                        _state.value = CircuitWorkoutState.RestBetweenWork(currentExercise.restBetweenWork * 1000L)
                        soundAndVibrationManager.playRestSound()
                        soundAndVibrationManager.vibrate()
                        if (_isRunning.value) startNextInterval() // Inicia o próximo intervalo
                    } else {
                        _isSecondWorkInterval.value = true
                        _state.value = CircuitWorkoutState.Work(currentExercise, currentExercise.workTime2 * 1000)
                        soundAndVibrationManager.playWorkStartSound()
                        soundAndVibrationManager.vibrate()
                        if (_isRunning.value) startNextInterval() // Inicia o próximo intervalo
                    }
                } else {
                    // Finished work (either first and only, or second)
                    _isSecondWorkInterval.value = false // Reset for next exercise
                    moveToNextExerciseOrRound()
                    if (_state.value !is CircuitWorkoutState.Finished && _isRunning.value) {
                        startNextInterval() // Inicia o próximo intervalo após mover
                    }
                }
            }
            is CircuitWorkoutState.RestBetweenWork -> {
                val currentExercise = circuit.exercises[currentExerciseIndex]
                _isSecondWorkInterval.value = true
                _state.value = CircuitWorkoutState.Work(currentExercise, currentExercise.workTime2!! * 1000)
                soundAndVibrationManager.playWorkStartSound()
                soundAndVibrationManager.vibrate()
                if (_isRunning.value) startNextInterval() // Inicia o próximo intervalo
            }
            is CircuitWorkoutState.Rest -> {
                _isSecondWorkInterval.value = false // Reset for next exercise
                val exercise = circuit.exercises[currentExerciseIndex]
                _state.value = CircuitWorkoutState.Work(exercise, exercise.workTime1 * 1000)
                soundAndVibrationManager.playWorkStartSound()
                soundAndVibrationManager.vibrate()
                if (_isRunning.value) startNextInterval() // Inicia o próximo intervalo
            }
            is CircuitWorkoutState.RestBetweenRounds -> {
                _isSecondWorkInterval.value = false // Reset for next round
                _currentRound.value++
                currentExerciseIndex = 0
                val firstExercise = circuit.exercises.first()
                _state.value = CircuitWorkoutState.Work(firstExercise, firstExercise.workTime1 * 1000)
                soundAndVibrationManager.playWorkStartSound()
                soundAndVibrationManager.vibrate()
                if (_isRunning.value) startNextInterval() // Inicia o próximo intervalo
            }
            is CircuitWorkoutState.Finished -> {
                // Não faz nada, o treino já terminou
            }
            else -> return
        }
    }

    private fun moveToNextExerciseOrRound() {
        val nextExerciseIndex = currentExerciseIndex + 1
        if (nextExerciseIndex < circuit.exercises.size) {
            currentExerciseIndex++
            val nextExercise = circuit.exercises[currentExerciseIndex]
            val restTime = circuit.exercises[currentExerciseIndex - 1].restTime * 1000
            _state.value = CircuitWorkoutState.Rest(nextExercise, restTime)
            soundAndVibrationManager.playRestSound()
            soundAndVibrationManager.vibrate()
        } else {
            // End of round
            if (_currentRound.value < circuit.rounds) {
                _state.value = CircuitWorkoutState.RestBetweenRounds(circuit.restBetweenRounds * 1000)
                soundAndVibrationManager.playRestSound()
                soundAndVibrationManager.vibrate()
            } else {
                finishWorkout()
            }
        }
    }

    private fun finishWorkout() {
        _state.value = CircuitWorkoutState.Finished
        _isRunning.value = false
        logWorkout(totalWorkoutTime, "Completed")
        soundAndVibrationManager.playFinishSound()
        soundAndVibrationManager.vibrate()
    }

    fun pauseWorkout() {
        timer?.cancel()
        _isRunning.value = false
    }

    fun skip() {
        handleIntervalFinish()
    }

    fun stopWorkout() {
        timer?.cancel()
        _isRunning.value = false
        logWorkout(totalWorkoutTime, "Interrupted")
    }

    private fun logWorkout(duration: Long, status: String) {
        viewModelScope.launch(defaultDispatcher) {
            workoutLogDao.insert(
                WorkoutLog(
                    workoutType = "Circuito: ${circuit.name}",
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
