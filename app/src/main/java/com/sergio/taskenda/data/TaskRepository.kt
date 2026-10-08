package com.sergio.taskenda.data

import kotlinx.coroutines.flow.Flow

class TaskRepository(private val dao: TaskDao) {

    val all: Flow<List<Task>> = dao.observeAll()
    val pending: Flow<List<Task>> = dao.observePending()
    val completed: Flow<List<Task>> = dao.observeCompleted()
    val allSubtasks: Flow<List<Subtask>> = dao.observeAllSubtasks()

    suspend fun add(task: Task): Long = dao.insert(task)
    suspend fun update(task: Task) = dao.update(task)
    suspend fun delete(task: Task) = dao.delete(task)
    suspend fun get(id: Long): Task? = dao.get(id)

    suspend fun renameList(oldName: String, newName: String) =
        dao.renameList(oldName, newName)

    suspend fun moveTasksToList(fromList: String, toList: String) =
        dao.moveTasksToList(fromList, toList)

    suspend fun saveTaskWithSubtasks(task: Task, subtasks: List<Subtask>): Long =
        dao.saveTaskWithSubtasks(task, subtasks)

    suspend fun getSubtasks(taskId: Long): List<Subtask> =
        dao.getSubtasks(taskId)

    suspend fun addSubtask(subtask: Subtask): Long =
        dao.insertSubtask(subtask)

    suspend fun updateSubtask(subtask: Subtask) =
        dao.updateSubtask(subtask)

    suspend fun deleteSubtask(subtask: Subtask) =
        dao.deleteSubtask(subtask)
}
