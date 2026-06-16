package com.example.projectmobileapplications.phone

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.projectmobileapplications.R
import com.example.projectmobileapplications.viewmodel.ElementsViewModel
import java.util.*
import androidx.compose.ui.platform.LocalLocale
import com.example.projectmobileapplications.ui.theme.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventDialog(
    isDarkMode: Boolean,
    viewModel: ElementsViewModel,
    onDismiss: () -> Unit
) {
    val locale = LocalLocale.current.platformLocale

    val cardBg = if (isDarkMode) cardBgDark else cardBgLight
    val textColor = if (isDarkMode) Color.White else Color.Black
    val textFieldBg = if (isDarkMode) darkBg else lightBg

    var todoTask by remember { mutableStateOf("") }
    
    var showDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

    val formattedStartTime = if (viewModel.is24HourFormat) {
        String.format(locale, "%02d:%02d", viewModel.startTimeHour, viewModel.startTimeMinute)
    } else {
        formatAmPm(viewModel.startTimeHour, viewModel.startTimeMinute)
    }

    val formattedEndTime = if (viewModel.is24HourFormat) {
        String.format(locale, "%02d:%02d", viewModel.endTimeHour, viewModel.endTimeMinute)
    } else {
        formatAmPm(viewModel.endTimeHour, viewModel.endTimeMinute)
    }

    val daysOfWeek = listOf(
        stringResource(R.string.mon), stringResource(R.string.tue), stringResource(R.string.wed),
        stringResource(R.string.thu), stringResource(R.string.fri), stringResource(R.string.sat),
        stringResource(R.string.sun)
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = cardBg)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(purpleTheme)
                    .padding(vertical = 20.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ){
                Text(
                    text = if (viewModel.isEditing) stringResource(R.string.edit_event) else stringResource(R.string.create_event),
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.event_name),
                    color = textColor,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Start)
                )

                DialogEventTextField(
                    value = viewModel.eventName,
                    onValueChange = { viewModel.eventName = it },
                    placeholder = stringResource(R.string.name_event),
                    bgColor = textFieldBg,
                    textColor = textColor,
                    singleLine = true,
                    maxChars = 30
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(stringResource(R.string.repeat_every_week), color = textColor, fontSize = 14.sp)
                    Switch(
                        checked = viewModel.isRepeating,
                        onCheckedChange = { viewModel.isRepeating = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = purpleTheme, checkedTrackColor = eventHeaderColor)
                    )
                }

                if (viewModel.isRepeating) {
                    Text(stringResource(R.string.select_days), color = textColor, fontSize = 14.sp, modifier = Modifier.align(Alignment.Start))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        itemsIndexed(daysOfWeek) { index, day ->
                            val isSelected = viewModel.selectedDaysOfWeek.contains(index)
                            val isWeekend = index >= 5
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    val current = viewModel.selectedDaysOfWeek.toMutableSet()
                                    if (isSelected) current.remove(index) else current.add(index)
                                    viewModel.selectedDaysOfWeek = current
                                },
                                label = { Text(day, fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = if (isWeekend) weekendColor else eventHeaderColor,
                                    selectedLabelColor = Color.White,
                                    labelColor = if (isWeekend) weekendColor else textColor
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = if (isWeekend) weekendColor.copy(alpha = 0.7F) else Color.White.copy(alpha = 0.7F),
                                    selectedBorderColor = Color.Transparent,
                                    borderWidth = 1.dp
                                )
                            )
                        }
                    }
                } else {
                    DialogClickableField(
                        label = stringResource(R.string.set_day),
                        value = String.format(locale, "%02d/%02d/%04d",
                            viewModel.selectedDate.get(Calendar.DAY_OF_MONTH),
                            viewModel.selectedDate.get(Calendar.MONTH) + 1,
                            viewModel.selectedDate.get(Calendar.YEAR)),
                        bgColor = textFieldBg,
                        textColor = textColor,
                        onClick = { showDatePicker = true }
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DialogClickableField(
                        label = stringResource(R.string.set_from),
                        value = formattedStartTime,
                        bgColor = textFieldBg,
                        textColor = textColor,
                        modifier = Modifier.weight(1f),
                        onClick = { showStartTimePicker = true }
                    )
                    DialogClickableField(
                        label = stringResource(R.string.set_to),
                        value = formattedEndTime,
                        bgColor = textFieldBg,
                        textColor = textColor,
                        modifier = Modifier.weight(1f),
                        onClick = { showEndTimePicker = true }
                    )
                }

                HorizontalDivider(color = textColor.copy(alpha = 0.2f), thickness = 1.dp)

                Text(
                    text = stringResource(R.string.comments),
                    color = textColor,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Start)
                )

                DialogEventTextField(
                    value = viewModel.eventComments,
                    onValueChange = { viewModel.eventComments = it },
                    placeholder = stringResource(R.string.add_comments),
                    bgColor = textFieldBg,
                    textColor = textColor
                )

                HorizontalDivider(color = textColor.copy(alpha = 0.2f), thickness = 1.dp)

                Text(
                    text = stringResource(R.string.todo_list),
                    color = textColor,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Start)
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    viewModel.eventTodoList.forEach { todo ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = todo.task, color = textColor, modifier = Modifier.weight(1f))
                            IconButton(onClick = { viewModel.removeTodoItem(todo.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                            }
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DialogEventTextField(
                            value = todoTask,
                            onValueChange = { todoTask = it },
                            placeholder = stringResource(R.string.add_task),
                            bgColor = textFieldBg,
                            textColor = textColor,
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        IconButton(onClick = {
                            if (todoTask.isNotBlank()) {
                                viewModel.addTodoItem(todoTask)
                                todoTask = ""
                            }
                        }) {
                            Icon(Icons.Default.Add, contentDescription = "Add", tint = eventHeaderColor)
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = {
                        viewModel.resetEventForm()
                        onDismiss()
                    }) {
                        Text(stringResource(R.string.cancel), color = eventHeaderColor)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            viewModel.saveEvent()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = eventHeaderColor),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(stringResource(R.string.save), color = Color.White)
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = viewModel.selectedDate.timeInMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        viewModel.selectedDate = Calendar.getInstance().apply { timeInMillis = it }
                    }
                    showDatePicker = false
                }) { Text(stringResource(R.string.save)) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text(stringResource(R.string.cancel)) }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showStartTimePicker) {
        DialogTimePickerDialog(
            initialHour = viewModel.startTimeHour,
            initialMinute = viewModel.startTimeMinute,
            is24Hour = viewModel.is24HourFormat,
            onDismiss = { showStartTimePicker = false },
            onTimeSelected = { h, m ->
                viewModel.startTimeHour = h
                viewModel.startTimeMinute = m
                showStartTimePicker = false
            }
        )
    }

    if (showEndTimePicker) {
        DialogTimePickerDialog(
            initialHour = viewModel.endTimeHour,
            initialMinute = viewModel.endTimeMinute,
            is24Hour = viewModel.is24HourFormat,
            onDismiss = { showEndTimePicker = false },
            onTimeSelected = { h, m ->
                viewModel.endTimeHour = h
                viewModel.endTimeMinute = m
                showEndTimePicker = false
            }
        )
    }

    if (viewModel.showConflictWarning) {
        AlertDialog(
            onDismissRequest = { viewModel.showConflictWarning = false },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Red) },
            title = { Text(stringResource(R.string.conflict_warning_title)) },
            text = {
                Column {
                    Text(stringResource(R.string.conflict_warning_message))
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                        items(viewModel.conflictingEvents) { event ->
                            Text("- ${event.title} (${if (viewModel.is24HourFormat) event.timeLabel else event.getAmPmTimeLabel()})", 
                                fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { viewModel.saveEvent(force = true) }) {
                    Text(stringResource(R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.showConflictWarning = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DialogTimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    is24Hour: Boolean,
    onDismiss: () -> Unit,
    onTimeSelected: (Int, Int) -> Unit
) {
    val timePickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = is24Hour
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onTimeSelected(timePickerState.hour, timePickerState.minute) }) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
        text = {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                TimePicker(state = timePickerState)
            }
        }
    )
}

@Composable
private fun DialogClickableField(
    label: String,
    value: String,
    bgColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Text(text = label, color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(start = 4.dp, bottom = 2.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(bgColor, RoundedCornerShape(8.dp))
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(text = value, color = textColor, fontSize = 16.sp)
        }
    }
}

@Composable
private fun DialogEventTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    bgColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier,
    singleLine: Boolean = false,
    maxChars: Int? = null
) {
    Column(modifier = modifier) {
        TextField(
            value = value,
            onValueChange = {
                if (maxChars == null || it.length <= maxChars) {
                    onValueChange(it)
                }
            },
            placeholder = { Text(placeholder, color = Color.Gray) },
            singleLine = singleLine,
            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = bgColor,
                unfocusedContainerColor = bgColor,
                focusedTextColor = textColor,
                unfocusedTextColor = textColor,
                cursorColor = textColor,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            shape = RoundedCornerShape(8.dp)
        )
        if (maxChars != null) {
            Text(
                text = "${maxChars - value.length}/$maxChars",
                color = Color.Gray,
                fontSize = 12.sp,
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(top = 2.dp, end = 4.dp)
            )
        }
    }
}

private fun formatAmPm(hour: Int, minute: Int): String {
    val amPm = if (hour < 12) "AM" else "PM"
    val h = when {
        hour == 0 -> 12
        hour > 12 -> hour - 12
        else -> hour
    }
    return String.format("%d:%02d %s", h, minute, amPm)
}
