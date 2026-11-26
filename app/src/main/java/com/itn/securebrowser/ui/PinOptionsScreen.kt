package com.itn.securebrowser.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.itn.securebrowser.pinEntryRoute
import com.itn.securebrowser.util.PinManager

@Composable
fun PinOptionsScreen(navController: NavController) {
    val context = LocalContext.current

    ScreenBody(title = "PIN Lock", onBack = { navController.popBackStack() }) {
        SettingRow(
            title = "Change PIN",
            onClick = {
                navController.navigate(
                    pinEntryRoute(
                        mode = "verify",
                        subtitle = "Verify first",
                        changeAction = "setup"
                    )
                )
            }
        )
        SettingRow(
            title = "Disable PIN",
            onClick = {
                PinManager.clear(context)
                navController.popBackStack("settings", false)
            }
        )
    }
}
