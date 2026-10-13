package com.sergio.taskenda

import android.app.Activity
import android.app.DatePickerDialog as AndroidDatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.pm.PackageManager
import android.view.HapticFeedbackConstants
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.animation.animateColorAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.Room
import com.sergio.taskenda.data.Subtask
import com.sergio.taskenda.data.Task
import com.sergio.taskenda.data.TaskDatabase
import com.sergio.taskenda.data.TaskRepository
import com.sergio.taskenda.data.TaskListStore
import com.sergio.taskenda.ui.TaskViewModel
import com.sergio.taskenda.ui.TaskViewModelFactory
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = Room.databaseBuilder(
            applicationContext,
            TaskDatabase::class.java,
            "taskenda.db"
        )
            .addMigrations(
                TaskDatabase.MIGRATION_1_2,
                TaskDatabase.MIGRATION_2_3,
                TaskDatabase.MIGRATION_3_4
            )
            .build()

        val repository = TaskRepository(database.taskDao())

        setContent {
            val darkTheme = isSystemInDarkTheme()
            val context = LocalContext.current

            val colorScheme = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (darkTheme) {
                    dynamicDarkColorScheme(context)
                } else {
                    dynamicLightColorScheme(context)
                }
            } else {
                if (darkTheme) darkColorScheme() else lightColorScheme()
            }

            MaterialTheme(colorScheme = colorScheme) {
                val listStore = remember { TaskListStore(context) }
                val vm: TaskViewModel = viewModel(
                    factory = TaskViewModelFactory(repository, listStore, context)
                )

                TaskendaApp(vm)
            }
        }
    }
}

