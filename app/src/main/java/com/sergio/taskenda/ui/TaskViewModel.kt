package com.sergio.taskenda.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sergio.taskenda.data.Task
import com.sergio.taskenda.data.TaskRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TaskViewModel(private val repository: TaskRepository) : ViewModel() {
    val tasks = repository.all.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun add(title: String, notes: String = "", important: Boolean = false) {
        if (title.isBlank()) return
        viewModelScope.launch {
            repository.add(Task(title = title.trim(), notes = notes, important = important))
        }
    }

    fun toggle(task: Task) {
        viewModelScope.launch {
            repository.update(task.copy(completed = !task.completed))
        }
    }

    fun setImportant(task: Task) {
        viewModelScope.launch {
            repository.update(task.copy(important = !task.important))
        }
    }

    fun update(task: Task) {
        viewModelScope.launch { repository.update(task) }
    }

    fun delete(task: Task) {
        viewModelScope.launch { repository.delete(task) }
    }
}
