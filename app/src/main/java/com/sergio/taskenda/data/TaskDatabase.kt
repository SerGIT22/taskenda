package com.sergio.taskenda.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [Task::class],
    version = 2,
    exportSchema = false
)
abstract class TaskDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao

    companion object {

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                val columns = mutableSetOf<String>()

                database.query("PRAGMA table_info(tasks)").use { cursor ->
                    val nameIndex = cursor.getColumnIndex("name")
                    while (cursor.moveToNext()) {
                        if (nameIndex >= 0) {
                            columns.add(cursor.getString(nameIndex))
                        }
                    }
                }

                if (!columns.contains("dueDate")) {
                    database.execSQL(
                        "ALTER TABLE tasks ADD COLUMN dueDate INTEGER"
                    )
                }

                if (!columns.contains("dueTime")) {
                    database.execSQL(
                        "ALTER TABLE tasks ADD COLUMN dueTime TEXT"
                    )
                }

                if (!columns.contains("listName")) {
                    database.execSQL(
                        "ALTER TABLE tasks ADD COLUMN listName TEXT NOT NULL DEFAULT 'Mis tareas'"
                    )
                }

                if (!columns.contains("createdAt")) {
                    database.execSQL(
                        "ALTER TABLE tasks ADD COLUMN createdAt INTEGER NOT NULL DEFAULT 0"
                    )
                }
            }
        }
    }
}
