package com.sergio.taskenda.data

import android.content.Context
import org.json.JSONArray

/**
 * Persists Taskenda lists while remaining compatible with older app versions.
 * Older versions stored "lists" as a JSON String; newer versions may have
 * stored it as a StringSet. Read both formats to avoid crashing on startup.
 */
class TaskListStore(context: Context) {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    @Synchronized
    fun getLists(): List<String> {
        val stored: List<String> = try {
            when (val value = preferences.all[KEY_LISTS]) {
                is Set<*> -> value.filterIsInstance<String>()
                is String -> parseLegacyJson(value)
                else -> emptyList()
            }
        } catch (_: Exception) {
            emptyList()
        }

        val cleaned = stored.map { it.trim() }
            .filter { it.isNotBlank() && !it.equals(DEFAULT_LIST, ignoreCase = true) }
            .distinctBy { it.lowercase() }
            .sortedWith(String.CASE_INSENSITIVE_ORDER)

        return listOf(DEFAULT_LIST) + cleaned
    }

    @Synchronized
    fun addList(name: String): Boolean {
        val clean = name.trim()
        if (clean.isBlank() || clean.equals(DEFAULT_LIST, ignoreCase = true)) return false

        val current = getLists()
        if (current.any { it.equals(clean, ignoreCase = true) }) return false

        return saveLists(current.filterNot { it == DEFAULT_LIST } + clean)
    }

    @Synchronized
    fun removeList(name: String): Boolean {
        if (name.equals(DEFAULT_LIST, ignoreCase = true)) return false
        val current = getLists()
        val updated = current.filterNot { it.equals(name, ignoreCase = true) }
        if (updated.size == current.size) return false
        return saveLists(updated.filterNot { it == DEFAULT_LIST })
    }

    private fun parseLegacyJson(raw: String): List<String> {
        return try {
            val json = JSONArray(raw)
            buildList {
                for (index in 0 until json.length()) {
                    val value = json.optString(index, "").trim()
                    if (value.isNotBlank()) add(value)
                }
            }
        } catch (_: Exception) {
            // Some older builds may have stored a single plain-text list name.
            raw.trim().takeIf { it.isNotBlank() }?.let(::listOf).orEmpty()
        }
    }

    /** Keep the legacy JSON-string format so older installed builds can read it too. */
    private fun saveLists(lists: List<String>): Boolean {
        val json = JSONArray()
        lists.filterNot { it.equals(DEFAULT_LIST, ignoreCase = true) }
            .distinctBy { it.lowercase() }
            .forEach(json::put)
        return preferences.edit().putString(KEY_LISTS, json.toString()).commit()
    }

    companion object {
        const val DEFAULT_LIST = "Mis tareas"
        private const val PREFERENCES_NAME = "taskenda_lists"
        private const val KEY_LISTS = "lists"
    }
}
