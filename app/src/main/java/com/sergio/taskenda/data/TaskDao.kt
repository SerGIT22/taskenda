package com.sergio.taskenda.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Query("SELECT * FROM tasks ORDER BY completed ASC, dueDate ASC, createdAt DESC")
    fun observeAll(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE completed = 0 ORDER BY dueDate ASC, createdAt DESC")
    fun observePending(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE completed = 1 ORDER BY createdAt DESC")
    fun observeCompleted(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE id = :id LIMIT 1")
    suspend fun get(id: Long): Task?

    @Insert
    suspend fun insert(task: Task): Long

    @Update
    suspend fun update(task: Task)

    @Delete
    suspend fun delete(task: Task)

    @Query("UPDATE tasks SET listName = :newName WHERE listName = :oldName")
    suspend fun renameList(oldName: String, newName: String)

    @Query("UPDATE tasks SET listName = :toList WHERE listName = :fromList")
    suspend fun moveTasksToList(fromList: String, toList: String)

    @Query("SELECT * FROM subtasks ORDER BY taskId ASC, position ASC, createdAt ASC")
    fun observeAllSubtasks(): Flow<List<Subtask>>

    @Query("SELECT * FROM subtasks WHERE taskId = :taskId ORDER BY position ASC, createdAt ASC")
    suspend fun getSubtasks(taskId: Long): List<Subtask>

    @Insert
    suspend fun insertSubtasks(subtasks: List<Subtask>)

    @Insert
    suspend fun insertSubtask(subtask: Subtask): Long

    @Update
    suspend fun updateSubtask(subtask: Subtask)

    @Delete
    suspend fun deleteSubtask(subtask: Subtask)

    @Query("DELETE FROM subtasks WHERE taskId = :taskId")
    suspend fun deleteSubtasks(taskId: Long)

    @Transaction
    suspend fun saveTaskWithSubtasks(task: Task, subtasks: List<Subtask>): Long {
        val taskId = if (task.id == 0L) {
            insert(task)
        } else {
            update(task)
            task.id
        }

        deleteSubtasks(taskId)

        val validSubtasks = subtasks
            .map { it.copy(title = it.title.trim()) }
            .filter { it.title.isNotBlank() }

        if (validSubtasks.isNotEmpty()) {
            insertSubtasks(
                validSubtasks.mapIndexed { index, subtask ->
                    subtask.copy(
                        id = 0L,
                        taskId = taskId,
                        position = index
                    )
                }
            )
        }

        return taskId
    }
}
