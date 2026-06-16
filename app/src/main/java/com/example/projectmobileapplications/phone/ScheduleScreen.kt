package com.example.projectmobileapplications.phone

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projectmobileapplications.R
import com.example.projectmobileapplications.tablet.TabletTabRow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import com.example.projectmobileapplications.ui.theme.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    isDarkMode: Boolean,
    is24HourFormat: Boolean,
    weekOffset: Int,
    selectedDayIndex: Int,
    scheduleItems: List<ScheduleItem>,
    onWeekOffsetChange: (Int) -> Unit,
    onDayIndexChange: (Int) -> Unit,
    onNavigate: (String) -> Unit,
    onEventClick: (ScheduleItem) -> Unit,
    isTablet: Boolean = false
) {

    val currentBg = if (isDarkMode) darkBg else lightBg
    val currentCardBg = if (isDarkMode) cardBgDark else cardBgLight
    val textColor = if (isDarkMode) Color.White else Color.Black

    var currentTime by remember { mutableStateOf(Calendar.getInstance()) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = Calendar.getInstance()
            delay(1000 * 60)
        }
    }

    val hour = currentTime.get(Calendar.HOUR_OF_DAY)
    val minute = currentTime.get(Calendar.MINUTE)
    val currentTimeFloat = hour + (minute / 60f)

    val todayIndex = Calendar.getInstance().let { cal ->
        val d = cal.get(Calendar.DAY_OF_WEEK)
        if (d == Calendar.SUNDAY) 6 else d - 2
    }
    val showRedLine = weekOffset == 0 && selectedDayIndex == todayIndex

    val activeEvent = if (showRedLine) {
        scheduleItems.find { item ->
            if (item.isRepeating) {
                todayIndex in item.daysOfWeek && currentTimeFloat >= item.startTime && currentTimeFloat < item.endTime
            } else {
                item.specificDate?.let { date ->
                    date.get(Calendar.YEAR) == currentTime.get(Calendar.YEAR) &&
                    date.get(Calendar.DAY_OF_YEAR) == currentTime.get(Calendar.DAY_OF_YEAR) &&
                    currentTimeFloat >= item.startTime && currentTimeFloat < item.endTime
                } ?: false
            }
        }
    } else null
    val currentActivity = activeEvent?.title ?: stringResource(R.string.free_time)

    val weekRangeText = remember(weekOffset) {
        val cal = Calendar.getInstance()
        cal.add(Calendar.WEEK_OF_YEAR, weekOffset)

        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        val diffToMonday = if (dayOfWeek == Calendar.SUNDAY) -6 else 2 - dayOfWeek
        cal.add(Calendar.DAY_OF_YEAR, diffToMonday)

        val startDay = cal.time
        cal.add(Calendar.DAY_OF_YEAR, 6)
        val endDay = cal.time

        val sdf = SimpleDateFormat("d MMMM", Locale.getDefault())
        "${sdf.format(startDay)} - ${sdf.format(endDay)}"
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IconButton(onClick = { onWeekOffsetChange(weekOffset - 1) }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBackIos, null, tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                            Text(
                                text = weekRangeText,
                                color = Color.White,
                                fontSize = 18.sp
                            )
                            IconButton(onClick = { onWeekOffsetChange(weekOffset + 1) }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, null, tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
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
                    TabletTabRow(currentScreen = "schedule", onNavigate = onNavigate)
                }
            }
        },
        containerColor = currentBg,
        contentWindowInsets = WindowInsets(0)
    ) { innerPadding ->
        if (isTablet) {
            Box(modifier = Modifier.padding(innerPadding)) {
                ScheduleContent(
                    isDarkMode, is24HourFormat, weekOffset, selectedDayIndex, scheduleItems,
                    onDayIndexChange, onEventClick, currentBg, currentCardBg, textColor,
                    purpleTheme, eventColor, weekendColor, currentActivity, showRedLine,
                    hour, minute
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
                                currentScreen = "schedule",
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
                        ScheduleContent(
                            isDarkMode, is24HourFormat, weekOffset, selectedDayIndex, scheduleItems,
                            onDayIndexChange, onEventClick, currentBg, currentCardBg, textColor,
                            purpleTheme, eventColor, weekendColor, currentActivity, showRedLine,
                            hour, minute
                        )
                    }
                }
            }
        }
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
private fun ScheduleContent(
    isDarkMode: Boolean,
    is24HourFormat: Boolean,
    weekOffset: Int,
    selectedDayIndex: Int,
    scheduleItems: List<ScheduleItem>,
    onDayIndexChange: (Int) -> Unit,
    onEventClick: (ScheduleItem) -> Unit,
    currentBg: Color,
    currentCardBg: Color,
    textColor: Color,
    purpleTheme: Color,
    eventColor: Color,
    weekendColor: Color,
    currentActivity: String,
    showRedLine: Boolean,
    hour: Int,
    minute: Int
) {
    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            Surface(color = purpleTheme) {
                Row(
                    modifier = Modifier
                        .navigationBarsPadding()
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val days = listOf(
                        stringResource(R.string.mon), stringResource(R.string.tue),
                        stringResource(R.string.wed), stringResource(R.string.thu),
                        stringResource(R.string.fri), stringResource(R.string.sat),
                        stringResource(R.string.sun)
                    )
                    days.forEachIndexed { index, day ->
                        val isSelected = index == selectedDayIndex
                        Box(
                            modifier = Modifier
                                .background(
                                    if (isSelected) eventColor else Color.Transparent,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { onDayIndexChange(index) }
                                .padding(horizontal = 8.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = day,
                                color = if (index >= 5) weekendColor else Color.White,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 16.sp,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
            }
        },
        containerColor = currentBg
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp)
                .padding(bottom = 16.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = currentCardBg),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Text(
                    stringResource(R.string.now_you_have, currentActivity),
                    color = textColor,
                    modifier = Modifier.padding(24.dp),
                    fontSize = 20.sp
                )
            }

            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .border(
                        1.dp,
                        Color.Gray.copy(alpha = 0.5f),
                        RoundedCornerShape(12.dp)
                    ),
                color = currentCardBg,
                shape = RoundedCornerShape(12.dp)
            ) {
                val scrollState = rememberScrollState()
                val hourHeight = 70.dp
                val timeColWidth = 65.dp

                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                ) {
                    val totalWidth = maxWidth
                    val eventAreaWidth = totalWidth - timeColWidth - 12.dp

                    Column {
                        (0..23).forEach { h ->
                            Row(modifier = Modifier.height(hourHeight)) {
                                Box(
                                    modifier = Modifier
                                        .width(timeColWidth)
                                        .fillMaxHeight()
                                        .border(0.5.dp, if (isDarkMode) Color.White else Color.Gray),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val timeText = if (is24HourFormat) {
                                        "$h:00"
                                    } else {
                                        when {
                                            h == 0 -> "12 AM"
                                            h < 12 -> "$h AM"
                                            h == 12 -> "12 PM"
                                            else -> "${h - 12} PM"
                                        }
                                    }
                                    Text(
                                        text = timeText,
                                        color = textColor,
                                        fontSize = 12.sp
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .width(1.dp)
                                        .fillMaxHeight()
                                        .background(if (isDarkMode) Color.White.copy(alpha = 0.5f) else Color.Gray.copy(alpha = 0.5f))
                                )
                                Column(modifier = Modifier.fillMaxSize()) {
                                    repeat(4) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .weight(1f)
                                                .border(
                                                    width = 0.5.dp,
                                                    color = Color.Gray.copy(alpha = 0.5f)
                                                )
                                        )
                                    }
                                }
                            }
                        }
                    }

                    val dayItems = scheduleItems.filter { item ->
                        if (item.isRepeating) {
                            selectedDayIndex in item.daysOfWeek
                        } else {
                            item.specificDate?.let { date ->
                                val cal = Calendar.getInstance()
                                cal.add(Calendar.WEEK_OF_YEAR, weekOffset)
                                val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
                                val diffToMonday = if (dayOfWeek == Calendar.SUNDAY) -6 else 2 - dayOfWeek
                                cal.add(Calendar.DAY_OF_YEAR, diffToMonday)
                                cal.add(Calendar.DAY_OF_YEAR, selectedDayIndex)
                                
                                date.get(Calendar.YEAR) == cal.get(Calendar.YEAR) &&
                                date.get(Calendar.DAY_OF_YEAR) == cal.get(Calendar.DAY_OF_YEAR)
                            } ?: false
                        }
                    }.sortedBy { it.startTime }

                    val clusters = mutableListOf<MutableList<ScheduleItem>>()
                    for (item in dayItems) {
                        if (clusters.isEmpty() || item.startTime >= clusters.last().maxOf { it.endTime }) {
                            clusters.add(mutableListOf(item))
                        } else {
                            clusters.last().add(item)
                        }
                    }

                    clusters.forEach { cluster ->
                        val lanes = mutableListOf<MutableList<ScheduleItem>>()
                        val itemToLane = mutableMapOf<ScheduleItem, Int>()
                        
                        for (item in cluster) {
                            var placed = false
                            for (i in lanes.indices) {
                                if (lanes[i].last().endTime <= item.startTime) {
                                    lanes[i].add(item)
                                    itemToLane[item] = i
                                    placed = true
                                    break
                                }
                            }
                            if (!placed) {
                                itemToLane[item] = lanes.size
                                lanes.add(mutableListOf(item))
                            }
                        }
                        
                        val totalLanes = lanes.size
                        val colWidth = eventAreaWidth / totalLanes
                        
                        cluster.forEach { item ->
                            val laneIndex = itemToLane[item] ?: 0
                            val startOffset = timeColWidth + 6.dp + (colWidth * laneIndex)
                            
                            ScheduleEventItem(
                                item = item,
                                hourHeight = hourHeight,
                                is24HourFormat = is24HourFormat,
                                onClick = { onEventClick(item) },
                                width = colWidth,
                                startOffset = startOffset
                            )
                        }
                    }

                    if (showRedLine) {
                        val minuteOffset = (minute / 60f) * hourHeight.value
                        val yOffset = (hour * hourHeight.value) + minuteOffset

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .offset(y = yOffset.dp)
                                .height(2.dp)
                                .background(Color.Red)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ScheduleEventItem(
    item: ScheduleItem,
    hourHeight: Dp,
    is24HourFormat: Boolean,
    onClick: () -> Unit,
    width: Dp,
    startOffset: Dp
) {
    val displayTime = if (is24HourFormat) item.timeLabel else item.getAmPmTimeLabel()

    Column(
        modifier = Modifier
            .offset(x = startOffset, y = hourHeight * item.startTime + 2.dp)
            .width(width)
            .height(hourHeight * item.duration - 4.dp)
            .padding(horizontal = 2.dp)
            .background(item.bodyColor, RoundedCornerShape(8.dp))
            .border(1.dp, Color.Black.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(item.headerColor, RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                val textColor = Color.Black
                Text(displayTime, color = textColor, fontSize = 11.sp, maxLines = 1)
                Text(item.title, color = textColor, fontSize = 12.sp, maxLines = 1)
            }
        }
    }
}
