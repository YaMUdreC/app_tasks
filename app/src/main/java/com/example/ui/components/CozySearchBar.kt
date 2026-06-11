package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun CozySearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    isDark: Boolean
) {
    var text by remember { mutableStateOf(query) }

    LaunchedEffect(query) {
        if (query.isEmpty() && text.isNotEmpty()) {
            text = ""
        }
    }

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
            if (text.isEmpty()) {
                Text(
                    text = "Search tasks...",
                    color = textCol.copy(alpha = 0.4f),
                    fontSize = 15.sp,
                    fontFamily = FontFamily.SansSerif
                )
            }
            
            BasicTextField(
                value = text,
                onValueChange = { 
                    text = it 
                    onQueryChange(it) 
                },
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
        
        if (text.isNotEmpty()) {
            IconButton(
                onClick = { 
                    text = ""
                    onQueryChange("") 
                },
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
