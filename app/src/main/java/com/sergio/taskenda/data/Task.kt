package com.sergio.taskenda.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val notes: String = "",
    val dueDate: Long? = null,
    val dueTime: String? = null,
    val completed: Boolean = false,
    val important: Boolean = false,
    val listName: String = "Mis tareas",
    val createdAt: Long = System.currentTimeMillis()
)
