package com.example.stagekeeper

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

// Changed: Using a String for pinId so it matches Firebase, and added partyId
@Entity(tableName = "meetup_locations")
data class MeetupLocation(
    @PrimaryKey val pinId: String = "",
    val partyId: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val note: String = ""
)

// Defining my map pin database queries here
@Dao
interface LocationDao {
    // Keeps the map updated in real-time using Flow
    @Query("SELECT * FROM meetup_locations")
    fun getAllLocations(): Flow<List<MeetupLocation>>

    // Changed: Replace strategy ensures when cloud syncs an existing pin, it just updates it
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertLocation(location: MeetupLocation)

    @Query("DELETE FROM meetup_locations")
    fun deleteAll()
}

// Bumped version to 5 to cleanly apply the new table schema!
@Database(entities = [MeetupLocation::class], version = 5, exportSchema = false)
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
                    // If I change the database structure during testing, just blow it away instead of crashing
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}