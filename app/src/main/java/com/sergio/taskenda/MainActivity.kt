package com.sergio.taskenda

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBarItem
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

                    val item =
                        array.getJSONObject(i)

                    add(
                        Task(
                            id = item.optString("id"),
                            title = item.optString("title"),
                            notes = item.optString("notes"),
                            completed =
                                item.optBoolean("completed"),
                            important =
                                item.optBoolean("important"),
                            dueDate =
                                item.optString("dueDate")
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
        if (android.os.Build.VERSION.SDK_INT >= 31) {

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

    var showAddSheet by remember {
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

                selectedTab =
                    selectedTab,

                onTabSelected = { tab ->

                    if (
                        selectedTab != tab
                    ) {

                        haptic.performHapticFeedback(
                            HapticFeedbackType.VirtualKey
                        )

                        selectedTab = tab
                    }
                }
            )
        },

        floatingActionButtonPosition =
            FabPosition.Center,

        floatingActionButton = {

            FloatingActionButton(

                onClick = {

                    haptic.performHapticFeedback(
                        HapticFeedbackType.Confirm
                    )

                    editingTask = null

                    showAddSheet = true
                },

                modifier =
                    Modifier.size(58.dp),

                shape =
                    RoundedCornerShape(19.dp),

                containerColor =
                    MaterialTheme.colorScheme.primaryContainer,

                contentColor =
                    MaterialTheme.colorScheme.onPrimaryContainer

            ) {

                Icon(

                    Icons.Rounded.Add,

                    contentDescription =
                        "Añadir tarea",

                    modifier =
                        Modifier.size(28.dp)
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

                selectedTab =
                    selectedTab,

                taskCount =
                    visibleTasks.size
            )

            Spacer(
                modifier =
                    Modifier.height(18.dp)
            )

            if (
                visibleTasks.isEmpty()
            ) {

                EmptyTasks(
                    selectedTab =
                        selectedTab
                )

            } else {

                LazyColumn(

                    modifier =
                        Modifier.fillMaxSize(),

                    contentPadding =
                        PaddingValues(
                            bottom = 90.dp
                        ),

                    verticalArrangement =
                        Arrangement.spacedBy(
                            10.dp
                        )

                ) {

                    items(

                        items =
                            visibleTasks,

                        key = {
                            it.id
                        }

                    ) { task ->

                        TaskRow(

                            task =
                                task,

                            onToggle = {

                                val index =
                                    tasks.indexOfFirst {
                                        it.id ==
                                            task.id
                                    }

                                if (
                                    index >= 0
                                ) {

                                    tasks[index] =
                                        tasks[index].copy(
                                            completed =
                                                !tasks[index]
                                                    .completed
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
                                        it.id ==
                                            task.id
                                    }

                                if (
                                    index >= 0
                                ) {

                                    tasks[index] =
                                        tasks[index].copy(
                                            important =
                                                !tasks[index]
                                                    .important
                                        )

                                    haptic.performHapticFeedback(
                                        HapticFeedbackType.VirtualKey
                                    )

                                    persist()
                                }
                            },

                            onEdit = {

                                editingTask =
                                    task

                                showAddSheet =
                                    true
                            },

                            onDelete = {

                                taskToDelete =
                                    task
                            }
                        )
                    }
                }
            }
        }
    }

    if (
        showAddSheet
    ) {

        TaskEditorSheet(

            task =
                editingTask,

            onDismiss = {

                showAddSheet =
                    false

                editingTask =
                    null
            },

            onSave = {

                title,
                notes,
                important,
                dueDate ->

                if (
                    editingTask == null
                ) {

                    tasks.add(
                        Task(
                            title =
                                title,
                            notes =
                                notes,
                            important =
                                important,
                            dueDate =
                                dueDate
                        )
                    )

                } else {

                    val index =
                        tasks.indexOfFirst {
                            it.id ==
                                editingTask!!.id
                        }

                    if (
                        index >= 0
                    ) {

                        tasks[index] =
                            tasks[index].copy(
                                title =
                                    title,
                                notes =
                                    notes,
                                important =
                                    important,
                                dueDate =
                                    dueDate
                            )
                    }
                }

                persist()

                haptic.performHapticFeedback(
                    HapticFeedbackType.Confirm
                )

                showAddSheet =
                    false

                editingTask =
                    null
            }
        )
    }

    if (
        taskToDelete != null
    ) {

        AlertDialog(

            onDismissRequest = {

                taskToDelete =
                    null
            },

            title = {

                Text(
                    "Eliminar tarea"
                )
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

                        haptic.performHapticFeedback(
                            HapticFeedbackType.Reject
                        )

                        taskToDelete =
                            null
                    }

                ) {

                    Text(
                        "Eliminar"
                    )
                }
            },

            dismissButton = {

                TextButton(

                    onClick = {

                        taskToDelete =
                            null
                    }

                ) {

                    Text(
                        "Cancelar"
                    )
                }
            }
        )
    }
}

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
                    start = 18.dp,
                    end = 18.dp,
                    bottom = 8.dp
                ),

        shape =
            RoundedCornerShape(28.dp),

        color =
            MaterialTheme.colorScheme.surfaceContainerHigh,

        tonalElevation =
            5.dp,

        shadowElevation =
            6.dp

    ) {

        Row(

            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(68.dp)
                    .padding(
                        horizontal = 5.dp
                    ),

            verticalAlignment =
                Alignment.CenterVertically,

            horizontalArrangement =
                Arrangement.SpaceEvenly

        ) {

            TaskNavItem(

                selected =
                    selectedTab == 0,

                icon =
                    Icons.Rounded.Home,

                label =
                    "Inicio",

                onClick = {

                    onTabSelected(0)
                }
            )

            TaskNavItem(

                selected =
                    selectedTab == 1,

                icon =
                    Icons.Rounded.Upcoming,

                label =
                    "Pendientes",

                onClick = {

                    onTabSelected(1)
                }
            )

            Spacer(
                modifier =
                    Modifier.width(54.dp)
            )

            TaskNavItem(

                selected =
                    selectedTab == 2,

                icon =
                    Icons.Rounded.Star,

                label =
                    "Importantes",

                onClick = {

                    onTabSelected(2)
                }
            )

            TaskNavItem(

                selected =
                    selectedTab == 3,

                icon =
                    Icons.Rounded.Check,

                label =
                    "Hechas",

                onClick = {

                    onTabSelected(3)
                }
            )
        }
    }
}

