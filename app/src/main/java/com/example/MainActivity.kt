package com.example

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.DatePicker
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.Task
import com.example.data.TaskPriority
import com.example.ui.*
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    private val viewModel: TaskViewModel by viewModels {
        val app = application as SereneTasksApplication
        val prefs = app.getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
        TaskViewModelFactory(app.repository, prefs)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    contentWindowInsets = WindowInsets.systemBars
                ) { innerPadding ->
                    MainScreen(
                        viewModel = viewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

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

    val itemDateFormat = remember { SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.getDefault()) }

    var showAddEditSheet by remember { mutableStateOf(false) }
    var selectedTaskForEdit by remember { mutableStateOf<Task?>(null) }

    val context = LocalContext.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .imePadding(),
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
                            modifier = Modifier.animateContentSize()
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
                        onToggleComplete = { viewModel.toggleTaskCompletion(task) },
                        onDelete = { viewModel.deleteTask(task) },
                        onEdit = {
                            selectedTaskForEdit = task
                            showAddEditSheet = true
                        },
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

@Composable
fun CozyStatsCard(stats: TaskStats) {
    val progress = if (stats.total > 0) stats.completed.toFloat() / stats.total.toFloat() else 0f
    val isDark = isSystemInDarkTheme()
    
    val bgCol = if (isDark) CozyDarkSurface else NaturalHighBg
    val borderCol = if (isDark) Color.Transparent else MossBorder.copy(alpha = 0.5f)
    val textCol = if (isDark) CharcoalWalnutDark else NaturalHeading
    val subTextCol = if (isDark) CharcoalWalnutDark.copy(alpha = 0.6f) else MossGreen.copy(alpha = 0.8f)
    val progressTrackCol = if (isDark) Color.White.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.5f)
    val progressFillCol = if (isDark) CozyDarkPrimary else MossGreen
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .border(
                width = if (isDark) 0.dp else 1.dp,
                color = borderCol,
                shape = RoundedCornerShape(28.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = bgCol
        ),
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${stats.completed} of ${stats.total} completed",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Serif,
                    color = textCol
                )
                Text(
                    text = if (stats.total == 0) "begin your journey" else if (progress == 1f) "all tasks completed, enjoy the calm" else "${stats.active} tasks waiting peacefully",
                    fontSize = 13.sp,
                    fontFamily = FontFamily.SansSerif,
                    color = subTextCol,
                    modifier = Modifier.padding(top = 2.dp)
                )
                
                Spacer(modifier = Modifier.height(14.dp))
                
                // Custom Cozy Progress Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(CircleShape)
                        .background(progressTrackCol)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(progress)
                            .clip(CircleShape)
                            .background(progressFillCol)
                    )
                }
            }
            
            // Circular progress number or design accent
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .padding(start = 16.dp)
                    .size(64.dp)
                    .background(progressFillCol.copy(alpha = 0.12f), CircleShape)
            ) {
                Text(
                    text = "${(progress * 100).toInt()}%",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif,
                    color = progressFillCol
                )
            }
        }
    }
}

