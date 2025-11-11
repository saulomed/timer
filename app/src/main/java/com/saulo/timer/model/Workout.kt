package com.saulo.timer.model

sealed class Workout {
    data class Amrap(val totalTime: Long) : Workout()
    data class ForTime(val maxTime: Long? = null) : Workout()
    data class Tabata(val rounds: Int, val workTime: Long, val restTime: Long) : Workout()
    data class Emom(val minutes: Int) : Workout()
    data class CustomCircuit(val circuit: Circuit) : Workout()
}