@Composable
private fun TaskNavItem(

    selected: Boolean,

    icon:
        androidx.compose.ui.graphics.vector.ImageVector,

    label: String,

    onClick: () -> Unit

) {

    val background by
        animateColorAsState(

            targetValue =
                if (selected) {

                    MaterialTheme
                        .colorScheme
                        .primaryContainer

                } else {

                    Color.Transparent
                },

            animationSpec =
                spring(
                    stiffness =
                        Spring.StiffnessMediumLow
                ),

            label =
                "navigationBackground"
        )

    val contentColor =
        if (selected) {

            MaterialTheme
                .colorScheme
                .onPrimaryContainer

        } else {

            MaterialTheme
                .colorScheme
                .onSurfaceVariant
        }

    Surface(

        modifier =
            Modifier
                .height(54.dp)
                .clickable(
                    onClick =
                        onClick
                ),

        shape =
            RoundedCornerShape(
                21.dp
            ),

        color =
            background

    ) {

        Row(

            modifier =
                Modifier
                    .padding(
                        horizontal =
                            if (selected) {
                                10.dp
                            } else {
                                13.dp
                            }
                    ),

            verticalAlignment =
                Alignment.CenterVertically,

            horizontalArrangement =
                Arrangement.Center

        ) {

            Icon(

                imageVector =
                    icon,

                contentDescription =
                    label,

                modifier =
                    Modifier.size(
                        23.dp
                    ),

                tint =
                    contentColor
            )

            AnimatedVisibility(

                visible =
                    selected,

                enter =
                    fadeIn() +
                        scaleIn(),

                exit =
                    fadeOut() +
                        scaleOut()

            ) {

                Row {

                    Spacer(
                        modifier =
                            Modifier.width(5.dp)
                    )

                    Text(

                        text =
                            label,

                        style =
                            MaterialTheme
                                .typography
                                .labelMedium,

                        fontWeight =
                            FontWeight.SemiBold,

                        color =
                            contentColor,

                        maxLines =
                            1
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

                text =
                    title,

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

                text =
                    subtitle,

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

            shape =
                CircleShape,

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
                    Modifier.size(84.dp),

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
                            Modifier.size(38.dp),

                        tint =
                            MaterialTheme
                                .colorScheme
                                .primary
                    )
                }
            }

            Spacer(
                modifier =
                    Modifier.height(18.dp)
            )

            Text(

                title,

                style =
                    MaterialTheme
                        .typography
                        .titleLarge,

                fontWeight =
                    FontWeight.Bold
            )

            Spacer(
                modifier =
                    Modifier.height(6.dp)
            )

            Text(

                subtitle,

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

    val containerColor by
        animateColorAsState(

            targetValue =
                if (task.completed) {

                    MaterialTheme
                        .colorScheme
                        .surfaceContainerLow

                } else {

                    MaterialTheme
                        .colorScheme
                        .surfaceContainer
                },

            animationSpec =
                spring(
                    stiffness =
                        Spring.StiffnessMediumLow
                ),

            label =
                "taskContainer"
        )

    var menuVisible by
        remember {
            mutableStateOf(false)
        }

    Card(

        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(
                    onClick =
                        onEdit
                ),

        shape =
            RoundedCornerShape(22.dp),

        colors =
            CardDefaults.cardColors(
                containerColor =
                    containerColor
            ),

        elevation =
            CardDefaults.cardElevation(
                defaultElevation =
                    0.dp
            )

    ) {

        Row(

            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 8.dp,
                        end = 6.dp,
                        top = 9.dp,
                        bottom = 9.dp
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

                            TextDecoration
                                .LineThrough

                        } else {

                            TextDecoration
                                .None
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

                if (
                    task.notes.isNotBlank()
                ) {

                    Spacer(
                        modifier =
                            Modifier.height(3.dp)
                    )

                    Text(

                        text =
                            task.notes,

                        maxLines =
                            2,

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

                if (
                    task.dueDate.isNotBlank()
                ) {

                    Spacer(
                        modifier =
                            Modifier.height(5.dp)
                    )

                    Row(

                        verticalAlignment =
                            Alignment.CenterVertically

                    ) {

                        Icon(

                            Icons.Rounded.Today,

                            contentDescription =
                                null,

                            modifier =
                                Modifier.size(
                                    15.dp
                                ),

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

                            task.dueDate,

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

                onClick =
                    onImportant

            ) {

                Icon(

                    Icons.Rounded.Star,

                    contentDescription =
                        if (
                            task.important
                        ) {

                            "Quitar de importantes"

                        } else {

                            "Marcar como importante"
                        },

                    tint =
                        if (
                            task.important
                        ) {

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

                        menuVisible =
                            !menuVisible
                    }

                ) {

                    Icon(

                        Icons.Rounded.MoreVert,

                        contentDescription =
                            "Más opciones"
                    )
                }

                DropdownMenu(

                    expanded =
                        menuVisible,

                    onDismissRequest = {

                        menuVisible =
                            false
                    }

                ) {

                    DropdownMenuItem(

                        text = {
                            Text(
                                "Editar"
                            )
                        },

                        leadingIcon = {

                            Icon(
                                Icons.Rounded.Edit,
                                contentDescription =
                                    null
                            )
                        },

                        onClick = {

                            menuVisible =
                                false

                            onEdit()
                        }
                    )

                    DropdownMenuItem(

                        text = {
                            Text(
                                "Eliminar"
                            )
                        },

                        leadingIcon = {

                            Icon(
                                Icons.Rounded.Delete,
                                contentDescription =
                                    null
                            )
                        },

                        onClick = {

                            menuVisible =
                                false

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

    onClick: () -> Unit

) {

    val background by
        animateColorAsState(

            targetValue =
                if (checked) {

                    MaterialTheme
                        .colorScheme
                        .primary

                } else {

                    Color.Transparent
                },

            animationSpec =
                spring(
                    stiffness =
                        Spring.StiffnessMedium
                ),

            label =
                "checkboxBackground"
        )

    val borderColor =
        if (checked) {

            MaterialTheme
                .colorScheme
                .primary

        } else {

            MaterialTheme
                .colorScheme
                .outline
        }

    Surface(

        modifier =
            Modifier
                .size(44.dp)
                .clickable(
                    onClick =
                        onClick
                ),

        shape =
            CircleShape,

        color =
            background,

        border =
            BorderStroke(

                width =
                    if (checked) {
                        0.dp
                    } else {
                        2.dp
                    },

                color =
                    borderColor
            )

    ) {

        Box(
            contentAlignment =
                Alignment.Center
        ) {

            AnimatedVisibility(

                visible =
                    checked,

                enter =
                    fadeIn() +
                        scaleIn(),

                exit =
                    fadeOut() +
                        scaleOut()

            ) {

                Icon(

                    Icons.Rounded.Check,

                    contentDescription =
                        null,

                    modifier =
                        Modifier.size(
                            24.dp
                        ),

                    tint =
                        MaterialTheme
                            .colorScheme
                            .onPrimary
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

    onSave: (
        title: String,
        notes: String,
        important: Boolean,
        dueDate: String
    ) -> Unit

) {

    val sheetState =
        rememberModalBottomSheetState(
            skipPartiallyExpanded =
                true
        )

    var title by remember(
        task?.id
    ) {

        mutableStateOf(
            task?.title ?: ""
        )
    }

    var notes by remember(
        task?.id
    ) {

        mutableStateOf(
            task?.notes ?: ""
        )
    }

    var important by remember(
        task?.id
    ) {

        mutableStateOf(
            task?.important ?: false
        )
    }

    var dueDate by remember(
        task?.id
    ) {

        mutableStateOf(
            task?.dueDate
                ?: todayString()
        )
    }

    var showDatePicker by remember {
        mutableStateOf(false)
    }

    ModalBottomSheet(

        onDismissRequest =
            onDismiss,

        sheetState =
            sheetState,

        shape =
            RoundedCornerShape(
                topStart = 30.dp,
                topEnd = 30.dp
            )

    ) {

        Column(

            modifier =
                Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(
                        0.92f
                    )
                    .imePadding()
                    .padding(
                        start = 20.dp,
                        end = 20.dp,
                        bottom = 10.dp
                    )

        ) {

            Box(

                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(
                            bottom = 12.dp
                        ),

                contentAlignment =
                    Alignment.Center

            ) {

                Surface(

                    modifier =
                        Modifier.size(
                            width = 34.dp,
                            height = 4.dp
                        ),

                    shape =
                        RoundedCornerShape(
                            99.dp
                        ),

                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant
                            .copy(
                                alpha = 0.45f
                            )

                ) {}
            }

            Column(

                modifier =
                    Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(
                            rememberScrollState()
                        )

            ) {

                Text(

                    text =
                        if (
                            task == null
                        ) {

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

                Spacer(
                    modifier =
                        Modifier.height(
                            18.dp
                        )
                )

                OutlinedTextField(

                    value =
                        title,

                    onValueChange = {
                        title = it
                    },

                    modifier =
                        Modifier.fillMaxWidth(),

                    singleLine = true,

                    label = {
                        Text(
                            "Tarea"
                        )
                    },

                    leadingIcon = {

                        Icon(

                            Icons.Rounded.TaskAlt,

                            contentDescription =
                                null
                        )
                    },

                    shape =
                        RoundedCornerShape(
                            18.dp
                        )
                )

                Spacer(
                    modifier =
                        Modifier.height(
                            12.dp
                        )
                )

                OutlinedTextField(

                    value =
                        notes,

                    onValueChange = {
                        notes = it
                    },

                    modifier =
                        Modifier.fillMaxWidth(),

                    minLines =
                        3,

                    maxLines =
                        5,

                    label = {
                        Text(
                            "Notas"
                        )
                    },

                    shape =
                        RoundedCornerShape(
                            18.dp
                        )
                )

                Spacer(
                    modifier =
                        Modifier.height(
                            12.dp
                        )
                )

                OutlinedTextField(

                    value =
                        dueDate,

                    onValueChange = {},

                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .clickable {

                                showDatePicker =
                                    true
                            },

                    readOnly =
                        true,

                    singleLine =
                        true,

                    label = {
                        Text(
                            "Fecha"
                        )
                    },

                    leadingIcon = {

                        Icon(

                            Icons.Rounded.Today,

                            contentDescription =
                                null
                        )
                    },

                    shape =
                        RoundedCornerShape(
                            18.dp
                        )
                )

                Spacer(
                    modifier =
                        Modifier.height(
                            10.dp
                        )
                )

                Surface(

                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .clickable {

                                important =
                                    !important
                            },

                    shape =
                        RoundedCornerShape(
                            18.dp
                        ),

                    color =
                        if (
                            important
                        ) {

                            MaterialTheme
                                .colorScheme
                                .primaryContainer

                        } else {

                            MaterialTheme
                                .colorScheme
                                .surfaceContainer
                        }

                ) {

                    Row(

                        modifier =
                            Modifier.padding(
                                15.dp
                            ),

                        verticalAlignment =
                            Alignment.CenterVertically

                    ) {

                        Icon(

                            Icons.Rounded.Star,

                            contentDescription =
                                null,

                            tint =
                                if (
                                    important
                                ) {

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
                                Modifier.width(
                                    12.dp
                                )
                        )

                        Text(

                            "Marcar como importante",

                            modifier =
                                Modifier.weight(
                                    1f
                                )
                        )

                        Switch(

                            checked =
                                important,

                            onCheckedChange = {
                                important = it
                            }
                        )
                    }
                }

                Spacer(
                    modifier =
                        Modifier.height(
                            12.dp
                        )
                )
            }

            Button(

                onClick = {

                    val cleanTitle =
                        title.trim()

                    if (
                        cleanTitle.isNotEmpty()
                    ) {

                        onSave(

                            cleanTitle,

                            notes.trim(),

                            important,

                            dueDate.trim()
                        )
                    }
                },

                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(
                            54.dp
                        ),

                shape =
                    RoundedCornerShape(
                        18.dp
                    )

            ) {

                Icon(

                    Icons.Rounded.Check,

                    contentDescription =
                        null
                )

                Spacer(
                    modifier =
                        Modifier.width(
                            8.dp
                        )
                )

                Text(

                    if (
                        task == null
                    ) {

                        "Crear tarea"

                    } else {

                        "Guardar cambios"
                    }
                )
            }
        }
    }

    if (
        showDatePicker
    ) {

        TaskDatePickerDialog(

            currentDate =
                dueDate,

            onDismiss = {

                showDatePicker =
                    false
            },

            onDateSelected = { date ->

                dueDate =
                    date

                showDatePicker =
                    false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TaskDatePickerDialog(

    currentDate: String,

    onDismiss: () -> Unit,

    onDateSelected:
        (String) -> Unit

) {

    val initialMillis =
        remember(
            currentDate
        ) {

            parseDateMillis(
                currentDate
            )
        }

    val datePickerState =
        rememberDatePickerState(
            initialSelectedDateMillis =
                initialMillis
        )

    DatePickerDialog(

        onDismissRequest =
            onDismiss,

        confirmButton = {

            TextButton(

                onClick = {

                    val millis =
                        datePickerState
                            .selectedDateMillis

                    if (
                        millis != null
                    ) {

                        onDateSelected(
                            formatDate(
                                millis
                            )
                        )

                    } else {

                        onDismiss()
                    }
                }

            ) {

                Text(
                    "Aceptar"
                )
            }
        },

        dismissButton = {

            TextButton(
                onClick =
                    onDismiss
            ) {

                Text(
                    "Cancelar"
                )
            }
        }

    ) {

        DatePicker(

            state =
                datePickerState,

            showModeToggle =
                false
        )
    }
}

private fun todayString(): String {

    return SimpleDateFormat(

        "dd/MM/yyyy",

        Locale.getDefault()

    ).format(
        Date()
    )
}

private fun parseDateMillis(
    value: String
): Long {

    return try {

        val format =
            SimpleDateFormat(
                "dd/MM/yyyy",
                Locale.getDefault()
            )

        format.isLenient =
            false

        format.parse(
            value
        )?.time
            ?: Calendar
                .getInstance()
                .timeInMillis

    } catch (_: Exception) {

        Calendar
            .getInstance()
            .timeInMillis
    }
}

private fun formatDate(
    millis: Long
): String {

    return SimpleDateFormat(

        "dd/MM/yyyy",

        Locale.getDefault()

    ).format(
        Date(millis)
    )
}
