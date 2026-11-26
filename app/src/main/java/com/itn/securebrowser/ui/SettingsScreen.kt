package com.itn.securebrowser.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.itn.securebrowser.util.BlockDataStore
import com.itn.securebrowser.util.PinManager

/**
 * Chrome Android settings layout: top bar, rounded search pill, blue section
 * headers and clean rows with trailing values. All entries keep their existing
 * ITN Secure Browser destinations.
 */
@Composable
fun SettingsScreen(navController: NavController) {
    val context = LocalContext.current
    val dataStore = remember { BlockDataStore(context) }
    val groupCount = remember { mutableStateOf(0) }
    val query = remember { mutableStateOf("") }
    val showAbout = remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { groupCount.value = dataStore.getGroups().size }

    data class SettingItem(
        val section: String,
        val title: String,
        val subtitle: String? = null,
        val value: String? = null,
        val icon: ImageVector? = null,
        val action: (() -> Unit)? = null
    )

    val pinEnabled = PinManager.hasPin(context)
    val rows = listOf(
        SettingItem(
            section = "You and ITN",
            title = "App version",
            value = "2.0-compose",
            icon = Icons.Default.Info
        ),
        SettingItem(
            section = "Basics",
            title = "Search engine",
            value = "Google",
            icon = Icons.Default.Search
        ),
        SettingItem(
            section = "Basics",
            title = "Address bar",
            value = "Top"
        ),
        SettingItem(
            section = "Basics",
            title = "PIN Lock",
            subtitle = if (pinEnabled) "PIN is enabled" else "No PIN set",
            value = if (pinEnabled) "On" else "Off",
            icon = Icons.Default.Lock,
            action = {
                navController.navigate(
                    if (pinEnabled) "pin_options" else "pin_setup"
                )
            }
        ),
        SettingItem(
            section = "Basics",
            title = "Limit websites time",
            subtitle = "${groupCount.value} group(s)",
            icon = Icons.Default.List,
            action = { navController.navigate("groups") }
        ),
        SettingItem(
            section = "Privacy and security",
            title = "Clear browsing data",
            subtitle = "Cookies, cache and site storage",
            icon = Icons.Default.Delete,
            action = { navController.navigate("manage_space") }
        ),
        SettingItem(
            section = "Privacy and security",
            title = "Manage app data",
            icon = Icons.Default.Settings,
            action = { navController.navigate("manage_space") }
        ),
        SettingItem(
            section = "Advanced",
            title = "About ITN Browser",
            icon = Icons.Default.Info,
            action = { showAbout.value = true }
        )
    )

    val filtered = rows.filter { row ->
        query.value.isBlank() || listOf(row.section, row.title, row.subtitle, row.value)
            .any { it?.contains(query.value, ignoreCase = true) == true }
    }
    val grouped = filtered.groupBy { it.section }

    Scaffold(
        topBar = {
            ChromeTopBar(
                title = "Settings",
                onBack = { navController.popBackStack() },
                actions = {
                    IconButton(onClick = { showAbout.value = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More options")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            item { ChromeSearchBar(query = query.value, onQueryChange = { query.value = it }) }

            if (query.value.isBlank() || "ITN Browser".contains(query.value, ignoreCase = true)) {
                item {
                    ChromeAccountRow(
                        name = "ITN Browser",
                        subtitle = "Local profile · no account required"
                    )
                }
            }

            grouped.forEach { (section, sectionItems) ->
                item(key = "header_$section") { ChromeSectionHeader(section) }
                items(sectionItems, key = { it.title }) { row ->
                    ChromeSettingsRow(
                        title = row.title,
                        subtitle = row.subtitle,
                        value = row.value,
                        icon = row.icon,
                        onClick = row.action
                    )
                }
            }

            item { androidx.compose.foundation.layout.Spacer(Modifier.height(24.dp)) }
        }
    }

    if (showAbout.value) {
        AlertDialog(
            onDismissRequest = { showAbout.value = false },
            title = { Text("ITN Browser") },
            text = {
                Text(
                    "Version 2.0-compose\n\n" +
                        "A secure WebView browser with a Chrome-inspired Android UI."
                )
            },
            confirmButton = {
                TextButton(onClick = { showAbout.value = false }) { Text("OK") }
            }
        )
    }
}
