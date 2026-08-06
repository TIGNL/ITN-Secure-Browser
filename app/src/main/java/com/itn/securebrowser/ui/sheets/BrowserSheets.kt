package com.itn.securebrowser.ui.sheets

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Monitor
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.itn.securebrowser.MainActivity
import com.itn.securebrowser.R

@Composable
fun TabsSheet(sheet: Sheet.Tabs, activity: MainActivity, dismiss: () -> Unit) {
    val count = sheet.tabs.size
    val title = LocalContext.current.resources
        .getQuantityString(R.plurals.tab_count, count, count)

    val items = buildList {
        if (count == 0) {
            add(SheetItem.InfoBlock {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        "No open tabs",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            })
        } else {
            sheet.tabs.forEach { tab ->
                val active = tab.id == sheet.activeId
                add(
                    SheetItem.Row(
                        icon = null,
                        title = tab.title.ifBlank { stringResource(R.string.new_tab) },
                        titleColor = if (active) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        trailing = {
                            Icon(
                                Icons.Filled.Close,
                                contentDescription = stringResource(R.string.cd_close_tab)
                            )
                        },
                        trailingClick = { activity.closeTab(tab.id); dismiss() },
                        onClick = { activity.switchToTab(tab.id); dismiss() }
                    )
                )
            }
        }
        add(SheetItem.Divider)
        add(
            SheetItem.Row(
                icon = Icons.Filled.Add,
                title = stringResource(R.string.cd_new_tab),
                onClick = { activity.createNewTab(); dismiss() }
            )
        )
    }

    SheetScaffold(
        title = title,
        onClose = dismiss,
        items = items
    )
}

@Composable
fun MoreSheet(activity: MainActivity, dismiss: () -> Unit, push: (Sheet) -> Unit) {
    SheetScaffold(
        title = stringResource(R.string.sheet_options),
        onClose = dismiss,
        items = listOf(
            SheetItem.Row(
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
            ),
            SheetItem.Divider,
            SheetItem.Row(
                icon = Icons.Filled.Security,
                title = stringResource(R.string.parental_settings),
                onClick = { push(Sheet.Parental) }
            ),
            SheetItem.Divider,
            SheetItem.Row(
                icon = Icons.Filled.Settings,
                title = stringResource(R.string.general_settings),
                onClick = { push(Sheet.General) }
            )
        )
    )
}
