package com.example.projectmobileapplications

import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.projectmobileapplications.phone.*
import com.example.projectmobileapplications.tablet.TabletLayout
import com.example.projectmobileapplications.ui.theme.ProjectMobileApplicationsTheme
import com.example.projectmobileapplications.viewmodel.ElementsViewModel
import java.util.Locale
import android.Manifest
import android.annotation.SuppressLint

class MainActivity : ComponentActivity() {
    @SuppressLint("LocalContextConfigurationRead")
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }
        enableEdgeToEdge()
        setContent {
            val elementsViewModel: ElementsViewModel = viewModel()
            
            val currentLanguage = elementsViewModel.currentLanguage
            val locale = remember(currentLanguage) { Locale.forLanguageTag(currentLanguage.lowercase()) }
            val context = LocalContext.current

            LaunchedEffect(locale) {
                Locale.setDefault(locale)
                val resources = context.resources
                val configuration = Configuration(resources.configuration)
                configuration.setLocale(locale)
                configuration.setLayoutDirection(locale)
                @Suppress("DEPRECATION")
                resources.updateConfiguration(configuration, resources.displayMetrics)
            }

            val localizedContext = remember(locale) {
                val config = Configuration(context.resources.configuration)
                config.setLocale(locale)
                config.setLayoutDirection(locale)
                context.createConfigurationContext(config)
            }

            CompositionLocalProvider(
                LocalContext provides localizedContext,
                LocalConfiguration provides localizedContext.resources.configuration
            ) {
                ProjectMobileApplicationsTheme(darkTheme = elementsViewModel.isDarkMode) {
                    val configuration = LocalConfiguration.current
                    val isTablet = configuration.screenWidthDp >= 600

                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            if (isTablet) {
                                TabletLayout(viewModel = elementsViewModel)
                            } else {
                                AnimatedContent(
                                    targetState = elementsViewModel.currentScreen,
                                    transitionSpec = {
                                        fadeIn(animationSpec = tween(400)) togetherWith 
                                                fadeOut(animationSpec = tween(400))
                                    },
                                    label = "ScreenTransition",
                                    modifier = Modifier.fillMaxSize()
                                ) { screen ->
                                    when (screen) {
                                        "schedule" -> ScheduleScreen(
                                            isDarkMode = elementsViewModel.isDarkMode,
                                            is24HourFormat = elementsViewModel.is24HourFormat,
                                            weekOffset = elementsViewModel.weekOffset,
                                            selectedDayIndex = elementsViewModel.selectedDayIndex,
                                            scheduleItems = elementsViewModel.scheduleItems,
                                            onWeekOffsetChange = { elementsViewModel.updateWeekOffset(it) },
                                            onDayIndexChange = { elementsViewModel.updateSelectedDayIndex(it) },
                                            onNavigate = { elementsViewModel.navigateTo(it) },
                                            onEventClick = { elementsViewModel.openEventDetails(it) }
                                        )
                                        "events" -> EventScreen(
                                            isDarkMode = elementsViewModel.isDarkMode,
                                            is24HourFormat = elementsViewModel.is24HourFormat,
                                            scheduleItems = elementsViewModel.scheduleItems,
                                            onNavigate = { elementsViewModel.navigateTo(it) },
                                            onShowDialog = {
                                                elementsViewModel.resetEventForm()
                                                elementsViewModel.showCreateEventDialog = true
                                            },
                                            onEditItem = { elementsViewModel.startEditing(it) },
                                            onDeleteItem = { elementsViewModel.deleteItem(it) },
                                            onExportItem = { elementsViewModel.exportToGoogleCalendar(context, it) }
                                        )
                                        "create_event" -> CreateEventScreen(
                                            isDarkMode = elementsViewModel.isDarkMode,
                                            onNavigate = { elementsViewModel.navigateTo(it)
                                                         elementsViewModel.resetEventForm()
                                            },
                                            viewModel = elementsViewModel
                                        )
                                        "settings" -> SettingScreen(
                                            isDarkMode = elementsViewModel.isDarkMode,
                                            is24HourFormat = elementsViewModel.is24HourFormat,
                                            currentLanguage = elementsViewModel.currentLanguage,
                                            onThemeChange = { elementsViewModel.toggleTheme(it) },
                                            onTimeFormatChange = { elementsViewModel.toggleTimeFormat(it) },
                                            onLanguageChange = { elementsViewModel.changeLanguage(it) },
                                            onNavigate = { elementsViewModel.navigateTo(it) }
                                        )
                                    }
                                }
                            }

                            if (elementsViewModel.showCreateEventDialog) {
                                CreateEventDialog(
                                    isDarkMode = elementsViewModel.isDarkMode,
                                    viewModel = elementsViewModel,
                                    onDismiss = { elementsViewModel.showCreateEventDialog = false }
                                )
                            }

                            elementsViewModel.selectedEventForDetails?.let { event ->
                                EventDetailsDialog(
                                    item = event,
                                    isDarkMode = elementsViewModel.isDarkMode,
                                    is24HourFormat = elementsViewModel.is24HourFormat,
                                    onDismiss = { elementsViewModel.selectedEventForDetails = null },
                                    onEdit = {
                                        elementsViewModel.startEditing(event)
                                        elementsViewModel.selectedEventForDetails = null
                                    },
                                    onExport = {
                                        elementsViewModel.exportToGoogleCalendar(context, event)
                                    },
                                    onToggleTodo = { elementsViewModel.toggleTodoItem(it) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
