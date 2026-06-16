package com.example.projectmobileapplications.phone

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.projectmobileapplications.R
import com.example.projectmobileapplications.ui.theme.*


@Composable
fun EventDetailsDialog(
    item: ScheduleItem,
    isDarkMode: Boolean,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onExport: () -> Unit,
    onToggleTodo: (String) -> Unit,
    is24HourFormat: Boolean
) {
    val bgColor = if (isDarkMode) cardBgDark else cardBgLight
    val textColor = if (isDarkMode) Color.White else Color.Black
    val displayTime = if (is24HourFormat) item.timeLabel else item.getAmPmTimeLabel()

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = bgColor,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            Column {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(purpleTheme)
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.title,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp,
                                lineHeight = 26.sp
                            )
                            Text(
                                text = displayTime,
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 16.sp
                            )
                        }
                        Row {
                            IconButton(onClick = onExport) {
                                Icon(Icons.Default.CalendarMonth, contentDescription = "Export to Google Calendar", tint = Color.White)
                            }
                            IconButton(onClick = onEdit) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.White)
                            }
                            IconButton(onClick = onDismiss) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                            }
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    if (item.todoList.isNotEmpty()) {
                        Text(
                            text = stringResource(R.string.todo_list),
                            color = textColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        item.todoList.forEach { todo ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = todo.isDone,
                                    onCheckedChange = { onToggleTodo(todo.id) },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = eventHeaderColor
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = todo.task,
                                    color = textColor,
                                    fontSize = 16.sp,
                                    textDecoration = if (todo.isDone) TextDecoration.LineThrough else null
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    if (item.comments.isNotEmpty()) {
                        Text(
                            text = stringResource(R.string.comments),
                            color = textColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = item.comments,
                            color = textColor.copy(alpha = 0.8f),
                            fontSize = 15.sp,
                            lineHeight = 20.sp
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}
