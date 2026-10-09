package com.sergio.taskenda.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sergio.taskenda.data.Subtask
import com.sergio.taskenda.data.Task
import com.sergio.taskenda.data.TaskListStore
import com.sergio.taskenda.data.TaskRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
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

    val subtasks = repository.allSubtasks.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    private val _lists = MutableStateFlow(listStore.getLists())
    val lists = _lists

    var currentList: String = _lists.value.firstOrNull() ?: TaskListStore.DEFAULT_LIST
        private set

    fun selectList(name: String) {
        if (_lists.value.any { it.equals(name, ignoreCase = true) }) {
            currentList = _lists.value.first { it.equals(name, ignoreCase = true) }
        }
    }

    fun createList(name: String): String? {
        val clean = name.trim()
        if (!listStore.addList(clean)) return null

        val updated = listStore.getLists()
        _lists.value = updated
        currentList = updated.first { it.equals(clean, ignoreCase = true) }
        return currentList
    }

    fun renameList(oldName: String, newName: String) {
        val oldClean = oldName.trim()
        val newClean = newName.trim()

        if (oldClean.isBlank() || newClean.isBlank()) return
        if (oldClean.equals(TaskListStore.DEFAULT_LIST, ignoreCase = true)) return
        if (oldClean.equals(newClean, ignoreCase = true)) return
        if (_lists.value.any { it.equals(newClean, ignoreCase = true) }) return

        viewModelScope.launch(Dispatchers.IO) {
            repository.renameList(oldClean, newClean)

            // TaskListStore intentionally has no rename operation: recreate the entry.
            listStore.addList(newClean)
            listStore.removeList(oldClean)

            val updated = listStore.getLists()
            _lists.value = updated

            if (currentList.equals(oldClean, ignoreCase = true)) {
                currentList = newClean
            }
        }
    }

    fun deleteList(name: String) {
        val clean = name.trim()
        if (clean.isBlank() || clean.equals(TaskListStore.DEFAULT_LIST, ignoreCase = true)) return

        viewModelScope.launch(Dispatchers.IO) {
            repository.moveTasksToList(clean, TaskListStore.DEFAULT_LIST)
            listStore.removeList(clean)

            val updated = listStore.getLists()
            _lists.value = updated

            if (currentList.equals(clean, ignoreCase = true)) {
                currentList = TaskListStore.DEFAULT_LIST
            }
        }
    }

    fun add(
        title: String,
        notes: String = "",
        important: Boolean = false,
        dueDate: Long? = null,
        listName: String = TaskListStore.DEFAULT_LIST
    ) {
        val cleanTitle = title.trim()
        if (cleanTitle.isBlank()) return

        viewModelScope.launch(Dispatchers.IO) {
            repository.add(
                Task(
                    title = cleanTitle,
                    notes = notes.trim(),
                    important = important,
                    dueDate = dueDate,
                    listName = listName.trim().ifEmpty { TaskListStore.DEFAULT_LIST }
                )
            )
        }
    }

    fun save(task: Task, subtasks: List<Subtask>) {
        if (task.title.isBlank()) return

        viewModelScope.launch(Dispatchers.IO) {
            repository.saveTaskWithSubtasks(
                task.copy(
                    title = task.title.trim(),
                    notes = task.notes.trim(),
                    listName = task.listName.trim().ifEmpty { TaskListStore.DEFAULT_LIST }
                ),
                subtasks.filter { it.title.isNotBlank() }
            )
        }
    }

    fun toggle(task: Task) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.update(task.copy(completed = !task.completed))
        }
    }

    fun setImportant(task: Task) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.update(task.copy(important = !task.important))
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

    fun toggleSubtask(subtask: Subtask) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateSubtask(subtask.copy(completed = !subtask.completed))
        }
    }

    fun deleteSubtask(subtask: Subtask) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteSubtask(subtask)
        }
    }

    fun addSubtask(taskId: Long, title: String, position: Int) {
        val clean = title.trim()
        if (taskId == 0L || clean.isEmpty()) return

        viewModelScope.launch(Dispatchers.IO) {
            repository.addSubtask(
                Subtask(
                    taskId = taskId,
                    title = clean,
                    position = position
                )
            )
        }
    }
}
