package com.example.productivitycontrol

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

// 1. THE TABLE (What a task looks like in the database)
@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val durationMinutes: Int,
    val isCompleted: Boolean = false,
    val date: Long = System.currentTimeMillis()
)

// 2. THE COMMANDS (How we read/write data)
@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY id DESC")
    fun getAllTasks(): Flow<List<TaskEntity>> // "Flow" means it updates automatically!

    @Insert
    suspend fun insertTask(task: TaskEntity)

    @Query("UPDATE tasks SET isCompleted = :completed WHERE id = :taskId")
    suspend fun updateCompletion(taskId: Int, completed: Boolean)

    @Delete
    suspend fun deleteTask(task: TaskEntity)
}

// 3. THE DATABASE ( The actual box holding the tables)
@Database(entities = [TaskEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao

    companion object {
        // Singleton prevents multiple database instances opening at the same time.
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "task_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}