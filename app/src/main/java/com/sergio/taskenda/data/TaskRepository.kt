package com.sergio.taskenda.data

class TaskRepository(private val dao: TaskDao) {
    val all = dao.observeAll()
    val pending = dao.observePending()
    val completed = dao.observeCompleted()

    suspend fun add(task: Task) = dao.insert(task)
    suspend fun update(task: Task) = dao.update(task)
    suspend fun delete(task: Task) = dao.delete(task)
    suspend fun get(id: Long) = dao.get(id)
}
