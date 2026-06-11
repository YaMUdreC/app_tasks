package com.example.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.TaskStats
import com.example.ui.theme.*

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
