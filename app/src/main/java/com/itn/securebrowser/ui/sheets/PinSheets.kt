package com.itn.securebrowser.ui.sheets

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.itn.securebrowser.PinManager
import com.itn.securebrowser.R

@Composable
fun PinSheet(dismiss: () -> Unit, push: (Sheet) -> Unit) {
    val context = LocalContext.current
    var hasPin by remember { mutableStateOf(PinManager.hasPin(context)) }

    fun pushVerifyThenClear() {
        push(
            Sheet.PinEntry(
                mode = MODE_VERIFY,
                subtitle = context.getString(R.string.pin_verify_first),
                onVerified = {
                    PinManager.clear(context)
                    hasPin = false
                },
                onDismissed = { hasPin = PinManager.hasPin(context) }
            )
        )
    }

    val items = buildList {
        add(
            SheetItem.Row(
                icon = null,
                title = "PIN",
                onClick = {
                    if (hasPin) pushVerifyThenClear()
                    else push(Sheet.LockMethod { type -> pushEntryForType(type, push) { hasPin = PinManager.hasPin(context) } })
                },
                trailing = {
                    Switch(
                        checked = hasPin,
                        onCheckedChange = { enabled ->
                            if (enabled) push(Sheet.LockMethod { type -> pushEntryForType(type, push) { hasPin = PinManager.hasPin(context) } })
                            else pushVerifyThenClear()
                        }
                    )
                }
            )
        )
        add(SheetItem.Divider)
        if (hasPin) {
            add(
                SheetItem.Row(
                    icon = null,
                    title = stringResource(R.string.btn_change_pin),
                    onClick = {
                        push(
                            Sheet.PinEntry(
                                mode = MODE_VERIFY,
                                subtitle = context.getString(R.string.pin_verify_first),
                                onVerified = {
                                    push(Sheet.LockMethod { type -> pushEntryForType(type, push) { hasPin = PinManager.hasPin(context) } })
                                }
                            )
                        )
                    }
                )
            )
        }
    }

    SheetScaffold(
        title = "PIN",
        onClose = dismiss,
        items = items
    )
}

private fun pushEntryForType(
    type: LockType,
    push: (Sheet) -> Unit,
    onVerified: () -> Unit
) {
    when (type) {
        LockType.PASSWORD -> push(
            Sheet.PinEntry(
                mode = MODE_SET,
                subtitle = "Choose a password",
                pinLength = 0,
                onVerified = onVerified
            )
        )
        LockType.PIN_4 -> push(
            Sheet.PinEntry(
                mode = MODE_SET,
                subtitle = "Choose a 4-digit PIN",
                pinLength = 4,
                onVerified = onVerified
            )
        )
        LockType.PIN_6 -> push(
            Sheet.PinEntry(
                mode = MODE_SET,
                subtitle = "Choose a 6-digit PIN",
                pinLength = 6,
                onVerified = onVerified
            )
        )
        LockType.PATTERN -> push(
            Sheet.PatternEntry(
                mode = MODE_SET,
                subtitle = "Draw a pattern",
                onVerified = onVerified
            )
        )
    }
}

@Composable
fun LockMethodSheet(sheet: Sheet.LockMethod, dismiss: () -> Unit) {
    val items = buildList {
        add(
            SheetItem.Row(
                icon = null,
                title = "Password",
                subtitle = "Text password",
                onClick = { sheet.onSelected(LockType.PASSWORD); dismiss() }
            )
        )
        add(SheetItem.Divider)
        add(
            SheetItem.Row(
                icon = null,
                title = "4-Digit PIN",
                subtitle = "Numeric, 4 digits",
                onClick = { sheet.onSelected(LockType.PIN_4); dismiss() }
            )
        )
        add(SheetItem.Divider)
        add(
            SheetItem.Row(
                icon = null,
                title = "6-Digit PIN",
                subtitle = "Numeric, 6 digits",
                onClick = { sheet.onSelected(LockType.PIN_6); dismiss() }
            )
        )
        add(SheetItem.Divider)
        add(
            SheetItem.Row(
                icon = null,
                title = "Pattern",
                subtitle = "3x3 dot pattern",
                onClick = { sheet.onSelected(LockType.PATTERN); dismiss() }
            )
        )
    }

    SheetScaffold(
        title = "Lock Method",
        onClose = dismiss,
        items = items
    )
}

