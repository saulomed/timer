package com.saulo.timer.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "circuits")
data class Circuit(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val exercises: List<Exercise>,
    val rounds: Int,
    val restBetweenRounds: Long
)
