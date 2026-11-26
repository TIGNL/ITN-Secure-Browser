package com.itn.securebrowser.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.itn.securebrowser.BrowserSession
import com.itn.securebrowser.DesignVariant
import com.itn.securebrowser.LocalDesignVariant

@Composable
fun TabListScreen(navController: NavController) {
    val context = LocalContext.current
    val pendingClose = remember { mutableStateOf<Int?>(null) }

    Scaffold(
        topBar = { ScreenTopBar("Tabs", onBack = { navController.popBackStack() }) },
        floatingActionButton = {
            if (LocalDesignVariant.current == DesignVariant.FULL) {
                ExtendedFloatingActionButton(
                    onClick = {
                        BrowserSession.createNewTab(context)
                        navController.popBackStack()
                    },
                    icon = { Icon(Icons.Default.Add, contentDescription = "New tab") },
                    text = { Text("New tab") }
                )
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            itemsIndexed(BrowserSession.tabs) { _, tab ->
                TabRow(
                    title = if (tab.id == BrowserSession.currentTabId) "▶ ${tab.title}" else tab.title,
                    onClick = {
                        BrowserSession.switchToTab(tab.id)
                        navController.popBackStack()
                    },
                    onClose = { pendingClose.value = tab.id }
                )
            }
        }
    }

    pendingClose.value?.let { id ->
        ConfirmDialog(
            message = "Close this tab?",
            onConfirm = {
                BrowserSession.closeTab(context, id)
                pendingClose.value = null
            },
            onDismiss = { pendingClose.value = null }
        )
    }
}

@Composable
private fun TabRow(title: String, onClick: () -> Unit, onClose: () -> Unit) {
    when (LocalDesignVariant.current) {
        DesignVariant.FULL -> ListItem(
            headlineContent = {
                Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis)
            },
            trailingContent = {
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Close tab")
                }
            },
            modifier = Modifier.clickable(onClick = onClick)
        )

        DesignVariant.FAITHFUL -> {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clickable(onClick = onClick),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Close tab")
                }
            }
            HorizontalDivider()
        }
    }
}
