package com.itn.securebrowser

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.itn.securebrowser.theme.ITNTheme
import com.itn.securebrowser.ui.BrowserScreen
import com.itn.securebrowser.ui.GroupEditScreen
import com.itn.securebrowser.ui.GroupListScreen
import com.itn.securebrowser.ui.ManageSpaceScreen
import com.itn.securebrowser.ui.PinEntryScreen
import com.itn.securebrowser.ui.PinOptionsScreen
import com.itn.securebrowser.ui.PinSetupScreen
import com.itn.securebrowser.ui.ScheduleEditScreen
import com.itn.securebrowser.ui.SettingsScreen
import com.itn.securebrowser.ui.TabListScreen
import com.itn.securebrowser.ui.TimePickerScreen

@Composable
fun ITNBrowserApp(startDestination: String = "browser") {
    val variant = if (BuildConfig.DESIGN_VARIANT == "full") DesignVariant.FULL else DesignVariant.FAITHFUL
    ITNTheme {
        CompositionLocalProvider(LocalDesignVariant provides variant) {
            val navController = rememberNavController()
            NavHost(navController = navController, startDestination = startDestination) {
                composable("browser") { BrowserScreen(navController) }
                composable("tabs") { TabListScreen(navController) }
                composable("settings") { SettingsScreen(navController) }
                composable("groups") { GroupListScreen(navController) }

                composable(
                    route = "group_edit?name={name}&new={new}",
                    arguments = listOf(
                        navArgument("name") { defaultValue = "" },
                        navArgument("new") { type = NavType.BoolType; defaultValue = true }
                    )
                ) { backStackEntry ->
                    GroupEditScreen(
                        navController = navController,
                        groupName = backStackEntry.arguments?.getString("name").orEmpty(),
                        isNew = backStackEntry.arguments?.getBoolean("new") ?: true
                    )
                }

                composable("schedule_edit") { ScheduleEditScreen(navController) }

                composable(
                    route = "time_picker?hour={hour}&minute={minute}&target={target}",
                    arguments = listOf(
                        navArgument("hour") { type = NavType.IntType; defaultValue = 12 },
                        navArgument("minute") { type = NavType.IntType; defaultValue = 0 },
                        navArgument("target") { defaultValue = "from" }
                    )
                ) { backStackEntry ->
                    TimePickerScreen(
                        navController = navController,
                        hour = backStackEntry.arguments?.getInt("hour") ?: 12,
                        minute = backStackEntry.arguments?.getInt("minute") ?: 0,
                        target = backStackEntry.arguments?.getString("target") ?: "from"
                    )
                }

                composable("pin_setup") { PinSetupScreen(navController) }
                composable("pin_options") { PinOptionsScreen(navController) }

                composable(
                    route = "pin_entry/{mode}?subtitle={subtitle}&next={next}&pinLength={pinLength}&lockType={lockType}&firstPin={firstPin}&changeAction={changeAction}",
                    arguments = listOf(
                        navArgument("mode") { defaultValue = "verify" },
                        navArgument("subtitle") { defaultValue = "" },
                        navArgument("next") { defaultValue = "" },
                        navArgument("pinLength") { type = NavType.IntType; defaultValue = 0 },
                        navArgument("lockType") { defaultValue = "" },
                        navArgument("firstPin") { defaultValue = "" },
                        navArgument("changeAction") { defaultValue = "" }
                    )
                ) { backStackEntry ->
                    val args = backStackEntry.arguments
                    PinEntryScreen(
                        navController = navController,
                        mode = args?.getString("mode").orEmpty(),
                        subtitle = args?.getString("subtitle").orEmpty(),
                        next = args?.getString("next").orEmpty(),
                        pinLength = args?.getInt("pinLength") ?: 0,
                        lockType = args?.getString("lockType").orEmpty(),
                        firstPin = args?.getString("firstPin").orEmpty(),
                        changeAction = args?.getString("changeAction").orEmpty()
                    )
                }

                composable("manage_space") { ManageSpaceScreen(navController) }
            }
        }
    }
}

fun pinEntryRoute(
    mode: String,
    subtitle: String = "",
    next: String = "",
    pinLength: Int = 0,
    lockType: String = "",
    firstPin: String = "",
    changeAction: String = ""
): String {
    val enc = { value: String -> Uri.encode(value) }
    return "pin_entry/${enc(mode)}?subtitle=${enc(subtitle)}&next=${enc(next)}" +
        "&pinLength=$pinLength&lockType=${enc(lockType)}" +
        "&firstPin=${enc(firstPin)}&changeAction=${enc(changeAction)}"
}

fun groupEditRoute(name: String, isNew: Boolean): String =
    "group_edit?name=${Uri.encode(name)}&new=$isNew"

fun timePickerRoute(hour: Int, minute: Int, target: String): String =
    "time_picker?hour=$hour&minute=$minute&target=$target"