@Composable
fun PinEntrySheet(
    sheet: Sheet.PinEntry,
    dismiss: () -> Unit,
    onDismissed: () -> Unit = sheet.onDismissed
) {
    val context = LocalContext.current
    var entered by remember { mutableStateOf("") }
    var firstPin by remember { mutableStateOf("") }
    var titleText by remember {
        mutableStateOf(
            when (sheet.mode) {
                MODE_SET -> context.getString(R.string.pin_title_new)
                else -> context.getString(R.string.pin_title_enter)
            }
        )
    }
    var pinSucceeded by remember { mutableStateOf(false) }

    androidx.compose.runtime.DisposableEffect(Unit) {
        onDispose { if (!pinSucceeded) onDismissed() }
    }

    fun handleComplete(pin: String) {
        when (sheet.mode) {
            MODE_VERIFY -> {
                if (PinManager.verify(context, pin)) {
                    pinSucceeded = true
                    sheet.onVerified()
                    dismiss()
                } else {
                    titleText = context.getString(R.string.pin_error_wrong)
                    entered = ""
                }
            }

            MODE_SET -> {
                if (firstPin.isEmpty()) {
                    firstPin = pin
                    entered = ""
                    titleText = context.getString(R.string.pin_title_confirm)
                } else {
                    if (pin == firstPin) {
                        pinSucceeded = true
                        PinManager.savePin(context, pin)
                        sheet.onVerified()
                        dismiss()
                    } else {
                        firstPin = ""
                        titleText = context.getString(R.string.pin_error_mismatch)
                        entered = ""
                    }
                }
            }
        }
    }

    val pinFocusRequester = remember { FocusRequester() }

    val isPassword = sheet.pinLength == 0
    val placeholder = if (isPassword) "Enter password" else "Enter PIN"
    val keyboardType = if (isPassword) KeyboardType.Password else KeyboardType.Number

    val items = buildList {
        add(
            SheetItem.TextField(
                label = "",
                value = entered,
                onValueChange = { entered = it },
                placeholder = placeholder,
                keyboardType = keyboardType,
                isPassword = true,
                focusRequester = pinFocusRequester
            )
        )
        add(SheetItem.BottomBar.Action(
            label = stringResource(R.string.btn_ok),
            onClick = { handleComplete(entered) }
        ))
    }

    SheetScaffold(title = titleText, onClose = dismiss, items = items)
}

