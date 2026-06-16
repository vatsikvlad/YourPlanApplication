package com.example.projectmobileapplications.tablet

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projectmobileapplications.R
import com.example.projectmobileapplications.phone.*
import com.example.projectmobileapplications.viewmodel.ElementsViewModel
import com.example.projectmobileapplications.ui.theme.*


@Composable
fun TabletTabRow(
    currentScreen: String,
    onNavigate: (String) -> Unit
) {
    val tabs = listOf(
        "schedule" to stringResource(R.string.schedule),
        "events" to stringResource(R.string.events),
        "create_event" to stringResource(R.string.create_event),
        "settings" to stringResource(R.string.settings)
    )
    val selectedIndex = tabs.indexOfFirst { it.first == currentScreen }.coerceAtLeast(0)

    TabRow(
        selectedTabIndex = selectedIndex,
        containerColor = purpleTheme,
        contentColor = Color.White,
        divider = {}
    ) {
        tabs.forEachIndexed { index, (route, label) ->
            Tab(
                selected = selectedIndex == index,
                onClick = { onNavigate(route) },
                text = {
                    Text(
                        text = label,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = if (selectedIndex == index) FontWeight.Bold else FontWeight.Normal
                    )
                }
            )
        }
    }
}

@Composable
fun TabletLayout(
    viewModel: ElementsViewModel
) {
    val context = LocalContext.current
    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = viewModel.currentScreen,
            transitionSpec = {
                fadeIn(animationSpec = tween(400)) togetherWith 
                        fadeOut(animationSpec = tween(400))
            },
            label = "TabletScreenTransition"
        ) { screen ->
            when (screen) {
                "schedule" -> ScheduleScreen(
                    isDarkMode = viewModel.isDarkMode,
                    is24HourFormat = viewModel.is24HourFormat,
                    weekOffset = viewModel.weekOffset,
                    selectedDayIndex = viewModel.selectedDayIndex,
                    scheduleItems = viewModel.scheduleItems,
                    onWeekOffsetChange = { viewModel.updateWeekOffset(it) },
                    onDayIndexChange = { viewModel.updateSelectedDayIndex(it) },
                    onNavigate = { viewModel.navigateTo(it) },
                    onEventClick = { viewModel.openEventDetails(it) },
                    isTablet = true
                )
                "events" -> EventScreen(
                    isDarkMode = viewModel.isDarkMode,
                    is24HourFormat = viewModel.is24HourFormat,
                    scheduleItems = viewModel.scheduleItems,
                    onNavigate = { viewModel.navigateTo(it) },
                    onShowDialog = {
                        viewModel.resetEventForm()
                        viewModel.showCreateEventDialog = true
                    },
                    onEditItem = { viewModel.startEditing(it) },
                    onDeleteItem = { viewModel.deleteItem(it) },
                    onExportItem = { viewModel.exportToGoogleCalendar(context, it) },
                    isTablet = true
                )
                "create_event" -> CreateEventScreen(
                    isDarkMode = viewModel.isDarkMode,
                    onNavigate = { 
                        viewModel.navigateTo(it)
                        viewModel.resetEventForm()
                    },
                    viewModel = viewModel,
                    isTablet = true
                )
                "settings" -> SettingScreen(
                    isDarkMode = viewModel.isDarkMode,
                    is24HourFormat = viewModel.is24HourFormat,
                    currentLanguage = viewModel.currentLanguage,
                    onThemeChange = { viewModel.toggleTheme(it) },
                    onTimeFormatChange = { viewModel.toggleTimeFormat(it) },
                    onLanguageChange = { viewModel.changeLanguage(it) },
                    onNavigate = { viewModel.navigateTo(it) },
                    isTablet = true
                )
            }
        }
    }
}
