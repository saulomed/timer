package com.saulo.timer.ui.history

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.saulo.timer.data.AppDatabase
import com.saulo.timer.model.Circuit
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val circuitDao = AppDatabase.getDatabase(application).circuitDao()

    val allCircuits: StateFlow<List<Circuit>> = circuitDao.getAllCircuits()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun deleteCircuit(circuit: Circuit) {
        viewModelScope.launch {
            circuitDao.delete(circuit)
        }
    }
}
