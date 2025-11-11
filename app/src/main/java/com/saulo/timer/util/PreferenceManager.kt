package com.saulo.timer.util

import android.content.Context
import android.content.SharedPreferences

class PreferenceManager(context: Context) {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("TimerAppPrefs", Context.MODE_PRIVATE)

    companion object {
        const val KEY_PREPARATION_TIME = "preparation_time"
        const val DEFAULT_PREPARATION_TIME = 10 // in seconds
    }

    fun getPreparationTime(): Int {
        return sharedPreferences.getInt(KEY_PREPARATION_TIME, DEFAULT_PREPARATION_TIME)
    }

    fun setPreparationTime(seconds: Int) {
        sharedPreferences.edit().putInt(KEY_PREPARATION_TIME, seconds).apply()
    }
}
