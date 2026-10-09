package com.sergio.taskenda.data

import android.content.Context

class TaskListStore(context: Context) {

    private val preferences = context.getSharedPreferences(
        "taskenda_lists",
        Context.MODE_PRIVATE
    )

    fun getLists(): List<String> {
        val stored = preferences.getStringSet(KEY_LISTS, null)?.toList().orEmpty()
        return (listOf(DEFAULT_LIST) + stored)
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()
            .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it })
            .let { lists ->
                listOf(DEFAULT_LIST) + lists.filterNot { it == DEFAULT_LIST }
            }
    }

    fun addList(name: String): Boolean {
        val clean = name.trim()
        if (clean.isBlank() || clean.equals(DEFAULT_LIST, ignoreCase = true)) return false

        val lists = getLists().toMutableSet()
        if (!lists.add(clean)) return false

        return preferences.edit()
            .putStringSet(KEY_LISTS, lists.filterNot { it == DEFAULT_LIST }.toSet())
            .commit()
    }

    fun removeList(name: String): Boolean {
        if (name == DEFAULT_LIST) return false

        val lists = getLists().toMutableSet()
        if (!lists.remove(name)) return false

        return preferences.edit()
            .putStringSet(KEY_LISTS, lists.filterNot { it == DEFAULT_LIST }.toSet())
            .commit()
    }

    companion object {
        const val DEFAULT_LIST = "Mis tareas"
        private const val KEY_LISTS = "lists"
    }
}
