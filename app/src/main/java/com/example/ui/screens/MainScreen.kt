package com.example.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.Task
import com.example.ui.*
import com.example.ui.components.*
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: TaskViewModel,
    modifier: Modifier = Modifier
) {
    val tasks by viewModel.filteredTasks.collectAsStateWithLifecycle()
    val stats by viewModel.taskStats.collectAsStateWithLifecycle()
    val statusFilter by viewModel.statusFilter.collectAsStateWithLifecycle()
    val priorityFilter by viewModel.priorityFilter.collectAsStateWithLifecycle()
    val dateFilter by viewModel.dateFilter.collectAsStateWithLifecycle()
    val recentDates by viewModel.recentDates.collectAsStateWithLifecycle()
    val sortOption by viewModel.sortOption.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    val itemDateFormat = remember { SimpleDateFormat("MMM d, yyyy 'at' HH:mm", Locale.getDefault()) }

    var showAddEditSheet by remember { mutableStateOf(false) }
    var selectedTaskForEdit by remember { mutableStateOf<Task?>(null) }

    val onToggleComplete: (Task) -> Unit = remember { { t -> viewModel.toggleTaskCompletion(t) } }
    val onDelete: (Task) -> Unit = remember { { t -> viewModel.deleteTask(t) } }
    val onEdit: (Task) -> Unit = remember { { t ->
        selectedTaskForEdit = t
        showAddEditSheet = true
    } }

    val context = LocalContext.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .imePadding()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp)
        ) {
            item {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val isDark = isSystemInDarkTheme()
                    val todayString = remember {
                        SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(Date())
                    }
                    Column {
                        Text(
                            text = "Today",
                            fontSize = 36.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.SansSerif,
                            color = if (isDark) CharcoalWalnutDark else NaturalHeading,
                            letterSpacing = (-1).sp,
                            modifier = Modifier.testTag("app_title")
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = todayString.uppercase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.SansSerif,
                            color = if (isDark) CozyDarkPrimary else NaturalMuted,
                            letterSpacing = 2.sp
                        )
                    }

                    // Sorting dropdown
                    var showSortMenu by remember { mutableStateOf(false) }
                    Box {
                        IconButton(
                            onClick = { showSortMenu = true },
                            modifier = Modifier
                                .background(Color.Transparent)
                                .size(40.dp)
                                .testTag("sort_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "Sort tasks",
                                tint = if (isDark) CozyDarkPrimary else NaturalMuted,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false },
                            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                        ) {
                            DropdownMenuItem(
                                text = { Text("By priority (High first)") },
                                onClick = {
                                    viewModel.setSortOption(TaskSortOption.PRIORITY_DESC)
                                    showSortMenu = false
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Star,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("By nearest due date") },
                                onClick = {
                                    viewModel.setSortOption(TaskSortOption.DUE_DATE_ASC)
                                    showSortMenu = false
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.DateRange,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("By newest created") },
                                onClick = {
                                    viewModel.setSortOption(TaskSortOption.CREATION_DESC)
                                    showSortMenu = false
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Info,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            )
                        }
                    }
                }

                // Stats Card
                CozyStatsCard(stats = stats)

                Spacer(modifier = Modifier.height(16.dp))

                // Search Bar
                CozySearchBar(
                    query = searchQuery,
                    onQueryChange = { viewModel.setSearchQuery(it) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Filters selector
                CozyFilterRow(
                    currentStatus = statusFilter,
                    onStatusChange = { viewModel.setStatusFilter(it) },
                    currentPriority = priorityFilter,
                    onPriorityChange = { viewModel.setPriorityFilter(it) },
                    currentDateFilter = dateFilter,
                    onDateFilterChange = { viewModel.setDateFilter(it) },
                    recentDates = recentDates,
                    onRecentDateAdded = { viewModel.addRecentDate(it) }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Task List Header / Summary
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${tasks.size} tasks shown",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = FontFamily.SansSerif,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )

                    if (stats.completed > 0) {
                        Text(
                            text = "keep on keeping on",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal,
                            fontFamily = FontFamily.Serif,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.animateContentSize(animationSpec = tween(150))
                        )
                    }
                }
            }

            // List of Tasks
            if (tasks.isEmpty()) {
                item {
                    CozyEmptyState(
                        isFiltered = searchQuery.isNotEmpty() || priorityFilter != TaskFilterPriority.ALL || statusFilter != TaskFilterStatus.ALL || dateFilter != null
                    )
                }
            } else {
                items(tasks, key = { it.id }) { task ->
                    TaskItemCard(
                        task = task,
                        modifier = Modifier.animateItem(
                            fadeInSpec = tween(150),
                            fadeOutSpec = tween(150),
                            placementSpec = spring(stiffness = Spring.StiffnessHigh)
                        ),
                        onToggleComplete = onToggleComplete,
                        onDelete = onDelete,
                        onEdit = onEdit,
                        dateFormat = itemDateFormat
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }

        // Elegant Bottom Rounded Floating Action Button
        FloatingActionButton(
            onClick = {
                selectedTaskForEdit = null
                showAddEditSheet = true
            },
            containerColor = SimpleThemePrimary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 24.dp, end = 24.dp)
                .size(60.dp)
                .testTag("add_task_fab"),
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = 2.dp,
                pressedElevation = 6.dp
            )
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add new task",
                modifier = Modifier.size(28.dp)
            )
        }

        // Add/Edit Bottom Sheet Dialog
        if (showAddEditSheet) {
            ModalBottomSheet(
                onDismissRequest = {
                    showAddEditSheet = false
                    selectedTaskForEdit = null
                },
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp,
                dragHandle = {
                    Box(
                        modifier = Modifier
                            .padding(vertical = 12.dp)
                            .width(36.dp)
                            .height(4.dp)
                            .background(
                                MaterialTheme.colorScheme.onBackground.copy(alpha = 0.15f),
                                CircleShape
                            )
                    )
                }
            ) {
                AddEditTaskContent(
                    taskToEdit = selectedTaskForEdit,
                    onSave = { title, notes, priority, dueDate ->
                        if (selectedTaskForEdit != null) {
                            viewModel.updateTask(
                                selectedTaskForEdit!!.copy(
                                    title = title,
                                    notes = notes,
                                    priority = priority,
                                    dueDate = dueDate
                                )
                            )
                        } else {
                            viewModel.saveTask(title, notes, priority, dueDate)
                        }
                        showAddEditSheet = false
                        selectedTaskForEdit = null
                    },
                    onCancel = {
                        showAddEditSheet = false
                        selectedTaskForEdit = null
                    }
                )
            }
        }
    }
}

// Inline getter for primary color to avoid unresolved theme reference inside non-theme scopes
private val SimpleThemePrimary: Color
    @Composable
    get() = MaterialTheme.colorScheme.primary
