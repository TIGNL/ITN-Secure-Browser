package com.itn.securebrowser.ui.sheets

import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.itn.securebrowser.BlockDataStore
import com.itn.securebrowser.BlockGroup
import com.itn.securebrowser.BlockSchedule
import com.itn.securebrowser.R

private const val ALL_DAYS =
    "SATURDAY,SUNDAY,MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY"

private fun toast(context: android.content.Context, msg: String) =
    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()

@Composable
fun ParentalSheet(dismiss: () -> Unit, push: (Sheet) -> Unit) {
    SheetScaffold(title = stringResource(R.string.parental_settings), onClose = dismiss) {
        SheetRow(
            icon = Icons.Filled.Lock,
            title = stringResource(R.string.tab_pin),
            onClick = { push(Sheet.Pin) }
        )
        SheetDivider()
        SheetRow(
            icon = Icons.Filled.Schedule,
            title = stringResource(R.string.tab_groups),
            onClick = { push(Sheet.Groups) }
        )
    }
}

@Composable
fun GeneralSheet(dismiss: () -> Unit) {
    SheetScaffold(title = stringResource(R.string.general_settings), onClose = dismiss) {
        Column(
            Modifier
                .weight(1f)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Filled.Public,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(56.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "No settings available",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun GroupsSheet(dismiss: () -> Unit, push: (Sheet) -> Unit, isTop: Boolean) {
    val context = LocalContext.current
    val store = remember { BlockDataStore(context) }
    var groups by remember { mutableStateOf(store.getGroups()) }

    LaunchedEffect(isTop) {
        if (isTop) groups = store.getGroups()
    }

    SheetScaffold(title = stringResource(R.string.tab_groups), onClose = dismiss) {
        if (groups.isEmpty()) {
            Column(
                Modifier
                    .weight(1f)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Filled.Public,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(56.dp)
                )
                Spacer(Modifier.height(16.dp))
                Text(stringResource(R.string.groups_empty_title), style = MaterialTheme.typography.titleMedium)
                Text(
                    stringResource(R.string.groups_empty_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(Modifier.weight(1f)) {
                items(groups, key = { it.name }) { group ->
                    SheetRow(
                        icon = null,
                        title = group.name,
                        subtitle = domainsPreview(group.domains),
                        onClick = { push(Sheet.GroupEdit(group.name)) },
                        trailing = {
                            IconButton(onClick = {
                                push(Sheet.DeleteGroup(group.name) {
                                    store.deleteGroup(group.name)
                                    groups = store.getGroups()
                                })
                            }) {
                                Icon(
                                    Icons.Filled.Delete,
                                    contentDescription = stringResource(R.string.cd_delete_group),
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    )
                    SheetDivider()
                }
            }
        }

        TextButton(
            onClick = { push(Sheet.GroupEdit(null)) },
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(vertical = 8.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = null)
            Text(stringResource(R.string.btn_add_group))
        }
    }
}

private fun domainsPreview(domains: List<String>): String = when (domains.size) {
    0 -> ""
    1 -> domains[0]
    2 -> "${domains[0]}, ${domains[1]}"
    else -> "${domains[0]}, ${domains[1]} +${domains.size - 2}"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupEditSheet(sheet: Sheet.GroupEdit, dismiss: () -> Unit, push: (Sheet) -> Unit) {
    val context = LocalContext.current
    val store = remember { BlockDataStore(context) }
    val editing = sheet.groupName

    var name by rememberSaveable { mutableStateOf(editing ?: "") }
    var limit by rememberSaveable { mutableStateOf("") }
    val domains = remember { mutableStateListOf<String>() }
    val schedules = remember { mutableStateListOf<BlockSchedule>() }

    val dayNames = mapOf(
        "SATURDAY" to stringResource(R.string.day_saturday),
        "SUNDAY" to stringResource(R.string.day_sunday),
        "MONDAY" to stringResource(R.string.day_monday),
        "TUESDAY" to stringResource(R.string.day_tuesday),
        "WEDNESDAY" to stringResource(R.string.day_wednesday),
        "THURSDAY" to stringResource(R.string.day_thursday),
        "FRIDAY" to stringResource(R.string.day_friday)
    )

    LaunchedEffect(editing) {
        if (editing != null) {
            val group = store.getGroups().find { it.name == editing } ?: return@LaunchedEffect
            name = group.name
            limit = group.dailyLimits.values.firstOrNull()?.toString() ?: ""
            domains.clear()
            domains.addAll(group.domains)
            schedules.clear()
            schedules.addAll(group.schedules)
        }
    }

    fun trySave() {
        val nameT = name.trim()
        if (nameT.isBlank()) { toast(context, context.getString(R.string.err_enter_group_name)); return }

        val rawLimit = limit.trim()
        val limitMins: Int? = if (rawLimit.isBlank()) null else {
            val n = rawLimit.toIntOrNull()
            if (n == null || n <= 0) {
                toast(context, context.getString(R.string.err_daily_limit_positive)); return
            }
            n
        }

        val dailyLimits: Map<String, Int> =
            if (limitMins != null) ALL_DAYS.split(",").associateWith { limitMins } else emptyMap()

        try {
            store.saveGroup(
                BlockGroup(
                    name = nameT,
                    domains = domains.toList(),
                    dailyLimits = dailyLimits,
                    schedules = schedules.toList()
                )
            )
            dismiss()
        } catch (e: IllegalStateException) {
            toast(context, e.message ?: context.getString(R.string.err_save))
        }
    }

    SheetScaffold(
        title = stringResource(if (editing != null) R.string.edit_group_title else R.string.new_group_title),
        onClose = dismiss
    ) {
        LazyColumn(Modifier.weight(1f)) {
            item {
                TextFieldRow(
                    label = stringResource(R.string.group_name_label),
                    value = name,
                    onValueChange = { if (editing == null) name = it },
                    placeholder = stringResource(R.string.group_name_hint),
                    enabled = editing == null
                )
                SheetDivider()
                TextFieldRow(
                    label = stringResource(R.string.group_daily_limit_label),
                    value = limit,
                    onValueChange = { limit = it },
                    placeholder = stringResource(R.string.group_daily_limit_hint),
                    keyboardType = KeyboardType.Number
                )
                SheetDivider()
                SheetRow(icon = Icons.Filled.Public, title = stringResource(R.string.domains_label))
            }
            if (domains.isEmpty()) {
                item {
                    SheetDivider()
                    SheetRow(
                        icon = null,
                        title = stringResource(R.string.domains_empty_hint),
                        titleColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                itemsIndexed(domains) { index, domain ->
                    SheetDivider()
                    SheetRow(
                        icon = null,
                        title = domain,
                        trailing = {
                            IconButton(onClick = { domains.removeAt(index) }) {
                                Icon(
                                    Icons.Filled.Close,
                                    contentDescription = stringResource(R.string.cd_delete_domain),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    )
                }
            }
            item {
                SheetDivider()
                SheetRow(
                    icon = Icons.Filled.Add,
                    title = stringResource(R.string.btn_add_domain),
                    onClick = { push(Sheet.AddDomain(domains.toList()) { d -> domains.add(d) }) }
                )
                SheetDivider()
                SheetRow(icon = Icons.Filled.Schedule, title = stringResource(R.string.schedules_label))
            }
            if (schedules.isEmpty()) {
                item {
                    SheetDivider()
                    SheetRow(
                        icon = null,
                        title = stringResource(R.string.schedules_empty_hint),
                        titleColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                itemsIndexed(schedules) { index, schedule ->
                    SheetDivider()
                    SheetRow(
                        icon = null,
                        title = schedule.days.joinToString(", ") { dayNames[it] ?: it },
                        subtitle = "${schedule.from} — ${schedule.to}",
                        trailing = {
                            IconButton(onClick = { schedules.removeAt(index) }) {
                                Icon(
                                    Icons.Filled.Close,
                                    contentDescription = stringResource(R.string.cd_delete_schedule),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    )
                }
            }
            item {
                SheetDivider()
                SheetRow(
                    icon = Icons.Filled.Add,
                    title = stringResource(R.string.btn_add_schedule),
                    onClick = { push(Sheet.AddSchedule { s -> schedules.add(s) }) }
                )
            }
        }
        SheetDivider()
        SheetRow(
            icon = null,
            title = stringResource(R.string.btn_save),
            titleColor = MaterialTheme.colorScheme.primary,
            onClick = { trySave() }
        )
    }
}

@Composable
fun AddDomainSheet(sheet: Sheet.AddDomain, dismiss: () -> Unit) {
    val context = LocalContext.current
    var input by rememberSaveable { mutableStateOf("") }

    SheetScaffold(title = stringResource(R.string.add_domain_title), onClose = dismiss) {
        TextFieldRow(
            label = stringResource(R.string.add_domain_hint),
            value = input,
            onValueChange = { input = it },
            placeholder = "e.g. instagram.com",
            keyboardType = KeyboardType.Uri
        )
        SheetDivider()
        SheetRow(
            icon = Icons.Filled.Add,
            title = stringResource(R.string.btn_add),
            titleColor = MaterialTheme.colorScheme.primary,
            onClick = {
                val raw = input.trim()
                    .removePrefix("https://").removePrefix("http://")
                    .removePrefix("www.").trimEnd('/').lowercase()
                when {
                    raw.isBlank() -> toast(context, context.getString(R.string.err_domain_blank))
                    !raw.contains('.') -> toast(context, context.getString(R.string.err_domain_invalid))
                    raw in sheet.existing -> toast(context, context.getString(R.string.err_domain_duplicate))
                    else -> { sheet.onAdd(raw); dismiss() }
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddScheduleSheet(sheet: Sheet.AddSchedule, dismiss: () -> Unit) {
    val context = LocalContext.current

    var days by remember { mutableStateOf(setOf<String>()) }
    var fromHour by remember { mutableIntStateOf(20) }
    var fromMin by remember { mutableIntStateOf(0) }
    var toHour by remember { mutableIntStateOf(23) }
    var toMin by remember { mutableIntStateOf(0) }

    fun fmt(h: Int, m: Int) = "%02d:%02d".format(h, m)

    val dayLabels = listOf(
        "SATURDAY" to stringResource(R.string.day_saturday),
        "SUNDAY" to stringResource(R.string.day_sunday),
        "MONDAY" to stringResource(R.string.day_monday),
        "TUESDAY" to stringResource(R.string.day_tuesday),
        "WEDNESDAY" to stringResource(R.string.day_wednesday),
        "THURSDAY" to stringResource(R.string.day_thursday),
        "FRIDAY" to stringResource(R.string.day_friday)
    )

    SheetScaffold(title = stringResource(R.string.add_schedule_title), onClose = dismiss) {
        LazyColumn(Modifier.weight(1f)) {
            item {
                SheetRow(icon = Icons.Filled.Schedule, title = stringResource(R.string.schedule_days_label))
            }
            items(dayLabels) { (key, label) ->
                SheetDivider()
                SheetRow(
                    icon = null,
                    title = label,
                    trailing = {
                        androidx.compose.material3.Checkbox(
                            checked = key in days,
                            onCheckedChange = null
                        )
                    },
                    onClick = { days = if (key in days) days - key else days + key }
                )
            }
            item {
                SheetDivider()
                SheetRow(icon = Icons.Filled.Schedule, title = stringResource(R.string.schedule_time_label))
                SheetDivider()
                SheetRow(
                    icon = null,
                    title = stringResource(R.string.schedule_from),
                    trailing = {
                        Text(
                            fmt(fromHour, fromMin),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    },
                    onClick = {
                        TimePickerDialog(context, { _, h, m -> fromHour = h; fromMin = m }, fromHour, fromMin, true).show()
                    }
                )
                SheetDivider()
                SheetRow(
                    icon = null,
                    title = stringResource(R.string.schedule_to),
                    trailing = {
                        Text(
                            fmt(toHour, toMin),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    },
                    onClick = {
                        TimePickerDialog(context, { _, h, m -> toHour = h; toMin = m }, toHour, toMin, true).show()
                    }
                )
            }
        }
        SheetDivider()
        SheetRow(
            icon = null,
            title = stringResource(R.string.btn_ok),
            titleColor = MaterialTheme.colorScheme.primary,
            onClick = {
                val selectedDays = ALL_DAYS.split(",").filter { it in days }
                if (selectedDays.isEmpty()) {
                    toast(context, context.getString(R.string.err_select_day))
                } else {
                    sheet.onAdd(BlockSchedule(days = selectedDays, from = fmt(fromHour, fromMin), to = fmt(toHour, toMin)))
                    dismiss()
                }
            }
        )
    }
}

@Composable
fun DeleteGroupSheet(sheet: Sheet.DeleteGroup, dismiss: () -> Unit) {
    val context = LocalContext.current
    SheetScaffold(title = stringResource(R.string.delete_group_title), onClose = dismiss) {
        SheetRow(
            icon = null,
            title = stringResource(R.string.delete_group_message, sheet.groupName),
            titleColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
        SheetDivider()
        SheetRow(
            icon = Icons.Filled.Delete,
            title = stringResource(R.string.btn_delete),
            titleColor = MaterialTheme.colorScheme.error,
            onClick = { sheet.onConfirm(); dismiss() }
        )
    }
}
