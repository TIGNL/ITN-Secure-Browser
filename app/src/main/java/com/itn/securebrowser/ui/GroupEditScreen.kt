package com.itn.securebrowser.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.itn.securebrowser.util.BlockDataStore
import kotlinx.coroutines.launch

@Composable
fun GroupEditScreen(navController: NavController, groupName: String, isNew: Boolean) {
    val context = LocalContext.current
    val dataStore = remember { BlockDataStore(context) }
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }

    val name = remember { mutableStateOf(if (isNew) "" else groupName) }
    val limit = remember { mutableStateOf("") }
    val domainInput = remember { mutableStateOf("") }
    val domainError = remember { mutableStateOf("") }
    val domains = remember { mutableStateListOf<String>() }
    val schedules = remember { mutableStateListOf<BlockDataStore.BlockSchedule>() }

    LaunchedEffect(groupName, isNew) {
        if (!isNew) {
            dataStore.getGroups().firstOrNull { it.name == groupName }?.let { group ->
                name.value = group.name
                domains.addAll(group.domains)
                group.dailyLimits.values.firstOrNull()?.let { limit.value = it.toString() }
                schedules.addAll(group.schedules)
            }
        }
    }

    val pendingSchedule = navController.currentBackStackEntry
        ?.savedStateHandle
        ?.getStateFlow<String?>("schedule_result", null)
        ?.collectAsState(initial = null)

    LaunchedEffect(pendingSchedule?.value) {
        val value = pendingSchedule?.value ?: return@LaunchedEffect
        val parts = value.split("|")
        if (parts.size == 3) {
            schedules.add(
                BlockDataStore.BlockSchedule(
                    parts[0].split(",").filter { it.isNotBlank() },
                    parts[1],
                    parts[2]
                )
            )
        }
        navController.currentBackStackEntry?.savedStateHandle?.remove<String>("schedule_result")
    }

    Scaffold(
        topBar = {
            ScreenTopBar(
                title = if (isNew) "Add group" else "Save group",
                onBack = { navController.popBackStack() }
            )
        },
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
                        val groupNameValue = name.value.trim()
                        if (groupNameValue.isEmpty()) {
                            scope.launch { snackbar.showSnackbar("Enter a group name") }
                            return@PrimaryButton
                        }
                        if (domains.isEmpty()) {
                            scope.launch { snackbar.showSnackbar("Add at least one domain") }
                            return@PrimaryButton
                        }
                        val limits = HashMap<String, Int>()
                        val limitText = limit.value.trim()
                        if (limitText.isNotEmpty()) {
                            val mins = limitText.toIntOrNull()
                            if (mins == null || mins <= 0) {
                                scope.launch { snackbar.showSnackbar("Daily limit must be a positive number") }
                                return@PrimaryButton
                            }
                            for (day in ALL_DAYS) limits[day] = mins
                        }
                        try {
                            dataStore.saveGroup(
                                BlockDataStore.BlockGroup(
                                    groupNameValue,
                                    domains.toList(),
                                    limits,
                                    schedules.toList()
                                )
                            )
                            navController.popBackStack()
                        } catch (e: IllegalStateException) {
                            scope.launch { snackbar.showSnackbar(e.message ?: "Could not save group") }
                        }
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
            OutlinedTextField(
                value = name.value,
                onValueChange = { name.value = it },
                label = { Text("Group name") },
                singleLine = true,
                enabled = isNew,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = limit.value,
                onValueChange = { limit.value = it },
                label = { Text("Daily limit (minutes, optional)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
            )

            SectionHeader("Domains")
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = domainInput.value,
                    onValueChange = {
                        domainInput.value = it
                        domainError.value = ""
                    },
                    label = { Text("Domain") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = {
                    val domain = domainInput.value.trim().lowercase()
                    domainError.value = when {
                        domain.isEmpty() -> "Enter a domain"
                        !domain.contains(".") -> "Invalid domain"
                        domains.contains(domain) -> "Domain already added"
                        else -> {
                            domains.add(domain)
                            domainInput.value = ""
                            ""
                        }
                    }
                }) {
                    Icon(Icons.Default.Add, contentDescription = "Add domain")
                }
            }
            if (domainError.value.isNotEmpty()) {
                Text(
                    text = domainError.value,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            domains.forEachIndexed { index, domain ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .clickable { domains.removeAt(index) },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(domain, modifier = Modifier.weight(1f), fontSize = 14.sp)
                    IconButton(onClick = { domains.removeAt(index) }) {
                        Icon(Icons.Default.Close, contentDescription = "Remove domain")
                    }
                }
                HorizontalDivider()
            }

            SectionHeader("Schedules")
            PrimaryButton(
                text = "Add schedule",
                onClick = { navController.navigate("schedule_edit") },
                modifier = Modifier.fillMaxWidth()
            )
            if (schedules.isEmpty()) {
                Text(
                    text = "No schedules",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }
            schedules.forEachIndexed { index, schedule ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .clickable { schedules.removeAt(index) },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${schedule.days.joinToString(", ")}  ${schedule.from} - ${schedule.to}",
                        modifier = Modifier.weight(1f),
                        fontSize = 13.sp
                    )
                    IconButton(onClick = { schedules.removeAt(index) }) {
                        Icon(Icons.Default.Close, contentDescription = "Remove schedule")
                    }
                }
                HorizontalDivider()
            }
        }
    }
}

private val ALL_DAYS = arrayOf("SATURDAY", "SUNDAY", "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY")
