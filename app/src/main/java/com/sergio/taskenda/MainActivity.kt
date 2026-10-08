package com.sergio.taskenda

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.List
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.Room
import com.sergio.taskenda.data.Task
import com.sergio.taskenda.data.TaskDatabase
import com.sergio.taskenda.data.TaskRepository
import com.sergio.taskenda.data.TaskListStore
import com.sergio.taskenda.ui.TaskViewModel
import com.sergio.taskenda.ui.TaskViewModelFactory
import java.text.SimpleDateFormat
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
            .addMigrations(TaskDatabase.MIGRATION_1_2)
            .fallbackToDestructiveMigration()
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
                val vm: TaskViewModel = viewModel(
                    factory = TaskViewModelFactory(repository)
                )

                TaskendaApp(vm)
            }
        }
    }
}

@Composable
private fun TaskendaApp(vm: TaskViewModel) {

    val view = LocalView.current
    val tasks by vm.tasks.collectAsState()

    var selectedTab by remember {
        mutableIntStateOf(0)
    }

    var editorVisible by remember {
        mutableStateOf(false)
    }

    var editingTask by remember {
        mutableStateOf<Task?>(null)
    }

    val context = LocalContext.current
    val listStore = remember { TaskListStore(context.applicationContext) }
    var lists by remember { mutableStateOf(listStore.getLists()) }
    var selectedList by remember { mutableStateOf(TaskListStore.DEFAULT_LIST) }
    var listSheetVisible by remember { mutableStateOf(false) }

    // Calculamos cada grupo una sola vez cuando cambia la lista de tareas.
    // Así evitamos recorrer toda la base de datos varias veces durante una creación.
    val listTasks = remember(tasks, selectedList) {
        tasks.filter { it.listName == selectedList }
    }
    val pendingTasks = remember(listTasks) {
        listTasks.filter { !it.completed }
    }
    val completedTasks = remember(listTasks) {
        listTasks.filter { it.completed }
    }
    val importantTasks = remember(listTasks) {
        listTasks.filter { !it.completed && it.important }
    }

    val visibleTasks = when (selectedTab) {
        0, 1 -> pendingTasks
        2 -> importantTasks
        3 -> completedTasks
        else -> emptyList()
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
        0, 1 -> pendingTasks.size
        2 -> importantTasks.size
        3 -> completedTasks.size
        else -> 0
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    performHaptic(view)
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
                    durationMillis = 220,
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
                        0, 1 -> pendingTasks.size
                        2 -> importantTasks.size
                        3 -> completedTasks.size
                        else -> 0
                    },
                    selectedList = selectedList,
                    onListClick = {
                        performHaptic(view)
                        listSheetVisible = true
                    }
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

                    val tabTasks = when (tab) {
                        0, 1 -> pendingTasks
                        2 -> importantTasks
                        3 -> completedTasks
                        else -> emptyList()
                    }

                    if (tabTasks.isEmpty()) {
                        EmptyTasks(
                            selectedTab = tab,
                            onCreate = {
                                performHaptic(view)
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

    if (editorVisible) {
        TaskEditorSheet(
            task = editingTask,
            lists = lists,
            onManageLists = {
                listSheetVisible = true
            },
            onDismiss = {
                editorVisible = false
                editingTask = null
            },
            onSave = { updatedTask ->
                if (editingTask == null) {
                    vm.add(
                        title = updatedTask.title,
                        notes = updatedTask.notes,
                        important = updatedTask.important,
                        dueDate = updatedTask.dueDate,
                        listName = updatedTask.listName
                    )
                } else {
                    vm.update(updatedTask)
                }

                editorVisible = false
                editingTask = null
            }
        )
    }

    if (listSheetVisible) {
        TaskListSheet(
            lists = lists,
            selectedList = selectedList,
            onSelect = { name ->
                performHaptic(view)
                selectedList = name
                listSheetVisible = false
            },
            onCreate = { name ->
                if (listStore.addList(name)) {
                    lists = listStore.getLists()
                }
            },
            onRename = { oldName, newName ->
                if (oldName != TaskListStore.DEFAULT_LIST && newName.isNotBlank() &&
                    lists.none { it.equals(newName.trim(), ignoreCase = true) }) {
                    vm.renameList(oldName, newName.trim())
                    if (listStore.removeList(oldName)) {
                        listStore.addList(newName.trim())
                        lists = listStore.getLists()
                        if (selectedList == oldName) selectedList = newName.trim()
                    }
                }
            },
            onDelete = { name ->
                if (name != TaskListStore.DEFAULT_LIST) {
                    vm.moveTasksToList(name, TaskListStore.DEFAULT_LIST)
                    listStore.removeList(name)
                    lists = listStore.getLists()
                    if (selectedList == name) selectedList = TaskListStore.DEFAULT_LIST
                }
            },
            onDismiss = { listSheetVisible = false }
        )
    }
}

@Composable
private fun TaskendaHeader(
    title: String,
    subtitle: String,
    taskCount: Int,
    selectedList: String,
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

            Spacer(Modifier.height(10.dp))

            Surface(
                onClick = onListClick,
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.List,
                        contentDescription = null,
                        modifier = Modifier.size(17.dp)
                    )
                    Spacer(Modifier.width(7.dp))
                    Text(
                        text = selectedList,
                        style = MaterialTheme.typography.labelLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.width(2.dp))
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Cambiar lista",
                        modifier = Modifier.size(18.dp)
                    )
                }
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
    onToggleComplete: () -> Unit,
    onToggleImportant: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {

    val view = LocalView.current
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
                    performHaptic(view)
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

            IconButton(
                onClick = {
                    performHaptic(view)
                    onToggleImportant()
                }
            ) {
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
                IconButton(
                    onClick = {
                        performHaptic(view)
                        menuExpanded = true
                    }
                ) {
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
                            performHaptic(view)
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
                            performHaptic(view)
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
            modifier = Modifier.size(28.dp),
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
                            modifier = Modifier.size(19.dp),
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
    onManageLists: () -> Unit,
    onDismiss: () -> Unit,
    onSave: (Task) -> Unit
) {

    val view = LocalView.current

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

    var listName by remember(task?.id) {
        mutableStateOf(task?.listName ?: TaskListStore.DEFAULT_LIST)
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

            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = {
                        performHaptic(view)
                        listMenuExpanded = true
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.List, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = listName,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
                }

                androidx.compose.material3.DropdownMenu(
                    expanded = listMenuExpanded,
                    onDismissRequest = { listMenuExpanded = false }
                ) {
                    lists.forEach { name ->
                        androidx.compose.material3.DropdownMenuItem(
                            text = { Text(name, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                            onClick = {
                                performHaptic(view)
                                listName = name
                                listMenuExpanded = false
                            }
                        )
                    }

                    androidx.compose.material3.HorizontalDivider()

                    androidx.compose.material3.DropdownMenuItem(
                        text = { Text("Gestionar listas") },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                        onClick = {
                            performHaptic(view)
                            listMenuExpanded = false
                            onManageLists()
                        }
                    )
                }
            }

            OutlinedButton(
                onClick = {
                    performHaptic(view)
                    datePickerVisible = true
                },
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
                    onClick = {
                        performHaptic(view)
                        dueDate = null
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Quitar fecha")
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
                    onCheckedChange = {
                        performHaptic(view)
                        important = it
                    }
                )
            }

            Button(
                onClick = {
                    performHaptic(view)
                    val cleanTitle = title.trim()
                    if (cleanTitle.isBlank()) return@Button

                    onSave(
                        task?.copy(
                            title = cleanTitle,
                            notes = notes.trim(),
                            important = important,
                            dueDate = dueDate,
                            listName = listName
                        ) ?: Task(
                            title = cleanTitle,
                            notes = notes.trim(),
                            important = important,
                            dueDate = dueDate,
                            listName = listName
                        )
                    )
                },
                enabled = title.trim().isNotBlank(),
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
private fun TaskListSheet(
    lists: List<String>,
    selectedList: String,
    onSelect: (String) -> Unit,
    onCreate: (String) -> Unit,
    onRename: (String, String) -> Unit,
    onDelete: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val view = LocalView.current
    var newListName by remember { mutableStateOf("") }
    var editingName by remember { mutableStateOf<String?>(null) }
    var editingValue by remember { mutableStateOf("") }
    var confirmDelete by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Mis listas",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Organiza tus tareas por proyectos, trabajo, casa o lo que quieras.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            lists.forEach { name ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(18.dp))
                            .then(Modifier),
                        shape = RoundedCornerShape(18.dp),
                        color = if (name == selectedList) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceContainerHigh
                        },
                        onClick = {
                            performHaptic(view)
                            onSelect(name)
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 13.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.List, contentDescription = null)
                            Spacer(Modifier.width(12.dp))
                            Text(
                                text = name,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (name == selectedList) FontWeight.SemiBold else FontWeight.Normal,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(Modifier.weight(1f))
                            if (name == selectedList) {
                                Icon(Icons.Default.Check, contentDescription = "Seleccionada")
                            }
                        }
                    }

                    if (name != TaskListStore.DEFAULT_LIST) {
                        IconButton(
                            onClick = {
                                performHaptic(view)
                                editingName = name
                                editingValue = name
                            }
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Renombrar lista")
                        }
                        IconButton(
                            onClick = {
                                performHaptic(view)
                                confirmDelete = name
                            }
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar lista")
                        }
                    }
                }
            }

            OutlinedTextField(
                value = newListName,
                onValueChange = { newListName = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Nueva lista") },
                placeholder = { Text("Ej. Trabajo") },
                singleLine = true,
                trailingIcon = {
                    IconButton(
                        enabled = newListName.trim().isNotBlank(),
                        onClick = {
                            performHaptic(view)
                            onCreate(newListName)
                            newListName = ""
                        }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Crear lista")
                    }
                }
            )

            Spacer(Modifier.height(4.dp))
        }
    }

    editingName?.let { oldName ->
        AlertDialog(
            onDismissRequest = { editingName = null },
            title = { Text("Renombrar lista") },
            text = {
                OutlinedTextField(
                    value = editingValue,
                    onValueChange = { editingValue = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    enabled = editingValue.trim().isNotBlank(),
                    onClick = {
                        performHaptic(view)
                        onRename(oldName, editingValue)
                        editingName = null
                    }
                ) { Text("Guardar") }
            },
            dismissButton = {
                TextButton(onClick = { editingName = null }) { Text("Cancelar") }
            }
        )
    }

    confirmDelete?.let { name ->
        AlertDialog(
            onDismissRequest = { confirmDelete = null },
            title = { Text("Eliminar lista") },
            text = { Text("Las tareas de «$name» pasarán a «${TaskListStore.DEFAULT_LIST}». No se eliminarán.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        performHaptic(view)
                        onDelete(name)
                        confirmDelete = null
                    }
                ) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = null }) { Text("Cancelar") }
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

    val view = LocalView.current
    val state = rememberDatePickerState(
        initialSelectedDateMillis = initialDate
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    performHaptic(view)
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
            TextButton(
                onClick = {
                    performHaptic(view)
                    onDismiss()
                }
            ) {
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

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(
                start = 10.dp,
                end = 10.dp,
                bottom = 8.dp
            ),
        shape = RoundedCornerShape(28.dp),
        tonalElevation = 4.dp,
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
                onClick = { performHaptic(view); onTabSelected(0) },
                modifier = Modifier.weight(1f),
                icon = {
                    Icon(
                        imageVector = if (selectedTab == 0) {
                            Icons.Default.Event
                        } else {
                            Icons.Outlined.Event
                        },
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
                onClick = { performHaptic(view); onTabSelected(1) },
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
                onClick = { performHaptic(view); onTabSelected(2) },
                modifier = Modifier.weight(1f),
                icon = {
                    Icon(
                        imageVector = if (selectedTab == 2) {
                            Icons.Default.Star
                        } else {
                            Icons.Outlined.StarBorder
                        },
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
                onClick = { performHaptic(view); onTabSelected(3) },
                modifier = Modifier.weight(1f),
                icon = {
                    Icon(
                        imageVector = if (selectedTab == 3) {
                            Icons.Default.CheckCircle
                        } else {
                            Icons.Outlined.CheckCircle
                        },
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

private fun formatDate(millis: Long): String {
    return SimpleDateFormat(
        "d MMM",
        Locale("es", "ES")
    ).format(Date(millis))
}

private fun performHaptic(view: android.view.View) {
    view.performHapticFeedback(
        android.view.HapticFeedbackConstants.CONTEXT_CLICK
    )
}

