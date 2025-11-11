package com.saulo.timer.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.saulo.timer.model.WorkoutLog
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutLogDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(workoutLog: WorkoutLog)

    @Query("SELECT * FROM workout_logs ORDER BY completedAt DESC")
    fun getAllLogs(): Flow<List<WorkoutLog>>
}
