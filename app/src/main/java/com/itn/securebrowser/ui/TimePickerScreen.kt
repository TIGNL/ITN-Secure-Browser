package com.itn.securebrowser.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun TimePickerScreen(navController: NavController, hour: Int, minute: Int, target: String) {
    val state = rememberTimePickerState(
        initialHour = hour.coerceIn(0, 23),
        initialMinute = minute.coerceIn(0, 59),
        is24Hour = true
    )

    Scaffold(
        topBar = { ScreenTopBar("Select time", onBack = { navController.popBackStack() }) },
        bottomBar = {
            PinnedBottomBar {
                SecondaryButton(
                    text = "Cancel",
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.weight(1f)
                )
                PrimaryButton(
                    text = "OK",
                    modifier = Modifier.weight(1f),
                    onClick = {
                        val key = if (target == "to") "time_to_result" else "time_from_result"
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set(key, "${state.hour}|${state.minute}")
                        navController.popBackStack()
                    }
                )
            }
        }
    ) { padding ->
        Row(
            modifier = Modifier
                .padding(padding)
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            TimePicker(state = state)
        }
    }
}
