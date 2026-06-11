package com.example.ui.components

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.data.Task
import com.example.data.TaskPriority
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

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
                TaskPriority.entries.forEach { level ->
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
                    DatePickerDialog(
                        context,
                        { _, year, month, dayOfMonth ->
                            TimePickerDialog(
                                context,
                                { _, hourOfDay, minute ->
                                    val selectedCal = Calendar.getInstance()
                                    selectedCal.set(Calendar.YEAR, year)
                                    selectedCal.set(Calendar.MONTH, month)
                                    selectedCal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                                    selectedCal.set(Calendar.HOUR_OF_DAY, hourOfDay)
                                    selectedCal.set(Calendar.MINUTE, minute)
                                    selectedCal.set(Calendar.SECOND, 0)
                                    selectedCal.set(Calendar.MILLISECOND, 0)
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
