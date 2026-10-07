package com.sergio.taskenda.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sergio.taskenda.data.Task
import com.sergio.taskenda.data.TaskListStore
import com.sergio.taskenda.data.TaskRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TaskViewModel(
    private val repository: TaskRepository,
    private val listStore: TaskListStore
) : ViewModel() {

    val tasks = repository.all.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    private val _lists = MutableStateFlow(listStore.loadLists())
    val lists: StateFlow<List<String>> = _lists.asStateFlow()

    var currentList: String = listStore.getSelectedList()
        private set

    fun selectList(name: String) {
        if (name !in _lists.value) return
        currentList = name
        listStore.saveSelectedList(name)
    }

    fun createList(name: String): String? {
        val clean = name.trim()
        if (clean.isBlank()) return null
        if (_lists.value.any { it.equals(clean, ignoreCase = true) }) return null

        val updated = _lists.value + clean
        _lists.value = updated
        listStore.saveLists(updated)
        return clean
    }

    fun renameList(oldName: String, newName: String) {
        val clean = newName.trim()
        if (oldName == TaskListStore.DEFAULT_LIST) return
        if (clean.isBlank() || clean.equals(oldName, ignoreCase = true)) return
        if (_lists.value.any { it.equals(clean, ignoreCase = true) }) return

        val updated = _lists.value.map { if (it == oldName) clean else it }
        _lists.value = updated
        listStore.saveLists(updated)

        viewModelScope.launch(Dispatchers.IO) {
            repository.moveTasksToList(oldName, clean)
        }

        if (currentList == oldName) {
            currentList = clean
            listStore.saveSelectedList(clean)
        }
    }

    fun deleteList(name: String) {
        if (name == TaskListStore.DEFAULT_LIST) return

        val updated = _lists.value.filterNot { it == name }
        _lists.value = if (updated.isEmpty()) listOf(TaskListStore.DEFAULT_LIST) else updated
        listStore.saveLists(_lists.value)

        viewModelScope.launch(Dispatchers.IO) {
            repository.moveTasksToList(name, TaskListStore.DEFAULT_LIST)
        }

        if (currentList == name) {
            currentList = TaskListStore.DEFAULT_LIST
            listStore.saveSelectedList(currentList)
        }
    }

    fun add(
        title: String,
        notes: String = "",
        important: Boolean = false,
        dueDate: Long? = null,
        listName: String = TaskListStore.DEFAULT_LIST
    ) {
        if (title.isBlank()) return

        viewModelScope.launch(Dispatchers.IO) {
            repository.add(
                Task(
                    title = title.trim(),
                    notes = notes.trim(),
                    important = important,
                    dueDate = dueDate,
                    listName = listName
                )
            )
        }
    }

    fun toggle(task: Task) {
        val newCompleted = !task.completed
        viewModelScope.launch(Dispatchers.IO) {
            repository.setCompleted(task.id, newCompleted)
        }
    }

    fun setImportant(task: Task) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.update(
                task.copy(important = !task.important)
            )
        }
    }

    fun update(task: Task) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.update(task)
        }
    }

    fun delete(task: Task) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.delete(task)
        }
    }
}
