package com.example.projectmobileapplications.phone

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projectmobileapplications.R
import com.example.projectmobileapplications.tablet.TabletTabRow
import kotlinx.coroutines.launch
import com.example.projectmobileapplications.ui.theme.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(
    isDarkMode: Boolean,
    is24HourFormat: Boolean,
    currentLanguage: String,
    onThemeChange: (Boolean) -> Unit,
    onTimeFormatChange: (Boolean) -> Unit,
    onLanguageChange: (String) -> Unit,
    onNavigate: (String) -> Unit,
    isTablet: Boolean = false
) {
    val itemBg = if (isDarkMode) Color(0xFF404040) else Color(0xFFE0E0E0)
    val currentBg = if (isDarkMode) darkBg else lightBg
    val currentCardBg = if (isDarkMode) cardBgDark else cardBgLight
    val textColor = if (isDarkMode) Color.White else Color.Black

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    var showThemeMenu by remember { mutableStateOf(false) }
    var showLanguageMenu by remember { mutableStateOf(false) }
    var showTimeMenu by remember { mutableStateOf(false) }

    BackHandler {
        onNavigate("schedule")
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text(text = stringResource(R.string.settings), color = Color.White, fontSize = 20.sp) },
                    actions = {
                        if (!isTablet) {
                            IconButton(onClick = { scope.launch { if (drawerState.isClosed) drawerState.open() else drawerState.close() } }) {
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
                    TabletTabRow(currentScreen = "settings", onNavigate = onNavigate)
                }
            }
        },
        containerColor = currentBg
    ) { innerPadding ->
        if (isTablet) {
            Box(modifier = Modifier.padding(innerPadding)) {
                SettingsContent(
                    currentCardBg, itemBg, textColor, currentLanguage, is24HourFormat, isDarkMode,
                    showLanguageMenu, onShowLanguageMenuChange = { showLanguageMenu = it },
                    showTimeMenu, onShowTimeMenuChange = { showTimeMenu = it },
                    showThemeMenu, onShowThemeMenuChange = { showThemeMenu = it },
                    onLanguageChange, onTimeFormatChange, onThemeChange
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
                                currentScreen = "settings",
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
                        SettingsContent(
                            currentCardBg, itemBg, textColor, currentLanguage, is24HourFormat, isDarkMode,
                            showLanguageMenu, onShowLanguageMenuChange = { showLanguageMenu = it },
                            showTimeMenu, onShowTimeMenuChange = { showTimeMenu = it },
                            showThemeMenu, onShowThemeMenuChange = { showThemeMenu = it },
                            onLanguageChange, onTimeFormatChange, onThemeChange
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsContent(
    currentCardBg: Color,
    itemBg: Color,
    textColor: Color,
    currentLanguage: String,
    is24HourFormat: Boolean,
    isDarkMode: Boolean,
    showLanguageMenu: Boolean,
    onShowLanguageMenuChange: (Boolean) -> Unit,
    showTimeMenu: Boolean,
    onShowTimeMenuChange: (Boolean) -> Unit,
    showThemeMenu: Boolean,
    onShowThemeMenuChange: (Boolean) -> Unit,
    onLanguageChange: (String) -> Unit,
    onTimeFormatChange: (Boolean) -> Unit,
    onThemeChange: (Boolean) -> Unit
) {
    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = currentCardBg),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                
                SettingRowWithMenu(
                    label = stringResource(R.string.language),
                    currentValue = if (currentLanguage == "EN") stringResource(R.string.en) else stringResource(R.string.pl),
                    expanded = showLanguageMenu,
                    onExpandedChange = onShowLanguageMenuChange,
                    itemBg = itemBg,
                    textColor = textColor,
                    currentCardBg = currentCardBg
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.en), color = textColor) },
                        onClick = { onLanguageChange("EN"); onShowLanguageMenuChange(false) }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.pl), color = textColor) },
                        onClick = { onLanguageChange("PL"); onShowLanguageMenuChange(false) }
                    )
                }

                SettingRowWithMenu(
                    label = stringResource(R.string.time_format),
                    currentValue = if (is24HourFormat) stringResource(R.string.h24) else stringResource(R.string.am_pm),
                    expanded = showTimeMenu,
                    onExpandedChange = onShowTimeMenuChange,
                    itemBg = itemBg,
                    textColor = textColor,
                    currentCardBg = currentCardBg
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.h24), color = textColor) },
                        onClick = { onTimeFormatChange(true); onShowTimeMenuChange(false) }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.am_pm), color = textColor) },
                        onClick = { onTimeFormatChange(false); onShowTimeMenuChange(false) }
                    )
                }
                
                SettingRowWithMenu(
                    label = stringResource(R.string.theme),
                    currentValue = if (isDarkMode) stringResource(R.string.dark) else stringResource(R.string.light),
                    expanded = showThemeMenu,
                    onExpandedChange = onShowThemeMenuChange,
                    itemBg = itemBg,
                    textColor = textColor,
                    currentCardBg = currentCardBg
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.dark), color = textColor) },
                        onClick = { onThemeChange(true); onShowThemeMenuChange(false) }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.light), color = textColor) },
                        onClick = { onThemeChange(false); onShowThemeMenuChange(false) }
                    )
                }
            }
        }
    }
}

@Composable
fun SettingRowWithMenu(
    label: String,
    currentValue: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    itemBg: Color,
    textColor: Color,
    currentCardBg: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    val context = LocalContext.current
    val config = LocalConfiguration.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = textColor, fontSize = 18.sp)
        Box {
            Surface(
                color = itemBg,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .width(120.dp)
                    .height(36.dp)
                    .clickable { onExpandedChange(true) }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = currentValue, color = textColor, fontSize = 16.sp, maxLines = 1)
                }
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { onExpandedChange(false) },
                modifier = Modifier.background(currentCardBg)
            ) {
                CompositionLocalProvider(
                    LocalContext provides context,
                    LocalConfiguration provides config
                ) {
                    content()
                }
            }
        }
    }
}
