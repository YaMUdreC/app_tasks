package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Task
import com.example.data.TaskPriority
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TaskItemCard(
    task: Task,
    modifier: Modifier = Modifier,
    onToggleComplete: (Task) -> Unit,
    onDelete: (Task) -> Unit,
    onEdit: (Task) -> Unit,
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

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("task_card_${task.id}")
            .clickable { onEdit(task) },
        shape = RoundedCornerShape(24.dp),
        color = containerColor,
        border = BorderStroke(1.dp, borderCol)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Elegant circular checkbox with standard 48dp touch target
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .clickable { onToggleComplete(task) }
                    .testTag("task_checkbox_${task.id}")
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
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
            }

            Spacer(modifier = Modifier.width(4.dp))

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
                        val formattedDate = remember(task.dueDate) { dateFormat.format(Date(task.dueDate)) }
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
                                text = formattedDate,
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
                onClick = { onDelete(task) },
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
