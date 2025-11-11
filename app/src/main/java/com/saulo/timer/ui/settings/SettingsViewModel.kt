package com.saulo.timer.ui.settings

import androidx.lifecycle.ViewModel
import com.saulo.timer.util.PreferenceManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SettingsViewModel(private val preferenceManager: PreferenceManager) : ViewModel() {

    private val _preparationTime = MutableStateFlow(preferenceManager.getPreparationTime())
    val preparationTime: StateFlow<Int> = _preparationTime

    fun setPreparationTime(seconds: Int) {
        preferenceManager.setPreparationTime(seconds)
        _preparationTime.value = seconds
    }
}
