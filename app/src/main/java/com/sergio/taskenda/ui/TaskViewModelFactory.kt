package com.sergio.taskenda.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.sergio.taskenda.data.TaskListStore
import com.sergio.taskenda.data.TaskRepository

class TaskViewModelFactory(
    private val repository: TaskRepository,
    private val listStore: TaskListStore,
    private val context: Context
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskViewModel::class.java)) {
            return TaskViewModel(repository, listStore, context) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
