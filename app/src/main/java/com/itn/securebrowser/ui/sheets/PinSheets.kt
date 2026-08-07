package com.itn.securebrowser.ui.sheets

import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
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
                onValueChange = { new ->
                    if (sheet.pinLength == 0 || new.length <= sheet.pinLength) entered = new
                },
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
