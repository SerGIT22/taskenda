package com.sergio.taskenda.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class TaskReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getLongExtra(TaskReminderScheduler.EXTRA_TASK_ID, 0L)
        val title = intent.getStringExtra(TaskReminderScheduler.EXTRA_TASK_TITLE).orEmpty()
        if (taskId > 0L) {
            TaskReminderScheduler.showNotification(context, taskId, title)
        }
    }
}
