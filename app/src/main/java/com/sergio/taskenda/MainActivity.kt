package com.sergio.taskenda

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.graphicsLayer
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Task(
    val id: Long,
    val title: String,
    val notes: String = "",
    val completed: Boolean = false,
    val important: Boolean = false,
    val dueDate: Long? = null
)

private object TaskStorage {

    private const val PREFS_NAME = "taskenda_storage"
    private const val TASKS_KEY = "tasks_json"

    private fun prefs(context: Context) =
        context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )

    fun loadTasks(context: Context): List<Task> {
        val raw = prefs(context)
            .getString(TASKS_KEY, null)

        if (raw.isNullOrBlank()) {
            return emptyList()
        }

        return try {
            val array = JSONArray(raw)
            val result = mutableListOf<Task>()

            for (i in 0 until array.length()) {
                val item = array.optJSONObject(i)
                    ?: continue

                val id = item.optLong("id")
                val title = item.optString("title")

                if (title.isBlank()) {
                    continue
                }

                val notes = item.optString(
                    "notes",
                    ""
                )

                val completed = item.optBoolean(
                    "completed",
                    false
                )

                val important = item.optBoolean(
                    "important",
                    false
                )

                val dueDate =
                    if (
                        item.has("dueDate") &&
                        !item.isNull("dueDate")
                    ) {
                        item.optLong("dueDate")
                            .takeIf { it > 0L }
                    } else {
                        null
                    }

                result.add(
                    Task(
                        id = id,
                        title = title,
                        notes = notes,
                        completed = completed,
                        important = important,
                        dueDate = dueDate
                    )
                )
            }

            result
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun saveTasks(
        context: Context,
        tasks: List<Task>
    ) {
        val array = JSONArray()

        tasks.forEach { task ->

            val objectJson = JSONObject().apply {
                put(
                    "id",
                    task.id
                )

                put(
                    "title",
                    task.title
                )

                put(
                    "notes",
                    task.notes
                )

                put(
                    "completed",
                    task.completed
                )

                put(
                    "important",
                    task.important
                )

                if (task.dueDate != null) {
                    put(
                        "dueDate",
                        task.dueDate
                    )
                } else {
                    put(
                        "dueDate",
                        JSONObject.NULL
                    )
                }
            }

            array.put(objectJson)
        }

        prefs(context)
            .edit()
            .putString(
                TASKS_KEY,
                array.toString()
            )
            .apply()
    }
}

class MainActivity : ComponentActivity() {

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        setContent {
            TaskendaApp()
        }
    }
}

