package com.example.stagekeeper

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "meetup_locations")
data class MeetupLocation(
    @PrimaryKey val pinId: String = "",
    val partyId: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val note: String = ""
)

@Dao
interface LocationDao {
    @Query("SELECT * FROM meetup_locations")
    fun getAllLocations(): Flow<List<MeetupLocation>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertLocation(location: MeetupLocation)

    @Query("DELETE FROM meetup_locations")
    fun deleteAll()
}

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
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}