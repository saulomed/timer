package com.saulo.timer.util

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.saulo.timer.data.AppDatabase
import com.saulo.timer.ui.circuit.CircuitViewModel
import com.saulo.timer.ui.circuit.CircuitWorkoutViewModel
import com.saulo.timer.ui.emom.EmomWorkoutViewModel
import com.saulo.timer.ui.history.HistoryViewModel
import com.saulo.timer.ui.history.WorkoutLogViewModel
import com.saulo.timer.ui.settings.SettingsViewModel
import com.saulo.timer.ui.tabata.TabataWorkoutViewModel
import com.saulo.timer.ui.timer.StopwatchViewModel
import com.saulo.timer.ui.timer.TimerViewModel
import kotlinx.coroutines.Dispatchers

class TimerViewModelFactory(private val application: Application) : ViewModelProvider.Factory {

    private val soundAndVibrationManager by lazy { SoundAndVibrationManager(application) }
    private val preferenceManager by lazy { PreferenceManager(application) }
    private val timerProvider by lazy { AndroidTimerProvider() }
    private val appDatabase by lazy { AppDatabase.getDatabase(application) }

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(CircuitViewModel::class.java) -> {
                CircuitViewModel(appDatabase.circuitDao()) as T
            }
            modelClass.isAssignableFrom(HistoryViewModel::class.java) -> {
                HistoryViewModel(application) as T
            }
            modelClass.isAssignableFrom(WorkoutLogViewModel::class.java) -> {
                WorkoutLogViewModel(appDatabase.workoutLogDao()) as T
            }
            modelClass.isAssignableFrom(TabataWorkoutViewModel::class.java) -> {
                TabataWorkoutViewModel(timerProvider, soundAndVibrationManager, preferenceManager, appDatabase.workoutLogDao(), Dispatchers.IO) as T
            }
            modelClass.isAssignableFrom(EmomWorkoutViewModel::class.java) -> {
                EmomWorkoutViewModel(timerProvider, soundAndVibrationManager, preferenceManager, appDatabase.workoutLogDao(), Dispatchers.IO) as T
            }
            modelClass.isAssignableFrom(CircuitWorkoutViewModel::class.java) -> {
                CircuitWorkoutViewModel(timerProvider, soundAndVibrationManager, preferenceManager, appDatabase.workoutLogDao(), Dispatchers.IO) as T
            }
            modelClass.isAssignableFrom(TimerViewModel::class.java) -> {
                TimerViewModel(timerProvider, soundAndVibrationManager, preferenceManager, appDatabase.workoutLogDao(), Dispatchers.IO) as T
            }
            modelClass.isAssignableFrom(StopwatchViewModel::class.java) -> {
                StopwatchViewModel(soundAndVibrationManager, appDatabase.workoutLogDao(), Dispatchers.IO) as T
            }
            modelClass.isAssignableFrom(SettingsViewModel::class.java) -> {
                SettingsViewModel(preferenceManager) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        } as T
    }
}
