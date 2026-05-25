package com.example.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

// --- 1. USER PROFILE ---
@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val name: String = "Aspirant",
    val studentClass: String = "Class 11",
    val goal: String = "IIT-JEE",
    val weakSubjects: String = "Physics Mechanics, Organic Chemistry",
    val availableHours: Int = 6,
    val examDate: String = "2027-05-15",
    val xp: Int = 150,
    val level: Int = 1,
    val coins: Int = 100,
    val streak: Int = 5,
    val productivityScore: Int = 78,
    val focusWarriorRank: String = "Apprentice Warrior",
    val isOnboarded: Boolean = false
)

// --- 2. PLANNER TASK ---
@Entity(tableName = "planner_task")
data class PlannerTask(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val category: String, // e.g. "Math", "Physics", "Chemistry", "Break", "Revision"
    val durationMin: Int,
    val isCompleted: Boolean = false,
    val scheduledTime: String = "10:00 AM",
    val dayName: String = "Today" // e.g., "Monday", "Today", "Weekly Target"
)

// --- 3. DOUBT SOLVER ---
@Entity(tableName = "doubt_record")
data class DoubtRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val question: String,
    val explanation: String,
    val style: String, // Quick, Detailed, Class 7, IIT-Level
    val solvedAt: Long = System.currentTimeMillis()
)

// --- 4. NOTE GENERATOR ---
@Entity(tableName = "note_record")
data class NoteRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val summaryText: String,
    val mindmapNodesJson: String, // Flat string representation or clean tags
    val flashcardsJson: String, // JSON string or comma-separated pairs
    val importantQuestions: String,
    val createdAt: Long = System.currentTimeMillis()
)

// --- 5. MOCK TEST ---
@Entity(tableName = "mock_test_record")
data class MockTestRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val subject: String,
    val score: Int,
    val totalQuestions: Int,
    val accuracy: Int, // percentage
    val speedSeconds: Int,
    val airPrediction: Int,
    val dateTaken: Long = System.currentTimeMillis()
)

// --- DAOs ---
@Dao
interface UserDao {
    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun getProfile(): Flow<UserProfile?>

    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    suspend fun getProfileSync(): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProfile(profile: UserProfile)
}

@Dao
interface PlannerDao {
    @Query("SELECT * FROM planner_task ORDER BY id ASC")
    fun getAllTasks(): Flow<List<PlannerTask>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: PlannerTask)

    @Update
    suspend fun updateTask(task: PlannerTask)

    @Delete
    suspend fun deleteTask(task: PlannerTask)

    @Query("DELETE FROM planner_task")
    suspend fun clearAllTasks()
}

@Dao
interface DoubtDao {
    @Query("SELECT * FROM doubt_record ORDER BY solvedAt DESC")
    fun getAllDoubts(): Flow<List<DoubtRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDoubt(doubt: DoubtRecord)
}

@Dao
interface NoteDao {
    @Query("SELECT * FROM note_record ORDER BY createdAt DESC")
    fun getAllNotes(): Flow<List<NoteRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteRecord)

    @Query("DELETE FROM note_record WHERE id = :id")
    suspend fun deleteNoteById(id: Int)
}

@Dao
interface MockTestDao {
    @Query("SELECT * FROM mock_test_record ORDER BY dateTaken DESC")
    fun getAllTests(): Flow<List<MockTestRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTest(test: MockTestRecord)
}

// --- APP DATABASE BUILD ---
@Database(
    entities = [UserProfile::class, PlannerTask::class, DoubtRecord::class, NoteRecord::class, MockTestRecord::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract val userDao: UserDao
    abstract val plannerDao: PlannerDao
    abstract val doubtDao: DoubtDao
    abstract val noteDao: NoteDao
    abstract val mockTestDao: MockTestDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "prepnexus_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
