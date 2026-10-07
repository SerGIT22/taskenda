package com.sergio.taskenda.data

import android.content.Context
import org.json.JSONArray

class TaskListStore(context: Context) {

    private val preferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    fun loadLists(): List<String> {
        val raw = preferences.getString(KEY_LISTS, null) ?: return listOf(DEFAULT_LIST)
        return runCatching {
            val json = JSONArray(raw)
            buildList {
                for (index in 0 until json.length()) {
                    val value = json.optString(index).trim()
                    if (value.isNotBlank() && !contains(value)) add(value)
                }
            }.ifEmpty { listOf(DEFAULT_LIST) }
        }.getOrDefault(listOf(DEFAULT_LIST))
    }

    fun saveLists(lists: List<String>) {
        val clean = lists
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()
            .let { if (it.contains(DEFAULT_LIST)) it else listOf(DEFAULT_LIST) + it }

        val json = JSONArray()
        clean.forEach(json::put)
        preferences.edit().putString(KEY_LISTS, json.toString()).apply()
    }

    fun getSelectedList(): String {
        val saved = preferences.getString(KEY_SELECTED_LIST, DEFAULT_LIST).orEmpty()
        return if (loadLists().contains(saved)) saved else DEFAULT_LIST
    }

    fun saveSelectedList(name: String) {
        preferences.edit().putString(KEY_SELECTED_LIST, name).apply()
    }

    companion object {
        const val DEFAULT_LIST = "Mis tareas"
        private const val PREFS_NAME = "taskenda_lists"
        private const val KEY_LISTS = "lists"
        private const val KEY_SELECTED_LIST = "selected_list"
    }
}
