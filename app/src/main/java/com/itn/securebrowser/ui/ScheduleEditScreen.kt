package com.itn.securebrowser.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.itn.securebrowser.timePickerRoute
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun ScheduleEditScreen(navController: NavController) {
    val dayIds = remember {
        listOf("SATURDAY", "SUNDAY", "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY")
    }
    val dayLabels = remember { listOf("Sat", "Sun", "Mon", "Tue", "Wed", "Thu", "Fri") }
    val selected = remember { mutableStateOf(setOf<String>()) }
    val fromHour = remember { mutableStateOf(8) }
    val fromMin = remember { mutableStateOf(0) }
    val toHour = remember { mutableStateOf(22) }
    val toMin = remember { mutableStateOf(0) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val fromResult = navController.currentBackStackEntry
        ?.savedStateHandle
        ?.getStateFlow<String?>("time_from_result", null)
        ?.collectAsState(initial = null)
    val toResult = navController.currentBackStackEntry
        ?.savedStateHandle
        ?.getStateFlow<String?>("time_to_result", null)
        ?.collectAsState(initial = null)

    LaunchedEffect(fromResult?.value) {
        val parts = fromResult?.value?.split("|") ?: return@LaunchedEffect
        if (parts.size == 2) {
            fromHour.value = parts[0].toIntOrNull() ?: 8
            fromMin.value = parts[1].toIntOrNull() ?: 0
        }
        navController.currentBackStackEntry?.savedStateHandle?.remove<String>("time_from_result")
    }
    LaunchedEffect(toResult?.value) {
        val parts = toResult?.value?.split("|") ?: return@LaunchedEffect
        if (parts.size == 2) {
            toHour.value = parts[0].toIntOrNull() ?: 22
            toMin.value = parts[1].toIntOrNull() ?: 0
        }
        navController.currentBackStackEntry?.savedStateHandle?.remove<String>("time_to_result")
    }

    Scaffold(
        topBar = { ScreenTopBar("Add Block Schedule", onBack = { navController.popBackStack() }) },
        bottomBar = {
            PinnedBottomBar {
                SecondaryButton(
                    text = "Cancel",
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.weight(1f)
                )
                PrimaryButton(
                    text = "Save",
                    modifier = Modifier.weight(1f),
                    onClick = {
                        if (selected.value.isEmpty()) {
                            scope.launch { snackbar.showSnackbar("Select at least one day") }
                            return@PrimaryButton
                        }
                        val result = selected.value.joinToString(",") + "|" +
                            formatTime(fromHour.value, fromMin.value) + "|" +
                            formatTime(toHour.value, toMin.value)
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("schedule_result", result)
                        navController.popBackStack()
                    }
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            SectionHeader("Days")
            Row(modifier = Modifier.fillMaxWidth()) {
                dayLabels.forEachIndexed { index, label ->
                    DayChip(
                        label = label,
                        selected = dayIds[index] in selected.value,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            selected.value = if (dayIds[index] in selected.value) {
                                selected.value - dayIds[index]
                            } else {
                                selected.value + dayIds[index]
                            }
                        }
                    )
                }
            }

            SectionHeader("From")
            SettingRow(
                title = formatTime(fromHour.value, fromMin.value),
                onClick = {
                    navController.navigate(
                        timePickerRoute(fromHour.value, fromMin.value, "from")
                    )
                }
            )
            SectionHeader("To")
            SettingRow(
                title = formatTime(toHour.value, toMin.value),
                onClick = {
                    navController.navigate(
                        timePickerRoute(toHour.value, toMin.value, "to")
                    )
                }
            )
        }
    }
}

@Composable
private fun DayChip(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    androidx.compose.material3.FilterChip(
        selected = selected,
        onClick = onClick,
        modifier = modifier.padding(horizontal = 2.dp),
        label = {
            Text(
                text = label,
                fontSize = 12.sp,
                color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
                else MaterialTheme.colorScheme.onSurface
            )
        }
    )
}

private fun formatTime(hour: Int, minute: Int): String =
    String.format(Locale.US, "%02d:%02d", hour, minute)