@Composable
private fun TaskendaApp(vm: TaskViewModel) {

    val tasks by vm.tasks.collectAsState()
    val taskLists by vm.lists.collectAsState()
    val subtasks by vm.subtasks.collectAsState()

    val subtasksByTask = remember(subtasks) {
        subtasks.groupBy { it.taskId }
    }

    var selectedList by remember { mutableStateOf(vm.currentList) }
    var listSheetVisible by remember { mutableStateOf(false) }

    var selectedTab by remember {
        mutableIntStateOf(0)
    }

    var editorVisible by remember {
        mutableStateOf(false)
    }

    var editingTask by remember {
        mutableStateOf<Task?>(null)
    }

    val listTasks = remember(tasks, selectedList) {
        tasks.filter { it.listName == selectedList }
    }

    // Filter each section once per task/list update instead of recalculating during transitions.
    val tabTasksByCategory = remember(listTasks) {
        listOf(
            listTasks.filter { !it.completed && isTodayOrOverdue(it.dueDate) },
            listTasks.filter { !it.completed },
            listTasks.filter { !it.completed && it.important },
            listTasks.filter { it.completed }
        )
    }

    val headerTitle = when (selectedTab) {
        0 -> "Hoy"
        1 -> "Tareas"
        2 -> "Importantes"
        3 -> "Completadas"
        else -> "Tareas"
    }

    val headerSubtitle = when (selectedTab) {
        0 -> "Tus tareas pendientes"
        1 -> "Todas las tareas pendientes"
        2 -> "Tareas marcadas como importantes"
        3 -> "Tareas que ya has terminado"
        else -> ""
    }

    val headerCount = when (selectedTab) {
        0 -> listTasks.count { !it.completed && isTodayOrOverdue(it.dueDate) }
        1 -> listTasks.count { !it.completed }
        2 -> listTasks.count { !it.completed && it.important }
        3 -> listTasks.count { it.completed }
        else -> 0
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingTask = null
                    editorVisible = true
                },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Añadir tarea"
                )
            }
        },
        bottomBar = {
            TaskendaBottomBar(
                selectedTab = selectedTab,
                onTabSelected = { newTab ->
                    if (newTab != selectedTab) {
                        selectedTab = newTab
                    }
                }
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            Crossfade(
                targetState = selectedTab,
                animationSpec = tween(
                    durationMillis = 180,
                    easing = FastOutSlowInEasing
                ),
                label = "taskenda_header_transition"
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
                        0 -> "Tus tareas pendientes"
                        1 -> "Todas las tareas pendientes"
                        2 -> "Tareas marcadas como importantes"
                        3 -> "Tareas que ya has terminado"
                        else -> ""
                    },
                    taskCount = when (tab) {
                        0 -> listTasks.count { !it.completed && isTodayOrOverdue(it.dueDate) }
                        1 -> listTasks.count { !it.completed }
                        2 -> listTasks.count { !it.completed && it.important }
                        3 -> listTasks.count { it.completed }
                        else -> 0
                    },
                    listName = selectedList,
                    onListClick = { listSheetVisible = true }
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {

                Crossfade(
                    targetState = selectedTab,
                    animationSpec = tween(
                        durationMillis = 240,
                        easing = FastOutSlowInEasing
                    ),
                    label = "taskenda_category_transition"
                ) { tab ->

                    val tabTasks = tabTasksByCategory.getOrElse(tab) { emptyList() }

                    if (tabTasks.isEmpty()) {
                        EmptyTasks(
                            selectedTab = tab,
                            onCreate = {
                                editingTask = null
                                editorVisible = true
                            }
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                start = 16.dp,
                                end = 16.dp,
                                top = 8.dp,
                                bottom = 110.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(
                                items = tabTasks,
                                key = { it.id }
                            ) { task ->
                                TaskRow(
                                    task = task,
                                    subtasks = subtasksByTask[task.id].orEmpty(),
                                    onToggleComplete = {
                                        vm.toggle(task)
                                    },
                                    onToggleImportant = {
                                        vm.setImportant(task)
                                    },
                                    onEdit = {
                                        editingTask = task
                                        editorVisible = true
                                    },
                                    onDelete = {
                                        vm.delete(task)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (listSheetVisible) {
        TaskListManagerSheet(
            lists = taskLists,
            selectedList = selectedList,
            onSelectList = { name ->
                selectedList = name
                vm.selectList(name)
                listSheetVisible = false
            },
            onCreateList = { name ->
                val created = vm.createList(name)
                if (created != null) {
                    selectedList = created
                    vm.selectList(created)
                }
            },
            onRenameList = { oldName, newName ->
                vm.renameList(oldName, newName)
                if (selectedList == oldName) selectedList = newName
            },
            onDeleteList = { name ->
                vm.deleteList(name)
                if (selectedList == name) {
                    selectedList = vm.currentList
                }
            },
            onDismiss = { listSheetVisible = false }
        )
    }

    if (editorVisible) {
        TaskEditorSheet(
            task = editingTask,
            lists = taskLists,
            defaultList = selectedList,
            onDismiss = {
                editorVisible = false
                editingTask = null
            },
            subtasks = editingTask?.let { subtasksByTask[it.id].orEmpty() } ?: emptyList(),
            onSave = { updatedTask, updatedSubtasks ->
                vm.save(updatedTask, updatedSubtasks)
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
    taskCount: Int,
    listName: String,
    onListClick: () -> Unit
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
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(4.dp))

            TextButton(
                onClick = onListClick,
                contentPadding = PaddingValues(horizontal = 0.dp, vertical = 0.dp)
            ) {
                Text(
                    text = listName,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.width(2.dp))
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Cambiar lista",
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(Modifier.width(12.dp))

        Surface(
            modifier = Modifier.size(48.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.secondaryContainer
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = taskCount.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
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
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(72.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = when (selectedTab) {
                            2 -> Icons.Default.Star
                            3 -> Icons.Default.CheckCircle
                            else -> Icons.Default.TaskAlt
                        },
                        contentDescription = null,
                        modifier = Modifier.size(36.dp),
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            Spacer(Modifier.height(18.dp))

            Text(
                text = when (selectedTab) {
                    2 -> "No tienes tareas importantes"
                    3 -> "No tienes tareas completadas"
                    else -> "No tienes tareas pendientes"
                },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = when (selectedTab) {
                    2 -> "Marca una tarea como importante para verla aquí."
                    3 -> "Las tareas que completes aparecerán aquí."
                    else -> "Pulsa + para crear tu primera tarea."
                },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            if (selectedTab == 0 || selectedTab == 1) {
                Spacer(Modifier.height(18.dp))
                Button(onClick = onCreate) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Crear tarea")
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TaskRow(
    task: Task,
    subtasks: List<Subtask>,
    onToggleComplete: () -> Unit,
    onToggleImportant: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {

    val context = LocalContext.current
    var menuExpanded by remember(task.id) {
        mutableStateOf(false)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onEdit,
                onLongClick = onToggleImportant
            ),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 8.dp,
                    end = 4.dp,
                    top = 8.dp,
                    bottom = 8.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TaskCheckbox(
                checked = task.completed,
                onCheckedChange = {
                    vibrate(context)
                    onToggleComplete()
                }
            )

            Spacer(Modifier.width(8.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 4.dp)
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    textDecoration = if (task.completed) {
                        TextDecoration.LineThrough
                    } else {
                        TextDecoration.None
                    },
                    color = if (task.completed) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (task.notes.isNotBlank()) {
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = task.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (subtasks.isNotEmpty()) {
                    Spacer(Modifier.height(5.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "${subtasks.count { it.completed }}/${subtasks.size} subtareas",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                task.dueDate?.let { date ->
                    Spacer(Modifier.height(5.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Event,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = formatDate(date),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            IconButton(onClick = onToggleImportant) {
                Icon(
                    imageVector = if (task.important) {
                        Icons.Default.Star
                    } else {
                        Icons.Outlined.StarBorder
                    },
                    contentDescription = if (task.important) {
                        "Quitar de importantes"
                    } else {
                        "Marcar como importante"
                    },
                    tint = if (task.important) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }

            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Más opciones"
                    )
                }

                androidx.compose.material3.DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    androidx.compose.material3.DropdownMenuItem(
                        text = { Text("Editar") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null
                            )
                        },
                        onClick = {
                            menuExpanded = false
                            onEdit()
                        }
                    )

                    androidx.compose.material3.DropdownMenuItem(
                        text = { Text("Eliminar") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Delete,
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

@Composable
private fun TaskCheckbox(
    checked: Boolean,
    onCheckedChange: () -> Unit
) {
    IconButton(
        onClick = onCheckedChange,
        modifier = Modifier.size(48.dp)
    ) {
        Surface(
            modifier = Modifier.size(44.dp),
            shape = CircleShape,
            color = if (checked) {
                MaterialTheme.colorScheme.primary
            } else {
                Color.Transparent
            },
            border = if (checked) {
                null
            } else {
                BorderStroke(
                    width = 2.dp,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        ) {
            Box(contentAlignment = Alignment.Center) {
                AnimatedContent(
                    targetState = checked,
                    label = "task_checkbox"
                ) { isChecked ->
                    if (isChecked) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Completada",
                            modifier = Modifier.size(25.dp),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Spacer(Modifier.size(1.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TaskEditorSheet(
    task: Task?,
    lists: List<String>,
    defaultList: String,
    subtasks: List<Subtask>,
    onDismiss: () -> Unit,
    onSave: (Task, List<Subtask>) -> Unit
) {

    val context = LocalContext.current

    var title by remember(task?.id) {
        mutableStateOf(task?.title.orEmpty())
    }

    var notes by remember(task?.id) {
        mutableStateOf(task?.notes.orEmpty())
    }

    var important by remember(task?.id) {
        mutableStateOf(task?.important ?: false)
    }

    var dueDate by remember(task?.id) {
        mutableStateOf(task?.dueDate)
    }

    var reminderAt by remember(task?.id) {
        mutableStateOf(task?.reminderAt)
    }

    var recurrence by remember(task?.id) {
        mutableStateOf(task?.recurrence ?: "NONE")
    }

    var repeatMenuExpanded by remember { mutableStateOf(false) }

    var listName by remember(task?.id, defaultList) {
        mutableStateOf(task?.listName ?: defaultList)
    }

    var localSubtasks by remember(task?.id) {
        mutableStateOf(subtasks.mapIndexed { index, subtask ->
            subtask.copy(position = index)
        })
    }

    var newSubtaskTitle by remember(task?.id) {
        mutableStateOf("")
    }

    var listMenuExpanded by remember { mutableStateOf(false) }

    var datePickerVisible by remember {
        mutableStateOf(false)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(
                    start = 20.dp,
                    end = 20.dp,
                    bottom = 28.dp
                ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = if (task == null) "Nueva tarea" else "Editar tarea",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Tarea") },
                placeholder = { Text("¿Qué quieres hacer?") },
                singleLine = true
            )

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5,
                label = { Text("Notas") },
                placeholder = { Text("Añade información opcional") }
            )

            Box {
                OutlinedButton(
                    onClick = { listMenuExpanded = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.TaskAlt,
                        contentDescription = null
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = listName,
                        modifier = Modifier.weight(1f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Start,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = null
                    )
                }

                androidx.compose.material3.DropdownMenu(
                    expanded = listMenuExpanded,
                    onDismissRequest = { listMenuExpanded = false }
                ) {
                    lists.forEach { name ->
                        androidx.compose.material3.DropdownMenuItem(
                            text = { Text(name, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                            trailingIcon = if (name == listName) {
                                { Icon(Icons.Default.Check, contentDescription = null) }
                            } else null,
                            onClick = {
                                listName = name
                                listMenuExpanded = false
                            }
                        )
                    }
                }
            }

            OutlinedButton(
                onClick = { datePickerVisible = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Outlined.Event,
                    contentDescription = null
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = dueDate?.let(::formatDate) ?: "Añadir fecha",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (dueDate != null) {
                TextButton(
                    onClick = { dueDate = null },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Quitar fecha")
                }
            }

            OutlinedButton(
                onClick = {
                    val activity = context as? Activity
                    if (Build.VERSION.SDK_INT >= 33 &&
                        ContextCompat.checkSelfPermission(
                            context,
                            android.Manifest.permission.POST_NOTIFICATIONS
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        activity?.requestPermissions(
                            arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                            7103
                        )
                    }

                    // Default to a future time. A due date is usually stored at midnight,
                    // which previously made a same-day reminder silently expire before saving.
                    val nowPlusFiveMinutes = System.currentTimeMillis() + 5 * 60 * 1000L
                    val initial = Calendar.getInstance().apply {
                        when {
                            reminderAt != null -> timeInMillis = reminderAt!!
                            dueDate != null -> {
                                timeInMillis = dueDate!!
                                val futureClock = Calendar.getInstance().apply {
                                    timeInMillis = nowPlusFiveMinutes
                                }
                                set(Calendar.HOUR_OF_DAY, futureClock.get(Calendar.HOUR_OF_DAY))
                                set(Calendar.MINUTE, futureClock.get(Calendar.MINUTE))
                                set(Calendar.SECOND, 0)
                                set(Calendar.MILLISECOND, 0)
                            }
                            else -> timeInMillis = nowPlusFiveMinutes
                        }
                    }
                    AndroidDatePickerDialog(
                        context,
                        { _, year, month, day ->
                            val selected = Calendar.getInstance().apply {
                                timeInMillis = initial.timeInMillis
                                set(Calendar.YEAR, year)
                                set(Calendar.MONTH, month)
                                set(Calendar.DAY_OF_MONTH, day)
                            }
                            TimePickerDialog(
                                context,
                                { _, hour, minute ->
                                    selected.set(Calendar.HOUR_OF_DAY, hour)
                                    selected.set(Calendar.MINUTE, minute)
                                    selected.set(Calendar.SECOND, 0)
                                    selected.set(Calendar.MILLISECOND, 0)
                                    reminderAt = selected.timeInMillis
                                },
                                selected.get(Calendar.HOUR_OF_DAY),
                                selected.get(Calendar.MINUTE),
                                true
                            ).show()
                        },
                        initial.get(Calendar.YEAR),
                        initial.get(Calendar.MONTH),
                        initial.get(Calendar.DAY_OF_MONTH)
                    ).show()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Outlined.Event, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = reminderAt?.let(::formatReminder) ?: "Añadir recordatorio",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (reminderAt != null) {
                if (reminderAt!! <= System.currentTimeMillis()) {
                    Text(
                        text = "La fecha y hora del recordatorio ya han pasado. Elige una hora futura.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                TextButton(
                    onClick = { reminderAt = null },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Quitar recordatorio")
                }
            }

            Box {
                OutlinedButton(
                    onClick = { repeatMenuExpanded = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = when (recurrence) {
                            "DAILY" -> "Repetir: cada día"
                            "WEEKLY" -> "Repetir: cada semana"
                            "MONTHLY" -> "Repetir: cada mes"
                            else -> "No se repite"
                        },
                        modifier = Modifier.weight(1f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Start
                    )
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
                }
                androidx.compose.material3.DropdownMenu(
                    expanded = repeatMenuExpanded,
                    onDismissRequest = { repeatMenuExpanded = false }
                ) {
                    listOf(
                        "NONE" to "No se repite",
                        "DAILY" to "Cada día",
                        "WEEKLY" to "Cada semana",
                        "MONTHLY" to "Cada mes"
                    ).forEach { (value, label) ->
                        androidx.compose.material3.DropdownMenuItem(
                            text = { Text(label) },
                            trailingIcon = if (recurrence == value) {
                                { Icon(Icons.Default.Check, contentDescription = null) }
                            } else null,
                            onClick = {
                                recurrence = value
                                repeatMenuExpanded = false
                            }
                        )
                    }
                }
            }

            if (localSubtasks.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Subtareas",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "${localSubtasks.count { it.completed }}/${localSubtasks.size} completadas",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        localSubtasks.forEachIndexed { index, subtask ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TaskCheckbox(
                                    checked = subtask.completed,
                                    onCheckedChange = {
                                        localSubtasks = localSubtasks.toMutableList().also { list ->
                                            list[index] = subtask.copy(completed = !subtask.completed)
                                        }
                                    }
                                )

                                Spacer(Modifier.width(6.dp))

                                Text(
                                    text = subtask.title,
                                    modifier = Modifier.weight(1f),
                                    style = MaterialTheme.typography.bodyMedium,
                                    textDecoration = if (subtask.completed) TextDecoration.LineThrough else TextDecoration.None,
                                    color = if (subtask.completed) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                                )

                                IconButton(
                                    onClick = {
                                        if (index > 0) {
                                            localSubtasks = localSubtasks.toMutableList().also { list ->
                                                val current = list[index]
                                                list[index] = list[index - 1]
                                                list[index - 1] = current
                                            }.mapIndexed { i, item -> item.copy(position = i) }
                                        }
                                    },
                                    enabled = index > 0
                                ) {
                                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Subir")
                                }

                                IconButton(
                                    onClick = {
                                        if (index < localSubtasks.lastIndex) {
                                            localSubtasks = localSubtasks.toMutableList().also { list ->
                                                val current = list[index]
                                                list[index] = list[index + 1]
                                                list[index + 1] = current
                                            }.mapIndexed { i, item -> item.copy(position = i) }
                                        }
                                    },
                                    enabled = index < localSubtasks.lastIndex
                                ) {
                                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Bajar", modifier = Modifier.rotate(180f))
                                }

                                IconButton(
                                    onClick = {
                                        localSubtasks = localSubtasks.toMutableList().also { it.removeAt(index) }
                                            .mapIndexed { i, item -> item.copy(position = i) }
                                    }
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Eliminar subtarea")
                                }
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (important) {
                        Icons.Default.Star
                    } else {
                        Icons.Outlined.StarBorder
                    },
                    contentDescription = null,
                    tint = if (important) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )

                Spacer(Modifier.width(10.dp))

                Text(
                    text = "Importante",
                    style = MaterialTheme.typography.bodyLarge
                )

                Spacer(Modifier.weight(1f))

                Switch(
                    checked = important,
                    onCheckedChange = { important = it }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newSubtaskTitle,
                    onValueChange = { newSubtaskTitle = it },
                    modifier = Modifier.weight(1f),
                    label = { Text("Nueva subtarea") },
                    singleLine = true
                )

                Spacer(Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        val clean = newSubtaskTitle.trim()
                        if (clean.isNotEmpty()) {
                            localSubtasks = localSubtasks + Subtask(
                                taskId = task?.id ?: 0L,
                                title = clean,
                                position = localSubtasks.size
                            )
                            newSubtaskTitle = ""
                        }
                    },
                    enabled = newSubtaskTitle.trim().isNotEmpty()
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Añadir subtarea")
                }
            }

            Button(
                onClick = {
                    val cleanTitle = title.trim()
                    if (cleanTitle.isBlank()) return@Button

                    onSave(
                        task?.copy(
                            title = cleanTitle,
                            notes = notes.trim(),
                            important = important,
                            dueDate = dueDate,
                            reminderAt = reminderAt,
                            recurrence = recurrence,
                            listName = listName
                        ) ?: Task(
                            title = cleanTitle,
                            notes = notes.trim(),
                            important = important,
                            dueDate = dueDate,
                            reminderAt = reminderAt,
                            recurrence = recurrence,
                            listName = listName
                        ),
                        localSubtasks.mapIndexed { index, subtask ->
                            subtask.copy(position = index)
                        }
                    )
                },
                enabled = title.trim().isNotBlank() && (reminderAt == null || reminderAt!! > System.currentTimeMillis()),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (task == null) {
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
            initialDate = dueDate,
            onDismiss = { datePickerVisible = false },
            onDateSelected = { selectedDate ->
                dueDate = selectedDate
                datePickerVisible = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TaskListManagerSheet(
    lists: List<String>,
    selectedList: String,
    onSelectList: (String) -> Unit,
    onCreateList: (String) -> Unit,
    onRenameList: (String, String) -> Unit,
    onDeleteList: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var createDialog by remember { mutableStateOf(false) }
    var renameTarget by remember { mutableStateOf<String?>(null) }
    var deleteTarget by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Mis listas",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Organiza tus tareas como quieras",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = { createDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Nueva lista")
                }
            }

            Spacer(Modifier.height(4.dp))

            lists.forEach { name ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (name == selectedList) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceContainer
                        }
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.TaskAlt,
                            contentDescription = null,
                            tint = if (name == selectedList) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = name,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onSelectList(name) },
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (name == selectedList) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (name == selectedList) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (name == selectedList) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Seleccionada",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        IconButton(onClick = { renameTarget = name }) {
                            Icon(Icons.Default.Edit, contentDescription = "Renombrar")
                        }
                        if (name != TaskListStore.DEFAULT_LIST) {
                            IconButton(onClick = { deleteTarget = name }) {
                                Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                            }
                        }
                    }
                }
            }
        }
    }

    if (createDialog) {
        ListNameDialog(
            title = "Nueva lista",
            confirmText = "Crear",
            onDismiss = { createDialog = false },
            onConfirm = {
                onCreateList(it)
                createDialog = false
            }
        )
    }

    renameTarget?.let { oldName ->
        ListNameDialog(
            title = "Renombrar lista",
            initialName = oldName,
            confirmText = "Guardar",
            onDismiss = { renameTarget = null },
            onConfirm = {
                onRenameList(oldName, it)
                renameTarget = null
            }
        )
    }

    deleteTarget?.let { name ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("Eliminar lista") },
            text = { Text("Las tareas de «$name» pasarán a «${TaskListStore.DEFAULT_LIST}».") },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteList(name)
                    deleteTarget = null
                }) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun ListNameDialog(
    title: String,
    initialName: String = "",
    confirmText: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var name by remember(initialName) { mutableStateOf(initialName) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Nombre") }
            )
        },
        confirmButton = {
            TextButton(
                enabled = name.trim().isNotBlank(),
                onClick = { onConfirm(name.trim()) }
            ) { Text(confirmText) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TaskDatePickerDialog(
    initialDate: Long?,
    onDismiss: () -> Unit,
    onDateSelected: (Long) -> Unit
) {

    val state = rememberDatePickerState(
        initialSelectedDateMillis = initialDate
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val selected = state.selectedDateMillis
                    if (selected != null) {
                        onDateSelected(selected)
                    } else {
                        onDismiss()
                    }
                }
            ) {
                Text("Aceptar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    ) {
        DatePicker(state = state)
    }
}

@Composable
private fun TaskendaBottomBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    val view = LocalView.current
    val colors = MaterialTheme.colorScheme
    val barColor by animateColorAsState(
        targetValue = colors.surfaceContainerHigh,
        animationSpec = tween(220, easing = FastOutSlowInEasing),
        label = "taskenda_bar_color"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(start = 12.dp, end = 12.dp, bottom = 8.dp),
        shape = RoundedCornerShape(28.dp),
        tonalElevation = 3.dp,
        shadowElevation = 5.dp,
        color = barColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .padding(horizontal = 5.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val destinations = listOf(
                Triple("Hoy", Icons.Outlined.Event, Icons.Default.CalendarToday),
                Triple("Tareas", Icons.Outlined.CheckCircle, Icons.Default.TaskAlt),
                Triple("Importantes", Icons.Outlined.StarBorder, Icons.Default.Star),
                Triple("Completadas", Icons.Outlined.CheckCircle, Icons.Default.CheckCircle)
            )
            destinations.forEachIndexed { index, destination ->
                TaskendaBottomItem(
                    index = index,
                    selectedTab = selectedTab,
                    label = destination.first,
                    icon = destination.second,
                    selectedIcon = destination.third,
                    onClick = {
                        if (selectedTab != index) {
                            view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                            onTabSelected(index)
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun TaskendaBottomItem(
    index: Int,
    selectedTab: Int,
    label: String,
    icon: ImageVector,
    selectedIcon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val selected = selectedTab == index
    val shape = RoundedCornerShape(22.dp)
    val colors = MaterialTheme.colorScheme
    val backgroundColor by animateColorAsState(
        targetValue = if (selected) colors.primaryContainer else Color.Transparent,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "taskenda_nav_background_$index"
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) colors.onPrimaryContainer else colors.onSurfaceVariant,
        animationSpec = tween(180, easing = FastOutSlowInEasing),
        label = "taskenda_nav_content_$index"
    )
    val borderColor by animateColorAsState(
        targetValue = if (selected) colors.primary.copy(alpha = 0.72f) else Color.Transparent,
        animationSpec = tween(180, easing = FastOutSlowInEasing),
        label = "taskenda_nav_border_$index"
    )
    Column(
        modifier = modifier
            .height(60.dp)
            .padding(horizontal = 2.dp)
            .clip(shape)
            .background(backgroundColor, shape)
            .border(
                width = if (selected) 1.25.dp else 0.dp,
                color = borderColor,
                shape = shape
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 2.dp, vertical = 3.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Crossfade(
            targetState = selected,
            animationSpec = tween(150, easing = FastOutSlowInEasing),
            label = "taskenda_nav_icon_transition_$index"
        ) { isSelected ->
            Icon(
                imageVector = if (isSelected) selectedIcon else icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(22.dp)
            )
        }
        AnimatedVisibility(
            visible = selected,
            enter = fadeIn(tween(130)) + expandVertically(
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                expandFrom = Alignment.Top
            ),
            exit = fadeOut(tween(90)) + shrinkVertically(
                animationSpec = tween(100, easing = FastOutSlowInEasing),
                shrinkTowards = Alignment.Top
            )
        ) {
            Text(
                text = label,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Clip,
                color = contentColor,
                fontSize = 10.sp,
                lineHeight = 12.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 1.dp)
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

private fun formatReminder(millis: Long): String {
    return SimpleDateFormat(
        "d MMM, HH:mm",
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