@Composable
fun CozySearchBar(
    query: String,
    onQueryChange: (String) -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val bgCol = if (isDark) CozyDarkSurface else Color.White
    val borderCol = if (isDark) Color.White.copy(alpha = 0.05f) else NaturalHighBg
    val textCol = if (isDark) CharcoalWalnutDark else NaturalText
    val iconCol = if (isDark) CozyDarkPrimary else NaturalMuted

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .background(bgCol, RoundedCornerShape(24.dp))
            .border(
                width = 1.dp,
                color = borderCol,
                shape = RoundedCornerShape(24.dp)
            )
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            tint = iconCol,
            modifier = Modifier.size(20.dp)
        )
        
        Spacer(modifier = Modifier.width(10.dp))
        
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.CenterStart
        ) {
            if (query.isEmpty()) {
                Text(
                    text = "Search tasks...",
                    color = textCol.copy(alpha = 0.4f),
                    fontSize = 15.sp,
                    fontFamily = FontFamily.SansSerif
                )
            }
            
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                textStyle = TextStyle(
                    color = textCol,
                    fontSize = 15.sp,
                    fontFamily = FontFamily.SansSerif
                ),
                cursorBrush = SolidColor(if (isDark) CozyDarkPrimary else TerracottaClay),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_input")
            )
        }
        
        if (query.isNotEmpty()) {
            IconButton(
                onClick = { onQueryChange("") },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = "Clear search",
                    tint = textCol.copy(alpha = 0.4f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun CozyFilterRow(
    currentStatus: TaskFilterStatus,
    onStatusChange: (TaskFilterStatus) -> Unit,
    currentPriority: TaskFilterPriority,
    onPriorityChange: (TaskFilterPriority) -> Unit,
    currentDateFilter: Long?,
    onDateFilterChange: (Long?) -> Unit,
    recentDates: List<Long>,
    onRecentDateAdded: (Long) -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val context = LocalContext.current
    val filterDateFormat = remember { SimpleDateFormat("MMM d", Locale.getDefault()) }
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Status filter buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TaskFilterStatus.values().forEach { status ->
                val isSelected = currentStatus == status
                val label = when (status) {
                    TaskFilterStatus.ALL -> "all"
                    TaskFilterStatus.ACTIVE -> "active"
                    TaskFilterStatus.COMPLETED -> "completed"
                }
                
                val bgCol = if (isSelected) {
                    if (isDark) CozyDarkPrimary else TerracottaClay
                } else {
                    if (isDark) CozyDarkSurface else Color.White
                }
                
                val textCol = if (isSelected) {
                    if (isDark) CozyDarkSurface else Color.White
                } else {
                    if (isDark) CharcoalWalnutDark.copy(alpha = 0.7f) else NaturalMuted
                }
                
                val borderCol = if (isSelected) {
                    Color.Transparent
                } else {
                    if (isDark) Color.White.copy(alpha = 0.05f) else NaturalHighBg
                }
                
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(bgCol)
                        .border(
                            width = 1.dp,
                            color = borderCol,
                            shape = RoundedCornerShape(14.dp)
                        )
                        .clickable { onStatusChange(status) }
                        .testTag("status_tab_${label}")
                ) {
                    Text(
                        text = label,
                        fontSize = 14.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        fontFamily = FontFamily.SansSerif,
                        color = textCol
                    )
                }
            }
        }
        
        // Priority
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Priority
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "priority:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = if (isDark) CharcoalWalnutDark.copy(alpha = 0.5f) else NaturalMuted,
                    modifier = Modifier.padding(end = 4.dp)
                )
                
                TaskFilterPriority.values().forEach { priority ->
                    val isSelected = currentPriority == priority
                    val label = when (priority) {
                        TaskFilterPriority.ALL -> "all"
                        TaskFilterPriority.LOW -> "low"
                        TaskFilterPriority.MEDIUM -> "medium"
                        TaskFilterPriority.HIGH -> "high"
                    }
                    
                    val labelColor = when (priority) {
                        TaskFilterPriority.ALL -> if (isDark) CharcoalWalnutDark else NaturalHeading
                        TaskFilterPriority.LOW -> MossGreen
                        TaskFilterPriority.MEDIUM -> TerracottaClay
                        TaskFilterPriority.HIGH -> MossGreen
                    }
                    
                    val textCol = if (isSelected) {
                        if (priority == TaskFilterPriority.ALL) {
                            if (isDark) CozyDarkSurface else Color.White
                        } else {
                            labelColor
                        }
                    } else {
                        labelColor.copy(alpha = 0.6f)
                    }
                    
                    val bgCol = if (isSelected) {
                        if (priority == TaskFilterPriority.ALL) {
                            if (isDark) CozyDarkPrimary else TerracottaClay
                        } else {
                            labelColor.copy(alpha = 0.15f)
                        }
                    } else {
                        Color.Transparent
                    }
                    
                    val borderCol = if (isSelected) {
                        if (priority == TaskFilterPriority.ALL) Color.Transparent else labelColor.copy(alpha = 0.5f)
                    } else {
                        if (isDark) Color.White.copy(alpha = 0.05f) else NaturalHighBg
                    }
     
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .height(28.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(bgCol)
                            .border(
                                width = 1.dp,
                                color = borderCol,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { onPriorityChange(priority) }
                            .padding(horizontal = 10.dp)
                    ) {
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            fontFamily = FontFamily.SansSerif,
                            color = textCol
                        )
                    }
                }
            }
        }

        // Date Filter
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "day:",
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                color = if (isDark) CharcoalWalnutDark.copy(alpha = 0.5f) else NaturalMuted,
                modifier = Modifier.padding(end = 4.dp)
            )

            // Any Day
            val isAnyDay = currentDateFilter == null
            val anyDayBgCol = if (isAnyDay) {
                if (isDark) CozyDarkPrimary else TerracottaClay
            } else Color.Transparent

            val anyDayTextCol = if (isAnyDay) {
                if (isDark) CozyDarkSurface else Color.White
            } else {
                if (isDark) CharcoalWalnutDark.copy(alpha = 0.6f) else NaturalMuted
            }
            
            val anyDayBorderCol = if (isAnyDay) Color.Transparent else {
                if (isDark) Color.White.copy(alpha = 0.05f) else NaturalHighBg
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .height(28.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(anyDayBgCol)
                    .border(width = 1.dp, color = anyDayBorderCol, shape = RoundedCornerShape(10.dp))
                    .clickable { onDateFilterChange(null) }
                    .padding(horizontal = 10.dp)
            ) {
                Text(
                    text = "all",
                    fontSize = 12.sp,
                    fontWeight = if (isAnyDay) FontWeight.SemiBold else FontWeight.Normal,
                    fontFamily = FontFamily.SansSerif,
                    color = anyDayTextCol
                )
            }

            recentDates.forEach { recentDateMillis ->
                val isSelected = currentDateFilter != null && Calendar.getInstance().apply { timeInMillis = recentDateMillis }.get(Calendar.DAY_OF_YEAR) == 
                                 Calendar.getInstance().apply { timeInMillis = currentDateFilter }.get(Calendar.DAY_OF_YEAR) &&
                                 Calendar.getInstance().apply { timeInMillis = recentDateMillis }.get(Calendar.YEAR) ==
                                 Calendar.getInstance().apply { timeInMillis = currentDateFilter }.get(Calendar.YEAR)

                val recentBgCol = if (isSelected) {
                    if (isDark) CozyDarkPrimary else TerracottaClay
                } else Color.Transparent

                val recentTextCol = if (isSelected) {
                    if (isDark) CozyDarkSurface else Color.White
                } else {
                    if (isDark) CharcoalWalnutDark.copy(alpha = 0.6f) else NaturalMuted
                }

                val recentBorderCol = if (isSelected) Color.Transparent else {
                    if (isDark) Color.White.copy(alpha = 0.05f) else NaturalHighBg
                }

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .height(28.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(recentBgCol)
                        .border(width = 1.dp, color = recentBorderCol, shape = RoundedCornerShape(10.dp))
                        .clickable { onDateFilterChange(recentDateMillis) }
                        .padding(horizontal = 10.dp)
                ) {
                    Text(
                        text = filterDateFormat.format(Date(recentDateMillis)),
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        fontFamily = FontFamily.SansSerif,
                        color = recentTextCol
                    )
                }
            }

            // Custom Date (Select button always unselected style)
            val customBgCol = Color.Transparent
            val customTextCol = if (isDark) CharcoalWalnutDark.copy(alpha = 0.6f) else NaturalMuted
            val customBorderCol = if (isDark) Color.White.copy(alpha = 0.05f) else NaturalHighBg

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .height(28.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(customBgCol)
                    .border(width = 1.dp, color = customBorderCol, shape = RoundedCornerShape(10.dp))
                    .clickable {
                        val calendar = Calendar.getInstance()
                        if (currentDateFilter != null) {
                            calendar.timeInMillis = currentDateFilter
                        }
                        android.app.DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                val selectedCal = Calendar.getInstance().apply {
                                    set(Calendar.YEAR, year)
                                    set(Calendar.MONTH, month)
                                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                                    set(Calendar.HOUR_OF_DAY, 0)
                                    set(Calendar.MINUTE, 0)
                                    set(Calendar.SECOND, 0)
                                    set(Calendar.MILLISECOND, 0)
                                }
                                val selectedTime = selectedCal.timeInMillis
                                onRecentDateAdded(selectedTime)
                                onDateFilterChange(selectedTime)
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    }
                    .padding(horizontal = 10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    tint = customTextCol,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "select",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    fontFamily = FontFamily.SansSerif,
                    color = customTextCol
                )
            }
        }
    }
}

