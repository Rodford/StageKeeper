package com.example.stagekeeper

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.example.stagekeeper.data.User
import com.example.stagekeeper.data.UserDao

// Blueprint for the pins I'm dropping on the map
@Entity(tableName = "meetup_locations")
data class MeetupLocation(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val latitude: Double,
    val longitude: Double,
    val note: String = ""
)

// Defining my map pin database queries here
@Dao
interface LocationDao {
    // Keeps the map updated in real-time using Flow
    @Query("SELECT * FROM meetup_locations")
    fun getAllLocations(): Flow<List<MeetupLocation>>

    @Insert
    fun insertLocation(location: MeetupLocation)

    @Query("DELETE FROM meetup_locations")
    fun deleteAll()
}

// Room Database setup including both locations and users
@Database(entities = [MeetupLocation::class, User::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun locationDao(): LocationDao
    abstract fun userDao(): UserDao

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