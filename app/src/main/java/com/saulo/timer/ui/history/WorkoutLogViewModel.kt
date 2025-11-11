package com.saulo.timer.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saulo.timer.data.WorkoutLogDao
import com.saulo.timer.model.WorkoutLog
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class WorkoutLogViewModel(private val workoutLogDao: WorkoutLogDao) : ViewModel() {

    val allLogs: StateFlow<List<WorkoutLog>> = workoutLogDao.getAllLogs()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}
