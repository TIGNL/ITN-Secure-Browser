package com.itn.securebrowser.ui.sheets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

sealed interface SheetItem {
    data class Row(
        val icon: ImageVector?,
        val title: String,
        val subtitle: String? = null,
        val titleColor: Color = Color.Unspecified,
        val trailing: @Composable (() -> Unit)? = null,
        val onClick: (() -> Unit)? = null
    ) : SheetItem

    data class TextField(
        val label: String,
        val value: String,
        val onValueChange: (String) -> Unit,
        val placeholder: String = "",
        val enabled: Boolean = true,
        val keyboardType: KeyboardType = KeyboardType.Text
    ) : SheetItem

    data object Divider : SheetItem

    data class InfoBlock(
        val content: @Composable () -> Unit
    ) : SheetItem
}

/**
 * Renders every sheet currently on the stack as a Material 3 [ModalBottomSheet].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SheetHost(
    stack: SnapshotStateList<Sheet>,
    content: @Composable (sheet: Sheet, isTop: Boolean, dismiss: () -> Unit) -> Unit
) {
    Box(Modifier.fillMaxSize()) {
        stack.forEachIndexed { index, sheet ->
            val isTop = index == stack.lastIndex
            val dismiss: () -> Unit = { stack.remove(sheet) }
            key(sheet) {
                ModalBottomSheet(
                    onDismissRequest = dismiss,
                    sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                    modifier = Modifier.fillMaxHeight(0.9f),
                    dragHandle = { BottomSheetDefaults.DragHandle() },
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    content(sheet, isTop, dismiss)
                }
            }
        }
    }
}

/** Standard sheet layout: title header + close button, divider, then structured items. */
@Composable
fun SheetScaffold(
    title: String,
    onClose: () -> Unit,
    items: List<SheetItem>
) {
    Column(
        Modifier
            .fillMaxHeight()
            .fillMaxWidth()
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 8.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onClose) {
                Icon(Icons.Filled.Close, contentDescription = "Close")
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            items.forEach { item ->
                when (item) {
                    is SheetItem.Row -> {
                        val modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .then(
                                if (item.onClick != null) Modifier.clickable(onClick = item.onClick)
                                else Modifier
                            )
                            .padding(horizontal = 20.dp)

                        Row(
                            modifier,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (item.icon != null) {
                                Box(
                                    modifier = Modifier.size(24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(Modifier.width(16.dp))
                            }
                            Column(Modifier.weight(1f)) {
                                Text(
                                    item.title,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = item.titleColor
                                )
                                if (item.subtitle != null) {
                                    Text(
                                        item.subtitle,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            if (item.trailing != null) {
                                Box(
                                    modifier = Modifier.height(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    item.trailing()
                                }
                            }
                        }
                    }

                    is SheetItem.TextField -> {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .height(64.dp)
                                .padding(horizontal = 20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = item.label,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(Modifier.width(12.dp))
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(32.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                BasicTextField(
                                    value = item.value,
                                    onValueChange = item.onValueChange,
                                    enabled = item.enabled,
                                    singleLine = true,
                                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                                        color = if (item.enabled) MaterialTheme.colorScheme.onSurface
                                        else MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    keyboardOptions = KeyboardOptions(keyboardType = item.keyboardType),
                                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                    modifier = Modifier.fillMaxWidth(),
                                    decorationBox = { innerTextField ->
                                        if (item.value.isEmpty()) {
                                            Text(
                                                text = item.placeholder,
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        innerTextField()
                                    }
                                )
                            }
                        }
                    }

                    is SheetItem.Divider -> {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant,
                            modifier = Modifier.padding(start = 20.dp)
                        )
                    }

                    is SheetItem.InfoBlock -> {
                        item.content()
                    }
                }
            }
        }
    }
}
