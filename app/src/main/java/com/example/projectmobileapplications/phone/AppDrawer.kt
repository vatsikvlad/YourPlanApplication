package com.example.projectmobileapplications.phone

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projectmobileapplications.R
import com.example.projectmobileapplications.ui.theme.*

@Composable
fun AppDrawerContent(
    isDarkMode: Boolean,
    currentScreen: String,
    onScreenSelected: (String) -> Unit
) {
    val drawerBg = if (isDarkMode) darkBg else cardBgLight
    val dividerColor = if (isDarkMode) Color.White else Color.LightGray
    val textColor = if (isDarkMode) Color.White else Color.Black

    ModalDrawerSheet(
        drawerContainerColor = drawerBg,
        drawerShape = RoundedCornerShape(0.dp),
        modifier = Modifier
            .fillMaxWidth(0.5f)
            .fillMaxHeight(),
        windowInsets = WindowInsets(0, 0, 0, 0)
    ) {
        DrawerItem(stringResource(R.string.schedule), currentScreen == "schedule", isDarkMode, textColor) { onScreenSelected("schedule") }
        HorizontalDivider(color = dividerColor, thickness = 0.5.dp)
        
        DrawerItem(stringResource(R.string.events), currentScreen == "events", isDarkMode, textColor) { onScreenSelected("events") }
        HorizontalDivider(color = dividerColor, thickness = 0.5.dp)
        
        DrawerItem(stringResource(R.string.create_event), currentScreen == "create_event", isDarkMode, textColor) { onScreenSelected("create_event") }
        HorizontalDivider(color = dividerColor, thickness = 0.5.dp)
        
        DrawerItem(stringResource(R.string.settings), currentScreen == "settings", isDarkMode, textColor) { onScreenSelected("settings") }
        HorizontalDivider(color = dividerColor, thickness = 0.5.dp)
    }
}

@Composable
private fun DrawerItem(
    label: String,
    selected: Boolean,
    isDarkMode: Boolean,
    textColor: Color,
    onClick: () -> Unit
) {
    NavigationDrawerItem(
        label = { 
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(
                    text = label, 
                    color = if (selected) Color.White else textColor, 
                    fontSize = 18.sp
                ) 
            } 
        },
        selected = selected,
        onClick = onClick,
        colors = NavigationDrawerItemDefaults.colors(
            selectedContainerColor = Color(0xFFA79EDD),
            unselectedContainerColor = Color.Transparent,
            selectedTextColor = Color.White,
            unselectedTextColor = textColor
        ),
        shape = RoundedCornerShape(0.dp),
        modifier = Modifier.height(48.dp)
    )
}
