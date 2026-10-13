package com.sergio.taskenda.reminders

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.sergio.taskenda.MainActivity
import com.sergio.taskenda.R
import com.sergio.taskenda.data.Task

object TaskReminderScheduler {
    const val CHANNEL_ID = "taskenda_task_reminders"
    const val EXTRA_TASK_ID = "taskenda_task_id"
    const val EXTRA_TASK_TITLE = "taskenda_task_title"

    fun schedule(context: Context, task: Task) {
        val appContext = context.applicationContext
        createChannel(appContext)
        cancel(appContext, task.id)

        if (task.completed) return
        val triggerAt = task.reminderAt ?: return
        if (task.id <= 0L || triggerAt <= System.currentTimeMillis()) return

        val intent = Intent(appContext, TaskReminderReceiver::class.java).apply {
            putExtra(EXTRA_TASK_ID, task.id)
            putExtra(EXTRA_TASK_TITLE, task.title)
        }
        val pending = PendingIntent.getBroadcast(
            appContext,
            task.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarmManager = appContext.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
            ?: return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pending)
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAt, pending)
        }
    }

    fun cancel(context: Context, taskId: Long) {
        if (taskId <= 0L) return
        val appContext = context.applicationContext
        val intent = Intent(appContext, TaskReminderReceiver::class.java)
        val pending = PendingIntent.getBroadcast(
            appContext,
            taskId.toInt(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pending != null) {
            (appContext.getSystemService(Context.ALARM_SERVICE) as? AlarmManager)
                ?.cancel(pending)
            pending.cancel()
        }
    }

    fun showNotification(context: Context, taskId: Long, title: String) {
        createChannel(context)
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_TASK_ID, taskId)
        }
        val contentIntent = PendingIntent.getActivity(
            context,
            taskId.toInt(),
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_taskenda_foreground)
            .setContentTitle("Tarea pendiente")
            .setContentText(title.ifBlank { "Tienes una tarea pendiente" })
            .setStyle(NotificationCompat.BigTextStyle().bigText(title))
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(taskId.toInt(), notification)
        } catch (_: SecurityException) {
            // Android 13+: notification permission may not have been granted.
        }
    }

    private fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Recordatorios de tareas",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Avisos de las tareas programadas en Taskenda"
            }
            manager.createNotificationChannel(channel)
        }
    }
}
