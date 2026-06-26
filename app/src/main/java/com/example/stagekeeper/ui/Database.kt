package com.example.stagekeeper

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

// Entity representing a custom map marker saved by the user
@Entity(tableName = "meetup_locations")
data class MeetupLocation(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val latitude: Double,
    val longitude: Double,
    val note: String = ""
)

// Data Access Object handling local database interactions
@Dao
interface LocationDao {
    @Query("SELECT * FROM meetup_locations")
    fun getAllLocations(): Flow<List<MeetupLocation>>

    @Insert
    fun insertLocation(location: MeetupLocation)

    @Query("DELETE FROM meetup_locations")
    fun deleteAll()
}

// Room Database setup with destructive migration for R&D testing
@Database(entities = [MeetupLocation::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun locationDao(): LocationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "stagekeeper_database"
                )
                    // Wipes old schema data to prevent crashes during rapid development
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}