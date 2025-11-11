package com.saulo.timer.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.saulo.timer.model.Circuit
import kotlinx.coroutines.flow.Flow

@Dao
interface CircuitDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(circuit: Circuit): Long

    @Query("SELECT * FROM circuits ORDER BY name ASC")
    fun getAllCircuits(): Flow<List<Circuit>>

    @Query("SELECT * FROM circuits WHERE id = :id")
    fun getCircuitById(id: Int): Flow<Circuit>

    @Delete
    suspend fun delete(circuit: Circuit)
}
