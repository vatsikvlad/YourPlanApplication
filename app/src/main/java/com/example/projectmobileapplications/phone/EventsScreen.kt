package com.example.projectmobileapplications.phone

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import java.util.Locale
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projectmobileapplications.R
import com.example.projectmobileapplications.tablet.TabletTabRow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import com.example.projectmobileapplications.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventScreen(
    isDarkMode: Boolean,
    is24HourFormat: Boolean,
    scheduleItems: List<ScheduleItem>,
    onNavigate: (String) -> Unit,
    onShowDialog: () -> Unit,
    onEditItem: (ScheduleItem) -> Unit,
    onDeleteItem: (String) -> Unit,
    onExportItem: (ScheduleItem) -> Unit,
    isTablet: Boolean = false
) {

    val currentBg = if (isDarkMode) darkBg else lightBg
    val currentCardBg = if (isDarkMode) cardBgDark else cardBgLight
    val textColor = if (isDarkMode) Color.White else Color.Black

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val daysNames = listOf(
        stringResource(R.string.monday), stringResource(R.string.tuesday),
        stringResource(R.string.wednesday), stringResource(R.string.thursday),
        stringResource(R.string.friday), stringResource(R.string.saturday),
        stringResource(R.string.sunday)
    )

    BackHandler {
        onNavigate("schedule")
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(text = stringResource(R.string.events), color = Color.White, fontSize = 20.sp)
                    },
                    actions = {
                        if (!isTablet) {
                            IconButton(onClick = {
                                scope.launch {
                                    if (drawerState.isClosed) drawerState.open() else drawerState.close()
                                }
                            }) {
                                Icon(
                                    imageVector = if (drawerState.isOpen) Icons.Default.Close else Icons.Default.Menu,
                                    contentDescription = "Menu",
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = purpleTheme)
                )
                if (isTablet) {
                    TabletTabRow(currentScreen = "events", onNavigate = onNavigate)
                }
            }
        },
        containerColor = currentBg,
        contentWindowInsets = WindowInsets(0)
    ) { innerPadding ->
        if (isTablet) {
            Box(modifier = Modifier.padding(innerPadding)) {
                EventsContent(
                    scheduleItems, daysNames, is24HourFormat, currentCardBg, textColor, eventHeaderColor, onEditItem, onDeleteItem, onExportItem, onShowDialog
                )
            }
        } else {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                            AppDrawerContent(
                                isDarkMode = isDarkMode,
                                currentScreen = "events",
                                onScreenSelected = { screen ->
                                    scope.launch { drawerState.close() }
                                    onNavigate(screen)
                                }
                            )
                        }
                    },
                    modifier = Modifier.padding(innerPadding)
                ) {
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                        EventsContent(
                            scheduleItems, daysNames, is24HourFormat, currentCardBg, textColor, eventHeaderColor, onEditItem, onDeleteItem, onExportItem, onShowDialog
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EventsContent(
    scheduleItems: List<ScheduleItem>,
    daysNames: List<String>,
    is24HourFormat: Boolean,
    currentCardBg: Color,
    textColor: Color,
    eventHeaderColor: Color,
    onEditItem: (ScheduleItem) -> Unit,
    onDeleteItem: (String) -> Unit,
    onExportItem: (ScheduleItem) -> Unit,
    onShowDialog: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        scheduleItems.forEach { item ->
            val dayText = if (item.isRepeating) {
                item.daysOfWeek.joinToString(", "){ daysNames[it] }
            } else {
                item.specificDate?.let { date ->
                    SimpleDateFormat("d MMMM", Locale.getDefault()).format(date.time)
                } ?: ""
            }
            
            val displayTime = if (is24HourFormat) item.timeLabel else item.getAmPmTimeLabel()
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onEditItem(item) },
                colors = CardDefaults.cardColors(containerColor = currentCardBg),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = item.title, color = textColor, fontSize = 18.sp)
                        Text(
                            text = "$dayText, $displayTime",
                            color = textColor.copy(alpha = 0.7f),
                            fontSize = 14.sp
                        )
                    }
                    Row {
                        IconButton(onClick = { onExportItem(item) }) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = "Export to Google Calendar",
                                tint = eventHeaderColor
                            )
                        }
                        IconButton(onClick = { onEditItem(item) }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = stringResource(R.string.edit),
                                tint = eventHeaderColor
                            )
                        }
                        IconButton(onClick = { onDeleteItem(item.id) }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = stringResource(R.string.delete),
                                tint = Color.Red
                            )
                        }
                    }
                }
            }
        }
        
        Card(
            modifier = Modifier
                .width(200.dp)
                .clickable { onShowDialog() },
            colors = CardDefaults.cardColors(containerColor = currentCardBg),
            shape = RoundedCornerShape(8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Text(
                stringResource(R.string.create_new),
                color = textColor,
                modifier = Modifier.padding(20.dp),
                fontSize = 17.sp
            )
        }
    }
}
