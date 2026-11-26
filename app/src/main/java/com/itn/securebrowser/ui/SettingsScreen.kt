package com.itn.securebrowser.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.itn.securebrowser.util.PinManager

@Composable
fun SettingsScreen(navController: NavController) {
    val context = LocalContext.current

    Column {
        ScreenTopBar("Settings", onBack = { navController.popBackStack() })
        SettingRow(
            title = "PIN Lock",
            onClick = {
                navController.navigate(
                    if (PinManager.hasPin(context)) "pin_options" else "pin_setup"
                )
            }
        )
        SettingRow(
            title = "Limit websites time",
            onClick = { navController.navigate("groups") }
        )
    }
}
