package com.itn.securebrowser.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.itn.securebrowser.DesignVariant
import com.itn.securebrowser.LocalDesignVariant
import com.itn.securebrowser.groupEditRoute
import com.itn.securebrowser.util.BlockDataStore

@Composable
fun GroupListScreen(navController: NavController) {
    val context = LocalContext.current
    val dataStore = remember { BlockDataStore(context) }
    val groups = remember { mutableStateOf(dataStore.getGroups()) }
    val pendingDelete = remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) { groups.value = dataStore.getGroups() }

    Scaffold(
        topBar = { ScreenTopBar("Limit websites time", onBack = { navController.popBackStack() }) },
        floatingActionButton = {
            if (LocalDesignVariant.current == DesignVariant.FULL) {
                ExtendedFloatingActionButton(
                    onClick = { navController.navigate(groupEditRoute("", true)) },
                    icon = { Icon(Icons.Default.Add, contentDescription = "Add group") },
                    text = { Text("Add group") }
                )
            }
        },
        bottomBar = {
            if (LocalDesignVariant.current == DesignVariant.FAITHFUL) {
                PinnedBottomBar {
                    PrimaryButton(
                        text = "Add",
                        onClick = { navController.navigate(groupEditRoute("", true)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    ) { padding ->
        if (groups.value.isEmpty()) {
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) { Text("No groups yet", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        } else {
            LazyColumn(modifier = Modifier.padding(padding)) {
                itemsIndexed(groups.value) { _, group ->
                    GroupRow(
                        label = "${group.name}  (${group.domains.size} domains)",
                        onClick = { navController.navigate(groupEditRoute(group.name, false)) },
                        onDelete = { pendingDelete.value = group.name }
                    )
                }
            }
        }
    }

    pendingDelete.value?.let { name ->
        ConfirmDialog(
            message = "Delete group \"$name\"?",
            onConfirm = {
                dataStore.deleteGroup(name)
                groups.value = dataStore.getGroups()
                pendingDelete.value = null
            },
            onDismiss = { pendingDelete.value = null }
        )
    }
}

@Composable
private fun GroupRow(label: String, onClick: () -> Unit, onDelete: () -> Unit) {
    when (LocalDesignVariant.current) {
        DesignVariant.FULL -> ListItem(
            headlineContent = { Text(label) },
            trailingContent = {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete group")
                }
            },
            modifier = Modifier.clickable(onClick = onClick)
        )

        DesignVariant.FAITHFUL -> Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clickable(onClick = onClick),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete group")
                }
            }
            HorizontalDivider()
        }
    }
}
