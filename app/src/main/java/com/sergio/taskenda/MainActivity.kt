        shadowElevation = 6.dp,
        color = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavigationBarItem(
                selected = selectedTab == 0,
                onClick = { onTabSelected(0) },
                modifier = Modifier.weight(1f),
                icon = {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Hoy"
                    )
                },
                label = { Text("Hoy", maxLines = 1) },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                )
            )

            NavigationBarItem(
                selected = selectedTab == 1,
                onClick = { onTabSelected(1) },
                modifier = Modifier.weight(1f),
                icon = {
                    Icon(
                        imageVector = Icons.Default.TaskAlt,
                        contentDescription = "Tareas"
                    )
                },
                label = { Text("Tareas", maxLines = 1) },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                )
            )

            NavigationBarItem(
                selected = selectedTab == 2,
                onClick = { onTabSelected(2) },
                modifier = Modifier.weight(1f),
                icon = {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Importantes"
                    )
                },
                label = { Text("Importantes", maxLines = 1) },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                )
            )

            NavigationBarItem(
                selected = selectedTab == 3,
                onClick = { onTabSelected(3) },
                modifier = Modifier.weight(1f),
                icon = {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Completadas"
                    )
                },
                label = { Text("Completadas", maxLines = 1) },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    }
}

private fun isTodayOrOverdue(dueDate: Long?): Boolean {
    if (dueDate == null) return false

    val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.ROOT)
    val taskDate = dateFormat.format(Date(dueDate))
    val today = dateFormat.format(Date())

    return taskDate <= today
}

private fun formatDate(millis: Long): String {
    return SimpleDateFormat(
        "d MMM",
        Locale("es", "ES")
    ).format(Date(millis))
}

private fun vibrate(context: Context) {
    val vibrator = ContextCompat.getSystemService(
        context,
        Vibrator::class.java
    ) ?: return

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        vibrator.vibrate(
            VibrationEffect.createPredefined(
                VibrationEffect.EFFECT_CLICK
            )
        )
    } else {
        @Suppress("DEPRECATION")
        vibrator.vibrate(18)
    }
}
