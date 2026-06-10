package com.example.ui

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.Task
import com.example.data.TaskPriority
import com.example.data.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class TaskFilterStatus {
    ALL, ACTIVE, COMPLETED
}

enum class TaskFilterPriority {
    ALL, LOW, MEDIUM, HIGH
}

enum class TaskSortOption {
    PRIORITY_DESC, DUE_DATE_ASC, CREATION_DESC
}

class TaskViewModel(
    private val repository: TaskRepository,
    private val prefs: SharedPreferences
) : ViewModel() {

    private val _statusFilter = MutableStateFlow(TaskFilterStatus.ALL)
    val statusFilter: StateFlow<TaskFilterStatus> = _statusFilter

    private val _priorityFilter = MutableStateFlow(TaskFilterPriority.ALL)
    val priorityFilter: StateFlow<TaskFilterPriority> = _priorityFilter

    private val _sortOption = MutableStateFlow(TaskSortOption.PRIORITY_DESC)
    val sortOption: StateFlow<TaskSortOption> = _sortOption

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _dateFilter = MutableStateFlow<Long?>(null)
    val dateFilter: StateFlow<Long?> = _dateFilter

    private val _recentDates = MutableStateFlow<List<Long>>(emptyList())
    val recentDates: StateFlow<List<Long>> = _recentDates

    init {
        val saved = prefs.getString("recent_dates", null)
        if (!saved.isNullOrBlank()) {
            _recentDates.value = saved.split(",").mapNotNull { it.toLongOrNull() }
        }
    }

    // Reactive COMBINED Flow of tasks based on filters, sorting, and search
    val filteredTasks: StateFlow<List<Task>> = combine(
        repository.allTasks,
        _statusFilter,
        _priorityFilter,
        _sortOption,
        _searchQuery,
        _dateFilter
    ) { args: Array<Any?> ->
        var result = args[0] as List<Task>
        val status = args[1] as TaskFilterStatus
        val priority = args[2] as TaskFilterPriority
        val sort = args[3] as TaskSortOption
        val search = args[4] as String
        val dateFilterMs = args[5] as Long?

        // 1. Status Filter
        result = when (status) {
            TaskFilterStatus.ALL -> result
            TaskFilterStatus.ACTIVE -> result.filter { !it.isCompleted }
            TaskFilterStatus.COMPLETED -> result.filter { it.isCompleted }
        }

        // 2. Priority Filter
        result = when (priority) {
            TaskFilterPriority.ALL -> result
            TaskFilterPriority.LOW -> result.filter { it.priority == TaskPriority.LOW }
            TaskFilterPriority.MEDIUM -> result.filter { it.priority == TaskPriority.MEDIUM }
            TaskFilterPriority.HIGH -> result.filter { it.priority == TaskPriority.HIGH }
        }

        // 3. Search Query
        if (search.isNotBlank()) {
            result = result.filter { 
                it.title.contains(search, ignoreCase = true) || 
                it.notes.contains(search, ignoreCase = true) 
            }
        }

        // 4. Date Filter
        if (dateFilterMs != null) {
            val filterCalendar = java.util.Calendar.getInstance().apply { timeInMillis = dateFilterMs }
            val filterYear = filterCalendar.get(java.util.Calendar.YEAR)
            val filterDayOfYear = filterCalendar.get(java.util.Calendar.DAY_OF_YEAR)

            result = result.filter { task ->
                val due = task.dueDate
                if (due != null) {
                    val taskCalendar = java.util.Calendar.getInstance().apply { timeInMillis = due }
                    taskCalendar.get(java.util.Calendar.YEAR) == filterYear && 
                        taskCalendar.get(java.util.Calendar.DAY_OF_YEAR) == filterDayOfYear
                } else false
            }
        }

        // 5. Sorting & Ordering (Uncompleted tasks at top, then sort option)
        result.sortedWith { t1, t2 ->
            // First level: uncompleted vs completed (active tasks on top)
            if (t1.isCompleted != t2.isCompleted) {
                if (!t1.isCompleted) -1 else 1
            } else {
                // Second level: user selected sort option
                when (sort) {
                    TaskSortOption.PRIORITY_DESC -> {
                        // High (2) > Medium (1) > Low (0)
                        val p1 = t1.priority.ordinal
                        val p2 = t2.priority.ordinal
                        p2.compareTo(p1) // descending
                    }
                    TaskSortOption.DUE_DATE_ASC -> {
                        val d1 = t1.dueDate ?: Long.MAX_VALUE
                        val d2 = t2.dueDate ?: Long.MAX_VALUE
                        d1.compareTo(d2) // ascending (earliest due first, tasks with no due date at the end)
                    }
                    TaskSortOption.CREATION_DESC -> {
                        t2.createdAt.compareTo(t1.createdAt) // descending
                    }
                }
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Task statistics
    val taskStats: StateFlow<TaskStats> = combine(repository.allTasks, _dateFilter) { all, dateFilterMs ->
        var list = all
        if (dateFilterMs != null) {
            val filterCalendar = java.util.Calendar.getInstance().apply { timeInMillis = dateFilterMs }
            val filterYear = filterCalendar.get(java.util.Calendar.YEAR)
            val filterDayOfYear = filterCalendar.get(java.util.Calendar.DAY_OF_YEAR)

            list = list.filter { task ->
                val due = task.dueDate
                if (due != null) {
                    val taskCalendar = java.util.Calendar.getInstance().apply { timeInMillis = due }
                    taskCalendar.get(java.util.Calendar.YEAR) == filterYear && 
                        taskCalendar.get(java.util.Calendar.DAY_OF_YEAR) == filterDayOfYear
                } else false
            }
        }
        
        val total = list.size
        val completed = list.count { it.isCompleted }
        val active = total - completed
        TaskStats(total = total, completed = completed, active = active)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TaskStats()
    )

    fun setStatusFilter(status: TaskFilterStatus) {
        _statusFilter.value = status
    }

    fun setPriorityFilter(priority: TaskFilterPriority) {
        _priorityFilter.value = priority
    }

    fun setSortOption(sort: TaskSortOption) {
        _sortOption.value = sort
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setDateFilter(date: Long?) {
        _dateFilter.value = date
    }

    fun addRecentDate(date: Long) {
        val current = _recentDates.value.toMutableList()
        val calDate = java.util.Calendar.getInstance().apply { timeInMillis = date }
        
        current.removeAll { 
            val r = java.util.Calendar.getInstance().apply { timeInMillis = it }
            r.get(java.util.Calendar.YEAR) == calDate.get(java.util.Calendar.YEAR) &&
            r.get(java.util.Calendar.DAY_OF_YEAR) == calDate.get(java.util.Calendar.DAY_OF_YEAR)
        }
        current.add(date)
        if (current.size > 3) {
            current.removeAt(0)
        }
        _recentDates.value = current
        prefs.edit().putString("recent_dates", current.joinToString(",")).apply()
    }

    // DB Operations
    fun saveTask(title: String, notes: String, priority: TaskPriority, dueDate: Long?) {
        viewModelScope.launch {
            repository.insert(
                Task(
                    title = title.trim(),
                    notes = notes.trim(),
                    priority = priority,
                    dueDate = dueDate
                )
            )
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            repository.update(task)
        }
    }

    fun toggleTaskCompletion(task: Task) {
        viewModelScope.launch {
            repository.update(task.copy(isCompleted = !task.isCompleted))
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            repository.delete(task)
        }
    }
}

data class TaskStats(
    val total: Int = 0,
    val completed: Int = 0,
    val active: Int = 0
)

class TaskViewModelFactory(
    private val repository: TaskRepository,
    private val prefs: SharedPreferences
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TaskViewModel(repository, prefs) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
