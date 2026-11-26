package com.itn.securebrowser.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.itn.securebrowser.pinEntryRoute

@Composable
fun PinSetupScreen(navController: NavController) {
    ScreenBody(title = "Choose lock type", onBack = { navController.popBackStack() }) {
        SettingRow(
            title = "Password",
            onClick = {
                navController.navigate(
                    pinEntryRoute(mode = "set", lockType = "password", pinLength = 0)
                )
            }
        )
        SettingRow(
            title = "4-Digit PIN",
            onClick = {
                navController.navigate(
                    pinEntryRoute(mode = "set", lockType = "pin4", pinLength = 4)
                )
            }
        )
        SettingRow(
            title = "6-Digit PIN",
            onClick = {
                navController.navigate(
                    pinEntryRoute(mode = "set", lockType = "pin6", pinLength = 6)
                )
            }
        )
    }
}