@Composable
fun TaskItemCard(
    task: Task,
    onToggleComplete: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    dateFormat: SimpleDateFormat
) {
    val isDark = isSystemInDarkTheme()
    
    // Custom color mapping based on priority & state
    val containerColor = if (isDark) {
        if (task.isCompleted) {
            CozyDarkSurface.copy(alpha = 0.4f)
        } else {
            when (task.priority) {
                TaskPriority.HIGH -> CozyDarkSurface.copy(alpha = 0.95f)
                TaskPriority.MEDIUM -> CozyDarkSurface.copy(alpha = 0.8f)
                TaskPriority.LOW -> CozyDarkSurface.copy(alpha = 0.6f)
            }
        }
    } else {
        if (task.isCompleted) {
            Color(0xFFEBE5DF).copy(alpha = 0.5f)
        } else {
            when (task.priority) {
                TaskPriority.HIGH -> NaturalHighBg
                TaskPriority.MEDIUM -> NaturalMedBg
                TaskPriority.LOW -> NaturalLowBg
            }
        }
    }

    val borderCol = if (isDark) {
        if (task.isCompleted) Color.Transparent else {
            when (task.priority) {
                TaskPriority.HIGH -> CozyDarkSecondary.copy(alpha = 0.25f)
                TaskPriority.MEDIUM -> CozyDarkPrimary.copy(alpha = 0.2f)
                TaskPriority.LOW -> CozyDarkTertiary.copy(alpha = 0.1f)
            }
        }
    } else {
        if (task.isCompleted) Color.Transparent else {
            when (task.priority) {
                TaskPriority.HIGH -> MossBorder.copy(alpha = 0.5f)
                TaskPriority.MEDIUM -> ClayBorder
                TaskPriority.LOW -> NaturalHighBg
            }
        }
    }

    val headingColor = if (isDark) {
        CharcoalWalnutDark
    } else {
        if (task.priority == TaskPriority.LOW) NaturalHeading.copy(alpha = 0.8f) else NaturalHeading
    }

    val notesColor = if (isDark) {
        CharcoalWalnutDark.copy(alpha = 0.61f)
    } else {
        when (task.priority) {
            TaskPriority.HIGH -> MossGreen.copy(alpha = 0.72f)
            TaskPriority.MEDIUM -> NaturalMuted
            TaskPriority.LOW -> NaturalMuted
        }
    }

    val ringColor = if (isDark) {
        when (task.priority) {
            TaskPriority.HIGH -> CozyDarkSecondary
            TaskPriority.MEDIUM -> CozyDarkPrimary
            TaskPriority.LOW -> CozyDarkTertiary.copy(alpha = 0.4f)
        }
    } else {
        when (task.priority) {
            TaskPriority.HIGH -> MossGreen
            TaskPriority.MEDIUM -> TerracottaClay
            TaskPriority.LOW -> TerracottaClay.copy(alpha = 0.4f)
        }
    }

    val contentAlpha = if (task.isCompleted) 0.5f else 1.0f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("task_card_${task.id}")
            .border(
                width = 1.dp,
                color = borderCol,
                shape = RoundedCornerShape(24.dp)
            )
            .clickable { onEdit() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Elegant circular checkbox
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .padding(end = 14.dp)
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(
                        if (task.isCompleted) {
                            if (isDark) CozyDarkPrimary else MossGreen
                        } else Color.Transparent
                    )
                    .border(
                        width = 2.dp,
                        color = if (task.isCompleted) Color.Transparent else ringColor,
                        shape = CircleShape
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onToggleComplete
                    )
                    .testTag("task_checkbox_${task.id}")
            ) {
                if (task.isCompleted) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Completed",
                        tint = if (isDark) CozyDarkSurface else NaturalBackground,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            // Task texts
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 4.dp)
            ) {
                // Info badges / priority row
                Row(
                    modifier = Modifier.padding(bottom = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Badge Styling
                    val badgeBg = if (isDark) {
                        when (task.priority) {
                            TaskPriority.HIGH -> CozyDarkSecondary.copy(alpha = 0.2f)
                            TaskPriority.MEDIUM -> CozyDarkPrimary.copy(alpha = 0.2f)
                            TaskPriority.LOW -> CozyDarkTertiary.copy(alpha = 0.2f)
                        }
                    } else {
                        when (task.priority) {
                            TaskPriority.HIGH -> MossGreen
                            TaskPriority.MEDIUM -> TerracottaClay
                            TaskPriority.LOW -> NaturalHighBg
                        }
                    }

                    val badgeText = if (isDark) {
                        when (task.priority) {
                            TaskPriority.HIGH -> CozyDarkSecondary
                            TaskPriority.MEDIUM -> CozyDarkPrimary
                            TaskPriority.LOW -> CozyDarkTertiary
                        }
                    } else {
                        when (task.priority) {
                            TaskPriority.HIGH -> NaturalMedBg
                            TaskPriority.MEDIUM -> Color.White
                            TaskPriority.LOW -> MossGreen
                        }
                    }

                    Box(
                        modifier = Modifier
                            .background(badgeBg, RoundedCornerShape(100.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = task.priority.name.uppercase(),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp,
                            fontFamily = FontFamily.SansSerif,
                            color = badgeText.copy(alpha = contentAlpha)
                        )
                    }

                    // Due Date
                    if (task.dueDate != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = null,
                                tint = if (isDark) CozyDarkPrimary.copy(alpha = 0.5f) else NaturalMuted,
                                modifier = Modifier.size(11.dp)
                            )
                            Text(
                                text = dateFormat.format(Date(task.dueDate)),
                                fontSize = 11.sp,
                                fontFamily = FontFamily.SansSerif,
                                color = if (isDark) CharcoalWalnutDark.copy(alpha = 0.5f) else NaturalMuted,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Text(
                    text = task.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.SansSerif,
                    color = headingColor.copy(alpha = contentAlpha),
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                if (task.notes.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = task.notes,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal,
                        fontFamily = FontFamily.SansSerif,
                        color = notesColor.copy(alpha = contentAlpha),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Simple delete trigger
            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .size(34.dp)
                    .testTag("delete_task_button_${task.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete task",
                    tint = if (isDark) CharcoalWalnutDark.copy(alpha = 0.25f) else NaturalHeading.copy(alpha = 0.2f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun CozyEmptyState(isFiltered: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Icon
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(72.dp)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.06f), CircleShape)
        ) {
            Icon(
                imageVector = if (isFiltered) Icons.Default.Info else Icons.Default.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (isFiltered) "No matched tasks" else "your mind is clear",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = FontFamily.Serif,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = if (isFiltered) "Try adjusting filters or searching catalog" else "all is orderly and rest is earned",
            fontSize = 13.sp,
            fontFamily = FontFamily.SansSerif,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun AddEditTaskContent(
    taskToEdit: Task?,
    onSave: (title: String, notes: String, priority: TaskPriority, dueDate: Long?) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val dateFormat = remember { SimpleDateFormat("MMMM d, yyyy 'at' h:mm a", Locale.getDefault()) }
    val isDark = isSystemInDarkTheme()

    var title by remember { mutableStateOf(taskToEdit?.title ?: "") }
    var notes by remember { mutableStateOf(taskToEdit?.notes ?: "") }
    var priority by remember { mutableStateOf(taskToEdit?.priority ?: TaskPriority.MEDIUM) }
    var dueDate by remember { mutableStateOf<Long?>(taskToEdit?.dueDate) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = 24.dp)
            .padding(top = 4.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Form Title
        Text(
            text = if (taskToEdit != null) "edit task" else "new task",
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.SansSerif,
            color = if (isDark) CharcoalWalnutDark else NaturalHeading
        )

        // Title Input
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Task title", color = if (isDark) CharcoalWalnutDark.copy(alpha = 0.5f) else NaturalMuted) },
            placeholder = { Text("What needs to be done?", color = if (isDark) CharcoalWalnutDark.copy(alpha = 0.3f) else NaturalMuted.copy(alpha = 0.5f)) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("task_title_input"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = if (isDark) CharcoalWalnutDark else NaturalText,
                unfocusedTextColor = if (isDark) CharcoalWalnutDark else NaturalText,
                focusedBorderColor = if (isDark) CozyDarkPrimary else TerracottaClay,
                unfocusedBorderColor = if (isDark) Color.White.copy(alpha = 0.05f) else NaturalHighBg,
                focusedContainerColor = if (isDark) CozyDarkSurface else Color.White,
                unfocusedContainerColor = if (isDark) CozyDarkSurface else Color.White
            ),
            shape = RoundedCornerShape(16.dp)
        )

        // Notes Input
        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Description (optional)", color = if (isDark) CharcoalWalnutDark.copy(alpha = 0.5f) else NaturalMuted) },
            placeholder = { Text("Add clean details here...", color = if (isDark) CharcoalWalnutDark.copy(alpha = 0.3f) else NaturalMuted.copy(alpha = 0.5f)) },
            minLines = 2,
            maxLines = 4,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("task_notes_input"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = if (isDark) CharcoalWalnutDark else NaturalText,
                unfocusedTextColor = if (isDark) CharcoalWalnutDark else NaturalText,
                focusedBorderColor = if (isDark) CozyDarkPrimary else TerracottaClay,
                unfocusedBorderColor = if (isDark) Color.White.copy(alpha = 0.05f) else NaturalHighBg,
                focusedContainerColor = if (isDark) CozyDarkSurface else Color.White,
                unfocusedContainerColor = if (isDark) CozyDarkSurface else Color.White
            ),
            shape = RoundedCornerShape(16.dp)
        )

        // Priority Selection
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Priority Level",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isDark) CharcoalWalnutDark.copy(alpha = 0.6f) else NaturalMuted
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TaskPriority.values().forEach { level ->
                    val isSelected = priority == level
                    val color = when (level) {
                        TaskPriority.LOW -> MossGreen
                        TaskPriority.MEDIUM -> TerracottaClay
                        TaskPriority.HIGH -> MossGreen
                    }
                    
                    val bgCol = if (isSelected) color.copy(alpha = 0.15f) else Color.Transparent
                    val borderCol = if (isSelected) color else (if (isDark) Color.White.copy(alpha = 0.05f) else NaturalHighBg)
                    val textCol = if (isSelected) color else (if (isDark) CharcoalWalnutDark.copy(alpha = 0.6f) else NaturalMuted)

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(bgCol)
                            .border(width = 1.dp, color = borderCol, shape = RoundedCornerShape(12.dp))
                            .clickable { priority = level }
                            .testTag("priority_option_${level.name.lowercase()}")
                    ) {
                        Text(
                            text = level.name.lowercase(),
                            fontSize = 14.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontFamily = FontFamily.SansSerif,
                            color = textCol
                        )
                    }
                }
            }
        }

        // Due Date Picker Trigger
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .background(if (isDark) CozyDarkSurface else Color.White, RoundedCornerShape(16.dp))
                .border(
                    width = 1.6.dp,
                    color = if (isDark) Color.White.copy(alpha = 0.05f) else NaturalHighBg,
                    shape = RoundedCornerShape(16.dp)
                )
                .clickable {
                    val calendar = Calendar.getInstance()
                    if (dueDate != null) {
                        calendar.timeInMillis = dueDate!!
                    }
                    android.app.DatePickerDialog(
                        context,
                        { _, year, month, dayOfMonth ->
                            android.app.TimePickerDialog(
                                context,
                                { _, hourOfDay, minute ->
                                    val selectedCal = Calendar.getInstance()
                                    selectedCal.set(Calendar.YEAR, year)
                                    selectedCal.set(Calendar.MONTH, month)
                                    selectedCal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                                    selectedCal.set(Calendar.HOUR_OF_DAY, hourOfDay)
                                    selectedCal.set(Calendar.MINUTE, minute)
                                    selectedCal.set(Calendar.SECOND, 0)
                                    dueDate = selectedCal.timeInMillis
                                },
                                calendar.get(Calendar.HOUR_OF_DAY),
                                calendar.get(Calendar.MINUTE),
                                false
                            ).show()
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                    ).show()
                }
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    tint = if (isDark) CozyDarkPrimary else NaturalMuted,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = if (dueDate != null) dateFormat.format(Date(dueDate!!)) else "Set a due date",
                    fontSize = 14.sp,
                    fontFamily = FontFamily.SansSerif,
                    color = if (dueDate != null) {
                        if (isDark) CharcoalWalnutDark else NaturalText
                    } else {
                        if (isDark) CharcoalWalnutDark.copy(alpha = 0.4f) else NaturalMuted
                    }
                )
            }
            
            if (dueDate != null) {
                IconButton(
                    onClick = { dueDate = null },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear due date",
                        tint = if (isDark) CharcoalWalnutDark.copy(alpha = 0.4f) else NaturalMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Cancel
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                border = BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.1f) else NaturalHighBg),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = "Cancel",
                    color = if (isDark) CharcoalWalnutDark.copy(alpha = 0.8f) else NaturalMuted,
                    fontWeight = FontWeight.Medium
                )
            }

            // Save
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onSave(title, notes, priority, dueDate)
                    }
                },
                enabled = title.isNotBlank(),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("save_task_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDark) CozyDarkPrimary else TerracottaClay,
                    contentColor = if (isDark) CozyDarkSurface else Color.White
                )
            ) {
                Text(
                    text = if (taskToEdit != null) "Save" else "Create",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
