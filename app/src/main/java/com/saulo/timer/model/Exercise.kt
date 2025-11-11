package com.saulo.timer.model

data class Exercise(
    val name: String,
    val workTime1: Long,
    val workTime2: Long? = null,
    val restTime: Long,
    val restBetweenWork: Int? = null, // Rest between work1 and work2 in seconds
    val notes: String? = null
)