@Composable
private fun TaskendaApp() {

    val isDark =
        isSystemInDarkTheme()

    MaterialTheme(
        colorScheme =
            if (isDark) {
                darkColorScheme()
            } else {
                lightColorScheme()
            }
    ) {
        TaskendaScreen()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TaskendaScreen() {

    val context =
        LocalContext.current

    val haptic =
        LocalHapticFeedback.current

    var tasks by remember {
        mutableStateOf(
            TaskStorage.loadTasks(context)
        )
    }

    var selectedTab by remember {
        mutableStateOf(0)
    }

    var editorVisible by remember {
        mutableStateOf(false)
    }

    var editingTask by remember {
        mutableStateOf<Task?>(null)
    }

    fun saveTasks(
        newTasks: List<Task>
    ) {
        tasks = newTasks

        TaskStorage.saveTasks(
            context,
            newTasks
        )
    }

    val visibleTasks =
        when (selectedTab) {

            0 -> tasks.filter {
                !it.completed
            }

            1 -> tasks.filter {
                !it.completed
            }

            2 -> tasks.filter {
                !it.completed &&
                        it.important
            }

            3 -> tasks.filter {
                it.completed
            }

            else -> tasks
        }

    Scaffold(
        containerColor =
            MaterialTheme.colorScheme.background,

        floatingActionButton = {

            FloatingActionButton(
                onClick = {

                    haptic.performHapticFeedback(
                        HapticFeedbackType.LongPress
                    )

                    editingTask = null
                    editorVisible = true
                },

                containerColor =
                    MaterialTheme.colorScheme.primaryContainer,

                contentColor =
                    MaterialTheme.colorScheme.onPrimaryContainer
            ) {

                Icon(
                    imageVector =
                        Icons.Default.Add,

                    contentDescription =
                        "Añadir tarea"
                )
            }
        },

        bottomBar = {

            TaskendaBottomBar(
                selectedTab = selectedTab,
                onTabSelected = {
                    selectedTab = it
                }
            )
        }

    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            /*
             * CABECERA ANIMADA
             *
             * La animamos por separado para que el cambio
             * de categoría no reconstruya bruscamente toda
             * la pantalla.
             */
            AnimatedContent(
                targetState = selectedTab,
                modifier = Modifier.fillMaxWidth(),

                transitionSpec = {

                    (
                        fadeIn(
                            animationSpec =
                                tween(
                                    durationMillis = 180
                                )
                        ) +
                            scaleIn(
                                initialScale = 0.97f,
                                animationSpec =
                                    tween(
                                        durationMillis = 220,
                                        easing =
                                            FastOutSlowInEasing
                                    )
                            )
                        ) togetherWith
                        (
                            fadeOut(
                                animationSpec =
                                    tween(
                                        durationMillis = 120
                                    )
                            ) +
                                scaleOut(
                                    targetScale = 0.985f,
                                    animationSpec =
                                        tween(
                                            durationMillis = 120
                                        )
                                )
                            )
                        .using(
                            SizeTransform(
                                clip = false
                            )
                        )
                },

                label = "header_transition"
            ) { tab ->

                TaskendaHeader(
                    title = when (tab) {
                        0 -> "Hoy"
                        1 -> "Tareas"
                        2 -> "Importantes"
                        3 -> "Completadas"
                        else -> "Tareas"
                    },

                    subtitle = when (tab) {
                        0 ->
                            "Tus tareas pendientes"

                        1 ->
                            "Todas las tareas pendientes"

                        2 ->
                            "Tareas marcadas como importantes"

                        3 ->
                            "Tareas que ya has terminado"

                        else ->
                            ""
                    },

                    taskCount =
                        when (tab) {

                            0 ->
                                tasks.count {
                                    !it.completed
                                }

                            1 ->
                                tasks.count {
                                    !it.completed
                                }

                            2 ->
                                tasks.count {
                                    !it.completed &&
                                            it.important
                                }

                            3 ->
                                tasks.count {
                                    it.completed
                                }

                            else ->
                                0
                        }
                )
            }

            /*
             * CONTENIDO
             *
             * Fade + ligera escala.
             *
             * No usamos slide horizontal porque es precisamente
             * lo que puede hacer que textos largos entren/salgan
             * cortados durante la transición.
             */
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {

                AnimatedContent(
                    targetState = selectedTab,
                    modifier = Modifier.fillMaxSize(),

                    transitionSpec = {

                        (
                            fadeIn(
                                animationSpec =
                                    tween(
                                        durationMillis = 190,
                                        delayMillis = 35
                                    )
                            ) +
                                scaleIn(
                                    initialScale = 0.985f,
                                    animationSpec =
                                        tween(
                                            durationMillis = 240,
                                            easing =
                                                FastOutSlowInEasing
                                        )
                                )
                            ) togetherWith
                            (
                                fadeOut(
                                    animationSpec =
                                        tween(
                                            durationMillis = 130
                                        )
                                ) +
                                    scaleOut(
                                        targetScale = 0.99f,
                                        animationSpec =
                                            tween(
                                                durationMillis = 130
                                            )
                                    )
                                )
                            )
                            .using(
                                SizeTransform(
                                    clip = false
                                )
                            )
                    },

                    label = "category_transition"
                ) { tab ->

                    val tabTasks =
                        when (tab) {

                            0 -> tasks.filter {
                                !it.completed
                            }

                            1 -> tasks.filter {
                                !it.completed
                            }

                            2 -> tasks.filter {
                                !it.completed &&
                                        it.important
                            }

                            3 -> tasks.filter {
                                it.completed
                            }

                            else -> emptyList()
                        }

                    if (tabTasks.isEmpty()) {

                        EmptyTasks(
                            selectedTab = tab,
                            onCreate = {

                                haptic.performHapticFeedback(
                                    HapticFeedbackType.LongPress
                                )

                                editingTask = null
                                editorVisible = true
                            }
                        )

                    } else {

                        LazyColumn(
                            modifier =
                                Modifier.fillMaxSize(),

                            contentPadding =
                                PaddingValues(
                                    start = 16.dp,
                                    end = 16.dp,
                                    top = 8.dp,
                                    bottom = 110.dp
                                ),

                            verticalArrangement =
                                Arrangement.spacedBy(
                                    10.dp
                                )
                        ) {

                            items(
                                items = tabTasks,
                                key = {
                                    it.id
                                }
                            ) { task ->

                                TaskRow(
                                    task = task,

                                    onToggleComplete = {

                                        haptic.performHapticFeedback(
                                            HapticFeedbackType.TextHandleMove
                                        )

                                        saveTasks(
                                            tasks.map {

                                                if (
                                                    it.id ==
                                                    task.id
                                                ) {
                                                    it.copy(
                                                        completed =
                                                            !it.completed
                                                    )
                                                } else {
                                                    it
                                                }
                                            }
                                        )
                                    },

                                    onToggleImportant = {

                                        haptic.performHapticFeedback(
                                            HapticFeedbackType.TextHandleMove
                                        )

                                        saveTasks(
                                            tasks.map {

                                                if (
                                                    it.id ==
                                                    task.id
                                                ) {
                                                    it.copy(
                                                        important =
                                                            !it.important
                                                    )
                                                } else {
                                                    it
                                                }
                                            }
                                        )
                                    },

                                    onEdit = {

                                        editingTask =
                                            task

                                        editorVisible =
                                            true
                                    },

                                    onDelete = {

                                        saveTasks(
                                            tasks.filter {
                                                it.id != task.id
                                            }
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (editorVisible) {

        TaskEditorSheet(
            task = editingTask,

            onDismiss = {

                editorVisible = false
                editingTask = null
            },

            onSave = { updatedTask ->

                val exists =
                    tasks.any {
                        it.id == updatedTask.id
                    }

                val newTasks =
                    if (exists) {

                        tasks.map {

                            if (
                                it.id ==
                                updatedTask.id
                            ) {
                                updatedTask
                            } else {
                                it
                            }
                        }

                    } else {

                        tasks +
                                updatedTask
                    }

                saveTasks(newTasks)

                editorVisible = false
                editingTask = null
            }
        )
    }
}

@Composable
private fun TaskendaHeader(
    title: String,
    subtitle: String,
    taskCount: Int
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = 20.dp,
                end = 20.dp,
                top = 20.dp,
                bottom = 10.dp
            ),

        verticalAlignment =
            Alignment.CenterVertically
    ) {

        Column(
            modifier =
                Modifier.weight(1f)
        ) {

            Text(
                text = title,

                style =
                    MaterialTheme.typography.headlineMedium,

                fontWeight =
                    FontWeight.Bold,

                maxLines = 1,

                overflow =
                    TextOverflow.Ellipsis
            )

            Spacer(
                modifier =
                    Modifier.height(4.dp)
            )

            Text(
                text = subtitle,

                style =
                    MaterialTheme.typography.bodyLarge,

                color =
                    MaterialTheme.colorScheme.onSurfaceVariant,

                maxLines = 2,

                overflow =
                    TextOverflow.Ellipsis
            )
        }

        Spacer(
            modifier =
                Modifier.width(12.dp)
        )

        Surface(
            modifier =
                Modifier.size(48.dp),

            shape =
                CircleShape,

            color =
                MaterialTheme.colorScheme.secondaryContainer
        ) {

            Box(
                contentAlignment =
                    Alignment.Center
            ) {

                Text(
                    text =
                        taskCount.toString(),

                    style =
                        MaterialTheme.typography.titleMedium,

                    fontWeight =
                        FontWeight.SemiBold,

                    color =
                        MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

@Composable
private fun EmptyTasks(
    selectedTab: Int,
    onCreate: () -> Unit
) {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                start = 32.dp,
                end = 32.dp,
                bottom = 60.dp
            ),

        contentAlignment =
            Alignment.Center
    ) {

        Column(
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {

            Surface(
                modifier =
                    Modifier.size(72.dp),

                shape =
                    CircleShape,

                color =
                    MaterialTheme.colorScheme.secondaryContainer
            ) {

                Box(
                    contentAlignment =
                        Alignment.Center
                ) {

                    Icon(
                        imageVector =
                            when (selectedTab) {
                                2 ->
                                    Icons.Default.Star

                                3 ->
                                    Icons.Default.CheckCircle

                                else ->
                                    Icons.Default.TaskAlt
                            },

                        contentDescription =
                            null,

                        modifier =
                            Modifier.size(36.dp),

                        tint =
                            MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            Spacer(
                modifier =
                    Modifier.height(18.dp)
            )

            Text(
                text =
                    when (selectedTab) {

                        2 ->
                            "No tienes tareas importantes"

                        3 ->
                            "No tienes tareas completadas"

                        else ->
                            "No tienes tareas pendientes"
                    },

                style =
                    MaterialTheme.typography.titleLarge,

                fontWeight =
                    FontWeight.SemiBold,

                maxLines = 2,

                overflow =
                    TextOverflow.Ellipsis
            )

            Spacer(
                modifier =
                    Modifier.height(6.dp)
            )

            Text(
                text =
                    when (selectedTab) {

                        2 ->
                            "Marca una tarea como importante para verla aquí."

                        3 ->
                            "Las tareas que completes aparecerán aquí."

                        else ->
                            "Pulsa + para crear tu primera tarea."
                    },

                style =
                    MaterialTheme.typography.bodyLarge,

                color =
                    MaterialTheme.colorScheme.onSurfaceVariant,

                maxLines = 3,

                overflow =
                    TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun TaskRow(
    task: Task,
    onToggleComplete: () -> Unit,
    onToggleImportant: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {

    var menuExpanded by remember {
        mutableStateOf(false)
    }

    Card(
        modifier =
            Modifier.fillMaxWidth(),

        shape =
            RoundedCornerShape(22.dp),

        colors =
            CardDefaults.cardColors(
                containerColor =
                    MaterialTheme.colorScheme.surfaceContainer
            )
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 10.dp,
                    end = 8.dp,
                    top = 10.dp,
                    bottom = 10.dp
                ),

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            TaskCheckbox(
                checked =
                    task.completed,

                onCheckedChange =
                    onToggleComplete
            )

            Spacer(
                modifier =
                    Modifier.width(8.dp)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        onEdit()
                    }
                    .padding(
                        vertical = 4.dp
                    )
            ) {

                Text(
                    text =
                        task.title,

                    style =
                        MaterialTheme.typography.bodyLarge,

                    fontWeight =
                        FontWeight.Medium,

                    color =
                        if (task.completed) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },

                    maxLines = 2,

                    overflow =
                        TextOverflow.Ellipsis
                )

                if (task.notes.isNotBlank()) {

                    Spacer(
                        modifier =
                            Modifier.height(3.dp)
                    )

                    Text(
                        text =
                            task.notes,

                        style =
                            MaterialTheme.typography.bodySmall,

                        color =
                            MaterialTheme.colorScheme.onSurfaceVariant,

                        maxLines = 2,

                        overflow =
                            TextOverflow.Ellipsis
                    )
                }

                if (task.dueDate != null) {

                    Spacer(
                        modifier =
                            Modifier.height(5.dp)
                    )

                    Row(
                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {

                        Icon(
                            imageVector =
                                Icons.Outlined.Event,

                            contentDescription =
                                null,

                            modifier =
                                Modifier.size(15.dp),

                            tint =
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(
                            modifier =
                                Modifier.width(4.dp)
                        )

                        Text(
                            text =
                                formatDate(
                                    task.dueDate
                                ),

                            style =
                                MaterialTheme.typography.labelSmall,

                            color =
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            IconButton(
                onClick =
                    onToggleImportant
            ) {

                Icon(
                    imageVector =
                        if (task.important) {
                            Icons.Default.Star
                        } else {
                            Icons.Default.StarBorder
                        },

                    contentDescription =
                        if (task.important) {
                            "Quitar de importantes"
                        } else {
                            "Marcar como importante"
                        },

                    tint =
                        if (task.important) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                )
            }

            Box {

                IconButton(
                    onClick = {
                        menuExpanded = true
                    }
                ) {

                    Icon(
                        imageVector =
                            Icons.Default.MoreVert,

                        contentDescription =
                            "Más opciones"
                    )
                }

                DropdownMenu(
                    expanded =
                        menuExpanded,

                    onDismissRequest = {
                        menuExpanded = false
                    }
                ) {

                    DropdownMenuItem(
                        text = {
                            Text("Editar")
                        },

                        leadingIcon = {
                            Icon(
                                imageVector =
                                    Icons.Default.Edit,

                                contentDescription =
                                    null
                            )
                        },

                        onClick = {
                            menuExpanded = false
                            onEdit()
                        }
                    )

                    DropdownMenuItem(
                        text = {
                            Text("Eliminar")
                        },

                        leadingIcon = {
                            Icon(
                                imageVector =
                                    Icons.Default.Delete,

                                contentDescription =
                                    null
                            )
                        },

                        onClick = {
                            menuExpanded = false
                            onDelete()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun TaskCheckbox(
    checked: Boolean,
    onCheckedChange: () -> Unit
) {

    val haptic =
        LocalHapticFeedback.current

    Surface(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .clickable {

                haptic.performHapticFeedback(
                    HapticFeedbackType.TextHandleMove
                )

                onCheckedChange()
            },

        shape =
            CircleShape,

        color =
            if (checked) {
                MaterialTheme.colorScheme.primary
            } else {
                Color.Transparent
            },

        border =
            if (!checked) {
                BorderStroke(
                    width = 2.dp,
                    color =
                        MaterialTheme.colorScheme.outline
                )
            } else {
                null
            }
    ) {

        Box(
            contentAlignment =
                Alignment.Center
        ) {

            if (checked) {

                Icon(
                    imageVector =
                        Icons.Default.Check,

                    contentDescription =
                        "Completada",

                    tint =
                        MaterialTheme.colorScheme.onPrimary,

                    modifier =
                        Modifier.size(25.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TaskEditorSheet(
    task: Task?,
    onDismiss: () -> Unit,
    onSave: (Task) -> Unit
) {

    var title by remember(task) {
        mutableStateOf(
            task?.title ?: ""
        )
    }

    var notes by remember(task) {
        mutableStateOf(
            task?.notes ?: ""
        )
    }

    var important by remember(task) {
        mutableStateOf(
            task?.important ?: false
        )
    }

    var dueDate by remember(task) {
        mutableStateOf(
            task?.dueDate
        )
    }

    var datePickerVisible by remember {
        mutableStateOf(false)
    }

    ModalBottomSheet(
        onDismissRequest =
            onDismiss,

        containerColor =
            MaterialTheme.colorScheme.surface
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(
                    start = 20.dp,
                    end = 20.dp,
                    bottom = 24.dp
                )
        ) {

            Text(
                text =
                    if (task == null) {
                        "Nueva tarea"
                    } else {
                        "Editar tarea"
                    },

                style =
                    MaterialTheme.typography.headlineSmall,

                fontWeight =
                    FontWeight.Bold
            )

            Spacer(
                modifier =
                    Modifier.height(18.dp)
            )

            OutlinedTextField(
                value =
                    title,

                onValueChange = {
                    title = it
                },

                modifier =
                    Modifier.fillMaxWidth(),

                label = {
                    Text("Tarea")
                },

                placeholder = {
                    Text("¿Qué quieres hacer?")
                },

                singleLine = true
            )

            Spacer(
                modifier =
                    Modifier.height(12.dp)
            )

            OutlinedTextField(
                value =
                    notes,

                onValueChange = {
                    notes = it
                },

                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),

                label = {
                    Text("Notas")
                },

                placeholder = {
                    Text(
                        "Añade información opcional"
                    )
                }
            )

            Spacer(
                modifier =
                    Modifier.height(12.dp)
            )

            OutlinedButton(
                onClick = {
                    datePickerVisible = true
                },

                modifier =
                    Modifier.fillMaxWidth()
            ) {

                Icon(
                    imageVector =
                        Icons.Outlined.Event,

                    contentDescription =
                        null
                )

                Spacer(
                    modifier =
                        Modifier.width(8.dp)
                )

                Text(
                    text =
                        dueDate?.let {
                            formatDate(it)
                        } ?: "Añadir fecha",

                    modifier =
                        Modifier.weight(1f)
                )

                if (dueDate != null) {

                    TextButton(
                        onClick = {
                            dueDate = null
                        }
                    ) {
                        Text("Quitar")
                    }
                }
            }

            Spacer(
                modifier =
                    Modifier.height(8.dp)
            )

            Row(
                modifier =
                    Modifier.fillMaxWidth(),

                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Icon(
                    imageVector =
                        if (important) {
                            Icons.Default.Star
                        } else {
                            Icons.Default.StarBorder
                        },

                    contentDescription =
                        null,

                    tint =
                        if (important) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                )

                Spacer(
                    modifier =
                        Modifier.width(10.dp)
                )

                Text(
                    text =
                        "Importante",

                    modifier =
                        Modifier.weight(1f),

                    style =
                        MaterialTheme.typography.bodyLarge
                )

                Switch(
                    checked =
                        important,

                    onCheckedChange = {
                        important = it
                    }
                )
            }

            Spacer(
                modifier =
                    Modifier.height(14.dp)
            )

            Button(
                onClick = {

                    val cleanTitle =
                        title.trim()

                    if (
                        cleanTitle.isBlank()
                    ) {
                        return@Button
                    }

                    onSave(
                        Task(
                            id =
                                task?.id
                                    ?: System.currentTimeMillis(),

                            title =
                                cleanTitle,

                            notes =
                                notes.trim(),

                            completed =
                                task?.completed
                                    ?: false,

                            important =
                                important,

                            dueDate =
                                dueDate
                        )
                    )
                },

                modifier =
                    Modifier.fillMaxWidth(),

                enabled =
                    title.trim().isNotBlank()
            ) {

                Text(
                    text =
                        if (task == null) {
                            "Crear tarea"
                        } else {
                            "Guardar cambios"
                        }
                )
            }
        }
    }

    if (datePickerVisible) {

        TaskDatePickerDialog(
            initialDate =
                dueDate,

            onDismiss = {
                datePickerVisible = false
            },

            onDateSelected = {
                dueDate = it
                datePickerVisible = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TaskDatePickerDialog(
    initialDate: Long?,
    onDismiss: () -> Unit,
    onDateSelected: (Long) -> Unit
) {

    val datePickerState =
        rememberDatePickerState(
            initialSelectedDateMillis =
                initialDate
        )

    DatePickerDialog(
        onDismissRequest =
            onDismiss,

        confirmButton = {

            TextButton(
                onClick = {

                    val selected =
                        datePickerState.selectedDateMillis

                    if (selected != null) {
                        onDateSelected(
                            selected
                        )
                    } else {
                        onDismiss()
                    }
                }
            ) {
                Text("Aceptar")
            }
        },

        dismissButton = {

            TextButton(
                onClick =
                    onDismiss
            ) {
                Text("Cancelar")
            }
        }
    ) {

        DatePicker(
            state =
                datePickerState
        )
    }
}

/*
 * ============================================================
 * BARRA INFERIOR
 * ============================================================
 *
 * Esta es la parte más importante del cambio.
 *
 * Antes:
 *
 *  25% | 25% | 25% | 25%
 *
 * Cada elemento tenía siempre el mismo ancho.
 *
 * Ahora:
 *
 *  seleccionado = se expande
 *  resto        = se comprime
 *
 * Igual que en Diario.
 *
 * Además, la etiqueta aparece con un pequeño retraso,
 * cuando el botón ya tiene espacio suficiente.
 */
@Composable
private fun TaskendaBottomBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(
                start = 12.dp,
                end = 12.dp,
                bottom = 8.dp
            ),

        shape =
            RoundedCornerShape(28.dp),

        tonalElevation =
            4.dp,

        shadowElevation =
            6.dp,

        color =
            MaterialTheme.colorScheme.surfaceContainerHigh
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(
                    horizontal = 6.dp
                ),

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            TaskendaBottomItem(
                selected =
                    selectedTab == 0,

                icon = {

                    Icon(
                        imageVector =
                            if (selectedTab == 0) {
                                Icons.Default.CheckCircle
                            } else {
                                Icons.Outlined.CheckCircle
                            },

                        contentDescription =
                            "Hoy"
                    )
                },

                label =
                    "Hoy",

                onClick = {
                    onTabSelected(0)
                }
            )

            TaskendaBottomItem(
                selected =
                    selectedTab == 1,

                icon = {

                    Icon(
                        imageVector =
                            if (selectedTab == 1) {
                                Icons.Default.TaskAlt
                            } else {
                                Icons.Outlined.CheckCircle
                            },

                        contentDescription =
                            "Tareas"
                    )
                },

                label =
                    "Tareas",

                onClick = {
                    onTabSelected(1)
                }
            )

            TaskendaBottomItem(
                selected =
                    selectedTab == 2,

                icon = {

                    Icon(
                        imageVector =
                            if (selectedTab == 2) {
                                Icons.Default.Star
                            } else {
                                Icons.Outlined.StarBorder
                            },

                        contentDescription =
                            "Importantes"
                    )
                },

                label =
                    "Importantes",

                onClick = {
                    onTabSelected(2)
                }
            )

            TaskendaBottomItem(
                selected =
                    selectedTab == 3,

                icon = {

                    Icon(
                        imageVector =
                            if (selectedTab == 3) {
                                Icons.Default.Check
                            } else {
                                Icons.Default.CheckCircle
                            },

                        contentDescription =
                            "Completadas"
                    )
                },

                label =
                    "Completadas",

                onClick = {
                    onTabSelected(3)
                }
            )
        }
    }
}

@Composable
private fun TaskendaBottomItem(
    selected: Boolean,
    icon: @Composable () -> Unit,
    label: String,
    onClick: () -> Unit
) {

    /*
     * El seleccionado gana bastante espacio.
     *
     * 1.70 seleccionado
     * 0.77 cada no seleccionado
     *
     * Esto deja suficiente espacio para:
     * "Importantes"
     * "Completadas"
     */
    val targetWeight =
        if (selected) {
            1.70f
        } else {
            0.77f
        }

    val animatedWeight by
        animateFloatAsState(
            targetValue =
                targetWeight,

            animationSpec =
                tween(
                    durationMillis = 300,
                    easing =
                        FastOutSlowInEasing
                ),

            label =
                "bottom_item_weight"
        )

    Surface(
        modifier = Modifier
            .weight(
                animatedWeight
            )
            .fillMaxHeight()
            .padding(
                horizontal = 2.dp,
                vertical = 5.dp
            )
            .clip(
                RoundedCornerShape(24.dp)
            )
            .clickable {
                onClick()
            },

        color =
            Color.Transparent
    ) {

        Box(
            modifier =
                Modifier.fillMaxSize(),

            contentAlignment =
                Alignment.Center
        ) {

            Surface(
                modifier = Modifier
                    .fillMaxHeight()
                    .then(
                        if (selected) {
                            Modifier
                                .padding(
                                    horizontal = 1.dp
                                )
                        } else {
                            Modifier
                        }
                    ),

                shape =
                    RoundedCornerShape(24.dp),

                color =
                    if (selected) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        Color.Transparent
                    }
            ) {

                Row(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(
                            horizontal =
                                if (selected) {
                                    14.dp
                                } else {
                                    8.dp
                                }
                        )
                        .animateContentSize(
                            animationSpec =
                                tween(
                                    durationMillis = 220,
                                    easing =
                                        FastOutSlowInEasing
                                )
                        ),

                    horizontalArrangement =
                        Arrangement.Center,

                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    icon()

                    /*
                     * La etiqueta aparece después de que
                     * el elemento haya empezado a expandirse.
                     *
                     * Así evitamos el problema anterior:
                     *
                     * IMPORTA
                     *
                     * mientras todavía no había ancho
                     * suficiente para IMPORTANTE.
                     */
                    AnimatedVisibility(
                        visible =
                            selected,

                        enter =
                            fadeIn(
                                animationSpec =
                                    tween(
                                        durationMillis = 160,
                                        delayMillis = 90
                                    )
                            ),

                        exit =
                            fadeOut(
                                animationSpec =
                                    tween(
                                        durationMillis = 90
                                    )
                            )
                    ) {

                        Row {

                            Spacer(
                                modifier =
                                    Modifier.width(7.dp)
                            )

                            Text(
                                text =
                                    label,

                                style =
                                    MaterialTheme.typography.labelLarge,

                                fontWeight =
                                    FontWeight.SemiBold,

                                maxLines = 1,

                                softWrap = false,

                                overflow =
                                    TextOverflow.Visible
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatDate(
    millis: Long
): String {

    return SimpleDateFormat(
        "d MMM",
        Locale("es", "ES")
    ).format(
        Date(millis)
    )
}
