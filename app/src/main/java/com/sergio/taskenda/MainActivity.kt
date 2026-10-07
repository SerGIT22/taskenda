package com.sergio.taskenda

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material.icons.rounded.TaskAlt
import androidx.compose.material.icons.rounded.Today
import androidx.compose.material.icons.rounded.Upcoming
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID


private data class Task(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val notes: String = "",
    val completed: Boolean = false,
    val important: Boolean = false,
    val dueDate: String = ""
)


private object TaskStorage {

    private const val PREFS = "taskenda_preferences"
    private const val TASKS = "tasks"

    fun load(context: Context): List<Task> {
        return try {
            val prefs = context.getSharedPreferences(
                PREFS,
                Context.MODE_PRIVATE
            )

            val raw = prefs.getString(
                TASKS,
                "[]"
            ) ?: "[]"

            val array = JSONArray(raw)

            buildList {
                for (i in 0 until array.length()) {
                    val item = array.getJSONObject(i)

                    add(
                        Task(
                            id = item.optString("id")
                                .ifBlank {
                                    UUID.randomUUID().toString()
                                },
                            title = item.optString("title"),
                            notes = item.optString("notes"),
                            completed = item.optBoolean("completed"),
                            important = item.optBoolean("important"),
                            dueDate = item.optString("dueDate")
                        )
                    )
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun save(
        context: Context,
        tasks: List<Task>
    ) {
        val array = JSONArray()

        tasks.forEach { task ->
            array.put(
                JSONObject().apply {
                    put("id", task.id)
                    put("title", task.title)
                    put("notes", task.notes)
                    put("completed", task.completed)
                    put("important", task.important)
                    put("dueDate", task.dueDate)
                }
            )
        }

        context
            .getSharedPreferences(
                PREFS,
                Context.MODE_PRIVATE
            )
            .edit()
            .putString(
                TASKS,
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

    val context = LocalContext.current
    val darkTheme = isSystemInDarkTheme()

    val colors =
        if (android.os.Build.VERSION.SDK_INT >= 31) {
            if (darkTheme) {
                dynamicDarkColorScheme(context)
            } else {
                dynamicLightColorScheme(context)
            }
        } else {
            MaterialTheme.colorScheme
        }

    MaterialTheme(
        colorScheme = colors
    ) {
        TaskendaScreen()
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TaskendaScreen() {

    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    val tasks = remember {
        mutableStateListOf<Task>().apply {
            addAll(
                TaskStorage.load(context)
            )
        }
    }

    var selectedTab by remember {
        mutableIntStateOf(0)
    }

    var showEditor by remember {
        mutableStateOf(false)
    }

    var editingTask by remember {
        mutableStateOf<Task?>(null)
    }

    var taskToDelete by remember {
        mutableStateOf<Task?>(null)
    }

    fun persist() {
        TaskStorage.save(
            context,
            tasks.toList()
        )
    }

    val visibleTasks =
        when (selectedTab) {
            1 -> tasks.filter {
                !it.completed
            }

            2 -> tasks.filter {
                it.important && !it.completed
            }

            3 -> tasks.filter {
                it.completed
            }

            else -> tasks.filter {
                !it.completed
            }
        }

    Scaffold(
        bottomBar = {
            TaskendaBottomBar(
                selectedTab = selectedTab,
                onTabSelected = { tab ->
                    if (tab != selectedTab) {
                        haptic.performHapticFeedback(
                            HapticFeedbackType.VirtualKey
                        )

                        selectedTab = tab
                    }
                }
            )
        },

        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    haptic.performHapticFeedback(
                        HapticFeedbackType.Confirm
                    )

                    editingTask = null
                    showEditor = true
                },

                modifier = Modifier.size(56.dp),

                shape = RoundedCornerShape(18.dp),

                containerColor =
                    MaterialTheme.colorScheme.primaryContainer,

                contentColor =
                    MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = "Añadir tarea",
                    modifier = Modifier.size(27.dp)
                )
            }
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(
                    start = 18.dp,
                    end = 18.dp,
                    top = 18.dp
                )
        ) {

            TaskendaHeader(
                selectedTab = selectedTab,
                taskCount = visibleTasks.size
            )

            Spacer(
                modifier = Modifier.height(18.dp)
            )

            if (visibleTasks.isEmpty()) {

                EmptyTasks(
                    selectedTab = selectedTab
                )

            } else {

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),

                    contentPadding = PaddingValues(
                        bottom = 76.dp
                    ),

                    verticalArrangement =
                        Arrangement.spacedBy(10.dp)
                ) {

                    items(
                        items = visibleTasks,
                        key = { it.id }
                    ) { task ->

                        TaskRow(
                            task = task,

                            onToggle = {
                                val index =
                                    tasks.indexOfFirst {
                                        it.id == task.id
                                    }

                                if (index >= 0) {

                                    tasks[index] =
                                        tasks[index].copy(
                                            completed =
                                                !tasks[index].completed
                                        )

                                    haptic.performHapticFeedback(
                                        HapticFeedbackType.ToggleOn
                                    )

                                    persist()
                                }
                            },

                            onImportant = {
                                val index =
                                    tasks.indexOfFirst {
                                        it.id == task.id
                                    }

                                if (index >= 0) {

                                    tasks[index] =
                                        tasks[index].copy(
                                            important =
                                                !tasks[index].important
                                        )

                                    haptic.performHapticFeedback(
                                        HapticFeedbackType.VirtualKey
                                    )

                                    persist()
                                }
                            },

                            onEdit = {
                                editingTask = task
                                showEditor = true
                            },

                            onDelete = {
                                taskToDelete = task
                            }
                        )
                    }
                }
            }
        }
    }

    if (showEditor) {

        TaskEditorSheet(
            task = editingTask,

            onDismiss = {
                showEditor = false
                editingTask = null
            },

            onSave = {
                    title,
                    notes,
                    important,
                    dueDate ->

                if (editingTask == null) {

                    tasks.add(
                        Task(
                            title = title,
                            notes = notes,
                            important = important,
                            dueDate = dueDate
                        )
                    )

                } else {

                    val index =
                        tasks.indexOfFirst {
                            it.id == editingTask!!.id
                        }

                    if (index >= 0) {

                        tasks[index] =
                            tasks[index].copy(
                                title = title,
                                notes = notes,
                                important = important,
                                dueDate = dueDate
                            )
                    }
                }

                persist()

                haptic.performHapticFeedback(
                    HapticFeedbackType.Confirm
                )

                showEditor = false
                editingTask = null
            }
        )
    }

    taskToDelete?.let { task ->

        AlertDialog(
            onDismissRequest = {
                taskToDelete = null
            },

            title = {
                Text("Eliminar tarea")
            },

            text = {
                Text(
                    "¿Quieres eliminar «${task.title}»?"
                )
            },

            confirmButton = {
                TextButton(
                    onClick = {

                        tasks.removeAll {
                            it.id == task.id
                        }

                        persist()

                        haptic.performHapticFeedback(
                            HapticFeedbackType.Reject
                        )

                        taskToDelete = null
                    }
                ) {
                    Text("Eliminar")
                }
            },

            dismissButton = {
                TextButton(
                    onClick = {
                        taskToDelete = null
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}


/* -------------------------------------------------------------------------- */
/* BARRA INFERIOR                                                             */
/* -------------------------------------------------------------------------- */

@Composable
private fun TaskendaBottomBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {

    val items = listOf(
        BottomItem(
            "Hoy",
            Icons.Rounded.Home
        ),
        BottomItem(
            "Tareas",
            Icons.Rounded.TaskAlt
        ),
        BottomItem(
            "Importantes",
            Icons.Rounded.Star
        ),
        BottomItem(
            "Completadas",
            Icons.Rounded.Check
        )
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(
                start = 14.dp,
                end = 14.dp,
                bottom = 10.dp
            ),

        shape = RoundedCornerShape(30.dp),

        color =
            MaterialTheme.colorScheme.surfaceContainerHigh,

        tonalElevation = 3.dp,

        shadowElevation = 4.dp
    ) {

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .padding(5.dp)
        ) {

            val slotWidth =
                maxWidth / items.size

            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                items.forEachIndexed { index, item ->

                    val selected =
                        selectedTab == index

                    Box(
                        modifier = Modifier
                            .width(slotWidth)
                            .fillMaxHeight()
                            .clickable {
                                onTabSelected(index)
                            },

                        contentAlignment =
                            Alignment.Center
                    ) {

                        if (selected) {

                            Surface(
                                modifier =
                                    Modifier.fillMaxSize(
                                        fraction = 0.94f
                                    ),

                                shape =
                                    RoundedCornerShape(
                                        25.dp
                                    ),

                                color =
                                    MaterialTheme
                                        .colorScheme
                                        .primaryContainer
                            ) {

                                Row(
                                    modifier =
                                        Modifier
                                            .fillMaxSize()
                                            .padding(
                                                horizontal = 8.dp
                                            ),

                                    horizontalArrangement =
                                        Arrangement.Center,

                                    verticalAlignment =
                                        Alignment.CenterVertically
                                ) {

                                    Icon(
                                        imageVector =
                                            if (
                                                index == 2
                                            ) {
                                                Icons.Rounded.Star
                                            } else {
                                                item.icon
                                            },

                                        contentDescription =
                                            item.label,

                                        modifier =
                                            Modifier.size(
                                                23.dp
                                            ),

                                        tint =
                                            MaterialTheme
                                                .colorScheme
                                                .onPrimaryContainer
                                    )

                                    Spacer(
                                        modifier =
                                            Modifier.width(6.dp)
                                    )

                                    Text(
                                        text = item.label,

                                        style =
                                            MaterialTheme
                                                .typography
                                                .labelMedium,

                                        fontWeight =
                                            FontWeight.SemiBold,

                                        maxLines = 1,

                                        color =
                                            MaterialTheme
                                                .colorScheme
                                                .onPrimaryContainer
                                    )
                                }
                            }

                        } else {

                            Icon(
                                imageVector =
                                    if (index == 2) {
                                        Icons.Rounded.StarBorder
                                    } else {
                                        item.icon
                                    },

                                contentDescription =
                                    item.label,

                                modifier =
                                    Modifier.size(24.dp),

                                tint =
                                    MaterialTheme
                                        .colorScheme
                                        .onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}


private data class BottomItem(
    val label: String,
    val icon: ImageVector
)


/* -------------------------------------------------------------------------- */
/* CABECERA                                                                   */
/* -------------------------------------------------------------------------- */

@Composable
private fun TaskendaHeader(
    selectedTab: Int,
    taskCount: Int
) {

    val title =
        when (selectedTab) {
            0 -> "Hoy"
            1 -> "Tareas"
            2 -> "Importantes"
            3 -> "Completadas"
            else -> "Tareas"
        }

    val subtitle =
        when (selectedTab) {
            0 -> "Tus tareas pendientes"
            1 -> "Todas las tareas pendientes"
            2 -> "Tareas marcadas como importantes"
            3 -> "Tareas que ya has terminado"
            else -> ""
        }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = title,

                    style =
                        MaterialTheme
                            .typography
                            .headlineMedium,

                    fontWeight =
                        FontWeight.Bold
                )

                Spacer(
                    modifier = Modifier.height(3.dp)
                )

                Text(
                    text = subtitle,

                    style =
                        MaterialTheme
                            .typography
                            .bodyMedium,

                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant
                )
            }

            Surface(
                shape = CircleShape,

                color =
                    MaterialTheme
                        .colorScheme
                        .secondaryContainer
            ) {

                Text(
                    text = taskCount.toString(),

                    modifier =
                        Modifier.padding(
                            horizontal = 13.dp,
                            vertical = 8.dp
                        ),

                    color =
                        MaterialTheme
                            .colorScheme
                            .onSecondaryContainer,

                    fontWeight =
                        FontWeight.Bold
                )
            }
        }
    }
}


/* -------------------------------------------------------------------------- */
/* ESTADO VACÍO                                                               */
/* -------------------------------------------------------------------------- */

@Composable
private fun EmptyTasks(
    selectedTab: Int
) {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                bottom = 90.dp
            ),

        contentAlignment =
            Alignment.Center
    ) {

        Column(
            horizontalAlignment =
                Alignment.CenterHorizontally,

            verticalArrangement =
                Arrangement.spacedBy(10.dp)
        ) {

            Surface(
                modifier = Modifier.size(76.dp),

                shape = CircleShape,

                color =
                    MaterialTheme
                        .colorScheme
                        .secondaryContainer
            ) {

                Box(
                    contentAlignment =
                        Alignment.Center
                ) {

                    Icon(
                        imageVector =
                            when (selectedTab) {
                                2 -> Icons.Rounded.StarBorder
                                3 -> Icons.Rounded.Check
                                else -> Icons.Rounded.TaskAlt
                            },

                        contentDescription = null,

                        modifier =
                            Modifier.size(38.dp),

                        tint =
                            MaterialTheme
                                .colorScheme
                                .onSecondaryContainer
                    )
                }
            }

            Text(
                text =
                    when (selectedTab) {
                        2 -> "No tienes tareas importantes"
                        3 -> "Todavía no has completado tareas"
                        else -> "No tienes tareas pendientes"
                    },

                style =
                    MaterialTheme
                        .typography
                        .titleMedium,

                fontWeight =
                    FontWeight.SemiBold
            )

            Text(
                text =
                    when (selectedTab) {
                        2 ->
                            "Marca una tarea como importante para verla aquí."

                        3 ->
                            "Cuando completes una tarea aparecerá aquí."

                        else ->
                            "Pulsa + para crear tu primera tarea."
                    },

                style =
                    MaterialTheme
                        .typography
                        .bodyMedium,

                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )
        }
    }
}


/* -------------------------------------------------------------------------- */
/* FILA DE TAREA                                                              */
/* -------------------------------------------------------------------------- */

@Composable
private fun TaskRow(
    task: Task,
    onToggle: () -> Unit,
    onImportant: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {

    var menuExpanded by remember(task.id) {
        mutableStateOf(false)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),

        shape =
            RoundedCornerShape(22.dp),

        colors =
            CardDefaults.cardColors(
                containerColor =
                    MaterialTheme
                        .colorScheme
                        .surfaceContainer
            ),

        border =
            if (task.important) {
                BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.primary
                )
            } else {
                null
            }
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 12.dp,
                    end = 8.dp,
                    top = 12.dp,
                    bottom = 12.dp
                ),

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            TaskCheckbox(
                checked = task.completed,
                onClick = onToggle
            )

            Spacer(
                modifier = Modifier.width(12.dp)
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = task.title,

                    style =
                        MaterialTheme
                            .typography
                            .titleMedium,

                    fontWeight =
                        FontWeight.SemiBold,

                    textDecoration =
                        if (task.completed) {
                            TextDecoration.LineThrough
                        } else {
                            TextDecoration.None
                        },

                    color =
                        if (task.completed) {
                            MaterialTheme
                                .colorScheme
                                .onSurfaceVariant
                        } else {
                            MaterialTheme
                                .colorScheme
                                .onSurface
                        }
                )

                if (task.notes.isNotBlank()) {

                    Spacer(
                        modifier =
                            Modifier.height(3.dp)
                    )

                    Text(
                        text = task.notes,

                        style =
                            MaterialTheme
                                .typography
                                .bodyMedium,

                        maxLines = 2,

                        color =
                            MaterialTheme
                                .colorScheme
                                .onSurfaceVariant,

                        textDecoration =
                            if (task.completed) {
                                TextDecoration.LineThrough
                            } else {
                                TextDecoration.None
                            }
                    )
                }

                if (task.dueDate.isNotBlank()) {

                    Spacer(
                        modifier =
                            Modifier.height(6.dp)
                    )

                    Row(
                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {

                        Icon(
                            imageVector =
                                Icons.Rounded.Today,

                            contentDescription =
                                null,

                            modifier =
                                Modifier.size(15.dp),

                            tint =
                                MaterialTheme
                                    .colorScheme
                                    .primary
                        )

                        Spacer(
                            modifier =
                                Modifier.width(4.dp)
                        )

                        Text(
                            text = task.dueDate,

                            style =
                                MaterialTheme
                                    .typography
                                    .labelMedium,

                            color =
                                MaterialTheme
                                    .colorScheme
                                    .primary
                        )
                    }
                }
            }

            IconButton(
                onClick = onImportant
            ) {

                Icon(
                    imageVector =
                        if (task.important) {
                            Icons.Rounded.Star
                        } else {
                            Icons.Rounded.StarBorder
                        },

                    contentDescription =
                        if (task.important) {
                            "Quitar de importantes"
                        } else {
                            "Marcar como importante"
                        },

                    tint =
                        if (task.important) {
                            MaterialTheme
                                .colorScheme
                                .primary
                        } else {
                            MaterialTheme
                                .colorScheme
                                .onSurfaceVariant
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
                            Icons.Rounded.MoreVert,

                        contentDescription =
                            "Más opciones"
                    )
                }

                DropdownMenu(
                    expanded = menuExpanded,

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
                                Icons.Rounded.Edit,
                                contentDescription = null
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
                                Icons.Rounded.Delete,
                                contentDescription = null
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


/* -------------------------------------------------------------------------- */
/* CHECKBOX                                                                   */
/* -------------------------------------------------------------------------- */

@Composable
private fun TaskCheckbox(
    checked: Boolean,
    onClick: () -> Unit
) {

    Surface(
        modifier = Modifier
            .size(34.dp)
            .clickable {
                onClick()
            },

        shape = CircleShape,

        color =
            if (checked) {
                MaterialTheme
                    .colorScheme
                    .primary
            } else {
                Color.Transparent
            },

        border =
            if (checked) {
                null
            } else {
                BorderStroke(
                    2.dp,
                    MaterialTheme
                        .colorScheme
                        .outline
                )
            }
    ) {

        Box(
            contentAlignment =
                Alignment.Center
        ) {

            if (checked) {

                Icon(
                    imageVector =
                        Icons.Rounded.Check,

                    contentDescription =
                        "Completada",

                    modifier =
                        Modifier.size(21.dp),

                    tint =
                        MaterialTheme
                            .colorScheme
                            .onPrimary
                )
            }
        }
    }
}


/* -------------------------------------------------------------------------- */
/* EDITOR                                                                     */
/* -------------------------------------------------------------------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TaskEditorSheet(
    task: Task?,
    onDismiss: () -> Unit,
    onSave: (
        String,
        String,
        Boolean,
        String
    ) -> Unit
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
            task?.dueDate ?: ""
        )
    }

    var showDatePicker by remember {
        mutableStateOf(false)
    }

    val sheetState =
        rememberModalBottomSheetState(
            skipPartiallyExpanded = true
        )

    ModalBottomSheet(

        onDismissRequest = onDismiss,

        sheetState = sheetState,

        modifier =
            Modifier.imePadding()
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 20.dp,
                    end = 20.dp,
                    bottom = 20.dp
                ),

            verticalArrangement =
                Arrangement.spacedBy(14.dp)
        ) {

            Row(
                modifier =
                    Modifier.fillMaxWidth(),

                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Column(
                    modifier =
                        Modifier.weight(1f)
                ) {

                    Text(
                        text =
                            if (task == null) {
                                "Nueva tarea"
                            } else {
                                "Editar tarea"
                            },

                        style =
                            MaterialTheme
                                .typography
                                .headlineSmall,

                        fontWeight =
                            FontWeight.Bold
                    )

                    Text(
                        text =
                            "Organiza lo que tienes pendiente",

                        style =
                            MaterialTheme
                                .typography
                                .bodyMedium,

                        color =
                            MaterialTheme
                                .colorScheme
                                .onSurfaceVariant
                    )
                }
            }

            OutlinedTextField(
                value = title,

                onValueChange = {
                    title = it
                },

                modifier =
                    Modifier.fillMaxWidth(),

                singleLine = true,

                label = {
                    Text("Tarea")
                },

                placeholder = {
                    Text("¿Qué tienes que hacer?")
                },

                shape =
                    RoundedCornerShape(18.dp)
            )

            OutlinedTextField(
                value = notes,

                onValueChange = {
                    notes = it
                },

                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(120.dp),

                label = {
                    Text("Notas")
                },

                placeholder = {
                    Text("Añade detalles opcionales")
                },

                shape =
                    RoundedCornerShape(18.dp)
            )

            Surface(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clickable {
                            showDatePicker = true
                        },

                shape =
                    RoundedCornerShape(18.dp),

                color =
                    MaterialTheme
                        .colorScheme
                        .surfaceContainerHigh,

                border =
                    BorderStroke(
                        1.dp,
                        MaterialTheme
                            .colorScheme
                            .outlineVariant
                    )
            ) {

                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(16.dp),

                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    Icon(
                        imageVector =
                            Icons.Rounded.Today,

                        contentDescription =
                            null,

                        tint =
                            MaterialTheme
                                .colorScheme
                                .primary
                    )

                    Spacer(
                        modifier =
                            Modifier.width(12.dp)
                    )

                    Column(
                        modifier =
                            Modifier.weight(1f)
                    ) {

                        Text(
                            text = "Fecha",

                            style =
                                MaterialTheme
                                    .typography
                                    .labelMedium,

                            color =
                                MaterialTheme
                                    .colorScheme
                                    .onSurfaceVariant
                        )

                        Text(
                            text =
                                if (dueDate.isBlank()) {
                                    "Sin fecha"
                                } else {
                                    dueDate
                                },

                            style =
                                MaterialTheme
                                    .typography
                                    .bodyLarge,

                            fontWeight =
                                FontWeight.Medium
                        )
                    }

                    if (dueDate.isNotBlank()) {

                        TextButton(
                            onClick = {
                                dueDate = ""
                            }
                        ) {
                            Text("Quitar")
                        }
                    }
                }
            }

            Surface(
                modifier =
                    Modifier.fillMaxWidth(),

                shape =
                    RoundedCornerShape(18.dp),

                color =
                    MaterialTheme
                        .colorScheme
                        .surfaceContainerHigh
            ) {

                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = 16.dp,
                                vertical = 10.dp
                            ),

                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    Icon(
                        imageVector =
                            if (important) {
                                Icons.Rounded.Star
                            } else {
                                Icons.Rounded.StarBorder
                            },

                        contentDescription =
                            null,

                        tint =
                            if (important) {
                                MaterialTheme
                                    .colorScheme
                                    .primary
                            } else {
                                MaterialTheme
                                    .colorScheme
                                    .onSurfaceVariant
                            }
                    )

                    Spacer(
                        modifier =
                            Modifier.width(12.dp)
                    )

                    Column(
                        modifier =
                            Modifier.weight(1f)
                    ) {

                        Text(
                            text = "Importante",

                            fontWeight =
                                FontWeight.SemiBold
                        )

                        Text(
                            text =
                                "Marca esta tarea como prioritaria",

                            style =
                                MaterialTheme
                                    .typography
                                    .bodySmall,

                            color =
                                MaterialTheme
                                    .colorScheme
                                    .onSurfaceVariant
                        )
                    }

                    Switch(
                        checked = important,

                        onCheckedChange = {
                            important = it
                        }
                    )
                }
            }

            Button(
                onClick = {

                    val cleanTitle =
                        title.trim()

                    if (cleanTitle.isNotEmpty()) {

                        onSave(
                            cleanTitle,
                            notes.trim(),
                            important,
                            dueDate
                        )
                    }
                },

                enabled =
                    title.trim().isNotEmpty(),

                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(54.dp),

                shape =
                    RoundedCornerShape(18.dp)
            ) {

                Icon(
                    imageVector =
                        if (task == null) {
                            Icons.Rounded.Add
                        } else {
                            Icons.Rounded.Check
                        },

                    contentDescription =
                        null
                )

                Spacer(
                    modifier =
                        Modifier.width(8.dp)
                )

                Text(
                    if (task == null) {
                        "Crear tarea"
                    } else {
                        "Guardar cambios"
                    }
                )
            }
        }
    }

    if (showDatePicker) {

        TaskDatePickerDialog(
            currentDate = dueDate,

            onDismiss = {
                showDatePicker = false
            },

            onDateSelected = { date ->

                dueDate = date
                showDatePicker = false
            }
        )
    }
}


/* -------------------------------------------------------------------------- */
/* SELECTOR DE FECHA                                                          */
/* -------------------------------------------------------------------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TaskDatePickerDialog(
    currentDate: String,
    onDismiss: () -> Unit,
    onDateSelected: (String) -> Unit
) {

    val state =
        rememberDatePickerState(
            initialSelectedDateMillis =
                parseDateMillis(currentDate)
        )

    DatePickerDialog(

        onDismissRequest = onDismiss,

        confirmButton = {

            TextButton(
                onClick = {

                    val millis =
                        state.selectedDateMillis

                    if (millis != null) {

                        onDateSelected(
                            formatDateMillis(millis)
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
                onClick = onDismiss
            ) {
                Text("Cancelar")
            }
        }
    ) {

        DatePicker(
            state = state
        )
    }
}


/* -------------------------------------------------------------------------- */
/* FECHAS                                                                     */
/* -------------------------------------------------------------------------- */

private fun parseDateMillis(
    value: String
): Long? {

    if (value.isBlank()) {
        return null
    }

    return try {

        val formatter =
            SimpleDateFormat(
                "dd/MM/yyyy",
                Locale.getDefault()
            )

        formatter.isLenient = false

        val date =
            formatter.parse(value)
                ?: return null

        val calendar =
            Calendar.getInstance()

        calendar.time = date

        calendar.set(
            Calendar.HOUR_OF_DAY,
            12
        )

        calendar.set(
            Calendar.MINUTE,
            0
        )

        calendar.set(
            Calendar.SECOND,
            0
        )

        calendar.set(
            Calendar.MILLISECOND,
            0
        )

        calendar.timeInMillis

    } catch (_: Exception) {

        null
    }
}


private fun formatDateMillis(
    millis: Long
): String {

    val formatter =
        SimpleDateFormat(
            "dd/MM/yyyy",
            Locale.getDefault()
        )

    return formatter.format(
        Date(millis)
    )
}
