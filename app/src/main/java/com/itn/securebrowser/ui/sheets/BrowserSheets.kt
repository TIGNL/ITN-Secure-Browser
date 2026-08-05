package com.itn.securebrowser.ui.sheets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Monitor
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.itn.securebrowser.MainActivity
import com.itn.securebrowser.R

@Composable
fun TabsSheet(sheet: Sheet.Tabs, activity: MainActivity, dismiss: () -> Unit) {
    val count = sheet.tabs.size
    val title = LocalContext.current.resources
        .getQuantityString(R.plurals.tab_count, count, count)

    SheetScaffold(title = title, onClose = dismiss) {
        if (count == 0) {
            Column(
                Modifier
                    .weight(1f)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
            ) {
                Text("No open tabs", style = MaterialTheme.typography.titleMedium)
            }
        } else {
            LazyColumn(Modifier.weight(1f)) {
                items(sheet.tabs, key = { it.id }) { tab ->
                    val active = tab.id == sheet.activeId
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .background(
                                if (active) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                                else Color.Transparent
                            )
                            .clickable {
                                activity.switchToTab(tab.id)
                                dismiss()
                            }
                            .padding(start = 20.dp, end = 8.dp, top = 10.dp, bottom = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = tab.title.ifBlank { stringResource(R.string.new_tab) },
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (active) MaterialTheme.colorScheme.onSurface
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = {
                            activity.closeTab(tab.id)
                            dismiss()
                        }) {
                            Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.cd_close_tab))
                        }
                    }
                }
            }
        }

        TextButton(
            onClick = {
                activity.createNewTab()
                dismiss()
            },
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(vertical = 8.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = null)
            Text(stringResource(R.string.cd_new_tab))
        }
    }
}

@Composable
fun MoreSheet(activity: MainActivity, dismiss: () -> Unit, push: (Sheet) -> Unit) {
    SheetScaffold(title = stringResource(R.string.sheet_options), onClose = dismiss) {
        SheetRow(
            icon = Icons.Filled.Monitor,
            title = stringResource(R.string.desktop_mode),
            trailing = {
                Switch(
                    checked = activity.isDesktopMode,
                    onCheckedChange = { enabled ->
                        activity.setDesktopMode(enabled)
                        dismiss()
                    }
                )
            },
            onClick = {
                activity.setDesktopMode(!activity.isDesktopMode)
                dismiss()
            }
        )
        SheetDivider()
        SheetRow(
            icon = Icons.Filled.Security,
            title = stringResource(R.string.parental_settings),
            onClick = { push(Sheet.Parental) }
        )
        SheetDivider()
        SheetRow(
            icon = Icons.Filled.Settings,
            title = stringResource(R.string.general_settings),
            onClick = { push(Sheet.General) }
        )
    }
}
