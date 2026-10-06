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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.unit.sp
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID


private data class Task(
    val id: String = UUID.randomUUID().toString(),
    var title: String,
    var notes: String = "",
    var completed: Boolean = false,
    var important: Boolean = false,
    var dueDate: String = ""
)


private object TaskStorage {

    private const val PREFS = "taskenda_preferences"
    private const val TASKS = "tasks"

    fun load(context: Context): List<Task> {
        return try {
            val prefs =
                context.getSharedPreferences(
                    PREFS,
                    Context.MODE_PRIVATE
                )

            val raw =
                prefs.getString(
                    TASKS,
                    "[]"
                ) ?: "[]"

            val array =
                JSONArray(raw)

            buildList {

                for (i in 0 until array.length()) {

                    val item =
                        array.getJSONObject(i)

                    add(
                        Task(
                            id = item.optString("id"),
                            title = item.optString("title"),
                            notes = item.optString("notes"),
                            completed =
                                item.optBoolean(
                                    "completed"
                                ),
                            important =
                                item.optBoolean(
                                    "important"
                                ),
                            dueDate =
                                item.optString(
                                    "dueDate"
                                )
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

                    put(
                        "dueDate",
                        task.dueDate
                    )
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

    val context =
        LocalContext.current

    val darkTheme =
        isSystemInDarkTheme()

    val colors =
        if (
            android.os.Build.VERSION.SDK_INT >= 31
        ) {

            if (darkTheme) {
                dynamicDarkColorScheme(
                    context
                )
            } else {
                dynamicLightColorScheme(
                    context
                )
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

    val context =
        LocalContext.current

    val haptic =
        LocalHapticFeedback.current

    val tasks =
        remember {

            mutableStateListOf<Task>().apply {

                addAll(
                    TaskStorage.load(
                        context
                    )
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

            1 ->
                tasks.filter {
                    !it.completed
                }

            2 ->
                tasks.filter {
                    it.important &&
                        !it.completed
                }

            3 ->
                tasks.filter {
                    it.completed
                }

            else ->
                tasks.filter {
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

                modifier =
                    Modifier.size(56.dp),

                shape =
                    RoundedCornerShape(18.dp),

                containerColor =
                    MaterialTheme
                        .colorScheme
                        .primaryContainer,

                contentColor =
                    MaterialTheme
                        .colorScheme
                        .onPrimaryContainer

            ) {

                Icon(
                    imageVector =
                        Icons.Rounded.Add,
                    contentDescription =
                        "Añadir tarea",
                    modifier =
                        Modifier.size(27.dp)
                )
            }
        }

    ) { padding ->

        Column(

            modifier =
                Modifier
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
                taskCount =
                    visibleTasks.size
            )

            Spacer(
                modifier =
                    Modifier.height(18.dp)
            )


            if (visibleTasks.isEmpty()) {

                EmptyTasks(
                    selectedTab = selectedTab
                )

            } else {

                LazyColumn(

                    modifier =
                        Modifier.fillMaxSize(),

                    contentPadding =
                        PaddingValues(
                            bottom = 76.dp
                        ),

                    verticalArrangement =
                        Arrangement.spacedBy(
                            10.dp
                        )

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
                                                !tasks[index]
                                                    .completed
                                        )

                                    haptic
                                        .performHapticFeedback(
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
                                                !tasks[index]
                                                    .important
                                        )

                                    haptic
                                        .performHapticFeedback(
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
                            it.id ==
                                editingTask!!.id
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


    if (taskToDelete != null) {

        AlertDialog(

            onDismissRequest = {
                taskToDelete = null
            },

            title = {
                Text("Eliminar tarea")
            },

            text = {
                Text(
                    "¿Quieres eliminar «${taskToDelete!!.title}»?"
                )
            },

            confirmButton = {

                TextButton(

                    onClick = {

                        tasks.removeAll {
                            it.id ==
                                taskToDelete!!.id
                        }

                        persist()

                        haptic
                            .performHapticFeedback(
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


/*
 * BARRA INFERIOR
 *
 * IMPORTANTE:
 * Los cuatro elementos tienen exactamente
 * el mismo espacio mediante weight().
 *
 * La selección NO modifica el espacio
 * de los demás elementos.
 */
@Composable
private fun TaskendaBottomBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {

    Surface(

        modifier =
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    bottom = 7.dp
                ),

        shape =
            RoundedCornerShape(26.dp),

        color =
            MaterialTheme
                .colorScheme
                .surfaceContainerHigh,

        tonalElevation = 4.dp,

        shadowElevation = 5.dp

    ) {

        Row(

            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(62.dp)
                    .padding(
                        horizontal = 4.dp
                    ),

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            TaskNavItem(
                selected =
                    selectedTab == 0,
                icon =
                    Icons.Rounded.Home,
                label = "Inicio",
                onClick = {
                    onTabSelected(0)
                }
            )

            TaskNavItem(
                selected =
                    selectedTab == 1,
                icon =
                    Icons.Rounded.Upcoming,
                label = "Pendientes",
                onClick = {
                    onTabSelected(1)
                }
            )

            TaskNavItem(
                selected =
                    selectedTab == 2,
                icon =
                    if (
                        selectedTab == 2
                    ) {
                        Icons.Rounded.Star
                    } else {
                        Icons.Rounded.StarBorder
                    },
                label = "Importantes",
                onClick = {
                    onTabSelected(2)
                }
            )

            TaskNavItem(
                selected =
                    selectedTab == 3,
                icon =
                    Icons.Rounded.Check,
                label = "Hechas",
                onClick = {
                    onTabSelected(3)
                }
            )
        }
    }
}


/*
 * Cada opción ocupa exactamente 1/4
 * de la barra.
 *
 * El contenedor seleccionado tiene
 * un tamaño FIJO para que seleccionar
 * otra sección nunca desplace los iconos.
 */
@Composable
private fun TaskNavItem(
    selected: Boolean,
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {

    Box(

        modifier =
            Modifier
                .weight(1f)
                .fillMaxHeight(),

        contentAlignment =
            Alignment.Center
    ) {

        Surface(

            modifier =
                Modifier
                    .width(78.dp)
                    .height(46.dp)
                    .clickable(
                        onClick = onClick
                    ),

            shape =
                RoundedCornerShape(18.dp),

            color =
                if (selected) {
                    MaterialTheme
                        .colorScheme
                        .primaryContainer
                } else {
                    Color.Transparent
                }

        ) {

            Row(

                modifier =
                    Modifier.fillMaxSize(),

                horizontalArrangement =
                    Arrangement.Center,

                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Icon(

                    imageVector = icon,

                    contentDescription =
                        label,

                    modifier =
                        Modifier.size(21.dp),

                    tint =
                        if (selected) {
                            MaterialTheme
                                .colorScheme
                                .onPrimaryContainer
                        } else {
                            MaterialTheme
                                .colorScheme
                                .onSurfaceVariant
                        }
                )


                if (selected) {

                    Spacer(
                        modifier =
                            Modifier.width(4.dp)
                    )

                    Text(

                        text = label,

                        style =
                            MaterialTheme
                                .typography
                                .labelSmall,

                        fontSize = 10.sp,

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
        }
    }
}


@Composable
private fun TaskendaHeader(
    selectedTab: Int,
    taskCount: Int
) {

    val title =
        when (selectedTab) {

            1 ->
                "Pendientes"

            2 ->
                "Importantes"

            3 ->
                "Completadas"

            else ->
                "Mis tareas"
        }


    val subtitle =
        when (taskCount) {

            0 ->
                "Todo al día"

            1 ->
                "1 tarea"

            else ->
                "$taskCount tareas"
        }


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

                text = title,

                style =
                    MaterialTheme
                        .typography
                        .headlineLarge,

                fontWeight =
                    FontWeight.Bold
            )

            Spacer(
                modifier =
                    Modifier.height(2.dp)
            )

            Text(

                text = subtitle,

                style =
                    MaterialTheme
                        .typography
                        .bodyLarge,

                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )
        }


        Surface(

            modifier =
                Modifier.size(48.dp),

            shape = CircleShape,

            color =
                MaterialTheme
                    .colorScheme
                    .primaryContainer
        ) {

            Box(
                contentAlignment =
                    Alignment.Center
            ) {

                Icon(

                    imageVector =
                        Icons.Rounded.TaskAlt,

                    contentDescription =
                        null,

                    tint =
                        MaterialTheme
                            .colorScheme
                            .onPrimaryContainer
                )
            }
        }
    }
}


@Composable
private fun EmptyTasks(
    selectedTab: Int
) {

    val title =
        when (selectedTab) {

            2 ->
                "Nada importante"

            3 ->
                "Aún no hay tareas hechas"

            else ->
                "No tienes tareas"
        }


    val subtitle =
        when (selectedTab) {

            2 ->
                "Marca una tarea como importante para verla aquí."

            3 ->
                "Las tareas completadas aparecerán aquí."

            else ->
                "Pulsa el botón + para crear tu primera tarea."
        }


    Box(

        modifier =
            Modifier.fillMaxSize(),

        contentAlignment =
            Alignment.Center
    ) {

        Column(

            horizontalAlignment =
                Alignment.CenterHorizontally,

            modifier =
                Modifier.padding(32.dp)
        ) {

            Surface(

                modifier =
                    Modifier.size(82.dp),

                shape =
                    CircleShape,

                color =
                    MaterialTheme
                        .colorScheme
                        .surfaceContainerHigh
            ) {

                Box(
                    contentAlignment =
                        Alignment.Center
                ) {

                    Icon(

                        imageVector =
                            when (selectedTab) {

                                2 ->
                                    Icons.Rounded.Star

                                3 ->
                                    Icons.Rounded.Check

                                else ->
                                    Icons.Rounded.TaskAlt
                            },

                        contentDescription =
                            null,

                        modifier =
                            Modifier.size(37.dp),

                        tint =
                            MaterialTheme
                                .colorScheme
                                .primary
                    )
                }
            }


            Spacer(
                modifier =
                    Modifier.height(16.dp)
            )


            Text(

                text = title,

                style =
                    MaterialTheme
                        .typography
                        .titleLarge,

                fontWeight =
                    FontWeight.Bold
            )


            Spacer(
                modifier =
                    Modifier.height(5.dp)
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
    }
}


@Composable
private fun TaskRow(
    task: Task,
    onToggle: () -> Unit,
    onImportant: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {

    var menuVisible by remember {
        mutableStateOf(false)
    }


    Card(

        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(
                    onClick = onEdit
                ),

        shape =
            RoundedCornerShape(21.dp),

        colors =
            CardDefaults.cardColors(

                containerColor =
                    if (task.completed) {

                        MaterialTheme
                            .colorScheme
                            .surfaceContainerLow

                    } else {

                        MaterialTheme
                            .colorScheme
                            .surfaceContainer
                    }
            ),

        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 0.dp
            )

    ) {

        Row(

            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 8.dp,
                        end = 5.dp,
                        top = 8.dp,
                        bottom = 8.dp
                    ),

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            TaskCheckbox(

                checked =
                    task.completed,

                onClick =
                    onToggle
            )


            Spacer(
                modifier =
                    Modifier.width(10.dp)
            )


            Column(

                modifier =
                    Modifier.weight(1f)
            ) {

                Text(

                    text =
                        task.title,

                    style =
                        MaterialTheme
                            .typography
                            .bodyLarge,

                    fontWeight =
                        FontWeight.Medium,

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
                            Modifier.height(2.dp)
                    )

                    Text(

                        text =
                            task.notes,

                        maxLines = 2,

                        style =
                            MaterialTheme
                                .
