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
import androidx.compose.animation.togetherWith
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.FilterList
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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.key
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
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

            // Conserva los colores dinámicos del sistema, pero fija los colores
            // semánticos del tema oscuro para impedir texto oscuro sobre superficies oscuras.
            val colorScheme = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (darkTheme) {
                    dynamicDarkColorScheme(context).copy(
                        background = Color(0xFF160A07),
                        onBackground = Color(0xFFFFF7F3),
                        surface = Color(0xFF21110D),
                        onSurface = Color(0xFFFFF7F3),
                        surfaceVariant = Color(0xFF39221B),
                        onSurfaceVariant = Color(0xFFE5CFC6),
                        outline = Color(0xFFB99A8E),
                        outlineVariant = Color(0xFF6C4B40),
                        surfaceContainerLowest = Color(0xFF100604),
                        surfaceContainerLow = Color(0xFF1B0D09),
                        surfaceContainer = Color(0xFF24130E),
                        surfaceContainerHigh = Color(0xFF2D1913),
                        surfaceContainerHighest = Color(0xFF39221B)
                    )
                } else {
                    dynamicLightColorScheme(context)
                }
            } else {
                if (darkTheme) {
                    darkColorScheme(
                        primary = Color(0xFFFFB4A3),
                        onPrimary = Color(0xFF561F13),
                        primaryContainer = Color(0xFF733426),
                        onPrimaryContainer = Color(0xFFFFDAD1),
                        secondary = Color(0xFFE7BDB1),
                        onSecondary = Color(0xFF442A23),
                        background = Color(0xFF160A07),
                        onBackground = Color(0xFFFFF7F3),
                        surface = Color(0xFF21110D),
                        onSurface = Color(0xFFFFF7F3),
                        surfaceVariant = Color(0xFF39221B),
                        onSurfaceVariant = Color(0xFFE5CFC6),
                        outline = Color(0xFFB99A8E),
                        outlineVariant = Color(0xFF6C4B40)
                    )
                } else lightColorScheme()
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

    // Filtro de fecha para separar hoy, mañana y tareas vencidas.
    var selectedDateFilter by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var sortMenuExpanded by remember { mutableStateOf(false) }
    var filtersMenuExpanded by remember { mutableStateOf(false) }
    var selectedSort by remember { mutableStateOf("Fecha") }
    var onlyImportant by remember { mutableStateOf(false) }
    var onlyWithSubtasks by remember { mutableStateOf(false) }
    var onlyWithDate by remember { mutableStateOf(false) }

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
            listTasks.filter { !it.completed },
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

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        cursorColor = MaterialTheme.colorScheme.primary
                    ),
                    placeholder = { Text("Buscar tareas", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Buscar")
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Borrar búsqueda")
                            }
                        }
                    }
                )
                Box {
                    IconButton(onClick = { filtersMenuExpanded = true }) {
                        Icon(
                            Icons.Default.FilterList,
                            contentDescription = "Filtrar tareas",
                            tint = if (onlyImportant || onlyWithSubtasks || onlyWithDate)
                                MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    DropdownMenu(
                        expanded = filtersMenuExpanded,
                        onDismissRequest = { filtersMenuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(if (onlyImportant) "✓ Importantes" else "Importantes") },
                            onClick = { onlyImportant = !onlyImportant }
                        )
                        DropdownMenuItem(
                            text = { Text(if (onlyWithSubtasks) "✓ Con subtareas" else "Con subtareas") },
                            onClick = { onlyWithSubtasks = !onlyWithSubtasks }
                        )
                        DropdownMenuItem(
                            text = { Text(if (onlyWithDate) "✓ Con fecha" else "Con fecha") },
                            onClick = { onlyWithDate = !onlyWithDate }
                        )
                        DropdownMenuItem(
                            text = { Text("Quitar filtros") },
                            enabled = onlyImportant || onlyWithSubtasks || onlyWithDate,
                            onClick = {
                                onlyImportant = false
                                onlyWithSubtasks = false
                                onlyWithDate = false
                                filtersMenuExpanded = false
                            }
                        )
                    }
                }
                Box {
                    IconButton(onClick = { sortMenuExpanded = true }) {
                        Icon(Icons.Default.Sort, contentDescription = "Ordenar tareas")
                    }
                    DropdownMenu(
                        expanded = sortMenuExpanded,
                        onDismissRequest = { sortMenuExpanded = false }
                    ) {
                        listOf("Fecha", "Importancia", "Nombre", "Creación").forEach { option ->
                            DropdownMenuItem(
                                text = { Text(if (selectedSort == option) "✓ $option" else option) },
                                onClick = {
                                    selectedSort = option
                                    sortMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            if (selectedTab == 0) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(start = 16.dp, end = 16.dp, top = 2.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = selectedDateFilter == 0,
                        onClick = { selectedDateFilter = 0 },
                        label = { Text("Hoy") }
                    )
                    FilterChip(
                        selected = selectedDateFilter == 1,
                        onClick = { selectedDateFilter = 1 },
                        label = { Text("Mañana") }
                    )
                    FilterChip(
                        selected = selectedDateFilter == 2,
                        onClick = { selectedDateFilter = 2 },
                        label = { Text("Vencidas") }
                    )
                    FilterChip(
                        selected = selectedDateFilter == 3,
                        onClick = { selectedDateFilter = 3 },
                        label = { Text("Todas") }
                    )
                }
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

                    val categoryTasks = tabTasksByCategory.getOrElse(tab) { emptyList() }
                    val tabTasks = if (tab == 0) {
                        categoryTasks.filter { task ->
                            when (selectedDateFilter) {
                                0 -> isDueToday(task.dueDate)
                                1 -> isDueTomorrow(task.dueDate)
                                2 -> isOverdue(task.dueDate)
                                3 -> true
                                else -> false
                            }
                        }
                    } else {
                        categoryTasks
                    }

                    val displayedTasks = remember(
                        tabTasks,
                        searchQuery,
                        selectedSort,
                        onlyImportant,
                        onlyWithSubtasks,
                        onlyWithDate,
                        subtasksByTask
                    ) {
                        val query = searchQuery.trim()
                        val filtered = tabTasks.filter { task ->
                            (query.isEmpty() || task.title.contains(query, ignoreCase = true) ||
                                task.notes.contains(query, ignoreCase = true)) &&
                                (!onlyImportant || task.important) &&
                                (!onlyWithSubtasks || !subtasksByTask[task.id].isNullOrEmpty()) &&
                                (!onlyWithDate || task.dueDate != null)
                        }
                        when (selectedSort) {
                            "Importancia" -> filtered.sortedWith(
                                compareByDescending<Task> { it.important }
                                    .thenBy { it.dueDate ?: Long.MAX_VALUE }
                                    .thenBy { it.createdAt }
                            )
                            "Nombre" -> filtered.sortedBy { it.title.lowercase() }
                            "Creación" -> filtered.sortedByDescending { it.createdAt }
                            else -> filtered.sortedWith(
                                compareBy<Task> { it.dueDate ?: Long.MAX_VALUE }
                                    .thenBy { it.dueTime ?: "99:99" }
                                    .thenByDescending { it.important }
                            )
                        }
                    }

                    if (displayedTasks.isEmpty()) {
                        if (searchQuery.isNotBlank() || onlyImportant || onlyWithSubtasks || onlyWithDate) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(Modifier.height(8.dp))
                                Text("No hay resultados", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                                Text("Cambia la búsqueda o desactiva algún filtro.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        } else EmptyTasks(
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
                                items = displayedTasks,
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
            .pointerInput(task.id) {
                detectTapGestures(
                    onTap = { onEdit() },
                    onLongPress = { onToggleImportant() }
                )
            },
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
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        dragHandle = null,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxHeight(0.96f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
                .padding(
                    start = 14.dp,
                    end = 14.dp,
                    top = 10.dp,
                    bottom = 16.dp
                ),
            verticalArrangement = Arrangement.spacedBy(4.dp)
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
                minLines = 2,
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
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
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
                            key(subtask.id.takeIf { it != 0L } ?: index.toLong()) {
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

                                val subtaskTextColor by animateColorAsState(
                                    targetValue = if (subtask.completed) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioNoBouncy,
                                        stiffness = Spring.StiffnessMedium
                                    ),
                                    label = "subtaskTextColor"
                                )
                                Text(
                                    text = subtask.title,
                                    modifier = Modifier.weight(1f),
                                    style = MaterialTheme.typography.bodyMedium,
                                    textDecoration = if (subtask.completed) TextDecoration.LineThrough else TextDecoration.None,
                                    color = subtaskTextColor
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
            verticalArrangement = Arrangement.spacedBy(4.dp)
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
    val haptic = LocalHapticFeedback.current
    val colors = MaterialTheme.colorScheme
    val destinations = listOf(
        Triple("Hoy", Icons.Outlined.Event, Icons.Default.CalendarToday),
        Triple("Tareas", Icons.Outlined.CheckCircle, Icons.Default.TaskAlt),
        Triple("Importantes", Icons.Outlined.StarBorder, Icons.Default.Star),
        Triple("Completadas", Icons.Outlined.CheckCircle, Icons.Default.CheckCircle)
    )
    val safeSelectedTab = selectedTab.coerceIn(0, destinations.lastIndex)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(start = 12.dp, end = 12.dp, bottom = 8.dp),
        shape = RoundedCornerShape(28.dp),
        tonalElevation = 3.dp,
        shadowElevation = 5.dp,
        color = colors.surfaceContainerHigh
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .padding(horizontal = 5.dp, vertical = 4.dp)
        ) {
            val slotWidth = maxWidth / destinations.size
            val capsuleWidth = slotWidth - 4.dp

            // Igual que Diario: la cápsula se desplaza por detrás de cuatro
            // destinos estables. Solo se anima su traslación, no su tamaño.
            val indicatorX by animateDpAsState(
                targetValue = slotWidth * safeSelectedTab + 2.dp,
                animationSpec = tween(
                    durationMillis = 300,
                    easing = FastOutSlowInEasing
                ),
                label = "taskenda_bottom_nav_indicator"
            )

            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .offset(x = indicatorX)
                    .width(capsuleWidth)
                    .height(52.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(colors.primaryContainer)
            )

            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                destinations.forEachIndexed { index, destination ->
                    TaskendaNavItem(
                        modifier = Modifier.weight(1f),
                        selected = safeSelectedTab == index,
                        inactiveIcon = destination.second,
                        activeIcon = destination.third,
                        label = destination.first,
                        onClick = {
                            if (safeSelectedTab != index) {
                                haptic.performHapticFeedback(HapticFeedbackType.VirtualKey)
                                haptic.performHapticFeedback(HapticFeedbackType.ToggleOn)
                                onTabSelected(index)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun TaskendaNavItem(
    modifier: Modifier = Modifier,
    selected: Boolean,
    inactiveIcon: androidx.compose.ui.graphics.vector.ImageVector,
    activeIcon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme

    Box(
        modifier = modifier
            .fillMaxHeight()
            .padding(vertical = 4.dp, horizontal = 2.dp)
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        // Copia el comportamiento de Diario: el icono y la etiqueta cambian
        // dentro de cada espacio fijo mediante fade + scale, sincronizados
        // con la cápsula que viaja por detrás.
        AnimatedContent(
            targetState = selected,
            transitionSpec = {
                (fadeIn(animationSpec = tween(180)) +
                    androidx.compose.animation.scaleIn(
                        initialScale = 0.88f,
                        animationSpec = tween(220)
                    )) togetherWith
                    (fadeOut(animationSpec = tween(120)) +
                        androidx.compose.animation.scaleOut(
                            targetScale = 0.88f,
                            animationSpec = tween(160)
                        ))
            },
            label = "taskenda_bottom_nav_item"
        ) { isSelected ->
            if (isSelected) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = activeIcon,
                        contentDescription = label,
                        modifier = Modifier.size(24.dp),
                        tint = colors.onPrimaryContainer
                    )
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        lineHeight = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false,
                        color = colors.onPrimaryContainer
                    )
                }
            } else {
                Icon(
                    imageVector = inactiveIcon,
                    contentDescription = label,
                    modifier = Modifier.size(24.dp),
                    tint = colors.onSurfaceVariant
                )
            }
        }
    }
}

private fun isTodayOrOverdue(dueDate: Long?): Boolean {
    return isDueToday(dueDate) || isOverdue(dueDate)
}

private fun dayKey(millis: Long): String =
    SimpleDateFormat("yyyyMMdd", Locale.ROOT).format(Date(millis))

private fun isDueToday(dueDate: Long?): Boolean =
    dueDate != null && dayKey(dueDate) == dayKey(System.currentTimeMillis())

private fun isDueTomorrow(dueDate: Long?): Boolean {
    if (dueDate == null) return false
    val tomorrow = java.util.Calendar.getInstance().apply {
        add(java.util.Calendar.DAY_OF_YEAR, 1)
    }.timeInMillis
    return dayKey(dueDate) == dayKey(tomorrow)
}

private fun isOverdue(dueDate: Long?): Boolean =
    dueDate != null && dayKey(dueDate) < dayKey(System.currentTimeMillis())

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
