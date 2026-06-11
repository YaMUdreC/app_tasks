package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.TaskFilterPriority
import com.example.ui.TaskFilterStatus
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

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
    
    val filterCal = remember(currentDateFilter) {
        currentDateFilter?.let { Calendar.getInstance().apply { timeInMillis = it } }
    }
    val taskCal = remember { Calendar.getInstance() }
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Status filter buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TaskFilterStatus.entries.forEach { status ->
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
                
                TaskFilterPriority.entries.forEach { priority ->
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
                val isSelected = filterCal != null && run {
                    taskCal.timeInMillis = recentDateMillis
                    taskCal.get(Calendar.DAY_OF_YEAR) == filterCal.get(Calendar.DAY_OF_YEAR) &&
                    taskCal.get(Calendar.YEAR) == filterCal.get(Calendar.YEAR)
                }

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
