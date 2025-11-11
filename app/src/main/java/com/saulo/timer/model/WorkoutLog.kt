package com.saulo.timer.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "workout_logs")
data class WorkoutLog(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val workoutType: String,
    val durationInMillis: Long,
    val completedAt: Date,
    val status: String // "Completed" or "Interrupted"
)