@Composable
fun PatternEntrySheet(
    sheet: Sheet.PatternEntry,
    dismiss: () -> Unit,
    onDismissed: () -> Unit = sheet.onDismissed
) {
    val context = LocalContext.current
    val dots = remember { mutableStateListOf<Int>() }
    var fingerPos by remember { mutableStateOf<Offset?>(null) }
    var titleText by remember {
        mutableStateOf(
            when (sheet.mode) {
                MODE_SET -> "Draw a pattern"
                else -> "Enter your pattern"
            }
        )
    }
    var firstPattern by remember { mutableStateOf<List<Int>>(emptyList()) }
    var pinSucceeded by remember { mutableStateOf(false) }
    var patternComplete by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose { if (!pinSucceeded) onDismissed() }
    }

    fun handleConfirm() {
        when (sheet.mode) {
            MODE_VERIFY -> {
                pinSucceeded = true
                sheet.onVerified()
                dismiss()
            }
            MODE_SET -> {
                if (firstPattern.isEmpty()) {
                    firstPattern = dots.toList()
                    dots.clear()
                    patternComplete = false
                    titleText = "Redraw pattern to confirm"
                } else {
                    if (dots.toList() == firstPattern) {
                        pinSucceeded = true
                        PinManager.savePin(context, dots.joinToString(","))
                        sheet.onVerified()
                        dismiss()
                    } else {
                        firstPattern = emptyList()
                        dots.clear()
                        patternComplete = false
                        titleText = context.getString(R.string.pin_error_mismatch)
                    }
                }
            }
        }
    }

    val dotPositions = remember { mutableStateMapOf<Int, Offset>() }
    val dotRadius = 24.dp
    val cellSize = 64.dp

    val items = buildList {
        add(SheetItem.InfoBlock {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (sheet.subtitle.isNotEmpty() && firstPattern.isEmpty()) {
                    Text(
                        text = sheet.subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 16.dp),
                        textAlign = TextAlign.Center
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(cellSize * 3)
                        .pointerInput(dots.toList(), patternComplete) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    if (!patternComplete) {
                                        dots.clear()
                                        patternComplete = false
                                        fingerPos = offset
                                        for ((idx, pos) in dotPositions) {
                                            if ((pos - offset).getDistance() < dotRadius.toPx() * 1.5f) {
                                                if (idx !in dots) dots.add(idx)
                                                break
                                            }
                                        }
                                    }
                                },
                                onDrag = { change, _ ->
                                    if (!patternComplete) {
                                        change.consume()
                                        fingerPos = change.position
                                        for ((idx, pos) in dotPositions) {
                                            if ((pos - change.position).getDistance() < dotRadius.toPx() * 1.5f) {
                                                if (idx !in dots) dots.add(idx)
                                                break
                                            }
                                        }
                                    }
                                },
                                onDragEnd = {
                                    if (!patternComplete && dots.isNotEmpty()) {
                                        patternComplete = true
                                        fingerPos = null
                                    }
                                },
                                onDragCancel = {
                                    fingerPos = null
                                }
                            )
                        }
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height
                        val cols = 4
                        val rows = 4
                        for (row in 0..2) {
                            for (col in 0..2) {
                                val idx = row * 3 + col
                                val cx = w * (col + 1) / cols
                                val cy = h * (row + 1) / rows
                                dotPositions[idx] = Offset(cx, cy)
                            }
                        }

                        val lineColor = Color(0xFF6750A4)
                        for (i in 0 until dots.size - 1) {
                            val from = dotPositions[dots[i]] ?: continue
                            val to = dotPositions[dots[i + 1]] ?: continue
                            drawLine(lineColor, from, to, strokeWidth = 4.dp.toPx())
                        }
                        if (fingerPos != null && dots.isNotEmpty()) {
                            val last = dotPositions[dots.last()] ?: return@Canvas
                            drawLine(lineColor, last, fingerPos!!, strokeWidth = 4.dp.toPx())
                        }

                        for ((idx, center) in dotPositions) {
                            val isSelected = idx in dots
                            drawCircle(
                                color = if (isSelected) lineColor else Color(0xFF49454F),
                                radius = dotRadius.toPx(),
                                center = center
                            )
                            if (!isSelected) {
                                drawCircle(
                                    color = Color(0xFF938F99),
                                    radius = dotRadius.toPx(),
                                    center = center,
                                    style = Stroke(2.dp.toPx())
                                )
                            }
                        }
                    }
                }

                if (patternComplete) {
                    Spacer(Modifier.height(16.dp))
                }
            }
        })
        add(SheetItem.Divider)
        add(SheetItem.BottomBar.Action(
            label = stringResource(R.string.btn_ok),
            onClick = { if (dots.isNotEmpty() && patternComplete) handleConfirm() }
        ))
    }

    SheetScaffold(title = titleText, onClose = dismiss, items = items)
}
