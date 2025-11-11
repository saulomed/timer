package com.saulo.timer.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.saulo.timer.model.Circuit
import com.saulo.timer.model.WorkoutLog

@Database(entities = [Circuit::class, WorkoutLog::class], version = 3, exportSchema = false)
@TypeConverters(Converters::class, DateConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun circuitDao(): CircuitDao
    abstract fun workoutLogDao(): WorkoutLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "timer_database"
                )
                .addMigrations(MIGRATION_2_3)
                .build()
                INSTANCE = instance
                instance
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE workout_logs ADD COLUMN status TEXT NOT NULL DEFAULT 'Completed'")
            }
        }
    }
}
