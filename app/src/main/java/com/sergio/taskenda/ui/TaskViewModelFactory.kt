package com.sergio.taskenda.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.sergio.taskenda.data.TaskListStore
import com.sergio.taskenda.data.TaskRepository

class TaskViewModelFactory(
    private val repository: TaskRepository,
    private val listStore: TaskListStore
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskViewModel::class.java)) {
            return TaskViewModel(repository, listStore) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
